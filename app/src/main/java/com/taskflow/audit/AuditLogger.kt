package com.taskflow.audit

import android.util.Log
import com.taskflow.domain.repository.AuditLogRepository
import com.taskflow.domain.util.AuditAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class AuditLogger @Inject constructor(
    private val auditLogRepository: AuditLogRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // ── Método genérico ───────────────────────────────────────
    fun log(
        action: AuditAction,
        userId: Long? = null,
        entityType: String? = null,
        entityId: Long? = null,
        details: String? = null
    ) {
        scope.launch {
            try {
                auditLogRepository.log(action, userId, entityType, entityId, details)
            } catch (e: Exception) {
                Log.e("AuditLogger", "Erro ao registar ação $action: ${e.message}")
            }
        }
    }

    // ── Auth ──────────────────────────────────────────────────
    fun logLogin(userId: Long) =
        log(AuditAction.LOGIN, userId = userId)

    fun logLogout(userId: Long) =
        log(AuditAction.LOGOUT, userId = userId)

    // ── CRUD ──────────────────────────────────────────────────
    fun logCreate(userId: Long, entityType: String, entityId: Long? = null, details: String? = null) =
        log(AuditAction.CREATE, userId, entityType, entityId, details)

    fun logUpdate(userId: Long, entityType: String, entityId: Long, details: String? = null) =
        log(AuditAction.UPDATE, userId, entityType, entityId, details)

    fun logDelete(userId: Long, entityType: String, entityId: Long) =
        log(AuditAction.DELETE, userId, entityType, entityId)

    // ── Sync ──────────────────────────────────────────────────
    fun logSync(userId: Long? = null, details: String? = null) =
        log(AuditAction.SYNC, userId = userId, entityType = "SYNC", details = details)
}