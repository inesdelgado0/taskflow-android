package com.taskflow.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.taskflow.data.local.entity.SyncQueueEntity

@Dao
interface SyncQueueDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun enqueue(item: SyncQueueEntity): Long

    // Próximo item da fila (FIFO por created_at)
    @Query("SELECT * FROM sync_queue ORDER BY created_at ASC LIMIT 1")
    suspend fun peek(): SyncQueueEntity?

    // Todos os itens pendentes por ordem de inserção
    @Query("SELECT * FROM sync_queue ORDER BY created_at ASC")
    suspend fun getAll(): List<SyncQueueEntity>

    @Query("DELETE FROM sync_queue WHERE id = :id")
    suspend fun remove(id: Long)

    @Query("UPDATE sync_queue SET retry_count = retry_count + 1, last_error = :error WHERE id = :id")
    suspend fun incrementRetry(id: Long, error: String)

    @Query("SELECT COUNT(*) FROM sync_queue")
    suspend fun count(): Int
}
