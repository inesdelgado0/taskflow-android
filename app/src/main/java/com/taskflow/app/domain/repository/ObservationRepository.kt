package com.taskflow.app.domain.repository

import com.taskflow.app.domain.model.Observation
import com.taskflow.app.util.ApiResult
import kotlinx.coroutines.flow.Flow

interface ObservationRepository {
    suspend fun createObservation(observation: Observation): Long
    suspend fun deleteObservation(id: Long)
    suspend fun getObservationById(id: Long): Observation?
    fun getObservationsByTaskFlow(taskId: Long): Flow<List<Observation>>
    fun getObservationsByTaskAndUserFlow(taskId: Long, userId: Long): Flow<List<Observation>>
    suspend fun refreshObservations(taskId: Long): ApiResult<List<Observation>>
    suspend fun pushObservation(observation: Observation): ApiResult<Observation>
    suspend fun uploadObservationPhoto(id: Long, bytes: ByteArray, contentType: String): ApiResult<Observation>
    suspend fun deleteObservationRemote(id: Long): ApiResult<Unit>
}
