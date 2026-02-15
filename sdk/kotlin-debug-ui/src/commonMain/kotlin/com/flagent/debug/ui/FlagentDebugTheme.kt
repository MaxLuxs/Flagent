package com.flagent.debug.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Dark theme colors for debug UI (shared across all platforms). */
private val FlagentColorScheme = darkColorScheme(
    primary = Color(0xFF6C5CE7),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF5B4FCF),
    onPrimaryContainer = Color.White,
    secondary = Color(0xFF00B894),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF00A085),
    onSecondaryContainer = Color.White,
    error = Color(0xFFE74C3C),
    onError = Color.White,
    errorContainer = Color(0xFF2D1F1F),
    onErrorContainer = Color(0xFFE74C3C),
    background = Color(0xFF1A1D21),
    onBackground = Color(0xFFE4E6EB),
    surface = Color(0xFF23262B),
    onSurface = Color(0xFFE4E6EB),
    surfaceVariant = Color(0xFF2C3036),
    onSurfaceVariant = Color(0xFFB0B4BA),
    outline = Color(0xFF3D4249),
)

/**
 * Wraps content with MaterialTheme using Flagent design tokens.
 */
@Composable
internal fun FlagentDebugTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = FlagentColorScheme,
        content = content
    )
}
