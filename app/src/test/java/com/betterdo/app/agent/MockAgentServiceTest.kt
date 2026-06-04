package com.betterdo.app.agent

import com.betterdo.app.domain.model.AgentTone
import com.betterdo.app.domain.model.Tag
import com.betterdo.app.domain.model.TodoIcon
import com.betterdo.app.domain.model.TodoSource
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MockAgentServiceTest {

    private val agent = MockAgentService()

    @Test
    fun derive_round1_returns_batch1() = runTest {
        val suggestions = agent.derive(emptyList(), round = 1)
        assertEquals(4, suggestions.size)
        assertEquals("把复盘同步给 Lena · 约 15min", suggestions.first().title)
    }

    @Test
    fun derive_round2_returns_batch2() = runTest {
        assertEquals(2, agent.derive(emptyList(), round = 2).size)
    }

    @Test
    fun derive_round3_is_empty() = runTest {
        assertTrue(agent.derive(emptyList(), round = 3).isEmpty())
    }

    @Test
    fun narration_resolves_for_all_three_tones() {
        for (tone in AgentTone.entries) {
            assertTrue("greeting/$tone", agent.greeting(tone).isNotBlank())
            assertTrue("brief/$tone", agent.morningBrief(tone).isNotBlank())
            assertTrue("reply/$tone", agent.replyAck(tone).isNotBlank())
            assertTrue("quickadd/$tone", agent.quickAddAck(tone).isNotBlank())
        }
    }

    @Test
    fun parseList_splits_lines_stripping_bullets_and_blanks() = runTest {
        val todos = agent.parseList(
            """
            - 买猫粮
            2. 给妈回个电话

            [ ] 读 30 页书
            """.trimIndent(),
        )
        assertEquals(3, todos.size)
        assertEquals(listOf("买猫粮", "给妈回个电话", "读 30 页书"), todos.map { it.title })
    }

    @Test
    fun parseList_detects_and_normalizes_times() = runTest {
        val todos = agent.parseList("14:00 OKR 复盘\n晚上8点 健身房推日\n9点半 站会\n给妈回电话")
        assertEquals("14:00", todos[0].time)
        assertEquals("OKR 复盘", todos[0].title)
        assertEquals("20:00", todos[1].time)
        assertEquals("9:30", todos[2].time)
        assertEquals(null, todos[3].time)
    }

    @Test
    fun parseList_classifies_tag_and_icon() = runTest {
        val todos = agent.parseList("健身房推日训练\n买猫粮\n季度 OKR 复盘\n给妈回电话\n随便写点啥")
        assertEquals(Tag.HEALTH to TodoIcon.DUMBBELL, todos[0].tag to todos[0].icon)
        assertEquals(Tag.LIFE to TodoIcon.CART, todos[1].tag to todos[1].icon)
        assertEquals(Tag.WORK to TodoIcon.TARGET, todos[2].tag to todos[2].icon)
        assertEquals(Tag.LIFE to TodoIcon.PHONE, todos[3].tag to todos[3].icon)
        assertEquals(Tag.LIFE to TodoIcon.SPARKLE, todos[4].tag to todos[4].icon) // default fallback
    }

    @Test
    fun parseList_items_are_user_sourced_with_unique_ids_and_order() = runTest {
        val todos = agent.parseList("a\nb\nc")
        assertTrue(todos.all { it.source == TodoSource.USER })
        assertEquals(3, todos.map { it.id }.toSet().size)
        assertEquals(listOf(0, 1, 2), todos.map { it.position })
    }

    @Test
    fun parseList_flags_urgent_as_priority() = runTest {
        val todos = agent.parseList("交季度方案！\n顺手买瓶水")
        assertTrue("urgent line should be priority", todos[0].priority)
        assertTrue("plain line should not be priority", !todos[1].priority)
    }

    @Test
    fun parseList_blank_input_yields_nothing() = runTest {
        assertTrue(agent.parseList("   \n\n   ").isEmpty())
    }

    @Test
    fun completion_reaction_wraps_negative_seed() {
        // Should not throw and always returns a non-blank line.
        assertTrue(agent.completionReaction(AgentTone.SAVAGE, seed = -7).isNotBlank())
        assertTrue(agent.completionReaction(AgentTone.GENTLE, seed = 999).isNotBlank())
    }
}
