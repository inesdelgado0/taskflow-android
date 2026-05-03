package com.taskflow.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "user_task",
    primaryKeys = ["user_id", "task_id"],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["task_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["user_id"]),
        Index(value = ["task_id"])
    ]
)
data class UserTaskEntity(
    @ColumnInfo(name = "user_id")
    val userId: Long,

    @ColumnInfo(name = "task_id")
    val taskId: Long,

    @ColumnInfo(name = "work_date")
    val workDate: Long? = null,

    @ColumnInfo(name = "location")
    val location: String? = null,

    @ColumnInfo(name = "completion_percentage", defaultValue = "0")
    val completionPercentage: Int = 0,

    @ColumnInfo(name = "time_spent_minutes", defaultValue = "0")
    val timeSpentMinutes: Int = 0,

    @ColumnInfo(name = "is_completed", defaultValue = "0")
    val isCompleted: Boolean = false,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long
)