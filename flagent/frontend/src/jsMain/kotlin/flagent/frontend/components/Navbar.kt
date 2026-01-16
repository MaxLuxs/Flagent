package flagent.frontend.components

import androidx.compose.runtime.Composable
import flagent.frontend.components.Icon
import flagent.frontend.theme.FlagentTheme
import org.jetbrains.compose.web.attributes.ATarget
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

/**
 * Navbar component - top navigation bar
 */
@Composable
fun Navbar() {
    Div({
        style {
            property("background", "linear-gradient(135deg, ${FlagentTheme.Primary} 0%, ${FlagentTheme.PrimaryDark} 100%)")
            color(FlagentTheme.Background)
            padding(18.px, 20.px)
            property("border-bottom", "2px solid ${FlagentTheme.PrimaryDark.toString()}")
            property("box-shadow", "0 4px 12px rgba(14, 165, 233, 0.3)")
            property("transition", "box-shadow 0.3s ease")
        }
    }) {
        Div({
            style {
                display(DisplayStyle.Flex)
                justifyContent(JustifyContent.SpaceBetween)
                alignItems(AlignItems.Center)
                maxWidth(1200.px)
                property("margin", "0 auto")
                property("flex-wrap", "wrap")
                gap(10.px)
            }
        }) {
            Div({
                style {
                    display(DisplayStyle.Flex)
                    alignItems(AlignItems.Center)
                    gap(12.px)
                }
            }) {
                A(href = "/", attrs = {
                    style {
                        textDecoration("none")
                        color(FlagentTheme.Background)
                        display(DisplayStyle.Flex)
                        alignItems(AlignItems.Center)
                        gap(8.px)
                        property("transition", "opacity 0.2s")
                    }
                    onMouseEnter {
                        (it.target as org.w3c.dom.HTMLElement).style.opacity = "0.9"
                    }
                    onMouseLeave {
                        (it.target as org.w3c.dom.HTMLElement).style.opacity = "1.0"
                    }
                }) {
                    Icon(
                        name = "flag",
                        size = 28.px,
                        color = FlagentTheme.Background,
                        animated = true
                    )
                    H3({
                        style {
                            margin(0.px)
                            fontWeight("bold")
                            color(FlagentTheme.Background)
                            fontSize(22.px)
                        }
                    }) {
                        Text("Flagent")
                    }
                }
                Span({
                    style {
                        fontSize(11.px)
                        color(FlagentTheme.Background)
                        opacity(0.85)
                        padding(4.px, 8.px)
                        property("background-color", "rgba(255, 255, 255, 0.2)")
                        borderRadius(12.px)
                        fontWeight("500")
                    }
                }) {
                    Text("v1.0.0")
                }
            }
            Div({
                style {
                    display(DisplayStyle.Flex)
                    gap(25.px)
                    alignItems(AlignItems.Center)
                }
            }) {
                A(href = "/docs", attrs = {
                    attr("target", "_blank")
                    style {
                        textDecoration("none")
                        color(FlagentTheme.Background)
                        fontWeight("600")
                        fontSize(14.px)
                        display(DisplayStyle.Flex)
                        alignItems(AlignItems.Center)
                        gap(6.px)
                        padding(8.px, 12.px)
                        borderRadius(6.px)
                        property("transition", "background-color 0.2s")
                    }
                    onMouseEnter {
                        (it.target as org.w3c.dom.HTMLElement).style.backgroundColor = "rgba(255, 255, 255, 0.15)"
                    }
                    onMouseLeave {
                        (it.target as org.w3c.dom.HTMLElement).style.backgroundColor = "transparent"
                    }
                }) {
                    Icon(
                        name = "api",
                        size = 18.px,
                        color = FlagentTheme.Background
                    )
                    Text("API")
                }
                A(href = "https://github.com/MaxLuxs/Flagent", attrs = {
                    attr("target", "_blank")
                    style {
                        textDecoration("none")
                        color(FlagentTheme.Background)
                        fontWeight("600")
                        fontSize(14.px)
                        display(DisplayStyle.Flex)
                        alignItems(AlignItems.Center)
                        gap(6.px)
                        padding(8.px, 12.px)
                        borderRadius(6.px)
                        property("transition", "background-color 0.2s")
                    }
                    onMouseEnter {
                        (it.target as org.w3c.dom.HTMLElement).style.backgroundColor = "rgba(255, 255, 255, 0.15)"
                    }
                    onMouseLeave {
                        (it.target as org.w3c.dom.HTMLElement).style.backgroundColor = "transparent"
                    }
                }) {
                    Icon(
                        name = "menu_book",
                        size = 18.px,
                        color = FlagentTheme.Background
                    )
                    Text("Docs")
                }
            }
        }
    }
}
