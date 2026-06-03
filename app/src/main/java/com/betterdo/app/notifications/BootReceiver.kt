package com.betterdo.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.betterdo.app.BetterDoApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Re-arms the day's reminders after a reboot — WorkManager survives reboots on
 * most OEMs, but this makes scheduling robust regardless and keeps the wording
 * in the user's current persona.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val app = context.applicationContext as BetterDoApplication
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val tone = app.container.settingsRepository.settings.first().tone
                ReminderScheduler.scheduleSeedReminders(app, tone)
            } finally {
                pending.finish()
            }
        }
    }
}
