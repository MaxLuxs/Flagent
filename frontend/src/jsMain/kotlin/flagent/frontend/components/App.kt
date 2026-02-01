package flagent.frontend.components

import androidx.compose.runtime.*
import flagent.frontend.components.auth.LoginForm
import flagent.frontend.components.common.NotificationToast
import flagent.frontend.config.AppConfig
import flagent.frontend.navigation.Route
import flagent.frontend.navigation.Router
import flagent.frontend.state.BackendOnboardingState
import flagent.frontend.state.GlobalState
import flagent.frontend.state.LocalGlobalState
import flagent.frontend.theme.FlagentTheme
import flagent.frontend.util.AppLogger
import flagent.frontend.viewmodel.AuthViewModel
import flagent.frontend.viewmodel.TenantViewModel
import kotlinx.browser.localStorage
import kotlinx.browser.sessionStorage
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

private const val AUTH_TOKEN_KEY = "auth_token"
private const val AUTH_RETURN_URL_KEY = "auth_return_url"
private const val API_KEY_STORAGE_KEY = "api_key"

/**
 * Main App component
 */
@Composable
fun App() {
    val globalState = remember { GlobalState() }
    val authViewModel = remember { AuthViewModel() }
    val tenantViewModel = if (AppConfig.Features.enableMultiTenancy) remember { TenantViewModel() } else null

    LaunchedEffect(Unit) {
        Router.initialize()
        AppLogger.info("App", "Application initialized")
    }

    val requiresAuth = AppConfig.requiresAuth
    val route = Router.currentRoute

    // Redirect enterprise-only routes when edition is open-source; allow /tenants and /login when backend needs tenant (401 tenant message)
    LaunchedEffect(route) {
        when (route) {
            is Route.FlagMetrics -> if (!AppConfig.Features.enableMetrics) Router.navigateTo(Route.Home)
            is Route.FlagRollout -> if (!AppConfig.Features.enableSmartRollout) Router.navigateTo(Route.Home)
            is Route.FlagAnomalies -> if (!AppConfig.Features.enableAnomalyDetection) Router.navigateTo(Route.Home)
            is Route.Alerts -> if (!AppConfig.Features.enableAnomalyDetection) Router.navigateTo(Route.Home)
            is Route.Tenants -> if (!AppConfig.Features.enableMultiTenancy && !BackendOnboardingState.allowTenantsAndLogin) Router.navigateTo(Route.Home)
            else -> {}
        }
    }

    // Tenant-first onboarding: when logged in but no tenant (no api_key), redirect from "app" routes to /tenants?create=1
    val allowTenantsAndLogin = BackendOnboardingState.allowTenantsAndLogin
    LaunchedEffect(route, allowTenantsAndLogin) {
        val routeNeedsTenant = route is Route.Dashboard || route is Route.FlagsList || route is Route.Experiments ||
            route is Route.Analytics || route is Route.CreateFlag || route is Route.FlagDetail || route is Route.DebugConsole ||
            route is Route.FlagHistory || route is Route.FlagMetrics || route is Route.FlagRollout || route is Route.FlagAnomalies ||
            route is Route.Alerts
        if (!routeNeedsTenant) return@LaunchedEffect
        val token = localStorage.getItem(AUTH_TOKEN_KEY)?.takeIf { it.isNotBlank() }
        val apiKeyFromStorage = localStorage.getItem(API_KEY_STORAGE_KEY)?.takeIf { it.isNotBlank() }
        val apiKeyFromEnv = (js("window.ENV_API_KEY") as? String)?.takeIf { it.isNotBlank() }
        val hasNoApiKey = apiKeyFromStorage == null && apiKeyFromEnv == null
        val shouldRedirect = token != null && (AppConfig.Features.enableMultiTenancy || allowTenantsAndLogin) && hasNoApiKey
        if (shouldRedirect) Router.navigateToTenantsWithCreate()
    }

    // Redirect authenticated users from landing (/) to dashboard
    LaunchedEffect(requiresAuth, route) {
        if (route is Route.Home && requiresAuth) {
            val token = localStorage.getItem(AUTH_TOKEN_KEY)
            if (token != null) {
                Router.navigateTo(Route.Dashboard)
                return@LaunchedEffect
            }
        }
        if (!requiresAuth) return@LaunchedEffect
        if (route is Route.Login) return@LaunchedEffect
        val token = localStorage.getItem(AUTH_TOKEN_KEY)
        if (token != null) return@LaunchedEffect
        val returnPath = when (val r = route) {
            is Route.Home -> Route.Home.PATH
            is Route.FlagsList -> Route.FlagsList.PATH
            is Route.Dashboard -> Route.Dashboard.PATH
            is Route.Experiments -> Route.Experiments.PATH
            is Route.Analytics -> Route.Analytics.PATH
            is Route.CreateFlag -> Route.CreateFlag.PATH
            is Route.FlagDetail -> r.path()
            is Route.DebugConsole -> r.path()
            is Route.FlagHistory -> r.path()
            is Route.FlagMetrics -> r.path()
            is Route.FlagRollout -> r.path()
            is Route.FlagAnomalies -> r.path()
            is Route.Alerts -> Route.Alerts.PATH
            is Route.Settings -> Route.Settings.PATH
            is Route.Tenants -> Route.Tenants.PATH
            is Route.Login -> Route.Home.PATH
        }
        sessionStorage.setItem(AUTH_RETURN_URL_KEY, returnPath)
        Router.navigateTo(Route.Login)
    }

    CompositionLocalProvider(LocalGlobalState provides globalState) {
        when (route) {
            is Route.Login -> {
                LoginForm(
                    viewModel = authViewModel,
                    onSuccess = {
                        val returnUrl = sessionStorage.getItem(AUTH_RETURN_URL_KEY) ?: Route.Home.PATH
                        sessionStorage.removeItem(AUTH_RETURN_URL_KEY)
                        Router.navigateToPath(returnUrl)
                    }
                )
            }
            else -> {
                Div({
                    style {
                        fontFamily("-apple-system, BlinkMacSystemFont, 'Segoe UI', Helvetica, Arial, sans-serif")
                        minHeight(100.vh)
                        backgroundColor(FlagentTheme.BackgroundAlt)
                    }
                }) {
                    Navbar(authViewModel = authViewModel, tenantViewModel = tenantViewModel)
                    NotificationToast(
                        notifications = globalState.notifications,
                        onDismiss = { id -> globalState.removeNotification(id) }
                    )
                    Div({
                        style {
                            maxWidth(1200.px)
                            property("margin", "0 auto")
                            padding(20.px)
                            property("width", "100%")
                            property("box-sizing", "border-box")
                        }
                    }) {
                        when (route) {
                            is Route.FlagDetail, is Route.CreateFlag, is Route.DebugConsole, is Route.FlagHistory -> Breadcrumbs()
                            else -> {}
                        }
                        when (val r = Router.currentRoute) {
                            is Route.Home -> LandingPage()
                            is Route.FlagsList -> FlagsList()
                            is Route.Dashboard -> Dashboard()
                            is Route.Experiments -> flagent.frontend.components.experiments.ExperimentsPage()
                            is Route.Analytics -> flagent.frontend.components.analytics.AnalyticsPage()
                            is Route.FlagDetail -> FlagEditor(r.flagId)
                            is Route.CreateFlag -> FlagEditor(null)
                            is Route.DebugConsole -> DebugConsole(r.flagKey)
                            is Route.FlagHistory -> FlagHistory(r.flagId)
                            is Route.FlagMetrics -> flagent.frontend.components.metrics.MetricsDashboard(r.flagId)
                            is Route.FlagRollout -> flagent.frontend.components.rollout.SmartRolloutConfig(r.flagId)
                            is Route.FlagAnomalies -> flagent.frontend.components.anomaly.AnomalyAlertsList(r.flagId)
                            is Route.Alerts -> flagent.frontend.components.alerts.AlertsPage()
                            is Route.Settings -> flagent.frontend.components.settings.SettingsPage()
                            is Route.Tenants -> flagent.frontend.components.tenants.TenantsList()
                            is Route.Login -> {}
                        }
                    }
                }
            }
        }
    }
}
