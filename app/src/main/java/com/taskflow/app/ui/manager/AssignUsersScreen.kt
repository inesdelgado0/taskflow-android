package com.taskflow.app.ui.manager

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.taskflow.app.R
import com.taskflow.app.domain.model.User
import com.taskflow.app.domain.util.UserRole
import com.taskflow.app.ui.common.TaskFlowDataViewModel
import com.taskflow.app.ui.common.components.EmptyData
import com.taskflow.app.ui.common.components.FormScreen
import com.taskflow.app.ui.common.components.SearchField
import com.taskflow.app.ui.common.components.SectionCard
import com.taskflow.app.ui.common.components.SyncStatus
import com.taskflow.app.ui.common.components.taskFlowState
import com.taskflow.app.ui.common.theme.Muted
import com.taskflow.app.ui.common.theme.Soft
import com.taskflow.app.ui.common.util.initial

@Composable
fun AssignUsersScreen(nav: NavController, taskId: Long?) {
    val viewModel: TaskFlowDataViewModel = hiltViewModel()
    val state by taskFlowState()
    var query by rememberSaveable { mutableStateOf("") }
    val task = taskId?.let { id -> state.tasks.firstOrNull { it.id == id } }
    val project = state.projects.firstOrNull { it.id == task?.projectId }
    val projectUserIds = state.userProjectAssignments
        .filter { it.projectId == project?.id }
        .map { it.userId }
        .toSet()
    val assignedUserIds = state.userTaskAssignments
        .filter { it.taskId == task?.id }
        .map { it.userId }
        .toSet()
    val assignableUsers = state.users.filter { user ->
        user.isActive &&
            user.roles.any { it == UserRole.USER } &&
            (projectUserIds.isEmpty() || user.id in projectUserIds)
    }
    val assignedUsers = assignableUsers.filter { it.id in assignedUserIds }
    val availableUsers = assignableUsers
        .filter { it.id !in assignedUserIds }
        .filter { user ->
            query.isBlank() ||
                user.name.contains(query, ignoreCase = true) ||
                user.username.contains(query, ignoreCase = true) ||
                user.email.contains(query, ignoreCase = true)
        }

    FormScreen(stringResource(R.string.assign_users), { nav.popBackStack() }) {
        SyncStatus(state)
        SectionCard(stringResource(R.string.task_title)) {
            if (task == null) {
                EmptyData()
            } else {
                Text(task.title, fontWeight = FontWeight.SemiBold)
                Text(
                    stringResource(R.string.project_prefix, project?.name.orEmpty()),
                    color = Muted
                )
            }
        }
        SectionCard(stringResource(R.string.assigned_users)) {
            assignedUsers.forEach { user ->
                TaskAssignmentUserRow(
                    user = user,
                    assigned = true,
                    onClick = { viewModel.removeUserFromTask(task, user) }
                )
            }
            if (assignedUsers.isEmpty()) EmptyData()
        }
        SectionCard(stringResource(R.string.available_to_assign)) {
            SearchField(
                placeholder = stringResource(R.string.search_users_hint),
                value = query,
                onValueChange = { query = it }
            )
            availableUsers.forEach { user ->
                TaskAssignmentUserRow(
                    user = user,
                    assigned = false,
                    onClick = { viewModel.assignUserToTask(task, user) }
                )
            }
            if (availableUsers.isEmpty()) EmptyData()
        }
    }
}

@Composable
private fun TaskAssignmentUserRow(
    user: User,
    assigned: Boolean,
    onClick: () -> Unit
) {
    val rowColor = if (assigned) Color(0xFFEAF3FF) else Soft
    val borderColor = if (assigned) Color(0xFFBBD7FF) else Color.Transparent
    val avatarColor = if (assigned) Color(0xFF3B82F6) else Color(0xFF9AA5B1)
    val actionColor = if (assigned) Color(0xFFFF3B30) else Color(0xFF2F7DF6)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(rowColor)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(modifier = Modifier.weight(1f), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(avatarColor),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text(user.name.initial(), color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text(user.name, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                Text(stringResource(R.string.dashboard_user), color = Muted, style = MaterialTheme.typography.labelSmall)
            }
        }
        Spacer(Modifier.width(8.dp))
        IconButton(onClick = onClick, modifier = Modifier.size(32.dp)) {
            Icon(
                imageVector = if (assigned) Icons.Default.Close else Icons.Default.Add,
                contentDescription = stringResource(if (assigned) R.string.cd_remove_user else R.string.cd_add_user),
                tint = actionColor,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
