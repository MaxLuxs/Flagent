package flagent.frontend

import org.jetbrains.compose.web.renderComposable
import flagent.frontend.components.App

fun main() {
    renderComposable(rootElementId = "root") {
        App()
    }
}
