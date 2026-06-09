package com.taskflow.app.ui.manager

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.taskflow.app.R
import com.taskflow.app.domain.util.TaskPriority
import com.taskflow.app.domain.util.TaskStatus
import com.taskflow.app.ui.common.components.EmptyData
import com.taskflow.app.ui.common.components.ListScreen
import com.taskflow.app.ui.common.components.ManagerTaskCard
import com.taskflow.app.ui.common.components.ProjectFilterDropdown
import com.taskflow.app.ui.common.components.SearchField
import com.taskflow.app.ui.common.TaskFlowDataViewModel
import com.taskflow.app.ui.common.util.label
import com.taskflow.app.ui.common.util.projectManagers
import com.taskflow.app.ui.navigation.Routes

@Composable
fun ManagerTasksListScreen(nav: NavController, userId: Long? = null) {
    val viewModel: TaskFlowDataViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsState()
    var query by rememberSaveable { mutableStateOf("") }
    var selectedPriority by rememberSaveable { mutableStateOf<TaskPriority?>(null) }
    var selectedManagerId by rememberSaveable { mutableStateOf<Long?>(null) }
    var showCompleted by rememberSaveable { mutableStateOf(false) }
    val managers = state.users.projectManagers()
    val filteredTasks = state.tasks
        .filter { task ->
            query.isBlank() ||
                task.title.contains(query, ignoreCase = true) ||
                task.description.orEmpty().contains(query, ignoreCase = true)
        }
        .filter { task -> selectedPriority == null || task.priority == selectedPriority }
        .filter { task ->
            selectedManagerId == null ||
                state.projects.firstOrNull { it.id == task.projectId }?.managerId == selectedManagerId
        }
        .filter { task ->
            userId == null || state.userTaskAssignments.any { assignment ->
                assignment.userId == userId && assignment.taskId == task.id
            }
        }
        .filter { task ->
            if (showCompleted) {
                task.status == TaskStatus.COMPLETED
            } else {
                task.status != TaskStatus.COMPLETED && task.status != TaskStatus.CANCELLED
            }
        }

    ListScreen(stringResource(R.string.task_management), stringResource(R.string.new_action), { nav.popBackStack() }, { nav.navigate(Routes.MANAGER_TASK_CREATE) }) {
        SearchField(
            placeholder = stringResource(R.string.search_tasks_hint),
            value = query,
            onValueChange = { query = it }
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            ProjectFilterDropdown(
                selectedText = selectedPriority?.label() ?: stringResource(R.string.all_priorities),
                modifier = Modifier.weight(1f)
            ) {
                DropdownMenuItem(text = { Text(stringResource(R.string.all_priorities)) }, onClick = { selectedPriority = null })
                TaskPriority.entries.forEach { priority ->
                    DropdownMenuItem(text = { Text(priority.label()) }, onClick = { selectedPriority = priority })
                }
            }
            ProjectFilterDropdown(
                selectedText = managers.firstOrNull { it.id == selectedManagerId }?.name ?: stringResource(R.string.all_managers),
                modifier = Modifier.weight(1f)
            ) {
                DropdownMenuItem(text = { Text(stringResource(R.string.all_managers)) }, onClick = { selectedManagerId = null })
                managers.forEach { manager ->
                    DropdownMenuItem(text = { Text(manager.name) }, onClick = { selectedManagerId = manager.id })
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = !showCompleted,
                onClick = { showCompleted = false },
                label = { Text(stringResource(R.string.pending_filter)) }
            )
            FilterChip(
                selected = showCompleted,
                onClick = { showCompleted = true },
                label = { Text(stringResource(R.string.completed_filter)) }
            )
        }
        filteredTasks.forEach { task ->
            ManagerTaskCard(task, state.projects, {
                nav.navigate(Routes.managerAssignUsers(task.id))
            }, {
                viewModel.selectTask(task.id)
                nav.navigate(Routes.MANAGER_TASK_EDIT)
            })
        }
        if (filteredTasks.isEmpty()) EmptyData()
    }
}
