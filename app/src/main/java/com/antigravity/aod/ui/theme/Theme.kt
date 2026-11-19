package com.antigravity.aod.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val SlateDarkColorScheme = darkColorScheme(
    primary = RyPrimary,
    onPrimary = RyTextPrimary,
    primaryContainer = RyPrimaryContainer,
    onPrimaryContainer = RyOnPrimaryContainer,
    secondary = RyAccent,
    onSecondary = RyBackground,
    secondaryContainer = RySurfaceElevated,
    onSecondaryContainer = RyTextPrimary,
    tertiary = RyScallopBadge,
    background = RyBackground,
    onBackground = RyTextPrimary,
    surface = RySurface,
    onSurface = RyTextPrimary,
    surfaceVariant = RySurfaceVariant,
    onSurfaceVariant = RyTextSecondary,
    outline = RyBorder
)

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun AodTheme(
    content: @Composable () -> Unit
) {
    // We enforce our signature Slate Dark MD3 aesthetic matching the user's design reference
    MaterialTheme(
        colorScheme = SlateDarkColorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}
