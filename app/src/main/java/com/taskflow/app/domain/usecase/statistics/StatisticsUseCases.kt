package com.taskflow.app.domain.usecase.statistics

import com.taskflow.app.domain.model.StatisticRow
import com.taskflow.app.domain.model.StatisticsSnapshot
import com.taskflow.app.domain.model.Task
import com.taskflow.app.domain.repository.ProjectRepository
import com.taskflow.app.domain.repository.TaskRepository
import com.taskflow.app.domain.repository.UserRepository
import com.taskflow.app.domain.util.TaskStatus
import javax.inject.Inject
import kotlinx.coroutines.flow.first

class BuildUserStatisticsUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(userId: Long, now: Long = System.currentTimeMillis()): StatisticsSnapshot {
        val user = userRepository.getUserById(userId)
        val pendingTasks = taskRepository.getPendingTasksForUserFlow(userId).first()
        val completedTasks = taskRepository.getCompletedTasksForUserFlow(userId).first()
        val tasks = pendingTasks + completedTasks

        return StatisticsSnapshot(
            title = "Estatisticas do utilizador: ${user?.name ?: userId}",
            generatedAt = now,
            rows = listOf(tasks.toStatisticRow(user?.name ?: "Utilizador $userId", now))
        )
    }
}

class BuildProjectStatisticsUseCase @Inject constructor(
    private val projectRepository: ProjectRepository,
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(projectId: Long, now: Long = System.currentTimeMillis()): StatisticsSnapshot {
        val project = projectRepository.getProjectById(projectId)
        val tasks = taskRepository.getTasksByProjectFlow(projectId).first()
        val label = project?.name ?: "Projeto $projectId"

        return StatisticsSnapshot(
            title = "Estatisticas do projeto: $label",
            generatedAt = now,
            rows = listOf(tasks.toStatisticRow(label, now))
        )
    }
}

class BuildTaskStatisticsUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(taskId: Long, now: Long = System.currentTimeMillis()): StatisticsSnapshot {
        val task = taskRepository.getTaskById(taskId)
        val rows = listOfNotNull(task?.let { listOf(it).toStatisticRow(it.title, now) })

        return StatisticsSnapshot(
            title = "Estatisticas da tarefa: ${task?.title ?: taskId}",
            generatedAt = now,
            rows = rows
        )
    }
}

private fun List<Task>.toStatisticRow(label: String, now: Long): StatisticRow {
    val completed = count { it.status == TaskStatus.COMPLETED }
    val pending = count { it.status != TaskStatus.COMPLETED && it.status != TaskStatus.CANCELLED }
    val overdue = count { task ->
        task.deadline != null &&
            task.deadline < now &&
            task.status != TaskStatus.COMPLETED &&
            task.status != TaskStatus.CANCELLED
    }

    return StatisticRow(
        label = label,
        totalTasks = size,
        completedTasks = completed,
        pendingTasks = pending,
        overdueTasks = overdue
    )
}
