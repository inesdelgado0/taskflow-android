package com.taskflow.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import com.taskflow.app.ui.auth.LoginScreen
import com.taskflow.app.ui.onboarding.OnboardingScreen
import com.taskflow.app.ui.theme.TaskFlowTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private val ComponentActivity.dataStore by preferencesDataStore(name = "taskflow_preferences")

private enum class AppScreen {
    Onboarding,
    Login
}

class MainActivity : ComponentActivity() {

    private val onboardingCompletedKey = booleanPreferencesKey("onboarding_completed")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var onboardingCompleted by mutableStateOf<Boolean?>(null)
        var currentScreen by mutableStateOf(AppScreen.Onboarding)

        lifecycleScope.launch {
            val preferences = dataStore.data.first()
            onboardingCompleted = preferences[onboardingCompletedKey] == true
            currentScreen = if (preferences[onboardingCompletedKey] == true) {
                AppScreen.Login
            } else {
                AppScreen.Onboarding
            }
        }

        setContent {
            TaskFlowTheme {
                when (onboardingCompleted) {
                    false -> OnboardingScreen(
                        onLoginRequested = {
                            lifecycleScope.launch {
                                dataStore.edit { preferences ->
                                    preferences[onboardingCompletedKey] = true
                                }
                                onboardingCompleted = true
                                currentScreen = AppScreen.Login
                            }
                        }
                    )

                    true -> when (currentScreen) {
                        AppScreen.Onboarding -> OnboardingScreen(
                            onLoginRequested = { currentScreen = AppScreen.Login }
                        )

                        AppScreen.Login -> LoginScreen()
                    }

                    null -> LoginScreen(isLoading = true)
                }
            }
        }
    }
}
