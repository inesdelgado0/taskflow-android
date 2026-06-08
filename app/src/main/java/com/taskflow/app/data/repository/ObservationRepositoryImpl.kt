package com.taskflow.app.data.repository

import android.database.sqlite.SQLiteConstraintException
import com.taskflow.app.audit.AuditLogger
import com.taskflow.app.data.local.dao.ObservationDao
import com.taskflow.app.data.local.dao.UserDao
import com.taskflow.app.data.local.entity.ObservationEntity
import com.taskflow.app.data.remote.api.ObservationApi
import com.taskflow.app.data.remote.TokenManager
import com.taskflow.app.data.remote.dto.ObservationDto
import com.taskflow.app.data.remote.dto.ObservationRequest
import com.taskflow.app.domain.model.Observation
import com.taskflow.app.domain.repository.ObservationRepository
import com.taskflow.app.util.ApiResult
import com.taskflow.app.util.map
import com.taskflow.app.util.onSuccess
import com.taskflow.app.util.safeApiCall
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class ObservationRepositoryImpl @Inject constructor(
    private val observationDao: ObservationDao,
    private val userDao: UserDao,
    private val observationApi: ObservationApi,
    private val tokenManager: TokenManager,
    private val auditLogger: AuditLogger
) : ObservationRepository {

    override suspend fun createObservation(observation: Observation): Long =
        observationDao.insert(observation.toEntity())

    override suspend fun deleteObservation(id: Long) {
        val entity = observationDao.getById(id) ?: return
        observationDao.delete(entity)
    }

    override suspend fun getObservationById(id: Long): Observation? =
        observationDao.getById(id)?.toDomain()

    override fun getObservationsByTaskFlow(taskId: Long): Flow<List<Observation>> =
        observationDao.getByTaskFlow(taskId).map { list -> list.map { it.toDomain() } }

    override fun getObservationsByTaskAndUserFlow(taskId: Long, userId: Long): Flow<List<Observation>> =
        observationDao.getByTaskAndUserFlow(taskId, userId).map { list -> list.map { it.toDomain() } }

    override suspend fun refreshObservations(taskId: Long): ApiResult<List<Observation>> =
        safeApiCall { observationApi.getTaskObservations(taskId) }
            .map { observations -> observations.map { it.toDomain() } }
            .onSuccess { observations ->
                runCatching {
                    observationDao.upsertAll(observations.map { it.toEntity() })
                }.onFailure { e ->
                    if (e is SQLiteConstraintException) {
                        observations.forEach { obs ->
                            if (userDao.getById(obs.userId) != null) {
                                runCatching { observationDao.upsert(obs.toEntity()) }
                            }
                        }
                    }
                }
            }

    override suspend fun pushObservation(observation: Observation): ApiResult<Observation> =
        safeApiCall { observationApi.createObservation(observation.taskId, observation.toRequest()) }
            .map { it.toDomain() }
            .onSuccess { synced ->
                observationDao.upsert(synced.toEntity())
                auditLogger.logCreate(
                    currentActorId(),
                    "OBSERVATION",
                    synced.id,
                    details = "taskId=${synced.taskId},userId=${synced.userId}"
                )
            }

    override suspend fun uploadObservationPhoto(
        id: Long,
        bytes: ByteArray,
        contentType: String
    ): ApiResult<Observation> =
        safeApiCall {
            observationApi.uploadObservationPhoto(
                id = id,
                contentType = contentType,
                body = bytes.toRequestBody(contentType.toMediaType())
            )
        }
            .map { it.toDomain() }
            .onSuccess { synced -> observationDao.upsert(synced.toEntity()) }

    override suspend fun deleteObservationRemote(id: Long): ApiResult<Unit> =
        safeApiCall { observationApi.deleteObservation(id) }
            .onSuccess {
                deleteObservation(id)
                auditLogger.logDelete(currentActorId(), "OBSERVATION", id)
            }

    private suspend fun currentActorId(): Long? = tokenManager.getUserId()

    private fun Observation.toEntity() = ObservationEntity(
        id = id,
        taskId = taskId,
        userId = userId,
        text = text,
        photoPath = photoPath,
        createdAt = createdAt
    )

    private fun ObservationEntity.toDomain() = Observation(
        id = id,
        taskId = taskId,
        userId = userId,
        text = text,
        photoPath = photoPath,
        createdAt = createdAt
    )

    private fun ObservationDto.toDomain() = Observation(
        id = id,
        taskId = taskId,
        userId = userId,
        text = text,
        photoPath = photoPath,
        createdAt = createdAt
    )

    private fun Observation.toRequest() = ObservationRequest(
        userId = userId,
        text = text,
        photoPath = photoPath
    )
}
