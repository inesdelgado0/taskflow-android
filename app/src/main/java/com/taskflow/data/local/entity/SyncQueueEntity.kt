package com.taskflow.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.taskflow.domain.util.HttpMethod

// Sem ForeignKeys intencionalmente: os IDs remotos podem ainda não existir
@Entity(
    tableName = "sync_queue",
    indices = [Index(value = ["created_at"])]  // para processamento FIFO
)
data class SyncQueueEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "endpoint")
    val endpoint: String,

    @ColumnInfo(name = "http_method")
    val httpMethod: HttpMethod,

    @ColumnInfo(name = "payload")
    val payload: String? = null,  // corpo do pedido em JSON

    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    @ColumnInfo(name = "retry_count", defaultValue = "0")
    val retryCount: Int = 0,

    @ColumnInfo(name = "last_error")
    val lastError: String? = null
)
