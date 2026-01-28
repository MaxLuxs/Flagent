package flagent.domain.repository

import flagent.domain.entity.*
import java.time.LocalDate

/**
 * ITenantRepository - repository interface for tenant management.
 *
 * Multi-tenancy operations:
 * - CRUD for tenants
 * - API key management
 * - User management
 * - Usage tracking
 */
interface ITenantRepository {
    
    // ============================================================================
    // TENANT OPERATIONS
    // ============================================================================
    
    /**
     * Create a new tenant.
     *
     * @param tenant Tenant entity
     * @return Created tenant with ID
     */
    suspend fun create(tenant: Tenant): Tenant
    
    /**
     * Find tenant by ID.
     *
     * @param id Tenant ID
     * @return Tenant or null if not found
     */
    suspend fun findById(id: Long): Tenant?
    
    /**
     * Find tenant by key.
     *
     * @param key Tenant key (unique identifier)
     * @return Tenant or null if not found
     */
    suspend fun findByKey(key: String): Tenant?
    
    /**
     * Find tenant by schema name.
     *
     * @param schemaName Schema name
     * @return Tenant or null if not found
     */
    suspend fun findBySchemaName(schemaName: String): Tenant?
    
    /**
     * Find tenant by Stripe customer ID.
     *
     * @param stripeCustomerId Stripe customer ID
     * @return Tenant or null if not found
     */
    suspend fun findByStripeCustomerId(stripeCustomerId: String): Tenant?
    
    /**
     * Update tenant.
     *
     * @param tenant Updated tenant
     * @return Updated tenant
     */
    suspend fun update(tenant: Tenant): Tenant
    
    /**
     * Soft delete tenant.
     *
     * @param id Tenant ID
     */
    suspend fun delete(id: Long)
    
    /**
     * List all tenants.
     *
     * @param includeDeleted Include soft-deleted tenants
     * @return List of tenants
     */
    suspend fun listAll(includeDeleted: Boolean = false): List<Tenant>
    
    // ============================================================================
    // API KEY OPERATIONS
    // ============================================================================
    
    /**
     * Create API key for tenant.
     *
     * @param apiKey API key entity
     * @return Created API key with ID
     */
    suspend fun createApiKey(apiKey: TenantApiKey): TenantApiKey
    
    /**
     * Find API key by hash.
     *
     * @param keyHash Hashed API key
     * @return API key or null if not found
     */
    suspend fun findApiKeyByHash(keyHash: String): TenantApiKey?
    
    /**
     * List API keys for tenant.
     *
     * @param tenantId Tenant ID
     * @return List of API keys
     */
    suspend fun listApiKeys(tenantId: Long): List<TenantApiKey>
    
    /**
     * Update API key last used timestamp.
     *
     * @param apiKeyId API key ID
     */
    suspend fun updateApiKeyLastUsed(apiKeyId: Long)
    
    /**
     * Delete API key.
     *
     * @param apiKeyId API key ID
     */
    suspend fun deleteApiKey(apiKeyId: Long)
    
    // ============================================================================
    // USER OPERATIONS
    // ============================================================================
    
    /**
     * Add user to tenant.
     *
     * @param user Tenant user entity
     * @return Created user with ID
     */
    suspend fun addUser(user: TenantUser): TenantUser
    
    /**
     * Find user by tenant and email.
     *
     * @param tenantId Tenant ID
     * @param email User email
     * @return User or null if not found
     */
    suspend fun findUser(tenantId: Long, email: String): TenantUser?
    
    /**
     * List users for tenant.
     *
     * @param tenantId Tenant ID
     * @return List of users
     */
    suspend fun listUsers(tenantId: Long): List<TenantUser>
    
    /**
     * Update user role.
     *
     * @param userId User ID
     * @param role New role
     */
    suspend fun updateUserRole(userId: Long, role: TenantRole)
    
    /**
     * Remove user from tenant.
     *
     * @param userId User ID
     */
    suspend fun removeUser(userId: Long)
    
    // ============================================================================
    // USAGE TRACKING
    // ============================================================================
    
    /**
     * Increment evaluations count for today.
     *
     * @param tenantId Tenant ID
     * @param count Number of evaluations to add
     */
    suspend fun incrementEvaluations(tenantId: Long, count: Long = 1)
    
    /**
     * Increment API calls count for today.
     *
     * @param tenantId Tenant ID
     * @param count Number of API calls to add
     */
    suspend fun incrementApiCalls(tenantId: Long, count: Long = 1)
    
    /**
     * Update flags count for today.
     *
     * @param tenantId Tenant ID
     * @param count Current number of flags
     */
    suspend fun updateFlagsCount(tenantId: Long, count: Int)
    
    /**
     * Get usage for tenant for specific period.
     *
     * @param tenantId Tenant ID
     * @param periodStart Start date
     * @param periodEnd End date
     * @return List of usage records
     */
    suspend fun getUsage(
        tenantId: Long,
        periodStart: LocalDate,
        periodEnd: LocalDate
    ): List<TenantUsage>
    
    /**
     * Get total usage for tenant (all time).
     *
     * @param tenantId Tenant ID
     * @return Aggregated usage
     */
    suspend fun getTotalUsage(tenantId: Long): TenantUsage
}
