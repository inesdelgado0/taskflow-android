package com.taskflow.app.ui.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.taskflow.app.R
import com.taskflow.app.domain.util.TaskStatus
import com.taskflow.app.ui.common.TaskFlowDataUiState
import com.taskflow.app.ui.common.theme.Blue
import com.taskflow.app.ui.common.theme.Border
import com.taskflow.app.ui.common.theme.Green
import com.taskflow.app.ui.common.theme.Muted
import com.taskflow.app.ui.common.theme.Orange
import com.taskflow.app.ui.common.theme.Red
import com.taskflow.app.ui.common.util.isNearDeadline

internal data class InAppNotification(
    val title: String,
    val message: String,
    val taskId: Long? = null,
    val isImportant: Boolean = false
)

internal fun TaskFlowDataUiState.inAppNotifications(): List<InAppNotification> {
    val now = System.currentTimeMillis()
    val currentUserTaskIds = currentUser?.id?.let { userId ->
        userTaskAssignments
            .filter { assignment -> assignment.userId == userId }
            .map { assignment -> assignment.taskId }
            .toSet()
    }
    val visibleTasks = if (currentUser != null && currentUserTaskIds != null) {
        tasks.filter { task -> task.id in currentUserTaskIds }
    } else {
        tasks
    }
    val taskNotifications = visibleTasks
        .filter { task -> task.status != TaskStatus.COMPLETED && task.status != TaskStatus.CANCELLED }
        .flatMap { task ->
            val notifications = mutableListOf<InAppNotification>()
            if (task.deadline != null && task.deadline < now) {
                notifications += InAppNotification(
                    title = "Tarefa atrasada",
                    message = task.title,
                    taskId = task.id,
                    isImportant = true
                )
            } else if (task.deadline.isNearDeadline()) {
                notifications += InAppNotification(
                    title = "Prazo proximo",
                    message = task.title,
                    taskId = task.id,
                    isImportant = true
                )
            }
            if (task.status == TaskStatus.PENDING) {
                notifications += InAppNotification(
                    title = "Tarefa pendente",
                    message = task.title,
                    taskId = task.id
                )
            }
            notifications
        }

    val syncNotifications = buildList {
        if (isRefreshing) {
            add(
                InAppNotification(
                    title = "Sincronizacao em curso",
                    message = "Os dados estao a ser atualizados."
                )
            )
        }
        if (refreshError != null) {
            add(
                InAppNotification(
                    title = "Sincronizacao incompleta",
                    message = refreshError,
                    isImportant = true
                )
            )
        }
    }

    return (syncNotifications + taskNotifications)
        .distinctBy { "${it.title}:${it.taskId}:${it.message}" }
        .sortedByDescending { it.isImportant }
        .take(8)
}

@Composable
internal fun StatusPill(text: String, color: Color) {
    Box(
        Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(text, color = color, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
internal fun SyncStatus(state: TaskFlowDataUiState) {
    val errorText = state.refreshErrorRes?.let { stringResource(it) } ?: state.refreshError
    val (label, color) = when {
        state.isRefreshing -> stringResource(R.string.sync_status_syncing) to Orange
        errorText != null -> stringResource(R.string.sync_status_offline) to Red
        else -> stringResource(R.string.sync_status_synced) to Green
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = color, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
        errorText?.let {
            Text(it, color = Red, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
internal fun NotificationStateComponent(state: TaskFlowDataUiState) {
    val notifications = state.inAppNotifications()
    val importantCount = notifications.count { it.isImportant }
    val pendingSync = state.isRefreshing

    SectionCard("Notificacoes") {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.Notifications,
                contentDescription = null,
                tint = if (importantCount > 0 || pendingSync) Orange else Green,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.size(8.dp))
            Column {
                Text(
                    if (importantCount > 0) "$importantCount alerta(s) importante(s)"
                    else if (pendingSync) stringResource(R.string.sync_status_syncing)
                    else stringResource(R.string.sync_status_synced),
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    if (state.refreshError != null || state.refreshErrorRes != null) stringResource(R.string.sync_status_offline)
                    else "Notificacoes de tarefas, prazos e sincronizacao ativas",
                    color = Muted,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
internal fun Dots(index: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        repeat(3) {
            Box(
                Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(if (it == index) Blue else Color(0xFFD2DAE5))
            )
        }
    }
}

@Composable
internal fun ProgressLine(label: String, value: String, progress: Float) {
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = Muted, style = MaterialTheme.typography.bodySmall)
            Text(value, color = Muted, style = MaterialTheme.typography.bodySmall)
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = Blue,
            trackColor = Border
        )
    }
}
