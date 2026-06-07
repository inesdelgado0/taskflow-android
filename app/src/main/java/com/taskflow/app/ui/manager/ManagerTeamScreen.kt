package com.taskflow.app.ui.manager

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.taskflow.app.R
import com.taskflow.app.ui.common.components.ListScreen
import com.taskflow.app.ui.common.components.SyncStatus
import com.taskflow.app.ui.common.components.TeamMemberCard
import com.taskflow.app.ui.common.components.taskFlowState
import com.taskflow.app.ui.common.util.toDemoUser
import com.taskflow.app.ui.navigation.Routes

@Composable
fun ManagerTeamScreen(nav: NavController) {
    val state by taskFlowState()
    ListScreen(stringResource(R.string.team_title), stringResource(R.string.add_action), { nav.popBackStack() }, { nav.navigate(Routes.MANAGER_ADD_TEAM) }) {
        SyncStatus(state)
        state.users.forEach { user ->
            TeamMemberCard(user.toDemoUser()) { nav.navigate(Routes.MANAGER_EVALUATE_USER) }
        }
    }
}
