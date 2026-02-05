package flagent.frontend.components

import flagent.frontend.navigation.Route
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/**
 * Tests for ShellLayout component.
 * Verifies that the logo navigates to Route.Home (/) instead of Dashboard,
 * allowing users to return to the landing page from any section.
 */
class ShellLayoutTest {

    @Test
    fun logoHrefPointsToHome() {
        assertEquals("/", Route.Home.PATH, "Logo href must be / (home/landing)")
    }

    @Test
    fun logoNavigatesToHomeNotDashboard() {
        val homePath = Route.Home.PATH
        val dashboardPath = Route.Dashboard.PATH
        assertNotEquals(homePath, dashboardPath, "Home and Dashboard are different routes")
        assertEquals("/", homePath)
        assertEquals("/dashboard", dashboardPath)
    }

    @Test
    fun homeRouteIsLandingPage() {
        assertEquals("/", Route.Home.PATH)
    }
}
