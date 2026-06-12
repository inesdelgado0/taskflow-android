package com.taskflow.app.ui.manager

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.taskflow.app.ui.common.components.DatePickerField
import com.taskflow.app.ui.common.components.DropdownSelector
import com.taskflow.app.ui.common.components.Field
import com.taskflow.app.ui.common.components.FormScreen
import com.taskflow.app.ui.common.components.SectionCard
import com.taskflow.app.ui.common.components.SyncStatus
import com.taskflow.app.ui.common.TaskFlowDataViewModel
import com.taskflow.app.ui.common.theme.Blue
import com.taskflow.app.ui.common.theme.Red
import com.taskflow.app.ui.common.util.label

@Composable
fun TaskFormScreen(nav: NavController, edit: Boolean, taskId: Long? = null) {
    val viewModel: TaskFlowDataViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsState()
    val selectedTaskId = taskId ?: state.selectedTaskId
    val task = selectedTaskId?.let { id -> state.tasks.firstOrNull { it.id == id } }
    val project = state.projects.firstOrNull { it.id == task?.projectId }
    val initialProjectId = if (edit) task?.projectId else state.selectedProjectId ?: project?.id
    var hasChanges by rememberSaveable(edit, task?.id, initialProjectId) { mutableStateOf(false) }
    var showDeleteDialog by rememberSaveable(task?.id, edit) { mutableStateOf(false) }

    LaunchedEffect(taskId) {
        if (taskId != null) {
            viewModel.selectTask(taskId)
        }
    }

    if (showDeleteDialog && edit && task != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.dialog_delete_title)) },
            text = { Text(stringResource(R.string.dialog_delete_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteTask(task) { nav.popBackStack() }
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

    FormScreen(
        title = if (edit) stringResource(R.string.edit_task_title) else stringResource(R.string.create_task_title),
        onBack = { nav.popBackStack() },
        confirmOnBack = hasChanges
    ) {
        SyncStatus(state)
        TaskFormContent(
            edit = edit,
            initialTitle = if (edit) task?.title.orEmpty() else "",
            initialDescription = if (edit) task?.description.orEmpty() else "",
            initialProjectId = initialProjectId,
            projects = state.projects.map { it.id to it.name },
            initialPriority = if (edit) task?.priority ?: TaskPriority.MEDIUM else TaskPriority.MEDIUM,
            initialDeadline = if (edit) task?.deadline else null,
            initialStatus = if (edit) task?.status ?: TaskStatus.PENDING else TaskStatus.PENDING,
            onDirtyChange = { hasChanges = it },
            onDelete = if (edit) {
                { showDeleteDialog = true }
            } else {
                null
            },
            onSave = { title, description, projectId, priority, deadline, status ->
                val selectedProject = state.projects.firstOrNull { it.id == projectId }
                viewModel.saveTask(
                    existing = if (edit) task else null,
                    project = selectedProject,
                    title = title,
                    description = description,
                    priority = priority,
                    deadline = deadline,
                    status = status,
                    onDone = { nav.popBackStack() }
                )
            },
            onCancel = { nav.popBackStack() }
        )
    }
}

@Composable
internal fun TaskFormContent(
    edit: Boolean,
    initialTitle: String,
    initialDescription: String,
    initialProjectId: Long?,
    projects: List<Pair<Long, String>>,
    initialPriority: TaskPriority,
    initialDeadline: Long?,
    initialStatus: TaskStatus,
    onDirtyChange: (Boolean) -> Unit,
    onDelete: (() -> Unit)?,
    onSave: (String, String, Long?, TaskPriority, Long?, TaskStatus) -> Unit,
    onCancel: () -> Unit
) {
    var title by rememberSaveable(edit, initialTitle) { mutableStateOf(initialTitle) }
    var description by rememberSaveable(edit, initialDescription) { mutableStateOf(initialDescription) }
    var selectedProjectId by rememberSaveable(edit, initialProjectId) { mutableStateOf(initialProjectId) }
    var selectedPriority by rememberSaveable(edit, initialPriority) { mutableStateOf(initialPriority) }
    var selectedDeadline by rememberSaveable(edit, initialDeadline) { mutableStateOf(initialDeadline) }
    var selectedStatus by rememberSaveable(edit, initialStatus) { mutableStateOf(initialStatus) }
    val hasChanges = title != initialTitle ||
        description != initialDescription ||
        selectedProjectId != initialProjectId ||
        selectedPriority != initialPriority ||
        selectedDeadline != initialDeadline ||
        selectedStatus != initialStatus

    LaunchedEffect(hasChanges) {
        onDirtyChange(hasChanges)
    }

    SectionCard("") {
        Field(stringResource(R.string.task_title_label), title, onValueChange = { title = it })
        Field(stringResource(R.string.description_label), description, onValueChange = { description = it }, minLines = 3)
        DropdownSelector(
            label = stringResource(R.string.project_label_name),
            selectedText = projects.firstOrNull { it.first == selectedProjectId }?.second
                ?: stringResource(R.string.select_project)
        ) {
            projects.forEach { project ->
                DropdownMenuItem(
                    text = { Text(project.second) },
                    onClick = { selectedProjectId = project.first }
                )
            }
        }
        DropdownSelector(
            label = stringResource(R.string.priority_label),
            selectedText = selectedPriority.label()
        ) {
            TaskPriority.entries.forEach { priority ->
                DropdownMenuItem(
                    text = { Text(priority.label()) },
                    onClick = { selectedPriority = priority }
                )
            }
        }
        DatePickerField(
            label = stringResource(R.string.deadline_label),
            value = selectedDeadline,
            onDateSelected = { selectedDeadline = it }
        )
        DropdownSelector(
            label = stringResource(R.string.status_label),
            selectedText = selectedStatus.label()
        ) {
            TaskStatus.entries.forEach { status ->
                DropdownMenuItem(
                    text = { Text(status.label()) },
                    onClick = { selectedStatus = status }
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = { onSave(title, description, selectedProjectId, selectedPriority, selectedDeadline, selectedStatus) },
                modifier = Modifier.weight(1f).height(52.dp),
                colors = ButtonDefaults.buttonColors(Blue),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(if (edit) stringResource(R.string.save_changes) else stringResource(R.string.create_task_title))
            }
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f).height(52.dp), shape = RoundedCornerShape(8.dp)) {
                Text(stringResource(R.string.btn_cancel))
            }
        }
        if (edit && onDelete != null) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onDelete,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Red)
            ) {
                Text(stringResource(R.string.task_btn_delete))
            }
        }
    }
}
