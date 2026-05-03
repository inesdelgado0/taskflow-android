package com.taskflow.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.taskflow.data.local.entity.ProjectEntity
import com.taskflow.domain.util.ProjectStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(project: ProjectEntity): Long

    @Update
    suspend fun update(project: ProjectEntity)

    @Delete
    suspend fun delete(project: ProjectEntity)

    @Query("SELECT * FROM projects WHERE id = :id")
    suspend fun getById(id: Long): ProjectEntity?

    @Query("SELECT * FROM projects ORDER BY created_at DESC")
    fun getAllFlow(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE status = :status ORDER BY created_at DESC")
    fun getByStatusFlow(status: ProjectStatus): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE manager_id = :managerId ORDER BY created_at DESC")
    fun getByManagerFlow(managerId: Long): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE name LIKE '%' || :query || '%'")
    fun searchFlow(query: String): Flow<List<ProjectEntity>>

    @Query("UPDATE projects SET status = :status, updated_at = :updatedAt WHERE id = :id")
    suspend fun updateStatus(id: Long, status: ProjectStatus, updatedAt: Long)

    @Query("UPDATE projects SET manager_id = :managerId, updated_at = :updatedAt WHERE id = :id")
    suspend fun assignManager(id: Long, managerId: Long?, updatedAt: Long)
}
