package com.taskflow.app.data.remote.api

import com.taskflow.app.data.remote.dto.EvaluationDto
import com.taskflow.app.data.remote.dto.EvaluationRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface EvaluationApi {
    @GET("projects/{projectId}/evaluations")
    suspend fun getProjectEvaluations(@Path("projectId") projectId: Long): Response<List<EvaluationDto>>

    @PUT("users/{userId}/evaluate")
    suspend fun evaluateUser(
        @Path("userId") userId: Long,
        @Body body: EvaluationRequest
    ): Response<EvaluationDto>
}
