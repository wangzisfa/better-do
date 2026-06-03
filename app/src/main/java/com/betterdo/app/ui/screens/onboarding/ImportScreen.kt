package com.betterdo.app.ui.screens.onboarding

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.betterdo.app.data.seed.SeedData
import com.betterdo.app.domain.model.AgentTone
import com.betterdo.app.domain.model.Todo
import com.betterdo.app.domain.model.TodoIcon
import com.betterdo.app.domain.model.TodoSource
import com.betterdo.app.ui.components.BdCard
import com.betterdo.app.ui.components.BdGlyph
import com.betterdo.app.ui.components.BdIcon
import com.betterdo.app.ui.components.DodoAvatar
import com.betterdo.app.ui.components.Eyebrow
import com.betterdo.app.ui.components.GhostButton
import com.betterdo.app.ui.components.Glyph
import com.betterdo.app.ui.components.PrimaryButton
import com.betterdo.app.ui.theme.BdTheme
import com.betterdo.app.ui.theme.color
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private enum class Phase { SOURCES, INPUT, SCANNING, REVIEW, TONE }

@Composable
fun ImportScreen(vm: ImportViewModel, onDone: (tone: AgentTone, usedSample: Boolean) -> Unit) {
    var phase by remember { mutableStateOf(Phase.SOURCES) }
    var selectedTone by remember { mutableStateOf(AgentTone.COACH) }
    var usedSample by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        Modifier.fillMaxSize().statusBarsPadding().imePadding()
            .padding(horizontal = 24.dp, vertical = 18.dp),
    ) {
        when (phase) {
            Phase.SOURCES -> SourcesStep(
                onHandwrite = { phase = Phase.INPUT },
                onSample = { usedSample = true; phase = Phase.SCANNING },
            )
            Phase.INPUT -> InputStep(
                text = vm.raw,
                onText = vm::updateRaw,
                onBack = { phase = Phase.SOURCES },
                onContinue = { phase = Phase.SCANNING },
            )
            Phase.SCANNING -> {
                LaunchedEffect(usedSample) {
                    if (usedSample) delay(1200) else vm.parse()
                    phase = Phase.REVIEW
                }
                ScanningStep()
            }
            Phase.REVIEW -> ReviewStep(
                items = if (usedSample) SeedData.seedTodos(0L) else vm.parsed,
                sample = usedSample,
                onBack = { phase = if (usedSample) Phase.SOURCES else Phase.INPUT },
                onContinue = { phase = Phase.TONE },
            )
            Phase.TONE -> ToneStep(
                selected = selectedTone,
                onSelect = { selectedTone = it },
                onDone = {
                    scope.launch {
                        if (usedSample) vm.commitSample(selectedTone) else vm.commitHandwritten(selectedTone)
                        onDone(selectedTone, usedSample)
                    }
                },
            )
        }
    }
}

@Composable
private fun SourcesStep(onHandwrite: () -> Unit, onSample: () -> Unit) {
    val colors = BdTheme.colors
    var hint by remember { mutableStateOf<String?>(null) }
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
            SourceCard(TodoIcon.BOOK, "粘贴 / 手写清单", "备忘录、聊天记录都行", onClick = onHandwrite)
            SourceCard(TodoIcon.CALENDAR, "从日历导入", "今日日程自动带入",
                onClick = { hint = "日历接入还在路上，先粘贴 / 手写一份吧～" })
            SourceCard(TodoIcon.SPARKLE, "连接 Todoist / Notion", "保持双向同步",
                onClick = { hint = "Todoist / Notion 同步还在路上，先手写一份吧～" })
        }
        hint?.let {
            Spacer(Modifier.size(12.dp))
            Text(it, style = MaterialTheme.typography.bodySmall, color = colors.inkFaint)
        }

        Spacer(Modifier.weight(1f))
        PrimaryButton("交给 Agent 整理", onClick = onHandwrite, trailing = Glyph.SPARKLE, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.size(14.dp))
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(
                "没有清单？先看看示例 →",
                style = MaterialTheme.typography.labelLarge,
                color = colors.inkFaint,
                modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable(onClick = onSample)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            )
        }
    }
}

@Composable
private fun SourceCard(icon: TodoIcon, title: String, subtitle: String, onClick: () -> Unit) {
    val colors = BdTheme.colors
    BdCard(Modifier.fillMaxWidth(), onClick = onClick) {
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
private fun InputStep(
    text: String,
    onText: (String) -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit,
) {
    val colors = BdTheme.colors
    val accent = BdTheme.accent
    val count = text.split('\n').count { it.isNotBlank() }
    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.clip(RoundedCornerShape(8.dp)).clickable(onClick = onBack).padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BdGlyph(Glyph.CHEVRON_LEFT, size = 18.dp, tint = colors.inkFaint)
            Spacer(Modifier.width(4.dp))
            Eyebrow("返回")
        }
        Spacer(Modifier.size(12.dp))
        Text("把清单写在这儿", style = MaterialTheme.typography.headlineMedium, color = colors.ink)
        Spacer(Modifier.size(8.dp))
        Text(
            "一行一件事，乱一点没关系。写上时间或地点也行，Agent 会读懂、归类。",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.inkSoft,
        )
        Spacer(Modifier.size(18.dp))
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = colors.surface2,
            modifier = Modifier.fillMaxWidth().weight(1f),
        ) {
            Box(Modifier.fillMaxSize().padding(16.dp)) {
                if (text.isEmpty()) {
                    Text(
                        "例如：\n14:00 OKR 复盘文档\n晚上 7 点 健身房 推日\n给妈回个电话\n买猫粮",
                        style = MaterialTheme.typography.bodyLarge.merge(TextStyle(lineHeight = 28.sp)),
                        color = colors.inkFaint,
                    )
                }
                BasicTextField(
                    value = text,
                    onValueChange = onText,
                    textStyle = MaterialTheme.typography.bodyLarge.merge(TextStyle(color = colors.ink, lineHeight = 28.sp)),
                    cursorBrush = SolidColor(accent),
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        Spacer(Modifier.size(10.dp))
        if (count > 0) {
            Text("已写 $count 件", style = MaterialTheme.typography.labelSmall, color = colors.inkFaint)
            Spacer(Modifier.size(8.dp))
        }
        PrimaryButton(
            "交给 Agent 整理",
            onClick = onContinue,
            enabled = text.isNotBlank(),
            trailing = Glyph.SPARKLE,
            modifier = Modifier.fillMaxWidth(),
        )
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
private fun ReviewStep(
    items: List<Todo>,
    sample: Boolean,
    onBack: () -> Unit,
    onContinue: () -> Unit,
) {
    val colors = BdTheme.colors
    Column(Modifier.fillMaxSize()) {
        Spacer(Modifier.size(8.dp))
        Eyebrow("整理好了")
        Spacer(Modifier.size(10.dp))
        if (items.isEmpty()) {
            Text("没读到清单内容", style = MaterialTheme.typography.headlineMedium, fontStyle = FontStyle.Italic)
            Spacer(Modifier.size(10.dp))
            Text("回去随手写几条吧，一行一件就好。", style = MaterialTheme.typography.bodyLarge, color = colors.inkSoft)
            Spacer(Modifier.weight(1f))
            PrimaryButton("再写一份", onClick = onBack, trailing = Glyph.CHEVRON_LEFT, modifier = Modifier.fillMaxWidth())
            return
        }
        Text("今天 ${items.size} 件事", style = MaterialTheme.typography.headlineMedium, fontStyle = FontStyle.Italic)
        Spacer(Modifier.size(10.dp))
        Text(
            if (sample) "已识别时间和分类，并帮你补了到期的事。"
            else "已识别时间、分好了类，挑顺手的先开始。",
            style = MaterialTheme.typography.bodyLarge,
            color = colors.inkSoft,
        )
        Spacer(Modifier.size(20.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items.take(5).forEach { ParsedRow(it) }
            if (items.size > 5) {
                Text("…还有 ${items.size - 5} 件", style = MaterialTheme.typography.bodySmall, color = colors.inkFaint)
            }
        }
        Spacer(Modifier.weight(1f))
        if (!sample) {
            GhostButton("再补几条", onClick = onBack, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.size(10.dp))
        }
        PrimaryButton("选一个 Agent 的性格", onClick = onContinue, trailing = Glyph.CHEVRON_RIGHT, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun ParsedRow(todo: Todo) {
    val colors = BdTheme.colors
    val meta = if (todo.source == TodoSource.AI) "Agent 补的 · 到期"
    else listOfNotNull(todo.time, todo.tag.label).joinToString(" · ")
    Row(verticalAlignment = Alignment.CenterVertically) {
        BdIcon(todo.icon, size = 16.dp, tint = colors.inkSoft)
        Spacer(Modifier.width(12.dp))
        Text(
            todo.title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.width(8.dp))
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
