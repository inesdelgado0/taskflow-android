package com.taskflow.app.ui.manager

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.taskflow.app.ui.user.UserTaskDetailsScreen

@Composable
fun ManagerTaskDetailsScreen(nav: NavController) {
    UserTaskDetailsScreen(nav = nav, managerMode = true)
}
