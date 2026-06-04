package com.betterdo.app.notifications

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import androidx.core.content.ContextCompat
import com.betterdo.app.MainActivity
import com.betterdo.app.R

/**
 * Central builder for every notification BetterDo posts. Covers the full surface:
 *
 * - channels + a channel group (high-importance reminders, low-importance ongoing)
 * - heads-up reminders with a large icon, accent color, vibration & lights
 * - **lock screen privacy**: VISIBILITY_PRIVATE + a redacted public version
 * - action buttons (complete / snooze / focus) and an **inline reply** (RemoteInput)
 * - **grouping**: each reminder is bundled under an InboxStyle summary
 * - a **custom RemoteViews** ongoing notification with a live progress bar (focus mode)
 */
object NotificationHelper {

    const val CHANNEL_REMINDERS = "reminders"
    const val CHANNEL_ONGOING = "ongoing"
    private const val GROUP_KEY = "com.betterdo.app.reminders"
    private const val GROUP_CHANNELS = "betterdo"

    const val SUMMARY_ID = 1900
    const val FOCUS_ID = 1500

    private val ACCENT = 0xFFF0502E.toInt()

    // ---- channels ----

    /** Idempotent; called from [com.betterdo.app.BetterDoApplication.onCreate]. */
    fun ensureChannel(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannelGroup(
            NotificationChannelGroup(GROUP_CHANNELS, context.getString(R.string.channel_group_name)),
        )
        val reminders = NotificationChannel(
            CHANNEL_REMINDERS,
            context.getString(R.string.reminder_channel_name),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = context.getString(R.string.reminder_channel_desc)
            group = GROUP_CHANNELS
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 200, 150, 200)
            enableLights(true)
            lightColor = ACCENT
            setShowBadge(true)
        }
        val ongoing = NotificationChannel(
            CHANNEL_ONGOING,
            context.getString(R.string.focus_channel_name),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = context.getString(R.string.focus_channel_desc)
            group = GROUP_CHANNELS
            setSound(null, null)
            enableVibration(false)
            setShowBadge(false)
        }
        manager.createNotificationChannel(reminders)
        manager.createNotificationChannel(ongoing)
    }

    // ---- reminders ----

    fun show(context: Context, id: Int, todoId: String?, title: String, text: String) {
        val builder = NotificationCompat.Builder(context, CHANNEL_REMINDERS)
            .setSmallIcon(R.drawable.ic_stat_reminder)
            .setLargeIcon(largeIcon(context))
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setColor(ACCENT)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setShowWhen(true)
            .setContentIntent(openAppIntent(context, id))
            .setGroup(GROUP_KEY)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setPublicVersion(redactedVersion(context, title))
            .addAction(
                R.drawable.ic_action_check,
                context.getString(R.string.action_complete),
                actionIntent(context, NotificationActionReceiver.ACTION_COMPLETE, id, todoId, title, text),
            )
            .addAction(
                R.drawable.ic_action_clock,
                context.getString(R.string.action_snooze),
                actionIntent(context, NotificationActionReceiver.ACTION_SNOOZE, id, todoId, title, text),
            )
            .addAction(
                R.drawable.ic_action_focus,
                context.getString(R.string.action_focus),
                actionIntent(context, NotificationActionReceiver.ACTION_FOCUS, id, todoId, title, text),
            )
            .addAction(replyAction(context, id, todoId))

        post(context, id, builder.build())
        post(context, SUMMARY_ID, summary(context))
    }

    /** Replaces a reminder with a short ack after the user replies inline. */
    fun showReplied(context: Context, id: Int, reply: String) {
        val body = if (reply.isBlank()) context.getString(R.string.reply_empty) else reply
        val n = NotificationCompat.Builder(context, CHANNEL_REMINDERS)
            .setSmallIcon(R.drawable.ic_stat_reminder)
            .setColor(ACCENT)
            .setContentTitle(context.getString(R.string.reply_posted))
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setGroup(GROUP_KEY)
            .setAutoCancel(true)
            .setTimeoutAfter(4_000)
            .build()
        post(context, id, n)
    }

    private fun summary(context: Context): Notification =
        NotificationCompat.Builder(context, CHANNEL_REMINDERS)
            .setSmallIcon(R.drawable.ic_stat_reminder)
            .setColor(ACCENT)
            .setContentTitle(context.getString(R.string.summary_title))
            .setStyle(
                NotificationCompat.InboxStyle()
                    .setSummaryText(context.getString(R.string.summary_text)),
            )
            .setGroup(GROUP_KEY)
            .setGroupSummary(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .build()

    /** Lock-screen redacted copy: shows the title only, hides the toned body. */
    private fun redactedVersion(context: Context, title: String): Notification =
        NotificationCompat.Builder(context, CHANNEL_REMINDERS)
            .setSmallIcon(R.drawable.ic_stat_reminder)
            .setColor(ACCENT)
            .setContentTitle(title)
            .setContentText(context.getString(R.string.reminder_locked_hint))
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()

    // ---- focus (foreground) ----

    /** Ongoing notification with a custom layout + live progress, used by [FocusService]. */
    fun buildFocus(context: Context, remainingSec: Int, totalSec: Int): Notification {
        val collapsed = focusViews(context, R.layout.notification_focus_collapsed, remainingSec, totalSec)
        val expanded = focusViews(context, R.layout.notification_focus_expanded, remainingSec, totalSec)
        expanded.setOnClickPendingIntent(R.id.btn_stop, stopFocusIntent(context))

        return NotificationCompat.Builder(context, CHANNEL_ONGOING)
            .setSmallIcon(R.drawable.ic_action_focus)
            .setColor(ACCENT)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(collapsed)
            .setCustomBigContentView(expanded)
            .setContentIntent(openAppIntent(context, FOCUS_ID))
            .build()
    }

    private fun focusViews(context: Context, layout: Int, remainingSec: Int, totalSec: Int): RemoteViews =
        RemoteViews(context.packageName, layout).apply {
            setTextViewText(R.id.title, context.getString(R.string.focus_title))
            setTextViewText(R.id.subtitle, formatClock(remainingSec))
            setProgressBar(R.id.progress, totalSec, totalSec - remainingSec, false)
        }

    // ---- helpers ----

    fun canPost(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    private fun post(context: Context, id: Int, notification: Notification) {
        if (canPost(context)) NotificationManagerCompat.from(context).notify(id, notification)
    }

    private fun openAppIntent(context: Context, requestCode: Int): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
            .apply { flags = Intent.FLAG_ACTIVITY_SINGLE_TOP }
        return PendingIntent.getActivity(
            context, requestCode, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    private fun actionIntent(
        context: Context,
        action: String,
        notifId: Int,
        todoId: String?,
        title: String,
        text: String,
    ): PendingIntent {
        val intent = Intent(context, NotificationActionReceiver::class.java).apply {
            this.action = action
            putExtra(NotificationActionReceiver.EXTRA_NOTIF_ID, notifId)
            putExtra(NotificationActionReceiver.EXTRA_TODO_ID, todoId)
            putExtra(NotificationActionReceiver.EXTRA_TITLE, title)
            putExtra(NotificationActionReceiver.EXTRA_TEXT, text)
        }
        return PendingIntent.getBroadcast(
            context, notifId * 16 + action.hashCode().and(0xF), intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    private fun replyAction(context: Context, notifId: Int, todoId: String?): NotificationCompat.Action {
        val remoteInput = RemoteInput.Builder(NotificationActionReceiver.KEY_REPLY)
            .setLabel(context.getString(R.string.reply_hint))
            .build()
        val intent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_REPLY
            putExtra(NotificationActionReceiver.EXTRA_NOTIF_ID, notifId)
            putExtra(NotificationActionReceiver.EXTRA_TODO_ID, todoId)
        }
        val pending = PendingIntent.getBroadcast(
            context, notifId * 16 + 15, intent,
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        return NotificationCompat.Action.Builder(
            R.drawable.ic_action_reply,
            context.getString(R.string.action_reply),
            pending,
        ).addRemoteInput(remoteInput).setAllowGeneratedReplies(true).build()
    }

    private fun stopFocusIntent(context: Context): PendingIntent {
        val intent = Intent(context, FocusService::class.java).apply { action = FocusService.ACTION_STOP }
        return PendingIntent.getService(
            context, 99, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    private fun formatClock(totalSec: Int): String {
        val s = totalSec.coerceAtLeast(0)
        return "%d:%02d".format(s / 60, s % 60)
    }

    /** Accent disc with the reminder glyph, used as the large icon. */
    private fun largeIcon(context: Context): Bitmap {
        val density = context.resources.displayMetrics.density
        val size = (48 * density).toInt().coerceAtLeast(48)
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = ACCENT }
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)
        val glyph = ContextCompat.getDrawable(context, R.drawable.ic_stat_reminder)
        if (glyph != null) {
            val inset = (size * 0.24f).toInt()
            glyph.setTint(Color.WHITE)
            glyph.setBounds(inset, inset, size - inset, size - inset)
            glyph.draw(canvas)
        }
        return bmp
    }
}
