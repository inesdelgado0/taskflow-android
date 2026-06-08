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
import androidx.compose.material3.DropdownMenuItem
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
import com.taskflow.app.ui.common.components.DropdownSelector
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
fun AddTeamScreen(nav: NavController) {
    val viewModel: TaskFlowDataViewModel = hiltViewModel()
    val state by taskFlowState()
    var query by rememberSaveable { mutableStateOf("") }
    var selectedProjectId by rememberSaveable(state.selectedProjectId, state.projects.size) {
        mutableStateOf(state.selectedProjectId ?: state.projects.firstOrNull()?.id)
    }
    val project = state.projects.firstOrNull { it.id == selectedProjectId }
    val assignableUsers = state.users.filter { user ->
        user.isActive && user.roles.any { it == UserRole.USER }
    }
    val assignedUserIds = state.userProjectAssignments
        .filter { it.projectId == project?.id }
        .map { it.userId }
        .toSet()
    val assignedUsers = assignableUsers.filter { it.id in assignedUserIds }
    val availableUsers = assignableUsers
        .filter { it.id !in assignedUserIds }
        .filter { user ->
            query.isBlank() ||
                user.name.contains(query, ignoreCase = true) ||
                user.username.contains(query, ignoreCase = true) ||
                user.email.contains(query, ignoreCase = true)
        }

    FormScreen(stringResource(R.string.add_to_team), { nav.popBackStack() }) {
        SyncStatus(state)
        SectionCard(stringResource(R.string.select_project)) {
            DropdownSelector(
                label = stringResource(R.string.select_project),
                selectedText = project?.name ?: stringResource(R.string.no_projects_synced)
            ) {
                state.projects.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item.name) },
                        onClick = {
                            selectedProjectId = item.id
                            viewModel.selectProject(item.id)
                        }
                    )
                }
            }
            Text(
                stringResource(R.string.assign_project_users_hint),
                color = Muted,
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall
            )
        }
        SectionCard(stringResource(R.string.current_project_team, assignedUsers.size)) {
            assignedUsers.forEach { user ->
                AssignmentUserRow(
                    user = user,
                    assigned = true,
                    onClick = { viewModel.removeUserFromProject(project, user) }
                )
            }
            if (assignedUsers.isEmpty()) EmptyData()
        }
        SectionCard(stringResource(R.string.available_users)) {
            SearchField(
                placeholder = stringResource(R.string.search_users_hint),
                value = query,
                onValueChange = { query = it }
            )
            availableUsers.forEach { user ->
                AssignmentUserRow(
                    user = user,
                    assigned = false,
                    onClick = { viewModel.assignUserToProject(project, user) }
                )
            }
            if (availableUsers.isEmpty()) EmptyData()
        }
    }
}

@Composable
private fun AssignmentUserRow(
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
                contentDescription = null,
                tint = actionColor,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
