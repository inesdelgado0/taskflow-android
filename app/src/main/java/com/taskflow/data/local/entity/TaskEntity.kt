package com.taskflow.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.taskflow.domain.util.TaskPriority
import com.taskflow.domain.util.TaskStatus

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["project_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["created_by"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["project_id"]),
        Index(value = ["status"]),
        Index(value = ["priority"]),
        Index(value = ["deadline"]),
        Index(value = ["created_by"])
    ]
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "project_id")
    val projectId: Long,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "description")
    val description: String? = null,

    @ColumnInfo(name = "priority", defaultValue = "MEDIUM")
    val priority: TaskPriority = TaskPriority.MEDIUM,

    @ColumnInfo(name = "deadline")
    val deadline: Long? = null,

    @ColumnInfo(name = "status", defaultValue = "PENDING")
    val status: TaskStatus = TaskStatus.PENDING,

    @ColumnInfo(name = "created_by")
    val createdBy: Long,

    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long
)
