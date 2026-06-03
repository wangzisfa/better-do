package com.betterdo.app.agent

import com.betterdo.app.domain.model.AgentTone
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
    fun completion_reaction_wraps_negative_seed() {
        // Should not throw and always returns a non-blank line.
        assertTrue(agent.completionReaction(AgentTone.SAVAGE, seed = -7).isNotBlank())
        assertTrue(agent.completionReaction(AgentTone.GENTLE, seed = 999).isNotBlank())
    }
}
