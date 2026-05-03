package com.taskflow.data.repository

import com.taskflow.data.local.dao.SyncQueueDao
import com.taskflow.data.local.entity.SyncQueueEntity
import com.taskflow.domain.model.SyncQueueItem
import com.taskflow.domain.repository.SyncQueueRepository
import com.taskflow.domain.util.HttpMethod
import javax.inject.Inject

class SyncQueueRepositoryImpl @Inject constructor(
    private val syncQueueDao: SyncQueueDao
) : SyncQueueRepository {

    override suspend fun enqueue(endpoint: String, method: HttpMethod, payload: String?): Long =
        syncQueueDao.enqueue(
            SyncQueueEntity(
                endpoint = endpoint,
                httpMethod = method,
                payload = payload,
                createdAt = System.currentTimeMillis()
            )
        )

    override suspend fun peek(): SyncQueueItem? =
        syncQueueDao.peek()?.toDomain()

    override suspend fun getAll(): List<SyncQueueItem> =
        syncQueueDao.getAll().map { it.toDomain() }

    override suspend fun remove(id: Long) =
        syncQueueDao.remove(id)

    override suspend fun incrementRetry(id: Long, error: String) =
        syncQueueDao.incrementRetry(id, error)

    override suspend fun count(): Int =
        syncQueueDao.count()

    // ── Mapper ───────────────────────────────────────────────────────────────

    private fun SyncQueueEntity.toDomain() = SyncQueueItem(
        id = id,
        endpoint = endpoint,
        httpMethod = httpMethod,
        payload = payload,
        createdAt = createdAt,
        retryCount = retryCount,
        lastError = lastError
    )
}
