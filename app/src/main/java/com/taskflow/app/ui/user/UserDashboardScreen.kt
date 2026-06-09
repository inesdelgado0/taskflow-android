package com.taskflow.app.ui.user

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.taskflow.app.R
import com.taskflow.app.domain.util.TaskStatus
import com.taskflow.app.ui.common.TaskFlowDataViewModel
import com.taskflow.app.ui.common.components.AppScaffold
import com.taskflow.app.ui.common.components.EmptyData
import com.taskflow.app.ui.common.components.NotificationStateComponent
import com.taskflow.app.ui.common.components.SectionCard
import com.taskflow.app.ui.common.components.SmallStat
import com.taskflow.app.ui.common.components.SyncStatus
import com.taskflow.app.ui.common.components.UserTaskLine
import com.taskflow.app.ui.common.components.Welcome
import com.taskflow.app.ui.common.components.taskFlowState
import com.taskflow.app.ui.common.theme.Blue
import com.taskflow.app.ui.common.theme.Orange
import com.taskflow.app.ui.common.util.isNearDeadline
import com.taskflow.app.ui.navigation.Routes

@Composable
fun UserDashboardScreen(nav: NavController, onLogout: () -> Unit) {
    val viewModel: TaskFlowDataViewModel = hiltViewModel()
    val state by taskFlowState()
    AppScaffold(
        role = "U",
        accent = Orange,
        onLogout = onLogout,
        onProfile = { nav.navigate(Routes.USER_PROFILE) },
        onNotificationClick = { taskId ->
            taskId?.let {
                viewModel.selectTask(it)
                nav.navigate(Routes.userTaskExecution(it))
            }
        }
    ) {
        Welcome(state.currentUser?.name ?: stringResource(R.string.dashboard_user))
        SyncStatus(state)
        NotificationStateComponent(state)
        val activeTasks = state.tasks.filter {
            it.status != TaskStatus.COMPLETED && it.status != TaskStatus.CANCELLED
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            SmallStat(
                Icons.Default.CheckBox,
                stringResource(R.string.dashboard_active_tasks),
                activeTasks.size.toString(),
                stringResource(R.string.dashboard_near_deadline, activeTasks.count { it.deadline.isNearDeadline() }),
                Orange,
                Modifier.weight(1f)
            )
            SmallStat(
                Icons.Default.CalendarToday,
                stringResource(R.string.dashboard_completed),
                state.tasks.count { it.status == TaskStatus.COMPLETED }.toString(),
                stringResource(R.string.dashboard_total_synced),
                Blue,
                Modifier.weight(1f)
            )
        }
        SectionCard(stringResource(R.string.pending_tasks_title)) {
            activeTasks.forEach { task ->
                UserTaskLine(task, state.projects) {
                    viewModel.selectTask(task.id)
                    nav.navigate(Routes.userTaskExecution(task.id))
                }
            }
            if (activeTasks.isEmpty()) EmptyData()
        }
        OutlinedButton(
            onClick = { nav.navigate(Routes.USER_HISTORY) },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(stringResource(R.string.view_completed_history))
        }
    }
}
