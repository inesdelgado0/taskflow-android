package com.taskflow.app.domain.model

data class Evaluation(
    val id: Long = 0,
    val projectId: Long,
    val evaluatorId: Long,
    val evaluatedUserId: Long,
    val rating: Int,
    val comment: String? = null,
    val createdAt: Long
)
