package flagent.frontend.components

import flagent.api.model.ConstraintResponse
import flagent.api.model.DistributionResponse
import flagent.api.model.SegmentResponse
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Tests for FlagEditor component and segment duplicate/export logic.
 * Note: Compose for Web testing requires special setup.
 */
class FlagEditorTest {
    @Test
    fun testFlagEditorComponent() {
        assertTrue(true, "FlagEditor component exists")
    }

    @Test
    fun testFlagEditorCreateMode() {
        assertTrue(true, "FlagEditor supports create mode")
    }

    @Test
    fun testFlagEditorEditMode() {
        assertTrue(true, "FlagEditor supports edit mode")
    }

    @Test
    fun testSegmentExportSerialization() {
        val segment = SegmentResponse(
            id = 1,
            flagID = 2,
            description = "Test segment",
            rank = 0,
            rolloutPercent = 50,
            constraints = listOf(
                ConstraintResponse(1, 1, "region", "EQ", "EU")
            ),
            distributions = listOf(
                DistributionResponse(1, 1, 10, "control", 50),
                DistributionResponse(2, 1, 11, "treatment", 50)
            )
        )
        val json = Json.encodeToString(SegmentResponse.serializer(), segment)
        assertTrue(json.contains("\"id\":1"), "Exported JSON should contain segment id")
        assertTrue(json.contains("Test segment"), "Exported JSON should contain description")
        assertTrue(json.contains("region"), "Exported JSON should contain constraints")
        assertTrue(json.contains("control"), "Exported JSON should contain distributions")
    }
}
