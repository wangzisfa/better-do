package com.betterdo.app.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.betterdo.app.domain.model.AgentTone
import com.betterdo.app.domain.model.ThemeMode
import com.betterdo.app.ui.theme.DefaultAccentArgb
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

data class Settings(
    val onboarded: Boolean = false,
    val tone: AgentTone = AgentTone.COACH,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val accentArgb: Long = DefaultAccentArgb,
    val aiComments: Boolean = true,
)

/** App-level preferences backed by Jetpack DataStore. */
class SettingsRepository(private val context: Context) {

    private object Keys {
        val ONBOARDED = booleanPreferencesKey("onboarded")
        val TONE = stringPreferencesKey("tone")
        val THEME = stringPreferencesKey("theme")
        val ACCENT = longPreferencesKey("accent")
        val AI_COMMENTS = booleanPreferencesKey("ai_comments")
    }

    val settings: Flow<Settings> = context.dataStore.data.map { p ->
        Settings(
            onboarded = p[Keys.ONBOARDED] ?: false,
            tone = AgentTone.fromKey(p[Keys.TONE]),
            themeMode = ThemeMode.fromKey(p[Keys.THEME]),
            accentArgb = p[Keys.ACCENT] ?: DefaultAccentArgb,
            aiComments = p[Keys.AI_COMMENTS] ?: true,
        )
    }

    suspend fun setOnboarded(value: Boolean) =
        edit { it[Keys.ONBOARDED] = value }

    suspend fun setTone(tone: AgentTone) =
        edit { it[Keys.TONE] = tone.key }

    suspend fun setThemeMode(mode: ThemeMode) =
        edit { it[Keys.THEME] = mode.name }

    suspend fun setAccent(argb: Long) =
        edit { it[Keys.ACCENT] = argb }

    suspend fun setAiComments(value: Boolean) =
        edit { it[Keys.AI_COMMENTS] = value }

    private suspend fun edit(block: (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        context.dataStore.edit(block)
    }
}
