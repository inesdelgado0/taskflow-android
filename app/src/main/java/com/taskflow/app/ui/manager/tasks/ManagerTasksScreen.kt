package com.taskflow.app.ui.manager.tasks

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.taskflow.app.R
import com.taskflow.app.data.local.entity.ProjectEntity
import com.taskflow.app.data.local.entity.TaskEntity
import com.taskflow.app.domain.util.TaskPriority
import com.taskflow.app.domain.util.TaskStatus
import com.taskflow.app.ui.common.rememberWindowInfo

private val PrimaryBlue = Color(0xFF3B6FF0)
private val ScreenBlue = Color(0xFFEEF2FF)
private val BorderGray = Color(0xFFD1D5DB)
private val TextSecondary = Color(0xFF6B7280)

@Composable
fun ManagerTasksScreen(
    navController: NavController,
    managerId: Long,
    viewModel: ManagerTasksViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val formState by viewModel.formState.collectAsState()
    val windowInfo = rememberWindowInfo()

    LaunchedEffect(managerId) {
        viewModel.loadProjects(managerId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBlue)
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.tasks_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = { navController.popBackStack() }) {
                Text(text = stringResource(R.string.btn_back), color = PrimaryBlue)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (windowInfo.isLandscape) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                TaskFormCard(
                    modifier = Modifier.weight(0.9f),
                    formState = formState,
                    uiState = uiState,
                    managerId = managerId,
                    viewModel = viewModel
                )
                TasksListCard(
                    modifier = Modifier.weight(1.1f),
                    uiState = uiState,
                    viewModel = viewModel
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                item {
                    TaskFormCard(
                        modifier = Modifier.fillMaxWidth(),
                        formState = formState,
                        uiState = uiState,
                        managerId = managerId,
                        viewModel = viewModel
                    )
                }
                item {
                    TasksListCard(
                        modifier = Modifier.fillMaxWidth(),
                        uiState = uiState,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskFormCard(
    modifier: Modifier,
    formState: ManagerTaskFormState,
    uiState: ManagerTasksUiState,
    managerId: Long,
    viewModel: ManagerTasksViewModel
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = if (uiState.editingTaskId == null) {
                    stringResource(R.string.tasks_btn_new)
                } else {
                    stringResource(R.string.task_btn_edit)
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            ProjectSelector(
                projects = uiState.projects,
                selectedProjectId = formState.projectId,
                onProjectSelected = viewModel::onProjectSelected
            )
            formState.projectError?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.height(12.dp))
            TaskTextField(
                label = stringResource(R.string.task_label_title),
                value = formState.title,
                onValueChange = viewModel::onTitleChange,
                error = formState.titleError
            )
            TaskTextField(
                label = stringResource(R.string.task_label_description),
                value = formState.description,
                onValueChange = viewModel::onDescriptionChange,
                error = null
            )
            TaskTextField(
                label = stringResource(R.string.task_label_deadline),
                value = formState.deadlineText,
                onValueChange = viewModel::onDeadlineChange,
                error = formState.deadlineError,
                placeholder = stringResource(R.string.task_deadline_placeholder),
                keyboardType = KeyboardType.Text
            )
            OptionGroup(
                title = stringResource(R.string.task_label_priority),
                options = TaskPriority.entries,
                selected = formState.priority,
                label = { priorityLabel(it) },
                onSelect = viewModel::onPriorityChange
            )
            Spacer(modifier = Modifier.height(10.dp))
            OptionGroup(
                title = stringResource(R.string.task_label_status),
                options = TaskStatus.entries,
                selected = formState.status,
                label = { statusLabel(it) },
                onSelect = viewModel::onStatusChange
            )
            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = { viewModel.saveTask(managerId) },
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = Color.White)
                } else {
                    Text(text = stringResource(R.string.btn_save), color = Color.White)
                }
            }
            TextButton(
                onClick = viewModel::clearForm,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(text = stringResource(R.string.btn_cancel), color = TextSecondary)
            }
            uiState.errorMessage?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun TasksListCard(
    modifier: Modifier,
    uiState: ManagerTasksUiState,
    viewModel: ManagerTasksViewModel
) {
    val visibleTasks = if (uiState.query.isBlank()) {
        uiState.tasks
    } else {
        uiState.tasks.filter {
            it.title.contains(uiState.query, ignoreCase = true) ||
                it.description.orEmpty().contains(uiState.query, ignoreCase = true)
        }
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            OutlinedTextField(
                value = uiState.query,
                onValueChange = viewModel::onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(text = stringResource(R.string.search_hint)) },
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBlue,
                    unfocusedBorderColor = BorderGray
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
            if (visibleTasks.isEmpty()) {
                Text(
                    text = stringResource(R.string.tasks_empty),
                    color = TextSecondary,
                    modifier = Modifier.padding(vertical = 18.dp)
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(visibleTasks, key = { it.id }) { task ->
                        TaskRow(
                            task = task,
                            onEdit = { viewModel.startEdit(task) },
                            onDelete = { viewModel.deleteTask(task) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProjectSelector(
    projects: List<ProjectEntity>,
    selectedProjectId: Long,
    onProjectSelected: (Long) -> Unit
) {
    Text(
        text = stringResource(R.string.manager_select_project),
        style = MaterialTheme.typography.bodySmall,
        color = TextSecondary
    )
    Spacer(modifier = Modifier.height(8.dp))
    if (projects.isEmpty()) {
        Text(text = stringResource(R.string.projects_empty), color = TextSecondary)
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            projects.forEach { project ->
                SelectableButton(
                    text = project.name,
                    selected = project.id == selectedProjectId,
                    onClick = { onProjectSelected(project.id) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun TaskRow(
    task: TaskEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
        border = BorderStroke(1.dp, BorderGray)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = task.title, fontWeight = FontWeight.Bold)
                    task.description?.let { Text(text = it, color = TextSecondary) }
                    Text(
                        text = "${priorityLabel(task.priority)} · ${statusLabel(task.status)}",
                        color = PrimaryBlue,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    TextButton(onClick = onEdit) {
                        Text(text = stringResource(R.string.btn_edit), color = PrimaryBlue)
                    }
                    TextButton(onClick = onDelete) {
                        Text(text = stringResource(R.string.btn_delete), color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
private fun <T> OptionGroup(
    title: String,
    options: List<T>,
    selected: T,
    label: @Composable (T) -> String,
    onSelect: (T) -> Unit
) {
    Text(text = title, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
    Spacer(modifier = Modifier.height(8.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            SelectableButton(
                text = label(option),
                selected = option == selected,
                onClick = { onSelect(option) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SelectableButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (selected) PrimaryBlue else Color.White,
            contentColor = if (selected) Color.White else TextSecondary
        ),
        border = BorderStroke(1.dp, if (selected) PrimaryBlue else BorderGray)
    ) {
        Text(text = text, maxLines = 1, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun TaskTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    error: String?,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(modifier = Modifier.padding(bottom = 10.dp)) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(text = placeholder) },
            isError = error != null,
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryBlue,
                unfocusedBorderColor = BorderGray
            )
        )
        error?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun priorityLabel(priority: TaskPriority): String =
    when (priority) {
        TaskPriority.LOW -> stringResource(R.string.task_priority_low)
        TaskPriority.MEDIUM -> stringResource(R.string.task_priority_medium)
        TaskPriority.HIGH -> stringResource(R.string.task_priority_high)
        TaskPriority.CRITICAL -> stringResource(R.string.task_priority_critical)
    }

@Composable
private fun statusLabel(status: TaskStatus): String =
    when (status) {
        TaskStatus.PENDING -> stringResource(R.string.task_status_pending)
        TaskStatus.IN_PROGRESS -> stringResource(R.string.task_status_in_progress)
        TaskStatus.COMPLETED -> stringResource(R.string.task_status_completed)
        TaskStatus.CANCELLED -> stringResource(R.string.task_status_cancelled)
    }
