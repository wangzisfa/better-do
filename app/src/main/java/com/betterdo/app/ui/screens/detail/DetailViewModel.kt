package com.betterdo.app.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.betterdo.app.data.prefs.Settings
import com.betterdo.app.data.seed.SeedData
import com.betterdo.app.di.AppContainer
import com.betterdo.app.domain.model.Comment
import com.betterdo.app.domain.model.Toned
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class DetailViewModel(
    private val container: AppContainer,
    private val todoId: String,
) : ViewModel() {

    private val clock = DateTimeFormatter.ofPattern("H:mm")

    val settings = container.settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, Settings())

    val todo = container.todoRepository.observeTodo(todoId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _busy = MutableStateFlow(false)
    val busy = _busy.asStateFlow()

    fun toggleDone() {
        viewModelScope.launch {
            todo.value?.let { container.todoRepository.setDone(todoId, !it.done) }
        }
    }

    fun toggleSubtask(subtaskId: String) {
        viewModelScope.launch { container.todoRepository.toggleSubtask(todoId, subtaskId) }
    }

    fun like(commentId: String) {
        viewModelScope.launch { container.todoRepository.likeComment(todoId, commentId) }
    }

    fun reply(text: String) {
        viewModelScope.launch {
            container.todoRepository.appendComment(
                todoId,
                Comment(uid(), fromAi = false, authorName = SeedData.USER_NAME,
                    body = Toned.plain(text), time = now()),
            )
            val ack = container.agent.replyAck(settings.value.tone)
            container.todoRepository.appendComment(
                todoId,
                Comment(uid(), fromAi = true, authorName = SeedData.AI_NAME,
                    body = Toned.plain(ack), time = now()),
            )
        }
    }

    fun splitIntoSteps() {
        viewModelScope.launch {
            val current = todo.value ?: return@launch
            _busy.value = true
            val steps = container.agent.splitIntoSubtasks(current)
            container.todoRepository.update(current.copy(subtasks = steps))
            _busy.value = false
        }
    }

    private fun now() = LocalTime.now().format(clock)
    private fun uid() = "u-" + UUID.randomUUID().toString().take(8)
}
