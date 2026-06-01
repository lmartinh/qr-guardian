package com.lmartin.qrguardian.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = QrGuardianColors.Primary,
    secondary = QrGuardianColors.Secondary,
    background = QrGuardianColors.DarkBackground,
    surface = QrGuardianColors.DarkSurface,
    surfaceVariant = QrGuardianColors.DarkSurfaceVariant,
    onPrimary = Color(0xFF031018),
    onBackground = QrGuardianColors.DarkTextPrimary,
    onSurface = QrGuardianColors.DarkTextPrimary,
    onSurfaceVariant = QrGuardianColors.DarkTextSecondary,
    outline = QrGuardianColors.DarkBorder,
    error = QrGuardianColors.Danger,
)

private val LightColorScheme = lightColorScheme(
    primary = QrGuardianColors.Primary,
    secondary = QrGuardianColors.Secondary,
    background = QrGuardianColors.LightBackground,
    surface = QrGuardianColors.LightSurface,
    surfaceVariant = QrGuardianColors.LightSurfaceVariant,
    onPrimary = Color.White,
    onBackground = QrGuardianColors.LightTextPrimary,
    onSurface = QrGuardianColors.LightTextPrimary,
    onSurfaceVariant = QrGuardianColors.LightTextSecondary,
    outline = QrGuardianColors.LightBorder,
    error = QrGuardianColors.Danger,
)

@Composable
fun QrGuardianTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = QrGuardianTypography,
        shapes = QrGuardianShapes,
        content = content,
    )
}
