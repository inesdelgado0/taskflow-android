package com.taskflow.app.domain.usecase.manager.tasks

import com.taskflow.app.data.local.dao.ProjectDao
import com.taskflow.app.data.local.dao.TaskDao
import com.taskflow.app.data.local.entity.ProjectEntity
import com.taskflow.app.data.local.entity.TaskEntity
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetManagerProjectsUseCase @Inject constructor(
    private val projectDao: ProjectDao
) {
    operator fun invoke(managerId: Long): Flow<List<ProjectEntity>> =
        projectDao.getByManagerFlow(managerId)
}

class GetProjectTasksUseCase @Inject constructor(
    private val taskDao: TaskDao
) {
    operator fun invoke(projectId: Long): Flow<List<TaskEntity>> =
        taskDao.getByProjectFlow(projectId)
}

class CreateTaskUseCase @Inject constructor(
    private val taskDao: TaskDao
) {
    suspend operator fun invoke(task: TaskEntity): Result<Long> =
        runCatching { taskDao.insert(task) }
}

class UpdateTaskUseCase @Inject constructor(
    private val taskDao: TaskDao
) {
    suspend operator fun invoke(task: TaskEntity): Result<Unit> =
        runCatching { taskDao.update(task) }
}

class DeleteTaskUseCase @Inject constructor(
    private val taskDao: TaskDao
) {
    suspend operator fun invoke(task: TaskEntity): Result<Unit> =
        runCatching { taskDao.delete(task) }
}
