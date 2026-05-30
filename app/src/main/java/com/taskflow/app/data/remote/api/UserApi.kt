package com.taskflow.app.data.remote.api

import com.taskflow.app.data.remote.dto.UserDto
import retrofit2.Response
import retrofit2.http.GET

interface UserApi {
    @GET("users")
    suspend fun getUsers(): Response<List<UserDto>>
}
