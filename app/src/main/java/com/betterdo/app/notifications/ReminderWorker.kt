package com.betterdo.app.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class ReminderWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val title = inputData.getString(KEY_TITLE) ?: return Result.success()
        val text = inputData.getString(KEY_TEXT).orEmpty()
        val id = inputData.getInt(KEY_ID, 1)
        val todoId = inputData.getString(KEY_TODO_ID)
        val workName = inputData.getString(KEY_WORK_NAME)

        NotificationHelper.show(applicationContext, id, todoId, title, text)

        // Daily recurrence: re-arm the same slot for the next day. Snoozed
        // requests carry no work name, so they fire once and stop here.
        if (workName != null) {
            ReminderScheduler.rescheduleNextDay(applicationContext, workName, id, todoId, title, text)
        }
        return Result.success()
    }

    companion object {
        const val KEY_TITLE = "title"
        const val KEY_TEXT = "text"
        const val KEY_ID = "id"
        const val KEY_TODO_ID = "todo_id"
        const val KEY_WORK_NAME = "work_name"
    }
}
