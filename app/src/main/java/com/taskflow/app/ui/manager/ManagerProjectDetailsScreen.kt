package com.taskflow.app.ui.manager

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.taskflow.app.R
import com.taskflow.app.domain.util.TaskStatus
import com.taskflow.app.ui.common.components.EvalLine
import com.taskflow.app.ui.common.components.EmptyData
import com.taskflow.app.ui.common.components.FormScreen
import com.taskflow.app.ui.common.components.ProgressLine
import com.taskflow.app.ui.common.components.SectionCard
import com.taskflow.app.ui.common.components.StatusPill
import com.taskflow.app.ui.common.components.SyncStatus
import com.taskflow.app.ui.common.TaskFlowDataViewModel
import com.taskflow.app.ui.common.components.TwoMetrics
import com.taskflow.app.ui.common.theme.Green
import com.taskflow.app.ui.common.theme.Muted
import com.taskflow.app.ui.common.util.color
import com.taskflow.app.ui.common.util.completionRate
import com.taskflow.app.ui.common.util.displayDate
import com.taskflow.app.ui.common.util.toDemoUser
import com.taskflow.app.ui.navigation.Routes

@Composable
fun ManagerProjectDetailsScreen(nav: NavController, projectId: Long? = null) {
    val viewModel: TaskFlowDataViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsState()
    val selectedProjectId = projectId ?: state.selectedProjectId
    val project = selectedProjectId?.let { id -> state.projects.firstOrNull { it.id == id } }
    val projectTasks = state.tasks.filter { it.projectId == project?.id }
    val projectUserIds = state.userProjectAssignments
        .filter { it.projectId == project?.id }
        .map { it.userId }
        .toSet()
    val projectUsers = state.users.filter { it.id in projectUserIds }
    var showCompletedTasks by rememberSaveable(project?.id) { mutableStateOf(false) }
    val pendingTasksCount = projectTasks.count { task ->
        task.status != TaskStatus.COMPLETED && task.status != TaskStatus.CANCELLED
    }
    val completedTasksCount = projectTasks.count { task ->
        task.status == TaskStatus.COMPLETED
    }

    LaunchedEffect(projectId) {
        if (projectId != null) {
            viewModel.selectProject(projectId)
        }
    }

    FormScreen(stringResource(R.string.project_details), { nav.popBackStack() }) {
        if (project == null) {
            EmptyData()
            return@FormScreen
        }
        SyncStatus(state)
        SectionCard(project.name) {
            StatusPill(project.status.name, project.status.color())
            Text(stringResource(R.string.deadline_prefix, project.endDate.displayDate()), color = Muted)
            Text(project.description.orEmpty(), color = Muted)
            TwoMetrics(stringResource(R.string.tasks_title), projectTasks.size.toString(), stringResource(R.string.dashboard_completed), projectTasks.count { it.status == TaskStatus.COMPLETED }.toString())
            val rate = projectTasks.completionRate()
            ProgressLine(stringResource(R.string.progress_label), "$rate%", rate / 100f)
            Button(onClick = { viewModel.completeProject(project) }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(Green), shape = RoundedCornerShape(8.dp)) {
                Text(stringResource(R.string.complete_project))
            }
        }
        SectionCard(stringResource(R.string.tasks_title)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = !showCompletedTasks,
                    onClick = { showCompletedTasks = false },
                    label = { Text("${stringResource(R.string.pending_filter)} ($pendingTasksCount)") }
                )
                FilterChip(
                    selected = showCompletedTasks,
                    onClick = { showCompletedTasks = true },
                    label = { Text("${stringResource(R.string.completed_filter)} ($completedTasksCount)") }
                )
            }
            OutlinedButton(
                onClick = {
                    nav.navigate(Routes.managerProjectTasks(project.id, showCompletedTasks))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.view_all_tasks))
            }
        }
        SectionCard(stringResource(R.string.project_team_count, projectUsers.size)) {
            projectUsers.take(5).forEach { user ->
                EvalLine(user.toDemoUser()) { nav.navigate(Routes.managerEvaluateUser(user.id)) }
            }
            if (projectUsers.isEmpty()) EmptyData()
            OutlinedButton(onClick = { nav.navigate(Routes.MANAGER_ADD_TEAM) }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.add_to_team))
            }
        }
    }
}
