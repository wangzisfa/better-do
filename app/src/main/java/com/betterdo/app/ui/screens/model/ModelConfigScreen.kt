package com.betterdo.app.ui.screens.model

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.betterdo.app.di.AppContainer
import com.betterdo.app.domain.model.ModelConfig
import com.betterdo.app.domain.model.ModelInfo
import com.betterdo.app.ui.components.BdGlyph
import com.betterdo.app.ui.components.Glyph
import com.betterdo.app.ui.components.GhostButton
import com.betterdo.app.ui.components.PrimaryButton
import com.betterdo.app.ui.components.SectionHeader
import com.betterdo.app.ui.theme.BdTheme

private val OkGreen = Color(0xFF1FA37A)
private val ErrRed = Color(0xFFF0502E)

@Composable
fun ModelConfigScreen(vm: ModelConfigViewModel, onBack: () -> Unit) {
    val colors = BdTheme.colors

    Column(Modifier.fillMaxSize().imePadding()) {
        Row(
            Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BdGlyph(
                Glyph.CHEVRON_LEFT, size = 22.dp, tint = colors.ink,
                modifier = Modifier.size(32.dp).clickable(onClick = onBack),
            )
            Spacer(Modifier.width(4.dp))
            Text("模型接入", style = MaterialTheme.typography.titleMedium)
        }

        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            Text(
                "接入 OpenRouter，让 Dodo 用真实大模型解析清单、派生待办、拆步骤、写评论。关闭时全程使用离线示例引擎。",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.inkSoft,
            )

            // Enable
            Surface(
                shape = RoundedCornerShape(14.dp), color = colors.surface2,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("启用真实模型", style = MaterialTheme.typography.bodyLarge)
                        Text("通过 OpenRouter 调用", style = MaterialTheme.typography.bodySmall, color = colors.inkFaint)
                    }
                    Switch(checked = vm.enabled, onCheckedChange = vm::setEnabled)
                }
            }

            Section("API KEY") {
                FieldBox(
                    value = vm.apiKey,
                    onChange = vm::setApiKey,
                    placeholder = "sk-or-...",
                    visual = PasswordVisualTransformation(),
                    singleLine = true,
                )
                Spacer(Modifier.size(6.dp))
                Text(
                    "在 openrouter.ai/keys 创建，仅保存在本机。",
                    style = MaterialTheme.typography.bodySmall, color = colors.inkFaint,
                )
            }

            Section("BASE URL") {
                FieldBox(
                    value = vm.baseUrl,
                    onChange = vm::setBaseUrl,
                    placeholder = ModelConfig.DEFAULT_BASE_URL,
                    singleLine = true,
                    keyboardType = KeyboardType.Uri,
                )
            }

            Section("选择模型") {
                FieldBox(
                    value = vm.model,
                    onChange = vm::setModel,
                    placeholder = ModelConfig.DEFAULT_MODEL,
                    singleLine = true,
                )
                Spacer(Modifier.size(10.dp))

                val list = pickList(vm)
                Column(
                    Modifier.fillMaxWidth().heightIn(max = 260.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    list.forEach { info ->
                        ModelRow(info, selected = info.id == vm.model.trim()) { vm.setModel(info.id) }
                    }
                }
                Spacer(Modifier.size(10.dp))
                GhostButton(
                    if (vm.fetching) "拉取中…" else "拉取模型列表",
                    onClick = { if (!vm.fetching) vm.fetchModels() },
                    modifier = Modifier.fillMaxWidth(),
                )
                vm.fetchError?.let {
                    Spacer(Modifier.size(6.dp))
                    Text("拉取失败：$it", style = MaterialTheme.typography.bodySmall, color = ErrRed)
                }
                if (vm.fetchedModels.isNotEmpty()) {
                    Spacer(Modifier.size(6.dp))
                    Text(
                        "共 ${vm.fetchedModels.size} 个模型，输入关键字可过滤。",
                        style = MaterialTheme.typography.bodySmall, color = colors.inkFaint,
                    )
                }
            }

            Section("调试") {
                PrimaryButton(
                    if (vm.test is TestState.Running) "测试中…" else "测试连接",
                    onClick = { vm.runTest() },
                    enabled = vm.test !is TestState.Running,
                    trailing = Glyph.SPARKLE,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.size(10.dp))
                TestResult(vm.test)
            }

            PrimaryButton(
                if (vm.saved) "已保存" else "保存配置",
                onClick = { vm.save() },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.size(20.dp))
        }
    }
}

/** Presets, or — once fetched — the live catalog filtered by what's typed. */
private fun pickList(vm: ModelConfigViewModel): List<ModelInfo> {
    if (vm.fetchedModels.isEmpty()) return ModelConfig.PRESETS
    val q = vm.model.trim().lowercase()
    val filtered = if (q.isBlank()) vm.fetchedModels
    else vm.fetchedModels.filter { it.id.lowercase().contains(q) || (it.name?.lowercase()?.contains(q) == true) }
    return filtered.take(60)
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column {
        SectionHeader(title)
        Spacer(Modifier.size(10.dp))
        content()
    }
}

@Composable
private fun FieldBox(
    value: String,
    onChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    visual: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    val colors = BdTheme.colors
    val accent = BdTheme.accent
    Surface(
        shape = RoundedCornerShape(12.dp), color = colors.surface2,
        modifier = modifier.fillMaxWidth(),
    ) {
        Box(Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            if (value.isEmpty()) {
                Text(placeholder, style = MaterialTheme.typography.bodyLarge, color = colors.inkFaint)
            }
            BasicTextField(
                value = value,
                onValueChange = onChange,
                singleLine = singleLine,
                visualTransformation = visual,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = ImeAction.Done),
                textStyle = MaterialTheme.typography.bodyLarge.merge(TextStyle(color = colors.ink)),
                cursorBrush = SolidColor(accent),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun ModelRow(info: ModelInfo, selected: Boolean, onClick: () -> Unit) {
    val colors = BdTheme.colors
    val accent = BdTheme.accent
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (selected) accent.copy(alpha = 0.10f) else colors.surface,
        contentColor = colors.ink,
        border = if (selected) BorderStroke(1.dp, accent) else BorderStroke(1.dp, colors.line),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    info.name ?: info.id, style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
                if (info.name != null) {
                    Text(info.id, style = MaterialTheme.typography.bodySmall, color = colors.inkFaint,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            Box(
                Modifier.size(20.dp).clip(CircleShape)
                    .background(if (selected) accent else colors.surface2),
                contentAlignment = Alignment.Center,
            ) {
                if (selected) BdGlyph(Glyph.CHECK, size = 12.dp, tint = Color.White, strokeWidth = 3f)
            }
        }
    }
}

@Composable
private fun TestResult(state: TestState) {
    val colors = BdTheme.colors
    when (state) {
        TestState.Idle -> Text(
            "保存当前配置并发一条测试消息，验证 Key 与模型是否可用。",
            style = MaterialTheme.typography.bodySmall, color = colors.inkFaint,
        )
        TestState.Running -> Text(
            "正在连接…", style = MaterialTheme.typography.bodyMedium, color = colors.inkSoft,
        )
        is TestState.Ok -> Surface(
            shape = RoundedCornerShape(12.dp),
            color = OkGreen.copy(alpha = 0.12f),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.padding(14.dp)) {
                Text("✓ 连接成功 · ${state.millis}ms", style = MaterialTheme.typography.bodyLarge, color = OkGreen)
                Spacer(Modifier.size(4.dp))
                Text("模型回复：${state.reply}", style = MaterialTheme.typography.bodySmall, color = colors.inkSoft)
            }
        }
        is TestState.Error -> Surface(
            shape = RoundedCornerShape(12.dp),
            color = ErrRed.copy(alpha = 0.12f),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.padding(14.dp)) {
                Text("✕ 连接失败", style = MaterialTheme.typography.bodyLarge, color = ErrRed)
                Spacer(Modifier.size(4.dp))
                Text(state.message, style = MaterialTheme.typography.bodySmall, color = colors.inkSoft)
            }
        }
    }
}
