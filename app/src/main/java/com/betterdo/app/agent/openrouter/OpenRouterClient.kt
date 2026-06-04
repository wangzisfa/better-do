package com.betterdo.app.agent.openrouter

import com.betterdo.app.domain.model.ModelConfig
import com.betterdo.app.domain.model.ModelInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * Thin OpenRouter (OpenAI-compatible) HTTP client built on [HttpURLConnection] so
 * the app pulls in no extra networking dependency. All calls run on [Dispatchers.IO]
 * and return a [Result] — callers decide whether to surface the error (the debug
 * screen) or fall back to the offline mock agent (the live app).
 */
class OpenRouterClient {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = false }

    /** One round-trip chat completion. Returns the assistant message content. */
    suspend fun chat(
        config: ModelConfig,
        system: String,
        user: String,
        jsonMode: Boolean = false,
        temperature: Double? = null,
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val body = json.encodeToString(
                ChatRequest.serializer(),
                ChatRequest(
                    model = config.model,
                    messages = listOf(Msg("system", system), Msg("user", user)),
                    responseFormat = if (jsonMode) ResponseFormat("json_object") else null,
                    temperature = temperature,
                ),
            )
            val raw = post(config, "/chat/completions", body)
            val parsed = json.decodeFromString(ChatResponse.serializer(), raw)
            parsed.error?.let { throw IOException(it.message.ifBlank { "model error" }) }
            parsed.choices.firstOrNull()?.message?.content?.takeIf { it.isNotBlank() }
                ?: throw IOException("空响应")
        }
    }

    /** Fetch the provider's model catalog (GET /models). */
    suspend fun listModels(config: ModelConfig): Result<List<ModelInfo>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val raw = get(config, "/models")
                json.decodeFromString(ModelsResponse.serializer(), raw)
                    .data.map { ModelInfo(it.id, it.name) }
                    .sortedBy { it.id }
            }
        }

    // ---- transport ----

    private fun post(config: ModelConfig, path: String, body: String): String =
        open(config, path, "POST").run {
            doOutput = true
            outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
            readResult(this)
        }

    private fun get(config: ModelConfig, path: String): String =
        open(config, path, "GET").run { readResult(this) }

    private fun open(config: ModelConfig, path: String, method: String): HttpURLConnection {
        val base = config.baseUrl.trimEnd('/').ifBlank { ModelConfig.DEFAULT_BASE_URL }
        val conn = URL(base + path).openConnection() as HttpURLConnection
        conn.requestMethod = method
        conn.connectTimeout = 15_000
        conn.readTimeout = 45_000
        conn.setRequestProperty("Authorization", "Bearer ${config.apiKey}")
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("Accept", "application/json")
        // Optional OpenRouter attribution headers.
        conn.setRequestProperty("HTTP-Referer", "https://github.com/wangzisfa/better-do")
        conn.setRequestProperty("X-Title", "BetterDo")
        return conn
    }

    private fun readResult(conn: HttpURLConnection): String {
        val code = conn.responseCode
        val stream = if (code in 200..299) conn.inputStream else conn.errorStream
        val text = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
        if (code !in 200..299) {
            val msg = runCatching {
                json.decodeFromString(ChatResponse.serializer(), text).error?.message
            }.getOrNull()
            throw IOException("HTTP $code${msg?.let { "：$it" } ?: ""}")
        }
        return text
    }

    // ---- wire DTOs ----

    @Serializable
    private data class ChatRequest(
        val model: String,
        val messages: List<Msg>,
        @SerialName("response_format") val responseFormat: ResponseFormat? = null,
        val temperature: Double? = null,
    )

    @Serializable
    private data class Msg(val role: String, val content: String)

    @Serializable
    private data class ResponseFormat(val type: String)

    @Serializable
    private data class ChatResponse(
        val choices: List<Choice> = emptyList(),
        val error: ApiError? = null,
    )

    @Serializable
    private data class Choice(val message: Msg)

    @Serializable
    private data class ApiError(val message: String = "")

    @Serializable
    private data class ModelsResponse(val data: List<WireModel> = emptyList())

    @Serializable
    private data class WireModel(val id: String, val name: String? = null)
}
