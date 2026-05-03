package com.taskflow.domain.model

import com.taskflow.domain.util.HttpMethod

data class SyncQueueItem(
    val id: Long = 0,
    val endpoint: String,
    val httpMethod: HttpMethod,
    val payload: String? = null,
    val createdAt: Long,
    val retryCount: Int = 0,
    val lastError: String? = null
)
