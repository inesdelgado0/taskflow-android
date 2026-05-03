package com.taskflow.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.taskflow.data.local.entity.UserTaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserTaskDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(userTask: UserTaskEntity)

    // REPLACE para atualizar os dados de execução (progresso, tempo, etc.)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(userTask: UserTaskEntity)

    @Query("SELECT * FROM user_task WHERE user_id = :userId AND task_id = :taskId")
    suspend fun get(userId: Long, taskId: Long): UserTaskEntity?

    @Query("SELECT * FROM user_task WHERE task_id = :taskId")
    fun getUsersByTaskFlow(taskId: Long): Flow<List<UserTaskEntity>>

    @Query("SELECT * FROM user_task WHERE user_id = :userId")
    fun getTasksByUserFlow(userId: Long): Flow<List<UserTaskEntity>>

    @Query("""
        UPDATE user_task SET
            completion_percentage = :percentage,
            time_spent_minutes = :timeSpent,
            work_date = :workDate,
            location = :location,
            updated_at = :updatedAt
        WHERE user_id = :userId AND task_id = :taskId
    """)
    suspend fun updateProgress(
        userId: Long,
        taskId: Long,
        percentage: Int,
        timeSpent: Int,
        workDate: Long?,
        location: String?,
        updatedAt: Long
    )

    @Query("UPDATE user_task SET is_completed = 1, updated_at = :updatedAt WHERE user_id = :userId AND task_id = :taskId")
    suspend fun markCompleted(userId: Long, taskId: Long, updatedAt: Long)

    @Query("DELETE FROM user_task WHERE task_id = :taskId")
    suspend fun deleteAllForTask(taskId: Long)
}