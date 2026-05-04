package com.taskflow.domain.util

enum class UserRole {
    ADMIN, MANAGER, USER
}

enum class ProjectStatus {
    ACTIVE, COMPLETED, CANCELLED
}

enum class TaskStatus {
    PENDING, IN_PROGRESS, COMPLETED, CANCELLED
}

enum class TaskPriority {
    LOW, MEDIUM, HIGH, CRITICAL
}

enum class AuditAction {
    LOGIN, LOGOUT, CREATE, UPDATE, DELETE, SYNC
}

enum class HttpMethod {
    GET, POST, PUT, DELETE
}
