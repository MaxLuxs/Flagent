package flagent.frontend.components.common

import androidx.compose.runtime.Composable
import flagent.frontend.components.Icon
import flagent.frontend.state.LocalThemeMode
import flagent.frontend.state.ThemeMode
import flagent.frontend.theme.FlagentTheme
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

@Composable
fun EmptyState(
    icon: String = "inbox",
    title: String,
    description: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    val themeMode = LocalThemeMode.current
    Div(attrs = {
        style {
            padding(48.px, 24.px)
            property("background", if (themeMode == ThemeMode.Dark) "rgba(255,255,255,0.04)" else FlagentTheme.BackgroundAlt.toString())
            property("border", "1px solid ${FlagentTheme.cardBorder(themeMode)}")
            property("backdrop-filter", "blur(12px)")
            borderRadius(16.px)
            property("box-shadow", "0 4px 24px ${FlagentTheme.Shadow}")
            property("transition", "all 0.3s ease")
            maxWidth(420.px)
            property("margin", "0 auto")
        }
        onMouseEnter {
            (it.target as org.w3c.dom.HTMLElement).style.setProperty("box-shadow", "0 8px 32px rgba(0,0,0,0.12)")
        }
        onMouseLeave {
            (it.target as org.w3c.dom.HTMLElement).style.setProperty("box-shadow", "0 4px 24px rgba(0,0,0,0.08)")
        }
    }) {
        Div(attrs = {
            style {
                display(DisplayStyle.Flex)
                flexDirection(FlexDirection.Column)
                alignItems(AlignItems.Center)
                justifyContent(JustifyContent.Center)
                textAlign("center")
            }
        }) {
            Div(attrs = {
                style {
                    width(80.px)
                    height(80.px)
                    borderRadius(20.px)
                    property("background", "linear-gradient(135deg, rgba(14, 165, 233, 0.2) 0%, rgba(20, 184, 166, 0.15) 100%)")
                    display(DisplayStyle.Flex)
                    alignItems(AlignItems.Center)
                    justifyContent(JustifyContent.Center)
                    marginBottom(20.px)
                }
            }) {
                Icon(icon, size = 40.px, color = FlagentTheme.PrimaryLight)
            }
            H3(attrs = {
                style {
                    fontSize(20.px)
                    fontWeight(600)
                    color(FlagentTheme.text(themeMode))
                    margin(0.px)
                    marginBottom(8.px)
                }
            }) {
                Text(title)
            }
            description?.let {
                P(attrs = {
                    style {
                        fontSize(14.px)
                        color(FlagentTheme.textLight(themeMode))
                        margin(0.px)
                        marginBottom(24.px)
                        maxWidth(360.px)
                        lineHeight("1.5")
                    }
                }) {
                    Text(it)
                }
            }
            if (actionLabel != null && onAction != null) {
                Button(attrs = {
                    style {
                        padding(12.px, 24.px)
                        property(
                            "background",
                            "linear-gradient(135deg, ${FlagentTheme.Primary} 0%, ${FlagentTheme.PrimaryDark} 100%)"
                        )
                        color(Color.white)
                        border(0.px)
                        borderRadius(10.px)
                        cursor("pointer")
                        fontSize(14.px)
                        fontWeight(600)
                        property("box-shadow", "0 4px 12px rgba(14, 165, 233, 0.35)")
                        property("transition", "all 0.2s ease")
                    }
                    onClick { onAction() }
                    onMouseEnter {
                        val el = it.target as org.w3c.dom.HTMLElement
                        el.style.transform = "translateY(-2px)"
                        el.style.setProperty("box-shadow", "0 6px 20px rgba(14, 165, 233, 0.45)")
                    }
                    onMouseLeave {
                        val el = it.target as org.w3c.dom.HTMLElement
                        el.style.transform = "translateY(0)"
                        el.style.setProperty("box-shadow", "0 4px 12px rgba(14, 165, 233, 0.35)")
                    }
                }) {
                    Text(actionLabel)
                }
            }
        }
    }
}
