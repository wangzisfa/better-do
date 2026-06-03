package com.betterdo.app.domain.model

import kotlinx.serialization.Serializable

/**
 * Domain models for BetterDo. These mirror the design mockup's data shapes
 * (`seedTodos`, `REMINDERS`, tone metadata) and are storage/UI agnostic — colors
 * are kept as ARGB [Long]s so the domain layer never depends on Compose.
 */

/** The Agent's persona. Every AI string in the app is authored in all three. */
enum class AgentTone(
    val label: String,
    val short: String,
    val colorArgb: Long,
    val emoji: String,
) {
    GENTLE("温柔助理", "温柔", 0xFF1FA37A, "☺"),
    COACH("GTD 教练", "教练", 0xFF3B6FE0, "◷"),
    SAVAGE("毒舌损友", "毒舌", 0xFFF0502E, "⚡");

    val key: String get() = name.lowercase()

    /** One-line persona blurb shown during onboarding / settings. */
    val blurb: String
        get() = when (this) {
            GENTLE -> "温柔陪伴，轻轻推你一把，不施压"
            COACH -> "理性 GTD，帮你排优先级、拆步骤"
            SAVAGE -> "毒舌损友，专治拖延和找借口"
        }

    companion object {
        fun fromKey(key: String?): AgentTone =
            entries.firstOrNull { it.key == key } ?: COACH
    }
}

/** Task category. */
enum class Tag(val label: String, val colorArgb: Long) {
    WORK("工作", 0xFF3B6FE0),
    LIFE("生活", 0xFFE08A1E),
    HEALTH("健康", 0xFF1FA37A),
    HABIT("习惯", 0xFF8A5AD8),
}

/** Stroke-icon set used by todo rows (drawn in [com.betterdo.app.ui.components.BdIcon]). */
enum class TodoIcon {
    TARGET, DUMBBELL, PHONE, CODE, CART, BOOK, PIN, SUN,
    BELL, FLAME, CLOCK, CHECK, SPARKLE, CALENDAR, PLUS, DOTS;

    companion object {
        fun fromKey(key: String?): TodoIcon = when (key) {
            "target" -> TARGET
            "dumbbell" -> DUMBBELL
            "phone" -> PHONE
            "code" -> CODE
            "cart" -> CART
            "book" -> BOOK
            "pin" -> PIN
            "sun" -> SUN
            "bell" -> BELL
            "flame" -> FLAME
            "clock" -> CLOCK
            "sparkle" -> SPARKLE
            "cal" -> CALENDAR
            else -> SPARKLE
        }
    }
}

/** Who created the todo. AI-sourced todos carry a [Todo.sourceNote] explaining why. */
enum class TodoSource { USER, AI }

/** A piece of copy that differs per persona; user-authored text is the same in all three. */
@Serializable
data class Toned(val gentle: String, val coach: String, val savage: String) {
    operator fun get(tone: AgentTone): String = when (tone) {
        AgentTone.GENTLE -> gentle
        AgentTone.COACH -> coach
        AgentTone.SAVAGE -> savage
    }

    companion object {
        fun plain(text: String) = Toned(text, text, text)
    }
}

@Serializable
data class Subtask(
    val id: String,
    val title: String,
    val done: Boolean = false,
    val byAi: Boolean = false,
)

@Serializable
data class Comment(
    val id: String,
    val fromAi: Boolean,
    val authorName: String,
    val body: Toned,
    val time: String,
    val likes: Int = 0,
)

data class Todo(
    val id: String,
    val title: String,
    val note: String? = null,
    val time: String? = null,
    val tag: Tag = Tag.LIFE,
    val icon: TodoIcon = TodoIcon.SPARKLE,
    val done: Boolean = false,
    val priority: Boolean = false,
    val streak: Int = 0,
    val source: TodoSource = TodoSource.USER,
    val sourceNote: String? = null,
    val createdAt: Long = 0L,
    val position: Int = 0,
    val subtasks: List<Subtask> = emptyList(),
    val comments: List<Comment> = emptyList(),
)

/** A model-derived "next step" suggestion linked back to its source todo. */
data class DerivedSuggestion(
    val id: String,
    val sourceTitle: String,
    val sourceIcon: TodoIcon,
    val title: String,
    val tag: Tag,
    val why: Toned,
)

/** Inputs the Agent uses to write the end-of-day review. */
data class ReviewStats(
    val completed: Int,
    val total: Int,
    val carriedOver: Int,
    val topStreak: Int,
    val topStreakLabel: String,
)

/** User-selectable theme. */
enum class ThemeMode { LIGHT, DARK, SYSTEM;
    companion object {
        fun fromKey(key: String?): ThemeMode = entries.firstOrNull { it.name == key } ?: SYSTEM
    }
}
