package flagent.route.mapper

import flagent.domain.entity.*
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ResponseMappersTest {

    @Test
    fun mapFlagToResponseMapsAllFields() {
        val segment = Segment(id = 1, flagId = 1, rank = 1, rolloutPercent = 50, constraints = emptyList(), distributions = emptyList())
        val variant = Variant(id = 1, flagId = 1, key = "control", attachment = null)
        val tag = Tag(id = 1, value = "prod")
        val flag = Flag(
            id = 1,
            key = "test_flag",
            description = "Test flag",
            createdBy = "u1",
            updatedBy = "u2",
            enabled = true,
            snapshotId = 5,
            notes = "notes",
            dataRecordsEnabled = true,
            entityType = "user",
            segments = listOf(segment),
            variants = listOf(variant),
            tags = listOf(tag),
            updatedAt = "2024-01-01T00:00:00Z"
        )

        val response = ResponseMappers.mapFlagToResponse(flag)

        assertEquals(1, response.id)
        assertEquals("test_flag", response.key)
        assertEquals("Test flag", response.description)
        assertEquals("u1", response.createdBy)
        assertEquals("u2", response.updatedBy)
        assertEquals(true, response.enabled)
        assertEquals(5, response.snapshotID)
        assertEquals("notes", response.notes)
        assertEquals(true, response.dataRecordsEnabled)
        assertEquals("user", response.entityType)
        assertEquals(1, response.segments.size)
        assertEquals(1, response.variants.size)
        assertEquals(1, response.tags.size)
        assertEquals("2024-01-01T00:00:00Z", response.updatedAt)
    }

    @Test
    fun mapSegmentToResponseMapsAllFields() {
        val constraint = Constraint(id = 1, segmentId = 1, property = "region", operator = "EQ", value = "EU")
        val distribution = Distribution(id = 1, segmentId = 1, variantId = 10, variantKey = "A", percent = 100)
        val segment = Segment(
            id = 1,
            flagId = 2,
            description = "Segment 1",
            rank = 1,
            rolloutPercent = 75,
            constraints = listOf(constraint),
            distributions = listOf(distribution)
        )

        val response = ResponseMappers.mapSegmentToResponse(segment)

        assertEquals(1, response.id)
        assertEquals(2, response.flagID)
        assertEquals("Segment 1", response.description)
        assertEquals(1, response.rank)
        assertEquals(75, response.rolloutPercent)
        assertEquals(1, response.constraints.size)
        assertEquals(1, response.distributions.size)
    }

    @Test
    fun mapVariantToResponseMapsKeyAndAttachment() {
        val attachment = buildJsonObject {
            put("url", "https://example.com")
            put("enabled", true)
        }
        val variant = Variant(id = 1, flagId = 1, key = "treatment", attachment = attachment)

        val response = ResponseMappers.mapVariantToResponse(variant)

        assertEquals(1, response.id)
        assertEquals(1, response.flagID)
        assertEquals("treatment", response.key)
        assertEquals(2, response.attachment?.size)
        assertEquals("https://example.com", response.attachment?.get("url"))
    }

    @Test
    fun mapVariantToResponseWithNullAttachment() {
        val variant = Variant(id = 2, flagId = 1, key = "control", attachment = null)

        val response = ResponseMappers.mapVariantToResponse(variant)

        assertEquals(2, response.id)
        assertEquals("control", response.key)
        assertNull(response.attachment)
    }

    @Test
    fun mapConstraintToResponseMapsAllFields() {
        val constraint = Constraint(id = 1, segmentId = 2, property = "tier", operator = "IN", value = "premium")

        val response = ResponseMappers.mapConstraintToResponse(constraint)

        assertEquals(1, response.id)
        assertEquals(2, response.segmentID)
        assertEquals("tier", response.property)
        assertEquals("IN", response.operator)
        assertEquals("premium", response.value)
    }

    @Test
    fun mapDistributionToResponseMapsAllFields() {
        val distribution = Distribution(id = 1, segmentId = 2, variantId = 10, variantKey = "B", percent = 50)

        val response = ResponseMappers.mapDistributionToResponse(distribution)

        assertEquals(1, response.id)
        assertEquals(2, response.segmentID)
        assertEquals(10, response.variantID)
        assertEquals("B", response.variantKey)
        assertEquals(50, response.percent)
    }

    @Test
    fun mapTagToResponseMapsValue() {
        val tag = Tag(id = 1, value = "production")

        val response = ResponseMappers.mapTagToResponse(tag)

        assertEquals(1, response.id)
        assertEquals("production", response.value)
    }
}
