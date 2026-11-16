package com.aman.shiftmate.work

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.aman.shiftmate.MainActivity
import com.aman.shiftmate.data.ShiftDatabase
import java.util.concurrent.TimeUnit

/**
 * Background job: if the user is still clocked in after [REMINDER_AFTER_HOURS],
 * post one reminder notification for that shift.
 */
class ClockReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val open = ShiftDatabase.getInstance(applicationContext).shiftDao().getOpenShift()
        if (open != null) {
            val hours = (System.currentTimeMillis() - open.startTime) / 3_600_000.0
            val prefs = applicationContext.getSharedPreferences("shiftmate_reminders", Context.MODE_PRIVATE)
            val alreadyNotified = prefs.getLong(KEY_LAST_SHIFT, -1L) == open.id
            if (hours >= REMINDER_AFTER_HOURS && !alreadyNotified) {
                if (showNotification()) {
                    prefs.edit().putLong(KEY_LAST_SHIFT, open.id).apply()
                }
            }
        }
        return Result.success()
    }

    /** Returns true if the notification was posted (needs the notification permission on Android 13+). */
    private fun showNotification(): Boolean {
        val ctx = applicationContext
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }

        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Clock-out reminders", NotificationManager.IMPORTANCE_DEFAULT
            )
            ctx.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        val openApp = PendingIntent.getActivity(
            ctx, 0, Intent(ctx, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // No pay figures in the text: lock screens can be seen by other people
        val notification = NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Still clocked in?")
            .setContentText("Remember to clock out when your shift ends.")
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(ctx).notify(NOTIFICATION_ID, notification)
        return true
    }

    companion object {
        /** Set to 0.0 while testing, then back to 8.0. */
        const val REMINDER_AFTER_HOURS = 0.0

        private const val CHANNEL_ID = "clock_reminders"
        private const val NOTIFICATION_ID = 1
        private const val KEY_LAST_SHIFT = "last_notified_shift"
        private const val WORK_NAME = "clock_reminder"

        /** Schedules the check about every 15 minutes (the minimum WorkManager allows). */
        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<ClockReminderWorker>(15, TimeUnit.MINUTES).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request
            )
        }
    }
}