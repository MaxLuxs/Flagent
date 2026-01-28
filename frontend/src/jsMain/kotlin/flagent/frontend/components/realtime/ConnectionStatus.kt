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
            backgroundColor(if (isConnected) Color("#DCFCE7") else Color("#FEE2E2"))
            borderRadius(20.px)
            property("box-shadow", "0 2px 8px rgba(0, 0, 0, 0.1)")
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
                color(if (isConnected) Color("#166534") else Color("#991B1B"))
            }
        }) {
            Text(if (isConnected) "Connected" else "Disconnected")
        }
    }
}
