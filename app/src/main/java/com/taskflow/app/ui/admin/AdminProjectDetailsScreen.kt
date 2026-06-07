package com.taskflow.app.ui.admin

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.taskflow.app.R
import com.taskflow.app.domain.util.UserRole
import com.taskflow.app.ui.common.components.CompactUser
import com.taskflow.app.ui.common.components.EmptyData
import com.taskflow.app.ui.common.components.FormScreen
import com.taskflow.app.ui.common.components.ProjectDetailsCard
import com.taskflow.app.ui.common.components.SectionCard
import com.taskflow.app.ui.common.TaskFlowDataViewModel
import com.taskflow.app.ui.common.theme.Red
import com.taskflow.app.ui.common.util.managerName
import com.taskflow.app.ui.common.util.toDemoUser
import com.taskflow.app.ui.navigation.Routes
import androidx.compose.ui.res.stringResource

@Composable
fun AdminProjectDetailsScreen(nav: NavController) {
    val viewModel: TaskFlowDataViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsState()
    val project = state.projects.firstOrNull { it.id == state.selectedProjectId } ?: state.projects.firstOrNull()
    val projectTasks = state.tasks.filter { it.projectId == project?.id }
    val managerName = state.users.managerName(project?.managerId)
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    if (showDeleteDialog && project != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.dialog_delete_title)) },
            text = { Text(stringResource(R.string.dialog_delete_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteProject(project) { nav.popBackStack() }
                    }
                ) {
                    Text(stringResource(R.string.btn_delete), color = Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }

    FormScreen(stringResource(R.string.project_details), onBack = { nav.popBackStack() }) {
        if (project == null) {
            EmptyData()
            return@FormScreen
        }
        ProjectDetailsCard(
            project = project,
            managerName = managerName,
            tasks = projectTasks,
            onEdit = { nav.navigate(Routes.ADMIN_PROJECT_EDIT) },
            onDelete = { showDeleteDialog = true }
        )
        val teamPreview = state.users
            .filter { it.id == project.managerId || it.role == UserRole.USER }
            .take(3)
        SectionCard(stringResource(R.string.project_team_count, teamPreview.size)) {
            teamPreview.forEach { user ->
                val demo = user.toDemoUser()
                CompactUser(demo.name, demo.initial, demo.color)
            }
            if (teamPreview.isEmpty()) EmptyData()
        }
    }
}
