package com.taskflow.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF146C94),
    onPrimary = Color.White,
    secondary = Color(0xFF1F8A70),
    onSecondary = Color.White,
    tertiary = Color(0xFFF2B705),
    background = Color(0xFFF7F9FB),
    onBackground = Color(0xFF17212B),
    surface = Color.White,
    onSurface = Color(0xFF17212B),
    surfaceVariant = Color(0xFFE6EDF3),
    onSurfaceVariant = Color(0xFF4B5A67)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF6EC6E8),
    onPrimary = Color(0xFF073144),
    secondary = Color(0xFF6FD6B4),
    onSecondary = Color(0xFF073A2D),
    tertiary = Color(0xFFFFD95A),
    background = Color(0xFF111820),
    onBackground = Color(0xFFEAF0F6),
    surface = Color(0xFF18222C),
    onSurface = Color(0xFFEAF0F6),
    surfaceVariant = Color(0xFF263441),
    onSurfaceVariant = Color(0xFFB8C6D1)
)

@Composable
fun TaskFlowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content
    )
}
