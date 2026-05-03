package com.taskflow.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.taskflow.data.local.entity.EvaluationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EvaluationDao {

    // REPLACE implementa o comportamento UPSERT: atualiza se já existir (project+user únicos)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(evaluation: EvaluationEntity): Long

    @Query("SELECT * FROM evaluations WHERE id = :id")
    suspend fun getById(id: Long): EvaluationEntity?

    @Query("SELECT * FROM evaluations WHERE project_id = :projectId AND evaluated_user_id = :userId LIMIT 1")
    suspend fun getForUserInProject(projectId: Long, userId: Long): EvaluationEntity?

    @Query("SELECT * FROM evaluations WHERE project_id = :projectId ORDER BY created_at DESC")
    fun getByProjectFlow(projectId: Long): Flow<List<EvaluationEntity>>

    @Query("SELECT * FROM evaluations WHERE evaluated_user_id = :userId ORDER BY created_at DESC")
    fun getByUserFlow(userId: Long): Flow<List<EvaluationEntity>>

    @Query("SELECT AVG(rating) FROM evaluations WHERE evaluated_user_id = :userId")
    suspend fun getAverageRating(userId: Long): Float?
}
