package com.betterdo.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.betterdo.app.domain.model.AgentTone

/**
 * Warm "paper" palette, ported verbatim from the design tokens
 * (`:root` / `.bd-screen[data-theme]`). Held in a custom [BetterDoColors] so the
 * editorial look is independent of Material's default scheme.
 */
@Immutable
data class BetterDoColors(
    val paper: Color,
    val surface: Color,
    val surface2: Color,
    val ink: Color,
    val inkSoft: Color,
    val inkFaint: Color,
    val line: Color,
    val line2: Color,
    val doneInk: Color,
    val accent: Color,
    val isDark: Boolean,
)

// ---- Accent swatches (default = ember). Stored as plain ARGB longs because
// Compose's Color.value is a packed representation, not a raw ARGB int. ----
const val DefaultAccentArgb: Long = 0xFFF0502E

val AccentArgbs: List<Long> =
    listOf(0xFFF0502E, 0xFF4B57D6, 0xFF1F8A5B, 0xFF7A4DE0, 0xFF1B1A17)

val AccentEmber = Color(DefaultAccentArgb)
val AccentSwatches: List<Color> = AccentArgbs.map { Color(it) }

fun accentColor(argb: Long): Color = Color(argb)

fun lightColors(accent: Color = AccentEmber) = BetterDoColors(
    paper = Color(0xFFFAF8F3),
    surface = Color(0xFFFFFFFF),
    surface2 = Color(0xFFF2EFE8),
    ink = Color(0xFF1B1A17),
    inkSoft = Color(0xFF6E6A61),
    inkFaint = Color(0xFFA8A39A),
    line = Color(0x171B1A17),
    line2 = Color(0x0D1B1A17),
    doneInk = Color(0xFFB6B1A6),
    accent = accent,
    isDark = false,
)

fun darkColors(accent: Color = AccentEmber) = BetterDoColors(
    paper = Color(0xFF111110),
    surface = Color(0xFF1B1A18),
    surface2 = Color(0xFF26241F),
    ink = Color(0xFFF3F0E9),
    inkSoft = Color(0xFFA39E93),
    inkFaint = Color(0xFF6C685F),
    line = Color(0x1AF3F0E9),
    line2 = Color(0x0DF3F0E9),
    doneInk = Color(0xFF5C584F),
    accent = accent,
    isDark = true,
)

/** The persona's signature color. */
fun AgentTone.color(): Color = Color(colorArgb)

/** A tint of a color mixed toward transparency, used for soft persona washes. */
fun Color.softWash(alpha: Float = 0.10f): Color = copy(alpha = alpha)
