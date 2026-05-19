package com.taskflow.app.ui.common

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

data class WindowInfo(
    val isLandscape: Boolean
)

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun rememberWindowInfo(): WindowInfo {
    val activity = LocalContext.current.findActivity()
    val windowSizeClass = calculateWindowSizeClass(activity)
    val isLandscape = windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact

    return remember(isLandscape) {
        WindowInfo(isLandscape = isLandscape)
    }
}

private tailrec fun Context.findActivity(): Activity =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> error("Activity context is required to calculate window size class.")
    }

