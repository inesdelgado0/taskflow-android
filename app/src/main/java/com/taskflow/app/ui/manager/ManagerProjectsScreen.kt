package com.taskflow.app.ui.manager

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.taskflow.app.R
import com.taskflow.app.ui.common.components.ListScreen
import com.taskflow.app.ui.common.components.ProjectSummary
import com.taskflow.app.ui.common.components.SyncStatus
import com.taskflow.app.ui.common.TaskFlowDataViewModel
import com.taskflow.app.ui.common.util.completionRate
import com.taskflow.app.ui.navigation.Routes

@Composable
fun ManagerProjectsScreen(nav: NavController) {
    val viewModel: TaskFlowDataViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsState()
    ListScreen(stringResource(R.string.dashboard_my_projects), null, { nav.popBackStack() }, {}) {
        SyncStatus(state)
        state.projects.forEach { project ->
            ProjectSummary(project, state.tasks.filter { it.projectId == project.id }) {
                viewModel.selectProject(project.id)
                nav.navigate(Routes.MANAGER_PROJECT_DETAILS)
            }
        }
    }
}
