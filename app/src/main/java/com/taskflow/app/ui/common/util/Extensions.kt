package com.taskflow.app.ui.common.util

import android.content.Intent
import androidx.compose.ui.graphics.Color
import com.taskflow.app.data.export.StatisticsCsvFormatter
import com.taskflow.app.data.export.StatisticsFileExporter
import com.taskflow.app.domain.model.Evaluation
import com.taskflow.app.domain.model.Project
import com.taskflow.app.domain.model.StatisticRow
import com.taskflow.app.domain.model.StatisticsExportFormat
import com.taskflow.app.domain.model.StatisticsGrouping
import com.taskflow.app.domain.model.StatisticsSnapshot
import com.taskflow.app.domain.model.Task
import com.taskflow.app.domain.model.User
import com.taskflow.app.domain.util.ProjectStatus
import com.taskflow.app.domain.util.TaskPriority
import com.taskflow.app.domain.util.TaskStatus
import com.taskflow.app.domain.util.UserRole
import com.taskflow.app.ui.common.TaskFlowDataUiState
import com.taskflow.app.ui.common.theme.Blue
import com.taskflow.app.ui.common.theme.Green
import com.taskflow.app.ui.common.theme.Muted
import com.taskflow.app.ui.common.theme.Orange
import com.taskflow.app.ui.common.theme.Red

data class DemoUser(val name: String, val email: String, val role: String, val initial: String, val color: Color)

internal fun User.toDemoUser(): DemoUser {
    val primaryRole = roles.firstOrNull() ?: role
    val color = when (primaryRole.name) {
        "ADMIN" -> Blue
        "MANAGER" -> Green
        else -> Orange
    }

    return DemoUser(
        name = name,
        email = email,
        role = primaryRole.name,
        initial = name.initial(),
        color = color
    )
}

internal fun String?.initial(): String =
    this
        ?.trim()
        ?.firstOrNull()
        ?.uppercase()
        ?: ""

internal fun List<Task>.completionRate(): Int {
    if (isEmpty()) return 0
    return count { it.status == TaskStatus.COMPLETED } * 100 / size
}

internal fun List<User>.projectManagers(): List<User> =
    filter { user -> user.isActive && user.roles.any { it == UserRole.MANAGER } }

internal fun List<User>.managerName(managerId: Long?): String =
    firstOrNull { it.id == managerId }?.name
        ?: if (java.util.Locale.getDefault().language == "pt") "Sem gestor" else "No manager"

internal fun TaskFlowDataUiState.toStatisticsSnapshot(
    title: String,
    grouping: StatisticsGrouping
): StatisticsSnapshot {
    val now = System.currentTimeMillis()
    val rows: List<StatisticRow> = when (grouping) {
        StatisticsGrouping.BY_USER -> {
            if (users.isEmpty()) {
                listOf(tasks.toStatisticRow(title, now))
            } else {
                users.map { user ->
                    tasks.filter { it.createdBy == user.id }.toStatisticRow(user.name, now)
                }
            }
        }
        StatisticsGrouping.BY_PROJECT -> {
            if (projects.isEmpty()) {
                listOf(tasks.toStatisticRow(title, now))
            } else {
                projects.map { project ->
                    tasks.filter { it.projectId == project.id }.toStatisticRow(project.name, now)
                }
            }
        }
        StatisticsGrouping.BY_TASK -> {
            if (tasks.isEmpty()) {
                emptyList()
            } else {
                tasks.map { task ->
                    StatisticRow(
                        label = task.title,
                        totalTasks = 1,
                        completedTasks = if (task.status == TaskStatus.COMPLETED) 1 else 0,
                        pendingTasks = if (task.status != TaskStatus.COMPLETED && task.status != TaskStatus.CANCELLED) 1 else 0,
                        overdueTasks = if (
                            task.deadline != null &&
                            task.deadline < now &&
                            task.status != TaskStatus.COMPLETED &&
                            task.status != TaskStatus.CANCELLED
                        ) 1 else 0
                    )
                }
            }
        }
    }

    return StatisticsSnapshot(
        title = title,
        generatedAt = now,
        rows = rows
    )
}

internal fun List<Task>.toStatisticRow(label: String, now: Long): StatisticRow =
    StatisticRow(
        label = label,
        totalTasks = size,
        completedTasks = count { it.status == TaskStatus.COMPLETED },
        pendingTasks = count { it.status != TaskStatus.COMPLETED && it.status != TaskStatus.CANCELLED },
        overdueTasks = count { task ->
            task.deadline != null &&
                task.deadline < now &&
                task.status != TaskStatus.COMPLETED &&
                task.status != TaskStatus.CANCELLED
        }
    )

internal fun exportStatistics(
    context: android.content.Context,
    snapshot: StatisticsSnapshot,
    format: StatisticsExportFormat
) {
    val exporter = StatisticsFileExporter(context, StatisticsCsvFormatter())
    val file = exporter.export(snapshot, format)
    val intent = Intent.createChooser(exporter.shareIntent(file), snapshot.title)
    context.startActivity(intent)
}

internal fun List<Evaluation>.averageRating(): String {
    if (isEmpty()) return ""
    return "%.1f".format(map { it.rating }.average())
}

internal fun Long?.displayDate(): String {
    if (this == null || this == 0L) return ""
    val formatter = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
    return formatter.format(java.util.Date(this))
}

internal fun Long?.toDateInput(): String {
    if (this == null || this == 0L) return ""
    val formatter = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
    return formatter.format(java.util.Date(this))
}

internal fun String.toEpochMillisOrNull(): Long? {
    val value = trim()
    if (value.isBlank()) return null
    return runCatching {
        java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).apply {
            isLenient = false
        }.parse(value)?.time
    }.getOrNull()
}

internal fun Long?.isNearDeadline(): Boolean {
    if (this == null) return false
    val now = System.currentTimeMillis()
    val sevenDays = 7L * 24L * 60L * 60L * 1000L
    return this in now..(now + sevenDays)
}

internal fun Long.relativeTime(): String {
    val diff = (System.currentTimeMillis() - this).coerceAtLeast(0L)
    val minutes = diff / 60_000L
    val hours = minutes / 60L
    val days = hours / 24L
    return when {
        days > 0 -> "${days}d"
        hours > 0 -> "${hours}h"
        minutes > 0 -> "${minutes}min"
        else -> if (java.util.Locale.getDefault().language == "pt") "agora" else "now"
    }
}

internal fun ProjectStatus.color(): Color =
    when (this) {
        ProjectStatus.ACTIVE -> Green
        ProjectStatus.COMPLETED -> Color(0xFF9CA3AF)
        ProjectStatus.CANCELLED -> Red
    }

internal fun TaskPriority.color(): Color =
    when (this) {
        TaskPriority.LOW -> Green
        TaskPriority.MEDIUM -> Orange
        TaskPriority.HIGH -> Red
        TaskPriority.CRITICAL -> Color(0xFF7C3AED)
    }

internal fun ProjectStatus.label(): String {
    val isPortuguese = java.util.Locale.getDefault().language == "pt"
    return when (this) {
        ProjectStatus.ACTIVE -> if (isPortuguese) "Ativo" else "Active"
        ProjectStatus.COMPLETED -> if (isPortuguese) "Concluido" else "Completed"
        ProjectStatus.CANCELLED -> if (isPortuguese) "Cancelado" else "Cancelled"
    }
}

internal fun TaskPriority.label(): String {
    val isPortuguese = java.util.Locale.getDefault().language == "pt"
    return when (this) {
        TaskPriority.LOW -> if (isPortuguese) "Baixa" else "Low"
        TaskPriority.MEDIUM -> if (isPortuguese) "Media" else "Medium"
        TaskPriority.HIGH -> if (isPortuguese) "Alta" else "High"
        TaskPriority.CRITICAL -> if (isPortuguese) "Critica" else "Critical"
    }
}
