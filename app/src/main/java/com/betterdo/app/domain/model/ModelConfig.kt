package com.betterdo.app.domain.model

/**
 * Real-model access config (OpenRouter). Persisted via
 * [com.betterdo.app.data.prefs.SettingsRepository] and read on every generative
 * call by [com.betterdo.app.agent.RoutingAgentService] — when [usable] the app
 * talks to a real LLM, otherwise it falls back to the offline mock agent.
 */
data class ModelConfig(
    val enabled: Boolean = false,
    val apiKey: String = "",
    val model: String = DEFAULT_MODEL,
    val baseUrl: String = DEFAULT_BASE_URL,
) {
    /** True only when the model can actually be called. */
    val usable: Boolean get() = enabled && apiKey.isNotBlank() && model.isNotBlank()

    companion object {
        const val DEFAULT_BASE_URL = "https://openrouter.ai/api/v1"
        const val DEFAULT_MODEL = "openai/gpt-4o-mini"

        /** Curated quick-pick list of popular OpenRouter models (id → friendly label). */
        val PRESETS: List<ModelInfo> = listOf(
            ModelInfo("openai/gpt-4o-mini", "GPT-4o mini · 快·便宜"),
            ModelInfo("openai/gpt-4o", "GPT-4o · 通用强"),
            ModelInfo("anthropic/claude-3.5-sonnet", "Claude 3.5 Sonnet"),
            ModelInfo("anthropic/claude-3.5-haiku", "Claude 3.5 Haiku · 快"),
            ModelInfo("google/gemini-2.0-flash-001", "Gemini 2.0 Flash"),
            ModelInfo("deepseek/deepseek-chat", "DeepSeek Chat · 中文好"),
            ModelInfo("meta-llama/llama-3.3-70b-instruct", "Llama 3.3 70B"),
            ModelInfo("qwen/qwen-2.5-72b-instruct", "Qwen2.5 72B · 中文好"),
        )
    }
}

/** A model offered by the provider — used for presets and the fetched /models list. */
data class ModelInfo(val id: String, val name: String? = null) {
    val label: String get() = name?.takeIf { it.isNotBlank() } ?: id
}
