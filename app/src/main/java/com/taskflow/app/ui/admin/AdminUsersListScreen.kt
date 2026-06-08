package com.taskflow.app.ui.admin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.taskflow.app.ui.common.components.EmptyData
import com.taskflow.app.ui.common.components.ListScreen
import com.taskflow.app.ui.common.components.SearchField
import com.taskflow.app.ui.common.components.SyncStatus
import com.taskflow.app.ui.common.TaskFlowDataViewModel
import com.taskflow.app.ui.common.components.UserCard
import com.taskflow.app.ui.common.util.toDemoUser
import com.taskflow.app.ui.navigation.Routes
import androidx.compose.ui.res.stringResource
import com.taskflow.app.R

@Composable
fun AdminUsersListScreen(nav: NavController) {
    val viewModel: TaskFlowDataViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsState()
    ListScreen(
        title = stringResource(R.string.users_title),
        actionText = stringResource(R.string.new_action),
        onBack = { nav.popBackStack() },
        onAction = { nav.navigate(Routes.ADMIN_USER_CREATE) }
    ) {
        SyncStatus(state)
        SearchField(stringResource(R.string.search_users_hint))
        state.users.forEach { user ->
            UserCard(
                user.toDemoUser(),
                showRole = true,
                onEdit = {
                    viewModel.selectUser(user.id)
                    nav.navigate(Routes.ADMIN_USER_EDIT)
                },
                onRemove = { viewModel.deleteUser(user) }
            )
        }
        if (state.users.isEmpty()) EmptyData()
    }
}
