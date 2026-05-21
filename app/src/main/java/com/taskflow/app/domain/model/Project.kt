package com.taskflow.app.domain.model

import com.taskflow.app.domain.util.ProjectStatus

data class Project(
    val id: Long = 0,
    val name: String,
    val description: String? = null,
    val startDate: Long? = null,
    val endDate: Long? = null,
    val status: ProjectStatus = ProjectStatus.ACTIVE,
    val managerId: Long? = null,
    val createdBy: Long,
    val createdAt: Long,
    val updatedAt: Long
)
