package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkNavyPurpleScheme = darkColorScheme(
    primary = PurpleNeon,
    onPrimary = Color.White,
    primaryContainer = PurpleDark,
    onPrimaryContainer = VioletSoft,
    secondary = PurpleLight,
    onSecondary = Color.White,
    secondaryContainer = Navy800,
    onSecondaryContainer = VioletSoft,
    tertiary = CyanGlow,
    onTertiary = Navy950,
    background = Navy950,
    onBackground = TextPrimary,
    surface = Navy900,
    onSurface = TextPrimary,
    surfaceVariant = Navy800,
    onSurfaceVariant = TextSecondary,
    outline = Navy700,
    outlineVariant = Navy600,
    error = CoralError,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkNavyPurpleScheme,
        typography = Typography,
        content = content
    )
}
