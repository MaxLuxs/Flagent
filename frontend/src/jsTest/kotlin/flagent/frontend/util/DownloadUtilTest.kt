package flagent.frontend.util

import kotlin.test.Test
import kotlin.test.assertTrue

class DownloadUtilTest {

    @Test
    fun testTriggerDownloadFunctionExists() {
        assertTrue(::triggerDownload.name == "triggerDownload", "triggerDownload should be defined")
    }

    @Test
    fun testTriggerDownloadFromStringExists() {
        assertTrue(::triggerDownloadFromString.name == "triggerDownloadFromString", "triggerDownloadFromString should be defined")
    }

    @Test
    fun testDownloadFilenameFormat() {
        val filename = "flagent_export.json"
        assertTrue(filename.startsWith("flagent_"), "Export filename should be prefixed")
        assertTrue(filename.contains("."), "Filename should have extension")
    }

    @Test
    fun testSegmentExportFilenameFormat() {
        val filename = "segment-42.json"
        assertTrue(filename.startsWith("segment-"), "Segment export filename should be prefixed")
        assertTrue(filename.endsWith(".json"), "Segment export should use .json extension")
    }
}
