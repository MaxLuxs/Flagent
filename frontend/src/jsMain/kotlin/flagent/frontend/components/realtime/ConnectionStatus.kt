package flagent.frontend.components.realtime

import androidx.compose.runtime.Composable
import flagent.frontend.components.Icon
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

/**
 * Connection Status indicator (Phase 3)
 */
@Composable
fun ConnectionStatus(isConnected: Boolean) {
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
            backgroundColor(if (isConnected) Color("rgba(16, 185, 129, 0.2)") else Color("rgba(239, 68, 68, 0.2)"))
            property("border", "1px solid " + (if (isConnected) "rgba(16, 185, 129, 0.4)" else "rgba(239, 68, 68, 0.4)"))
            borderRadius(20.px)
            property("backdrop-filter", "blur(8px)")
        }
    }) {
        Div({
            style {
                width(8.px)
                height(8.px)
                borderRadius(50.percent)
                backgroundColor(if (isConnected) Color("#10B981") else Color("#EF4444"))
                property("animation", if (isConnected) "none" else "pulse 2s infinite")
            }
        })
        
        Span({
            style {
                fontSize(12.px)
                fontWeight(500)
                color(if (isConnected) Color("#34D399") else Color("#F87171"))
            }
        }) {
            Text(if (isConnected) "Connected" else "Disconnected")
        }
    }
}
