package com.taskflow.app.data.remote.api

import com.taskflow.app.data.remote.dto.UpdateProfileRequest
import com.taskflow.app.data.remote.dto.UserRequest
import com.taskflow.app.data.remote.dto.UserRolesRequest
import com.taskflow.app.data.remote.dto.UserDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface UserApi {
    @GET("users")
    suspend fun getUsers(): Response<List<UserDto>>

    @GET("users/{id}")
    suspend fun getUser(@Path("id") id: Long): Response<UserDto>

    @POST("users")
    suspend fun createUser(@Body body: UserRequest): Response<UserDto>

    @PUT("users/{id}")
    suspend fun updateUser(
        @Path("id") id: Long,
        @Body body: UserRequest
    ): Response<UserDto>

    @PUT("users/{id}/roles")
    suspend fun updateRoles(
        @Path("id") id: Long,
        @Body body: UserRolesRequest
    ): Response<UserDto>

    @DELETE("users/{id}")
    suspend fun deleteUser(@Path("id") id: Long): Response<Unit>

    @PUT("users/me")
    suspend fun updateProfile(@Body body: UpdateProfileRequest): Response<UserDto>
}
