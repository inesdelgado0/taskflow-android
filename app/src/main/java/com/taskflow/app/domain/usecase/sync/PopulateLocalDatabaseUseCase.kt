package com.taskflow.app.domain.usecase.sync

import com.taskflow.app.domain.repository.EvaluationRepository
import com.taskflow.app.domain.repository.ObservationRepository
import com.taskflow.app.domain.repository.ProjectRepository
import com.taskflow.app.domain.repository.TaskRepository
import com.taskflow.app.domain.repository.UserRepository
import com.taskflow.app.util.ApiResult
import javax.inject.Inject

class PopulateLocalDatabaseUseCase @Inject constructor(
    private val projectRepository: ProjectRepository,
    private val taskRepository: TaskRepository,
    private val observationRepository: ObservationRepository,
    private val evaluationRepository: EvaluationRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): Result<Unit> = runCatching {
        userRepository.refreshUsers()

        val projects = when (val result = projectRepository.refreshProjects()) {
            is ApiResult.Success -> result.data
            else -> emptyList()
        }

        projects.forEach { project ->
            val tasks = when (val result = taskRepository.refreshTasks(project.id)) {
                is ApiResult.Success -> result.data
                else -> emptyList()
            }

            evaluationRepository.refreshEvaluations(project.id)
            tasks.forEach { task ->
                observationRepository.refreshObservations(task.id)
            }
        }
    }

    suspend operator fun invoke(userId: Long): Result<Unit> =
        invoke().onSuccess {
            taskRepository.refreshUserTaskAssignments(userId)
        }
}
