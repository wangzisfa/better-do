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
        NotificationHelper.show(applicationContext, id, title, text)
        return Result.success()
    }

    companion object {
        const val KEY_TITLE = "title"
        const val KEY_TEXT = "text"
        const val KEY_ID = "id"
    }
}
