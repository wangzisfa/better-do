package com.betterdo.app.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.betterdo.app.domain.model.AgentTone
import com.betterdo.app.domain.model.TodoIcon
import com.betterdo.app.ui.components.BdCard
import com.betterdo.app.ui.components.BdGlyph
import com.betterdo.app.ui.components.BdIcon
import com.betterdo.app.ui.components.DodoAvatar
import com.betterdo.app.ui.components.Eyebrow
import com.betterdo.app.ui.components.Glyph
import com.betterdo.app.ui.components.PrimaryButton
import com.betterdo.app.ui.theme.BdTheme
import com.betterdo.app.ui.theme.color
import kotlinx.coroutines.delay

private enum class Phase { SOURCES, SCANNING, REVIEW, TONE }

@Composable
fun ImportScreen(onDone: (AgentTone) -> Unit) {
    val colors = BdTheme.colors
    var phase by remember { mutableStateOf(Phase.SOURCES) }
    var selectedTone by remember { mutableStateOf(AgentTone.COACH) }

    Column(
        Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 24.dp, vertical = 18.dp),
    ) {
        when (phase) {
            Phase.SOURCES -> SourcesStep(onContinue = { phase = Phase.SCANNING })
            Phase.SCANNING -> {
                LaunchedEffect(Unit) {
                    delay(1500)
                    phase = Phase.REVIEW
                }
                ScanningStep()
            }
            Phase.REVIEW -> ReviewStep(onContinue = { phase = Phase.TONE })
            Phase.TONE -> ToneStep(
                selected = selectedTone,
                onSelect = { selectedTone = it },
                onDone = { onDone(selectedTone) },
            )
        }
    }
}

@Composable
private fun SourcesStep(onContinue: () -> Unit) {
    val colors = BdTheme.colors
    Column(Modifier.fillMaxSize()) {
        Spacer(Modifier.size(8.dp))
        Text("把你的清单", style = MaterialTheme.typography.headlineMedium, color = colors.ink)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("交给 ", style = MaterialTheme.typography.headlineMedium, color = colors.ink)
            Text("Dodo", style = MaterialTheme.typography.headlineMedium, fontStyle = FontStyle.Italic, color = BdTheme.accent)
        }
        Spacer(Modifier.size(12.dp))
        Text(
            "随手记的乱清单也没关系。Agent 会读懂、整理成今天的待办，还会一直陪你把它们做完。",
            style = MaterialTheme.typography.bodyLarge,
            color = colors.inkSoft,
        )
        Spacer(Modifier.size(24.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SourceCard(TodoIcon.BOOK, "粘贴 / 手写清单", "备忘录、聊天记录都行")
            SourceCard(TodoIcon.CALENDAR, "从日历导入", "今日日程自动带入")
            SourceCard(TodoIcon.SPARKLE, "连接 Todoist / Notion", "保持双向同步")
        }

        Spacer(Modifier.weight(1f))
        PrimaryButton("交给 Agent 整理", onClick = onContinue, trailing = Glyph.SPARKLE, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun SourceCard(icon: TodoIcon, title: String, subtitle: String) {
    val colors = BdTheme.colors
    BdCard(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(40.dp).clip(CircleShape).background(colors.surface2),
                contentAlignment = Alignment.Center,
            ) { BdIcon(icon, size = 18.dp, tint = colors.inkSoft) }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = colors.inkFaint)
            }
            BdGlyph(Glyph.CHEVRON_RIGHT, size = 16.dp, tint = colors.inkFaint)
        }
    }
}

@Composable
private fun ScanningStep() {
    val colors = BdTheme.colors
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            DodoAvatar(AgentTone.COACH, size = 64.dp, active = true)
            Spacer(Modifier.size(18.dp))
            Text("Agent 正在读你的清单…", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.size(6.dp))
            Text("识别时间 · 归类 · 补全你漏掉的事", style = MaterialTheme.typography.bodyMedium, color = colors.inkSoft)
        }
    }
}

@Composable
private fun ReviewStep(onContinue: () -> Unit) {
    val colors = BdTheme.colors
    Column(Modifier.fillMaxSize()) {
        Spacer(Modifier.size(8.dp))
        Eyebrow("整理好了")
        Spacer(Modifier.size(10.dp))
        Text("今天 7 件事", style = MaterialTheme.typography.headlineMedium, fontStyle = FontStyle.Italic)
        Spacer(Modifier.size(10.dp))
        Text(
            "已识别时间和分类，并帮你补了 1 件到期的事。",
            style = MaterialTheme.typography.bodyLarge,
            color = colors.inkSoft,
        )
        Spacer(Modifier.size(20.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            ParsedRow(TodoIcon.TARGET, "季度 OKR 复盘文档", "14:00 · 工作")
            ParsedRow(TodoIcon.DUMBBELL, "推日训练", "19:00 · 健康")
            ParsedRow(TodoIcon.PHONE, "给妈回个电话", "生活")
            ParsedRow(TodoIcon.PIN, "预约牙医复查", "Agent 补的 · 到期")
        }
        Spacer(Modifier.weight(1f))
        PrimaryButton("选一个 Agent 的性格", onClick = onContinue, trailing = Glyph.CHEVRON_RIGHT, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun ParsedRow(icon: TodoIcon, title: String, meta: String) {
    val colors = BdTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically) {
        BdIcon(icon, size = 16.dp, tint = colors.inkSoft)
        Spacer(Modifier.width(12.dp))
        Text(title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Text(meta, style = MaterialTheme.typography.labelSmall, color = colors.inkFaint)
    }
}

@Composable
private fun ToneStep(selected: AgentTone, onSelect: (AgentTone) -> Unit, onDone: () -> Unit) {
    val colors = BdTheme.colors
    Column(Modifier.fillMaxSize()) {
        Spacer(Modifier.size(8.dp))
        Eyebrow("最后一步")
        Spacer(Modifier.size(10.dp))
        Text("Agent 用什么语气陪你？", style = MaterialTheme.typography.headlineMedium, fontStyle = FontStyle.Italic)
        Spacer(Modifier.size(8.dp))
        Text("随时能在设置里调。它会影响提醒文案和评论的口吻。", style = MaterialTheme.typography.bodyMedium, color = colors.inkSoft)
        Spacer(Modifier.size(20.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AgentTone.entries.forEach { tone ->
                ToneOption(tone, selected == tone, onClick = { onSelect(tone) })
            }
        }
        Spacer(Modifier.weight(1f))
        PrimaryButton("进入今天", onClick = onDone, trailing = Glyph.CHEVRON_RIGHT, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
fun ToneOption(tone: AgentTone, selected: Boolean, onClick: () -> Unit) {
    val colors = BdTheme.colors
    BdCard(
        Modifier.fillMaxWidth(),
        onClick = onClick,
        border = true,
        color = if (selected) tone.color().copy(alpha = 0.06f) else colors.surface,
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            DodoAvatar(tone, size = 40.dp, active = selected)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(tone.label, style = MaterialTheme.typography.titleMedium)
                Text(tone.blurb, style = MaterialTheme.typography.bodySmall, color = colors.inkFaint)
            }
            Box(
                Modifier.size(22.dp).clip(CircleShape)
                    .background(if (selected) tone.color() else colors.surface2),
                contentAlignment = Alignment.Center,
            ) {
                if (selected) {
                    BdGlyph(Glyph.CHECK, size = 13.dp, tint = Color.White, strokeWidth = 3f)
                }
            }
        }
    }
}
