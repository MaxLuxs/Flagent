package flagent.frontend.components

import androidx.compose.runtime.*
import flagent.frontend.components.auth.LoginForm
import flagent.frontend.components.common.NotificationToast
import flagent.frontend.config.AppConfig
import flagent.frontend.navigation.Route
import flagent.frontend.navigation.Router
import flagent.frontend.state.GlobalState
import flagent.frontend.state.LocalGlobalState
import flagent.frontend.theme.FlagentTheme
import flagent.frontend.util.AppLogger
import flagent.frontend.viewmodel.AuthViewModel
import kotlinx.browser.localStorage
import kotlinx.browser.sessionStorage
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

private const val AUTH_TOKEN_KEY = "auth_token"
private const val AUTH_RETURN_URL_KEY = "auth_return_url"

/**
 * Main App component
 */
@Composable
fun App() {
    val globalState = remember { GlobalState() }
    val authViewModel = remember { AuthViewModel() }

    LaunchedEffect(Unit) {
        Router.initialize()
        AppLogger.info("App", "Application initialized")
    }

    val requiresAuth = AppConfig.requiresAuth
    val route = Router.currentRoute

    LaunchedEffect(requiresAuth, route) {
        if (!requiresAuth) return@LaunchedEffect
        if (route is Route.Login) return@LaunchedEffect
        val token = localStorage.getItem(AUTH_TOKEN_KEY)
        if (token != null) return@LaunchedEffect
        val returnPath = when (val r = route) {
            is Route.Home -> Route.Home.PATH
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
                    Navbar(authViewModel = authViewModel)
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
                            is Route.Home -> FlagsList()
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
