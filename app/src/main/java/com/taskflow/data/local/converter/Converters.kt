package com.taskflow.data.local.converter

import androidx.room.TypeConverter
import com.taskflow.domain.util.AuditAction
import com.taskflow.domain.util.HttpMethod
import com.taskflow.domain.util.ProjectStatus
import com.taskflow.domain.util.TaskPriority
import com.taskflow.domain.util.TaskStatus
import com.taskflow.domain.util.UserRole

class Converters {

    // UserRole
    @TypeConverter fun fromUserRole(value: UserRole): String = value.name
    @TypeConverter fun toUserRole(value: String): UserRole = UserRole.valueOf(value)

    // ProjectStatus
    @TypeConverter fun fromProjectStatus(value: ProjectStatus): String = value.name
    @TypeConverter fun toProjectStatus(value: String): ProjectStatus = ProjectStatus.valueOf(value)

    // TaskStatus
    @TypeConverter fun fromTaskStatus(value: TaskStatus): String = value.name
    @TypeConverter fun toTaskStatus(value: String): TaskStatus = TaskStatus.valueOf(value)

    // TaskPriority
    @TypeConverter fun fromTaskPriority(value: TaskPriority): String = value.name
    @TypeConverter fun toTaskPriority(value: String): TaskPriority = TaskPriority.valueOf(value)

    // AuditAction
    @TypeConverter fun fromAuditAction(value: AuditAction): String = value.name
    @TypeConverter fun toAuditAction(value: String): AuditAction = AuditAction.valueOf(value)

    // HttpMethod
    @TypeConverter fun fromHttpMethod(value: HttpMethod): String = value.name
    @TypeConverter fun toHttpMethod(value: String): HttpMethod = HttpMethod.valueOf(value)
}
