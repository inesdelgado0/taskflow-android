package com.taskflow.app.data.repository

import com.taskflow.app.data.local.dao.TaskDao
import com.taskflow.app.data.local.entity.TaskEntity
import com.taskflow.app.data.remote.api.TaskApi
import com.taskflow.app.data.remote.dto.TaskDto
import com.taskflow.app.data.remote.dto.TaskRequest
import com.taskflow.app.data.remote.dto.TaskStatusRequest
import com.taskflow.app.domain.model.Task
import com.taskflow.app.domain.repository.TaskRepository
import com.taskflow.app.domain.util.TaskPriority
import com.taskflow.app.domain.util.TaskStatus
import com.taskflow.app.notification.TaskNotificationScheduler
import com.taskflow.app.util.ApiResult
import com.taskflow.app.util.map
import com.taskflow.app.util.onSuccess
import com.taskflow.app.util.safeApiCall
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao,
    private val taskApi: TaskApi,
    private val notificationScheduler: TaskNotificationScheduler
) : TaskRepository {

    override suspend fun createTask(task: Task): Long {
        val id = taskDao.insert(task.toEntity())
        notificationScheduler.onTaskAssigned(task.copy(id = id))
        return id
    }

    override suspend fun updateTask(task: Task) {
        taskDao.update(task.toEntity())
        if (task.status == TaskStatus.COMPLETED || task.status == TaskStatus.CANCELLED) {
            notificationScheduler.cancelDeadlineReminder(task.id)
        } else {
            notificationScheduler.scheduleDeadlineReminder(task)
        }
    }

    override suspend fun deleteTask(id: Long) {
        val entity = taskDao.getById(id) ?: return
        taskDao.delete(entity)
        notificationScheduler.cancelDeadlineReminder(id)
    }

    override suspend fun getTaskById(id: Long): Task? =
        taskDao.getById(id)?.toDomain()

    override fun getTasksByProjectFlow(projectId: Long): Flow<List<Task>> =
        taskDao.getByProjectFlow(projectId).map { list -> list.map { it.toDomain() } }

    override fun getTasksByProjectAndStatusFlow(projectId: Long, status: TaskStatus): Flow<List<Task>> =
        taskDao.getByProjectAndStatusFlow(projectId, status).map { list -> list.map { it.toDomain() } }

    override fun getPendingTasksForUserFlow(userId: Long): Flow<List<Task>> =
        taskDao.getPendingForUserFlow(userId).map { list -> list.map { it.toDomain() } }

    override fun getCompletedTasksForUserFlow(userId: Long): Flow<List<Task>> =
        taskDao.getCompletedForUserFlow(userId).map { list -> list.map { it.toDomain() } }

    override fun searchTasksFlow(
        query: String,
        projectId: Long?,
        status: TaskStatus?,
        priority: TaskPriority?
    ): Flow<List<Task>> =
        taskDao.searchFlow(query, projectId, status, priority).map { list -> list.map { it.toDomain() } }

    override suspend fun updateTaskStatus(id: Long, status: TaskStatus) {
        taskDao.updateStatus(id, status, System.currentTimeMillis())
        if (status == TaskStatus.COMPLETED || status == TaskStatus.CANCELLED) {
            notificationScheduler.cancelDeadlineReminder(id)
        }
    }

    override suspend fun refreshTasks(projectId: Long): ApiResult<List<Task>> =
        safeApiCall { taskApi.getProjectTasks(projectId) }
            .map { tasks -> tasks.map { it.toDomain() } }
            .onSuccess { tasks ->
                taskDao.upsertAll(tasks.map { it.toEntity() })
                tasks.forEach { notificationScheduler.scheduleDeadlineReminder(it) }
            }

    override suspend fun pushTask(task: Task): ApiResult<Task> {
        val result = if (task.id == 0L) {
            safeApiCall { taskApi.createTask(task.projectId, task.toRequest()) }
        } else {
            safeApiCall { taskApi.updateTask(task.id, task.toRequest()) }
        }

        return result
            .map { it.toDomain() }
            .onSuccess { synced ->
                taskDao.upsert(synced.toEntity())
                notificationScheduler.scheduleDeadlineReminder(synced)
            }
    }

    override suspend fun updateTaskStatusRemote(id: Long, status: TaskStatus): ApiResult<Task> =
        safeApiCall { taskApi.updateStatus(id, TaskStatusRequest(status)) }
            .map { it.toDomain() }
            .onSuccess { synced ->
                taskDao.upsert(synced.toEntity())
                if (synced.status == TaskStatus.COMPLETED || synced.status == TaskStatus.CANCELLED) {
                    notificationScheduler.cancelDeadlineReminder(synced.id)
                } else {
                    notificationScheduler.scheduleDeadlineReminder(synced)
                }
            }

    override suspend fun deleteTaskRemote(id: Long): ApiResult<Unit> =
        safeApiCall { taskApi.deleteTask(id) }
            .onSuccess {
                deleteTask(id)
                notificationScheduler.cancelDeadlineReminder(id)
            }

    private fun Task.toEntity() = TaskEntity(
        id = id,
        projectId = projectId,
        title = title,
        description = description,
        priority = priority,
        deadline = deadline,
        status = status,
        createdBy = createdBy,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun TaskEntity.toDomain() = Task(
        id = id,
        projectId = projectId,
        title = title,
        description = description,
        priority = priority,
        deadline = deadline,
        status = status,
        createdBy = createdBy,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun TaskDto.toDomain() = Task(
        id = id,
        projectId = projectId,
        title = title,
        description = description,
        priority = priority,
        deadline = deadline,
        status = status,
        createdBy = createdBy,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun Task.toRequest() = TaskRequest(
        title = title,
        description = description,
        priority = priority,
        deadline = deadline,
        status = status,
        createdBy = createdBy
    )
}
