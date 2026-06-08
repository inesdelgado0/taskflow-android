package com.taskflow.app.data.remote.api

import com.taskflow.app.data.remote.dto.UpdateProfileRequest
import com.taskflow.app.data.remote.dto.UserDto
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT

interface UserApi {
    @GET("users")
    suspend fun getUsers(): Response<List<UserDto>>

    @GET("users/me")
    suspend fun getMe(): Response<UserDto>

    @PUT("users/me")
    suspend fun updateProfile(@Body body: UpdateProfileRequest): Response<UserDto>

    @POST("users/me/photo")
    suspend fun uploadProfilePhoto(
        @Header("Content-Type") contentType: String,
        @Body body: RequestBody
    ): Response<UserDto>
}
