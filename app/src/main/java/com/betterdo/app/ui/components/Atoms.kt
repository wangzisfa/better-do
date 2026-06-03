package com.betterdo.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.betterdo.app.domain.model.Tag
import com.betterdo.app.ui.theme.BdTheme
import com.betterdo.app.ui.theme.Radii

@Composable
fun BdCard(
    modifier: Modifier = Modifier,
    color: Color = BdTheme.colors.surface,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(Radii.card),
    border: Boolean = true,
    elevation: Dp = 2.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val colors = BdTheme.colors
    val stroke = if (border) BorderStroke(1.dp, colors.line) else null
    if (onClick != null) {
        Surface(
            onClick = onClick, modifier = modifier, shape = shape, color = color,
            contentColor = colors.ink, shadowElevation = elevation, border = stroke,
            content = content,
        )
    } else {
        Surface(
            modifier = modifier, shape = shape, color = color, contentColor = colors.ink,
            shadowElevation = elevation, border = stroke, content = content,
        )
    }
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    trailing: Glyph? = null,
) {
    val accent = BdTheme.accent
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(50.dp),
        shape = RoundedCornerShape(14.dp),
        color = if (enabled) accent else accent.copy(alpha = 0.35f),
        contentColor = Color.White,
    ) {
        Row(
            Modifier.fillMaxHeight().padding(horizontal = 22.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            if (trailing != null) {
                Spacer(Modifier.width(6.dp))
                BdGlyph(trailing, size = 16.dp, tint = Color.White, strokeWidth = 2.4f)
            }
        }
    }
}

@Composable
fun GhostButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: Glyph? = null,
) {
    val colors = BdTheme.colors
    Surface(
        onClick = onClick,
        modifier = modifier.height(46.dp),
        shape = RoundedCornerShape(13.dp),
        color = Color.Transparent,
        contentColor = colors.ink,
        border = BorderStroke(1.dp, colors.line),
    ) {
        Row(
            Modifier.fillMaxHeight().padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(text, style = MaterialTheme.typography.labelLarge, color = colors.inkSoft)
            if (trailing != null) {
                Spacer(Modifier.width(6.dp))
                BdGlyph(trailing, size = 15.dp, tint = colors.inkSoft, strokeWidth = 2.2f)
            }
        }
    }
}

@Composable
private fun ChipBase(content: @Composable RowScope.() -> Unit) {
    Surface(
        shape = RoundedCornerShape(Radii.chip),
        color = BdTheme.colors.surface2,
        contentColor = BdTheme.colors.inkSoft,
    ) {
        Row(
            Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )
    }
}

@Composable
fun TagChip(tag: Tag) = ChipBase {
    Box(Modifier.size(7.dp).clip(CircleShape).background(Color(tag.colorArgb)))
    Spacer(Modifier.width(5.dp))
    Text(tag.label, style = MaterialTheme.typography.labelSmall)
}

@Composable
fun TimeChip(time: String?) {
    if (time.isNullOrBlank()) return
    ChipBase {
        BdGlyph(Glyph.CLOCK, size = 12.dp, tint = BdTheme.colors.inkSoft, strokeWidth = 2.1f)
        Spacer(Modifier.width(4.dp))
        Text(time, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun StreakChip(streak: Int) {
    if (streak <= 0) return
    ChipBase {
        Text("🔥", fontSize = 10.sp)
        Spacer(Modifier.width(3.dp))
        Text("连续 $streak 天", style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun CheckCircle(
    done: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
) {
    val accent = BdTheme.accent
    val colors = BdTheme.colors
    Box(
        modifier
            .size(size)
            .clip(CircleShape)
            .then(
                if (done) Modifier.background(accent)
                else Modifier.border(2.dp, colors.inkFaint, CircleShape)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (done) {
            BdGlyph(Glyph.CHECK, size = size * 0.58f, tint = Color.White, strokeWidth = 2.8f)
        }
    }
}

@Composable
fun Eyebrow(text: String, color: Color = BdTheme.colors.inkFaint) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = color,
        letterSpacing = 1.5.sp,
        fontWeight = FontWeight.Medium,
    )
}

@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier, trailing: String? = null) {
    Row(
        modifier.padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            title,
            style = MaterialTheme.typography.labelMedium,
            color = BdTheme.colors.inkFaint,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.6.sp,
        )
        if (trailing != null) {
            Spacer(Modifier.width(8.dp))
            Text(trailing, style = MaterialTheme.typography.labelSmall, color = BdTheme.colors.inkFaint)
        }
    }
}
