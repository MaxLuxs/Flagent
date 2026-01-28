package flagent.frontend.components

import androidx.compose.runtime.*
import flagent.frontend.api.ApiClient
import flagent.frontend.components.Icon
import flagent.frontend.config.AppConfig
import flagent.frontend.navigation.Route
import flagent.frontend.navigation.Router
import flagent.frontend.theme.FlagentTheme
import flagent.frontend.viewmodel.AnomalyViewModel
import flagent.frontend.viewmodel.AuthViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.attributes.ATarget
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

/**
 * Navbar component - top navigation bar
 */
@Composable
fun Navbar(authViewModel: AuthViewModel? = null) {
    val scope = rememberCoroutineScope()
    val anomalyViewModel = if (AppConfig.Features.enableAnomalyDetection) {
        remember { AnomalyViewModel() }
    } else null
    
    LaunchedEffect(Unit) {
        if (anomalyViewModel != null) {
            while (true) {
                anomalyViewModel.loadUnresolvedAlerts()
                delay(30000) // Refresh every 30 seconds
            }
        }
    }
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
                    gap(15.px)
                    alignItems(AlignItems.Center)
                }
            }) {
                NavLink("Dashboard", "dashboard", Route.Dashboard.PATH)
                NavLink("Flags", "flag", Route.Home.PATH)
                NavLink("Experiments", "science", Route.Experiments.PATH)
                NavLink("Analytics", "analytics", Route.Analytics.PATH)
                
                // Alerts with badge
                if (AppConfig.Features.enableAnomalyDetection && anomalyViewModel != null) {
                    Div({
                        style {
                            position(Position.Relative)
                        }
                    }) {
                        A(href = Route.Alerts.PATH, attrs = {
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
                            onClick { e ->
                                e.preventDefault()
                                Router.navigateTo(Route.Alerts)
                            }
                        }) {
                            Icon("notifications", size = 18.px, color = FlagentTheme.Background)
                            Text("Alerts")
                        }
                        
                        // Badge with count
                        if (anomalyViewModel.alerts.isNotEmpty()) {
                            Span({
                                style {
                                    position(Position.Absolute)
                                    property("top", "-4px")
                                    property("right", "-4px")
                                    backgroundColor(Color("#EF4444"))
                                    color(FlagentTheme.Background)
                                    fontSize(11.px)
                                    fontWeight("bold")
                                    padding(2.px, 6.px)
                                    borderRadius(10.px)
                                    minWidth(18.px)
                                    textAlign("center")
                                }
                            }) {
                                Text(anomalyViewModel.alerts.size.toString())
                            }
                        }
                    }
                }
                
                NavLink("Settings", "settings", Route.Settings.PATH)

                if (AppConfig.requiresAuth && authViewModel != null) {
                    if (authViewModel.isAuthenticated) {
                        Span({
                            style {
                                color(FlagentTheme.Background)
                                fontSize(14.px)
                                padding(0.px, 8.px)
                            }
                        }) {
                            Text(authViewModel.currentUser?.name ?: authViewModel.currentUser?.email ?: "User")
                        }
                        Button({
                            onClick {
                                scope.launch {
                                    runCatching { ApiClient.ssoLogout() }
                                    authViewModel.logout()
                                    Router.navigateTo(Route.Login)
                                }
                            }
                            style {
                                padding(8.px, 12.px)
                                backgroundColor(Color("transparent"))
                                color(FlagentTheme.Background)
                                border(1.px, LineStyle.Solid, FlagentTheme.Background)
                                borderRadius(6.px)
                                cursor("pointer")
                                fontSize(14.px)
                                fontWeight("600")
                            }
                        }) {
                            Text("Logout")
                        }
                    } else {
                        A(href = Route.Login.PATH, attrs = {
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
                            onClick { e ->
                                e.preventDefault()
                                Router.navigateTo(Route.Login)
                            }
                        }) {
                            Icon("login", size = 18.px, color = FlagentTheme.Background)
                            Text("Login")
                        }
                    }
                }
                
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
                        name = "menu_book",
                        size = 18.px,
                        color = FlagentTheme.Background
                    )
                    Text(flagent.frontend.i18n.LocalizedStrings.docs)
                }
            }
        }
    }
}

@Composable
private fun NavLink(label: String, icon: String, path: String) {
    A(href = path, attrs = {
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
        onClick { e ->
            e.preventDefault()
            Router.navigateToPath(path)
        }
    }) {
        Icon(icon, size = 18.px, color = FlagentTheme.Background)
        Text(label)
    }
}
