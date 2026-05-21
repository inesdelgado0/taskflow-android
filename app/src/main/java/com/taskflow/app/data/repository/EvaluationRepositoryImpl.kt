package com.taskflow.app.data.repository

import com.taskflow.app.data.local.dao.EvaluationDao
import com.taskflow.app.data.local.entity.EvaluationEntity
import com.taskflow.app.data.remote.api.EvaluationApi
import com.taskflow.app.data.remote.dto.EvaluationDto
import com.taskflow.app.data.remote.dto.EvaluationRequest
import com.taskflow.app.domain.model.Evaluation
import com.taskflow.app.domain.repository.EvaluationRepository
import com.taskflow.app.util.ApiResult
import com.taskflow.app.util.map
import com.taskflow.app.util.onSuccess
import com.taskflow.app.util.safeApiCall
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class EvaluationRepositoryImpl @Inject constructor(
    private val evaluationDao: EvaluationDao,
    private val evaluationApi: EvaluationApi
) : EvaluationRepository {

    override suspend fun upsertEvaluation(evaluation: Evaluation): Long =
        evaluationDao.upsert(evaluation.toEntity())

    override suspend fun getEvaluationById(id: Long): Evaluation? =
        evaluationDao.getById(id)?.toDomain()

    override suspend fun getEvaluationForUserInProject(projectId: Long, userId: Long): Evaluation? =
        evaluationDao.getForUserInProject(projectId, userId)?.toDomain()

    override fun getEvaluationsByProjectFlow(projectId: Long): Flow<List<Evaluation>> =
        evaluationDao.getByProjectFlow(projectId).map { list -> list.map { it.toDomain() } }

    override fun getEvaluationsByUserFlow(userId: Long): Flow<List<Evaluation>> =
        evaluationDao.getByUserFlow(userId).map { list -> list.map { it.toDomain() } }

    override suspend fun getAverageRating(userId: Long): Float? =
        evaluationDao.getAverageRating(userId)

    override suspend fun refreshEvaluations(projectId: Long): ApiResult<List<Evaluation>> =
        safeApiCall { evaluationApi.getProjectEvaluations(projectId) }
            .map { evaluations -> evaluations.map { it.toDomain() } }
            .onSuccess { evaluations -> evaluationDao.upsertAll(evaluations.map { it.toEntity() }) }

    override suspend fun pushEvaluation(evaluation: Evaluation): ApiResult<Evaluation> =
        safeApiCall { evaluationApi.evaluateUser(evaluation.evaluatedUserId, evaluation.toRequest()) }
            .map { it.toDomain() }
            .onSuccess { synced -> evaluationDao.upsert(synced.toEntity()) }

    private fun Evaluation.toEntity() = EvaluationEntity(
        id = id,
        projectId = projectId,
        evaluatorId = evaluatorId,
        evaluatedUserId = evaluatedUserId,
        rating = rating,
        comment = comment,
        createdAt = createdAt
    )

    private fun EvaluationEntity.toDomain() = Evaluation(
        id = id,
        projectId = projectId,
        evaluatorId = evaluatorId,
        evaluatedUserId = evaluatedUserId,
        rating = rating,
        comment = comment,
        createdAt = createdAt
    )

    private fun EvaluationDto.toDomain() = Evaluation(
        id = id,
        projectId = projectId,
        evaluatorId = evaluatorId,
        evaluatedUserId = evaluatedUserId,
        rating = rating,
        comment = comment,
        createdAt = createdAt
    )

    private fun Evaluation.toRequest() = EvaluationRequest(
        projectId = projectId,
        evaluatorId = evaluatorId,
        rating = rating,
        comment = comment
    )
}
