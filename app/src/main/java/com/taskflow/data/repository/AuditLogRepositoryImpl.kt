package com.taskflow.data.repository

import com.taskflow.data.local.dao.AuditLogDao
import com.taskflow.data.local.entity.AuditLogEntity
import com.taskflow.domain.model.AuditLogItem
import com.taskflow.domain.repository.AuditLogRepository
import com.taskflow.domain.util.AuditAction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuditLogRepositoryImpl @Inject constructor(
    private val auditLogDao: AuditLogDao
) : AuditLogRepository {

    override suspend fun log(
        action: AuditAction,
        userId: Long?,
        entityType: String?,
        entityId: Long?,
        details: String?
    ): Long = auditLogDao.insert(
        AuditLogEntity(
            userId     = userId,
            action     = action,
            entityType = entityType,
            entityId   = entityId,
            details    = details,
            timestamp  = System.currentTimeMillis()
        )
    )

    override fun getAllFlow(): Flow<List<AuditLogItem>> =
        auditLogDao.getAllFlow().map { list -> list.map { it.toDomain() } }

    override fun getByUserFlow(userId: Long): Flow<List<AuditLogItem>> =
        auditLogDao.getByUserFlow(userId).map { list -> list.map { it.toDomain() } }

    override fun getByActionFlow(action: AuditAction): Flow<List<AuditLogItem>> =
        auditLogDao.getByActionFlow(action).map { list -> list.map { it.toDomain() } }

    override fun getByRangeFlow(from: Long, to: Long): Flow<List<AuditLogItem>> =
        auditLogDao.getByRangeFlow(from, to).map { list -> list.map { it.toDomain() } }

    // ── Mapper ───────────────────────────────────────────────
    private fun AuditLogEntity.toDomain() = AuditLogItem(
        id         = id,
        userId     = userId,
        action     = action,
        entityType = entityType,
        entityId   = entityId,
        details    = details,
        timestamp  = timestamp
    )
}