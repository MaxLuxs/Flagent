package flagent.frontend.components.realtime

import androidx.compose.runtime.Composable
import flagent.frontend.components.Icon
import flagent.frontend.state.LocalThemeMode
import flagent.frontend.theme.FlagentTheme
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

/**
 * Connection Status indicator (Phase 3)
 */
@Composable
fun ConnectionStatus(isConnected: Boolean) {
    val themeMode = LocalThemeMode.current
    Div({
        style {
            position(Position.Fixed)
            property("bottom", "20px")
            property("left", "20px")
            property("z-index", "9999")
            display(DisplayStyle.Flex)
            alignItems(AlignItems.Center)
            gap(8.px)
            padding(8.px, 16.px)
            backgroundColor(if (isConnected) FlagentTheme.successBg(themeMode) else FlagentTheme.errorBg(themeMode))
            property("border", if (isConnected) "1px solid var(--flagent-color-success)" else "1px solid var(--flagent-color-error)")
            borderRadius(20.px)
            property("backdrop-filter", "blur(8px)")
        }
    }) {
        Div({
            style {
                width(8.px)
                height(8.px)
                borderRadius(50.percent)
                backgroundColor(if (isConnected) FlagentTheme.Success else FlagentTheme.Error)
                property("animation", if (isConnected) "none" else "pulse 2s infinite")
            }
        })
        
        Span({
            style {
                fontSize(12.px)
                fontWeight(500)
                color(if (isConnected) FlagentTheme.Success else FlagentTheme.Error)
            }
        }) {
            Text(if (isConnected) "Connected" else "Disconnected")
        }
    }
}
