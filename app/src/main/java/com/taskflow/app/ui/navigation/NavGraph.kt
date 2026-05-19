package com.taskflow.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.taskflow.app.ui.admin.users.AdminUsersScreen
import com.taskflow.app.ui.admin.users.AdminUsersViewModel
import com.taskflow.app.ui.auth.AuthViewModel
import com.taskflow.app.ui.auth.LoginScreen
import com.taskflow.app.ui.auth.RegisterScreen
import com.taskflow.app.ui.manager.tasks.ManagerTasksScreen
import com.taskflow.app.ui.manager.tasks.ManagerTasksViewModel
import com.taskflow.app.ui.user.UserDashboardScreen

@Composable
fun TaskFlowNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN
    ) {
        composable(Routes.LOGIN) {
            val viewModel: AuthViewModel = hiltViewModel()
            LoginScreen(
                navController = navController,
                viewModel = viewModel
            )
        }

        composable(Routes.REGISTER) {
            val viewModel: AuthViewModel = hiltViewModel()
            RegisterScreen(
                navController = navController,
                viewModel = viewModel
            )
        }

        composable(Routes.ADMIN_DASHBOARD) {
            val viewModel: AdminUsersViewModel = hiltViewModel()
            AdminUsersScreen(
                navController = navController,
                viewModel = viewModel
            )
        }

        composable(Routes.MANAGER_DASHBOARD) {
            val viewModel: ManagerTasksViewModel = hiltViewModel()
            ManagerTasksScreen(
                navController = navController,
                managerId = 0L,
                viewModel = viewModel
            )
        }

        composable(
            route = Routes.MANAGER_DASHBOARD_WITH_ID,
            arguments = listOf(
                navArgument(Routes.MANAGER_ID_ARG) {
                    type = NavType.LongType
                    defaultValue = 0L
                }
            )
        ) { backStackEntry ->
            val viewModel: ManagerTasksViewModel = hiltViewModel()
            val managerId = backStackEntry.arguments?.getLong(Routes.MANAGER_ID_ARG) ?: 0L
            ManagerTasksScreen(
                navController = navController,
                managerId = managerId,
                viewModel = viewModel
            )
        }

        composable(Routes.USER_DASHBOARD) {
            UserDashboardScreen(
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.LOGIN) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}
