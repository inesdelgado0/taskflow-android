package com.taskflow.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.taskflow.data.local.entity.UserProjectEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProjectDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(userProject: UserProjectEntity)

    @Delete
    suspend fun delete(userProject: UserProjectEntity)

    @Query("SELECT * FROM user_project WHERE project_id = :projectId")
    fun getUsersByProjectFlow(projectId: Long): Flow<List<UserProjectEntity>>

    @Query("SELECT * FROM user_project WHERE user_id = :userId")
    fun getProjectsByUserFlow(userId: Long): Flow<List<UserProjectEntity>>

    @Query("SELECT COUNT(*) FROM user_project WHERE user_id = :userId AND project_id = :projectId")
    suspend fun isUserInProject(userId: Long, projectId: Long): Int

    @Query("DELETE FROM user_project WHERE project_id = :projectId")
    suspend fun deleteAllForProject(projectId: Long)
}
