package com.betterdo.app.di

import android.content.Context
import com.betterdo.app.agent.AgentService
import com.betterdo.app.agent.MockAgentService
import com.betterdo.app.data.local.BetterDoDatabase
import com.betterdo.app.data.prefs.SettingsRepository
import com.betterdo.app.data.repo.TodoRepository

/**
 * Manual dependency container — created once in [com.betterdo.app.BetterDoApplication].
 * Lightweight on purpose (no Hilt/kapt) so the build stays simple and offline-friendly.
 *
 * To switch to a real model later, replace [agent] with `LlmAgentService(...)`.
 */
class AppContainer(context: Context) {
    private val database = BetterDoDatabase.get(context)
    val todoRepository = TodoRepository(database.todoDao())
    val settingsRepository = SettingsRepository(context)
    val agent: AgentService = MockAgentService()
}
