package com.taskflow

import android.app.Application
import com.taskflow.sync.SyncManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class TaskFlowApp : Application() {

    @Inject lateinit var syncManager: SyncManager

    override fun onCreate() {
        super.onCreate()
        syncManager.startObserving()
    }
}