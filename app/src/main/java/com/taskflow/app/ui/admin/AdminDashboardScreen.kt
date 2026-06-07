package com.taskflow.app.ui.admin

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Work
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import com.taskflow.app.domain.util.ProjectStatus
import com.taskflow.app.ui.common.components.ActivityLine
import com.taskflow.app.ui.common.components.AppScaffold
import com.taskflow.app.ui.common.components.EmptyData
import com.taskflow.app.ui.common.components.NotificationStateComponent
import com.taskflow.app.ui.common.components.SectionCard
import com.taskflow.app.ui.common.components.StatNavCard
import com.taskflow.app.ui.common.components.SyncStatus
import com.taskflow.app.ui.common.components.Welcome
import com.taskflow.app.ui.common.components.taskFlowState
import com.taskflow.app.ui.common.theme.Blue
import com.taskflow.app.ui.common.theme.Green
import com.taskflow.app.ui.common.theme.Purple
import com.taskflow.app.ui.common.util.completionRate
import com.taskflow.app.ui.common.util.relativeTime
import com.taskflow.app.ui.navigation.Routes
import androidx.compose.ui.res.stringResource
import com.taskflow.app.R

@Composable
fun AdminDashboardScreen(nav: NavController, onLogout: () -> Unit) {
    val state by taskFlowState()
    AppScaffold(
        role = "A",
        accent = Blue,
        onLogout = onLogout,
        onProfile = { nav.navigate(Routes.ADMIN_PROFILE) },
        content = {
            Welcome(stringResource(R.string.dashboard_admin))
            SyncStatus(state)
            NotificationStateComponent(state)
            val activeProjects = state.projects.count { it.status == ProjectStatus.ACTIVE }
            val completedProjects = state.projects.count { it.status == ProjectStatus.COMPLETED }
            val managers = state.users.count { user -> user.roles.any { it.name == "MANAGER" } }
            val regularUsers = state.users.count { user -> user.roles.any { it.name == "USER" } }
            val completion = state.tasks.completionRate()
            StatNavCard(Icons.Default.Work, stringResource(R.string.projects_title), state.projects.size.toString(), stringResource(R.string.dashboard_projects_detail, activeProjects, completedProjects), Blue) { nav.navigate(Routes.ADMIN_PROJECTS) }
            StatNavCard(Icons.Default.Group, stringResource(R.string.users_title), state.users.size.toString(), stringResource(R.string.dashboard_users_detail, managers, regularUsers), Green) { nav.navigate(Routes.ADMIN_USERS_LIST) }
            StatNavCard(Icons.Default.CalendarToday, stringResource(R.string.stats_title), "$completion%", stringResource(R.string.dashboard_completion_rate), Purple) { nav.navigate(Routes.ADMIN_STATS) }
            SectionCard(stringResource(R.string.dashboard_recent_activity)) {
                state.projects.take(3).forEach { project ->
                    ActivityLine(stringResource(R.string.updated_project_activity, project.name), project.updatedAt.relativeTime())
                }
                if (state.projects.isEmpty()) EmptyData()
            }
        }
    )
}
