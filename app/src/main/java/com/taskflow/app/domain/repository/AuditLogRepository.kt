package com.taskflow.app.domain.repository

import com.taskflow.app.domain.model.AuditLogItem
import com.taskflow.app.domain.util.AuditAction
import kotlinx.coroutines.flow.Flow

interface AuditLogRepository {

    /** Regista uma ação no audit log */
    suspend fun log(
        action: AuditAction,
        userId: Long? = null,
        entityType: String? = null,
        entityId: Long? = null,
        details: String? = null
    ): Long

    fun getAllFlow(): Flow<List<AuditLogItem>>

    fun getByUserFlow(userId: Long): Flow<List<AuditLogItem>>

    fun getByActionFlow(action: AuditAction): Flow<List<AuditLogItem>>

    fun getByRangeFlow(from: Long, to: Long): Flow<List<AuditLogItem>>
}
