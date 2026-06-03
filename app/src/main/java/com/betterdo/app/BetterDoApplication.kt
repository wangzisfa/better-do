package com.betterdo.app

import android.app.Application
import com.betterdo.app.di.AppContainer
import com.betterdo.app.notifications.NotificationHelper
import com.betterdo.app.notifications.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BetterDoApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        NotificationHelper.ensureChannel(this)
        // Seed the sample day on first launch so the designed experience is visible,
        // then arm the day's reminders in the user's current persona.
        CoroutineScope(Dispatchers.IO).launch {
            container.todoRepository.ensureSeeded()
            val tone = container.settingsRepository.settings.first().tone
            ReminderScheduler.scheduleSeedReminders(this@BetterDoApplication, tone)
        }
    }
}
