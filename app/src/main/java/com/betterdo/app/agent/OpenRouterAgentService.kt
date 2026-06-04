package com.betterdo.app.agent

import com.betterdo.app.agent.openrouter.OpenRouterClient
import com.betterdo.app.agent.openrouter.OpenRouterPrompts
import com.betterdo.app.data.seed.SeedData
import com.betterdo.app.domain.model.AgentTone
import com.betterdo.app.domain.model.Comment
import com.betterdo.app.domain.model.DerivedSuggestion
import com.betterdo.app.domain.model.ModelConfig
import com.betterdo.app.domain.model.ReviewStats
import com.betterdo.app.domain.model.Subtask
import com.betterdo.app.domain.model.Todo

/**
 * Real-model agent backed by OpenRouter. Each generative call asks the model for
 * strict JSON (all three tones in one shot) and maps it to domain types; any
 * network/parse failure transparently falls back to [fallback] so the app never
 * breaks when offline or misconfigured.
 *
 * The non-generative narration helpers (greeting, brief, …) are short toned copy
 * and resolve from [fallback]'s curated seed — no per-launch network needed.
 */
class OpenRouterAgentService(
    private val config: ModelConfig,
    private val client: OpenRouterClient = OpenRouterClient(),
    private val fallback: AgentService = MockAgentService(),
) : AgentService {

    override suspend fun parseList(raw: String): List<Todo> =
        client.chat(config, OpenRouterPrompts.parseSystem, OpenRouterPrompts.parseUser(raw), jsonMode = true)
            .mapCatching { OpenRouterPrompts.parseTodos(it) }
            .getOrElse { return fallback.parseList(raw) }
            .ifEmpty { fallback.parseList(raw) }

    override suspend fun derive(today: List<Todo>, round: Int): List<DerivedSuggestion> =
        client.chat(config, OpenRouterPrompts.deriveSystem(round), OpenRouterPrompts.deriveUser(today), jsonMode = true)
            .mapCatching { OpenRouterPrompts.parseSuggestions(it) }
            .getOrElse { fallback.derive(today, round) }

    override suspend fun splitIntoSubtasks(todo: Todo): List<Subtask> =
        client.chat(config, OpenRouterPrompts.splitSystem, OpenRouterPrompts.splitUser(todo), jsonMode = true)
            .mapCatching { OpenRouterPrompts.parseSubtasks(it) }
            .getOrElse { return fallback.splitIntoSubtasks(todo) }
            .ifEmpty { fallback.splitIntoSubtasks(todo) }

    override suspend fun commentOn(todo: Todo): Comment =
        client.chat(config, OpenRouterPrompts.commentSystem, OpenRouterPrompts.commentUser(todo), jsonMode = true)
            .mapCatching { OpenRouterPrompts.parseComment(it, SeedData.AI_NAME) }
            .getOrElse { fallback.commentOn(todo) }

    // Static toned narration — resolved from curated seed copy.
    override fun greeting(tone: AgentTone) = fallback.greeting(tone)
    override fun morningBrief(tone: AgentTone) = fallback.morningBrief(tone)
    override fun reviewSummary(stats: ReviewStats, tone: AgentTone) = fallback.reviewSummary(stats, tone)
    override fun completionReaction(tone: AgentTone, seed: Int) = fallback.completionReaction(tone, seed)
    override fun replyAck(tone: AgentTone) = fallback.replyAck(tone)
    override fun quickAddAck(tone: AgentTone) = fallback.quickAddAck(tone)
}
