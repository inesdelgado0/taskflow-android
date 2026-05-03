package com.taskflow.domain.repository

import com.taskflow.domain.model.SyncQueueItem
import com.taskflow.domain.util.HttpMethod

interface SyncQueueRepository {

    suspend fun enqueue(endpoint: String, method: HttpMethod, payload: String?): Long
    suspend fun peek(): SyncQueueItem?
    suspend fun getAll(): List<SyncQueueItem>
    suspend fun remove(id: Long)
    suspend fun incrementRetry(id: Long, error: String)
    suspend fun count(): Int
}
