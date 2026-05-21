package com.taskflow.app.data.remote.api

import com.taskflow.app.data.remote.dto.AssignManagerRequest
import com.taskflow.app.data.remote.dto.ProjectDto
import com.taskflow.app.data.remote.dto.ProjectRequest
import com.taskflow.app.data.remote.dto.ProjectStatusRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ProjectApi {
    @GET("projects")
    suspend fun getProjects(): Response<List<ProjectDto>>

    @GET("projects/{id}")
    suspend fun getProject(@Path("id") id: Long): Response<ProjectDto>

    @POST("projects")
    suspend fun createProject(@Body body: ProjectRequest): Response<ProjectDto>

    @PUT("projects/{id}")
    suspend fun updateProject(
        @Path("id") id: Long,
        @Body body: ProjectRequest
    ): Response<ProjectDto>

    @PUT("projects/{id}/manager")
    suspend fun assignManager(
        @Path("id") id: Long,
        @Body body: AssignManagerRequest
    ): Response<ProjectDto>

    @PUT("projects/{id}/complete")
    suspend fun completeProject(@Path("id") id: Long): Response<ProjectDto>

    @PUT("projects/{id}/status")
    suspend fun updateStatus(
        @Path("id") id: Long,
        @Body body: ProjectStatusRequest
    ): Response<ProjectDto>

    @DELETE("projects/{id}")
    suspend fun deleteProject(@Path("id") id: Long): Response<Unit>
}
