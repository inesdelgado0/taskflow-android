package com.taskflow.app.di

import com.taskflow.app.data.repository.AuditLogRepositoryImpl
import com.taskflow.app.data.repository.SyncQueueRepositoryImpl
import com.taskflow.app.domain.repository.AuditLogRepository
import com.taskflow.app.domain.repository.SyncQueueRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuditLogRepository(
        impl: AuditLogRepositoryImpl
    ): AuditLogRepository

    @Binds
    @Singleton
    abstract fun bindSyncQueueRepository(
        impl: SyncQueueRepositoryImpl
    ): SyncQueueRepository
}
