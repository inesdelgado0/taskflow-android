package com.taskflow.data.repository

import com.taskflow.data.local.dao.UserDao
import com.taskflow.data.local.entity.UserEntity
import com.taskflow.domain.model.User
import com.taskflow.domain.repository.UserRepository
import com.taskflow.domain.util.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao
) : UserRepository {

    override suspend fun createUser(user: User): Long =
        userDao.insert(user.toEntity())

    override suspend fun updateUser(user: User) =
        userDao.update(user.toEntity())

    override suspend fun deleteUser(id: Long) {
        val entity = userDao.getById(id) ?: return
        userDao.delete(entity)
    }

    override suspend fun getUserById(id: Long): User? =
        userDao.getById(id)?.toDomain()

    override suspend fun getUserByEmail(email: String): User? =
        userDao.getByEmail(email)?.toDomain()

    override suspend fun getUserByUsername(username: String): User? =
        userDao.getByUsername(username)?.toDomain()

    override fun getAllUsersFlow(): Flow<List<User>> =
        userDao.getAllFlow().map { list -> list.map { it.toDomain() } }

    override fun getUsersByRoleFlow(role: UserRole): Flow<List<User>> =
        userDao.getByRoleFlow(role).map { list -> list.map { it.toDomain() } }

    override fun searchUsersFlow(query: String): Flow<List<User>> =
        userDao.searchFlow(query).map { list -> list.map { it.toDomain() } }

    override suspend fun setUserActive(id: Long, isActive: Boolean) {
        userDao.setActive(id, isActive, System.currentTimeMillis())
    }

    // ── Mappers ──────────────────────────────────────────────────────────────

    private fun User.toEntity() = UserEntity(
        id = id,
        name = name,
        username = username,
        email = email,
        passwordHash = passwordHash,
        photoUrl = photoUrl,
        role = role,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun UserEntity.toDomain() = User(
        id = id,
        name = name,
        username = username,
        email = email,
        passwordHash = passwordHash,
        photoUrl = photoUrl,
        role = role,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}