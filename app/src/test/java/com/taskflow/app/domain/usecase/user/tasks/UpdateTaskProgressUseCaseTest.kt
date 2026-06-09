package com.taskflow.app.domain.usecase.user.tasks

import com.taskflow.app.data.local.dao.UserTaskDao
import com.taskflow.app.data.local.entity.UserTaskEntity
import com.taskflow.app.domain.model.Task
import com.taskflow.app.domain.repository.TaskRepository
import com.taskflow.app.domain.util.TaskPriority
import com.taskflow.app.domain.util.TaskStatus
import com.taskflow.app.util.ApiResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateTaskProgressUseCaseTest {
    @Test
    fun invoke_createsProgressAndMarksTaskInProgress() = runBlocking {
        val userTaskDao = FakeUserTaskDao()
        val taskRepository = FakeTaskRepository()
        val useCase = UpdateTaskProgressUseCase(userTaskDao, taskRepository)

        val result = useCase(
            userId = 4L,
            taskId = 12L,
            dateText = "2026-06-05",
            location = "Remoto",
            percentage = 60,
            timeSpentMinutes = 90
        )

        val saved = userTaskDao.saved.getValue(4L to 12L)
        assertTrue(result.isSuccess)
        assertEquals(60, saved.completionPercentage)
        assertEquals(90, saved.timeSpentMinutes)
        assertEquals("Remoto", saved.location)
        assertFalse(saved.isCompleted)
        assertEquals(12L to TaskStatus.IN_PROGRESS, taskRepository.lastStatusUpdate)
    }

    @Test
    fun invoke_withBlankLocationStoresNull() = runBlocking {
        val userTaskDao = FakeUserTaskDao()
        val useCase = UpdateTaskProgressUseCase(userTaskDao, FakeTaskRepository())

        val result = useCase(
            userId = 4L,
            taskId = 12L,
            dateText = "2026-06-05",
            location = "   ",
            percentage = 10,
            timeSpentMinutes = 15
        )

        assertTrue(result.isSuccess)
        assertNull(userTaskDao.saved.getValue(4L to 12L).location)
    }

    @Test
    fun invoke_whenPercentageIsOneHundredMarksUserAndTaskCompleted() = runBlocking {
        val userTaskDao = FakeUserTaskDao(
            initial = UserTaskEntity(
                userId = 4L,
                taskId = 12L,
                workDate = null,
                location = null,
                completionPercentage = 40,
                timeSpentMinutes = 30,
                isCompleted = false,
                updatedAt = 1L
            )
        )
        val taskRepository = FakeTaskRepository()
        val useCase = UpdateTaskProgressUseCase(userTaskDao, taskRepository)

        val result = useCase(
            userId = 4L,
            taskId = 12L,
            dateText = "2026-06-05",
            location = "Escritorio",
            percentage = 100,
            timeSpentMinutes = 120
        )

        val saved = userTaskDao.saved.getValue(4L to 12L)
        assertTrue(result.isSuccess)
        assertEquals(100, saved.completionPercentage)
        assertTrue(saved.isCompleted)
        assertEquals(12L to TaskStatus.COMPLETED, taskRepository.lastStatusUpdate)
    }

    @Test
    fun invoke_rejectsInvalidInputsWithoutPersisting() = runBlocking {
        val invalidCases = listOf(
            Triple("2026-06-05", -1, 10),
            Triple("2026-06-05", 101, 10),
            Triple("2026-06-05", 50, -1),
            Triple("not-a-date", 50, 10)
        )

        invalidCases.forEach { (date, percentage, minutes) ->
            val userTaskDao = FakeUserTaskDao()
            val taskRepository = FakeTaskRepository()
            val useCase = UpdateTaskProgressUseCase(userTaskDao, taskRepository)

            val result = useCase(
                userId = 4L,
                taskId = 12L,
                dateText = date,
                location = "Remoto",
                percentage = percentage,
                timeSpentMinutes = minutes
            )

            assertTrue(result.isFailure)
            assertTrue(userTaskDao.saved.isEmpty())
            assertNull(taskRepository.lastStatusUpdate)
        }
    }
}

private class FakeUserTaskDao(
    initial: UserTaskEntity? = null
) : UserTaskDao {
    val saved = mutableMapOf<Pair<Long, Long>, UserTaskEntity>()

    init {
        initial?.let { saved[it.userId to it.taskId] = it }
    }

    override suspend fun insert(userTask: UserTaskEntity) {
        saved.putIfAbsent(userTask.userId to userTask.taskId, userTask)
    }

    override suspend fun upsert(userTask: UserTaskEntity) {
        saved[userTask.userId to userTask.taskId] = userTask
    }

    override suspend fun upsertAll(userTasks: List<UserTaskEntity>) {
        userTasks.forEach { upsert(it) }
    }

    override suspend fun get(userId: Long, taskId: Long): UserTaskEntity? =
        saved[userId to taskId]

    override fun getUsersByTaskFlow(taskId: Long): Flow<List<UserTaskEntity>> =
        flowOf(saved.values.filter { it.taskId == taskId })

    override fun getTasksByUserFlow(userId: Long): Flow<List<UserTaskEntity>> =
        flowOf(saved.values.filter { it.userId == userId })

    override fun getAllFlow(): Flow<List<UserTaskEntity>> =
        flowOf(saved.values.toList())

    override suspend fun updateProgress(
        userId: Long,
        taskId: Long,
        percentage: Int,
        timeSpent: Int,
        workDate: Long?,
        location: String?,
        updatedAt: Long
    ) {
        val current = saved.getValue(userId to taskId)
        saved[userId to taskId] = current.copy(
            completionPercentage = percentage,
            timeSpentMinutes = timeSpent,
            workDate = workDate,
            location = location,
            updatedAt = updatedAt
        )
    }

    override suspend fun markCompleted(userId: Long, taskId: Long, updatedAt: Long) {
        val current = saved.getValue(userId to taskId)
        saved[userId to taskId] = current.copy(isCompleted = true, updatedAt = updatedAt)
    }

    override suspend fun deleteAllForTask(taskId: Long) {
        saved.keys.filter { it.second == taskId }.forEach(saved::remove)
    }

    override suspend fun delete(taskId: Long, userId: Long) {
        saved.remove(userId to taskId)
    }
}

private class FakeTaskRepository : TaskRepository {
    var lastStatusUpdate: Pair<Long, TaskStatus>? = null

    override suspend fun createTask(task: Task): Long = task.id
    override suspend fun updateTask(task: Task) = Unit
    override suspend fun deleteTask(id: Long) = Unit
    override suspend fun getTaskById(id: Long): Task? = null
    override fun getTasksByProjectFlow(projectId: Long): Flow<List<Task>> = flowOf(emptyList())
    override fun getTasksByProjectAndStatusFlow(projectId: Long, status: TaskStatus): Flow<List<Task>> =
        flowOf(emptyList())
    override fun getPendingTasksForUserFlow(userId: Long): Flow<List<Task>> = flowOf(emptyList())
    override fun getCompletedTasksForUserFlow(userId: Long): Flow<List<Task>> = flowOf(emptyList())
    override fun searchTasksFlow(
        query: String,
        projectId: Long?,
        status: TaskStatus?,
        priority: TaskPriority?
    ): Flow<List<Task>> = flowOf(emptyList())
    override suspend fun updateTaskStatus(id: Long, status: TaskStatus) {
        lastStatusUpdate = id to status
    }
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
