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
import com.taskflow.app.ui.admin.users.AdminUsersScreen
import com.taskflow.app.ui.admin.users.AdminUsersViewModel
import com.taskflow.app.ui.auth.AuthViewModel
import com.taskflow.app.ui.auth.LoginScreen
import com.taskflow.app.ui.auth.RegisterScreen
import com.taskflow.app.ui.manager.tasks.ManagerTasksScreen
import com.taskflow.app.ui.manager.tasks.ManagerTasksViewModel
import com.taskflow.app.ui.onboarding.OnboardingScreen
import com.taskflow.app.ui.user.UserDashboardScreen
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier


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
            OnboardingScreen(
                onLoginRequested = {
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
                }
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

