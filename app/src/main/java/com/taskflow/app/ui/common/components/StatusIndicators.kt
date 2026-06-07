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
import com.taskflow.app.ui.common.util.isNearDeadline
import com.taskflow.app.ui.common.theme.Green
import com.taskflow.app.ui.common.theme.Muted
import com.taskflow.app.ui.common.theme.Orange
import com.taskflow.app.ui.common.theme.Red

@Composable
internal fun StatusPill(text: String, color: Color) {
    Box(Modifier.clip(RoundedCornerShape(50)).background(color.copy(alpha = 0.12f)).padding(horizontal = 8.dp, vertical = 3.dp)) {
        Text(text, color = color, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
internal fun SyncStatus(state: TaskFlowDataUiState) {
    val (label, color) = when {
        state.isRefreshing -> stringResource(R.string.sync_status_syncing) to Orange
        state.refreshError != null -> stringResource(R.string.sync_status_offline) to Red
        else -> stringResource(R.string.sync_status_synced) to Green
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = color, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
        state.refreshError?.let {
            Text(it, color = Red, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
internal fun NotificationStateComponent(state: TaskFlowDataUiState) {
    val nearDeadline = state.tasks.count { it.deadline.isNearDeadline() && it.status != TaskStatus.COMPLETED }
    val pendingSync = state.isRefreshing
    SectionCard("Notificações") {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Notifications, null, tint = if (nearDeadline > 0 || pendingSync) Orange else Green, modifier = Modifier.size(22.dp))
            Spacer(Modifier.size(8.dp))
            Column {
                Text(
                    if (nearDeadline > 0) "$nearDeadline tarefa(s) com prazo próximo"
                    else if (pendingSync) stringResource(R.string.sync_status_syncing)
                    else stringResource(R.string.sync_status_synced),
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    if (state.refreshError != null) stringResource(R.string.sync_status_offline)
                    else "Notificações de tarefas, prazos e sincronização ativas",
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
            Box(Modifier.size(7.dp).clip(CircleShape).background(if (it == index) Blue else Color(0xFFD2DAE5)))
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
        LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(6.dp), color = Blue, trackColor = com.taskflow.app.ui.common.theme.Border)
    }
}
