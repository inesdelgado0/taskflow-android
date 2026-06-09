package com.taskflow.app.domain.usecase.user.tasks

import com.taskflow.app.domain.model.Task
import com.taskflow.app.domain.repository.TaskRepository
import com.taskflow.app.domain.util.TaskPriority
import com.taskflow.app.domain.util.TaskStatus
import com.taskflow.app.util.ApiResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class UserTaskListUseCasesTest {
    @Test
    fun getPendingUserTasks_returnsRepositoryPendingTasks() = runBlocking {
        val pending = listOf(task(1L, TaskStatus.PENDING), task(2L, TaskStatus.IN_PROGRESS))
        val useCase = GetPendingUserTasksUseCase(
            FakeTaskListRepository(pendingForUser = pending)
        )

        assertEquals(pending, useCase(userId = 4L).first())
    }

    @Test
    fun getCompletedUserTasks_returnsRepositoryCompletedTasks() = runBlocking {
        val completed = listOf(task(3L, TaskStatus.COMPLETED))
        val useCase = GetCompletedUserTasksUseCase(
            FakeTaskListRepository(completedForUser = completed)
        )

        assertEquals(completed, useCase(userId = 4L).first())
    }

    private fun task(id: Long, status: TaskStatus) = Task(
        id = id,
        projectId = 1L,
        title = "Task $id",
        priority = TaskPriority.MEDIUM,
        status = status,
        createdBy = 1L,
        createdAt = 0L,
        updatedAt = 0L
    )
}

private class FakeTaskListRepository(
    private val pendingForUser: List<Task> = emptyList(),
    private val completedForUser: List<Task> = emptyList()
) : TaskRepository {
    override suspend fun createTask(task: Task): Long = task.id
    override suspend fun updateTask(task: Task) = Unit
    override suspend fun deleteTask(id: Long) = Unit
    override suspend fun getTaskById(id: Long): Task? = null
    override fun getTasksByProjectFlow(projectId: Long): Flow<List<Task>> = flowOf(emptyList())
    override fun getTasksByProjectAndStatusFlow(projectId: Long, status: TaskStatus): Flow<List<Task>> =
        flowOf(emptyList())
    override fun getPendingTasksForUserFlow(userId: Long): Flow<List<Task>> = flowOf(pendingForUser)
    override fun getCompletedTasksForUserFlow(userId: Long): Flow<List<Task>> = flowOf(completedForUser)
    override fun searchTasksFlow(
        query: String,
        projectId: Long?,
        status: TaskStatus?,
        priority: TaskPriority?
    ): Flow<List<Task>> = flowOf(emptyList())
    override suspend fun updateTaskStatus(id: Long, status: TaskStatus) = Unit
    override suspend fun refreshTasks(projectId: Long): ApiResult<List<Task>> = ApiResult.Success(emptyList())
    override suspend fun refreshUserTaskAssignments(userId: Long): ApiResult<Unit> = ApiResult.Success(Unit)
    override suspend fun pushTask(task: Task): ApiResult<Task> = ApiResult.Success(task)
    override suspend fun pushTaskProgress(
        taskId: Long,
        userId: Long,
        workDate: Long?,
        location: String?,
        completionPercentage: Int,
        timeSpentMinutes: Int
    ): ApiResult<Unit> = ApiResult.Success(Unit)
    override suspend fun refreshTaskUsers(taskId: Long): ApiResult<List<Long>> = ApiResult.Success(emptyList())
    override suspend fun assignUserToTaskRemote(taskId: Long, userId: Long): ApiResult<Unit> = ApiResult.Success(Unit)
    override suspend fun removeUserFromTaskRemote(taskId: Long, userId: Long): ApiResult<Unit> = ApiResult.Success(Unit)
    override suspend fun updateTaskStatusRemote(id: Long, status: TaskStatus): ApiResult<Task> =
        ApiResult.Success(
            Task(
                id = id,
                projectId = 1L,
                title = "Task $id",
                status = status,
                createdBy = 1L,
                createdAt = 0L,
                updatedAt = 0L
            )
        )
    override suspend fun deleteTaskRemote(id: Long): ApiResult<Unit> = ApiResult.Success(Unit)
}
