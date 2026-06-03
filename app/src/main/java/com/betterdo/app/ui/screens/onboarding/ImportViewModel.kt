package com.betterdo.app.ui.screens.onboarding

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.betterdo.app.di.AppContainer
import com.betterdo.app.domain.model.AgentTone
import com.betterdo.app.domain.model.Todo

/**
 * Drives the onboarding first screen: collects the user's hand-written / pasted list,
 * lets the [com.betterdo.app.agent.AgentService] parse it into todos, then persists the
 * result (or the curated sample day) and marks onboarding done.
 */
class ImportViewModel(private val container: AppContainer) : ViewModel() {

    /** The raw text the user is typing / pasting. */
    var raw by mutableStateOf("")
        private set

    /** What the Agent made of [raw] — shown on the review step. */
    var parsed by mutableStateOf<List<Todo>>(emptyList())
        private set

    fun updateRaw(value: String) {
        raw = value
    }

    /** Hand [raw] to the Agent; the call carries its own "scanning" beat. */
    suspend fun parse() {
        parsed = container.agent.parseList(raw)
    }

    /** Persist the parsed hand-written list and finish onboarding. */
    suspend fun commitHandwritten(tone: AgentTone) {
        if (parsed.isNotEmpty()) container.todoRepository.addAll(parsed)
        finish(tone)
    }

    /** Load the curated sample day instead and finish onboarding. */
    suspend fun commitSample(tone: AgentTone) {
        container.todoRepository.ensureSeeded()
        finish(tone)
    }

    private suspend fun finish(tone: AgentTone) {
        container.settingsRepository.setTone(tone)
        container.settingsRepository.setOnboarded(true)
    }
}
