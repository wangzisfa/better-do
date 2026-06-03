package com.betterdo.app.ui.screens.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.betterdo.app.data.prefs.Settings
import com.betterdo.app.di.AppContainer
import com.betterdo.app.domain.model.AgentTone
import com.betterdo.app.domain.model.Tag
import com.betterdo.app.domain.model.Todo
import com.betterdo.app.domain.model.TodoIcon
import com.betterdo.app.domain.model.TodoSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.random.Random

class TodayViewModel(private val container: AppContainer) : ViewModel() {

    val settings = container.settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, Settings())

    val todos = container.todoRepository.observeTodos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _toast = MutableStateFlow<String?>(null)
    val toast = _toast.asStateFlow()

    fun greeting(tone: AgentTone): String = container.agent.greeting(tone)
    fun brief(tone: AgentTone): String = container.agent.morningBrief(tone)

    fun toggleDone(todo: Todo) {
        viewModelScope.launch {
            val nowDone = !todo.done
            container.todoRepository.setDone(todo.id, nowDone)
            if (nowDone) {
                _toast.value = container.agent.completionReaction(settings.value.tone, Random.nextInt(1000))
            }
        }
    }

    fun addQuick(title: String) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val pos = (todos.value.maxOfOrNull { it.position } ?: 0) + 1
            val todo = Todo(
                id = "u-" + UUID.randomUUID().toString().take(8),
                title = title,
                tag = Tag.LIFE,
                icon = TodoIcon.SPARKLE,
                source = TodoSource.USER,
                createdAt = now,
                position = pos,
            )
            container.todoRepository.addTodo(todo)
            _toast.value = container.agent.quickAddAck(settings.value.tone)
            container.todoRepository.appendComment(todo.id, container.agent.commentOn(todo))
        }
    }

    fun clearToast() {
        _toast.value = null
    }
}
