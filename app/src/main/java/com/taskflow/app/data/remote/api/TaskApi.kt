package com.taskflow.app.data.remote.api

import com.taskflow.app.data.remote.dto.TaskDto
import com.taskflow.app.data.remote.dto.TaskRequest
import com.taskflow.app.data.remote.dto.TaskStatusRequest
import com.taskflow.app.data.remote.dto.UserTaskDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface TaskApi {
    @GET("projects/{projectId}/tasks")
    suspend fun getProjectTasks(@Path("projectId") projectId: Long): Response<List<TaskDto>>

    @GET("tasks/{id}")
    suspend fun getTask(@Path("id") id: Long): Response<TaskDto>

    @GET("users/{userId}/task-assignments")
    suspend fun getUserTaskAssignments(@Path("userId") userId: Long): Response<List<UserTaskDto>>

    @POST("projects/{projectId}/tasks")
    suspend fun createTask(
        @Path("projectId") projectId: Long,
        @Body body: TaskRequest
    ): Response<TaskDto>

    @PUT("tasks/{id}")
    suspend fun updateTask(
        @Path("id") id: Long,
        @Body body: TaskRequest
    ): Response<TaskDto>

    @PUT("tasks/{id}/status")
    suspend fun updateStatus(
        @Path("id") id: Long,
        @Body body: TaskStatusRequest
    ): Response<TaskDto>

    @DELETE("tasks/{id}")
    suspend fun deleteTask(@Path("id") id: Long): Response<Unit>
}
