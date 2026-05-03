package com.taskflow.domain.repository

import com.taskflow.domain.model.User
import com.taskflow.domain.util.UserRole
import kotlinx.coroutines.flow.Flow

interface UserRepository {

    suspend fun createUser(user: User): Long
    suspend fun updateUser(user: User)
    suspend fun deleteUser(id: Long)
    suspend fun getUserById(id: Long): User?
    suspend fun getUserByEmail(email: String): User?
    suspend fun getUserByUsername(username: String): User?
    fun getAllUsersFlow(): Flow<List<User>>
    fun getUsersByRoleFlow(role: UserRole): Flow<List<User>>
    fun searchUsersFlow(query: String): Flow<List<User>>
    suspend fun setUserActive(id: Long, isActive: Boolean)
}
