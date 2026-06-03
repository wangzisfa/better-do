package com.betterdo.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.betterdo.app.domain.model.AgentTone
import com.betterdo.app.domain.model.Todo
import com.betterdo.app.domain.model.TodoSource
import com.betterdo.app.ui.theme.BdTheme
import com.betterdo.app.ui.theme.color

@Composable
fun UserAvatar(size: androidx.compose.ui.unit.Dp = 32.dp, label: String = "我") {
    val colors = BdTheme.colors
    Box(
        Modifier.size(size).clip(CircleShape).background(colors.surface2),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = colors.inkSoft)
    }
}

@Composable
fun FocusChip() {
    val accent = BdTheme.accent
    Surface(shape = CircleShape, color = accent.copy(alpha = 0.12f), contentColor = accent) {
        Text(
            "聚焦",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
        )
    }
}

@Composable
fun TodoRow(
    todo: Todo,
    tone: AgentTone,
    onToggle: () -> Unit,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = BdTheme.colors
    BdCard(modifier = modifier.fillMaxWidth(), onClick = onOpen, elevation = 2.dp) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
            CheckCircle(done = todo.done, onClick = onToggle, size = 24.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BdIcon(todo.icon, size = 16.dp, tint = colors.inkSoft, strokeWidth = 1.8f)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        todo.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (todo.done) colors.doneInk else colors.ink,
                        textDecoration = if (todo.done) TextDecoration.LineThrough else null,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (todo.priority) {
                        Spacer(Modifier.width(6.dp))
                        FocusChip()
                    }
                }

                if (!todo.note.isNullOrBlank()) {
                    Spacer(Modifier.size(3.dp))
                    Text(
                        todo.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.inkFaint,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Spacer(Modifier.size(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    TagChip(todo.tag)
                    TimeChip(todo.time)
                    StreakChip(todo.streak)
                }

                if (todo.source == TodoSource.AI && !todo.sourceNote.isNullOrBlank()) {
                    Spacer(Modifier.size(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BdGlyph(Glyph.SPARKLE, size = 12.dp, tint = tone.color(), strokeWidth = 2f)
                        Spacer(Modifier.width(5.dp))
                        Text(
                            "Agent 加的 · ${todo.sourceNote}",
                            style = MaterialTheme.typography.labelSmall,
                            color = tone.color(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                todo.comments.lastOrNull()?.let { last ->
                    Spacer(Modifier.size(9.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (last.fromAi) DodoAvatar(tone, size = 22.dp) else UserAvatar(size = 22.dp)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            last.body[tone],
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.inkSoft,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "点开回复 →",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.inkFaint,
                            fontSize = 11.sp,
                        )
                    }
                }
            }
        }
    }
}
