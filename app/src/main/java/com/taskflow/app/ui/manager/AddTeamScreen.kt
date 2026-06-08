package com.taskflow.app.ui.manager

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import com.taskflow.app.R
import com.taskflow.app.ui.common.components.Assigned
import com.taskflow.app.ui.common.components.AvailableUser
import com.taskflow.app.ui.common.components.EmptyData
import com.taskflow.app.ui.common.components.FormScreen
import com.taskflow.app.ui.common.components.MiniSelect
import com.taskflow.app.ui.common.components.SearchField
import com.taskflow.app.ui.common.components.SectionCard
import com.taskflow.app.ui.common.components.taskFlowState
import com.taskflow.app.ui.common.theme.Muted
import com.taskflow.app.ui.common.util.toDemoUser

@Composable
fun AddTeamScreen(nav: NavController) {
    val state by taskFlowState()
    FormScreen(stringResource(R.string.add_to_team), { nav.popBackStack() }) {
        SectionCard(stringResource(R.string.select_project)) {
            MiniSelect(state.projects.firstOrNull()?.name ?: stringResource(R.string.no_projects_synced))
            Text(stringResource(R.string.assign_project_users_hint), color = Muted, style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
        }
        SectionCard(stringResource(R.string.current_project_team, state.users.take(3).size)) {
            state.users.take(3).forEach { user ->
                val demo = user.toDemoUser()
                Assigned(demo.name, demo.initial, demo.color)
            }
            if (state.users.isEmpty()) EmptyData()
        }
        SectionCard(stringResource(R.string.available_users)) {
            SearchField(stringResource(R.string.search_users_hint))
            state.users.drop(2).forEach { user -> AvailableUser(user.toDemoUser()) }
            if (state.users.drop(2).isEmpty()) EmptyData()
        }
    }
}
