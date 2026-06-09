package com.taskflow.app.ui.project

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.taskflow.app.R
import com.taskflow.app.domain.util.ProjectStatus
import com.taskflow.app.ui.common.components.DatePickerField
import com.taskflow.app.ui.common.components.DropdownSelector
import com.taskflow.app.ui.common.components.Field
import com.taskflow.app.ui.common.components.FormScreen
import com.taskflow.app.ui.common.components.SectionCard
import com.taskflow.app.ui.common.components.SyncStatus
import com.taskflow.app.ui.common.TaskFlowDataViewModel
import com.taskflow.app.ui.common.theme.Blue
import com.taskflow.app.ui.common.util.color
import com.taskflow.app.ui.common.util.label
import com.taskflow.app.ui.common.util.projectManagers

@Composable
fun ProjectFormScreen(nav: NavController, edit: Boolean) {
    val viewModel: TaskFlowDataViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsState()
    val project = state.projects.firstOrNull { it.id == state.selectedProjectId } ?: state.projects.firstOrNull()
    val managers = state.users.projectManagers()
    FormScreen(
        title = if (edit) stringResource(R.string.edit_project) else stringResource(R.string.create_project),
        onBack = { nav.popBackStack() },
        confirmOnBack = true
    ) {
        SyncStatus(state)
        ProjectFormContent(
            edit = edit,
            initialName = if (edit) project?.name.orEmpty() else "",
            initialDescription = if (edit) project?.description.orEmpty() else "",
            initialStartDate = if (edit) project?.startDate else null,
            initialEndDate = if (edit) project?.endDate else null,
            initialManagerId = if (edit) project?.managerId else null,
            initialStatus = if (edit) project?.status ?: ProjectStatus.ACTIVE else ProjectStatus.ACTIVE,
            managers = managers.map { it.id to it.name },
            onSave = { name, description, startDate, endDate, managerId, status ->
                viewModel.saveProject(
                    existing = if (edit) project else null,
                    name = name,
                    description = description,
                    startDate = startDate,
                    endDate = endDate,
                    managerId = managerId,
                    status = status,
                    onDone = { nav.popBackStack() }
                )
            },
            onCancel = { nav.popBackStack() }
        )
    }
}

@Composable
internal fun ProjectFormContent(
    edit: Boolean,
    initialName: String,
    initialDescription: String,
    initialStartDate: Long?,
    initialEndDate: Long?,
    initialManagerId: Long?,
    initialStatus: ProjectStatus,
    managers: List<Pair<Long, String>>,
    onSave: (String, String, Long?, Long?, Long?, ProjectStatus) -> Unit,
    onCancel: () -> Unit
) {
    var name by rememberSaveable(edit, initialName) { mutableStateOf(initialName) }
    var description by rememberSaveable(edit, initialDescription) { mutableStateOf(initialDescription) }
    var startDate by rememberSaveable(edit, initialStartDate) { mutableStateOf(initialStartDate) }
    var endDate by rememberSaveable(edit, initialEndDate) { mutableStateOf(initialEndDate) }
    var selectedManagerId by rememberSaveable(edit, initialManagerId) { mutableStateOf(initialManagerId) }
    var selectedStatus by rememberSaveable(edit, initialStatus) { mutableStateOf(initialStatus) }

    SectionCard("") {
        Field(
            stringResource(R.string.project_label_name),
            name,
            placeholder = if (edit) "" else "Ex: Website Redesign",
            onValueChange = { name = it }
        )
        Field(
            stringResource(R.string.project_label_description),
            description,
            placeholder = if (edit) "" else "Descricao detalhada do projeto",
            onValueChange = { description = it },
            minLines = 4
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            DatePickerField(
                label = stringResource(R.string.start_date),
                value = startDate,
                onDateSelected = { startDate = it },
                modifier = Modifier.weight(1f)
            )
            DatePickerField(
                label = stringResource(R.string.end_date),
                value = endDate,
                onDateSelected = { endDate = it },
                modifier = Modifier.weight(1f)
            )
        }
        DropdownSelector(
            label = if (edit) "Gestor de Projeto (RF04)" else stringResource(R.string.project_label_manager),
            selectedText = managers.firstOrNull { it.first == selectedManagerId }?.second.orEmpty(),
            helperText = if (edit) "" else stringResource(R.string.assign_manager_hint)
        ) {
            DropdownMenuItem(text = { Text(stringResource(R.string.assign_manager_hint)) }, onClick = { selectedManagerId = null })
            managers.forEach { manager ->
                DropdownMenuItem(text = { Text(manager.second) }, onClick = { selectedManagerId = manager.first })
            }
        }
        DropdownSelector(
            label = stringResource(R.string.project_label_status),
            selectedText = selectedStatus.label()
        ) {
            ProjectStatus.entries.forEach { status ->
                DropdownMenuItem(text = { Text(status.label()) }, onClick = { selectedStatus = status })
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = { onSave(name, description, startDate, endDate, selectedManagerId, selectedStatus) },
                modifier = Modifier.weight(1f).height(52.dp),
                colors = ButtonDefaults.buttonColors(Blue),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(if (edit) stringResource(R.string.save_changes) else stringResource(R.string.create_project), textAlign = TextAlign.Center)
            }
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f).height(52.dp), shape = RoundedCornerShape(8.dp)) {
                Text(stringResource(R.string.btn_cancel))
            }
        }
    }
}
