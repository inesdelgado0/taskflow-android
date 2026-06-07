package com.taskflow.app.data.repository

import com.taskflow.app.audit.AuditLogger
import com.taskflow.app.data.local.dao.UserDao
import com.taskflow.app.data.local.database.AppDatabase
import com.taskflow.app.data.local.entity.RoleEntity
import com.taskflow.app.data.local.entity.UserEntity
import com.taskflow.app.data.local.entity.UserRoleEntity
import com.taskflow.app.data.local.entity.UserWithRoles
import com.taskflow.app.data.remote.dto.UpdateProfileRequest
import com.taskflow.app.data.remote.api.UserApi
import com.taskflow.app.data.remote.TokenManager
import com.taskflow.app.data.remote.dto.UserRequest
import com.taskflow.app.data.remote.dto.UserRolesRequest
import com.taskflow.app.data.remote.dto.UserDto
import com.taskflow.app.domain.model.User
import com.taskflow.app.domain.repository.UserRepository
import com.taskflow.app.domain.util.UserRole
import com.taskflow.app.util.ApiResult
import com.taskflow.app.util.map
import com.taskflow.app.util.onSuccess
import com.taskflow.app.util.safeApiCall
import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val database: AppDatabase,
    private val userApi: UserApi,
    private val tokenManager: TokenManager,
    private val auditLogger: AuditLogger
) : UserRepository {

    override suspend fun createUser(user: User): Long {
        return database.withTransaction {
            seedRoles()
            val userId = userDao.insert(user.toEntity())
            userDao.replaceRolesForUser(userId, user.effectiveRoles())
            userId
        }
    }

    override suspend fun updateUser(user: User) {
        database.withTransaction {
            seedRoles()
            userDao.update(user.toEntity())
            userDao.replaceRolesForUser(user.id, user.effectiveRoles())
        }
    }

    override suspend fun deleteUser(id: Long) {
        val entity = userDao.getById(id) ?: return
        userDao.delete(entity)
    }

    override suspend fun getUserById(id: Long): User? =
        userDao.getWithRolesById(id)?.toDomain()

    override suspend fun getUserByEmail(email: String): User? =
        userDao.getWithRolesByEmail(email)?.toDomain()

    override suspend fun getUserByUsername(username: String): User? =
        userDao.getWithRolesByUsername(username)?.toDomain()

    override fun getAllUsersFlow(): Flow<List<User>> =
        userDao.getAllFlow().map { list -> list.map { it.toDomain() } }

    override fun getUsersByRoleFlow(role: UserRole): Flow<List<User>> =
        userDao.getByRoleFlow(role).map { list -> list.map { it.toDomain() } }

    override fun searchUsersFlow(query: String): Flow<List<User>> =
        userDao.searchFlow(query).map { list -> list.map { it.toDomain() } }

    override suspend fun setUserActive(id: Long, isActive: Boolean) {
        userDao.setActive(id, isActive, System.currentTimeMillis())
    }

    override suspend fun refreshUsers(): ApiResult<List<User>> =
        safeApiCall { userApi.getUsers() }
            .map { users -> users.map { it.toDomain() } }
            .onSuccess { users ->
                database.withTransaction {
                    seedRoles()
                    userDao.upsertAll(users.map { it.toEntity() })
                    users.forEach { user ->
                        userDao.replaceRolesForUser(user.id, user.effectiveRoles())
                    }
                }
            }

    override suspend fun pushUser(user: User, password: String?): ApiResult<User> {
        val isCreate = user.id == 0L
        val request = user.toRequest(password)
        val result = if (user.id == 0L) {
            safeApiCall { userApi.createUser(request) }
        } else {
            safeApiCall { userApi.updateUser(user.id, request) }
        }

        return result
            .map { it.toDomain() }
            .onSuccess { syncedUser ->
                upsertUserWithRoles(syncedUser)
                val details = "email=${syncedUser.email},roles=${syncedUser.roles.joinToString(",")}"
                if (isCreate) {
                    auditLogger.logCreate(currentActorId(), "USER", syncedUser.id, details)
                } else {
                    auditLogger.logUpdate(currentActorId(), "USER", syncedUser.id, details)
                }
            }
    }

    override suspend fun updateUserRolesRemote(id: Long, roles: List<UserRole>): ApiResult<User> =
        safeApiCall { userApi.updateRoles(id, UserRolesRequest(roles.map { it.name })) }
            .map { it.toDomain() }
            .onSuccess { syncedUser ->
                upsertUserWithRoles(syncedUser)
                auditLogger.logUpdate(
                    currentActorId(),
                    "USER",
                    syncedUser.id,
                    details = "roles=${syncedUser.roles.joinToString(",")}"
                )
            }

    override suspend fun deleteUserRemote(id: Long): ApiResult<Unit> =
        safeApiCall { userApi.deleteUser(id) }
            .onSuccess {
                deleteUser(id)
                auditLogger.logDelete(currentActorId(), "USER", id)
            }

    override suspend fun updateProfileRemote(user: User, newPassword: String?): ApiResult<User> =
        safeApiCall {
            userApi.updateProfile(
                UpdateProfileRequest(
                    name = user.name,
                    username = user.username,
                    email = user.email,
                    photoUrl = user.photoUrl,
                    password = newPassword?.takeIf { it.isNotBlank() }
                )
            )
        }
            .map { updatedUser -> updatedUser.toDomain() }
            .onSuccess { updatedUser ->
                upsertUserWithRoles(updatedUser)
                auditLogger.logUpdate(
                    currentActorId(),
                    "USER",
                    updatedUser.id,
                    details = "profile:${updatedUser.email}"
                )
            }

    private suspend fun upsertUserWithRoles(user: User) {
        database.withTransaction {
            seedRoles()
            val existing = userDao.getById(user.id)
            if (existing == null) {
                userDao.insert(user.toEntity())
            } else {
                userDao.update(user.toEntity())
            }
            userDao.replaceRolesForUser(user.id, user.effectiveRoles())
        }
    }

    private suspend fun seedRoles() {
        userDao.upsertRoles(
            listOf(
                RoleEntity(id = UserRole.ADMIN.roleId, code = UserRole.ADMIN, description = "Administrador"),
                RoleEntity(id = UserRole.MANAGER.roleId, code = UserRole.MANAGER, description = "Gestor de Projeto"),
                RoleEntity(id = UserRole.USER.roleId, code = UserRole.USER, description = "Utilizador")
            )
        )
    }

    private suspend fun UserDao.replaceRolesForUser(userId: Long, roles: List<UserRole>) {
        deleteRolesForUser(userId)
        insertUserRoles(
            roles.map { role ->
                UserRoleEntity(
                    userId = userId,
                    roleId = role.roleId,
                    assignedAt = System.currentTimeMillis()
                )
            }
        )
    }

    private fun User.effectiveRoles(): List<UserRole> =
        roles.ifEmpty { listOf(role) }.distinct()

    private suspend fun currentActorId(): Long? = tokenManager.getUserId()

    private val UserRole.roleId: Long
        get() = when (this) {
            UserRole.ADMIN -> 1L
            UserRole.MANAGER -> 2L
            UserRole.USER -> 3L
        }

    private fun User.toEntity() = UserEntity(
        id = id,
        name = name,
        username = username,
        email = email,
        passwordHash = passwordHash,
        photoUrl = photoUrl,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun UserWithRoles.toDomain(): User {
        val roleList = roles
            .map { it.code }
            .ifEmpty { listOf(UserRole.USER) }
            .distinct()

        return User(
            id = user.id,
            name = user.name,
            username = user.username,
            email = user.email,
            passwordHash = user.passwordHash,
            photoUrl = user.photoUrl,
            role = roleList.first(),
            roles = roleList,
            isActive = user.isActive,
            createdAt = user.createdAt,
            updatedAt = user.updatedAt
        )
    }

    private fun UserDto.toDomain(): User {
        val roleList = roles
            .mapNotNull { value -> runCatching { UserRole.valueOf(value) }.getOrNull() }
            .ifEmpty { listOf(UserRole.USER) }
        val now = System.currentTimeMillis()

        return User(
            id = id,
            name = name,
            username = username,
            email = email,
            passwordHash = "",
            photoUrl = photoUrl,
            role = roleList.first(),
            roles = roleList,
            isActive = isActive,
            createdAt = createdAt ?: now,
            updatedAt = updatedAt ?: now
        )
    }

    private fun User.toRequest(password: String?) = UserRequest(
        name = name,
        username = username,
        email = email,
        password = password?.takeIf { it.isNotBlank() },
        roles = effectiveRoles().map { it.name },
        photoUrl = photoUrl,
        isActive = isActive
    )
}
