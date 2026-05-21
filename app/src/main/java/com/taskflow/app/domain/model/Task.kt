package com.taskflow.app.domain.model

import com.taskflow.app.domain.util.TaskPriority
import com.taskflow.app.domain.util.TaskStatus

data class Task(
    val id: Long = 0,
    val projectId: Long,
    val title: String,
    val description: String? = null,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val deadline: Long? = null,
    val status: TaskStatus = TaskStatus.PENDING,
    val createdBy: Long,
    val createdAt: Long,
    val updatedAt: Long
)
