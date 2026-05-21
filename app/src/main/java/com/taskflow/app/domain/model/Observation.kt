package com.taskflow.app.domain.model

data class Observation(
    val id: Long = 0,
    val taskId: Long,
    val userId: Long,
    val text: String? = null,
    val photoPath: String? = null,
    val createdAt: Long
)
