package com.betterdo.app.agent

import com.betterdo.app.domain.model.AgentTone
import com.betterdo.app.domain.model.Comment
import com.betterdo.app.domain.model.DerivedSuggestion
import com.betterdo.app.domain.model.ReviewStats
import com.betterdo.app.domain.model.Subtask
import com.betterdo.app.domain.model.Todo

/**
 * Stub for a future real model (e.g. the Claude API). NOT wired in v1 — it
 * documents the contract so swapping the binding in [com.betterdo.app.di.AppContainer]
 * is the only change required.
 *
 * Suggested request/response contract (JSON over the wire):
 *
 *   parseList → POST { raw }
 *             ← { items:[{ title, time, tag, icon }] }   // service maps to Todo
 *
 *   derive →  POST { todos:[{title,note,tag,done,streak}], history, round }
 *             ← { suggestions:[{ sourceTitle, title, tag,
 *                                why:{gentle,coach,savage} }] }
 *
 *   commentOn → POST { todo:{...} }
 *             ← { comment:{ gentle, coach, savage } }
 *
 *   splitIntoSubtasks → POST { todo:{...} } ← { steps:[ "...", "..." ] }
 *
 * System prompt is parameterized by persona; the gentle/coach/savage voice guides
 * (see the seed copy in SeedData) become few-shot examples. Always request all
 * three tones in one call so tone-switching stays instant and offline.
 */
class LlmAgentService(
    @Suppress("unused") private val fallback: AgentService = MockAgentService(),
) : AgentService {

    private val notWired: Nothing
        get() = throw NotImplementedError("LlmAgentService is a v1 stub — bind MockAgentService.")

    override suspend fun parseList(raw: String): List<Todo> = notWired
    override suspend fun derive(today: List<Todo>, round: Int): List<DerivedSuggestion> = notWired
    override suspend fun splitIntoSubtasks(todo: Todo): List<Subtask> = notWired
    override suspend fun commentOn(todo: Todo): Comment = notWired
    override fun greeting(tone: AgentTone): String = notWired
    override fun morningBrief(tone: AgentTone): String = notWired
    override fun reviewSummary(stats: ReviewStats, tone: AgentTone): String = notWired
    override fun completionReaction(tone: AgentTone, seed: Int): String = notWired
    override fun replyAck(tone: AgentTone): String = notWired
    override fun quickAddAck(tone: AgentTone): String = notWired
}
