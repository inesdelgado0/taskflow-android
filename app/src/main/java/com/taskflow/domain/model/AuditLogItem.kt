package com.taskflow.domain.model

import com.taskflow.domain.util.AuditAction

data class AuditLogItem(
    val id: Long = 0,
    val userId: Long? = null,
    val action: AuditAction,
    val entityType: String? = null,
    val entityId: Long? = null,
    val details: String? = null,
    val timestamp: Long
)