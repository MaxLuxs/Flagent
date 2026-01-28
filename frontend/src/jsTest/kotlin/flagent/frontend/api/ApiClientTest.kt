package flagent.frontend.api

import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Tests for ApiClient
 * Note: These are basic unit tests for path construction
 * Full integration tests would require a mock server
 */
class ApiClientTest {
    @Test
    fun testApiClientPathConstruction() {
        // Test getApiPath method
        val path = ApiClient.getApiPath("/flags")
        assertTrue(path.contains("/api/v1/flags"), "Path should contain /api/v1/flags")
    }
    
    @Test
    fun testApiClientExists() {
        // Verify ApiClient singleton exists and is accessible
        val client = ApiClient
        assertTrue(client != null, "ApiClient should be accessible as singleton")
    }
}
