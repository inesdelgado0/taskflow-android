package com.taskflow.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.taskflow.app.domain.util.UserRole

@Entity(
    tableName = "roles",
    indices = [
        Index(value = ["code"], unique = true)
    ]
)
data class RoleEntity(
    @PrimaryKey
    val id: Long,

    @ColumnInfo(name = "code")
    val code: UserRole,

    @ColumnInfo(name = "description")
    val description: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = 0L
)

