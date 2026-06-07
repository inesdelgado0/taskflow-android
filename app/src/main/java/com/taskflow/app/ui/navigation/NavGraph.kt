package com.taskflow.app.ui.navigation

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.taskflow.app.ui.auth.AuthViewModel
import com.taskflow.app.ui.auth.LoginScreen
import com.taskflow.app.ui.auth.RegisterScreen
import com.taskflow.app.ui.onboarding.TaskFlowOnboardingScreen
import com.taskflow.app.ui.profile.ProfileScreen
import com.taskflow.app.ui.project.ProjectFormScreen
import com.taskflow.app.ui.admin.AdminDashboardScreen
import com.taskflow.app.ui.admin.AdminProjectsScreen
import com.taskflow.app.ui.admin.AdminProjectDetailsScreen
import com.taskflow.app.ui.admin.AdminUsersListScreen
import com.taskflow.app.ui.admin.UserFormScreen
import com.taskflow.app.ui.admin.AdminStatsScreen
import com.taskflow.app.ui.manager.ManagerDashboardScreen
import com.taskflow.app.ui.manager.ManagerTasksListScreen
import com.taskflow.app.ui.manager.ManagerTaskDetailsScreen
import com.taskflow.app.ui.manager.ManagerTeamScreen
import com.taskflow.app.ui.manager.ManagerProjectsScreen
import com.taskflow.app.ui.manager.ManagerProjectDetailsScreen
import com.taskflow.app.ui.manager.ManagerStatsScreen
import com.taskflow.app.ui.manager.TaskFormScreen
import com.taskflow.app.ui.manager.AssignUsersScreen
import com.taskflow.app.ui.manager.AddTeamScreen
import com.taskflow.app.ui.manager.EvaluateUserScreen
import com.taskflow.app.ui.user.UserDashboardScreen
import com.taskflow.app.ui.user.UserTaskDetailsScreen
import com.taskflow.app.ui.user.UserHistoryScreen
import com.taskflow.app.ui.user.ObservationsScreen
import com.taskflow.app.ui.user.history.UserTaskHistoryScreen
import com.taskflow.app.ui.user.tasks.TaskExecutionScreen
import com.taskflow.app.ui.user.tasks.UserTasksScreen
import com.taskflow.app.ui.common.TaskFlowDataViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color


private val Context.dataStore by preferencesDataStore(name = "taskflow_preferences")
private val onboardingCompletedKey = booleanPreferencesKey("onboarding_completed")

@Composable
fun TaskFlowNavGraph() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var onboardingCompleted by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(Unit) {
        val preferences = context.dataStore.data.first()
        onboardingCompleted = preferences[onboardingCompletedKey] == true
    }

    if (onboardingCompleted == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    NavHost(
        navController = navController,
        startDestination = if (onboardingCompleted == true) {
            Routes.LOGIN
        } else {
            Routes.ONBOARDING
        }
    ) {
        composable(Routes.ONBOARDING) {
            TaskFlowOnboardingScreen(
                step = 0,
                onNext = { navController.navigate(Routes.ONBOARDING_TEAM) },
                onBack = {}
            )
        }

        composable(Routes.ONBOARDING_TEAM) {
            TaskFlowOnboardingScreen(
                step = 1,
                onNext = { navController.navigate(Routes.ONBOARDING_PROGRESS) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.ONBOARDING_PROGRESS) {
            TaskFlowOnboardingScreen(
                step = 2,
                onNext = {
                    coroutineScope.launch {
                        context.dataStore.edit { preferences ->
                            preferences[onboardingCompletedKey] = true
                        }

                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.ONBOARDING) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

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
            val viewModel: AuthViewModel = hiltViewModel()
            AdminDashboardScreen(
                nav = navController,
                onLogout = {
                    viewModel.logout {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }

        composable(Routes.MANAGER_DASHBOARD) {
            val viewModel: AuthViewModel = hiltViewModel()
            ManagerDashboardScreen(
                nav = navController,
                onLogout = {
                    viewModel.logout {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
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
            val viewModel: AuthViewModel = hiltViewModel()
            backStackEntry.arguments?.getLong(Routes.MANAGER_ID_ARG) ?: 0L
            ManagerDashboardScreen(
                nav = navController,
                onLogout = {
                    viewModel.logout {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }

        composable(Routes.USER_DASHBOARD) {
            val viewModel: AuthViewModel = hiltViewModel()
            UserDashboardScreen(
                nav = navController,
                onLogout = {
                    viewModel.logout {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.LOGIN) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }

        composable(
            route = Routes.USER_TASK_EXECUTION,
            arguments = listOf(
                navArgument(Routes.USER_TASK_ID_ARG) {
                    type = NavType.LongType
                }
            )
        ) {
            TaskExecutionScreen(nav = navController)
        }

        composable(Routes.USER_TASK_HISTORY) {
            UserTaskHistoryScreen(nav = navController)
        }

        composable(Routes.USER_TASKS) {
            UserTasksScreen(nav = navController)
        }

        composable(Routes.USER_OBSERVATIONS) {
            ObservationsScreen(nav = navController)
        }

        composable(Routes.ADMIN_PROJECTS) { AdminProjectsScreen(navController) }
        composable(Routes.ADMIN_PROJECT_CREATE) { ProjectFormScreen(navController, edit = false) }
        composable(Routes.ADMIN_PROJECT_EDIT) { ProjectFormScreen(navController, edit = true) }
        composable(Routes.ADMIN_PROJECT_DETAILS) { AdminProjectDetailsScreen(navController) }
        composable(Routes.ADMIN_USERS_LIST) { AdminUsersListScreen(navController) }
        composable(Routes.ADMIN_USER_CREATE) { UserFormScreen(navController, edit = false) }
        composable(Routes.ADMIN_USER_EDIT) { UserFormScreen(navController, edit = true) }
        composable(Routes.ADMIN_STATS) { AdminStatsScreen(navController) }
        composable(Routes.ADMIN_PROFILE) {
            ProfileScreen(navController, role = "A", accent = Color(0xFF2F7DF6))
        }

        composable(Routes.MANAGER_TASKS_LIST) { ManagerTasksListScreen(navController) }
        composable(Routes.MANAGER_TASK_CREATE) { TaskFormScreen(navController, edit = false) }
        composable(Routes.MANAGER_TASK_EDIT) { TaskFormScreen(navController, edit = true) }
        composable(Routes.MANAGER_TASK_DETAILS) { ManagerTaskDetailsScreen(navController) }
        composable(Routes.MANAGER_TEAM) { ManagerTeamScreen(navController) }
        composable(Routes.MANAGER_ADD_TEAM) { AddTeamScreen(navController) }
        composable(Routes.MANAGER_ASSIGN_USERS) { AssignUsersScreen(navController) }
        composable(Routes.MANAGER_EVALUATE_USER) { EvaluateUserScreen(navController) }
        composable(Routes.MANAGER_PROJECTS) { ManagerProjectsScreen(navController) }
        composable(Routes.MANAGER_PROJECT_DETAILS) { ManagerProjectDetailsScreen(navController) }
        composable(Routes.MANAGER_STATS) { ManagerStatsScreen(navController) }
        composable(Routes.MANAGER_PROFILE) {
            ProfileScreen(navController, role = "G", accent = Color(0xFF06C167))
        }

        composable(Routes.USER_TASK_DETAILS) { UserTaskDetailsScreen(navController) }
        composable(Routes.USER_HISTORY) { UserHistoryScreen(navController) }
        composable(Routes.USER_PROFILE) {
            ProfileScreen(navController, role = "U", accent = Color(0xFFFF6A00))
        }
    }
}

