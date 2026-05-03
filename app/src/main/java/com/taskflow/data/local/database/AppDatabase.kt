package com.taskflow.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.taskflow.data.local.converter.Converters
import com.taskflow.data.local.dao.AuditLogDao
import com.taskflow.data.local.dao.EvaluationDao
import com.taskflow.data.local.dao.ObservationDao
import com.taskflow.data.local.dao.ProjectDao
import com.taskflow.data.local.dao.SyncQueueDao
import com.taskflow.data.local.dao.TaskDao
import com.taskflow.data.local.dao.UserDao
import com.taskflow.data.local.dao.UserProjectDao
import com.taskflow.data.local.dao.UserTaskDao
import com.taskflow.data.local.entity.AuditLogEntity
import com.taskflow.data.local.entity.EvaluationEntity
import com.taskflow.data.local.entity.ObservationEntity
import com.taskflow.data.local.entity.ProjectEntity
import com.taskflow.data.local.entity.SyncQueueEntity
import com.taskflow.data.local.entity.TaskEntity
import com.taskflow.data.local.entity.UserEntity
import com.taskflow.data.local.entity.UserProjectEntity
import com.taskflow.data.local.entity.UserTaskEntity

@Database(
    entities = [
        UserEntity::class,
        ProjectEntity::class,
        TaskEntity::class,
        UserProjectEntity::class,
        UserTaskEntity::class,
        ObservationEntity::class,
        EvaluationEntity::class,
        AuditLogEntity::class,
        SyncQueueEntity::class
    ],
    version = 1,
    exportSchema = true  // gera JSON do schema em app/schemas/ — útil para migrações futuras
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun projectDao(): ProjectDao
    abstract fun taskDao(): TaskDao
    abstract fun userProjectDao(): UserProjectDao
    abstract fun userTaskDao(): UserTaskDao
    abstract fun observationDao(): ObservationDao
    abstract fun evaluationDao(): EvaluationDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun syncQueueDao(): SyncQueueDao

    companion object {
        const val DATABASE_NAME = "taskflow.db"
    }
}
