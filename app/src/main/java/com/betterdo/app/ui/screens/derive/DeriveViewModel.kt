package com.betterdo.app.ui.screens.derive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.betterdo.app.data.prefs.Settings
import com.betterdo.app.di.AppContainer
import com.betterdo.app.domain.model.DerivedSuggestion
import com.betterdo.app.domain.model.Todo
import com.betterdo.app.domain.model.TodoSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class CardStatus { PENDING, ACCEPTED, IGNORED }

data class DeriveState(
    val scanning: Boolean = true,
    val round: Int = 1,
    val cards: List<DerivedSuggestion> = emptyList(),
    val statuses: Map<String, CardStatus> = emptyMap(),
) {
    val accepted: Int get() = statuses.values.count { it == CardStatus.ACCEPTED }
    val ignored: Int get() = statuses.values.count { it == CardStatus.IGNORED }
    val allResolved: Boolean get() = cards.isNotEmpty() && cards.all { statuses[it.id] != CardStatus.PENDING }
    val canThinkMore: Boolean get() = round == 1 && allResolved && !scanning
    val finished: Boolean get() = round >= 2 && allResolved && !scanning
}

class DeriveViewModel(private val container: AppContainer) : ViewModel() {

    val settings = container.settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, Settings())

    private val _state = MutableStateFlow(DeriveState())
    val state = _state.asStateFlow()

    init { load(round = 1, append = false) }

    private fun load(round: Int, append: Boolean) {
        viewModelScope.launch {
            _state.update { it.copy(scanning = true, round = round) }
            val today = container.todoRepository.observeTodos().first()
            val fresh = container.agent.derive(today, round)
            _state.update { st ->
                val cards = if (append) st.cards + fresh else fresh
                val statuses = st.statuses.toMutableMap()
                fresh.forEach { statuses[it.id] = CardStatus.PENDING }
                st.copy(scanning = false, cards = cards, statuses = statuses, round = round)
            }
        }
    }

    fun thinkMore() = load(round = 2, append = true)

    fun accept(suggestion: DerivedSuggestion) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val pos = (container.todoRepository.observeTodos().first().maxOfOrNull { it.position } ?: 0) + 1
            container.todoRepository.addTodo(
                Todo(
                    id = "u-${suggestion.id}",
                    title = suggestion.title,
                    tag = suggestion.tag,
                    icon = suggestion.sourceIcon,
                    source = TodoSource.AI,
                    sourceNote = "源自「${suggestion.sourceTitle}」",
                    createdAt = now,
                    position = pos,
                ),
            )
            mark(suggestion.id, CardStatus.ACCEPTED)
        }
    }

    fun ignore(suggestion: DerivedSuggestion) = mark(suggestion.id, CardStatus.IGNORED)

    private fun mark(id: String, status: CardStatus) {
        _state.update { it.copy(statuses = it.statuses + (id to status)) }
    }
}
