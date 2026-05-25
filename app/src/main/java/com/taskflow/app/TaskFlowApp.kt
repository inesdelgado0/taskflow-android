package com.taskflow.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.taskflow.app.notification.TaskFlowNotifier
import com.taskflow.app.sync.SyncManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class TaskFlowApp : Application(), Configuration.Provider {

    @Inject lateinit var syncManager: SyncManager
    @Inject lateinit var notifier: TaskFlowNotifier
    @Inject lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        notifier.createNotificationChannels()
        syncManager.startObserving()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
