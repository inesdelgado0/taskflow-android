package com.taskflow.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "evaluations",
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
            childColumns = ["evaluator_id"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["evaluated_user_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["project_id"]),
        Index(value = ["evaluator_id"]),
        Index(value = ["evaluated_user_id"]),
        // Um utilizador só pode ser avaliado uma vez por projeto
        Index(value = ["project_id", "evaluated_user_id"], unique = true)
    ]
)
data class EvaluationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "project_id")
    val projectId: Long,

    @ColumnInfo(name = "evaluator_id")
    val evaluatorId: Long,

    @ColumnInfo(name = "evaluated_user_id")
    val evaluatedUserId: Long,

    // Validação 1-5 feita no UseCase
    @ColumnInfo(name = "rating")
    val rating: Int,

    @ColumnInfo(name = "comment")
    val comment: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long
)
