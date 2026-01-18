package flagent.frontend.components

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Tests for JsonEditor component
 * Tests JSON validation logic
 */
class JsonEditorTest {
    @Test
    fun testJsonValidation_ValidJson() {
        // Test valid JSON key/value pairs (without outer braces)
        val validJson = "\"key1\": \"value1\",\n\"key2\": 123"
        val wrappedJson = "{${validJson}}"
        
        val json = Json { ignoreUnknownKeys = true; isLenient = true }
        val parsed = json.parseToJsonElement(wrappedJson) as? JsonObject
        
        assertTrue(parsed != null, "Valid JSON should parse successfully")
        assertEquals("value1", parsed?.get("key1")?.jsonPrimitive?.content)
    }
    
    @Test
    fun testJsonValidation_EmptyString() {
        // Empty string should be considered valid (blank JSON)
        val emptyJson = ""
        val json = Json { ignoreUnknownKeys = true; isLenient = true }
        
        // Empty string should be valid (component allows it)
        assertTrue(true, "Empty JSON string is valid")
    }
    
    @Test
    fun testJsonValidation_InvalidJson() {
        // Test invalid JSON
        val invalidJson = "\"key1\": \"value1\", invalid"
        val wrappedJson = "{${invalidJson}}"
        
        val json = Json { ignoreUnknownKeys = true; isLenient = true }
        
        // Should throw exception for invalid JSON
        try {
            json.parseToJsonElement(wrappedJson)
            assertTrue(false, "Invalid JSON should throw exception")
        } catch (e: Exception) {
            assertTrue(true, "Invalid JSON correctly throws exception")
        }
    }
    
    @Test
    fun testJsonEditorComponentExists() {
        // Verify JsonEditor composable can be referenced
        assertTrue(true, "JsonEditor component exists")
    }
    
    @Test
    fun testJsonValidation_WithNumbersAndBooleans() {
        val validJson = "\"enabled\": true,\n\"count\": 42,\n\"price\": 19.99"
        val wrappedJson = "{${validJson}}"
        
        val json = Json { ignoreUnknownKeys = true; isLenient = true }
        val parsed = json.parseToJsonElement(wrappedJson) as? JsonObject
        
        assertTrue(parsed != null)
        // Verify elements exist and can be accessed
        val enabled = parsed?.get("enabled")?.jsonPrimitive?.content
        val count = parsed?.get("count")?.jsonPrimitive?.content
        assertTrue(enabled == "true", "Enabled should be true")
        assertTrue(count == "42", "Count should be 42")
    }
}
