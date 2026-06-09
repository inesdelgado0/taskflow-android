package com.taskflow.app.ui.common

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration

data class WindowInfo(
    val isLandscape: Boolean
)

@Composable
fun rememberWindowInfo(): WindowInfo {
    val configuration = LocalConfiguration.current
    return WindowInfo(
        isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    )
}
