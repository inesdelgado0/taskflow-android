package com.taskflow.app.di

import com.taskflow.app.data.repository.AuditLogRepositoryImpl
import com.taskflow.app.data.repository.EvaluationRepositoryImpl
import com.taskflow.app.data.repository.ObservationRepositoryImpl
import com.taskflow.app.data.repository.ProjectRepositoryImpl
import com.taskflow.app.data.repository.SyncQueueRepositoryImpl
import com.taskflow.app.data.repository.TaskRepositoryImpl
import com.taskflow.app.domain.repository.AuditLogRepository
import com.taskflow.app.domain.repository.EvaluationRepository
import com.taskflow.app.domain.repository.ObservationRepository
import com.taskflow.app.domain.repository.ProjectRepository
import com.taskflow.app.domain.repository.SyncQueueRepository
import com.taskflow.app.domain.repository.TaskRepository
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

    @Binds
    @Singleton
    abstract fun bindProjectRepository(
        impl: ProjectRepositoryImpl
    ): ProjectRepository

    @Binds
    @Singleton
    abstract fun bindTaskRepository(
        impl: TaskRepositoryImpl
    ): TaskRepository

    @Binds
    @Singleton
    abstract fun bindObservationRepository(
        impl: ObservationRepositoryImpl
    ): ObservationRepository

    @Binds
    @Singleton
    abstract fun bindEvaluationRepository(
        impl: EvaluationRepositoryImpl
    ): EvaluationRepository
}
