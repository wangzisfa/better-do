package com.betterdo.app.agent

import com.betterdo.app.agent.openrouter.OpenRouterClient
import com.betterdo.app.data.prefs.SettingsRepository
import com.betterdo.app.domain.model.AgentTone
import com.betterdo.app.domain.model.Comment
import com.betterdo.app.domain.model.DerivedSuggestion
import com.betterdo.app.domain.model.ReviewStats
import com.betterdo.app.domain.model.Subtask
import com.betterdo.app.domain.model.Todo
import kotlinx.coroutines.flow.first

/**
 * The single agent the app binds. It reads the latest [com.betterdo.app.domain.model.ModelConfig]
 * before every generative call, so toggling the model on/off (or switching models)
 * in Settings takes effect immediately with no app restart: when configured it
 * routes to [OpenRouterAgentService], otherwise to the offline [MockAgentService].
 */
class RoutingAgentService(
    private val settings: SettingsRepository,
    private val mock: MockAgentService = MockAgentService(),
    private val client: OpenRouterClient = OpenRouterClient(),
) : AgentService {

    private suspend fun backend(): AgentService {
        val cfg = settings.modelConfig.first()
        return if (cfg.usable) OpenRouterAgentService(cfg, client, mock) else mock
    }

    override suspend fun parseList(raw: String) = backend().parseList(raw)
    override suspend fun derive(today: List<Todo>, round: Int) = backend().derive(today, round)
    override suspend fun splitIntoSubtasks(todo: Todo) = backend().splitIntoSubtasks(todo)
    override suspend fun commentOn(todo: Todo): Comment = backend().commentOn(todo)

    // Static toned narration always resolves from the curated seed copy.
    override fun greeting(tone: AgentTone) = mock.greeting(tone)
    override fun morningBrief(tone: AgentTone) = mock.morningBrief(tone)
    override fun reviewSummary(stats: ReviewStats, tone: AgentTone) = mock.reviewSummary(stats, tone)
    override fun completionReaction(tone: AgentTone, seed: Int) = mock.completionReaction(tone, seed)
    override fun replyAck(tone: AgentTone) = mock.replyAck(tone)
    override fun quickAddAck(tone: AgentTone) = mock.quickAddAck(tone)
}
