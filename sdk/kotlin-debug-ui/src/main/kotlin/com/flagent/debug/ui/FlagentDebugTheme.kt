package com.flagent.debug.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.flagent.design.tokens.FlagentDesignTokens

/**
 * Material3 ColorScheme from Flagent design tokens (dark workspace style).
 */
private val FlagentColorScheme = darkColorScheme(
    primary = FlagentDesignTokens.Primary,
    onPrimary = Color.White,
    primaryContainer = FlagentDesignTokens.PrimaryDark,
    onPrimaryContainer = Color.White,
    secondary = FlagentDesignTokens.Secondary,
    onSecondary = Color.White,
    secondaryContainer = FlagentDesignTokens.SecondaryDark,
    onSecondaryContainer = Color.White,
    error = FlagentDesignTokens.Error,
    onError = Color.White,
    errorContainer = FlagentDesignTokens.Dark.errorBg,
    onErrorContainer = FlagentDesignTokens.Dark.errorText,
    background = FlagentDesignTokens.Dark.background,
    onBackground = FlagentDesignTokens.Dark.text,
    surface = FlagentDesignTokens.Dark.cardBg,
    onSurface = FlagentDesignTokens.Dark.text,
    surfaceVariant = FlagentDesignTokens.Dark.badgeBg,
    onSurfaceVariant = FlagentDesignTokens.Dark.textLight,
    outline = FlagentDesignTokens.Dark.border,
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
