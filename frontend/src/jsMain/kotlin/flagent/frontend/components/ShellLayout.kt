package flagent.frontend.components

import androidx.compose.runtime.*
import flagent.frontend.api.ApiClient
import flagent.frontend.components.tenants.TenantSwitcher
import kotlinx.browser.document
import org.w3c.dom.events.Event
import org.w3c.dom.events.KeyboardEvent
import flagent.frontend.config.AppConfig
import flagent.frontend.navigation.Route
import flagent.frontend.navigation.Router
import flagent.frontend.components.Icon
import flagent.frontend.state.BackendOnboardingState
import flagent.frontend.state.LocalGlobalState
import flagent.frontend.state.LocalThemeMode
import flagent.frontend.state.ThemeMode
import flagent.frontend.state.ThemeState
import flagent.frontend.theme.FlagentTheme
import flagent.frontend.viewmodel.AnomalyViewModel
import flagent.frontend.viewmodel.AuthViewModel
import flagent.frontend.viewmodel.TenantViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

/**
 * Shell layout: top navbar (logo, tenant, user) + left sidebar + main content.
 * Unleash-style layout with compact navigation.
 */
@Composable
fun ShellLayout(
    authViewModel: AuthViewModel? = null,
    tenantViewModel: TenantViewModel? = null,
    content: @Composable (TenantViewModel?) -> Unit
) {
    val themeMode = LocalThemeMode.current
    val scope = rememberCoroutineScope()
    val globalState = LocalGlobalState.current
    val anomalyViewModel = if (AppConfig.Features.enableAnomalyDetection) {
        remember { AnomalyViewModel() }
    } else null

    LaunchedEffect(tenantViewModel) {
        tenantViewModel?.loadTenants()
    }
    LaunchedEffect(tenantViewModel?.currentTenant) {
        tenantViewModel?.currentTenant?.let { globalState.currentTenant = it }
    }
    LaunchedEffect(Unit) {
        if (anomalyViewModel != null) {
            while (true) {
                anomalyViewModel.loadUnresolvedAlerts()
                delay(30000)
            }
        }
    }

    val commandBarOpen = remember { mutableStateOf(false) }
    DisposableEffect(Unit) {
        val handler: (KeyboardEvent) -> Unit = { event ->
            if ((event.key == "k" || event.key == "K") && (event.metaKey || event.ctrlKey)) {
                event.preventDefault()
                commandBarOpen.value = true
            }
        }
        val wrappedHandler: (Event) -> Unit = { handler(it.unsafeCast<KeyboardEvent>()) }
        document.addEventListener("keydown", wrappedHandler)
        onDispose { document.removeEventListener("keydown", wrappedHandler) }
    }

    CommandBar(isOpen = commandBarOpen.value, onClose = { commandBarOpen.value = false })

    Div(attrs = {
        style {
            display(DisplayStyle.Flex)
            flexDirection(FlexDirection.Column)
            height(100.vh)
            property("max-height", "100vh")
            overflow("hidden")
            backgroundColor(FlagentTheme.contentBg(themeMode))
        }
    }) {
        Div(attrs = {
            style {
                flexShrink(0)
            }
        }) {
            TopNavbar(
                authViewModel = authViewModel,
                tenantViewModel = tenantViewModel,
                anomalyViewModel = anomalyViewModel,
                onOpenCommandBar = { commandBarOpen.value = true }
            )
        }
        Div({
            style {
                display(DisplayStyle.Flex)
                flex(1)
                property("min-height", "0")
            }
        }) {
            Sidebar(anomalyViewModel = anomalyViewModel)
            Div(attrs = {
                style {
                    flex(1)
                    display(DisplayStyle.Flex)
                    flexDirection(FlexDirection.Column)
                    property("min-height", "0")
                }
            }) {
                Div(attrs = {
                    style {
                        flex(1)
                        overflow("auto")
                        padding(20.px)
                        property("box-sizing", "border-box")
                        backgroundColor(FlagentTheme.contentBg(themeMode))
                    }
                }) {
                    Div({
                        style {
                            maxWidth(1400.px)
                            property("margin", "0 auto")
                            property("width", "100%")
                            property("box-sizing", "border-box")
                        }
                    }) {
                        content(tenantViewModel)
                    }
                }
                ShellFooter()
            }
        }
    }
}

@Composable
private fun TopNavbar(
    authViewModel: AuthViewModel?,
    tenantViewModel: TenantViewModel?,
    anomalyViewModel: AnomalyViewModel?,
    onOpenCommandBar: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val themeMode = LocalThemeMode.current
    val version = remember { mutableStateOf("v0.1.6") }
    LaunchedEffect(Unit) {
        try {
            val info = ApiClient.getInfo()
            version.value = "v${info.version}"
        } catch (_: Exception) { /* fallback to default */ }
    }
    Div(attrs = {
        style {
            property("background", "linear-gradient(135deg, rgba(14,165,233,0.25) 0%, rgba(15,23,42,0.85) 50%, rgba(15,23,42,0.9) 100%)")
            property("backdrop-filter", "blur(16px)")
            property("-webkit-backdrop-filter", "blur(16px)")
            property("border-bottom", "1px solid rgba(255,255,255,0.06)")
            property("box-shadow", "0 1px 0 rgba(255,255,255,0.04) inset")
            color(Color.white)
            padding(14.px, 20.px)
            property("font-family", "'DM Sans', -apple-system, BlinkMacSystemFont, sans-serif")
        }
    }) {
        Div(attrs = {
            style {
                display(DisplayStyle.Flex)
                justifyContent(JustifyContent.SpaceBetween)
                alignItems(AlignItems.Center)
                maxWidth(1400.px)
                property("margin", "0 auto")
                gap(12.px)
            }
        }) {
            A(href = Route.Home.PATH, attrs = {
                style {
                    textDecoration("none")
                    color(Color.white)
                    display(DisplayStyle.Flex)
                    alignItems(AlignItems.Center)
                    gap(10.px)
                    property("transition", "opacity 0.2s ease")
                }
                onClick { e -> e.preventDefault(); Router.navigateTo(Route.Home) }
                onMouseEnter { (it.target as org.w3c.dom.HTMLElement).style.opacity = "0.9" }
                onMouseLeave { (it.target as org.w3c.dom.HTMLElement).style.opacity = "1" }
            }) {
                Div(attrs = {
                    style {
                        width(38.px)
                        height(38.px)
                        borderRadius(12.px)
                        property("background", "linear-gradient(135deg, ${FlagentTheme.Primary} 0%, ${FlagentTheme.Secondary} 100%)")
                        display(DisplayStyle.Flex)
                        alignItems(AlignItems.Center)
                        justifyContent(JustifyContent.Center)
                        property("box-shadow", "0 4px 12px rgba(14, 165, 233, 0.3)")
                    }
                }) {
                    Icon(name = "flag", size = 20.px, color = Color.white, animated = true)
                }
                Span(attrs = { style { fontWeight("600"); fontSize(18.px) } }) { Text("Flagent") }
                Span(attrs = {
                    style {
                        fontSize(11.px)
                        opacity(0.9)
                        padding(3.px, 8.px)
                        property("background", "rgba(255,255,255,0.12)")
                        borderRadius(8.px)
                        fontWeight("500")
                    }
                }) { Text(version.value) }
            }
            Div(attrs = {
                style {
                    display(DisplayStyle.Flex)
                    alignItems(AlignItems.Center)
                    gap(4.px)
                }
            }) {
                if (AppConfig.Features.enableMultiTenancy && tenantViewModel != null) {
                    TenantSwitcher(viewModel = tenantViewModel)
                }
                Button({
                    onClick { onOpenCommandBar() }
                    style {
                        padding(8.px, 12.px)
                        backgroundColor(Color.transparent)
                        color(Color("rgba(255,255,255,0.85)"))
                        border(0.px)
                        borderRadius(8.px)
                        cursor("pointer")
                        fontSize(13.px)
                        display(DisplayStyle.Flex)
                        alignItems(AlignItems.Center)
                        gap(6.px)
                        property("transition", "all 0.2s ease")
                    }
                    onMouseEnter {
                        val el = it.target as org.w3c.dom.HTMLElement
                        el.style.backgroundColor = "rgba(255,255,255,0.1)"
                        el.style.color = "white"
                    }
                    onMouseLeave {
                        val el = it.target as org.w3c.dom.HTMLElement
                        el.style.backgroundColor = "transparent"
                        el.style.color = "rgba(255,255,255,0.85)"
                    }
                }) {
                    Icon("search", size = 18.px, color = FlagentTheme.Background)
                    Span(attrs = { style { opacity(0.9) } }) { Text("⌘K") }
                }
                NavbarLink(AppConfig.docsUrl, "menu_book", flagent.frontend.i18n.LocalizedStrings.docs, external = true)
                A(href = AppConfig.blogUrl, attrs = {
                    attr("target", "_blank")
                    attr("rel", "noopener noreferrer")
                    style {
                        textDecoration("none")
                        color(Color("rgba(255,255,255,0.85)"))
                        fontSize(13.px)
                        display(DisplayStyle.Flex)
                        alignItems(AlignItems.Center)
                        gap(6.px)
                        padding(8.px, 12.px)
                        borderRadius(8.px)
                        property("transition", "all 0.2s ease")
                    }
                    onMouseEnter {
                        val el = it.target as org.w3c.dom.HTMLElement
                        el.style.backgroundColor = "rgba(255,255,255,0.1)"
                        el.style.color = "white"
                    }
                    onMouseLeave {
                        val el = it.target as org.w3c.dom.HTMLElement
                        el.style.backgroundColor = "transparent"
                        el.style.color = "rgba(255,255,255,0.85)"
                    }
                }) { Text("Blog") }
                NavbarLink(AppConfig.githubUrl, "code", "GitHub", external = true)
                Button({
                    onClick { ThemeState.toggle() }
                    style {
                        padding(8.px, 12.px)
                        backgroundColor(Color.transparent)
                        color(Color("rgba(255,255,255,0.9)"))
                        border(0.px)
                        borderRadius(8.px)
                        cursor("pointer")
                        fontSize(18.px)
                        display(DisplayStyle.Flex)
                        alignItems(AlignItems.Center)
                        property("transition", "all 0.2s ease")
                    }
                    onMouseEnter { (it.target as org.w3c.dom.HTMLElement).style.backgroundColor = "rgba(255,255,255,0.1)" }
                    onMouseLeave { (it.target as org.w3c.dom.HTMLElement).style.backgroundColor = "transparent" }
                }) {
                    Icon(if (themeMode == ThemeMode.Dark) "light_mode" else "dark_mode", size = 20.px, color = FlagentTheme.Background)
                }
                if ((AppConfig.requiresAuth || BackendOnboardingState.allowTenantsAndLogin) && authViewModel != null) {
                    if (authViewModel.isAuthenticated) {
                        Span({ style { color(Color("rgba(255,255,255,0.9)")); fontSize(13.px); padding(0.px, 8.px) } }) {
                            Text(authViewModel.currentUser?.name ?: authViewModel.currentUser?.email ?: "User")
                        }
                        Button({
                            onClick {
                                scope.launch {
                                    runCatching { flagent.frontend.api.ApiClient.ssoLogout() }
                                    authViewModel.logout()
                                    Router.navigateTo(Route.Login)
                                }
                            }
                            style {
                                padding(8.px, 16.px)
                                backgroundColor(Color.transparent)
                                color(Color.white)
                                border(1.px, LineStyle.Solid, Color("rgba(255,255,255,0.4)"))
                                borderRadius(8.px)
                                cursor("pointer")
                                fontSize(13.px)
                                fontWeight("600")
                                property("transition", "all 0.2s ease")
                            }
                            onMouseEnter {
                                val el = it.target as org.w3c.dom.HTMLElement
                                el.style.backgroundColor = "rgba(255,255,255,0.1)"
                                el.style.borderColor = "rgba(255,255,255,0.6)"
                            }
                            onMouseLeave {
                                val el = it.target as org.w3c.dom.HTMLElement
                                el.style.backgroundColor = "transparent"
                                el.style.borderColor = "rgba(255,255,255,0.4)"
                            }
                        }) { Text("Logout") }
                    } else {
                        A(href = Route.Login.PATH, attrs = {
                            style {
                                textDecoration("none")
                                color(Color.white)
                                fontSize(13.px)
                                display(DisplayStyle.Flex)
                                alignItems(AlignItems.Center)
                                gap(6.px)
                                padding(8.px, 12.px)
                                borderRadius(8.px)
                                property("transition", "all 0.2s ease")
                            }
                            onClick { e -> e.preventDefault(); Router.navigateTo(Route.Login) }
                            onMouseEnter { (it.target as org.w3c.dom.HTMLElement).style.backgroundColor = "rgba(255,255,255,0.1)" }
                            onMouseLeave { (it.target as org.w3c.dom.HTMLElement).style.backgroundColor = "transparent" }
                        }) {
                            Icon("login", size = 18.px, color = FlagentTheme.Background)
                            Text("Login")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ShellFooter() {
    val themeMode = LocalThemeMode.current
    val version = remember { mutableStateOf("v0.1.6") }
    LaunchedEffect(Unit) {
        try {
            val info = ApiClient.getInfo()
            version.value = "v${info.version}"
        } catch (_: Exception) { /* fallback */ }
    }
    val borderColor = if (themeMode == ThemeMode.Dark) "rgba(255,255,255,0.06)" else "rgba(0,0,0,0.08)"
    val textColor = if (themeMode == ThemeMode.Dark) "rgba(255,255,255,0.5)" else "rgba(0,0,0,0.5)"
    val linkColor = if (themeMode == ThemeMode.Dark) "rgba(255,255,255,0.65)" else "rgba(0,0,0,0.6)"
    Div(attrs = {
        style {
            padding(12.px, 20.px)
            property("border-top", "1px solid $borderColor")
            display(DisplayStyle.Flex)
            justifyContent(JustifyContent.SpaceBetween)
            alignItems(AlignItems.Center)
            property("flex-wrap", "wrap")
            gap(12.px)
            fontSize(12.px)
            property("font-family", "'DM Sans', -apple-system, BlinkMacSystemFont, sans-serif")
        }
    }) {
        Span(attrs = { style { color(Color(textColor)) } }) { Text("Flagent ${version.value}") }
        Div(attrs = {
            style {
                display(DisplayStyle.Flex)
                alignItems(AlignItems.Center)
                gap(16.px)
            }
        }) {
            A(href = AppConfig.docsUrl, attrs = {
                attr("target", "_blank")
                attr("rel", "noopener noreferrer")
                style {
                    color(Color(linkColor))
                    textDecoration("none")
                    property("transition", "color 0.15s")
                }
                onMouseEnter { (it.target as org.w3c.dom.HTMLElement).style.color = FlagentTheme.Primary.toString() }
                onMouseLeave { (it.target as org.w3c.dom.HTMLElement).style.color = linkColor }
            }) { Text("Docs") }
            A(href = AppConfig.githubUrl, attrs = {
                attr("target", "_blank")
                attr("rel", "noopener noreferrer")
                style {
                    color(Color(linkColor))
                    textDecoration("none")
                    property("transition", "color 0.15s")
                }
                onMouseEnter { (it.target as org.w3c.dom.HTMLElement).style.color = FlagentTheme.Primary.toString() }
                onMouseLeave { (it.target as org.w3c.dom.HTMLElement).style.color = linkColor }
            }) { Text("GitHub") }
            Span(attrs = { style { color(Color(textColor)) } }) {
                Text("© ${js("new Date().getFullYear()") as Int} Flagent")
            }
        }
    }
}

@Composable
private fun NavbarLink(url: String, icon: String, label: String, external: Boolean = false) {
    A(href = url, attrs = {
        if (external) {
            attr("target", "_blank")
            attr("rel", "noopener noreferrer")
        }
        style {
            textDecoration("none")
            color(Color("rgba(255,255,255,0.85)"))
            fontSize(13.px)
            display(DisplayStyle.Flex)
            alignItems(AlignItems.Center)
            gap(6.px)
            padding(8.px, 12.px)
            borderRadius(8.px)
            property("transition", "all 0.2s ease")
        }
        onMouseEnter {
            val el = it.target as org.w3c.dom.HTMLElement
            el.style.backgroundColor = "rgba(255,255,255,0.1)"
            el.style.color = "white"
        }
        onMouseLeave {
            val el = it.target as org.w3c.dom.HTMLElement
            el.style.backgroundColor = "transparent"
            el.style.color = "rgba(255,255,255,0.85)"
        }
    }) {
        Icon(icon, size = 18.px, color = FlagentTheme.Background)
        Text(label)
    }
}

@Composable
private fun Sidebar(anomalyViewModel: AnomalyViewModel?) {
    val route = Router.currentRoute
    val themeMode = LocalThemeMode.current
    Div(attrs = {
        style {
            width(220.px)
            flexShrink(0)
            property("overflow-y", "auto")
            property("overflow-x", "hidden")
            backgroundColor(FlagentTheme.sidebarBg(themeMode))
            property("border-right", "1px solid ${FlagentTheme.cardBorder(themeMode)}")
            padding(12.px, 0.px)
            property("backdrop-filter", "blur(8px)")
        }
    }) {
        SidebarLink(themeMode, "Dashboard", "dashboard", Route.Dashboard.PATH, route is Route.Dashboard)
        SidebarLink(themeMode, "Flags", "flag", Route.FlagsList.PATH, route is Route.FlagsList)
        SidebarLink(themeMode, "Experiments", "science", Route.Experiments.PATH, route is Route.Experiments)
        SidebarLink(themeMode, "Analytics", "analytics", Route.Analytics.PATH, route is Route.Analytics)
        if (AppConfig.Features.enableCrashAnalytics) {
            SidebarLink(themeMode, "Crash", "bug_report", Route.Crash.PATH, route is Route.Crash)
        }
        if (AppConfig.Features.enableAnomalyDetection && anomalyViewModel != null) {
            Div({ style { position(Position.Relative) } }) {
                SidebarLink(themeMode, "Alerts", "notifications", Route.Alerts.PATH, route is Route.Alerts)
                if (anomalyViewModel.alerts.isNotEmpty()) {
                    Span({
                        style {
                            position(Position.Absolute)
                            property("top", "8px")
                            property("right", "12px")
                            backgroundColor(Color("#EF4444"))
                            color(Color.white)
                            fontSize(10.px)
                            fontWeight("bold")
                            padding(2.px, 5.px)
                            borderRadius(8.px)
                            minWidth(16.px)
                            textAlign("center")
                        }
                    }) { Text(anomalyViewModel.alerts.size.toString()) }
                }
            }
        }
        Div(attrs = {
            style {
                property("border-top", "1px solid ${FlagentTheme.cardBorder(themeMode)}")
                marginTop(8.px)
                paddingTop(8.px)
            }
        }) {
            SidebarLink(themeMode, flagent.frontend.i18n.LocalizedStrings.debugConsole, "bug_report", "/debug", route is Route.DebugConsole)
            if (AppConfig.Features.enableMultiTenancy) {
                SidebarLink(themeMode, "Tenants", "business", Route.Tenants.PATH, route is Route.Tenants)
            }
            SidebarLink(themeMode, "Settings", "settings", Route.Settings.PATH, route is Route.Settings)
        }
    }
}

@Composable
private fun SidebarLink(themeMode: ThemeMode, label: String, icon: String, path: String, isActive: Boolean) {
    A(href = path, attrs = {
        style {
            display(DisplayStyle.Flex)
            alignItems(AlignItems.Center)
            gap(10.px)
            padding(10.px, 16.px)
            textDecoration("none")
            color(if (isActive) FlagentTheme.PrimaryLight else FlagentTheme.textLight(themeMode))
            fontWeight(if (isActive) "600" else "500")
            fontSize(14.px)
            property("border-left", if (isActive) "3px solid ${FlagentTheme.Primary}" else "3px solid transparent")
            property("background", if (isActive) "rgba(14, 165, 233, 0.15)" else "transparent")
            property("transition", "all 0.15s")
        }
        onMouseEnter {
            val el = it.target as org.w3c.dom.HTMLElement
            if (!isActive) el.style.backgroundColor = "rgba(255,255,255,0.06)"
        }
        onMouseLeave {
            val el = it.target as org.w3c.dom.HTMLElement
            if (!isActive) el.style.backgroundColor = "transparent"
        }
        onClick { e -> e.preventDefault(); Router.navigateToPath(path) }
    }) {
        Icon(icon, size = 20.px, color = if (isActive) FlagentTheme.PrimaryLight else FlagentTheme.textLight(themeMode))
        Text(label)
    }
}
