package com.betterdo.app.agent

import com.betterdo.app.data.seed.SeedData
import com.betterdo.app.domain.model.AgentTone
import com.betterdo.app.domain.model.Comment
import com.betterdo.app.domain.model.DerivedSuggestion
import com.betterdo.app.domain.model.ReviewStats
import com.betterdo.app.domain.model.Subtask
import com.betterdo.app.domain.model.Todo
import com.betterdo.app.domain.model.Toned
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID

/**
 * Deterministic, offline agent for v1. Returns the design's curated copy for the
 * seed day and falls back to simple per-tag/tone templates for user-added todos.
 * Short delays reproduce the design's "Agent 正在读你的清单…" scanning beat.
 */
class MockAgentService : AgentService {

    private val clock = DateTimeFormatter.ofPattern("H:mm")

    override suspend fun derive(today: List<Todo>, round: Int): List<DerivedSuggestion> {
        delay(900)
        val seeds = when (round) {
            1 -> SeedData.derivedBatch1
            2 -> SeedData.derivedBatch2
            else -> emptyList()
        }
        return seeds.map {
            DerivedSuggestion(
                id = it.id, sourceTitle = it.source, sourceIcon = it.srcIcon,
                title = it.title, tag = it.tag, why = it.why,
            )
        }
    }

    override suspend fun splitIntoSubtasks(todo: Todo): List<Subtask> {
        delay(700)
        // Faithful for the OKR seed; generic 3-step scaffold otherwise.
        if (todo.id == "t1") {
            return listOf(
                Subtask(uid(), "写「关键成果」回顾", byAi = true),
                Subtask(uid(), "补 3 个掉链子的指标 + 原因", byAi = true),
                Subtask(uid(), "下季度 1 个聚焦目标", byAi = true),
            )
        }
        return listOf(
            Subtask(uid(), "理清目标，先迈出最小的一步", byAi = true),
            Subtask(uid(), "动手做核心的那部分", byAi = true),
            Subtask(uid(), "收尾、检查，划掉它", byAi = true),
        )
    }

    override suspend fun commentOn(todo: Todo): Comment {
        delay(500)
        val title = todo.title
        return Comment(
            id = uid(),
            fromAi = true,
            authorName = SeedData.AI_NAME,
            body = Toned(
                gentle = "「$title」记下啦~ 想先从最小的一步开始吗？我陪你 ☺︎",
                coach = "「$title」已收。给它配个具体时间点，更容易落地。",
                savage = "「$title」？写了就别供着——定个时间，几点干？",
            ),
            time = LocalTime.now().format(clock),
            likes = 0,
        )
    }

    override fun greeting(tone: AgentTone) = SeedData.greeting[tone]
    override fun morningBrief(tone: AgentTone) = SeedData.brief[tone]
    override fun reviewSummary(stats: ReviewStats, tone: AgentTone) = SeedData.reviewSummary[tone]
    override fun replyAck(tone: AgentTone) = SeedData.replyAck[tone]
    override fun quickAddAck(tone: AgentTone) = SeedData.quickAddAck[tone]

    override fun completionReaction(tone: AgentTone, seed: Int): String {
        val list = SeedData.cheer[tone.key] ?: SeedData.cheer.getValue("coach")
        return list[(seed % list.size + list.size) % list.size]
    }

    private fun uid() = "u-" + UUID.randomUUID().toString().take(8)
}
