package com.taskflow.app.ui.manager

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.taskflow.app.R
import com.taskflow.app.domain.util.TaskStatus
import com.taskflow.app.ui.common.components.Avatar
import com.taskflow.app.ui.common.components.Field
import com.taskflow.app.ui.common.components.FormScreen
import com.taskflow.app.ui.common.components.SectionCard
import com.taskflow.app.ui.common.components.SyncStatus
import com.taskflow.app.ui.common.TaskFlowDataViewModel
import com.taskflow.app.ui.common.components.TwoMetrics
import com.taskflow.app.ui.common.theme.Blue
import com.taskflow.app.ui.common.theme.Muted
import com.taskflow.app.ui.common.theme.Orange
import com.taskflow.app.ui.common.theme.Yellow
import com.taskflow.app.ui.common.util.initial
import com.taskflow.app.ui.common.util.toDemoUser

@Composable
fun EvaluateUserScreen(nav: NavController) {
    val viewModel: TaskFlowDataViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsState()
    val user = state.users.firstOrNull()
    val project = state.projects.firstOrNull { it.id == state.selectedProjectId } ?: state.projects.firstOrNull()
    var rating by rememberSaveable { mutableStateOf(5) }
    var comment by rememberSaveable { mutableStateOf("") }
    FormScreen(stringResource(R.string.evaluate_user), { nav.popBackStack() }) {
        SyncStatus(state)
        SectionCard("") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(user?.name.initial(), user?.toDemoUser()?.color ?: Orange, 56)
                Spacer(Modifier.width(12.dp))
                androidx.compose.foundation.layout.Column {
                    Text(user?.name.orEmpty(), fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.project_prefix, project?.name.orEmpty()), color = Muted)
                }
            }
        }
        SectionCard(stringResource(R.string.performance_evaluation)) {
            TwoMetrics(
                stringResource(R.string.completed_tasks_metric),
                state.tasks.count { it.status == TaskStatus.COMPLETED }.toString(),
                stringResource(R.string.active_tasks_metric),
                state.tasks.count { it.status != TaskStatus.COMPLETED && it.status != TaskStatus.CANCELLED }.toString()
            )
            Text(stringResource(R.string.numeric_rating), fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(5) { index ->
                    Icon(
                        Icons.Default.Star,
                        null,
                        tint = if (index < rating) Yellow else Muted,
                        modifier = Modifier.size(30.dp).clickable { rating = index + 1 }
                    )
                }
            }
            Field(stringResource(R.string.comment_optional), comment, onValueChange = { comment = it }, minLines = 4)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { viewModel.saveEvaluation(user, project, rating, comment) { nav.popBackStack() } },
                    modifier = Modifier.weight(1f).height(52.dp),
                    colors = ButtonDefaults.buttonColors(Blue)
                ) { Text(stringResource(R.string.save_evaluation)) }
                OutlinedButton(onClick = { nav.popBackStack() }, modifier = Modifier.weight(1f).height(52.dp)) { Text(stringResource(R.string.btn_cancel)) }
            }
        }
    }
}
