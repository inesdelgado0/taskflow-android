package com.taskflow.app.ui.user

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.taskflow.app.domain.util.TaskStatus
import com.taskflow.app.ui.common.components.EmptyData
import com.taskflow.app.ui.common.components.Field
import com.taskflow.app.ui.common.components.FormScreen
import com.taskflow.app.ui.common.components.Observation
import com.taskflow.app.ui.common.components.ProgressLine
import com.taskflow.app.ui.common.components.SectionCard
import com.taskflow.app.ui.common.components.StatusPill
import com.taskflow.app.ui.common.components.SyncStatus
import com.taskflow.app.ui.common.TaskFlowDataViewModel
import com.taskflow.app.ui.common.components.TwoMetrics
import com.taskflow.app.ui.common.theme.Blue
import com.taskflow.app.ui.common.theme.Muted
import com.taskflow.app.ui.common.util.color
import com.taskflow.app.ui.common.util.displayDate
import com.taskflow.app.ui.navigation.Routes

@Composable
fun UserTaskDetailsScreen(nav: NavController, managerMode: Boolean = false) {
    val viewModel: TaskFlowDataViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsState()
    val task = state.tasks.firstOrNull { it.id == state.selectedTaskId } ?: state.tasks.firstOrNull()
    val project = state.projects.firstOrNull { it.id == task?.projectId }
    var observationText by rememberSaveable(task?.id) { mutableStateOf("") }
    FormScreen(stringResource(R.string.task_details), { nav.popBackStack() }) {
        SyncStatus(state)
        if (task == null) {
            EmptyData()
            return@FormScreen
        }
        SectionCard(task.title) {
            val assignment = state.userTaskAssignments.firstOrNull {
                it.taskId == task.id && it.userId == state.currentUser?.id
            }
            StatusPill(task.priority.name, task.priority.color())
            Text(stringResource(R.string.project_prefix, project?.name.orEmpty()), color = Muted)
            Text(task.description.orEmpty(), color = Muted)
            TwoMetrics(stringResource(R.string.deadline_label), task.deadline.displayDate(), stringResource(R.string.status_label), task.status.name)
            val progress = assignment?.completionPercentage
                ?: if (task.status == TaskStatus.COMPLETED) 100 else 0
            val assignedCount = state.userTaskAssignments.count { it.taskId == task.id }
            TwoMetrics(stringResource(R.string.assigned_count), assignedCount.toString(), stringResource(R.string.progress_label), "$progress%")
            TwoMetrics(stringResource(R.string.location_label), assignment?.location.orEmpty(), stringResource(R.string.time_spent), "${assignment?.timeSpentMinutes ?: 0} min")
            ProgressLine(stringResource(R.string.progress_label), "$progress%", progress / 100f)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        if (managerMode) {
                            viewModel.selectTask(task.id)
                            nav.navigate(Routes.MANAGER_TASK_EDIT)
                        } else {
                            viewModel.updateTaskStatus(task, TaskStatus.IN_PROGRESS)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(Blue)
                ) {
                    Text(if (managerMode) stringResource(R.string.edit_task_title) else stringResource(R.string.save_progress))
                }
                OutlinedButton(
                    onClick = {
                        if (managerMode) {
                            viewModel.selectTask(task.id)
                            nav.navigate(Routes.MANAGER_ASSIGN_USERS)
                        } else {
                            nav.navigate(Routes.USER_OBSERVATIONS)
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (managerMode) stringResource(R.string.associate_action) else stringResource(R.string.new_observation_short))
                }
            }
        }
        if (!managerMode) {
            SectionCard(stringResource(R.string.task_progress_register)) {
                Field(stringResource(R.string.data_label), "")
                Field(stringResource(R.string.location_label), "")
                Field(stringResource(R.string.completion_percentage), "")
                Field(stringResource(R.string.time_spent), "")
                Field(stringResource(R.string.comment_optional), observationText, onValueChange = { observationText = it }, minLines = 3)
                Button(onClick = { viewModel.updateTaskStatus(task, TaskStatus.COMPLETED) }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(Blue)) {
                    Text(stringResource(R.string.save_progress))
                }
            }
        }
        SectionCard(stringResource(R.string.observations_count, state.observations.size)) {
            state.observations.forEach { observation ->
                val user = state.users.firstOrNull { it.id == observation.userId }
                Observation(user?.name.orEmpty(), observation.text.orEmpty(), observation.createdAt.toString())
            }
            if (state.observations.isEmpty()) EmptyData()
        }
    }
}
