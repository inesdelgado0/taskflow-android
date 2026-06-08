package com.taskflow.app.ui.manager

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.taskflow.app.R
import com.taskflow.app.domain.util.TaskStatus
import com.taskflow.app.ui.common.components.ListScreen
import com.taskflow.app.ui.common.components.TeamMemberCard
import com.taskflow.app.ui.common.TaskFlowDataViewModel
import com.taskflow.app.ui.common.util.averageRating
import com.taskflow.app.ui.common.components.taskFlowState
import com.taskflow.app.ui.common.util.toDemoUser
import com.taskflow.app.ui.navigation.Routes

@Composable
fun ManagerTeamScreen(nav: NavController) {
    val viewModel: TaskFlowDataViewModel = hiltViewModel()
    val state by taskFlowState()
    ListScreen(stringResource(R.string.team_title), stringResource(R.string.add_action), { nav.popBackStack() }, { nav.navigate(Routes.MANAGER_ADD_TEAM) }) {
        state.users
            .filter { it.isActive && it.roles.any { role -> role.name == "USER" } }
            .forEach { user ->
                val assignedTaskIds = state.userTaskAssignments
                    .filter { assignment -> assignment.userId == user.id }
                    .map { it.taskId }
                    .distinct()
                val userTasks = state.tasks.filter { task -> assignedTaskIds.contains(task.id) }
                val completedTasks = userTasks.count { it.status == TaskStatus.COMPLETED }
                val activeTasks = userTasks.count { it.status != TaskStatus.COMPLETED && it.status != TaskStatus.CANCELLED }
                val rating = state.allEvaluations
                    .filter { evaluation -> evaluation.evaluatedUserId == user.id }
                    .averageRating()

                TeamMemberCard(
                    user = user.toDemoUser(),
                    rating = rating,
                    activeTasksText = stringResource(R.string.active_tasks_count, activeTasks),
                    completedTasks = completedTasks.toString(),
                    activeTasks = activeTasks.toString(),
                    onViewTasks = { viewModel.selectUser(user.id) },
                    onEvaluate = { nav.navigate(Routes.managerEvaluateUser(user.id)) }
                )
        }
    }
}
