package com.taskflow.di

import android.content.Context
import androidx.room.Room
import com.taskflow.data.local.dao.AuditLogDao
import com.taskflow.data.local.dao.EvaluationDao
import com.taskflow.data.local.dao.ObservationDao
import com.taskflow.data.local.dao.ProjectDao
import com.taskflow.data.local.dao.SyncQueueDao
import com.taskflow.data.local.dao.TaskDao
import com.taskflow.data.local.dao.UserDao
import com.taskflow.data.local.dao.UserProjectDao
import com.taskflow.data.local.dao.UserTaskDao
import com.taskflow.data.local.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration() // trocar por migrações reais antes da entrega
            .build()

    @Provides fun provideUserDao(db: AppDatabase): UserDao = db.userDao()
    @Provides fun provideProjectDao(db: AppDatabase): ProjectDao = db.projectDao()
    @Provides fun provideTaskDao(db: AppDatabase): TaskDao = db.taskDao()
    @Provides fun provideUserProjectDao(db: AppDatabase): UserProjectDao = db.userProjectDao()
    @Provides fun provideUserTaskDao(db: AppDatabase): UserTaskDao = db.userTaskDao()
    @Provides fun provideObservationDao(db: AppDatabase): ObservationDao = db.observationDao()
    @Provides fun provideEvaluationDao(db: AppDatabase): EvaluationDao = db.evaluationDao()
    @Provides fun provideAuditLogDao(db: AppDatabase): AuditLogDao = db.auditLogDao()
    @Provides fun provideSyncQueueDao(db: AppDatabase): SyncQueueDao = db.syncQueueDao()
}
