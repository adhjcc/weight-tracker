package com.weighttracker.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val Primary = Color(0xFF4CAF50)
val PrimaryDark = Color(0xFF388E3C)
val PrimaryLight = Color(0xFFC8E6C9)
val Secondary = Color(0xFF8BC34A)
val Background = Color(0xFFF5F5F5)
val Surface = Color(0xFFFFFFFF)
val OnPrimary = Color.White
val OnSecondary = Color.White
val OnBackground = Color(0xFF212121)
val OnSurface = Color(0xFF212121)
val TextSecondary = Color(0xFF757575)

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryLight,
    onPrimaryContainer = PrimaryDark,
    secondary = Secondary,
    onSecondary = OnSecondary,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = Color(0xFFE0E0E0),
    onSurfaceVariant = TextSecondary
)

@Composable
fun WeightTrackerTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
