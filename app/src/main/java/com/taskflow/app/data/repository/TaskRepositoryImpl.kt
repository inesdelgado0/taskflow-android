package com.taskflow.app.data.repository

import android.database.sqlite.SQLiteConstraintException
import com.taskflow.app.audit.AuditLogger
import com.taskflow.app.data.local.dao.TaskDao
import com.taskflow.app.data.local.dao.UserDao
import com.taskflow.app.data.local.dao.UserTaskDao
import com.taskflow.app.data.local.entity.TaskEntity
import com.taskflow.app.data.local.entity.UserTaskEntity
import com.taskflow.app.data.remote.api.TaskApi
import com.taskflow.app.data.remote.TokenManager
import com.taskflow.app.data.remote.dto.AssignUserRequest
import com.taskflow.app.data.remote.dto.TaskDto
import com.taskflow.app.data.remote.dto.TaskProgressRequest
import com.taskflow.app.data.remote.dto.TaskRequest
import com.taskflow.app.data.remote.dto.TaskStatusRequest
import com.taskflow.app.data.remote.dto.UserTaskDto
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
    private val userDao: UserDao,
    private val userTaskDao: UserTaskDao,
    private val taskApi: TaskApi,
    private val notificationScheduler: TaskNotificationScheduler,
    private val tokenManager: TokenManager,
    private val auditLogger: AuditLogger
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
                runCatching {
                    taskDao.upsertAll(tasks.map { it.toEntity() })
                }.onFailure { e ->
                    if (e is SQLiteConstraintException) {
                        // Tentar inserir uma a uma, ignorando as que falham por FK
                        tasks.forEach { task ->
                            runCatching { taskDao.upsert(task.toEntity()) }
                                .onFailure { /* ignora falhas de FK — user remoto pode não existir localmente */ }
                        }
                    }
                }
                tasks.forEach { notificationScheduler.scheduleDeadlineReminder(it) }
            }

    override suspend fun refreshUserTaskAssignments(userId: Long): ApiResult<Unit> =
        safeApiCall { taskApi.getUserTaskAssignments(userId) }
            .map { assignments -> assignments.map { it.toEntity() } }
            .onSuccess { assignments ->
                assignments.forEach { remote ->
                    val local = userTaskDao.get(remote.userId, remote.taskId)
                    userTaskDao.upsert(mergeAssignment(local, remote))
                }
            }
            .map { Unit }

    override suspend fun pushTask(task: Task): ApiResult<Task> {
        val isCreate = task.id == 0L
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
                val details = "title=${synced.title},status=${synced.status.name},priority=${synced.priority.name}"
                if (isCreate) {
                    auditLogger.logCreate(currentActorId(), "TASK", synced.id, details)
                } else {
                    auditLogger.logUpdate(currentActorId(), "TASK", synced.id, details)
                }
            }
    }

    override suspend fun pushTaskProgress(
        taskId: Long,
        userId: Long,
        workDate: Long?,
        location: String?,
        completionPercentage: Int,
        timeSpentMinutes: Int
    ): ApiResult<Unit> =
        safeApiCall {
            taskApi.updateProgress(
                taskId,
                TaskProgressRequest(
                    userId = userId,
                    workDate = workDate,
                    location = location,
                    completionPercentage = completionPercentage,
                    timeSpentMinutes = timeSpentMinutes
                )
            )
        }.map { Unit }
            .onSuccess {
                auditLogger.logUpdate(
                    currentActorId(),
                    "TASK",
                    taskId,
                    details = "progress=$completionPercentage,timeSpentMinutes=$timeSpentMinutes,userId=$userId"
                )
            }

    override suspend fun refreshTaskUsers(taskId: Long): ApiResult<List<Long>> =
        safeApiCall { taskApi.getTaskUsers(taskId) }
            .map { users -> users.map { it.id } }
            .onSuccess { userIds ->
                userIds.forEach { userId ->
                    // Só inserir na junction se o user existir localmente (evita FK constraint)
                    if (userDao.getById(userId) != null) {
                        upsertAssignmentPreservingProgress(userId, taskId)
                    }
                }
            }

    override suspend fun assignUserToTaskRemote(taskId: Long, userId: Long): ApiResult<Unit> =
        safeApiCall { taskApi.assignUser(taskId, AssignUserRequest(userId)) }
            .map { Unit }
            .onSuccess {
                if (userDao.getById(userId) != null) {
                    upsertAssignmentPreservingProgress(userId, taskId)
                }
                auditLogger.logUpdate(currentActorId(), "TASK", taskId, details = "assignUser:$userId")
            }

    override suspend fun removeUserFromTaskRemote(taskId: Long, userId: Long): ApiResult<Unit> =
        safeApiCall { taskApi.removeUser(taskId, userId) }
            .onSuccess {
                userTaskDao.delete(taskId, userId)
                auditLogger.logUpdate(currentActorId(), "TASK", taskId, details = "removeUser:$userId")
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
                auditLogger.logUpdate(currentActorId(), "TASK", synced.id, details = "status=${synced.status.name}")
            }

    override suspend fun deleteTaskRemote(id: Long): ApiResult<Unit> =
        safeApiCall { taskApi.deleteTask(id) }
            .onSuccess {
                deleteTask(id)
                notificationScheduler.cancelDeadlineReminder(id)
                auditLogger.logDelete(currentActorId(), "TASK", id)
            }

    private suspend fun currentActorId(): Long? = tokenManager.getUserId()

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

    private fun UserTaskDto.toEntity() = UserTaskEntity(
        userId = userId,
        taskId = taskId,
        workDate = workDate,
        location = location,
        completionPercentage = completionPercentage,
        timeSpentMinutes = timeSpentMinutes,
        isCompleted = isCompleted,
        updatedAt = updatedAt
    )

    private fun mergeAssignment(local: UserTaskEntity?, remote: UserTaskEntity): UserTaskEntity {
        if (local == null) return remote

        val localIsNewer = local.updatedAt >= remote.updatedAt
        val bestProgress = maxOf(local.completionPercentage, remote.completionPercentage)

        return UserTaskEntity(
            userId = remote.userId,
            taskId = remote.taskId,
            workDate = when {
                localIsNewer && local.workDate != null -> local.workDate
                remote.workDate != null -> remote.workDate
                else -> local.workDate
            },
            location = when {
                localIsNewer && !local.location.isNullOrBlank() -> local.location
                !remote.location.isNullOrBlank() -> remote.location
                else -> local.location
            },
            completionPercentage = bestProgress,
            timeSpentMinutes = maxOf(local.timeSpentMinutes, remote.timeSpentMinutes),
            isCompleted = local.isCompleted || remote.isCompleted || bestProgress == 100,
            updatedAt = maxOf(local.updatedAt, remote.updatedAt)
        )
    }

    private suspend fun upsertAssignmentPreservingProgress(userId: Long, taskId: Long) {
        val now = System.currentTimeMillis()
        val existing = userTaskDao.get(userId, taskId)
        if (existing == null) {
            userTaskDao.upsert(
                UserTaskEntity(
                    userId = userId,
                    taskId = taskId,
                    updatedAt = now
                )
            )
        } else {
            userTaskDao.upsert(existing.copy(updatedAt = maxOf(existing.updatedAt, now)))
        }
    }
}
