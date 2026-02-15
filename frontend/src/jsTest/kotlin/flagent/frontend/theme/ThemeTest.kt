package flagent.frontend.theme

import flagent.frontend.state.ThemeMode
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests for FlagentTheme. Theme values are CSS variables (design-system tokens).
 */
class ThemeTest {
    @Test
    fun testPrimaryColorsAreTokenRefs() {
        assertNotNull(FlagentTheme.Primary)
        assertNotNull(FlagentTheme.PrimaryDark)
        assertNotNull(FlagentTheme.PrimaryLight)
        assertTrue(FlagentTheme.Primary.toString().contains("flagent-color-primary"))
    }

    @Test
    fun testSecondaryColorsAreTokenRefs() {
        assertNotNull(FlagentTheme.Secondary)
        assertNotNull(FlagentTheme.SecondaryDark)
        assertNotNull(FlagentTheme.SecondaryLight)
        assertTrue(FlagentTheme.Secondary.toString().contains("flagent-color-secondary"))
    }

    @Test
    fun testThemeAwareCardBg() {
        assertNotNull(FlagentTheme.cardBg(ThemeMode.Light))
        assertNotNull(FlagentTheme.cardBg(ThemeMode.Dark))
        assertTrue(FlagentTheme.cardBg(ThemeMode.Light).toString().contains("cardBg"))
        assertTrue(FlagentTheme.cardBg(ThemeMode.Dark).toString().contains("cardBg"))
    }

    @Test
    fun testThemeAwareText() {
        assertNotNull(FlagentTheme.text(ThemeMode.Light))
        assertNotNull(FlagentTheme.text(ThemeMode.Dark))
    }

    @Test
    fun testShadowValuesAreTokenRefs() {
        assertNotNull(FlagentTheme.Shadow)
        assertNotNull(FlagentTheme.ShadowHover)
        assertTrue(FlagentTheme.Shadow.contains("flagent-shadow"))
    }
}
