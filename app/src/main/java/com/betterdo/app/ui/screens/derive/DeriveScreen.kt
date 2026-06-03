package com.betterdo.app.ui.screens.derive

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.betterdo.app.ui.components.BdCard
import com.betterdo.app.ui.components.BdGlyph
import com.betterdo.app.ui.components.DeriveCardView
import com.betterdo.app.ui.components.DodoAvatar
import com.betterdo.app.ui.components.Glyph
import com.betterdo.app.ui.components.PrimaryButton
import com.betterdo.app.ui.theme.BdTheme
import com.betterdo.app.ui.theme.color

@Composable
fun DeriveScreen(vm: DeriveViewModel, onBack: () -> Unit) {
    val colors = BdTheme.colors
    val settings by vm.settings.collectAsState()
    val tone = settings.tone
    val state by vm.state.collectAsState()

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BdGlyph(
                Glyph.CHEVRON_LEFT, size = 22.dp, tint = colors.ink,
                modifier = Modifier.size(32.dp).clickable(onClick = onBack),
            )
            Spacer(Modifier.width(4.dp))
            Text("Agent 派生待办", style = MaterialTheme.typography.titleMedium)
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    DodoAvatar(tone, size = 34.dp, active = state.scanning)
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("读完你今天写的事，它顺出了这些", style = MaterialTheme.typography.bodyMedium, color = colors.ink, fontWeight = FontWeight.Medium)
                        Text("加入 / 忽略都行，Agent 会记住你的偏好", style = MaterialTheme.typography.labelSmall, color = colors.inkFaint)
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                    Tally(state.accepted, "已加入", tone.color())
                    Tally(state.ignored, "已忽略", colors.inkFaint)
                    Tally(state.cards.count { state.statuses[it.id] == CardStatus.PENDING }, "待定", colors.inkSoft)
                }
            }

            if (state.scanning) {
                item { ScanningCard(tone) }
            } else {
                items(state.cards, key = { it.id }) { card ->
                    val status = state.statuses[card.id]
                    DeriveCardView(
                        suggestion = card,
                        tone = tone,
                        pending = status == CardStatus.PENDING,
                        accepted = status == CardStatus.ACCEPTED,
                        onAccept = { vm.accept(card) },
                        onIgnore = { vm.ignore(card) },
                    )
                }

                if (state.finished) {
                    item { DoneCard(state.accepted, state.ignored, tone) }
                } else if (state.canThinkMore) {
                    item {
                        PrimaryButton(
                            "让 Agent 再想想",
                            onClick = vm::thinkMore,
                            trailing = Glyph.SPARKLE,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            item {
                Spacer(Modifier.size(4.dp))
                Text(
                    "派生的待办都连回来源，你说了算。",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.inkFaint,
                )
            }
        }
    }
}

@Composable
private fun Tally(n: Int, label: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("$n", style = MaterialTheme.typography.headlineSmall, fontStyle = FontStyle.Italic, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = BdTheme.colors.inkFaint)
    }
}

@Composable
private fun ScanningCard(tone: com.betterdo.app.domain.model.AgentTone) {
    val colors = BdTheme.colors
    BdCard(modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            DodoAvatar(tone, size = 36.dp, active = true)
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Agent 正在读你的清单…", style = MaterialTheme.typography.titleMedium)
                Text("找出每件事顺带该做的下一步", style = MaterialTheme.typography.bodySmall, color = colors.inkSoft)
            }
        }
    }
}

@Composable
private fun DoneCard(accepted: Int, ignored: Int, tone: com.betterdo.app.domain.model.AgentTone) {
    val colors = BdTheme.colors
    BdCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            DodoAvatar(tone, size = 40.dp)
            Spacer(Modifier.size(10.dp))
            Text("都看完啦", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.size(6.dp))
            Text(
                "这一轮加入 $accepted 件、忽略 $ignored 件。Agent 每天还会接着帮你顺新的。",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.inkSoft,
            )
        }
    }
}
