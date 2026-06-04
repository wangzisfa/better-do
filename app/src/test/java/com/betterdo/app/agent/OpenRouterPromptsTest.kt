package com.betterdo.app.agent

import com.betterdo.app.agent.openrouter.OpenRouterPrompts
import com.betterdo.app.domain.model.ModelConfig
import com.betterdo.app.domain.model.Tag
import com.betterdo.app.domain.model.TodoIcon
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Covers the OpenRouter JSON → domain mapping (the only logic-heavy part of the
 * real-model path) and [ModelConfig.usable]. Network calls aren't exercised here.
 */
class OpenRouterPromptsTest {

    @Test fun `usable requires enabled key and model`() {
        assertFalse(ModelConfig(enabled = false, apiKey = "k", model = "m").usable)
        assertFalse(ModelConfig(enabled = true, apiKey = "", model = "m").usable)
        assertFalse(ModelConfig(enabled = true, apiKey = "k", model = "").usable)
        assertTrue(ModelConfig(enabled = true, apiKey = "k", model = "m").usable)
    }

    @Test fun `parseTodos maps fields, tags and icons`() {
        val json = """
            {"items":[
              {"title":"OKR 复盘","time":"14:00","tag":"work","icon":"target","priority":true},
              {"title":"买猫粮","tag":"life","icon":"cart"}
            ]}
        """.trimIndent()
        val todos = OpenRouterPrompts.parseTodos(json, now = 0L)
        assertEquals(2, todos.size)
        assertEquals("OKR 复盘", todos[0].title)
        assertEquals("14:00", todos[0].time)
        assertEquals(Tag.WORK, todos[0].tag)
        assertEquals(TodoIcon.TARGET, todos[0].icon)
        assertTrue(todos[0].priority)
        assertEquals(0, todos[0].position)
        assertEquals(Tag.LIFE, todos[1].tag)
        assertEquals(TodoIcon.CART, todos[1].icon)
        assertEquals(null, todos[1].time)
    }

    @Test fun `parseTodos tolerates markdown fences and chinese tag words`() {
        val fenced = "```json\n{\"items\":[{\"title\":\"读书\",\"tag\":\"习惯\",\"icon\":\"book\"}]}\n```"
        val todos = OpenRouterPrompts.parseTodos(fenced, now = 0L)
        assertEquals(1, todos.size)
        assertEquals(Tag.HABIT, todos[0].tag)
    }

    @Test fun `parseSubtasks keeps non-blank steps and marks them AI`() {
        val subs = OpenRouterPrompts.parseSubtasks("""{"steps":["先列大纲",""," 动手写 "]}""")
        assertEquals(2, subs.size)
        assertEquals("先列大纲", subs[0].title)
        assertEquals("动手写", subs[1].title)
        assertTrue(subs.all { it.byAi })
    }

    @Test fun `parseComment fills missing tones from coach`() {
        val c = OpenRouterPrompts.parseComment(
            """{"comment":{"coach":"配个时间点","gentle":"慢慢来"}}""",
            authorName = "Dodo",
        )
        assertTrue(c.fromAi)
        assertEquals("Dodo", c.authorName)
        assertEquals("慢慢来", c.body.gentle)
        assertEquals("配个时间点", c.body.coach)
        // savage was blank → falls back to coach
        assertEquals("配个时间点", c.body.savage)
    }

    @Test fun `parseSuggestions maps source icon and toned why`() {
        val json = """
            {"suggestions":[
              {"sourceTitle":"OKR 复盘","sourceIcon":"target","title":"同步给 Lena","tag":"work",
               "why":{"gentle":"占个坑","coach":"对齐","savage":"别白写"}}
            ]}
        """.trimIndent()
        val s = OpenRouterPrompts.parseSuggestions(json)
        assertEquals(1, s.size)
        assertEquals("同步给 Lena", s[0].title)
        assertEquals(TodoIcon.TARGET, s[0].sourceIcon)
        assertEquals(Tag.WORK, s[0].tag)
        assertEquals("对齐", s[0].why.coach)
    }
}
