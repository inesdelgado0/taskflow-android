package com.taskflow.app

import android.Manifest
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.os.ConfigurationCompat
import com.taskflow.app.ui.common.locale.LanguageManager
import com.taskflow.app.ui.navigation.TaskFlowNavGraph
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermission()

        setContent {
            LocalizedTaskFlowApp()
        }
    }

    private fun requestNotificationPermission() {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

@Composable
private fun LocalizedTaskFlowApp() {
    val baseContext = LocalContext.current
    val selectedLanguage by remember {
        LanguageManager.languageFlow(baseContext)
    }.collectAsState(initial = LanguageManager.SYSTEM)
    val localizedContext = rememberLocalizedContext(baseContext, selectedLanguage)

    CompositionLocalProvider(
        LocalContext provides localizedContext,
        LocalConfiguration provides localizedContext.resources.configuration
    ) {
        MaterialTheme {
            TaskFlowNavGraph()
        }
    }
}

@Composable
private fun rememberLocalizedContext(
    baseContext: Context,
    selectedLanguage: String
): Context {
    val currentConfiguration = LocalConfiguration.current
    return remember(baseContext, selectedLanguage, currentConfiguration) {
        if (selectedLanguage == LanguageManager.SYSTEM) {
            ConfigurationCompat.getLocales(currentConfiguration)[0]?.let(Locale::setDefault)
            baseContext
        } else {
            val locale = Locale.forLanguageTag(selectedLanguage)
            Locale.setDefault(locale)
            val configuration = Configuration(currentConfiguration)
            configuration.setLocale(locale)
            baseContext.createConfigurationContext(configuration)
        }
    }
}

