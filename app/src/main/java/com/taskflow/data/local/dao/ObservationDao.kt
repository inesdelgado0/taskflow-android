package com.taskflow.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.taskflow.data.local.entity.ObservationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ObservationDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(observation: ObservationEntity): Long

    @Delete
    suspend fun delete(observation: ObservationEntity)

    @Query("SELECT * FROM observations WHERE id = :id")
    suspend fun getById(id: Long): ObservationEntity?

    @Query("SELECT * FROM observations WHERE task_id = :taskId ORDER BY created_at DESC")
    fun getByTaskFlow(taskId: Long): Flow<List<ObservationEntity>>

    @Query("SELECT * FROM observations WHERE task_id = :taskId AND user_id = :userId ORDER BY created_at DESC")
    fun getByTaskAndUserFlow(taskId: Long, userId: Long): Flow<List<ObservationEntity>>
}
