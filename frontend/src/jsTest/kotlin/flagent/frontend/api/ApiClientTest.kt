package flagent.frontend.api

import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Tests for ApiClient path construction.
 * Full integration tests (login, API calls) would require a mock server or test backend.
 */
class ApiClientTest {

    @Test
    fun getApiPathContainsApiV1AndPath() {
        val path = ApiClient.getApiPath("/flags")
        assertTrue(path.contains("/api/v1/flags"), "Path should contain /api/v1/flags")
    }

    @Test
    fun getApiPathWithEmptyBaseUsesRelativePath() {
        val path = ApiClient.getApiPath("/health")
        assertTrue(path.contains("/api/v1/health"), "Path should contain /api/v1/health")
    }

    @Test
    fun getAdminPathContainsAdminAndPath() {
        val path = ApiClient.getAdminPath("/tenants")
        assertTrue(path.contains("/admin/tenants"), "Path should contain /admin/tenants")
    }

    @Test
    fun getAuthPathReturnsPathWithLeadingSlashWhenBaseEmpty() {
        val path = ApiClient.getAuthPath("/auth/login")
        assertTrue(path == "/auth/login" || path.endsWith("/auth/login"), "Auth path should end with /auth/login")
    }

    @Test
    fun apiClientSingletonAccessible() {
        val client = ApiClient
        assertTrue(client != null, "ApiClient should be accessible as singleton")
    }
}
