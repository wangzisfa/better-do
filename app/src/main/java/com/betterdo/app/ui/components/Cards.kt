package com.betterdo.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.betterdo.app.domain.model.AgentTone
import com.betterdo.app.domain.model.Comment
import com.betterdo.app.domain.model.DerivedSuggestion
import com.betterdo.app.ui.theme.BdTheme
import com.betterdo.app.ui.theme.color
import com.betterdo.app.ui.theme.softWash

@Composable
fun MorningBriefCard(
    tone: AgentTone,
    text: String,
    onSeeSuggestions: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = BdTheme.colors
    BdCard(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                DodoAvatar(tone, size = 30.dp, active = true)
                Spacer(Modifier.width(9.dp))
                Text(
                    "Agent 的晨间简报",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = tone.color(),
                )
            }
            Spacer(Modifier.size(9.dp))
            Text(text, style = MaterialTheme.typography.bodyMedium, color = colors.inkSoft)
            Spacer(Modifier.size(12.dp))
            Row(
                Modifier.clickable(onClick = onSeeSuggestions),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BdGlyph(Glyph.SPARKLE, size = 14.dp, tint = BdTheme.accent, strokeWidth = 2f)
                Spacer(Modifier.width(6.dp))
                Text(
                    "查看 Agent 建议",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = BdTheme.accent,
                )
                Spacer(Modifier.width(3.dp))
                BdGlyph(Glyph.CHEVRON_RIGHT, size = 14.dp, tint = BdTheme.accent, strokeWidth = 2f)
            }
        }
    }
}

@Composable
fun CommentBubble(
    comment: Comment,
    tone: AgentTone,
    onLike: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = BdTheme.colors
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        if (comment.fromAi) DodoAvatar(tone, size = 32.dp) else UserAvatar(32.dp)
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(comment.authorName, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.width(6.dp))
                Text(comment.time, style = MaterialTheme.typography.labelSmall, color = colors.inkFaint)
            }
            Spacer(Modifier.size(5.dp))
            Surface(
                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 14.dp, bottomEnd = 14.dp, bottomStart = 14.dp),
                color = if (comment.fromAi) tone.color().softWash(0.10f) else colors.surface2,
                contentColor = colors.ink,
            ) {
                Text(
                    comment.body[tone],
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                )
            }
            Spacer(Modifier.size(5.dp))
            Row(
                Modifier.clickable(onClick = onLike),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BdGlyph(
                    Glyph.HEART, size = 13.dp,
                    tint = if (comment.likes > 0) BdTheme.accent else colors.inkFaint,
                    strokeWidth = 1.9f,
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    if (comment.likes > 0) comment.likes.toString() else "赞",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.inkFaint,
                )
            }
        }
    }
}

@Composable
fun DeriveCardView(
    suggestion: DerivedSuggestion,
    tone: AgentTone,
    pending: Boolean,
    accepted: Boolean,
    onAccept: () -> Unit,
    onIgnore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = BdTheme.colors
    BdCard(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BdIcon(suggestion.sourceIcon, size = 14.dp, tint = colors.inkFaint, strokeWidth = 1.7f)
                Spacer(Modifier.width(6.dp))
                Text("源自 ${suggestion.sourceTitle}", style = MaterialTheme.typography.labelSmall, color = colors.inkFaint)
                Spacer(Modifier.weight(1f))
                BdGlyph(Glyph.SPARKLE, size = 12.dp, tint = tone.color(), strokeWidth = 2f)
                Spacer(Modifier.width(4.dp))
                Text("Agent 派生", style = MaterialTheme.typography.labelSmall, color = tone.color())
            }
            Spacer(Modifier.size(8.dp))
            Text(suggestion.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.size(4.dp))
            Text(suggestion.why[tone], style = MaterialTheme.typography.bodySmall, color = colors.inkSoft)
            Spacer(Modifier.size(13.dp))
            when {
                pending -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    GhostButton("忽略", onIgnore, Modifier.weight(1f))
                    PrimaryButton("加到今天", onAccept, Modifier.weight(1f), trailing = Glyph.PLUS)
                }
                accepted -> Row(verticalAlignment = Alignment.CenterVertically) {
                    BdGlyph(Glyph.CHECK, size = 15.dp, tint = tone.color(), strokeWidth = 2.4f)
                    Spacer(Modifier.width(6.dp))
                    Text("已加到「今天」", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = tone.color())
                }
                else -> Text("已忽略", style = MaterialTheme.typography.labelMedium, color = colors.inkFaint)
            }
        }
    }
}
