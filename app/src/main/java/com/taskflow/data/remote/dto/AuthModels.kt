package com.taskflow.data.remote.dto

import com.google.gson.annotations.SerializedName



data class LoginRequest(
    @SerializedName("email")    val email: String,
    @SerializedName("password") val password: String
)

data class RegisterRequest(
    @SerializedName("name")     val name: String,
    @SerializedName("username") val username: String,
    @SerializedName("email")    val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("role")     val role: String = "USER"
)

data class AuthResponse(
    @SerializedName("token") val token: String,
    @SerializedName("user")  val user: UserDto
)

data class UserDto(
    @SerializedName("id")         val id: Long,
    @SerializedName("name")       val name: String,
    @SerializedName("username")   val username: String,
    @SerializedName("email")      val email: String,
    @SerializedName("role")       val role: String,
    @SerializedName("photo_url")  val photoUrl: String?  = null,
    @SerializedName("is_active")  val isActive: Boolean  = true,
    @SerializedName("created_at") val createdAt: Long?   = null,
    @SerializedName("updated_at") val updatedAt: Long?   = null
)

data class RefreshTokenRequest(
    @SerializedName("refresh_token") val refreshToken: String
)

data class ApiErrorResponse(
    @SerializedName("message") val message: String?,
    @SerializedName("errors")  val errors: Map<String, List<String>>? = null
)