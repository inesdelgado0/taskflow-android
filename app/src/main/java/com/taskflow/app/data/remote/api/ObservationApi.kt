package com.taskflow.app.data.remote.api

import com.taskflow.app.data.remote.dto.ObservationDto
import com.taskflow.app.data.remote.dto.ObservationRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ObservationApi {
    @GET("tasks/{taskId}/observations")
    suspend fun getTaskObservations(@Path("taskId") taskId: Long): Response<List<ObservationDto>>

    @POST("tasks/{taskId}/observations")
    suspend fun createObservation(
        @Path("taskId") taskId: Long,
        @Body body: ObservationRequest
    ): Response<ObservationDto>

    @DELETE("observations/{id}")
    suspend fun deleteObservation(@Path("id") id: Long): Response<Unit>
}
