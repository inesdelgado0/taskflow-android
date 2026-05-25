package com.taskflow.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "user_roles",
    primaryKeys = ["user_id", "role_id"],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = RoleEntity::class,
            parentColumns = ["id"],
            childColumns = ["role_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["user_id"]),
        Index(value = ["role_id"])
    ]
)
data class UserRoleEntity(
    @ColumnInfo(name = "user_id")
    val userId: Long,

    @ColumnInfo(name = "role_id")
    val roleId: Long,

    @ColumnInfo(name = "assigned_at")
    val assignedAt: Long
)

