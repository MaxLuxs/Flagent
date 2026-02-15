package flagent.frontend.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Tests for Router navigation logic
 */
class RouterTest {
    @Test
    fun testHomePathIsRoot() {
        assertEquals("/", Route.Home.PATH, "Home route must be root path for landing page")
    }

    @Test
    fun testRoutePathGeneration() {
        assertEquals("/", Route.Home.PATH)
        assertEquals("/flags", Route.FlagsList.PATH)
        assertEquals("/flags/new", Route.CreateFlag.PATH)
        
        val flagDetail = Route.FlagDetail(123)
        assertEquals("/flags/123", flagDetail.path())
        
        val flagHistory = Route.FlagHistory(456)
        assertEquals("/flags/456/history", flagHistory.path())
        
        val debugConsole = Route.DebugConsole()
        assertEquals("/debug", debugConsole.path())
        
        val debugConsoleWithKey = Route.DebugConsole("test_flag")
        assertEquals("/debug?flagKey=test_flag", debugConsoleWithKey.path())
    }
    
    @Test
    fun testRouterExtensionPath() {
        val router = Router
        val homePath = Route.Home.PATH
        assertEquals("/", homePath)
        
        val flagDetail = Route.FlagDetail(789)
        val flagPath = flagDetail.path()
        assertEquals("/flags/789", flagPath)
    }

    @Test
    fun testRoutePathExtensionForHome() {
        val path = with(Router) { Route.Home.path() }
        assertEquals("/", path, "Route.path() for Home must return root for logo href")
    }
    
    @Test
    fun testRoutePathEdgeCases() {
        // Test FlagDetail with different IDs
        val flagDetail1 = Route.FlagDetail(1)
        assertEquals("/flags/1", flagDetail1.path())
        
        val flagDetail100 = Route.FlagDetail(100)
        assertEquals("/flags/100", flagDetail100.path())
        
        // Test FlagHistory with different IDs
        val flagHistory1 = Route.FlagHistory(1)
        assertEquals("/flags/1/history", flagHistory1.path())
        
        // Test DebugConsole variations
        val debugConsole1 = Route.DebugConsole(null)
        assertEquals("/debug", debugConsole1.path())
        
        val debugConsole2 = Route.DebugConsole("")
        assertEquals("/debug", debugConsole2.path())
        
        val debugConsole3 = Route.DebugConsole("my_flag")
        assertEquals("/debug?flagKey=my_flag", debugConsole3.path())

        // Flag sub-routes
        val flagRollout = Route.FlagRollout(42)
        assertEquals("/flags/42/rollout", flagRollout.path())

        val flagAnomalies = Route.FlagAnomalies(99)
        assertEquals("/flags/99/anomalies", flagAnomalies.path())

        val flagMetricsWithType = Route.FlagMetrics(7, "eval")
        assertEquals("/flags/7/metrics?metric=eval", flagMetricsWithType.path())
    }

    @Test
    fun testAllKeyRoutesHavePaths() {
        with(Router) {
            assertEquals("/", Route.Home.path())
            assertEquals("/flags", Route.FlagsList.path())
            assertEquals("/flags/new", Route.CreateFlag.path())
            assertEquals("/dashboard", Route.Dashboard.path())
            assertEquals("/experiments", Route.Experiments.path())
            assertEquals("/segments", Route.Segments.path())
            assertEquals("/analytics", Route.Analytics.path())
            assertEquals("/settings", Route.Settings.path())
            assertEquals("/login", Route.Login.path())
            assertEquals("/debug", Route.DebugConsole().path())
        }
    }
    
    @Test
    fun testRouteEquality() {
        val route1 = Route.Home
        val route2 = Route.Home
        // Object equality test
        assertTrue(route1 == route2)
        
        val route3 = Route.CreateFlag
        // Different routes are not equal
        assertTrue(route1 != route3)
        
        val route4 = Route.FlagDetail(123)
        val route5 = Route.FlagDetail(123)
        val route6 = Route.FlagDetail(456)
        // Data classes equality
        assertEquals(route4.flagId, route5.flagId)
        assertTrue(route4.flagId != route6.flagId)
    }
}
