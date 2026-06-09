package com.taskflow.app.ui.manager

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Work
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.taskflow.app.R
import com.taskflow.app.domain.util.ProjectStatus
import com.taskflow.app.domain.util.TaskStatus
import com.taskflow.app.ui.common.components.AppScaffold
import com.taskflow.app.ui.common.components.EmptyData
import com.taskflow.app.ui.common.components.NotificationStateComponent
import com.taskflow.app.ui.common.components.SectionCard
import com.taskflow.app.ui.common.components.StatNavCard
import com.taskflow.app.ui.common.components.SyncStatus
import com.taskflow.app.ui.common.components.TaskPriorityLine
import com.taskflow.app.ui.common.components.Welcome
import com.taskflow.app.ui.common.components.taskFlowState
import com.taskflow.app.ui.common.theme.Blue
import com.taskflow.app.ui.common.theme.Green
import com.taskflow.app.ui.common.theme.Orange
import com.taskflow.app.ui.common.theme.Purple
import com.taskflow.app.ui.common.util.color
import com.taskflow.app.ui.common.util.completionRate
import com.taskflow.app.ui.navigation.Routes

@Composable
fun ManagerDashboardScreen(nav: NavController, onLogout: () -> Unit) {
    val state by taskFlowState()
    AppScaffold(
        role = "G",
        accent = Green,
        onLogout = onLogout,
        onProfile = { nav.navigate(Routes.MANAGER_PROFILE) },
        onNotificationClick = { taskId ->
            if (taskId != null) nav.navigate(Routes.MANAGER_TASKS_LIST)
        }
    ) {
        Welcome(stringResource(R.string.dashboard_manager))
        SyncStatus(state)
        NotificationStateComponent(state)
        val pending = state.tasks.count { it.status != TaskStatus.COMPLETED && it.status != TaskStatus.CANCELLED }
        val completed = state.tasks.count { it.status == TaskStatus.COMPLETED }
        StatNavCard(Icons.Default.CheckBox, stringResource(R.string.tasks_title), state.tasks.size.toString(), stringResource(R.string.dashboard_tasks_detail, pending, completed), Blue) { nav.navigate(Routes.MANAGER_TASKS_LIST) }
        StatNavCard(Icons.Default.Group, stringResource(R.string.team_title), state.users.size.toString(), stringResource(R.string.dashboard_active_members), Green) { nav.navigate(Routes.MANAGER_TEAM) }
        StatNavCard(Icons.Default.Work, stringResource(R.string.dashboard_my_projects), state.projects.size.toString(), stringResource(R.string.dashboard_active_projects, state.projects.count { it.status == ProjectStatus.ACTIVE }), Purple) { nav.navigate(Routes.MANAGER_PROJECTS) }
        StatNavCard(Icons.Default.CalendarToday, stringResource(R.string.stats_title), "${state.tasks.completionRate()}%", stringResource(R.string.dashboard_completion_rate), Orange) { nav.navigate(Routes.MANAGER_STATS) }
        SectionCard(stringResource(R.string.dashboard_priority_tasks)) {
            state.tasks.take(3).forEach { task ->
                TaskPriorityLine(task.priority.name, task.title, task.priority.color())
            }
            if (state.tasks.isEmpty()) EmptyData()
        }
    }
}
