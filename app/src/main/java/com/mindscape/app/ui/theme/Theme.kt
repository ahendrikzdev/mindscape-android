package com.mindscape.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val MindscapeColors = darkColorScheme(
    primary = NeonCyan,
    onPrimary = ForestBg,
    primaryContainer = ForestSurface,
    onPrimaryContainer = TextPrimary,
    secondary = NeonLime,
    onSecondary = ForestBg,
    secondaryContainer = ForestBorder,
    onSecondaryContainer = TextPrimary,
    tertiary = NeonEmerald,
    onTertiary = ForestBg,
    background = ForestBg,
    onBackground = TextPrimary,
    surface = ForestCard,
    onSurface = TextPrimary,
    surfaceVariant = ForestBorder,
    onSurfaceVariant = TextMuted,
    outline = ForestBorder,
    outlineVariant = ForestBorder,
    error = Rose,
    onError = TextPrimary,
)

@Composable
fun MindscapeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MindscapeColors,
        typography = MindscapeTypography,
        content = content,
    )
}
