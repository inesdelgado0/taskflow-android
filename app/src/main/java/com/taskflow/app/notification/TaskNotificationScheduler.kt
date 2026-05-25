package com.taskflow.app.notification

import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.taskflow.app.domain.model.Task
import com.taskflow.app.domain.util.TaskStatus
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskNotificationScheduler @Inject constructor(
    private val workManager: WorkManager,
    private val notifier: TaskFlowNotifier
) {
    fun onTaskAssigned(task: Task) {
        if (task.status == TaskStatus.COMPLETED || task.status == TaskStatus.CANCELLED) return

        notifier.showTaskAssigned(task.id, task.title)
        scheduleDeadlineReminder(task)
    }

    fun scheduleDeadlineReminder(task: Task) {
        val deadline = task.deadline ?: return
        if (task.status == TaskStatus.COMPLETED || task.status == TaskStatus.CANCELLED) return

        val now = System.currentTimeMillis()
        if (deadline <= now) return

        val reminderAt = (deadline - REMINDER_BEFORE_DEADLINE_MS).coerceAtLeast(now)
        val delay = reminderAt - now

        val inputData = Data.Builder()
            .putLong(DeadlineReminderWorker.KEY_TASK_ID, task.id)
            .putString(DeadlineReminderWorker.KEY_TASK_TITLE, task.title)
            .build()

        val request = OneTimeWorkRequestBuilder<DeadlineReminderWorker>()
            .setInputData(inputData)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .build()

        workManager.enqueueUniqueWork(
            deadlineWorkName(task.id),
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun cancelDeadlineReminder(taskId: Long) {
        workManager.cancelUniqueWork(deadlineWorkName(taskId))
    }

    private fun deadlineWorkName(taskId: Long): String =
        "taskflow_deadline_$taskId"

    companion object {
        private const val REMINDER_BEFORE_DEADLINE_MS = 24 * 60 * 60 * 1000L
    }
}

