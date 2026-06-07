package com.taskflow.app.domain.repository

import com.taskflow.app.domain.model.User
import com.taskflow.app.domain.util.UserRole
import com.taskflow.app.util.ApiResult
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
    suspend fun refreshUsers(): ApiResult<List<User>>
    suspend fun pushUser(user: User, password: String?): ApiResult<User>
    suspend fun updateUserRolesRemote(id: Long, roles: List<UserRole>): ApiResult<User>
    suspend fun deleteUserRemote(id: Long): ApiResult<Unit>
    suspend fun updateProfileRemote(user: User, newPassword: String?): ApiResult<User>
}

