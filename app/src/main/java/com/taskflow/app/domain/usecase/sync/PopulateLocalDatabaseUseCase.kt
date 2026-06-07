package com.taskflow.app.domain.usecase.sync

import com.taskflow.app.data.local.database.AppDatabase
import com.taskflow.app.domain.repository.EvaluationRepository
import com.taskflow.app.domain.repository.ObservationRepository
import com.taskflow.app.domain.repository.ProjectRepository
import com.taskflow.app.domain.repository.TaskRepository
import com.taskflow.app.domain.repository.UserRepository
import com.taskflow.app.util.ApiResult
import javax.inject.Inject

class PopulateLocalDatabaseUseCase @Inject constructor(
    private val database: AppDatabase,
    private val projectRepository: ProjectRepository,
    private val taskRepository: TaskRepository,
    private val observationRepository: ObservationRepository,
    private val evaluationRepository: EvaluationRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): Result<Unit> = runCatching {
        // Desativa foreign keys durante a sync em bulk para evitar
        // SQLITE_CONSTRAINT_TRIGGER quando dados remotos contêm
        // referências a registos que ainda não foram inseridos localmente.
        database.withForeignKeysDisabled {
            userRepository.refreshUsers()

            val projects = when (val result = projectRepository.refreshProjects()) {
                is ApiResult.Success -> result.data
                else -> emptyList()
            }

            projects.forEach { project ->
                projectRepository.refreshProjectUsers(project.id)

                val tasks = when (val result = taskRepository.refreshTasks(project.id)) {
                    is ApiResult.Success -> result.data
                    else -> emptyList()
                }

                evaluationRepository.refreshEvaluations(project.id)
                tasks.forEach { task ->
                    taskRepository.refreshTaskUsers(task.id)
                    observationRepository.refreshObservations(task.id)
                }
            }
        }
    }
}
