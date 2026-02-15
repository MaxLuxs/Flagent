package com.flagent.sample.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.flagent.design.tokens.FlagentDesignTokens

private val LightColorScheme = lightColorScheme(
    primary = FlagentDesignTokens.Primary,
    onPrimary = Color.White,
    primaryContainer = FlagentDesignTokens.PrimaryLight,
    onPrimaryContainer = FlagentDesignTokens.Light.text,
    secondary = FlagentDesignTokens.Secondary,
    onSecondary = Color.White,
    secondaryContainer = FlagentDesignTokens.SecondaryLight,
    onSecondaryContainer = FlagentDesignTokens.Light.text,
    tertiary = FlagentDesignTokens.Accent,
    onTertiary = Color.White,
    tertiaryContainer = FlagentDesignTokens.Light.warningBg,
    onTertiaryContainer = FlagentDesignTokens.Light.text,
    error = FlagentDesignTokens.Error,
    onError = Color.White,
    errorContainer = FlagentDesignTokens.Light.errorBg,
    onErrorContainer = FlagentDesignTokens.Light.errorText,
    background = FlagentDesignTokens.Light.background,
    onBackground = FlagentDesignTokens.Light.text,
    surface = FlagentDesignTokens.Light.cardBg,
    onSurface = FlagentDesignTokens.Light.text,
    surfaceVariant = FlagentDesignTokens.Light.backgroundDark,
    onSurfaceVariant = FlagentDesignTokens.Light.textLight,
    outline = FlagentDesignTokens.Light.border
)

private val DarkColorScheme = darkColorScheme(
    primary = FlagentDesignTokens.PrimaryLight,
    onPrimary = FlagentDesignTokens.Dark.background,
    primaryContainer = FlagentDesignTokens.PrimaryDark,
    onPrimaryContainer = Color.White,
    secondary = FlagentDesignTokens.SecondaryLight,
    onSecondary = FlagentDesignTokens.Dark.background,
    secondaryContainer = FlagentDesignTokens.SecondaryDark,
    onSecondaryContainer = Color.White,
    tertiary = FlagentDesignTokens.Accent,
    onTertiary = FlagentDesignTokens.Dark.background,
    tertiaryContainer = FlagentDesignTokens.Dark.warningBg,
    onTertiaryContainer = FlagentDesignTokens.Dark.text,
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
    outline = FlagentDesignTokens.Dark.border
)

@Composable
fun FlagentTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        content = content
    )
}
