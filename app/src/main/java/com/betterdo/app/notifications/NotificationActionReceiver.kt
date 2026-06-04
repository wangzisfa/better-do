package com.betterdo.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import com.betterdo.app.BetterDoApplication
import com.betterdo.app.domain.model.Comment
import com.betterdo.app.domain.model.Toned
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.LocalTime

/**
 * Handles the action buttons + inline reply attached to reminder notifications.
 * Each branch runs off the main thread via [goAsync] so DB writes finish even
 * after the broadcast returns.
 */
class NotificationActionReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext
        val notifId = intent.getIntExtra(EXTRA_NOTIF_ID, -1)
        val todoId = intent.getStringExtra(EXTRA_TODO_ID)
        val title = intent.getStringExtra(EXTRA_TITLE).orEmpty()
        val text = intent.getStringExtra(EXTRA_TEXT).orEmpty()

        when (intent.action) {
            ACTION_COMPLETE -> {
                val pending = goAsync()
                scope.launch {
                    try {
                        todoId?.let { repo(app).setDone(it, true) }
                        NotificationManagerCompat.from(app).cancel(notifId)
                    } finally {
                        pending.finish()
                    }
                }
            }

            ACTION_SNOOZE -> {
                ReminderScheduler.snooze(app, notifId, todoId, title, text, minutes = 60)
                NotificationManagerCompat.from(app).cancel(notifId)
            }

            ACTION_FOCUS -> {
                FocusService.start(app, minutes = 25)
                NotificationManagerCompat.from(app).cancel(notifId)
            }

            ACTION_REPLY -> {
                val reply = RemoteInput.getResultsFromIntent(intent)
                    ?.getCharSequence(KEY_REPLY)?.toString()?.trim().orEmpty()
                val pending = goAsync()
                scope.launch {
                    try {
                        if (todoId != null && reply.isNotEmpty()) {
                            val now = LocalTime.now()
                            repo(app).appendComment(
                                todoId,
                                Comment(
                                    id = "reply-${System.currentTimeMillis()}",
                                    fromAi = false,
                                    authorName = "我",
                                    body = Toned.plain(reply),
                                    time = "${now.hour}:${"%02d".format(now.minute)}",
                                ),
                            )
                        }
                        NotificationHelper.showReplied(app, notifId, reply)
                    } finally {
                        pending.finish()
                    }
                }
            }
        }
    }

    private fun repo(context: Context) =
        (context as BetterDoApplication).container.todoRepository

    companion object {
        const val ACTION_COMPLETE = "com.betterdo.app.action.COMPLETE"
        const val ACTION_SNOOZE = "com.betterdo.app.action.SNOOZE"
        const val ACTION_FOCUS = "com.betterdo.app.action.FOCUS"
        const val ACTION_REPLY = "com.betterdo.app.action.REPLY"

        const val EXTRA_NOTIF_ID = "notif_id"
        const val EXTRA_TODO_ID = "todo_id"
        const val EXTRA_TITLE = "title"
        const val EXTRA_TEXT = "text"

        const val KEY_REPLY = "key_reply"
    }
}
