package com.taskflow.app.data.remote.api

import com.taskflow.app.data.remote.dto.UpdateProfileRequest
import com.taskflow.app.data.remote.dto.UserDto
import retrofit2.http.Body
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.PUT

interface UserApi {
    @GET("users")
    suspend fun getUsers(): Response<List<UserDto>>

    @PUT("users/me")
    suspend fun updateProfile(@Body body: UpdateProfileRequest): Response<UserDto>
}
