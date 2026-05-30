package com.taskflow.app.domain.repository

import com.taskflow.app.domain.model.Task
import com.taskflow.app.domain.util.TaskPriority
import com.taskflow.app.domain.util.TaskStatus
import com.taskflow.app.util.ApiResult
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    suspend fun createTask(task: Task): Long
    suspend fun updateTask(task: Task)
    suspend fun deleteTask(id: Long)
    suspend fun getTaskById(id: Long): Task?
    fun getTasksByProjectFlow(projectId: Long): Flow<List<Task>>
    fun getTasksByProjectAndStatusFlow(projectId: Long, status: TaskStatus): Flow<List<Task>>
    fun getPendingTasksForUserFlow(userId: Long): Flow<List<Task>>
    fun getCompletedTasksForUserFlow(userId: Long): Flow<List<Task>>
    fun searchTasksFlow(
        query: String,
        projectId: Long? = null,
        status: TaskStatus? = null,
        priority: TaskPriority? = null
    ): Flow<List<Task>>
    suspend fun updateTaskStatus(id: Long, status: TaskStatus)
    suspend fun refreshTasks(projectId: Long): ApiResult<List<Task>>
    suspend fun pushTask(task: Task): ApiResult<Task>
    suspend fun updateTaskStatusRemote(id: Long, status: TaskStatus): ApiResult<Task>
    suspend fun deleteTaskRemote(id: Long): ApiResult<Unit>
}
