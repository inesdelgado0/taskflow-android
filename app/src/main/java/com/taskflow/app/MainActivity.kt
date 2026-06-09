package com.taskflow.app

import android.Manifest
import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
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
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
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
    }.collectAsState(initial = LanguageManager.PORTUGUESE)
    val localizedConfiguration = rememberLocalizedConfiguration(baseContext, selectedLanguage)

    key(selectedLanguage) {
        CompositionLocalProvider(LocalConfiguration provides localizedConfiguration) {
            MaterialTheme {
                TaskFlowNavGraph()
            }
        }
    }
}

@Composable
private fun rememberLocalizedConfiguration(
    baseContext: Context,
    selectedLanguage: String
): Configuration {
    return remember(baseContext, selectedLanguage) {
        val safeLanguage = selectedLanguage.takeIf { it in LanguageManager.supportedLanguages }
            ?: LanguageManager.PORTUGUESE
        val configuration = Configuration(baseContext.resources.configuration)
        val locale = Locale.forLanguageTag(safeLanguage).takeIf { it.language.isNotBlank() }
            ?: Locale.forLanguageTag(LanguageManager.PORTUGUESE)

        Locale.setDefault(locale)
        configuration.setLocale(locale)
        configuration.setLocales(LocaleList(locale))
        @Suppress("DEPRECATION")
        baseContext.resources.updateConfiguration(configuration, baseContext.resources.displayMetrics)
        configuration
    }
}

