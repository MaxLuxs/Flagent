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

    data class FlagDetail(val flagId: Int) : Route() {
        fun path() = "/flags/$flagId"
    }

    object CreateFlag : Route() {
        const val PATH = "/flags/new"
    }

    data class DebugConsole(val flagKey: String? = null) : Route() {
        fun path() = if (flagKey != null) "/debug?flagKey=$flagKey" else "/debug"
    }

    data class FlagHistory(val flagId: Int) : Route() {
        fun path() = "/flags/$flagId/history"
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
            path == Route.CreateFlag.PATH -> Route.CreateFlag
            path.startsWith("/flags/") && path.endsWith("/history") -> {
                val flagId = path.removePrefix("/flags/").removeSuffix("/history").toIntOrNull()
                if (flagId != null) Route.FlagHistory(flagId) else Route.Home
            }

            path.startsWith("/flags/") -> {
                val flagId = path.removePrefix("/flags/").toIntOrNull()
                if (flagId != null) Route.FlagDetail(flagId) else Route.Home
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
            is Route.CreateFlag -> Route.CreateFlag.PATH
            is Route.FlagDetail -> this.path()
            is Route.DebugConsole -> this.path()
            is Route.FlagHistory -> this.path()
        }
    }
}
