package com.betterdo.app.agent.openrouter

import com.betterdo.app.domain.model.Comment
import com.betterdo.app.domain.model.DerivedSuggestion
import com.betterdo.app.domain.model.Subtask
import com.betterdo.app.domain.model.Tag
import com.betterdo.app.domain.model.Todo
import com.betterdo.app.domain.model.TodoIcon
import com.betterdo.app.domain.model.TodoSource
import com.betterdo.app.domain.model.Toned
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID

/**
 * Prompt construction + strict JSON → domain mapping for the OpenRouter agent.
 * Kept free of Android/network types so the mapping is unit-testable, and so the
 * one-call "all three tones" contract from [com.betterdo.app.agent.LlmAgentService]
 * lives in one place. Parsers throw on malformed output; the agent then falls back.
 */
object OpenRouterPrompts {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }
    private val clock = DateTimeFormatter.ofPattern("H:mm")

    /** Shared persona guide so every toned field stays on-voice. */
    private const val PERSONA_GUIDE = """
你是待办 App「BetterDo」里的 AI 助手 Dodo。所有面向用户的文案都用简体中文，并且要同时写出三种人格语气：
- gentle（温柔助理）：温柔陪伴，轻轻推一把，不施压。
- coach（GTD 教练）：理性、讲优先级、给可执行的下一步。
- savage（毒舌损友）：犀利吐槽、专治拖延，但本质是为对方好。
只输出 JSON，不要任何解释或 Markdown 代码块。"""

    val tags = "work（工作）/ life（生活）/ health（健康）/ habit（习惯）"
    val icons = "target, dumbbell, phone, code, cart, book, pin, sun, bell, flame, clock, sparkle, cal"

    // ---- parseList ----

    val parseSystem = """$PERSONA_GUIDE
任务：把用户随手写的乱清单整理成结构化待办。一行一件有意义的事。
为每件事推断：title（精炼标题）、time（HH:mm，没有就 null）、tag（$tags）、icon（$icons）、priority（是否紧急/重要，布尔）。
输出 JSON：{"items":[{"title":"","time":null,"tag":"work","icon":"sparkle","priority":false}]}"""

    fun parseUser(raw: String) = "用户的清单：\n\"\"\"\n$raw\n\"\"\""

    fun parseTodos(content: String, now: Long = System.currentTimeMillis()): List<Todo> {
        val items = json.decodeFromString(ParseResult.serializer(), clean(content)).items
        return items.mapIndexed { i, it ->
            Todo(
                id = uid(),
                title = it.title.trim(),
                time = it.time?.trim()?.takeIf { t -> t.isNotBlank() },
                tag = tag(it.tag),
                icon = TodoIcon.fromKey(it.icon),
                priority = it.priority,
                source = TodoSource.USER,
                createdAt = now + i,
                position = i,
            )
        }.filter { it.title.isNotBlank() }
    }

    // ---- derive ----

    fun deriveSystem(round: Int) = """$PERSONA_GUIDE
任务：读用户今天的待办，推理"做完它通常还要做的下一步"，给出新的"派生"待办建议。这是第 $round 轮，给${if (round > 1) "和上一轮不同的、更发散的" else "最贴切的"}建议（最多 4 条，没有合适的就空数组）。
每条包含：sourceTitle（来源待办标题）、sourceIcon（来源图标，$icons）、title（建议标题）、tag（$tags）、why（三种语气的理由）。
输出 JSON：{"suggestions":[{"sourceTitle":"","sourceIcon":"sparkle","title":"","tag":"work","why":{"gentle":"","coach":"","savage":""}}]}"""

    fun deriveUser(today: List<Todo>): String {
        val lines = today.joinToString("\n") {
            "- ${it.title}（${it.tag.label}${it.time?.let { t -> " · $t" } ?: ""}${if (it.done) " · 已完成" else ""}）"
        }
        return "今天的待办：\n$lines"
    }

    fun parseSuggestions(content: String): List<DerivedSuggestion> =
        json.decodeFromString(DeriveResult.serializer(), clean(content)).suggestions.map {
            DerivedSuggestion(
                id = uid(),
                sourceTitle = it.sourceTitle,
                sourceIcon = TodoIcon.fromKey(it.sourceIcon),
                title = it.title.trim(),
                tag = tag(it.tag),
                why = it.why.toToned(),
            )
        }.filter { it.title.isNotBlank() }

    // ---- splitIntoSubtasks ----

    val splitSystem = """$PERSONA_GUIDE
任务：把一件待办拆成 2-4 个具体、可执行的小步骤（中文，动词开头，简短）。
输出 JSON：{"steps":["",""]}"""

    fun splitUser(todo: Todo) =
        "待办：${todo.title}${todo.note?.let { "\n备注：$it" } ?: ""}"

    fun parseSubtasks(content: String): List<Subtask> =
        json.decodeFromString(SplitResult.serializer(), clean(content)).steps
            .map { it.trim() }.filter { it.isNotBlank() }
            .map { Subtask(uid(), it, byAi = true) }

    // ---- commentOn ----

    val commentSystem = """$PERSONA_GUIDE
任务：为用户刚加的一件待办写一条简短的 Agent 评论（三种语气，各 1-2 句，中文）。
输出 JSON：{"comment":{"gentle":"","coach":"","savage":""}}"""

    fun commentUser(todo: Todo) =
        "待办：${todo.title}${todo.note?.let { "\n备注：$it" } ?: ""}"

    fun parseComment(content: String, authorName: String): Comment {
        val body = json.decodeFromString(CommentResult.serializer(), clean(content)).comment.toToned()
        return Comment(
            id = uid(),
            fromAi = true,
            authorName = authorName,
            body = body,
            time = LocalTime.now().format(clock),
            likes = 0,
        )
    }

    // ---- helpers ----

    /** Strip stray ```json fences some models wrap JSON in. */
    private fun clean(s: String): String {
        val t = s.trim()
        if (!t.startsWith("```")) return t
        return t.removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
    }

    private fun tag(key: String?): Tag = when (key?.trim()?.lowercase()) {
        "work", "工作" -> Tag.WORK
        "health", "健康" -> Tag.HEALTH
        "habit", "习惯" -> Tag.HABIT
        else -> Tag.LIFE
    }

    private fun uid() = "u-" + UUID.randomUUID().toString().take(8)

    // ---- JSON DTOs ----

    @Serializable private data class ParseResult(val items: List<ParsedItem> = emptyList())
    @Serializable private data class ParsedItem(
        val title: String = "",
        val time: String? = null,
        val tag: String? = null,
        val icon: String? = null,
        val priority: Boolean = false,
    )

    @Serializable private data class DeriveResult(val suggestions: List<SuggestionItem> = emptyList())
    @Serializable private data class SuggestionItem(
        val sourceTitle: String = "",
        val sourceIcon: String? = null,
        val title: String = "",
        val tag: String? = null,
        val why: TonedDto = TonedDto(),
    )

    @Serializable private data class SplitResult(val steps: List<String> = emptyList())

    @Serializable private data class CommentResult(val comment: TonedDto = TonedDto())

    @Serializable private data class TonedDto(
        val gentle: String = "",
        val coach: String = "",
        val savage: String = "",
    ) {
        fun toToned() = Toned(
            gentle = gentle.ifBlank { coach },
            coach = coach.ifBlank { gentle },
            savage = savage.ifBlank { coach },
        )
    }
}
