package com.betterdo.app.ui.screens.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.betterdo.app.agent.openrouter.OpenRouterClient
import com.betterdo.app.di.AppContainer
import com.betterdo.app.domain.model.ModelConfig
import com.betterdo.app.domain.model.ModelInfo
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Result of a "调试" (test) round-trip against the configured model. */
sealed interface TestState {
    data object Idle : TestState
    data object Running : TestState
    data class Ok(val reply: String, val millis: Long) : TestState
    data class Error(val message: String) : TestState
}

/**
 * Drives the model-access config screen: edits the [ModelConfig] locally, persists
 * it, fetches the provider's model catalog and runs a live "调试" round-trip.
 */
class ModelConfigViewModel(private val container: AppContainer) : ViewModel() {

    private val client = OpenRouterClient()

    var enabled by mutableStateOf(false); private set
    var apiKey by mutableStateOf(""); private set
    var model by mutableStateOf(ModelConfig.DEFAULT_MODEL); private set
    var baseUrl by mutableStateOf(ModelConfig.DEFAULT_BASE_URL); private set

    var test by mutableStateOf<TestState>(TestState.Idle); private set
    var saved by mutableStateOf(false); private set

    /** Models fetched from /models, shown alongside the curated presets. */
    var fetchedModels by mutableStateOf<List<ModelInfo>>(emptyList()); private set
    var fetching by mutableStateOf(false); private set
    var fetchError by mutableStateOf<String?>(null); private set

    init {
        viewModelScope.launch {
            val cfg = container.settingsRepository.modelConfig.first()
            enabled = cfg.enabled
            apiKey = cfg.apiKey
            model = cfg.model
            baseUrl = cfg.baseUrl
        }
    }

    private fun current() = ModelConfig(enabled, apiKey.trim(), model.trim(), baseUrl.trim())

    fun setEnabled(v: Boolean) { enabled = v; dirty() }
    fun setApiKey(v: String) { apiKey = v; dirty() }
    fun setModel(v: String) { model = v; dirty() }
    fun setBaseUrl(v: String) { baseUrl = v; dirty() }

    private fun dirty() { saved = false }

    fun save() {
        viewModelScope.launch {
            container.settingsRepository.setModelConfig(current())
            saved = true
        }
    }

    /** Save, then send a tiny prompt and report the round-trip + latency. */
    fun runTest() {
        val cfg = current()
        if (cfg.apiKey.isBlank()) {
            test = TestState.Error("先填 API Key")
            return
        }
        viewModelScope.launch {
            container.settingsRepository.setModelConfig(cfg)
            saved = true
            test = TestState.Running
            val started = System.currentTimeMillis()
            val result = client.chat(
                config = cfg,
                system = "你是连接测试助手。只回复两个字：可用。",
                user = "ping",
                temperature = 0.0,
            )
            val elapsed = System.currentTimeMillis() - started
            test = result.fold(
                onSuccess = { TestState.Ok(it.trim().take(60), elapsed) },
                onFailure = { TestState.Error(it.message ?: it.javaClass.simpleName) },
            )
        }
    }

    fun fetchModels() {
        val cfg = current()
        viewModelScope.launch {
            fetching = true
            fetchError = null
            client.listModels(cfg).fold(
                onSuccess = { fetchedModels = it },
                onFailure = { fetchError = it.message ?: "拉取失败" },
            )
            fetching = false
        }
    }
}
