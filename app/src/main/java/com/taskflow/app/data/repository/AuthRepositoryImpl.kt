package com.taskflow.app.data.repository

import com.taskflow.app.data.remote.TokenManager
import com.taskflow.app.data.remote.api.AuthApi
import com.taskflow.app.data.remote.dto.LoginRequest
import com.taskflow.app.data.remote.dto.RegisterRequest
import com.taskflow.app.data.remote.dto.UserDto
import com.taskflow.app.domain.model.User
import com.taskflow.app.domain.repository.AuthRepository
import com.taskflow.app.domain.util.UserRole
import com.taskflow.app.util.ApiResult
import com.taskflow.app.util.safeApiCall
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val userRepository: UserRepositoryImpl,
    private val authApi: AuthApi,
    private val tokenManager: TokenManager
) : AuthRepository {

    override suspend fun login(email: String, password: String, role: UserRole): Result<User> {
        return when (val result = safeApiCall { authApi.login(LoginRequest(email.trim(), password)) }) {
            is ApiResult.Error -> Result.failure(IllegalArgumentException("Invalid credentials."))
            is ApiResult.Success -> {
                val apiUser = result.data.user.toDomain(activeRole = role)

                if (!apiUser.roles.contains(role) || !apiUser.isActive) {
                    return Result.failure(IllegalArgumentException("Invalid credentials."))
                }

                tokenManager.saveToken(
                    token = result.data.token,
                    role = role.name,
                    userId = apiUser.id
                )

                cacheUser(apiUser)
                Result.success(apiUser.copy(role = role))
            }
        }
    }

    override suspend fun register(
        name: String,
        username: String,
        email: String,
        password: String
    ): Result<User> {
        val request = RegisterRequest(
            name = name.trim(),
            username = username.trim(),
            email = email.trim(),
            password = password,
            roles = listOf(UserRole.USER.name)
        )

        return when (val result = safeApiCall { authApi.register(request) }) {
            is ApiResult.Error -> Result.failure(IllegalArgumentException("Unable to register user."))
            is ApiResult.Success -> {
                val user = result.data.user.toDomain(activeRole = UserRole.USER)
                tokenManager.saveToken(
                    token = result.data.token,
                    role = user.role.name,
                    userId = user.id
                )
                cacheUser(user)
                Result.success(user)
            }
        }
    }

    override suspend fun logout() {
        tokenManager.clearTokens()
    }

    private suspend fun cacheUser(user: User) {
        val existing = userRepository.getUserById(user.id)
        if (existing == null) {
            userRepository.createUser(user)
        } else {
            userRepository.updateUser(user)
        }
    }

    private fun UserDto.toDomain(activeRole: UserRole? = null): User {
        val parsedRoles = roles
            .mapNotNull { value -> value.toUserRoleOrNull() }
            .ifEmpty { listOf(UserRole.USER) }

        val primaryRole = activeRole?.takeIf { parsedRoles.contains(it) } ?: parsedRoles.first()
        val now = System.currentTimeMillis()

        return User(
            id = id,
            name = name,
            username = username,
            email = email,
            passwordHash = "",
            photoUrl = photoUrl,
            role = primaryRole,
            roles = parsedRoles,
            isActive = isActive,
            createdAt = createdAt ?: now,
            updatedAt = updatedAt ?: now
        )
    }

    private fun String.toUserRoleOrNull(): UserRole? =
        runCatching { UserRole.valueOf(this) }.getOrNull()
}

