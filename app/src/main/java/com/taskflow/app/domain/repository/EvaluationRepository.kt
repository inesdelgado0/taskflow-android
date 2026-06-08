package com.taskflow.app.domain.repository

import com.taskflow.app.domain.model.Evaluation
import com.taskflow.app.util.ApiResult
import kotlinx.coroutines.flow.Flow

interface EvaluationRepository {
    suspend fun upsertEvaluation(evaluation: Evaluation): Long
    suspend fun getEvaluationById(id: Long): Evaluation?
    suspend fun getEvaluationForUserInProject(projectId: Long, userId: Long): Evaluation?
    fun getAllEvaluationsFlow(): Flow<List<Evaluation>>
    fun getEvaluationsByProjectFlow(projectId: Long): Flow<List<Evaluation>>
    fun getEvaluationsByUserFlow(userId: Long): Flow<List<Evaluation>>
    suspend fun getAverageRating(userId: Long): Float?
    suspend fun refreshEvaluations(projectId: Long): ApiResult<List<Evaluation>>
    suspend fun pushEvaluation(evaluation: Evaluation): ApiResult<Evaluation>
}
