package com.taskflow.app.data.repository

import com.taskflow.app.domain.model.User
import com.taskflow.app.domain.repository.AuthRepository
import com.taskflow.app.domain.util.UserRole
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val userRepository: UserRepositoryImpl
) : AuthRepository {

    override suspend fun login(email: String, password: String, role: UserRole): Result<User> {
        val user = userRepository.getUserByEmail(email.trim())
            ?: return Result.failure(IllegalArgumentException("Invalid credentials."))

        if (user.passwordHash != password || user.role != role || !user.isActive) {
            return Result.failure(IllegalArgumentException("Invalid credentials."))
        }

        return Result.success(user)
    }

    override suspend fun register(
        name: String,
        username: String,
        email: String,
        password: String
    ): Result<User> {
        val now = System.currentTimeMillis()
        val user = User(
            name = name.trim(),
            username = username.trim(),
            email = email.trim(),
            passwordHash = password,
            role = UserRole.USER,
            createdAt = now,
            updatedAt = now
        )

        val id = userRepository.createUser(user)
        return Result.success(user.copy(id = id))
    }

    override suspend fun logout() {
        // Session persistence belongs to the app-level auth/session module.
    }
}
