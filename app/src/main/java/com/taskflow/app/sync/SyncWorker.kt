package com.taskflow.app.sync

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.taskflow.app.data.remote.TokenManager
import com.taskflow.app.domain.repository.SyncQueueRepository
import com.taskflow.app.domain.util.HttpMethod
import com.taskflow.app.notification.TaskFlowNotifier
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit


private const val TAG            = "SyncWorker"
private const val MAX_RETRIES    = 3
private const val RETRY_DELAY_MS = 5_000L

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncQueueRepository: SyncQueueRepository,
    private val tokenManager: TokenManager,
    private val okHttpClient: OkHttpClient,
    private val notifier: TaskFlowNotifier
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.d(TAG, "SyncWorker iniciado")

        val token = tokenManager.getAccessToken()
        if (token == null) {
            Log.w(TAG, "Sem token — sync cancelado")
            return@withContext Result.failure()
        }

        val items = syncQueueRepository.getAll()
        if (items.isEmpty()) {
            Log.d(TAG, "Fila vazia — nada a sincronizar")
            return@withContext Result.success()
        }

        Log.d(TAG, "${items.size} item(s) na fila")

        var hasFailures = false
        var syncedCount = 0

        for (item in items) {
            if (item.retryCount >= MAX_RETRIES) {
                Log.w(TAG, "Item ${item.id} excedeu MAX_RETRIES — a remover")
                syncQueueRepository.remove(item.id)
                continue
            }

            try {
                val request = buildRequest(item.endpoint, item.httpMethod, item.payload, token)
                val response = okHttpClient.newCall(request).execute()

                if (response.isSuccessful) {
                    Log.d(TAG, "✅ Sincronizado: ${item.httpMethod} ${item.endpoint}")
                    syncQueueRepository.remove(item.id)
                    syncedCount++
                } else {
                    val error = "HTTP ${response.code}"
                    Log.w(TAG, "❌ Falhou (${error}): ${item.endpoint}")
                    syncQueueRepository.incrementRetry(item.id, error)
                    hasFailures = true
                }
                response.close()

            } catch (e: Exception) {
                val error = e.message ?: "Erro desconhecido"
                Log.e(TAG, "❌ Exceção ao sincronizar ${item.endpoint}: $error")
                syncQueueRepository.incrementRetry(item.id, error)
                hasFailures = true
            }
        }

        if (hasFailures) {
            Log.d(TAG, "Sync concluído com falhas — reagendar")
            notifier.showSyncFailed()
            Result.retry()
        } else {
            Log.d(TAG, "Sync concluído com sucesso")
            notifier.showSyncCompleted(syncedCount)
            Result.success()
        }
    }

    private fun buildRequest(
        endpoint: String,
        httpMethod: HttpMethod,
        payload: String?,
        token: String
    ): Request {
        val mediaType = "application/json".toMediaType()
        val body = payload?.toRequestBody(mediaType)

        return Request.Builder()
            .url(endpoint)
            .header("Authorization", "Bearer $token")
            .header("Content-Type", "application/json")
            .apply {
                when (httpMethod) {
                    HttpMethod.GET    -> get()
                    HttpMethod.POST   -> post(body ?: "{}".toRequestBody(mediaType))
                    HttpMethod.PUT    -> put(body ?: "{}".toRequestBody(mediaType))
                    HttpMethod.DELETE -> delete(body)
                }
            }
            .build()
    }

    companion object {
        const val WORK_NAME = "SyncWorker"

        fun buildOneTimeRequest(): OneTimeWorkRequest =
            OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    RETRY_DELAY_MS,
                    TimeUnit.MILLISECONDS
                )
                .build()

        fun buildPeriodicRequest(): PeriodicWorkRequest =
            PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
    }
}
