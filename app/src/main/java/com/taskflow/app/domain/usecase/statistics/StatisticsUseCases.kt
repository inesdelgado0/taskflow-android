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

private val isPortuguese: Boolean
    get() = java.util.Locale.getDefault().language == "pt"

private fun localizedText(portuguese: String, english: String): String =
    if (isPortuguese) portuguese else english

class BuildUserStatisticsUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(userId: Long, now: Long = System.currentTimeMillis()): StatisticsSnapshot {
        val user = userRepository.getUserById(userId)
        val pendingTasks = taskRepository.getPendingTasksForUserFlow(userId).first()
        val completedTasks = taskRepository.getCompletedTasksForUserFlow(userId).first()
        val tasks = pendingTasks + completedTasks

        val titleSubject = user?.name ?: if (isPortuguese) "Utilizador $userId" else "User $userId"
        return StatisticsSnapshot(
            title = "${localizedText("Estatísticas do utilizador", "User statistics")} : $titleSubject",
            generatedAt = now,
            rows = listOf(tasks.toStatisticRow(titleSubject, now))
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
        val label = project?.name ?: if (isPortuguese) "Projeto $projectId" else "Project $projectId"

        return StatisticsSnapshot(
            title = "${localizedText("Estatísticas do projeto", "Project statistics")} : $label",
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

        val taskTitle = task?.title ?: if (isPortuguese) "Tarefa $taskId" else "Task $taskId"
        return StatisticsSnapshot(
            title = "${localizedText("Estatísticas da tarefa", "Task statistics")} : $taskTitle",
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
