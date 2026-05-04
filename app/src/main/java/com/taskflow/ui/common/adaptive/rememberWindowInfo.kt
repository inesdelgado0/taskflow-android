package com.taskflow.app.ui.common.adaptive

import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration

@Composable
fun rememberWindowInfo(windowSizeClass: WindowSizeClass): WindowInfo {
    val configuration = LocalConfiguration.current

    val widthType = when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> WindowType.COMPACT
        WindowWidthSizeClass.Medium -> WindowType.MEDIUM
        WindowWidthSizeClass.Expanded -> WindowType.EXPANDED
        else -> WindowType.COMPACT
    }

    val heightType = when (windowSizeClass.heightSizeClass) {
        WindowHeightSizeClass.Compact -> WindowType.COMPACT
        WindowHeightSizeClass.Medium -> WindowType.MEDIUM
        WindowHeightSizeClass.Expanded -> WindowType.EXPANDED
        else -> WindowType.COMPACT
    }

    return WindowInfo(
        widthType = widthType,
        heightType = heightType,
        isLandscape = configuration.screenWidthDp > configuration.screenHeightDp
    )
}