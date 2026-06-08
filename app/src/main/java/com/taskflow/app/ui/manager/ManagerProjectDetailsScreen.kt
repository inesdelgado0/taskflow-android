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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
fun ManagerProjectDetailsScreen(nav: NavController) {
    val viewModel: TaskFlowDataViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsState()
    val project = state.projects.firstOrNull { it.id == state.selectedProjectId } ?: state.projects.firstOrNull()
    val projectTasks = state.tasks.filter { it.projectId == project?.id }
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
                FilterChip(selected = true, onClick = {}, label = { Text(stringResource(R.string.pending_tasks_metric)) })
                FilterChip(selected = false, onClick = {}, label = { Text(stringResource(R.string.completed_filter)) })
            }
            OutlinedButton(onClick = { nav.navigate(Routes.MANAGER_TASKS_LIST) }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.view_all_tasks))
            }
        }
        SectionCard(stringResource(R.string.project_team_count, state.users.take(3).size)) {
            state.users.take(3).forEach { user ->
                EvalLine(user.toDemoUser()) { nav.navigate(Routes.managerEvaluateUser(user.id)) }
            }
            if (state.users.isEmpty()) EmptyData()
            OutlinedButton(onClick = { nav.navigate(Routes.MANAGER_TEAM) }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.view_full_team))
            }
        }
    }
}
