package com.taskflow.app.notification

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
import com.taskflow.app.MainActivity
import com.taskflow.app.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskFlowNotifier @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channels = listOf(
            NotificationChannel(
                CHANNEL_TASKS,
                context.getString(R.string.notif_channel_tasks_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.notif_channel_tasks_desc)
            },
            NotificationChannel(
                CHANNEL_DEADLINES,
                context.getString(R.string.notif_channel_deadlines_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.notif_channel_deadlines_desc)
            },
            NotificationChannel(
                CHANNEL_SYNC,
                context.getString(R.string.notif_channel_sync_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = context.getString(R.string.notif_channel_sync_desc)
            }
        )

        context
            .getSystemService(NotificationManager::class.java)
            .createNotificationChannels(channels)
    }

    fun showTaskAssigned(taskId: Long, title: String) {
        show(
            id = taskId.notificationId(TASK_NOTIFICATION_BASE),
            channelId = CHANNEL_TASKS,
            title = context.getString(R.string.notif_task_assigned_title),
            text = title,
            priority = NotificationCompat.PRIORITY_DEFAULT
        )
    }

    fun showDeadlineReminder(taskId: Long, title: String) {
        show(
            id = taskId.notificationId(DEADLINE_NOTIFICATION_BASE),
            channelId = CHANNEL_DEADLINES,
            title = context.getString(R.string.notif_deadline_reminder_title),
            text = title,
            priority = NotificationCompat.PRIORITY_HIGH
        )
    }

    fun showSyncCompleted(count: Int) {
        show(
            id = SYNC_SUCCESS_NOTIFICATION_ID,
            channelId = CHANNEL_SYNC,
            title = context.getString(R.string.notif_sync_completed_title),
            text = context.getString(R.string.notif_sync_completed_text, count),
            priority = NotificationCompat.PRIORITY_LOW
        )
    }

    fun showSyncFailed() {
        show(
            id = SYNC_FAILED_NOTIFICATION_ID,
            channelId = CHANNEL_SYNC,
            title = context.getString(R.string.notif_sync_failed_title),
            text = context.getString(R.string.notif_sync_failed_text),
            priority = NotificationCompat.PRIORITY_DEFAULT
        )
    }

    private fun show(
        id: Int,
        channelId: String,
        title: String,
        text: String,
        priority: Int
    ) {
        if (!canPostNotifications()) return

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(mainActivityIntent())
            .setAutoCancel(true)
            .setPriority(priority)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(id, notification)
        } catch (_: SecurityException) {
            return
        }
    }

    private fun canPostNotifications(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

    private fun mainActivityIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)

        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun Long.notificationId(base: Int): Int =
        base + (this % 100_000).toInt()

    companion object {
        const val CHANNEL_TASKS = "taskflow_tasks"
        const val CHANNEL_DEADLINES = "taskflow_deadlines"
        const val CHANNEL_SYNC = "taskflow_sync"

        private const val TASK_NOTIFICATION_BASE = 10_000
        private const val DEADLINE_NOTIFICATION_BASE = 120_000
        private const val SYNC_SUCCESS_NOTIFICATION_ID = 210_001
        private const val SYNC_FAILED_NOTIFICATION_ID = 210_002
    }
}