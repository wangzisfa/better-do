package com.betterdo.app.ui.screens.settings

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.betterdo.app.data.prefs.Settings
import com.betterdo.app.di.AppContainer
import com.betterdo.app.domain.model.AgentTone
import com.betterdo.app.domain.model.ThemeMode
import com.betterdo.app.notifications.ReminderScheduler
import com.betterdo.app.ui.components.BdGlyph
import com.betterdo.app.ui.components.Glyph
import com.betterdo.app.ui.components.SectionHeader
import com.betterdo.app.ui.screens.onboarding.ToneOption
import com.betterdo.app.ui.theme.AccentArgbs
import com.betterdo.app.ui.theme.BdTheme
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(container: AppContainer, onBack: () -> Unit) {
    val colors = BdTheme.colors
    val settings by container.settingsRepository.settings.collectAsState(initial = Settings())
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val prefs = container.settingsRepository

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
            Text("设置", style = MaterialTheme.typography.titleMedium)
        }

        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Section("AGENT 语气") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    AgentTone.entries.forEach { tone ->
                        ToneOption(tone, settings.tone == tone, onClick = {
                            scope.launch {
                                prefs.setTone(tone)
                                ReminderScheduler.scheduleSeedReminders(context, tone)
                            }
                        })
                    }
                }
            }

            Section("主题") {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ThemeChip("浅色", settings.themeMode == ThemeMode.LIGHT) { scope.launch { prefs.setThemeMode(ThemeMode.LIGHT) } }
                    ThemeChip("深色", settings.themeMode == ThemeMode.DARK) { scope.launch { prefs.setThemeMode(ThemeMode.DARK) } }
                    ThemeChip("跟随系统", settings.themeMode == ThemeMode.SYSTEM) { scope.launch { prefs.setThemeMode(ThemeMode.SYSTEM) } }
                }
            }

            Section("强调色") {
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    AccentArgbs.forEach { argb ->
                        AccentDot(argb, settings.accentArgb == argb) { scope.launch { prefs.setAccent(argb) } }
                    }
                }
            }

            Section("AI 评论") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("在待办上显示 Agent 评论", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    Switch(
                        checked = settings.aiComments,
                        onCheckedChange = { value -> scope.launch { prefs.setAiComments(value) } },
                    )
                }
            }

            Spacer(Modifier.size(20.dp))
        }
    }
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
private fun ThemeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = BdTheme.colors
    val accent = BdTheme.accent
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (selected) accent.copy(alpha = 0.10f) else colors.surface2,
        contentColor = if (selected) accent else colors.inkSoft,
        border = if (selected) BorderStroke(1.dp, accent) else null,
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp))
    }
}

@Composable
private fun AccentDot(argb: Long, selected: Boolean, onClick: () -> Unit) {
    val ring = BdTheme.colors.ink
    Box(
        Modifier.size(38.dp).clip(CircleShape)
            .then(if (selected) Modifier.background(ring) else Modifier)
            .clickable(onClick = onClick)
            .padding(if (selected) 3.dp else 0.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier.fillMaxSize().clip(CircleShape).background(Color(argb)),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) BdGlyph(Glyph.CHECK, size = 16.dp, tint = Color.White, strokeWidth = 2.6f)
        }
    }
}
