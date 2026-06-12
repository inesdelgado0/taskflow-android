package com.taskflow.app.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenuItem
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
import com.taskflow.app.domain.util.ProjectStatus
import com.taskflow.app.ui.common.components.EmptyData
import com.taskflow.app.ui.common.components.ProjectFilterDropdown
import com.taskflow.app.ui.common.components.ProjectListItem
import com.taskflow.app.ui.common.components.SearchField
import com.taskflow.app.ui.common.components.SyncStatus
import com.taskflow.app.ui.common.TaskFlowDataViewModel
import com.taskflow.app.ui.common.components.TopBar
import com.taskflow.app.ui.common.theme.Page
import com.taskflow.app.ui.common.util.color
import com.taskflow.app.ui.common.util.label
import com.taskflow.app.ui.common.util.managerName
import com.taskflow.app.ui.common.util.projectManagers
import com.taskflow.app.ui.navigation.Routes

@Composable
fun AdminProjectsScreen(nav: NavController) {
    val viewModel: TaskFlowDataViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsState()
    var query by rememberSaveable { mutableStateOf("") }
    var selectedStatus by rememberSaveable { mutableStateOf<ProjectStatus?>(null) }
    var selectedManagerId by rememberSaveable { mutableStateOf<Long?>(null) }
    val managers = state.users.projectManagers()
    val filteredProjects = state.projects
        .filter { project -> query.isBlank() || project.name.contains(query, ignoreCase = true) }
        .filter { project -> selectedStatus == null || project.status == selectedStatus }
        .filter { project -> selectedManagerId == null || project.managerId == selectedManagerId }

    Column(Modifier.fillMaxSize().background(Page)) {
        TopBar(
            title = stringResource(R.string.projects_title),
            onBack = { nav.popBackStack() },
            actionText = stringResource(R.string.new_action),
            onAction = { nav.navigate(Routes.ADMIN_PROJECT_CREATE) }
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SyncStatus(state)
                    SearchField(
                        placeholder = stringResource(R.string.search_projects_hint),
                        value = query,
                        onValueChange = { query = it }
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        ProjectFilterDropdown(
                            selectedText = selectedStatus?.label() ?: stringResource(R.string.all_statuses),
                            modifier = Modifier.weight(1f)
                        ) {
                            DropdownMenuItem(text = { Text(stringResource(R.string.all_statuses)) }, onClick = { selectedStatus = null })
                            ProjectStatus.entries.forEach { status ->
                                DropdownMenuItem(text = { Text(status.label()) }, onClick = { selectedStatus = status })
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
                }
            }
            items(filteredProjects, key = { it.id }) { project ->
                ProjectListItem(
                    project = project,
                    allTasks = state.tasks,
                    managerName = state.users.managerName(project.managerId),
                    onEdit = {
                        nav.navigate(Routes.adminProjectEdit(project.id))
                    },
                    onDetails = {
                        nav.navigate(Routes.adminProjectDetails(project.id))
                    }
                )
            }
            if (filteredProjects.isEmpty()) {
                item { EmptyData() }
            }
        }
    }
}
