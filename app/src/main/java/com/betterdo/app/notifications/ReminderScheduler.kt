package com.betterdo.app.notifications

import android.content.Context
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
 * clock time (next occurrence) with copy resolved for the current persona.
 * Rescheduled whenever the tone changes so the wording stays in voice.
 */
object ReminderScheduler {

    fun scheduleSeedReminders(context: Context, tone: AgentTone) {
        val wm = WorkManager.getInstance(context)
        SeedData.reminders.forEachIndexed { index, reminder ->
            val delay = delayMillisToNext(reminder.time)
            val data = workDataOf(
                ReminderWorker.KEY_TITLE to reminder.title,
                ReminderWorker.KEY_TEXT to reminder.body[tone],
                ReminderWorker.KEY_ID to (2000 + index),
            )
            val request = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .build()
            wm.enqueueUniqueWork(workName(reminder.id), ExistingWorkPolicy.REPLACE, request)
        }
    }

    fun cancelAll(context: Context) {
        val wm = WorkManager.getInstance(context)
        SeedData.reminders.forEach { wm.cancelUniqueWork(workName(it.id)) }
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
