package com.taskflow.app.domain.usecase.user.tasks

import com.taskflow.app.data.local.dao.UserTaskDao
import com.taskflow.app.data.local.entity.UserTaskEntity
import com.taskflow.app.domain.repository.TaskRepository
import com.taskflow.app.domain.util.TaskStatus
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class GetPendingUserTasksUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    operator fun invoke(userId: Long) =
        taskRepository.getPendingTasksForUserFlow(userId)
}

class GetCompletedUserTasksUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    operator fun invoke(userId: Long) =
        taskRepository.getCompletedTasksForUserFlow(userId)
}

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
    ): Result<Unit> = runCatching {
        require(userId > 0L) { "Utilizador invalido." }
        require(taskId > 0L) { "Tarefa invalida." }
        require(percentage in 0..100) { "Percentagem invalida." }
        require(timeSpentMinutes >= 0) { "Tempo invalido." }

        val workDate = LocalDate.parse(dateText.trim())
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        val now = System.currentTimeMillis()
        val cleanedLocation = location.trim().ifBlank { null }

        val existing = userTaskDao.get(userId, taskId)
        userTaskDao.upsert(
            (existing ?: UserTaskEntity(
                userId = userId,
                taskId = taskId,
                updatedAt = now
            )).copy(
                workDate = workDate,
                location = cleanedLocation,
                completionPercentage = percentage,
                timeSpentMinutes = timeSpentMinutes,
                isCompleted = percentage == 100,
                updatedAt = now
            )
        )

        taskRepository.updateTaskStatus(
            id = taskId,
            status = if (percentage == 100) TaskStatus.COMPLETED else TaskStatus.IN_PROGRESS
        )
        taskRepository.pushTaskProgress(
            taskId = taskId,
            userId = userId,
            workDate = workDate,
            location = cleanedLocation,
            completionPercentage = percentage,
            timeSpentMinutes = timeSpentMinutes
        )
        Unit
    }
}
