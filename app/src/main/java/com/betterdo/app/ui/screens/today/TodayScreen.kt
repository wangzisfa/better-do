package com.betterdo.app.ui.screens.today

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.betterdo.app.domain.model.Todo
import com.betterdo.app.ui.components.BdGlyph
import com.betterdo.app.ui.components.Eyebrow
import com.betterdo.app.ui.components.Glyph
import com.betterdo.app.ui.components.MorningBriefCard
import com.betterdo.app.ui.components.SectionHeader
import com.betterdo.app.ui.components.TodoRow
import com.betterdo.app.data.seed.SeedData
import com.betterdo.app.ui.theme.BdTheme

@Composable
fun TodayScreen(
    vm: TodayViewModel,
    onOpenTodo: (String) -> Unit,
    onOpenDerive: () -> Unit,
    onOpenSettings: () -> Unit,
    showToast: (String) -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val colors = BdTheme.colors
    val settings by vm.settings.collectAsState()
    val todos by vm.todos.collectAsState()
    val toast by vm.toast.collectAsState()
    val tone = settings.tone

    LaunchedEffect(toast) {
        toast?.let { showToast(it); vm.clearToast() }
    }

    val focus = todos.filter { it.priority && !it.done }
    val rest = todos.filter { !it.priority && !it.done }
    val done = todos.filter { it.done }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Column(Modifier.padding(horizontal = 20.dp).padding(top = 8.dp, bottom = 2.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Eyebrow("${SeedData.WEEKDAY} · ${SeedData.DATE_LABEL}")
                    Spacer(Modifier.weight(1f))
                    BdGlyph(
                        Glyph.DOTS, size = 20.dp, tint = colors.inkFaint,
                        modifier = Modifier.size(28.dp).clickable(onClick = onOpenSettings),
                    )
                }
                Spacer(Modifier.size(8.dp))
                Text(
                    vm.greeting(tone),
                    style = MaterialTheme.typography.headlineMedium,
                    fontStyle = FontStyle.Italic,
                    color = colors.ink,
                )
            }
        }

        item {
            MorningBriefCard(
                tone = tone,
                text = vm.brief(tone),
                onSeeSuggestions = onOpenDerive,
                modifier = Modifier.padding(horizontal = 20.dp).padding(top = 6.dp, bottom = 4.dp),
            )
        }

        if (focus.isNotEmpty()) {
            sectionHeader("聚焦 · 今天最重要")
            todoItems(focus, tone, vm, onOpenTodo)
        }

        sectionHeader("今天 · ${rest.size} 件")
        if (rest.isEmpty()) {
            item {
                Text(
                    "都清空啦，给自己点个赞 ☺︎",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.inkFaint,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                )
            }
        }
        todoItems(rest, tone, vm, onOpenTodo)

        if (done.isNotEmpty()) {
            sectionHeader("已完成 · ${done.size}")
            todoItems(done, tone, vm, onOpenTodo)
        }

        item { Spacer(Modifier.size(20.dp)) }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.sectionHeader(title: String) {
    item {
        SectionHeader(
            title,
            modifier = Modifier.padding(horizontal = 20.dp).padding(top = 10.dp, bottom = 2.dp),
        )
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.todoItems(
    todos: List<Todo>,
    tone: com.betterdo.app.domain.model.AgentTone,
    vm: TodayViewModel,
    onOpenTodo: (String) -> Unit,
) {
    items(todos, key = { it.id }) { todo ->
        TodoRow(
            todo = todo,
            tone = tone,
            onToggle = { vm.toggleDone(todo) },
            onOpen = { onOpenTodo(todo.id) },
            modifier = Modifier.padding(horizontal = 20.dp),
        )
    }
}
