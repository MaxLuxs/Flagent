package flagent.frontend.components.landing

import androidx.compose.runtime.*
import flagent.frontend.components.Icon
import flagent.frontend.config.AppConfig
import flagent.frontend.navigation.Route
import flagent.frontend.navigation.Router
import flagent.frontend.theme.FlagentTheme
import kotlinx.browser.document
import kotlinx.browser.window
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

/**
 * Marketing landing navbar: Product, Pricing, Blog, Docs, Sign in, GitHub.
 * Scroll-aware glass navbar, mobile hamburger menu.
 */
@Composable
fun LandingNavbar() {
    val scrolled = remember { mutableStateOf(false) }
    val menuOpen = remember { mutableStateOf(false) }
    val isMobile = remember { mutableStateOf(false) }
    val activeLink = remember { mutableStateOf<String?>(null) }
    val route = Router.currentRoute

    DisposableEffect(route) {
        val listener: (dynamic) -> Unit = {
            val scrollY = window.pageYOffset
            scrolled.value = scrollY > 50
            isMobile.value = (window.asDynamic().innerWidth as? Int ?: 1024) < 768
            activeLink.value = when (route) {
                is Route.Home -> if (scrollY < 500) "product" else null
                is Route.Pricing -> "pricing"
                else -> null
            }
        }
        window.addEventListener("scroll", listener)
        window.addEventListener("resize", listener)
        listener(null)
        onDispose {
            window.removeEventListener("scroll", listener)
            window.removeEventListener("resize", listener)
        }
    }

    Div(attrs = {
        style {
            position(Position.Fixed)
            property("top", "0")
            property("left", "0")
            property("right", "0")
            property("z-index", "100")
            padding(16.px, 24.px)
            property(
                "background",
                if (scrolled.value) "rgba(15, 23, 42, 0.95)" else "rgba(15, 23, 42, 0.7)"
            )
            property("backdrop-filter", "blur(12px)")
            property("-webkit-backdrop-filter", "blur(12px)")
            property("border-bottom", "1px solid rgba(255,255,255,0.06)")
            property("transition", "background 0.2s ease")
            display(DisplayStyle.Flex)
            flexDirection(FlexDirection.Column)
            property("font-family", "'DM Sans', -apple-system, BlinkMacSystemFont, sans-serif")
        }
    }) {
        Div(attrs = {
            style {
                display(DisplayStyle.Flex)
                justifyContent(JustifyContent.SpaceBetween)
                alignItems(AlignItems.Center)
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
                onClick { e ->
                    e.preventDefault()
                    if (route is Route.Home) {
                        document.getElementById("hero-section")?.asDynamic()?.scrollIntoView(js("({ behavior: 'smooth' })"))
                    } else {
                        Router.navigateTo(Route.Home)
                    }
                    menuOpen.value = false
                }
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
            if (isMobile.value) {
                Button(attrs = {
                    style {
                        padding(8.px)
                        backgroundColor(Color.transparent)
                        color(Color.white)
                        border(0.px)
                        cursor("pointer")
                        display(DisplayStyle.Flex)
                        alignItems(AlignItems.Center)
                        justifyContent(JustifyContent.Center)
                    }
                    onClick { menuOpen.value = !menuOpen.value }
                }) {
                    Icon(if (menuOpen.value) "close" else "menu", size = 24.px, color = Color.white)
                }
            } else {
                Div(attrs = {
                    style {
                        display(DisplayStyle.Flex)
                        alignItems(AlignItems.Center)
                        gap(32.px)
                    }
                }) {
                    NavLink("Product", Route.Home.PATH, activeLink.value == "product") {
                        if (route is Route.Home) {
                            document.getElementById("hero-section")?.asDynamic()?.scrollIntoView(js("({ behavior: 'smooth' })"))
                        } else {
                            Router.navigateTo(Route.Home)
                        }
                    }
                    NavLink("Pricing", Route.Pricing.PATH, activeLink.value == "pricing") {
                        Router.navigateTo(Route.Pricing)
                    }
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
        if (isMobile.value && menuOpen.value) {
            Div(attrs = {
                style {
                    paddingTop(16.px)
                    property("border-top", "1px solid rgba(255,255,255,0.08)")
                    display(DisplayStyle.Flex)
                    flexDirection(FlexDirection.Column)
                    gap(12.px)
                }
            }) {
                NavLinkMobile("Product", Route.Home.PATH, activeLink.value == "product") {
                    if (route is Route.Home) {
                        document.getElementById("hero-section")?.asDynamic()?.scrollIntoView(js("({ behavior: 'smooth' })"))
                    } else {
                        Router.navigateTo(Route.Home)
                    }
                    menuOpen.value = false
                }
                NavLinkMobile("Pricing", Route.Pricing.PATH, activeLink.value == "pricing") {
                    Router.navigateTo(Route.Pricing)
                    menuOpen.value = false
                }
                A(href = AppConfig.blogUrl, attrs = {
                    attr("target", "_blank")
                    attr("rel", "noopener noreferrer")
                    style {
                        color(Color("rgba(255,255,255,0.8)"))
                        textDecoration("none")
                        fontSize(14.px)
                        padding(12.px, 0.px)
                    }
                }) { Text("Blog") }
                A(href = AppConfig.docsUrl, attrs = {
                    attr("target", "_blank")
                    attr("rel", "noopener noreferrer")
                    style {
                        color(Color("rgba(255,255,255,0.8)"))
                        textDecoration("none")
                        fontSize(14.px)
                        padding(12.px, 0.px)
                    }
                }) { Text("Docs") }
                if (AppConfig.requiresAuth) {
                    Button(attrs = {
                        style {
                            padding(12.px, 20.px)
                            backgroundColor(Color.transparent)
                            color(Color.white)
                            border(1.px, LineStyle.Solid, Color("rgba(255,255,255,0.4)"))
                            borderRadius(8.px)
                            cursor("pointer")
                            fontSize(14.px)
                            textAlign("left")
                        }
                        onClick {
                            Router.navigateTo(Route.Login)
                            menuOpen.value = false
                        }
                    }) {
                        Text("Sign in")
                    }
                }
            }
        }
    }
}

@Composable
private fun NavLink(label: String, path: String, isActive: Boolean, onClick: () -> Unit) {
    val baseColor = if (isActive) "white" else "rgba(255,255,255,0.8)"
    val fontWeight = if (isActive) "600" else "500"
    A(href = path, attrs = {
        classes("nav-link-underline")
        style {
            color(Color(baseColor))
            textDecoration("none")
            fontSize(14.px)
            property("font-weight", fontWeight)
        }
        onClick { e -> e.preventDefault(); onClick() }
        onMouseEnter {
            val el = it.target as org.w3c.dom.HTMLElement
            if (!isActive) el.style.color = "white"
        }
        onMouseLeave {
            val el = it.target as org.w3c.dom.HTMLElement
            el.style.color = baseColor
        }
    }) { Text(label) }
}

@Composable
private fun NavLinkMobile(label: String, path: String, isActive: Boolean, onClick: () -> Unit) {
    val color = if (isActive) "white" else "rgba(255,255,255,0.8)"
    A(href = path, attrs = {
        style {
            color(Color(color))
            textDecoration("none")
            fontSize(14.px)
            padding(12.px, 0.px)
            property("font-weight", if (isActive) "600" else "500")
        }
        onClick { e -> e.preventDefault(); onClick() }
    }) { Text(label) }
}
