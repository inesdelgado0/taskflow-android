package com.taskflow.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.taskflow.domain.util.AuditAction

@Entity(
    tableName = "audit_log",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["user_id"]),
        Index(value = ["action"]),
        Index(value = ["timestamp"])
    ]
)
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // Nullable: ações do sistema (ex: sync automático) não têm utilizador
    @ColumnInfo(name = "user_id")
    val userId: Long? = null,

    @ColumnInfo(name = "action")
    val action: AuditAction,

    @ColumnInfo(name = "entity_type")
    val entityType: String? = null,  // "USER", "PROJECT", "TASK", etc.

    @ColumnInfo(name = "entity_id")
    val entityId: Long? = null,

    // Contexto adicional em JSON (ex: campos alterados)
    @ColumnInfo(name = "details")
    val details: String? = null,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long
)
