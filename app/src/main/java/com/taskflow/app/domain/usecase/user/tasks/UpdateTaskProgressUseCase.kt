package com.taskflow.app.domain.usecase.user.tasks

import com.taskflow.app.data.local.dao.UserTaskDao
import com.taskflow.app.data.local.entity.UserTaskEntity
import com.taskflow.app.domain.repository.TaskRepository
import com.taskflow.app.domain.util.TaskStatus
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class UpdateTaskProgressUseCase @Inject constructor(
    private val userTaskDao: UserTaskDao,
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(
        userId: Long,
        taskId: Long,
        dateText: String,
        location: String,
        percentage: Int,
        timeSpentMinutes: Int
    ): Result<Unit> {
        if (percentage !in 0..100) {
            return Result.failure(IllegalArgumentException("percentage"))
        }
        if (timeSpentMinutes < 0) {
            return Result.failure(IllegalArgumentException("time"))
        }

        val workDate = runCatching {
            LocalDate.parse(dateText)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        }.getOrElse {
            return Result.failure(IllegalArgumentException("date"))
        }

        val now = System.currentTimeMillis()
        val existing = userTaskDao.get(userId, taskId)

        if (existing == null) {
            userTaskDao.upsert(
                UserTaskEntity(
                    userId = userId,
                    taskId = taskId,
                    workDate = workDate,
                    location = location.ifBlank { null },
                    completionPercentage = percentage,
                    timeSpentMinutes = timeSpentMinutes,
                    isCompleted = percentage == 100,
                    updatedAt = now
                )
            )
        } else {
            userTaskDao.updateProgress(
                userId = userId,
                taskId = taskId,
                percentage = percentage,
                timeSpent = timeSpentMinutes,
                workDate = workDate,
                location = location.ifBlank { null },
                updatedAt = now
            )
            if (percentage == 100) {
                userTaskDao.markCompleted(userId, taskId, now)
            }
        }

        taskRepository.updateTaskStatus(
            id = taskId,
            status = if (percentage == 100) TaskStatus.COMPLETED else TaskStatus.IN_PROGRESS
        )

        return Result.success(Unit)
    }
}
