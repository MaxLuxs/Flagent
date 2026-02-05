package flagent.frontend.components

import androidx.compose.runtime.*
import flagent.frontend.api.ApiClient
import flagent.frontend.components.tenants.TenantSwitcher
import flagent.frontend.config.AppConfig
import flagent.frontend.navigation.Route
import flagent.frontend.navigation.Router
import flagent.frontend.state.BackendOnboardingState
import flagent.frontend.state.LocalGlobalState
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

    Div(attrs = {
        style {
            display(DisplayStyle.Flex)
            flexDirection(FlexDirection.Column)
            minHeight(100.vh)
            backgroundColor(FlagentTheme.WorkspaceBackground)
        }
    }) {
        TopNavbar(authViewModel = authViewModel, tenantViewModel = tenantViewModel, anomalyViewModel = anomalyViewModel)
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
                    overflow("auto")
                    padding(20.px)
                    property("box-sizing", "border-box")
                    backgroundColor(FlagentTheme.WorkspaceContentBg)
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
        }
    }
}

@Composable
private fun TopNavbar(
    authViewModel: AuthViewModel?,
    tenantViewModel: TenantViewModel?,
    anomalyViewModel: AnomalyViewModel?
) {
    val scope = rememberCoroutineScope()
    val version = remember { mutableStateOf("v0.1.4") }
    LaunchedEffect(Unit) {
        try {
            val info = ApiClient.getInfo()
            version.value = "v${info.version}"
        } catch (_: Exception) { /* fallback to default */ }
    }
    Div(attrs = {
        style {
            property("background", "linear-gradient(135deg, ${FlagentTheme.Primary} 0%, ${FlagentTheme.PrimaryDark} 100%)")
            property("backdrop-filter", "blur(12px)")
            property("-webkit-backdrop-filter", "blur(12px)")
            color(Color.white)
            padding(12.px, 16.px)
            property("border-bottom", "1px solid rgba(255,255,255,0.1)")
            property("box-shadow", "0 2px 12px rgba(0,0,0,0.15)")
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
                    gap(8.px)
                }
                onClick { e -> e.preventDefault(); Router.navigateTo(Route.Home) }
            }) {
                Div(attrs = {
                    style {
                        width(36.px)
                        height(36.px)
                        borderRadius(10.px)
                        property("background", "rgba(255,255,255,0.2)")
                        display(DisplayStyle.Flex)
                        alignItems(AlignItems.Center)
                        justifyContent(JustifyContent.Center)
                    }
                }) {
                    Icon(name = "flag", size = 20.px, color = FlagentTheme.Background, animated = true)
                }
                Span(attrs = { style { fontWeight("bold"); fontSize(18.px) } }) { Text("Flagent") }
                Span(attrs = { style { fontSize(11.px); opacity(0.9); padding(2.px, 6.px); property("background", "rgba(255,255,255,0.2)"); borderRadius(8.px) } }) { Text(version.value) }
            }
            Div(attrs = {
                style {
                    display(DisplayStyle.Flex)
                    alignItems(AlignItems.Center)
                    gap(12.px)
                }
            }) {
                if (AppConfig.Features.enableMultiTenancy && tenantViewModel != null) {
                    TenantSwitcher(viewModel = tenantViewModel)
                }
                A(href = AppConfig.docsUrl, attrs = {
                    attr("target", "_blank")
                    attr("rel", "noopener noreferrer")
                    style {
                        textDecoration("none")
                        color(Color.white)
                        fontSize(13.px)
                        display(DisplayStyle.Flex)
                        alignItems(AlignItems.Center)
                        gap(6.px)
                        padding(6.px, 10.px)
                        borderRadius(6.px)
                    }
                    onMouseEnter { (it.target as org.w3c.dom.HTMLElement).style.backgroundColor = "rgba(255,255,255,0.15)" }
                    onMouseLeave { (it.target as org.w3c.dom.HTMLElement).style.backgroundColor = "transparent" }
                }) {
                    Icon("menu_book", size = 18.px, color = FlagentTheme.Background)
                    Text(flagent.frontend.i18n.LocalizedStrings.docs)
                }
                A(href = AppConfig.githubUrl, attrs = {
                    attr("target", "_blank")
                    attr("rel", "noopener noreferrer")
                    style {
                        textDecoration("none")
                        color(Color.white)
                        fontSize(13.px)
                        display(DisplayStyle.Flex)
                        alignItems(AlignItems.Center)
                        gap(6.px)
                        padding(6.px, 10.px)
                        borderRadius(6.px)
                    }
                    onMouseEnter { (it.target as org.w3c.dom.HTMLElement).style.backgroundColor = "rgba(255,255,255,0.15)" }
                    onMouseLeave { (it.target as org.w3c.dom.HTMLElement).style.backgroundColor = "transparent" }
                }) {
                    Icon("code", size = 18.px, color = FlagentTheme.Background)
                    Text("GitHub")
                }
                if ((AppConfig.requiresAuth || BackendOnboardingState.allowTenantsAndLogin) && authViewModel != null) {
                    if (authViewModel.isAuthenticated) {
                        Span({ style { color(Color.white); fontSize(13.px); padding(0.px, 6.px) } }) {
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
                                padding(6.px, 12.px)
                                backgroundColor(Color.transparent)
                                color(Color.white)
                                border(1.px, LineStyle.Solid, FlagentTheme.Background)
                                borderRadius(6.px)
                                cursor("pointer")
                                fontSize(13.px)
                                fontWeight("600")
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
                                padding(6.px, 10.px)
                                borderRadius(6.px)
                            }
                            onClick { e -> e.preventDefault(); Router.navigateTo(Route.Login) }
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
private fun Sidebar(anomalyViewModel: AnomalyViewModel?) {
    val route = Router.currentRoute
    Div(attrs = {
        style {
            width(220.px)
            flexShrink(0)
            backgroundColor(FlagentTheme.WorkspaceSidebarBg)
            property("border-right", "1px solid ${FlagentTheme.WorkspaceBorder}")
            padding(12.px, 0.px)
            property("backdrop-filter", "blur(8px)")
        }
    }) {
        SidebarLink("Dashboard", "dashboard", Route.Dashboard.PATH, route is Route.Dashboard)
        SidebarLink("Flags", "flag", Route.FlagsList.PATH, route is Route.FlagsList)
        SidebarLink("Experiments", "science", Route.Experiments.PATH, route is Route.Experiments)
        SidebarLink("Analytics", "analytics", Route.Analytics.PATH, route is Route.Analytics)
        if (AppConfig.Features.enableCrashAnalytics) {
            SidebarLink("Crash", "bug_report", Route.Crash.PATH, route is Route.Crash)
        }
        if (AppConfig.Features.enableAnomalyDetection && anomalyViewModel != null) {
            Div({ style { position(Position.Relative) } }) {
                SidebarLink("Alerts", "notifications", Route.Alerts.PATH, route is Route.Alerts)
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
                property("border-top", "1px solid ${FlagentTheme.WorkspaceBorder}")
                marginTop(8.px)
                paddingTop(8.px)
            }
        }) {
            if (AppConfig.Features.enableMultiTenancy) {
                SidebarLink("Tenants", "business", Route.Tenants.PATH, route is Route.Tenants)
            }
            SidebarLink("Settings", "settings", Route.Settings.PATH, route is Route.Settings)
        }
    }
}

@Composable
private fun SidebarLink(label: String, icon: String, path: String, isActive: Boolean) {
    A(href = path, attrs = {
        style {
            display(DisplayStyle.Flex)
            alignItems(AlignItems.Center)
            gap(10.px)
            padding(10.px, 16.px)
            textDecoration("none")
            color(if (isActive) FlagentTheme.PrimaryLight else FlagentTheme.WorkspaceTextLight)
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
        Icon(icon, size = 20.px, color = if (isActive) FlagentTheme.PrimaryLight else FlagentTheme.WorkspaceTextLight)
        Text(label)
    }
}
