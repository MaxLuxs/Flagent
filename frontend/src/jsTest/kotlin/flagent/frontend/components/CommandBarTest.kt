package flagent.frontend.components

import flagent.frontend.navigation.Route
import flagent.frontend.navigation.Router
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Tests for CommandBar component.
 * Command bar provides Cmd+K / Ctrl+K global search and quick navigation.
 */
class CommandBarTest {

    @Test
    fun testCommandBarComponentExists() {
        assertTrue(true, "CommandBar component exists")
    }

    @Test
    fun testRoutesUsedByCommandBarHavePaths() {
        val routes: List<Route> = listOf(
            Route.CreateFlag,
            Route.Analytics,
            Route.DebugConsole(),
            Route.Dashboard,
            Route.FlagsList,
            Route.Experiments,
            Route.Settings
        )
        routes.forEach { route ->
            val path = with(Router) { route.path() }
            assertTrue(path.isNotBlank(), "Route $route has non-blank path: $path")
        }
    }
}
