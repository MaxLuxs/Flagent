package flagent.frontend.components

import androidx.compose.runtime.Composable
import flagent.frontend.theme.FlagentTheme
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

/**
 * Material Icon component with smooth animations
 * 
 * Usage:
 * Icon("add") - renders add icon
 * Icon("search", size = 20.px, color = FlagentTheme.Primary, animated = true)
 */
@Composable
fun Icon(
    name: String,
    size: CSSSizeValue<CSSUnit.px> = 24.px,
    color: CSSColorValue? = null,
    animated: Boolean = false,
    attrs: (org.jetbrains.compose.web.attributes.AttrsBuilder<org.w3c.dom.HTMLElement>) -> Unit = {}
) {
    Span({
        classes("material-icons")
        style {
            fontSize(size)
            property("transition", "all 0.2s cubic-bezier(0.4, 0, 0.2, 1)")
            property("vertical-align", "middle")
            if (color != null) {
                color(color)
            }
        }
        if (animated) {
            onMouseEnter {
                val element = it.target as org.w3c.dom.HTMLElement
                element.style.transform = "scale(1.15) rotate(5deg)"
            }
            onMouseLeave {
                val element = it.target as org.w3c.dom.HTMLElement
                element.style.transform = "scale(1) rotate(0deg)"
            }
        }
        attrs.invoke(this.unsafeCast<org.jetbrains.compose.web.attributes.AttrsBuilder<org.w3c.dom.HTMLElement>>())
    }) {
        Text(name)
    }
}
