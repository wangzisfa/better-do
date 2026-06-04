package com.betterdo.app.notifications

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.betterdo.app.data.seed.SeedData
import com.betterdo.app.domain.model.AgentTone
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

/**
 * Schedules the day's toned reminders via WorkManager. Reminders fire at their
 * clock time (next occurrence) with copy resolved for the current persona, then
 * re-arm themselves for the next day ([rescheduleNextDay]). Rescheduled whenever
 * the tone changes so the wording stays in voice.
 */
object ReminderScheduler {

    fun scheduleSeedReminders(context: Context, tone: AgentTone) {
        val wm = WorkManager.getInstance(context)
        SeedData.reminders.forEachIndexed { index, reminder ->
            val id = 2000 + index
            val name = workName(reminder.id)
            val data = workDataOf(
                ReminderWorker.KEY_TITLE to reminder.title,
                ReminderWorker.KEY_TEXT to reminder.body[tone],
                ReminderWorker.KEY_ID to id,
                ReminderWorker.KEY_TODO_ID to reminder.todoId,
                ReminderWorker.KEY_WORK_NAME to name,
            )
            enqueue(wm, name, data, delayMillisToNext(reminder.time), TimeUnit.MILLISECONDS)
        }
    }

    /** Re-arm a slot 24h out, carrying the same resolved copy. */
    fun rescheduleNextDay(
        context: Context,
        workName: String,
        id: Int,
        todoId: String?,
        title: String,
        text: String,
    ) {
        val data = workDataOf(
            ReminderWorker.KEY_TITLE to title,
            ReminderWorker.KEY_TEXT to text,
            ReminderWorker.KEY_ID to id,
            ReminderWorker.KEY_TODO_ID to todoId,
            ReminderWorker.KEY_WORK_NAME to workName,
        )
        enqueue(WorkManager.getInstance(context), workName, data, 24, TimeUnit.HOURS)
    }

    /** Fire the same reminder again after [minutes]; one-shot (no daily re-arm). */
    fun snooze(context: Context, id: Int, todoId: String?, title: String, text: String, minutes: Long) {
        val data = workDataOf(
            ReminderWorker.KEY_TITLE to title,
            ReminderWorker.KEY_TEXT to text,
            ReminderWorker.KEY_ID to id,
            ReminderWorker.KEY_TODO_ID to todoId,
        )
        enqueue(WorkManager.getInstance(context), "snooze_$id", data, minutes, TimeUnit.MINUTES)
    }

    fun cancelAll(context: Context) {
        val wm = WorkManager.getInstance(context)
        SeedData.reminders.forEach { wm.cancelUniqueWork(workName(it.id)) }
    }

    private fun enqueue(wm: WorkManager, name: String, data: Data, delay: Long, unit: TimeUnit) {
        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delay, unit)
            .setInputData(data)
            .build()
        wm.enqueueUniqueWork(name, ExistingWorkPolicy.REPLACE, request)
    }

    private fun workName(id: String) = "reminder_$id"

    private fun delayMillisToNext(hhmm: String): Long {
        val parts = hhmm.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 9
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
        val now = LocalDateTime.now()
        var target = now.toLocalDate().atTime(hour, minute)
        if (!target.isAfter(now)) target = target.plusDays(1)
        return Duration.between(now, target).toMillis().coerceAtLeast(0)
    }
}
