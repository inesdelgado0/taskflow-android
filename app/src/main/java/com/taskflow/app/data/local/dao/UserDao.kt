package com.taskflow.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import com.taskflow.app.data.local.entity.RoleEntity
import com.taskflow.app.data.local.entity.UserEntity
import com.taskflow.app.data.local.entity.UserRoleEntity
import com.taskflow.app.data.local.entity.UserWithRoles
import com.taskflow.app.domain.util.UserRole
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: UserEntity): Long

    @Upsert
    suspend fun upsertRoles(roles: List<RoleEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserRoles(userRoles: List<UserRoleEntity>)

    @Update
    suspend fun update(user: UserEntity)

    @Delete
    suspend fun delete(user: UserEntity)

    @Query("DELETE FROM user_roles WHERE user_id = :userId")
    suspend fun deleteRolesForUser(userId: Long)

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getById(id: Long): UserEntity?

    @Transaction
    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getWithRolesById(id: Long): UserWithRoles?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getByEmail(email: String): UserEntity?

    @Transaction
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getWithRolesByEmail(email: String): UserWithRoles?

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getByUsername(username: String): UserEntity?

    @Transaction
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getWithRolesByUsername(username: String): UserWithRoles?

    @Transaction
    @Query("SELECT * FROM users ORDER BY name ASC")
    fun getAllFlow(): Flow<List<UserWithRoles>>

    @Transaction
    @Query("""
        SELECT users.* FROM users
        INNER JOIN user_roles ON users.id = user_roles.user_id
        INNER JOIN roles ON user_roles.role_id = roles.id
        WHERE roles.code = :role
        ORDER BY users.name ASC
    """)
    fun getByRoleFlow(role: UserRole): Flow<List<UserWithRoles>>

    @Transaction
    @Query("SELECT * FROM users WHERE name LIKE '%' || :query || '%' OR username LIKE '%' || :query || '%'")
    fun searchFlow(query: String): Flow<List<UserWithRoles>>

    @Query("UPDATE users SET is_active = :isActive, updated_at = :updatedAt WHERE id = :id")
    suspend fun setActive(id: Long, isActive: Boolean, updatedAt: Long)
}

