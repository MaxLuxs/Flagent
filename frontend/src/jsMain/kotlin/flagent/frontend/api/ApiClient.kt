package flagent.frontend.api

import flagent.api.constants.ApiConstants
import flagent.api.model.*
import flagent.api.model.PutSegmentReorderRequest
import flagent.frontend.config.AppConfig
import flagent.frontend.util.AppLogger
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.browser.localStorage
import kotlinx.browser.window
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * API Client for Flagent backend (Singleton)
 */
private const val AUTH_TOKEN_KEY = "auth_token"
private const val USER_KEY = "current_user"
private const val API_KEY_STORAGE_KEY = "api_key"
private const val ADMIN_API_KEY_HEADER = "X-Admin-Key"

object ApiClient {
    private const val TAG = "ApiClient"
    
    internal val client = HttpClient {
        expectSuccess = true  // Throw ClientRequestException on 4xx before body parsing
        
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                encodeDefaults = true
            })
        }
        
        install(HttpTimeout) {
            requestTimeoutMillis = AppConfig.apiTimeout
            connectTimeoutMillis = AppConfig.apiTimeout
        }
        
        defaultRequest {
            val apiKey = getApiKey()
            if (apiKey != null) {
                header(ApiConstants.Headers.API_KEY, apiKey)
            }
            val token = getAuthToken()
            if (token != null) {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            val tenantId = getTenantId()
            if (tenantId != null) {
                header("X-Tenant-ID", tenantId)
            }
        }
        
        HttpResponseValidator {
            handleResponseExceptionWithRequest { exception, request ->
                val ex = exception as? ClientRequestException ?: return@handleResponseExceptionWithRequest
                if (ex.response.status == HttpStatusCode.Unauthorized) {
                    val hasAuth = getAuthToken() != null
                    val path = request.url.encodedPath
                    // 401 on /api/v1/* = tenant required (Create tenant first / X-API-Key) — show CTA, do NOT clear token or redirect
                    val isTenantApi = path.contains("/api/v1/") || path.contains("/api/v1")
                    if (isTenantApi || !hasAuth) {
                        throw exception  // Let ErrorHandler show "Create tenant first" / "Missing X-API-Key"
                    }
                    // 401 on /admin/* = admin auth required — clear session and redirect to login (SaaS / admin session expired)
                    localStorage.removeItem(AUTH_TOKEN_KEY)
                    localStorage.removeItem(USER_KEY)
                    window.location.href = "/login"
                    return@handleResponseExceptionWithRequest
                }
                throw exception
            }
        }
    }
    
    internal fun getApiPath(path: String): String {
        val baseUrl = AppConfig.apiBaseUrl
        return if (baseUrl.isEmpty() || baseUrl == "http://localhost" || baseUrl == "https://localhost") {
            "${ApiConstants.API_BASE_PATH}$path"
        } else {
            "$baseUrl${ApiConstants.API_BASE_PATH}$path"
        }
    }
    
    /** Admin API path (no API_BASE_PATH prefix) for tenants, etc. */
    internal fun getAdminPath(path: String): String {
        val baseUrl = AppConfig.apiBaseUrl
        return if (baseUrl.isEmpty() || baseUrl == "http://localhost" || baseUrl == "https://localhost") {
            "/admin$path"
        } else {
            "$baseUrl/admin$path"
        }
    }

    /** Auth API path for login (no API key required). */
    internal fun getAuthPath(path: String): String {
        val base = AppConfig.apiBaseUrl.trimEnd('/')
        return if (base.isEmpty()) path else "$base$path"
    }
    
    private fun getApiKey(): String? {
        (js("window.ENV_API_KEY") as? String)?.takeIf { it.isNotBlank() }?.let { return it }
        return localStorage.getItem(API_KEY_STORAGE_KEY)?.takeIf { it.isNotBlank() }
    }

    private fun getAuthToken(): String? = localStorage.getItem(AUTH_TOKEN_KEY)

    private fun getAdminApiKey(): String? {
        (js("window.ENV_ADMIN_API_KEY") as? String)?.takeIf { it.isNotBlank() }?.let { return it }
        return localStorage.getItem("admin_api_key")?.takeIf { it.isNotBlank() }
    }

    private fun getTenantId(): String? =
        localStorage.getItem("current_tenant")?.takeIf { it.isNotBlank() }

    /**
     * Admin login (email/password). Returns token and user. Throws on 401.
     */
    suspend fun login(email: String, password: String): LoginResponse {
        return client.post(getAuthPath("/auth/login")) {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(email = email, password = password))
        }.body()
    }
    
    /**
     * Get flags with optional filters and pagination.
     * Returns pair of (flags, totalCount). totalCount from X-Total-Count header.
     */
    suspend fun getFlags(
        limit: Int? = null,
        offset: Int = 0,
        enabled: Boolean? = null,
        descriptionLike: String? = null,
        key: String? = null,
        tags: String? = null
    ): Pair<List<FlagResponse>, Long> {
        val response = client.get(getApiPath("/flags")) {
            limit?.let { parameter("limit", it) }
            if (offset > 0) parameter("offset", offset)
            enabled?.let { parameter("enabled", it) }
            descriptionLike?.takeIf { it.isNotBlank() }?.let { parameter("descriptionLike", it) }
            key?.takeIf { it.isNotBlank() }?.let { parameter("key", it) }
            tags?.takeIf { it.isNotBlank() }?.let { parameter("tags", it) }
            parameter("preload", false)
        }
        val total = response.headers["X-Total-Count"]?.toLongOrNull() ?: 0L
        return response.body<List<FlagResponse>>() to total
    }

    /**
     * Get all tags (for filter dropdown)
     */
    suspend fun getTags(limit: Int? = 500, valueLike: String? = null): List<TagResponse> {
        return client.get(getApiPath("/tags")) {
            limit?.let { parameter("limit", it) }
            valueLike?.takeIf { it.isNotBlank() }?.let { parameter("value_like", it) }
        }.body()
    }
    
    /**
     * Get flag by ID
     */
    suspend fun getFlag(id: Int): FlagResponse {
        return client.get(getApiPath("/flags/$id")).body()
    }
    
    /**
     * Create flag
     */
    suspend fun createFlag(flag: CreateFlagRequest): FlagResponse {
        return client.post(getApiPath("/flags")) {
            contentType(ContentType.Application.Json)
            setBody(flag)
        }.body()
    }
    
    /**
     * Update flag
     */
    suspend fun updateFlag(id: Int, flag: UpdateFlagRequest): FlagResponse {
        return client.put(getApiPath("/flags/$id")) {
            contentType(ContentType.Application.Json)
            setBody(flag)
        }.body()
    }
    
    /**
     * Update flag with full PutFlagRequest
     */
    suspend fun updateFlagFull(id: Int, flag: PutFlagRequest): FlagResponse {
        return client.put(getApiPath("/flags/$id")) {
            contentType(ContentType.Application.Json)
            setBody(flag)
        }.body()
    }
    
    /**
     * Delete flag
     */
    suspend fun deleteFlag(id: Int) {
        client.delete(getApiPath("/flags/$id"))
    }
    
    /**
     * Evaluate flag
     */
    suspend fun evaluate(request: EvaluationRequest): EvaluationResponse {
        return client.post(getApiPath("/evaluation")) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
    
    // Segments
    suspend fun getSegments(flagId: Int): List<SegmentResponse> {
        return client.get(getApiPath("/flags/$flagId/segments")).body()
    }
    
    suspend fun createSegment(flagId: Int, request: CreateSegmentRequest): SegmentResponse {
        return client.post(getApiPath("/flags/$flagId/segments")) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
    
    suspend fun updateSegment(flagId: Int, segmentId: Int, request: PutSegmentRequest): SegmentResponse {
        return client.put(getApiPath("/flags/$flagId/segments/$segmentId")) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
    
    suspend fun deleteSegment(flagId: Int, segmentId: Int) {
        client.delete(getApiPath("/flags/$flagId/segments/$segmentId"))
    }
    
    suspend fun reorderSegments(flagId: Int, request: PutSegmentReorderRequest) {
        client.put(getApiPath("/flags/$flagId/segments/reorder")) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }
    
    // Constraints
    suspend fun getConstraints(flagId: Int, segmentId: Int): List<ConstraintResponse> {
        return client.get(getApiPath("/flags/$flagId/segments/$segmentId/constraints")).body()
    }
    
    suspend fun createConstraint(flagId: Int, segmentId: Int, request: CreateConstraintRequest): ConstraintResponse {
        return client.post(getApiPath("/flags/$flagId/segments/$segmentId/constraints")) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
    
    suspend fun updateConstraint(flagId: Int, segmentId: Int, constraintId: Int, request: PutConstraintRequest): ConstraintResponse {
        return client.put(getApiPath("/flags/$flagId/segments/$segmentId/constraints/$constraintId")) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
    
    suspend fun deleteConstraint(flagId: Int, segmentId: Int, constraintId: Int) {
        client.delete(getApiPath("/flags/$flagId/segments/$segmentId/constraints/$constraintId"))
    }
    
    // Distributions
    suspend fun getDistributions(flagId: Int, segmentId: Int): List<DistributionResponse> {
        return client.get(getApiPath("/flags/$flagId/segments/$segmentId/distributions")).body()
    }
    
    suspend fun updateDistributions(flagId: Int, segmentId: Int, request: PutDistributionsRequest): List<DistributionResponse> {
        return client.put(getApiPath("/flags/$flagId/segments/$segmentId/distributions")) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
    
    // Variants
    suspend fun getVariants(flagId: Int): List<VariantResponse> {
        return client.get(getApiPath("/flags/$flagId/variants")).body()
    }
    
    suspend fun createVariant(flagId: Int, request: CreateVariantRequest): VariantResponse {
        return client.post(getApiPath("/flags/$flagId/variants")) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
    
    suspend fun updateVariant(flagId: Int, variantId: Int, request: PutVariantRequest): VariantResponse {
        return client.put(getApiPath("/flags/$flagId/variants/$variantId")) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
    
    suspend fun deleteVariant(flagId: Int, variantId: Int) {
        client.delete(getApiPath("/flags/$flagId/variants/$variantId"))
    }
    
    // Flag Snapshots (History)
    suspend fun getFlagSnapshots(flagId: Int, limit: Int? = null, offset: Int = 0): List<FlagSnapshotResponse> {
        val url = buildString {
            append(getApiPath("/flags/$flagId/snapshots"))
            if (limit != null) append("?limit=$limit")
            if (offset > 0) append(if (limit != null) "&offset=$offset" else "?offset=$offset")
        }
        return client.get(url).body()
    }
    
    // Deleted Flags
    suspend fun getDeletedFlags(): List<FlagResponse> {
        return client.get(getApiPath("/flags?deleted=true")).body()
    }
    
    suspend fun restoreFlag(flagId: Int): FlagResponse {
        return client.put(getApiPath("/flags/$flagId/restore")).body()
    }
    
    // Tags
    suspend fun getAllTags(): List<TagResponse> {
        return client.get(getApiPath("/tags")).body()
    }
    
    suspend fun addTagToFlag(flagId: Int, tagValue: String): TagResponse {
        return client.post(getApiPath("/flags/$flagId/tags")) {
            contentType(ContentType.Application.Json)
            setBody(CreateTagRequest(tagValue))
        }.body()
    }
    
    suspend fun removeTagFromFlag(flagId: Int, tagId: Int) {
        client.delete(getApiPath("/flags/$flagId/tags/$tagId"))
    }
    
    // Entity Types
    suspend fun getEntityTypes(): List<String> {
        return client.get(getApiPath("/flags/entity_types")).body()
    }
    
    // Batch Evaluation
    suspend fun evaluateBatch(request: EvaluationBatchRequest): EvaluationBatchResponse {
        return client.post(getApiPath("/evaluation/batch")) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
    
    /**
     * Get server version info (no auth required for /info).
     */
    suspend fun getInfo(): InfoResponse {
        return client.get(getApiPath("/info")).body()
    }

    /**
     * Download export file with auth headers. Use for eval_cache, gitops, sqlite.
     */
    suspend fun downloadExport(path: String): ByteArray {
        return client.get(getApiPath(path)).bodyAsBytes()
    }

    /**
     * Import flags from GitOps format (YAML or JSON).
     */
    suspend fun importFlags(format: String, content: String): ImportResult {
        return client.post(getApiPath("/import")) {
            contentType(ContentType.Application.Json)
            setBody(ImportRequest(format = format, content = content))
        }.body()
    }

    // Set Flag Enabled
    suspend fun setFlagEnabled(flagId: Int, enabled: Boolean): FlagResponse {
        return client.put(getApiPath("/flags/$flagId/enabled")) {
            contentType(ContentType.Application.Json)
            setBody(SetFlagEnabledRequest(enabled))
        }.body()
    }

    suspend fun batchSetFlagEnabled(ids: List<Int>, enabled: Boolean): List<FlagResponse> {
        return client.put(getApiPath("/flags/batch/enabled")) {
            contentType(ContentType.Application.Json)
            setBody(BatchFlagEnabledRequest(ids = ids, enabled = enabled))
        }.body()
    }
    
    // ========== Tenants (Admin API) ==========
    
    suspend fun getTenants(includeDeleted: Boolean = false): List<TenantResponse> {
        val url = getAdminPath("/tenants") + if (includeDeleted) "?includeDeleted=true" else ""
        return client.get(url) {
            if (getAuthToken() == null) getAdminApiKey()?.let { header(ADMIN_API_KEY_HEADER, it) }
        }.body()
    }
    
    suspend fun getTenant(key: String): TenantResponse {
        return client.get(getAdminPath("/tenants/$key")) {
            if (getAuthToken() == null) getAdminApiKey()?.let { header(ADMIN_API_KEY_HEADER, it) }
        }.body()
    }
    
    suspend fun createTenant(request: CreateTenantRequest): CreateTenantResponse {
        return client.post(getAdminPath("/tenants")) {
            contentType(ContentType.Application.Json)
            setBody(request)
            if (getAuthToken() == null) getAdminApiKey()?.let { header(ADMIN_API_KEY_HEADER, it) }
        }.body()
    }
    
    suspend fun deleteTenant(id: Long, immediate: Boolean = false) {
        val url = getAdminPath("/tenants/$id") + if (immediate) "?immediate=true" else ""
        client.delete(url) {
            if (getAuthToken() == null) getAdminApiKey()?.let { header(ADMIN_API_KEY_HEADER, it) }
        }
    }
    
    // ========== Billing (Enterprise, requires auth) ==========
    
    internal fun getBillingPath(path: String): String {
        val baseUrl = AppConfig.apiBaseUrl
        return if (baseUrl.isEmpty() || baseUrl == "http://localhost" || baseUrl == "https://localhost") {
            "/api/billing$path"
        } else {
            "$baseUrl/api/billing$path"
        }
    }
    
    suspend fun createBillingCheckout(request: CreateCheckoutSessionRequest): CreateCheckoutSessionResponse {
        return client.post(getBillingPath("/checkout")) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
    
    suspend fun createBillingPortal(request: CreatePortalSessionRequest): CreatePortalSessionResponse {
        return client.post(getBillingPath("/portal")) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
    
    suspend fun getBillingSubscription(): SubscriptionResponse? {
        return try {
            client.get(getBillingPath("/subscription")).body<SubscriptionResponse>()
        } catch (e: Exception) {
            null
        }
    }
    
    suspend fun getBillingInvoices(limit: Int = 10): List<InvoiceResponse> {
        return client.get(getBillingPath("/invoices?limit=$limit")).body()
    }
    
    suspend fun getBillingUsage(startTime: String? = null, endTime: String? = null): UsageStatsResponse {
        val params = mutableListOf<String>()
        startTime?.let { params.add("startTime=$it") }
        endTime?.let { params.add("endTime=$it") }
        val query = if (params.isEmpty()) "" else "?${params.joinToString("&")}"
        return client.get(getBillingPath("/usage$query")).body()
    }
    
    suspend fun cancelBillingSubscription(cancelAtPeriodEnd: Boolean = true) {
        client.post(getBillingPath("/subscription/cancel?cancelAtPeriodEnd=$cancelAtPeriodEnd"))
    }
    
    // ========== SSO (Enterprise, requires tenant context) ==========
    
    /** Path for tenant-scoped routes (GET /tenants/me/users, etc.). */
    internal fun getTenantMePath(path: String): String {
        val baseUrl = AppConfig.apiBaseUrl.trimEnd('/')
        return if (baseUrl.isEmpty() || baseUrl == "http://localhost" || baseUrl == "https://localhost") {
            "/tenants/me$path"
        } else {
            "$baseUrl/tenants/me$path"
        }
    }

    /** Path for tenant-scoped SSO providers list (GET /tenants/me/sso/providers). */
    internal fun getSsoProvidersListPath(): String = getTenantMePath("/sso/providers")

    internal fun getSsoPath(path: String): String {
        val baseUrl = AppConfig.apiBaseUrl
        return if (baseUrl.isEmpty() || baseUrl == "http://localhost" || baseUrl == "https://localhost") {
            "/sso/providers$path"
        } else {
            "$baseUrl/sso/providers$path"
        }
    }

    internal fun getSsoAuthPath(path: String): String {
        val baseUrl = AppConfig.apiBaseUrl
        return if (baseUrl.isEmpty() || baseUrl == "http://localhost" || baseUrl == "https://localhost") {
            "/sso$path"
        } else {
            "$baseUrl/sso$path"
        }
    }

    suspend fun ssoLogout() {
        client.post(getSsoAuthPath("/logout"))
    }
    
    suspend fun getSsoProviders(): List<SsoProviderResponse> {
        return client.get(getSsoProvidersListPath()).body()
    }
    
    suspend fun createSsoProvider(request: CreateSsoProviderRequest): SsoProviderResponse {
        return client.post(getSsoPath("")) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
    
    // ========== Slack (Enterprise, webhook via env) ==========
    
    suspend fun getSlackStatus(): SlackStatusResponse {
        return client.get(getApiPath("/slack/status")).body()
    }
    
    suspend fun sendSlackTestNotification(): SlackTestResponse {
        return client.post(getApiPath("/slack/test")).body()
    }

    // ========== Webhooks ==========

    suspend fun getWebhooks(): List<WebhookResponse> {
        return client.get(getApiPath("/webhooks")).body()
    }

    suspend fun createWebhook(request: CreateWebhookRequest): WebhookResponse {
        return client.post(getApiPath("/webhooks")) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun updateWebhook(id: Int, request: PutWebhookRequest): WebhookResponse {
        return client.put(getApiPath("/webhooks/$id")) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun deleteWebhook(id: Int) {
        client.delete(getApiPath("/webhooks/$id"))
    }

    // ========== RBAC (Enterprise) ==========

    suspend fun getRoles(): RolesResponse {
        return client.get(getApiPath("/roles")).body()
    }

    suspend fun createRole(key: String, name: String, permissions: List<String>) {
        client.post(getApiPath("/roles")) {
            contentType(ContentType.Application.Json)
            setBody(mapOf("key" to key, "name" to name, "permissions" to permissions))
        }
    }

    suspend fun getTenantUsers(): List<TenantUserResponse> {
        return client.get(getTenantMePath("/users")).body()
    }

    suspend fun assignUserRole(userId: Long, roleKey: String?, customRoleId: Long?) {
        client.put(getTenantMePath("/users/$userId/roles")) {
            contentType(ContentType.Application.Json)
            setBody(AssignRoleRequest(roleKey = roleKey, customRoleId = customRoleId))
        }
    }

    // ========== Metrics overview (global aggregates) ==========

    /**
     * Get A/B experiment insights: conversion by variant, confidence intervals, significance.
     */
    suspend fun getExperimentInsights(
        flagId: Int,
        startTime: Long,
        endTime: Long
    ): ExperimentInsightsResponse {
        val url = "${getApiPath("/flags/$flagId/experiment-insights")}?start=$startTime&end=$endTime"
        return client.get(url).body()
    }

    /**
     * Get analytics events overview (Firebase-level: totalEvents, uniqueUsers, topEvents, timeSeries, dauByDay).
     */
    suspend fun getAnalyticsOverview(
        startTime: Long? = null,
        endTime: Long? = null,
        topLimit: Int = 20,
        timeBucketMs: Long = 3600_000
    ): AnalyticsOverviewResponse {
        val end = endTime ?: (kotlin.js.Date().getTime().toLong())
        val start = startTime ?: (end - 86400_000)
        val url = "${getApiPath("/analytics/overview")}?start=$start&end=$end&topLimit=$topLimit&timeBucketMs=$timeBucketMs"
        return client.get(url).body()
    }

    /**
     * Get global metrics overview: time series (evaluations per bucket) and top flags.
     * @param startTime start of range (ms), default last 24h
     * @param endTime end of range (ms), default now
     * @param topLimit max top flags (default 10)
     * @param timeBucketMs bucket size in ms (default 3600000 = 1h)
     */
    suspend fun getMetricsOverview(
        startTime: Long? = null,
        endTime: Long? = null,
        topLimit: Int = 10,
        timeBucketMs: Long = 3600_000
    ): GlobalMetricsOverviewResponse {
        val end = endTime ?: (kotlin.js.Date().getTime().toLong())
        val start = startTime ?: (end - 86400_000)
        val url = buildString {
            append(getApiPath("/metrics/overview"))
            append("?start=$start&end=$end&topLimit=$topLimit&timeBucketMs=$timeBucketMs")
        }
        return client.get(url).body()
    }

    /**
     * Get crash reports (Enterprise). Requires X-API-Key or Bearer token.
     */
    suspend fun getCrashes(
        startTime: Long? = null,
        endTime: Long? = null,
        limit: Int = 50,
        offset: Int = 0
    ): CrashListResponse {
        val url = buildString {
            append(getApiPath("/crashes"))
            append("?limit=$limit&offset=$offset")
            startTime?.let { append("&start=$it") }
            endTime?.let { append("&end=$it") }
        }
        return client.get(url).body()
    }

    /**
     * Get per-flag evaluation stats (Core OSS only): API evaluation count and time series.
     */
    suspend fun getFlagEvaluationStats(
        flagId: Int,
        startTime: Long,
        endTime: Long,
        timeBucketMs: Long = 3600_000
    ): FlagEvaluationStatsResponse {
        val url = buildString {
            append(getApiPath("/flags/$flagId/evaluation-stats"))
            append("?start=$startTime&end=$endTime&timeBucketMs=$timeBucketMs")
        }
        return client.get(url).body()
    }
}

@Serializable
data class WebhookResponse(
    val id: Int,
    val url: String,
    val events: List<String>,
    val secret: String? = null,
    val enabled: Boolean = true,
    val tenantId: String? = null,
    val createdAt: Long = 0,
    val updatedAt: Long = 0
)

@Serializable
data class CreateWebhookRequest(
    val url: String,
    val events: List<String>,
    val secret: String? = null,
    val enabled: Boolean = true
)

@Serializable
data class PutWebhookRequest(
    val url: String,
    val events: List<String>,
    val secret: String? = null,
    val enabled: Boolean = true
)

@Serializable
data class LoginRequest(val email: String = "", val password: String = "")

@Serializable
data class LoginUserResponse(val id: String, val email: String, val name: String)

@Serializable
data class LoginResponse(val token: String, val user: LoginUserResponse)

@Serializable
data class ImportRequest(val format: String = "json", val content: String = "")

@Serializable
data class ImportResult(
    val created: Int = 0,
    val updated: Int = 0,
    val errors: List<String> = emptyList()
)
