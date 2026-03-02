package flagent.frontend.theme

import androidx.compose.runtime.Composable
import flagent.frontend.state.ThemeMode
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div

/**
 * Shared gradient background layer used across marketing landing,
 * login screen and (optionally) app shell.
 *
 * Renders a fixed full-viewport gradient and then invokes [content] on top.
 */
@Composable
fun AppGradientBackground(content: @Composable () -> Unit) {
    // Backwards-compatible: always use dark hero gradient.
    GradientBackgroundLayer(FlagentTheme.GradientHero, content)
}

/**
 * Themed variant of [AppGradientBackground] that switches gradient
 * based on [ThemeMode] using FlagentTheme.heroGradient.
 *
 * Currently not wired anywhere by default to avoid visual regressions;
 * use explicitly where needed.
 */
@Composable
fun AppGradientBackground(
    mode: ThemeMode,
    content: @Composable () -> Unit
) {
    val gradient = FlagentTheme.heroGradient(mode)
    GradientBackgroundLayer(gradient, content)
}

@Composable
private fun GradientBackgroundLayer(
    background: String,
    content: @Composable () -> Unit
) {
    Div(attrs = {
        style {
            position(Position.Fixed)
            property("inset", "0")
            property("z-index", "0")
            property("pointer-events", "none")
            property("background", background)
            property("background-size", "400% 400%")
            property("animation", "morphGradient 20s ease infinite")
        }
    }) {}
    content()
}

