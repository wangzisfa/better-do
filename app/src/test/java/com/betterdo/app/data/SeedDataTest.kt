package com.betterdo.app.data

import com.betterdo.app.data.seed.SeedData
import com.betterdo.app.domain.model.AgentTone
import com.betterdo.app.domain.model.TodoSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SeedDataTest {

    @Test
    fun sample_day_has_seven_todos() {
        assertEquals(7, SeedData.seedTodos(0L).size)
    }

    @Test
    fun ai_sourced_todos_carry_a_source_note() {
        val aiTodos = SeedData.seedTodos(0L).filter { it.source == TodoSource.AI }
        assertTrue("expected some AI-sourced todos", aiTodos.isNotEmpty())
        assertTrue(aiTodos.all { !it.sourceNote.isNullOrBlank() })
    }

    @Test
    fun every_ai_comment_is_authored_in_all_three_tones() {
        val aiComments = SeedData.seedTodos(0L).flatMap { it.comments }.filter { it.fromAi }
        assertTrue(aiComments.isNotEmpty())
        aiComments.forEach { c ->
            AgentTone.entries.forEach { tone ->
                assertTrue("blank ${tone} on ${c.id}", c.body[tone].isNotBlank())
            }
        }
    }

    @Test
    fun reminders_and_derived_batches_are_populated() {
        assertEquals(3, SeedData.reminders.size)
        assertEquals(4, SeedData.derivedBatch1.size)
        assertEquals(2, SeedData.derivedBatch2.size)
    }
}
