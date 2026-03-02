package com.beekeeplog.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = NeonGreen,
    onPrimary = Black,
    secondary = NeonYellow,
    onSecondary = Black,
    tertiary = NeonBlue,
    background = Black,
    onBackground = White,
    surface = Surface12,
    onSurface = White,
    surfaceVariant = Surface1E,
    onSurfaceVariant = Grey60,
    error = NeonRed,
    onError = Black
)

/** Dark-only Material 3 theme with OLED brutalist palette. */
@Composable
fun BeekeepLogTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = BeekeepLogTypography,
        content = content
    )
}
