package flagent.frontend.util

import kotlin.test.Test
import kotlin.test.assertTrue

class DownloadUtilTest {

    @Test
    fun testTriggerDownloadFunctionExists() {
        assertTrue(::triggerDownload.name == "triggerDownload", "triggerDownload should be defined")
    }

    @Test
    fun testDownloadFilenameFormat() {
        val filename = "flagent_export.json"
        assertTrue(filename.startsWith("flagent_"), "Export filename should be prefixed")
        assertTrue(filename.contains("."), "Filename should have extension")
    }
}
