package com.betterdo.app.agent

import com.betterdo.app.domain.model.AgentTone
import com.betterdo.app.domain.model.Comment
import com.betterdo.app.domain.model.DerivedSuggestion
import com.betterdo.app.domain.model.ReviewStats
import com.betterdo.app.domain.model.Subtask
import com.betterdo.app.domain.model.Todo

/**
 * The model-in-the-loop seam. Every AI behavior in the app goes through this
 * interface, so v1 ships a deterministic [MockAgentService] and a real model can
 * later be dropped in ([LlmAgentService]) without touching the UI.
 *
 * Generative calls return *toned* content (all three personas) so switching tone
 * re-renders instantly; the plain narration helpers resolve to the active tone.
 */
interface AgentService {

    /** Read the day's todos and suggest follow-up "next step" todos. `round` lets the
     *  user ask the Agent to "再想想" for a fresh batch. Returns empty when tapped out. */
    suspend fun derive(today: List<Todo>, round: Int): List<DerivedSuggestion>

    /** Break a todo into a few actionable steps. */
    suspend fun splitIntoSubtasks(todo: Todo): List<Subtask>

    /** Author an AI comment (all tones) for a freshly added todo. */
    suspend fun commentOn(todo: Todo): Comment

    fun greeting(tone: AgentTone): String
    fun morningBrief(tone: AgentTone): String
    fun reviewSummary(stats: ReviewStats, tone: AgentTone): String
    fun completionReaction(tone: AgentTone, seed: Int): String
    fun replyAck(tone: AgentTone): String
    fun quickAddAck(tone: AgentTone): String
}
