package com.taskflow.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.taskflow.data.local.entity.TaskEntity
import com.taskflow.domain.util.TaskPriority
import com.taskflow.domain.util.TaskStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(task: TaskEntity): Long

    @Update
    suspend fun update(task: TaskEntity)

    @Delete
    suspend fun delete(task: TaskEntity)

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getById(id: Long): TaskEntity?

    @Query("SELECT * FROM tasks WHERE project_id = :projectId ORDER BY deadline ASC")
    fun getByProjectFlow(projectId: Long): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE project_id = :projectId AND status = :status")
    fun getByProjectAndStatusFlow(projectId: Long, status: TaskStatus): Flow<List<TaskEntity>>

    // Tarefas atribuídas a um utilizador via user_task
    @Query("""
        SELECT t.* FROM tasks t
        INNER JOIN user_task ut ON t.id = ut.task_id
        WHERE ut.user_id = :userId AND ut.is_completed = 0
        ORDER BY t.deadline ASC
    """)
    fun getPendingForUserFlow(userId: Long): Flow<List<TaskEntity>>

    // Histórico: tarefas concluídas pelo utilizador
    @Query("""
        SELECT t.* FROM tasks t
        INNER JOIN user_task ut ON t.id = ut.task_id
        WHERE ut.user_id = :userId AND ut.is_completed = 1
        ORDER BY ut.updated_at DESC
    """)
    fun getCompletedForUserFlow(userId: Long): Flow<List<TaskEntity>>

    @Query("""
        SELECT t.* FROM tasks t
        WHERE t.title LIKE '%' || :query || '%'
        AND (:projectId IS NULL OR t.project_id = :projectId)
        AND (:status IS NULL OR t.status = :status)
        AND (:priority IS NULL OR t.priority = :priority)
    """)
    fun searchFlow(
        query: String,
        projectId: Long? = null,
        status: TaskStatus? = null,
        priority: TaskPriority? = null
    ): Flow<List<TaskEntity>>

    @Query("UPDATE tasks SET status = :status, updated_at = :updatedAt WHERE id = :id")
    suspend fun updateStatus(id: Long, status: TaskStatus, updatedAt: Long)
}
