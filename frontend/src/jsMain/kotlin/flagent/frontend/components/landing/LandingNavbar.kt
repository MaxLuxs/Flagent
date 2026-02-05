package flagent.frontend.components.landing

import androidx.compose.runtime.Composable
import flagent.frontend.components.Icon
import flagent.frontend.config.AppConfig
import flagent.frontend.navigation.Route
import flagent.frontend.navigation.Router
import flagent.frontend.theme.FlagentTheme
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

/**
 * Marketing landing navbar: Product, Pricing, Blog, Docs, Sign in, GitHub.
 * Glass-style navbar over hero.
 */
@Composable
fun LandingNavbar() {
    Div(attrs = {
        style {
            position(Position.Fixed)
            property("top", "0")
            property("left", "0")
            property("right", "0")
            property("z-index", "100")
            padding(16.px, 24.px)
            property("background", "rgba(15, 23, 42, 0.7)")
            property("backdrop-filter", "blur(12px)")
            property("border-bottom", "1px solid rgba(255,255,255,0.06)")
            display(DisplayStyle.Flex)
            justifyContent(JustifyContent.SpaceBetween)
            alignItems(AlignItems.Center)
            property("font-family", "'DM Sans', -apple-system, BlinkMacSystemFont, sans-serif")
        }
    }) {
        Div(attrs = {
            style {
                display(DisplayStyle.Flex)
                alignItems(AlignItems.Center)
                gap(32.px)
            }
        }) {
            A(href = Route.Home.PATH, attrs = {
                style {
                    display(DisplayStyle.Flex)
                    alignItems(AlignItems.Center)
                    gap(8.px)
                    textDecoration("none")
                    color(Color.white)
                    fontWeight(600)
                    fontSize(18.px)
                }
                onClick { e -> e.preventDefault(); Router.navigateTo(Route.Home) }
            }) {
                Div(attrs = {
                    style {
                        width(36.px)
                        height(36.px)
                        borderRadius(10.px)
                        property(
                            "background",
                            "linear-gradient(135deg, ${FlagentTheme.Primary} 0%, ${FlagentTheme.Secondary} 100%)"
                        )
                        display(DisplayStyle.Flex)
                        alignItems(AlignItems.Center)
                        justifyContent(JustifyContent.Center)
                    }
                }) {
                    Icon("flag", size = 20.px, color = Color.white)
                }
                Text("Flagent")
            }
            NavLink("Product", Route.Home.PATH) { Router.navigateTo(Route.Home) }
            NavLink("Pricing", Route.Pricing.PATH) { Router.navigateTo(Route.Pricing) }
            A(href = AppConfig.blogUrl, attrs = {
                attr("target", "_blank")
                attr("rel", "noopener noreferrer")
                style {
                    color(Color("rgba(255,255,255,0.8)"))
                    textDecoration("none")
                    fontSize(14.px)
                    fontWeight(500)
                }
            }) { Text("Blog") }
            A(href = AppConfig.docsUrl, attrs = {
                attr("target", "_blank")
                attr("rel", "noopener noreferrer")
                style {
                    color(Color("rgba(255,255,255,0.8)"))
                    textDecoration("none")
                    fontSize(14.px)
                    fontWeight(500)
                }
            }) { Text("Docs") }
        }
        Div(attrs = {
            style {
                display(DisplayStyle.Flex)
                alignItems(AlignItems.Center)
                gap(16.px)
            }
        }) {
            if (AppConfig.requiresAuth) {
                Button(attrs = {
                    style {
                        padding(10.px, 20.px)
                        backgroundColor(Color.transparent)
                        color(Color.white)
                        border(1.px, LineStyle.Solid, Color("rgba(255,255,255,0.4)"))
                        borderRadius(8.px)
                        cursor("pointer")
                        fontSize(14.px)
                        fontWeight(500)
                    }
                    onClick { Router.navigateTo(Route.Login) }
                    onMouseEnter {
                        (it.target as org.w3c.dom.HTMLElement).style.backgroundColor = "rgba(255,255,255,0.1)"
                    }
                    onMouseLeave {
                        (it.target as org.w3c.dom.HTMLElement).style.backgroundColor = "transparent"
                    }
                }) {
                    Text("Sign in")
                }
            }
            A(href = AppConfig.githubUrl, attrs = {
                attr("target", "_blank")
                attr("rel", "noopener noreferrer")
                style {
                    display(DisplayStyle.Flex)
                    alignItems(AlignItems.Center)
                    gap(6.px)
                    padding(10.px, 16.px)
                    property(
                        "background",
                        "linear-gradient(135deg, ${FlagentTheme.Primary} 0%, ${FlagentTheme.PrimaryDark} 100%)"
                    )
                    color(Color.white)
                    textDecoration("none")
                    borderRadius(8.px)
                    fontSize(14.px)
                    fontWeight(500)
                }
                onMouseEnter {
                    val el = it.target as org.w3c.dom.HTMLElement
                    el.style.transform = "translateY(-1px)"
                }
                onMouseLeave {
                    (it.target as org.w3c.dom.HTMLElement).style.transform = "translateY(0)"
                }
            }) {
                Icon("code", size = 18.px, color = Color.white)
                Text("GitHub")
            }
        }
    }
}

@Composable
private fun NavLink(label: String, path: String, onClick: () -> Unit) {
    A(href = path, attrs = {
        style {
            color(Color("rgba(255,255,255,0.8)"))
            textDecoration("none")
            fontSize(14.px)
            fontWeight(500)
        }
        onClick { e -> e.preventDefault(); onClick() }
        onMouseEnter {
            (it.target as org.w3c.dom.HTMLElement).style.color = "white"
        }
        onMouseLeave {
            (it.target as org.w3c.dom.HTMLElement).style.color = "rgba(255,255,255,0.8)"
        }
    }) { Text(label) }
}
