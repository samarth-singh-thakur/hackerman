package com.hackerman.ghostwatcher.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MrRobotDark = darkColorScheme(
    primary = Color(0xFF00FF7F),
    onPrimary = Color.Black,
    background = Color(0xFF060807),
    surface = Color(0xFF0A0F0B),
    onSurface = Color(0xFF8CFFBE)
)

private val MrRobotLight = lightColorScheme(
    primary = Color(0xFF006B32),
    onPrimary = Color.White,
    background = Color(0xFFE9FFF1),
    surface = Color(0xFFDDFCE7),
    onSurface = Color(0xFF003D1D)
)

@Composable
fun GhostWatcherTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) MrRobotDark else MrRobotLight,
        typography = Typography,
        content = content
    )
}
