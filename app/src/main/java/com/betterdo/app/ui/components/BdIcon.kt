package com.betterdo.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.betterdo.app.domain.model.TodoIcon

/**
 * Stroke icon set ported from the design (24px grid, round caps, currentColor).
 * Each icon is an SVG path string rendered via [PathParser]; circles are written
 * as twin arcs so the whole set is one code path.
 */

enum class Glyph { CHECK, PLUS, CHEVRON_RIGHT, CHEVRON_LEFT, CLOCK, BELL, SPARKLE, FLAME, DOTS, REPLY, HEART, CALENDAR }

private fun circle(cx: Double, cy: Double, r: Double) =
    "M${cx - r},$cy a$r,$r 0 1 0 ${2 * r},0 a$r,$r 0 1 0 ${-2 * r},0 Z"

private val ICON_PATHS: Map<TodoIcon, String> = mapOf(
    TodoIcon.TARGET to (circle(12.0, 12.0, 9.0) + " " + circle(12.0, 12.0, 4.5) + " " + circle(12.0, 12.0, 1.4)),
    TodoIcon.DUMBBELL to "M6.5,6.5 L17.5,17.5 M4,9 l-1,1 l11,11 l1,-1 M9,4 l-1,1 l11,11 l1,-1",
    TodoIcon.PHONE to "M6,3 h3 l1.5,4.5 L8,9 a11,11 0 0 0 7,7 l1.5,-2.5 L21,15 v3 a2,2 0 0 1 -2,2 A16,16 0 0 1 4,5 a2,2 0 0 1 2,-2 Z",
    TodoIcon.CODE to "M9,8 L5,12 L9,16 M15,8 L19,12 L15,16",
    TodoIcon.CART to ("M3,4 h2 l2.2,11 h11 l1.8,-8 H6 " + circle(9.0, 20.0, 1.4) + " " + circle(18.0, 20.0, 1.4)),
    TodoIcon.BOOK to "M5,4 h10 a2,2 0 0 1 2,2 v14 H7 a2,2 0 0 0 -2,2 V4 Z M17,4 v16",
    TodoIcon.PIN to ("M12,21.5 C12,21.5 19,14.5 19,9.5 A7,7 0 1 0 5,9.5 C5,14.5 12,21.5 12,21.5 Z " + circle(12.0, 9.5, 2.4)),
    TodoIcon.SUN to (circle(12.0, 12.0, 4.0) + " M12,2.5 V5 M12,19 V21.5 M2.5,12 H5 M19,12 H21.5 M5.2,5.2 L6.9,6.9 M17.1,17.1 L18.8,18.8 M18.8,5.2 L17.1,6.9 M6.9,17.1 L5.2,18.8"),
    TodoIcon.BELL to "M6,9 a6,6 0 0 1 12,0 c0,5 2,6 2,6 H4 s2,-1 2,-6 Z M10,20 a2,2 0 0 0 4,0",
    TodoIcon.FLAME to "M12,3 c1,3 -1,4 -1,6 a3,3 0 0 0 6,0 c0,-1 -0.3,-2 -0.8,-2.7 C17.5,9 18,11 18,13 a6,6 0 1 1 -12,0 c0,-3.5 3,-5 4,-7 c0.6,-1.2 0.7,-2 0,-3 Z",
    TodoIcon.CLOCK to (circle(12.0, 12.0, 8.5) + " M12,7.5 L12,12 L15.5,14"),
    TodoIcon.CHECK to "M4,12.5 L9.5,18 L20,6.5",
    TodoIcon.SPARKLE to "M12,3 L13.7,8.3 L19,10 L13.7,11.7 L12,17 L10.3,11.7 L5,10 L10.3,8.3 Z",
    TodoIcon.CALENDAR to "M4,5 h16 v15 a1,1 0 0 1 -1,1 H5 a1,1 0 0 1 -1,-1 Z M4,9.5 H20 M8.5,3 V6 M15.5,3 V6",
    TodoIcon.PLUS to "M12,5 L12,19 M5,12 L19,12",
    TodoIcon.DOTS to (circle(5.0, 12.0, 1.4) + " " + circle(12.0, 12.0, 1.4) + " " + circle(19.0, 12.0, 1.4)),
)

private val GLYPH_PATHS: Map<Glyph, String> = mapOf(
    Glyph.CHECK to ICON_PATHS.getValue(TodoIcon.CHECK),
    Glyph.PLUS to ICON_PATHS.getValue(TodoIcon.PLUS),
    Glyph.CHEVRON_RIGHT to "M9,6 L15,12 L9,18",
    Glyph.CHEVRON_LEFT to "M15,6 L9,12 L15,18",
    Glyph.CLOCK to ICON_PATHS.getValue(TodoIcon.CLOCK),
    Glyph.BELL to ICON_PATHS.getValue(TodoIcon.BELL),
    Glyph.SPARKLE to ICON_PATHS.getValue(TodoIcon.SPARKLE),
    Glyph.FLAME to ICON_PATHS.getValue(TodoIcon.FLAME),
    Glyph.DOTS to ICON_PATHS.getValue(TodoIcon.DOTS),
    Glyph.REPLY to "M9,7 L4,12 L9,17 M4,12 H14 a5,5 0 0 1 5,5 V18",
    Glyph.HEART to "M12,20 C12,20 4,15 4,9 a4,4 0 0 1 7.5,-2 A4,4 0 0 1 20,9 c0,6 -8,11 -8,11 Z",
    Glyph.CALENDAR to ICON_PATHS.getValue(TodoIcon.CALENDAR),
)

@Composable
fun BdIcon(
    icon: TodoIcon,
    modifier: Modifier = Modifier,
    size: Dp = 18.dp,
    tint: Color = LocalContentColor.current,
    strokeWidth: Float = 1.9f,
) = StrokePath(ICON_PATHS[icon] ?: ICON_PATHS.getValue(TodoIcon.SPARKLE), modifier, size, tint, strokeWidth)

@Composable
fun BdGlyph(
    glyph: Glyph,
    modifier: Modifier = Modifier,
    size: Dp = 18.dp,
    tint: Color = LocalContentColor.current,
    strokeWidth: Float = 2f,
) = StrokePath(GLYPH_PATHS.getValue(glyph), modifier, size, tint, strokeWidth)

@Composable
private fun StrokePath(d: String, modifier: Modifier, size: Dp, color: Color, strokeWidth: Float) {
    val path = remember(d) { PathParser().parsePathString(d).toPath() }
    Canvas(modifier.then(Modifier).size(size)) {
        val u = this.size.minDimension / 24f
        withTransform({ scale(u, u, pivot = Offset.Zero) }) {
            drawPath(
                path = path,
                color = color,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round),
            )
        }
    }
}
