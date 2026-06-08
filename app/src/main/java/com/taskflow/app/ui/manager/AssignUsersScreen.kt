package com.taskflow.app.ui.manager

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.taskflow.app.R
import com.taskflow.app.ui.common.components.Assigned
import com.taskflow.app.ui.common.components.AvailableUser
import com.taskflow.app.ui.common.components.EmptyData
import com.taskflow.app.ui.common.components.FormScreen
import com.taskflow.app.ui.common.components.SectionCard
import com.taskflow.app.ui.common.components.taskFlowState
import com.taskflow.app.ui.common.theme.Blue
import com.taskflow.app.ui.common.util.toDemoUser
import com.taskflow.app.ui.common.util.initial

@Composable
fun AssignUsersScreen(nav: NavController) {
    val state by taskFlowState()
    val task = state.tasks.firstOrNull { it.id == state.selectedTaskId } ?: state.tasks.firstOrNull()
    FormScreen(stringResource(R.string.assign_users), { nav.popBackStack() }) {
        SectionCard(stringResource(R.string.task_title)) {
            androidx.compose.material3.Text(
                task?.title.orEmpty(),
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
            )
            androidx.compose.material3.Text(
                stringResource(R.string.project_prefix, state.projects.firstOrNull { it.id == task?.projectId }?.name.orEmpty()),
                color = com.taskflow.app.ui.common.theme.Muted
            )
        }
        SectionCard(stringResource(R.string.assigned_users)) {
            state.users.take(2).forEach { user ->
                Assigned(user.name, user.name.initial(), Blue)
            }
            if (state.users.isEmpty()) EmptyData()
        }
        SectionCard(stringResource(R.string.available_to_assign)) {
            state.users.drop(2).forEach { user -> AvailableUser(user.toDemoUser()) }
            if (state.users.drop(2).isEmpty()) EmptyData()
        }
    }
}
