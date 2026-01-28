package flagent.route

import flagent.domain.entity.ApiKeyScope
import flagent.domain.entity.TenantPlan
import flagent.domain.entity.TenantRole
import flagent.middleware.tenantContext
import flagent.service.TenantProvisioningService
import flagent.service.dto.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

private val logger = LoggerFactory.getLogger("TenantRoutes")

/**
 * Tenant management routes.
 *
 * Admin API (no tenant context required):
 * - POST /admin/tenants - Create tenant
 * - GET /admin/tenants - List all tenants
 * - GET /admin/tenants/{key} - Get tenant info
 * - DELETE /admin/tenants/{id} - Delete tenant
 *
 * Tenant API (requires tenant context):
 * - GET /tenants/me - Get current tenant info
 * - PUT /tenants/me/plan - Update tenant plan
 * - GET /tenants/me/users - List users
 * - POST /tenants/me/users - Add user
 * - DELETE /tenants/me/users/{userId} - Remove user
 * - GET /tenants/me/api-keys - List API keys
 * - POST /tenants/me/api-keys - Generate API key
 * - DELETE /tenants/me/api-keys/{keyId} - Delete API key
 * - GET /tenants/me/usage - Get usage stats
 */
fun Route.tenantRoutes(provisioningService: TenantProvisioningService) {
    
    // ============================================================================
    // ADMIN API (no tenant context required)
    // ============================================================================
    
    route("/admin/tenants") {
        
        /**
         * Create a new tenant.
         *
         * POST /admin/tenants
         * Body: { key, name, plan, ownerEmail }
         * Response: { tenant, ownerUser, apiKey }
         */
        post {
            try {
                val request = call.receive<CreateTenantRequest>()
                
                // Validate plan
                val plan = try {
                    TenantPlan.valueOf(request.plan.uppercase())
                } catch (e: IllegalArgumentException) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Invalid plan: ${request.plan}")
                    )
                    return@post
                }
                
                // Create tenant
                val result = provisioningService.createTenant(
                    key = request.key,
                    name = request.name,
                    plan = plan,
                    ownerEmail = request.ownerEmail
                )
                
                logger.info("Created tenant: key=${result.tenant.key}, id=${result.tenant.id}")
                
                call.respond(
                    HttpStatusCode.Created,
                    CreateTenantResponse(
                        tenant = result.tenant.toDTO(),
                        ownerUser = result.ownerUser.toDTO(),
                        apiKey = result.apiKey
                    )
                )
                
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            } catch (e: Exception) {
                logger.error("Failed to create tenant", e)
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to create tenant"))
            }
        }
        
        /**
         * List all tenants.
         *
         * GET /admin/tenants?includeDeleted=false
         * Response: [{ tenant }, ...]
         */
        get {
            try {
                val includeDeleted = call.request.queryParameters["includeDeleted"]?.toBoolean() ?: false
                
                val tenants = provisioningService.listTenants(includeDeleted)
                
                call.respond(HttpStatusCode.OK, tenants.map { it.toDTO() })
                
            } catch (e: Exception) {
                logger.error("Failed to list tenants", e)
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to list tenants"))
            }
        }
        
        /**
         * Get tenant by key.
         *
         * GET /admin/tenants/{key}
         * Response: { tenant }
         */
        get("/{key}") {
            try {
                val key = call.parameters["key"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing tenant key"))
                
                val tenant = provisioningService.getTenant(key)
                    ?: return@get call.respond(HttpStatusCode.NotFound, mapOf("error" to "Tenant not found"))
                
                call.respond(HttpStatusCode.OK, tenant.toDTO())
                
            } catch (e: Exception) {
                logger.error("Failed to get tenant", e)
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to get tenant"))
            }
        }
        
        /**
         * Delete tenant.
         *
         * DELETE /admin/tenants/{id}?immediate=false
         * Response: 204 No Content
         */
        delete("/{id}") {
            try {
                val id = call.parameters["id"]?.toLongOrNull()
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid tenant ID"))
                
                val immediate = call.request.queryParameters["immediate"]?.toBoolean() ?: false
                
                provisioningService.deleteTenant(id, immediate)
                
                logger.info("Deleted tenant: id=$id, immediate=$immediate")
                
                call.respond(HttpStatusCode.NoContent)
                
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to e.message))
            } catch (e: Exception) {
                logger.error("Failed to delete tenant", e)
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to delete tenant"))
            }
        }
    }
    
    // ============================================================================
    // TENANT API (requires tenant context)
    // ============================================================================
    
    route("/tenants/me") {
        
        /**
         * Get current tenant info.
         *
         * GET /tenants/me
         * Header: X-API-Key: xxx
         * Response: { tenant }
         */
        get {
            try {
                val tenantContext = call.tenantContext()
                
                val tenant = provisioningService.getTenant(tenantContext.tenantKey)
                    ?: return@get call.respond(HttpStatusCode.NotFound, mapOf("error" to "Tenant not found"))
                
                call.respond(HttpStatusCode.OK, tenant.toDTO())
                
            } catch (e: Exception) {
                logger.error("Failed to get tenant info", e)
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to get tenant info"))
            }
        }
        
        /**
         * Update tenant plan.
         *
         * PUT /tenants/me/plan
         * Body: { plan }
         * Response: { tenant }
         */
        put("/plan") {
            try {
                val tenantContext = call.tenantContext()
                
                // Check if user has permission
                if (!tenantContext.canManageBilling()) {
                    call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Insufficient permissions"))
                    return@put
                }
                
                val request = call.receive<UpdateTenantPlanRequest>()
                
                val plan = try {
                    TenantPlan.valueOf(request.plan.uppercase())
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid plan"))
                    return@put
                }
                
                provisioningService.updateTenantPlan(tenantContext.tenantId, plan)
                
                val tenant = provisioningService.getTenant(tenantContext.tenantKey)
                
                if (tenant != null) {
                    call.respond(HttpStatusCode.OK, tenant.toDTO())
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Tenant not found"))
                }
                
            } catch (e: Exception) {
                logger.error("Failed to update tenant plan", e)
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to update plan"))
            }
        }
        
        /**
         * Add user to tenant.
         *
         * POST /tenants/me/users
         * Body: { email, role }
         * Response: { user }
         */
        post("/users") {
            try {
                val tenantContext = call.tenantContext()
                
                // Check if user has permission
                if (!tenantContext.canManageUsers()) {
                    call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Insufficient permissions"))
                    return@post
                }
                
                val request = call.receive<AddUserRequest>()
                
                val role = try {
                    TenantRole.valueOf(request.role.uppercase())
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid role"))
                    return@post
                }
                
                val user = provisioningService.addUser(
                    tenantId = tenantContext.tenantId,
                    email = request.email,
                    role = role
                )
                
                call.respond(HttpStatusCode.Created, user.toDTO())
                
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            } catch (e: Exception) {
                logger.error("Failed to add user", e)
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to add user"))
            }
        }
        
        /**
         * Generate API key.
         *
         * POST /tenants/me/api-keys
         * Body: { name, scopes, expiresAt? }
         * Response: { apiKey, apiKeyInfo }
         */
        post("/api-keys") {
            try {
                val tenantContext = call.tenantContext()
                
                // Check if user has permission
                if (!tenantContext.canManageUsers()) {
                    call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Insufficient permissions"))
                    return@post
                }
                
                val request = call.receive<CreateApiKeyRequest>()
                
                val scopes = try {
                    request.scopes.map { ApiKeyScope.valueOf(it.uppercase()) }
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid scope"))
                    return@post
                }
                
                val expiresAt = request.expiresAt?.let { LocalDateTime.parse(it) }
                
                val (apiKey, apiKeyEntity) = provisioningService.generateApiKey(
                    tenantId = tenantContext.tenantId,
                    name = request.name,
                    scopes = scopes,
                    expiresAt = expiresAt
                )
                
                call.respond(
                    HttpStatusCode.Created,
                    CreateApiKeyResponse(
                        apiKey = apiKey,
                        apiKeyInfo = apiKeyEntity.toDTO()
                    )
                )
                
            } catch (e: Exception) {
                logger.error("Failed to generate API key", e)
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to generate API key"))
            }
        }
    }
}
