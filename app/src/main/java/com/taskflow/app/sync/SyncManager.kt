package com.taskflow.app.sync

import android.content.Context
import android.util.Log
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import com.taskflow.app.util.ConnectivityObserver
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class SyncManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val connectivityObserver: ConnectivityObserver
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val workManager = WorkManager.getInstance(context)

    fun startObserving() {
        connectivityObserver.connectionFlow
            .onEach { isConnected ->
                if (isConnected) {
                    Log.d("SyncManager", "Ligação detectada — a iniciar sync")
                    triggerSync()
                }
            }
            .launchIn(scope)
    }

    fun triggerSync() {
        workManager.enqueueUniqueWork(
            SyncWorker.WORK_NAME,
            ExistingWorkPolicy.KEEP,
            SyncWorker.buildOneTimeRequest()
        )
    }
}
