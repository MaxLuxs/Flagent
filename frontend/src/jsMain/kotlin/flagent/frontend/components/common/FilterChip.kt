package flagent.frontend.components.common

import androidx.compose.runtime.Composable
import flagent.frontend.state.ThemeMode
import flagent.frontend.theme.FlagentTheme
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

/**
 * Reusable filter chip/button with consistent min width and theme-aware colors.
 * Used in Flags list and other filter sections for active/inactive state.
 */
@Composable
fun FilterChip(
    themeMode: ThemeMode,
    label: String,
    active: Boolean,
    onClick: () -> Unit,
    minWidth: CSSSizeValue<CSSUnit.px> = 130.px
) {
    Span({
        style {
            padding(6.px, 12.px)
            minWidth(minWidth)
            property("flex-shrink", "0")
            property("white-space", "nowrap")
            display(DisplayStyle.InlineBlock)
            textAlign("center")
            backgroundColor(if (active) FlagentTheme.Primary else FlagentTheme.inputBg(themeMode))
            color(if (active) Color.white else FlagentTheme.text(themeMode))
            border {
                width(1.px)
                style(LineStyle.Solid)
                color(if (active) FlagentTheme.Primary else FlagentTheme.cardBorder(themeMode))
            }
            borderRadius(16.px)
            fontSize(12.px)
            cursor("pointer")
        }
        onClick { onClick() }
    }) {
        Text(label)
    }
}
