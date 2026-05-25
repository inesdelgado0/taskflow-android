package com.taskflow.app.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
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
import android.content.Intent

@Singleton
class TaskFlowNotifier @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channels = listOf(
            NotificationChannel(
                CHANNEL_TASKS,
                "Tarefas",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Tarefas atribuídas e atualizações de tarefas"
            },
            NotificationChannel(
                CHANNEL_DEADLINES,
                "Prazos",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Lembretes de prazos próximos"
            },
            NotificationChannel(
                CHANNEL_SYNC,
                "Sincronização",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Estado da sincronização offline"
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
            title = "Nova tarefa atribuída",
            text = title,
            priority = NotificationCompat.PRIORITY_DEFAULT
        )
    }

    fun showDeadlineReminder(taskId: Long, title: String) {
        show(
            id = taskId.notificationId(DEADLINE_NOTIFICATION_BASE),
            channelId = CHANNEL_DEADLINES,
            title = "Prazo próximo",
            text = title,
            priority = NotificationCompat.PRIORITY_HIGH
        )
    }

    fun showSyncCompleted(count: Int) {
        show(
            id = SYNC_SUCCESS_NOTIFICATION_ID,
            channelId = CHANNEL_SYNC,
            title = "Sincronização concluída",
            text = "$count operação(ões) sincronizada(s)",
            priority = NotificationCompat.PRIORITY_LOW
        )
    }

    fun showSyncFailed() {
        show(
            id = SYNC_FAILED_NOTIFICATION_ID,
            channelId = CHANNEL_SYNC,
            title = "Sincronização incompleta",
            text = "Algumas operações serão tentadas novamente",
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
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(mainActivityIntent())
            .setAutoCancel(true)
            .setPriority(priority)
            .build()

        NotificationManagerCompat.from(context).notify(id, notification)
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

