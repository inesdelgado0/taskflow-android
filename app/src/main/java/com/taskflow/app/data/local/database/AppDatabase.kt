package com.taskflow.app.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.taskflow.app.data.local.converter.Converters
import com.taskflow.app.data.local.dao.AuditLogDao
import com.taskflow.app.data.local.dao.EvaluationDao
import com.taskflow.app.data.local.dao.ObservationDao
import com.taskflow.app.data.local.dao.ProjectDao
import com.taskflow.app.data.local.dao.SyncQueueDao
import com.taskflow.app.data.local.dao.TaskDao
import com.taskflow.app.data.local.dao.UserDao
import com.taskflow.app.data.local.dao.UserProjectDao
import com.taskflow.app.data.local.dao.UserTaskDao
import com.taskflow.app.data.local.entity.AuditLogEntity
import com.taskflow.app.data.local.entity.EvaluationEntity
import com.taskflow.app.data.local.entity.ObservationEntity
import com.taskflow.app.data.local.entity.ProjectEntity
import com.taskflow.app.data.local.entity.RoleEntity
import com.taskflow.app.data.local.entity.SyncQueueEntity
import com.taskflow.app.data.local.entity.TaskEntity
import com.taskflow.app.data.local.entity.UserEntity
import com.taskflow.app.data.local.entity.UserProjectEntity
import com.taskflow.app.data.local.entity.UserRoleEntity
import com.taskflow.app.data.local.entity.UserTaskEntity

@Database(
    entities = [
        UserEntity::class,
        RoleEntity::class,
        UserRoleEntity::class,
        ProjectEntity::class,
        TaskEntity::class,
        UserProjectEntity::class,
        UserTaskEntity::class,
        ObservationEntity::class,
        EvaluationEntity::class,
        AuditLogEntity::class,
        SyncQueueEntity::class
    ],
    version = 2,
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

    /**
     * Executa [block] com as foreign keys desativadas.
     * Útil durante sync em bulk para evitar SQLITE_CONSTRAINT_TRIGGER
     * quando os dados remotos contêm referências a registos que ainda não foram inseridos localmente.
     */
    suspend fun <T> withForeignKeysDisabled(block: suspend () -> T): T {
        val db: SupportSQLiteDatabase = openHelper.writableDatabase
        db.execSQL("PRAGMA foreign_keys = OFF")
        return try {
            block()
        } finally {
            db.execSQL("PRAGMA foreign_keys = ON")
        }
    }

    companion object {
        const val DATABASE_NAME = "taskflow.db"
    }
}