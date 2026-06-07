package com.taskflow.app.ui.manager

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.taskflow.app.R
import com.taskflow.app.ui.common.components.EmptyData
import com.taskflow.app.ui.common.components.ListScreen
import com.taskflow.app.ui.common.components.ManagerTaskCard
import com.taskflow.app.ui.common.components.SearchField
import com.taskflow.app.ui.common.components.SyncStatus
import com.taskflow.app.ui.common.TaskFlowDataViewModel
import com.taskflow.app.ui.navigation.Routes

@Composable
fun ManagerTasksListScreen(nav: NavController) {
    val viewModel: TaskFlowDataViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsState()
    ListScreen(stringResource(R.string.task_management), stringResource(R.string.new_action), { nav.popBackStack() }, { nav.navigate(Routes.MANAGER_TASK_CREATE) }) {
        SyncStatus(state)
        SearchField(stringResource(R.string.search_tasks_hint))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = true, onClick = {}, label = { Text(stringResource(R.string.pending_tasks_metric)) })
            FilterChip(selected = false, onClick = {}, label = { Text(stringResource(R.string.completed_filter)) })
        }
        state.tasks.forEach { task ->
            ManagerTaskCard(task, state.projects, {
                viewModel.selectTask(task.id)
                nav.navigate(Routes.MANAGER_ASSIGN_USERS)
            }, {
                viewModel.selectTask(task.id)
                nav.navigate(Routes.MANAGER_TASK_EDIT)
            })
        }
        if (state.tasks.isEmpty()) EmptyData()
    }
}
