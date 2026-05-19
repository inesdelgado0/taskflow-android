package com.taskflow.app.domain.repository

import com.taskflow.app.domain.model.User
import com.taskflow.app.domain.util.UserRole

interface AuthRepository {
    suspend fun login(email: String, password: String, role: UserRole): Result<User>
    suspend fun register(name: String, username: String, email: String, password: String): Result<User>
    suspend fun logout()
}
