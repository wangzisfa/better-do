package com.betterdo.app.ui.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.betterdo.app.domain.model.Subtask
import com.betterdo.app.domain.model.Todo
import com.betterdo.app.domain.model.TodoSource
import com.betterdo.app.ui.components.BdGlyph
import com.betterdo.app.ui.components.BdIcon
import com.betterdo.app.ui.components.CheckCircle
import com.betterdo.app.ui.components.CommentBubble
import com.betterdo.app.ui.components.FocusChip
import com.betterdo.app.ui.components.GhostButton
import com.betterdo.app.ui.components.Glyph
import com.betterdo.app.ui.components.SectionHeader
import com.betterdo.app.ui.theme.BdTheme
import com.betterdo.app.ui.theme.color

@Composable
fun TodoDetailScreen(vm: DetailViewModel, onBack: () -> Unit) {
    val colors = BdTheme.colors
    val settings by vm.settings.collectAsState()
    val tone = settings.tone
    val todo by vm.todo.collectAsState()
    val busy by vm.busy.collectAsState()

    Column(Modifier.fillMaxSize().background(colors.paper)) {
        Row(
            Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BdGlyph(
                Glyph.CHEVRON_LEFT, size = 22.dp, tint = colors.ink,
                modifier = Modifier.size(32.dp).clickable(onClick = onBack),
            )
            Spacer(Modifier.width(4.dp))
            Text("待办详情", style = MaterialTheme.typography.titleMedium)
        }

        val t = todo
        if (t == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("加载中…", color = colors.inkFaint)
            }
            return@Column
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { DetailHeader(t, tone, onToggleDone = vm::toggleDone) }

            item {
                if (t.subtasks.isNotEmpty()) {
                    Column {
                        SectionHeader("Agent 拆的步骤")
                        Spacer(Modifier.size(8.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            t.subtasks.forEach { sub ->
                                SubtaskRow(sub, onToggle = { vm.toggleSubtask(sub.id) })
                            }
                        }
                    }
                } else {
                    GhostButton(
                        text = if (busy) "Agent 拆解中…" else "让 Agent 拆成几步",
                        onClick = { if (!busy) vm.splitIntoSteps() },
                        trailing = Glyph.SPARKLE,
                    )
                }
            }

            item {
                SectionHeader("讨论 · ${t.comments.size}")
                Spacer(Modifier.size(4.dp))
            }
            items(t.comments, key = { it.id }) { c ->
                CommentBubble(comment = c, tone = tone, onLike = { vm.like(c.id) })
            }
            item { Spacer(Modifier.size(12.dp)) }
        }

        ReplyBar(placeholder = "回复 Agent（${tone.short}）…", onSend = vm::reply)
    }
}

@Composable
private fun DetailHeader(todo: Todo, tone: com.betterdo.app.domain.model.AgentTone, onToggleDone: () -> Unit) {
    val colors = BdTheme.colors
    Row(verticalAlignment = Alignment.Top) {
        CheckCircle(todo.done, onClick = onToggleDone, size = 28.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BdIcon(todo.icon, size = 18.dp, tint = colors.inkSoft)
                Spacer(Modifier.width(8.dp))
                if (todo.priority) {
                    FocusChip()
                    Spacer(Modifier.width(6.dp))
                }
            }
            Spacer(Modifier.size(6.dp))
            Text(
                todo.title,
                style = MaterialTheme.typography.headlineSmall,
                color = if (todo.done) colors.doneInk else colors.ink,
                textDecoration = if (todo.done) TextDecoration.LineThrough else null,
            )
            if (!todo.note.isNullOrBlank()) {
                Spacer(Modifier.size(6.dp))
                Text(todo.note, style = MaterialTheme.typography.bodyMedium, color = colors.inkSoft)
            }
            if (todo.source == TodoSource.AI && !todo.sourceNote.isNullOrBlank()) {
                Spacer(Modifier.size(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BdGlyph(Glyph.SPARKLE, size = 13.dp, tint = tone.color(), strokeWidth = 2f)
                    Spacer(Modifier.width(5.dp))
                    Text(
                        "Agent 加的 · ${todo.sourceNote}",
                        style = MaterialTheme.typography.labelMedium,
                        color = tone.color(),
                    )
                }
            }
        }
    }
}

@Composable
private fun SubtaskRow(sub: Subtask, onToggle: () -> Unit) {
    val colors = BdTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically) {
        CheckCircle(sub.done, onClick = onToggle, size = 20.dp)
        Spacer(Modifier.width(10.dp))
        Text(
            sub.title,
            style = MaterialTheme.typography.bodyLarge,
            color = if (sub.done) colors.doneInk else colors.ink,
            textDecoration = if (sub.done) TextDecoration.LineThrough else null,
            modifier = Modifier.weight(1f, fill = false),
        )
        if (sub.byAi) {
            Spacer(Modifier.width(6.dp))
            BdGlyph(Glyph.SPARKLE, size = 11.dp, tint = colors.inkFaint, strokeWidth = 1.8f)
        }
    }
}

@Composable
private fun ReplyBar(placeholder: String, onSend: (String) -> Unit) {
    val colors = BdTheme.colors
    val accent = BdTheme.accent
    var text by remember { mutableStateOf("") }
    Surface(color = colors.surface, shadowElevation = 8.dp) {
        Row(
            Modifier.fillMaxWidth().navigationBarsPadding().imePadding().padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = colors.surface2,
                modifier = Modifier.weight(1f),
            ) {
                Box(Modifier.padding(horizontal = 14.dp, vertical = 11.dp)) {
                    if (text.isEmpty()) {
                        Text(placeholder, style = MaterialTheme.typography.bodyMedium, color = colors.inkFaint)
                    }
                    BasicTextField(
                        value = text,
                        onValueChange = { text = it },
                        textStyle = MaterialTheme.typography.bodyMedium.merge(androidx.compose.ui.text.TextStyle(color = colors.ink)),
                        cursorBrush = SolidColor(accent),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            Spacer(Modifier.width(10.dp))
            Box(
                Modifier.size(42.dp).clip(CircleShape).background(accent)
                    .clickable {
                        val t = text.trim()
                        if (t.isNotEmpty()) {
                            onSend(t)
                            text = ""
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                BdGlyph(Glyph.CHEVRON_RIGHT, size = 20.dp, tint = androidx.compose.ui.graphics.Color.White, strokeWidth = 2.6f)
            }
        }
    }
}
