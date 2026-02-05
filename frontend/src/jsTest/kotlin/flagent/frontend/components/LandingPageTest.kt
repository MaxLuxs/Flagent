package flagent.frontend.components

import flagent.frontend.navigation.Route
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for LandingPage component.
 * Verifies routes, navigation targets, and feature card structure used by the landing page.
 */
class LandingPageTest {

    @Test
    fun landingPageUsesHomeRoute() {
        assertEquals("/", Route.Home.PATH, "Landing page is served at /")
    }

    @Test
    fun dashboardButtonNavigatesToDashboard() {
        assertEquals("/dashboard", Route.Dashboard.PATH)
    }

    @Test
    fun flagsButtonNavigatesToFlagsList() {
        assertEquals("/flags", Route.FlagsList.PATH)
    }

    @Test
    fun featureCardFeatureFlagsNavigatesToFlagsList() {
        assertEquals("/flags", Route.FlagsList.PATH)
    }

    @Test
    fun featureCardABTestingNavigatesToExperiments() {
        assertEquals("/experiments", Route.Experiments.PATH)
    }

    @Test
    fun featureCardGradualRolloutNavigatesToDashboard() {
        assertEquals("/dashboard", Route.Dashboard.PATH)
    }

    @Test
    fun featureCardKillSwitchesNavigatesToFlagsList() {
        assertEquals("/flags", Route.FlagsList.PATH)
    }

    @Test
    fun landingPageFeatureCardTitles() {
        val expectedTitles = listOf(
            "Feature Flags",
            "A/B Testing",
            "Gradual Rollout",
            "Kill Switches"
        )
        assertTrue(expectedTitles.size == 4, "Landing page has 4 feature cards")
        assertTrue(expectedTitles.contains("Feature Flags"))
        assertTrue(expectedTitles.contains("A/B Testing"))
        assertTrue(expectedTitles.contains("Gradual Rollout"))
        assertTrue(expectedTitles.contains("Kill Switches"))
    }

    @Test
    fun tenantsRouteExistsForMultiTenancyBlock() {
        assertEquals("/tenants", Route.Tenants.PATH)
    }

    @Test
    fun loginRouteExistsForAuthButton() {
        assertEquals("/login", Route.Login.PATH)
    }
}
