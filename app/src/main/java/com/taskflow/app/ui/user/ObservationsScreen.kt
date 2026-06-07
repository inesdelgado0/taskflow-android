package com.taskflow.app.ui.user

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.taskflow.app.R
import com.taskflow.app.ui.common.components.EmptyData
import com.taskflow.app.ui.common.components.Field
import com.taskflow.app.ui.common.components.FormScreen
import com.taskflow.app.ui.common.components.Observation
import com.taskflow.app.ui.common.components.SectionCard
import com.taskflow.app.ui.common.components.SyncStatus
import com.taskflow.app.ui.common.TaskFlowDataViewModel
import com.taskflow.app.ui.common.theme.Blue
import com.taskflow.app.ui.common.theme.Muted

@Composable
fun ObservationsScreen(nav: NavController) {
    val viewModel: TaskFlowDataViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsState()
    val task = state.tasks.firstOrNull { it.id == state.selectedTaskId } ?: state.tasks.firstOrNull()
    var observationText by rememberSaveable(task?.id) { mutableStateOf("") }
    FormScreen(stringResource(R.string.observations_title), { nav.popBackStack() }) {
        SyncStatus(state)
        SectionCard(task?.title ?: stringResource(R.string.task_title)) {
            Text(task?.description.orEmpty().ifBlank { stringResource(R.string.no_description) }, color = Muted)
        }
        SectionCard(stringResource(R.string.new_observation_short)) {
            Field(stringResource(R.string.comment_optional), observationText, onValueChange = { observationText = it }, minLines = 4)
            Button(
                onClick = {
                    if (task != null) {
                        viewModel.createObservation(task, observationText) { observationText = "" }
                    }
                },
                enabled = task != null && observationText.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(Blue)
            ) {
                Text(stringResource(R.string.save_progress))
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
