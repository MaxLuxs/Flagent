package flagent.frontend

import org.jetbrains.compose.web.renderComposable
import flagent.frontend.components.App

fun main() {
    try {
        renderComposable(rootElementId = "root") {
            App()
        }
    } catch (e: Throwable) {
        console.error("Failed to render app:", e)
    }
}
