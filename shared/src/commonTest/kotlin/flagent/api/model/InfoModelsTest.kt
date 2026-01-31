package flagent.api.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlinx.serialization.json.Json

/**
 * Tests for Info API models serialization/deserialization
 */
class InfoModelsTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun testInfoResponseSerialization() {
        val info = InfoResponse(
            version = "0.1.0",
            buildTime = "2024-01-01T00:00:00Z",
            gitCommit = "abc123"
        )
        val jsonString = json.encodeToString(InfoResponse.serializer(), info)
        assertNotNull(jsonString)
        
        val deserialized = json.decodeFromString(InfoResponse.serializer(), jsonString)
        assertEquals("0.1.0", deserialized.version)
        assertEquals("2024-01-01T00:00:00Z", deserialized.buildTime)
        assertEquals("abc123", deserialized.gitCommit)
    }

    @Test
    fun testInfoResponseWithNulls() {
        val info = InfoResponse(version = "2.0.0")
        val jsonString = json.encodeToString(InfoResponse.serializer(), info)
        val deserialized = json.decodeFromString(InfoResponse.serializer(), jsonString)
        assertEquals("2.0.0", deserialized.version)
        assertEquals(null, deserialized.buildTime)
        assertEquals(null, deserialized.gitCommit)
    }

    @Test
    fun testInfoResponseMinimal() {
        val info = InfoResponse(version = "0.1.0")
        val jsonString = json.encodeToString(InfoResponse.serializer(), info)
        assertNotNull(jsonString)
        val deserialized = json.decodeFromString(InfoResponse.serializer(), jsonString)
        assertEquals("0.1.0", deserialized.version)
    }
}
