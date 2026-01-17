package flagent.api.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlinx.serialization.json.Json

/**
 * Tests for Common models serialization/deserialization
 */
class CommonModelsTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun testFlagSerialization() {
        val flag = Flag(
            id = 1,
            key = "test_flag",
            description = "Test flag",
            enabled = true,
            tags = listOf(Tag(id = 1, value = "production"))
        )
        val jsonString = json.encodeToString(Flag.serializer(), flag)
        assertNotNull(jsonString)
        
        val deserialized = json.decodeFromString(Flag.serializer(), jsonString)
        assertEquals(1, deserialized.id)
        assertEquals("test_flag", deserialized.key)
        assertEquals("Test flag", deserialized.description)
        assertEquals(true, deserialized.enabled)
        assertEquals(1, deserialized.tags.size)
        assertEquals("production", deserialized.tags.first().value)
    }

    @Test
    fun testFlagWithNullDescription() {
        val flag = Flag(
            id = 1,
            key = "test_flag",
            description = null,
            enabled = false
        )
        val jsonString = json.encodeToString(Flag.serializer(), flag)
        val deserialized = json.decodeFromString(Flag.serializer(), jsonString)
        assertEquals(null, deserialized.description)
        assertEquals(false, deserialized.enabled)
    }

    @Test
    fun testTagSerialization() {
        val tag = Tag(id = 1, value = "staging")
        val jsonString = json.encodeToString(Tag.serializer(), tag)
        val deserialized = json.decodeFromString(Tag.serializer(), jsonString)
        assertEquals(1, deserialized.id)
        assertEquals("staging", deserialized.value)
    }

    @Test
    fun testUpdateFlagRequestSerialization() {
        val request = UpdateFlagRequest(
            key = "updated_flag",
            description = "Updated description",
            enabled = true
        )
        val jsonString = json.encodeToString(UpdateFlagRequest.serializer(), request)
        val deserialized = json.decodeFromString(UpdateFlagRequest.serializer(), jsonString)
        assertEquals("updated_flag", deserialized.key)
        assertEquals("Updated description", deserialized.description)
        assertEquals(true, deserialized.enabled)
    }

    @Test
    fun testUpdateFlagRequestWithNulls() {
        val request = UpdateFlagRequest(
            key = null,
            description = null,
            enabled = null
        )
        val jsonString = json.encodeToString(UpdateFlagRequest.serializer(), request)
        val deserialized = json.decodeFromString(UpdateFlagRequest.serializer(), jsonString)
        assertEquals(null, deserialized.key)
        assertEquals(null, deserialized.description)
        assertEquals(null, deserialized.enabled)
    }

    @Test
    fun testUpdateFlagRequestPartialUpdate() {
        val request = UpdateFlagRequest(enabled = true)
        val jsonString = json.encodeToString(UpdateFlagRequest.serializer(), request)
        val deserialized = json.decodeFromString(UpdateFlagRequest.serializer(), jsonString)
        assertEquals(null, deserialized.key)
        assertEquals(null, deserialized.description)
        assertEquals(true, deserialized.enabled)
    }
}
