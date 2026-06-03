package com.betterdo.app.notifications

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * A foreground service that powers "专注模式" — an ongoing, low-priority
 * notification with a live progress bar and a Stop action, rendered through a
 * custom RemoteViews layout. Demonstrates the foreground-service notification
 * path (separate from the user-facing reminder notifications).
 */
class FocusService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var ticker: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }
        val minutes = (intent?.getIntExtra(EXTRA_MINUTES, DEFAULT_MINUTES) ?: DEFAULT_MINUTES)
            .coerceIn(1, 180)
        val totalSec = minutes * 60

        startAsForeground(NotificationHelper.buildFocus(this, totalSec, totalSec))

        ticker?.cancel()
        ticker = scope.launch {
            var remaining = totalSec
            while (remaining >= 0 && isActive) {
                val notif = NotificationHelper.buildFocus(this@FocusService, remaining, totalSec)
                if (NotificationHelper.canPost(this@FocusService)) {
                    NotificationManagerCompat.from(this@FocusService)
                        .notify(NotificationHelper.FOCUS_ID, notif)
                }
                delay(1000)
                remaining--
            }
            stopSelf()
        }
        return START_STICKY
    }

    private fun startAsForeground(notification: android.app.Notification) {
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        } else {
            0
        }
        ServiceCompat.startForeground(this, NotificationHelper.FOCUS_ID, notification, type)
    }

    override fun onDestroy() {
        ticker?.cancel()
        scope.cancel()
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    companion object {
        const val ACTION_STOP = "com.betterdo.app.action.FOCUS_STOP"
        const val EXTRA_MINUTES = "minutes"
        const val DEFAULT_MINUTES = 25

        fun start(context: Context, minutes: Int = DEFAULT_MINUTES) {
            val intent = Intent(context, FocusService::class.java)
                .putExtra(EXTRA_MINUTES, minutes)
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, FocusService::class.java).apply { action = ACTION_STOP }
            context.startService(intent)
        }
    }
}
