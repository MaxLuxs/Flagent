package flagent.api.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlinx.serialization.json.Json

/**
 * Tests for Flag API models serialization/deserialization
 */
class FlagModelsTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun testCreateFlagRequestSerialization() {
        val request = CreateFlagRequest(
            description = "Test flag",
            key = "test_flag"
        )
        val jsonString = json.encodeToString(CreateFlagRequest.serializer(), request)
        assertNotNull(jsonString)
        
        val deserialized = json.decodeFromString(CreateFlagRequest.serializer(), jsonString)
        assertEquals("Test flag", deserialized.description)
        assertEquals("test_flag", deserialized.key)
    }

    @Test
    fun testCreateFlagRequestWithoutKey() {
        val request = CreateFlagRequest(description = "Test flag")
        val jsonString = json.encodeToString(CreateFlagRequest.serializer(), request)
        val deserialized = json.decodeFromString(CreateFlagRequest.serializer(), jsonString)
        assertEquals("Test flag", deserialized.description)
        assertEquals(null, deserialized.key)
    }

    @Test
    fun testPutFlagRequestSerialization() {
        val request = PutFlagRequest(
            description = "Updated description",
            dataRecordsEnabled = true
        )
        val jsonString = json.encodeToString(PutFlagRequest.serializer(), request)
        val deserialized = json.decodeFromString(PutFlagRequest.serializer(), jsonString)
        assertEquals("Updated description", deserialized.description)
        assertEquals(true, deserialized.dataRecordsEnabled)
    }

    @Test
    fun testFlagResponseSerialization() {
        val flag = FlagResponse(
            id = 1,
            key = "test_flag",
            description = "Test flag",
            enabled = true,
            snapshotID = 1,
            dataRecordsEnabled = true
        )
        val jsonString = json.encodeToString(FlagResponse.serializer(), flag)
        val deserialized = json.decodeFromString(FlagResponse.serializer(), jsonString)
        assertEquals(1, deserialized.id)
        assertEquals("test_flag", deserialized.key)
        assertEquals("Test flag", deserialized.description)
        assertEquals(true, deserialized.enabled)
        assertEquals(1, deserialized.snapshotID)
        assertEquals(true, deserialized.dataRecordsEnabled)
    }

    @Test
    fun testFlagResponseWithSegmentsAndVariants() {
        val segment = SegmentResponse(
            id = 1,
            flagID = 1,
            description = "Segment 1",
            rank = 0,
            rolloutPercent = 100
        )
        val variant = VariantResponse(
            id = 1,
            flagID = 1,
            key = "variant_a"
        )
        val flag = FlagResponse(
            id = 1,
            key = "test_flag",
            description = "Test flag",
            enabled = true,
            snapshotID = 1,
            dataRecordsEnabled = true,
            segments = listOf(segment),
            variants = listOf(variant)
        )
        val jsonString = json.encodeToString(FlagResponse.serializer(), flag)
        val deserialized = json.decodeFromString(FlagResponse.serializer(), jsonString)
        assertEquals(1, deserialized.segments.size)
        assertEquals(1, deserialized.variants.size)
        assertEquals("Segment 1", deserialized.segments.first().description)
        assertEquals("variant_a", deserialized.variants.first().key)
    }

    @Test
    fun testSegmentResponseSerialization() {
        val segment = SegmentResponse(
            id = 1,
            flagID = 1,
            description = "Test segment",
            rank = 0,
            rolloutPercent = 50
        )
        val jsonString = json.encodeToString(SegmentResponse.serializer(), segment)
        val deserialized = json.decodeFromString(SegmentResponse.serializer(), jsonString)
        assertEquals(1, deserialized.id)
        assertEquals(1, deserialized.flagID)
        assertEquals("Test segment", deserialized.description)
        assertEquals(50, deserialized.rolloutPercent)
    }

    @Test
    fun testVariantResponseSerialization() {
        val variant = VariantResponse(
            id = 1,
            flagID = 1,
            key = "variant_a",
            attachment = mapOf("color" to "red")
        )
        val jsonString = json.encodeToString(VariantResponse.serializer(), variant)
        val deserialized = json.decodeFromString(VariantResponse.serializer(), jsonString)
        assertEquals(1, deserialized.id)
        assertEquals("variant_a", deserialized.key)
        assertEquals("red", deserialized.attachment?.get("color"))
    }

    @Test
    fun testConstraintResponseSerialization() {
        val constraint = ConstraintResponse(
            id = 1,
            segmentID = 1,
            property = "country",
            operator = "EQ",
            value = "US"
        )
        val jsonString = json.encodeToString(ConstraintResponse.serializer(), constraint)
        val deserialized = json.decodeFromString(ConstraintResponse.serializer(), jsonString)
        assertEquals("country", deserialized.property)
        assertEquals("EQ", deserialized.operator)
        assertEquals("US", deserialized.value)
    }

    @Test
    fun testDistributionResponseSerialization() {
        val distribution = DistributionResponse(
            id = 1,
            segmentID = 1,
            variantID = 1,
            variantKey = "variant_a",
            percent = 50
        )
        val jsonString = json.encodeToString(DistributionResponse.serializer(), distribution)
        val deserialized = json.decodeFromString(DistributionResponse.serializer(), jsonString)
        assertEquals(1, deserialized.segmentID)
        assertEquals(50, deserialized.percent)
    }

    @Test
    fun testTagResponseSerialization() {
        val tag = TagResponse(id = 1, value = "production")
        val jsonString = json.encodeToString(TagResponse.serializer(), tag)
        val deserialized = json.decodeFromString(TagResponse.serializer(), jsonString)
        assertEquals(1, deserialized.id)
        assertEquals("production", deserialized.value)
    }

    @Test
    fun testSetFlagEnabledRequestSerialization() {
        val request = SetFlagEnabledRequest(enabled = true)
        val jsonString = json.encodeToString(SetFlagEnabledRequest.serializer(), request)
        val deserialized = json.decodeFromString(SetFlagEnabledRequest.serializer(), jsonString)
        assertEquals(true, deserialized.enabled)
    }

    @Test
    fun testCreateSegmentRequestSerialization() {
        val request = CreateSegmentRequest(
            description = "New segment",
            rolloutPercent = 25
        )
        val jsonString = json.encodeToString(CreateSegmentRequest.serializer(), request)
        val deserialized = json.decodeFromString(CreateSegmentRequest.serializer(), jsonString)
        assertEquals("New segment", deserialized.description)
        assertEquals(25, deserialized.rolloutPercent)
    }

    @Test
    fun testPutSegmentRequestSerialization() {
        val request = PutSegmentRequest(
            description = "Updated segment",
            rolloutPercent = 75
        )
        val jsonString = json.encodeToString(PutSegmentRequest.serializer(), request)
        val deserialized = json.decodeFromString(PutSegmentRequest.serializer(), jsonString)
        assertEquals("Updated segment", deserialized.description)
        assertEquals(75, deserialized.rolloutPercent)
    }

    @Test
    fun testPutSegmentReorderRequestSerialization() {
        val request = PutSegmentReorderRequest(segmentIDs = listOf(3, 1, 2))
        val jsonString = json.encodeToString(PutSegmentReorderRequest.serializer(), request)
        val deserialized = json.decodeFromString(PutSegmentReorderRequest.serializer(), jsonString)
        assertEquals(3, deserialized.segmentIDs.size)
        assertEquals(3, deserialized.segmentIDs.first())
    }

    @Test
    fun testCreateConstraintRequestSerialization() {
        val request = CreateConstraintRequest(
            property = "country",
            operator = "EQ",
            value = "US"
        )
        val jsonString = json.encodeToString(CreateConstraintRequest.serializer(), request)
        val deserialized = json.decodeFromString(CreateConstraintRequest.serializer(), jsonString)
        assertEquals("country", deserialized.property)
        assertEquals("EQ", deserialized.operator)
        assertEquals("US", deserialized.value)
    }

    @Test
    fun testPutConstraintRequestSerialization() {
        val request = PutConstraintRequest(
            property = "country",
            operator = "NE",
            value = "CA"
        )
        val jsonString = json.encodeToString(PutConstraintRequest.serializer(), request)
        val deserialized = json.decodeFromString(PutConstraintRequest.serializer(), jsonString)
        assertEquals("country", deserialized.property)
        assertEquals("NE", deserialized.operator)
        assertEquals("CA", deserialized.value)
    }

    @Test
    fun testDistributionRequestSerialization() {
        val request = DistributionRequest(
            variantID = 1,
            variantKey = "variant_a",
            percent = 50
        )
        val jsonString = json.encodeToString(DistributionRequest.serializer(), request)
        val deserialized = json.decodeFromString(DistributionRequest.serializer(), jsonString)
        assertEquals(1, deserialized.variantID)
        assertEquals(50, deserialized.percent)
    }

    @Test
    fun testPutDistributionsRequestSerialization() {
        val distribution = DistributionRequest(variantID = 1, percent = 100)
        val request = PutDistributionsRequest(distributions = listOf(distribution))
        val jsonString = json.encodeToString(PutDistributionsRequest.serializer(), request)
        val deserialized = json.decodeFromString(PutDistributionsRequest.serializer(), jsonString)
        assertEquals(1, deserialized.distributions.size)
        assertEquals(1, deserialized.distributions.first().variantID)
    }

    @Test
    fun testCreateVariantRequestSerialization() {
        val request = CreateVariantRequest(
            key = "variant_b",
            attachment = mapOf("theme" to "dark")
        )
        val jsonString = json.encodeToString(CreateVariantRequest.serializer(), request)
        val deserialized = json.decodeFromString(CreateVariantRequest.serializer(), jsonString)
        assertEquals("variant_b", deserialized.key)
        assertEquals("dark", deserialized.attachment?.get("theme"))
    }

    @Test
    fun testPutVariantRequestSerialization() {
        val request = PutVariantRequest(
            key = "variant_c",
            attachment = mapOf("theme" to "light")
        )
        val jsonString = json.encodeToString(PutVariantRequest.serializer(), request)
        val deserialized = json.decodeFromString(PutVariantRequest.serializer(), jsonString)
        assertEquals("variant_c", deserialized.key)
        assertEquals("light", deserialized.attachment?.get("theme"))
    }

    @Test
    fun testCreateTagRequestSerialization() {
        val request = CreateTagRequest(value = "staging")
        val jsonString = json.encodeToString(CreateTagRequest.serializer(), request)
        val deserialized = json.decodeFromString(CreateTagRequest.serializer(), jsonString)
        assertEquals("staging", deserialized.value)
    }

    @Test
    fun testFlagSnapshotResponseSerialization() {
        val flag = FlagResponse(
            id = 1,
            key = "test_flag",
            description = "Test flag",
            enabled = true,
            snapshotID = 1,
            dataRecordsEnabled = true
        )
        val snapshot = FlagSnapshotResponse(
            id = 1,
            updatedBy = "user",
            flag = flag,
            updatedAt = "2024-01-01T00:00:00Z"
        )
        val jsonString = json.encodeToString(FlagSnapshotResponse.serializer(), snapshot)
        val deserialized = json.decodeFromString(FlagSnapshotResponse.serializer(), jsonString)
        assertEquals(1, deserialized.id)
        assertEquals("user", deserialized.updatedBy)
        assertEquals("test_flag", deserialized.flag.key)
        assertEquals("2024-01-01T00:00:00Z", deserialized.updatedAt)
    }
}
