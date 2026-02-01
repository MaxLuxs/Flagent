package flagent.frontend.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.browser.window

/**
 * Router for Compose for Web with URL-based navigation
 */
sealed class Route {
    object Home : Route() {
        const val PATH = "/"
    }

    object FlagsList : Route() {
        const val PATH = "/flags"
    }
    
    object Dashboard : Route() {
        const val PATH = "/dashboard"
    }

    object Experiments : Route() {
        const val PATH = "/experiments"
    }

    object Analytics : Route() {
        const val PATH = "/analytics"
    }

    data class FlagDetail(val flagId: Int) : Route() {
        fun path() = "/flags/$flagId"
    }

    object CreateFlag : Route() {
        const val PATH = "/flags/new"
    }

    data class DebugConsole(val flagKey: String? = null) : Route() {
        fun path() = if (flagKey != null && flagKey.isNotBlank()) "/debug?flagKey=$flagKey" else "/debug"
    }

    data class FlagHistory(val flagId: Int) : Route() {
        fun path() = "/flags/$flagId/history"
    }
    
    data class FlagMetrics(val flagId: Int) : Route() {
        fun path() = "/flags/$flagId/metrics"
    }
    
    data class FlagRollout(val flagId: Int) : Route() {
        fun path() = "/flags/$flagId/rollout"
    }
    
    data class FlagAnomalies(val flagId: Int) : Route() {
        fun path() = "/flags/$flagId/anomalies"
    }
    
    object Alerts : Route() {
        const val PATH = "/alerts"
    }
    
    object Settings : Route() {
        const val PATH = "/settings"
    }
    
    object Tenants : Route() {
        const val PATH = "/tenants"
    }

    object Login : Route() {
        const val PATH = "/login"
    }
}

object Router {
    var currentRoute by mutableStateOf<Route>(Route.Home)
        private set

    private val history = mutableListOf<Route>()
    private var initialized = false

    fun initialize() {
        if (!initialized) {
            initialized = true
            // Initialize from URL
            parseRouteFromUrl()

            // Listen to browser navigation (back/forward)
            window.addEventListener("popstate", { parseRouteFromUrl() })
        }
    }

    fun navigateTo(route: Route, addToHistory: Boolean = true) {
        currentRoute = route

        if (addToHistory) {
            history.add(route)
            window.history.pushState(null, "", route.path())
        }
    }

    fun navigateBack() {
        if (history.size > 1) {
            history.removeLast()
            val previousRoute = history.lastOrNull() ?: Route.Home
            currentRoute = previousRoute
            window.history.back()
        } else {
            navigateTo(Route.Home)
        }
    }

    fun navigateToPath(path: String) {
        val route = parseRouteFromPath(path)
        navigateTo(route)
    }

    /** Navigate to /tenants with ?create=1 so TenantsList opens Create Tenant modal. */
    fun navigateToTenantsWithCreate() {
        currentRoute = Route.Tenants
        history.add(Route.Tenants)
        window.history.pushState(null, "", "${Route.Tenants.PATH}?create=1")
    }

    private fun parseRouteFromUrl() {
        val path = window.location.pathname
        val search = window.location.search
        val route = parseRouteFromPath(path, search)
        currentRoute = route
        if (route !in history) {
            history.add(route)
        }
    }

    private fun parseRouteFromPath(path: String, search: String = ""): Route {
        return when {
            path == Route.Home.PATH -> Route.Home
            path == Route.FlagsList.PATH -> Route.FlagsList
            path == Route.Dashboard.PATH -> Route.Dashboard
            path == Route.Experiments.PATH -> Route.Experiments
            path == Route.Analytics.PATH -> Route.Analytics
            path == Route.CreateFlag.PATH -> Route.CreateFlag
            path == Route.Alerts.PATH -> Route.Alerts
            path == Route.Settings.PATH -> Route.Settings
            path == Route.Tenants.PATH -> Route.Tenants
            path == Route.Login.PATH -> Route.Login
            
            path.startsWith("/flags/") && path.endsWith("/history") -> {
                val flagId = path.removePrefix("/flags/").removeSuffix("/history").toIntOrNull()
                if (flagId != null) Route.FlagHistory(flagId) else Route.FlagsList
            }
            
            path.startsWith("/flags/") && path.endsWith("/metrics") -> {
                val flagId = path.removePrefix("/flags/").removeSuffix("/metrics").toIntOrNull()
                if (flagId != null) Route.FlagMetrics(flagId) else Route.FlagsList
            }
            
            path.startsWith("/flags/") && path.endsWith("/rollout") -> {
                val flagId = path.removePrefix("/flags/").removeSuffix("/rollout").toIntOrNull()
                if (flagId != null) Route.FlagRollout(flagId) else Route.FlagsList
            }
            
            path.startsWith("/flags/") && path.endsWith("/anomalies") -> {
                val flagId = path.removePrefix("/flags/").removeSuffix("/anomalies").toIntOrNull()
                if (flagId != null) Route.FlagAnomalies(flagId) else Route.FlagsList
            }

            path.startsWith("/flags/") -> {
                val flagId = path.removePrefix("/flags/").toIntOrNull()
                if (flagId != null) Route.FlagDetail(flagId) else Route.FlagsList
            }

            path == "/debug" -> {
                val flagKey = search.removePrefix("?flagKey=").takeIf { it.isNotBlank() }
                Route.DebugConsole(flagKey)
            }

            else -> Route.Home
        }
    }

    fun Route.path(): String {
        return when (this) {
            is Route.Home -> Route.Home.PATH
            is Route.FlagsList -> Route.FlagsList.PATH
            is Route.Dashboard -> Route.Dashboard.PATH
            is Route.Experiments -> Route.Experiments.PATH
            is Route.Analytics -> Route.Analytics.PATH
            is Route.CreateFlag -> Route.CreateFlag.PATH
            is Route.FlagDetail -> this.path()
            is Route.DebugConsole -> this.path()
            is Route.FlagHistory -> this.path()
            is Route.FlagMetrics -> this.path()
            is Route.FlagRollout -> this.path()
            is Route.FlagAnomalies -> this.path()
            is Route.Alerts -> Route.Alerts.PATH
            is Route.Settings -> Route.Settings.PATH
            is Route.Tenants -> Route.Tenants.PATH
            is Route.Login -> Route.Login.PATH
        }
    }
}
