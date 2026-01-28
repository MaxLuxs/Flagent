package flagent.middleware

import flagent.domain.entity.TenantPlan
import flagent.domain.entity.TenantRole
import flagent.domain.entity.isExpired
import flagent.domain.value.TenantContext
import flagent.repository.impl.TenantRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import java.security.MessageDigest

/**
 * TenantContextMiddleware - extracts and validates tenant context.
 *
 * Flow:
 * 1. Extract API key from header
 * 2. Resolve tenant from API key
 * 3. Validate tenant status (active, not suspended)
 * 4. Attach tenant context to request
 * 5. Track API usage
 *
 * Headers:
 * - X-API-Key: API key for authentication
 * - X-Tenant-Key: Tenant key for public APIs (optional)
 */

val TenantContextKey = AttributeKey<TenantContext>("TenantContext")

private val logger = LoggerFactory.getLogger("TenantContextMiddleware")

class TenantContextMiddleware(
    private val tenantRepository: TenantRepository
) {
    
    /**
     * Configure tenant context extraction.
     */
    fun configure(application: Application) {
        application.intercept(ApplicationCallPipeline.Plugins) {
            val requestPath = call.request.local.uri
            
            // Skip tenant context for public endpoints
            if (shouldSkipTenantContext(requestPath)) {
                return@intercept
            }
            
            // Extract API key
            val apiKey = call.request.headers["X-API-Key"]
            
            if (apiKey.isNullOrBlank()) {
                logger.warn("Missing X-API-Key header for: $requestPath")
                call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf("error" to "Missing X-API-Key header")
                )
                finish()
                return@intercept
            }
            
            // Resolve tenant from API key
            val tenantContext = withContext(Dispatchers.IO) {
                resolveTenantFromApiKey(apiKey)
            }
            
            if (tenantContext == null) {
                logger.warn("Invalid API key: ${apiKey.take(10)}...")
                call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf("error" to "Invalid API key")
                )
                finish()
                return@intercept
            }
            
            // Attach tenant context to request
            call.attributes.put(TenantContextKey, tenantContext)
            
            logger.debug("Tenant context resolved: tenant_id=${tenantContext.tenantId}, tenant_key=${tenantContext.tenantKey}")
            
            // Track API usage (async, non-blocking)
            withContext(Dispatchers.IO) {
                try {
                    tenantRepository.incrementApiCalls(tenantContext.tenantId)
                } catch (e: Exception) {
                    logger.error("Failed to track API usage", e)
                }
            }
        }
    }
    
    /**
     * Resolve tenant context from API key.
     *
     * @param apiKey Raw API key (not hashed)
     * @return TenantContext or null if invalid
     */
    private suspend fun resolveTenantFromApiKey(apiKey: String): TenantContext? {
        // Hash API key (SHA-256)
        val keyHash = hashApiKey(apiKey)
        
        // Find API key in database
        val apiKeyEntity = tenantRepository.findApiKeyByHash(keyHash) ?: return null
        
        // Check if key is expired
        if (apiKeyEntity.isExpired()) {
            logger.warn("Expired API key: ${keyHash.take(10)}...")
            return null
        }
        
        // Load tenant
        val tenant = tenantRepository.findById(apiKeyEntity.tenantId) ?: return null
        
        // Check tenant status
        if (tenant.status != flagent.domain.entity.TenantStatus.ACTIVE) {
            logger.warn("Inactive tenant: ${tenant.key} (status=${tenant.status})")
            return null
        }
        
        // Update last used timestamp
        tenantRepository.updateApiKeyLastUsed(apiKeyEntity.id)
        
        // Return tenant context
        return TenantContext(
            tenantId = tenant.id,
            tenantKey = tenant.key,
            schemaName = tenant.schemaName,
            plan = tenant.plan,
            userId = null, // API key doesn't have user context
            userEmail = null,
            role = TenantRole.ADMIN // API keys have admin-level access
        )
    }
    
    /**
     * Hash API key using SHA-256.
     *
     * @param apiKey Raw API key
     * @return Hashed key (hex string)
     */
    private fun hashApiKey(apiKey: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(apiKey.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }
    
    /**
     * Check if request path should skip tenant context.
     *
     * Public endpoints that don't require tenant authentication:
     * - Health check
     * - Metrics
     * - Documentation
     * - Admin tenant management APIs
     * - SSO auth (login, callback, validate, logout) - used before tenant context exists
     */
    private fun shouldSkipTenantContext(path: String): Boolean {
        return path.startsWith("/health") ||
                path.startsWith("/metrics") ||
                path.startsWith("/api-docs") ||
                path.startsWith("/admin/tenants") || // Admin tenant provisioning
                path.startsWith("/swagger") ||
                path.startsWith("/sso/login") ||
                path.startsWith("/sso/callback") ||
                path.startsWith("/sso/validate") ||
                path.startsWith("/sso/logout") ||
                path.startsWith("/auth/")
    }
}

/**
 * Extension to get tenant context from call.
 *
 * @throws IllegalStateException if tenant context not found
 */
fun ApplicationCall.tenantContext(): TenantContext {
    return attributes.getOrNull(TenantContextKey)
        ?: throw IllegalStateException("Tenant context not found. Make sure TenantContextMiddleware is configured.")
}

/**
 * Extension to get tenant context or null.
 */
fun ApplicationCall.tenantContextOrNull(): TenantContext? {
    return attributes.getOrNull(TenantContextKey)
}
