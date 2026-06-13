package com.taskflow.app.data.local.dao

import com.taskflow.app.data.local.entity.UserTaskEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UserTaskDaoContractTest {
    @Test
    fun insert_ignoresDuplicateCompositeKey() = runBlocking {
        val dao = FakeUserTaskDao()
        dao.insert(entity(userId = 4L, taskId = 1L, percentage = 20))
        dao.insert(entity(userId = 4L, taskId = 1L, percentage = 80))

        assertEquals(20, dao.get(4L, 1L)?.completionPercentage)
    }

    @Test
    fun upsert_replacesDuplicateCompositeKey() = runBlocking {
        val dao = FakeUserTaskDao()
        dao.upsert(entity(userId = 4L, taskId = 1L, percentage = 20))
        dao.upsert(entity(userId = 4L, taskId = 1L, percentage = 80))

        assertEquals(80, dao.get(4L, 1L)?.completionPercentage)
    }

    @Test
    fun updateProgress_updatesExecutionFieldsOnlyForMatchingAssignment() = runBlocking {
        val dao = FakeUserTaskDao()
        dao.upsert(entity(userId = 4L, taskId = 1L, percentage = 20))
        dao.upsert(entity(userId = 5L, taskId = 1L, percentage = 10))

        dao.updateProgress(
            userId = 4L,
            taskId = 1L,
            percentage = 70,
            timeSpent = 180,
            workDate = 1_780_000_000_000L,
            location = "Porto",
            isCompleted = false,
            updatedAt = 1_780_000_001_000L
        )

        val updated = dao.get(4L, 1L)
        assertEquals(70, updated?.completionPercentage)
        assertEquals(180, updated?.timeSpentMinutes)
        assertEquals("Porto", updated?.location)
        assertEquals(10, dao.get(5L, 1L)?.completionPercentage)
    }

    @Test
    fun markCompleted_setsCompletionFlag() = runBlocking {
        val dao = FakeUserTaskDao()
        dao.upsert(entity(userId = 4L, taskId = 1L, percentage = 100, completed = false))

        dao.markCompleted(userId = 4L, taskId = 1L, updatedAt = 2L)

        assertTrue(requireNotNull(dao.get(4L, 1L)).isCompleted)
    }

    @Test
    fun deleteAllForTask_removesOnlyMatchingTaskAssignments() = runBlocking {
        val dao = FakeUserTaskDao()
        dao.upsert(entity(userId = 4L, taskId = 1L))
        dao.upsert(entity(userId = 5L, taskId = 1L))
        dao.upsert(entity(userId = 4L, taskId = 2L))

        dao.deleteAllForTask(taskId = 1L)

        assertNull(dao.get(4L, 1L))
        assertNull(dao.get(5L, 1L))
        assertEquals(1, dao.getTasksByUserFlow(4L).first().size)
    }
}

private fun entity(
    userId: Long,
    taskId: Long,
    percentage: Int = 0,
    completed: Boolean = false
) = UserTaskEntity(
    userId = userId,
    taskId = taskId,
    completionPercentage = percentage,
    isCompleted = completed,
    updatedAt = 0L
)

private class FakeUserTaskDao : UserTaskDao {
    private val saved = mutableMapOf<Pair<Long, Long>, UserTaskEntity>()

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
        isCompleted: Boolean,
        updatedAt: Long
    ) {
        val current = saved.getValue(userId to taskId)
        saved[userId to taskId] = current.copy(
            completionPercentage = percentage,
            timeSpentMinutes = timeSpent,
            workDate = workDate,
            location = location,
            isCompleted = isCompleted,
            updatedAt = updatedAt
        )
    }

    override suspend fun markCompleted(userId: Long, taskId: Long, updatedAt: Long) {
        val current = saved.getValue(userId to taskId)
        saved[userId to taskId] = current.copy(isCompleted = true, updatedAt = updatedAt)
    }

    override suspend fun countUsersByTask(taskId: Long): Int =
        saved.values.count { it.taskId == taskId }

    override suspend fun delete(taskId: Long, userId: Long) {
        saved.remove(userId to taskId)
    }

    override suspend fun deleteAllForTask(taskId: Long) {
        saved.keys.filter { it.second == taskId }.forEach(saved::remove)
    }
}
