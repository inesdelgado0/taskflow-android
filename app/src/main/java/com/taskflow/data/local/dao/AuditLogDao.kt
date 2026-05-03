package com.taskflow.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.taskflow.data.local.entity.AuditLogEntity
import com.taskflow.domain.util.AuditAction
import kotlinx.coroutines.flow.Flow

@Dao
interface AuditLogDao {

    // Append-only: nunca update nem delete em condições normais
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(log: AuditLogEntity): Long

    @Query("SELECT * FROM audit_log ORDER BY timestamp DESC")
    fun getAllFlow(): Flow<List<AuditLogEntity>>

    @Query("SELECT * FROM audit_log WHERE user_id = :userId ORDER BY timestamp DESC")
    fun getByUserFlow(userId: Long): Flow<List<AuditLogEntity>>

    @Query("SELECT * FROM audit_log WHERE action = :action ORDER BY timestamp DESC")
    fun getByActionFlow(action: AuditAction): Flow<List<AuditLogEntity>>

    @Query("SELECT * FROM audit_log WHERE timestamp BETWEEN :from AND :to ORDER BY timestamp DESC")
    fun getByRangeFlow(from: Long, to: Long): Flow<List<AuditLogEntity>>
}
