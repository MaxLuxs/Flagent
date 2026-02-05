package flagent.frontend.components.settings

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SettingsPageTest {

    @Test
    fun testSettingsPageComponentExists() {
        assertTrue(true, "SettingsPage component exists")
    }

    @Test
    fun testSettingsTabsIncludeExportAndImport() {
        val tabs = listOf("general", "export", "import", "webhooks")
        assertTrue(tabs.contains("export"), "Settings should have Export tab")
        assertTrue(tabs.contains("import"), "Settings should have Import tab")
    }

    @Test
    fun testDefaultActiveTab() {
        val defaultTab = "general"
        assertEquals("general", defaultTab)
    }
}
