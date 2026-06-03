package com.betterdo.app.ui.screens.review

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.betterdo.app.data.prefs.Settings
import com.betterdo.app.data.seed.SeedData
import com.betterdo.app.di.AppContainer
import com.betterdo.app.domain.model.ReviewStats
import com.betterdo.app.domain.model.Todo
import com.betterdo.app.ui.components.BdCard
import com.betterdo.app.ui.components.BdIcon
import com.betterdo.app.ui.components.DodoAvatar
import com.betterdo.app.ui.components.ProgressRing
import com.betterdo.app.ui.components.SectionHeader
import com.betterdo.app.ui.theme.BdTheme
import com.betterdo.app.ui.theme.color

@Composable
fun ReviewScreen(container: AppContainer, contentPadding: PaddingValues = PaddingValues(0.dp)) {
    val colors = BdTheme.colors
    val settings by container.settingsRepository.settings.collectAsState(initial = Settings())
    val todos by container.todoRepository.observeTodos().collectAsState(initial = emptyList())
    val tone = settings.tone

    val done = todos.filter { it.done }
    val carried = todos.filter { !it.done }
    val total = todos.size
    val topStreak = todos.maxByOrNull { it.streak }
    val stats = ReviewStats(done.size, total, carried.size, topStreak?.streak ?: 0, topStreak?.title.orEmpty())
    val summary = container.agent.reviewSummary(stats, tone)
    val progress = if (total > 0) done.size.toFloat() / total else 0f

    Box(Modifier.fillMaxSize().padding(contentPadding)) {
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Column {
                    Text("${SeedData.WEEKDAY} · ${SeedData.DATE_LABEL}", style = MaterialTheme.typography.labelSmall, color = colors.inkFaint)
                    Text("今日复盘", style = MaterialTheme.typography.headlineMedium, fontStyle = FontStyle.Italic)
                }
            }

            item {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    ProgressRing(progress = progress, size = 132.dp, stroke = 13.dp) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${done.size}/$total", style = MaterialTheme.typography.headlineSmall, fontStyle = FontStyle.Italic)
                            Text("完成", style = MaterialTheme.typography.labelSmall, color = colors.inkFaint)
                        }
                    }
                }
            }

            item {
                BdCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            DodoAvatar(tone, size = 28.dp)
                            Spacer(Modifier.width(9.dp))
                            Text("Agent 的复盘", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = tone.color())
                        }
                        Spacer(Modifier.size(9.dp))
                        Text(summary, style = MaterialTheme.typography.bodyMedium, color = colors.inkSoft)
                    }
                }
            }

            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Stat(done.size, "完成")
                    Stat(stats.topStreak, "连胜", "天")
                    Stat(carried.size, "顺延")
                }
            }

            if (done.isNotEmpty()) {
                item { SectionHeader("今天划掉的") }
                items(done) { ReviewRow(it, struck = true, tone = tone) }
            }
            if (carried.isNotEmpty()) {
                item { SectionHeader("Agent 帮你挪到明天") }
                itemsIndexed(carried) { index, todo -> ReviewRow(todo, struck = false, tone = tone, firstTomorrow = index == 0) }
            }
            item { Spacer(Modifier.size(10.dp)) }
        }
    }
}

@Composable
private fun Stat(n: Int, label: String, suffix: String = "") {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text("$n", style = MaterialTheme.typography.displayMedium, fontStyle = FontStyle.Italic, color = BdTheme.colors.ink)
            if (suffix.isNotEmpty()) {
                Text(" $suffix", style = MaterialTheme.typography.bodySmall, color = BdTheme.colors.inkFaint)
            }
        }
        Text(label, style = MaterialTheme.typography.labelMedium, color = BdTheme.colors.inkFaint)
    }
}

@Composable
private fun ReviewRow(todo: Todo, struck: Boolean, tone: com.betterdo.app.domain.model.AgentTone, firstTomorrow: Boolean = false) {
    val colors = BdTheme.colors
    BdCard(Modifier.fillMaxWidth(), elevation = 1.dp) {
        Row(Modifier.padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
            BdIcon(todo.icon, size = 16.dp, tint = colors.inkFaint, strokeWidth = 1.7f)
            Spacer(Modifier.width(10.dp))
            Text(
                todo.title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (struck) colors.doneInk else colors.ink,
                textDecoration = if (struck) TextDecoration.LineThrough else null,
                modifier = Modifier.weight(1f),
            )
            if (firstTomorrow) {
                Text("明早第一件", style = MaterialTheme.typography.labelSmall, color = tone.color(), fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
