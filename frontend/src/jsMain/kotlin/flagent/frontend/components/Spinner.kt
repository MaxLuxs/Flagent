package flagent.frontend.components

import androidx.compose.runtime.Composable
import flagent.frontend.state.LocalThemeMode
import flagent.frontend.theme.FlagentTheme
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

/**
 * Spinner component - loading indicator
 */
@Composable
fun Spinner() {
    val themeMode = LocalThemeMode.current
    Div({
        style {
            display(DisplayStyle.Flex)
            justifyContent(JustifyContent.Center)
            alignItems(AlignItems.Center)
            padding(20.px)
        }
    }) {
        Div({
            style {
                width(40.px)
                height(40.px)
                border {
                    width(4.px)
                    style(LineStyle.Solid)
                    color(FlagentTheme.inputBg(themeMode))
                }
                property("border-top-color", FlagentTheme.Primary.toString())
                borderRadius(50.percent)
                property("animation", "spin 1s linear infinite")
            }
        }) {}
    }

    Style {
        """
        @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
        }
        """.trimIndent()
    }
}
