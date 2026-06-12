package com.taskflow.app.ui.manager

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.taskflow.app.ui.user.UserTaskDetailsScreen

@Composable
fun ManagerTaskDetailsScreen(nav: NavController, taskId: Long? = null) {
    UserTaskDetailsScreen(nav = nav, managerMode = true, taskId = taskId)
}
