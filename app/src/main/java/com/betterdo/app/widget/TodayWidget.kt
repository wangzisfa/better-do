package com.betterdo.app.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.betterdo.app.BetterDoApplication
import com.betterdo.app.MainActivity
import com.betterdo.app.R
import com.betterdo.app.domain.model.Todo
import kotlinx.coroutines.flow.first

/** Brand palette via color resources, so day/night variants resolve automatically. */
private val Accent = ColorProvider(R.color.widget_accent)
private val Surface = ColorProvider(R.color.widget_surface)
private val OnSurface = ColorProvider(R.color.widget_on_surface)
private val Muted = ColorProvider(R.color.widget_muted)
private val Disc = ColorProvider(R.color.widget_disc)

private val TodoIdKey = ActionParameters.Key<String>("todoId")

/** A home-screen widget that lists today's open todos and lets you tick them off. */
class TodayWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val todos = runCatching {
            val repo = (context.applicationContext as BetterDoApplication).container.todoRepository
            repo.observeTodos().first()
                .filter { !it.done }
                .sortedWith(compareByDescending<Todo> { it.priority }.thenBy { it.position })
                .take(5)
        }.getOrDefault(emptyList())

        provideContent { WidgetBody(todos) }
    }
}

@Composable
private fun WidgetBody(todos: List<Todo>) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(Surface)
            .cornerRadius(20.dp)
            .padding(16.dp)
            .clickable(actionStartActivity<MainActivity>()),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = GlanceModifier.fillMaxWidth(),
        ) {
            Text(
                text = "今日待办",
                style = TextStyle(color = OnSurface, fontSize = 16.sp, fontWeight = FontWeight.Bold),
                modifier = GlanceModifier.defaultWeight(),
            )
            Box(modifier = GlanceModifier.clickable(actionRunCallback<RefreshAction>())) {
                Text("↻", style = TextStyle(color = Accent, fontSize = 16.sp))
            }
        }
        Spacer(GlanceModifier.height(10.dp))

        if (todos.isEmpty()) {
            Box(
                modifier = GlanceModifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text("今天清空啦 🎉", style = TextStyle(color = Muted, fontSize = 14.sp))
            }
        } else {
            todos.forEach { todo ->
                TodoLine(todo)
                Spacer(GlanceModifier.height(6.dp))
            }
        }
    }
}

@Composable
private fun TodoLine(todo: Todo) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = GlanceModifier.fillMaxWidth(),
    ) {
        Box(
            modifier = GlanceModifier
                .size(22.dp)
                .cornerRadius(11.dp)
                .background(Disc)
                .clickable(
                    actionRunCallback<ToggleDoneAction>(
                        actionParametersOf(TodoIdKey to todo.id),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text("○", style = TextStyle(color = Accent, fontSize = 13.sp))
        }
        Spacer(GlanceModifier.width(10.dp))
        Column(modifier = GlanceModifier.defaultWeight()) {
            Text(
                text = todo.title,
                maxLines = 1,
                style = TextStyle(color = OnSurface, fontSize = 14.sp),
            )
            todo.time?.let {
                Text(it, style = TextStyle(color = Muted, fontSize = 11.sp))
            }
        }
        if (todo.priority) {
            Text("★", style = TextStyle(color = Accent, fontSize = 13.sp))
        }
    }
}

/** Tick a todo done straight from the widget, then refresh that instance. */
class ToggleDoneAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val todoId = parameters[TodoIdKey] ?: return
        (context.applicationContext as BetterDoApplication).container.todoRepository.setDone(todoId, true)
        TodayWidget().update(context, glanceId)
    }
}

class RefreshAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        TodayWidget().update(context, glanceId)
    }
}
