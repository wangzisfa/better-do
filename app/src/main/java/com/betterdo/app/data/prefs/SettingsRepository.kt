package com.betterdo.app.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.betterdo.app.domain.model.AgentTone
import com.betterdo.app.domain.model.ModelConfig
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
        val MODEL_ENABLED = booleanPreferencesKey("model_enabled")
        val MODEL_KEY = stringPreferencesKey("model_key")
        val MODEL_NAME = stringPreferencesKey("model_name")
        val MODEL_BASE = stringPreferencesKey("model_base")
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

    /** Real-model (OpenRouter) access config. */
    val modelConfig: Flow<ModelConfig> = context.dataStore.data.map { p ->
        ModelConfig(
            enabled = p[Keys.MODEL_ENABLED] ?: false,
            apiKey = p[Keys.MODEL_KEY] ?: "",
            model = p[Keys.MODEL_NAME] ?: ModelConfig.DEFAULT_MODEL,
            baseUrl = p[Keys.MODEL_BASE] ?: ModelConfig.DEFAULT_BASE_URL,
        )
    }

    suspend fun setModelConfig(config: ModelConfig) = edit {
        it[Keys.MODEL_ENABLED] = config.enabled
        it[Keys.MODEL_KEY] = config.apiKey
        it[Keys.MODEL_NAME] = config.model
        it[Keys.MODEL_BASE] = config.baseUrl
    }

    private suspend fun edit(block: (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        context.dataStore.edit(block)
    }
}
