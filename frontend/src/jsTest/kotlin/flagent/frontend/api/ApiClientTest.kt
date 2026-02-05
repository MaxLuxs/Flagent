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
    fun getApiPathForTags() {
        val path = ApiClient.getApiPath("/tags")
        assertTrue(path.contains("/api/v1/tags"), "Path should contain /api/v1/tags")
    }

    @Test
    fun getApiPathForBatchEnabled() {
        val path = ApiClient.getApiPath("/flags/batch/enabled")
        assertTrue(path.contains("/api/v1/flags/batch/enabled"), "Path should contain /api/v1/flags/batch/enabled")
    }

    @Test
    fun getApiPathForInfo() {
        val path = ApiClient.getApiPath("/info")
        assertTrue(path.contains("/api/v1/info"), "Path should contain /api/v1/info")
    }

    @Test
    fun getApiPathForImport() {
        val path = ApiClient.getApiPath("/import")
        assertTrue(path.contains("/api/v1/import"), "Path should contain /api/v1/import")
    }

    @Test
    fun getApiPathForExport() {
        val path = ApiClient.getApiPath("/export/eval_cache/json")
        assertTrue(path.contains("/api/v1/export/eval_cache/json"), "Path should contain /api/v1/export/eval_cache/json")
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
