package com.taskflow.app.ui.manager

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.taskflow.app.R
import com.taskflow.app.ui.common.components.Field
import com.taskflow.app.ui.common.components.FormScreen
import com.taskflow.app.ui.common.components.SectionCard
import com.taskflow.app.ui.common.components.SyncStatus
import com.taskflow.app.ui.common.TaskFlowDataViewModel
import com.taskflow.app.ui.common.theme.Blue

@Composable
fun TaskFormScreen(nav: NavController, edit: Boolean) {
    val viewModel: TaskFlowDataViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsState()
    val task = state.tasks.firstOrNull { it.id == state.selectedTaskId } ?: state.tasks.firstOrNull()
    val project = state.projects.firstOrNull { it.id == task?.projectId } ?: state.projects.firstOrNull()
    var title by rememberSaveable(task?.id, edit) { mutableStateOf(if (edit) task?.title.orEmpty() else "") }
    var description by rememberSaveable(task?.id, edit) { mutableStateOf(if (edit) task?.description.orEmpty() else "") }
    FormScreen(if (edit) stringResource(R.string.edit_task_title) else stringResource(R.string.create_task_title), { nav.popBackStack() }) {
        SyncStatus(state)
        SectionCard("") {
            Field(stringResource(R.string.task_title_label), title, onValueChange = { title = it })
            Field(stringResource(R.string.description_label), description, onValueChange = { description = it }, minLines = 3)
            Field(stringResource(R.string.project_label_name), project?.name.orEmpty(), enabled = false)
            Field(stringResource(R.string.priority_label), "")
            Field(stringResource(R.string.deadline_label), "")
            Field(stringResource(R.string.status_label), "")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        viewModel.saveTask(
                            existing = if (edit) task else null,
                            project = project,
                            title = title,
                            description = description,
                            onDone = { nav.popBackStack() }
                        )
                    },
                    modifier = Modifier.weight(1f).height(52.dp),
                    colors = ButtonDefaults.buttonColors(Blue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(if (edit) stringResource(R.string.save_changes) else stringResource(R.string.create_task_title))
                }
                OutlinedButton(onClick = { nav.popBackStack() }, modifier = Modifier.weight(1f).height(52.dp), shape = RoundedCornerShape(8.dp)) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        }
    }
}
