package flagent.service

import flagent.domain.entity.*
import flagent.domain.repository.ITenantRepository
import flagent.repository.tables.*
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.slf4j.LoggerFactory
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.LocalDateTime
import java.util.*

/**
 * TenantProvisioningService - manages tenant lifecycle.
 *
 * Responsibilities:
 * - Create tenant with dedicated schema
 * - Run migrations for tenant schema
 * - Generate initial API key
 * - Delete tenant and cleanup schema
 * - Manage tenant users
 *
 * Schema-per-tenant approach:
 * - Each tenant gets a dedicated PostgreSQL schema (tenant_<id>)
 * - All Flagent tables are created in tenant schema
 * - Strong isolation between tenants
 */
class TenantProvisioningService(
    private val tenantRepository: ITenantRepository
) {
    
    private val logger = LoggerFactory.getLogger(TenantProvisioningService::class.java)
    
    /**
     * Create a new tenant with full provisioning.
     *
     * Steps:
     * 1. Create tenant record
     * 2. Create tenant schema
     * 3. Run migrations (create tables)
     * 4. Add owner user
     * 5. Generate initial API key
     *
     * @param key Tenant key (unique, URL-friendly)
     * @param name Tenant display name
     * @param plan Tenant plan
     * @param ownerEmail Owner email address
     * @return Created tenant
     */
    suspend fun createTenant(
        key: String,
        name: String,
        plan: TenantPlan,
        ownerEmail: String
    ): CreateTenantResult {
        logger.info("Creating tenant: key=$key, plan=$plan, owner=$ownerEmail")
        
        // Validate tenant key
        validateTenantKey(key)
        
        // Check if tenant key already exists
        val existing = tenantRepository.findByKey(key)
        if (existing != null) {
            throw IllegalArgumentException("Tenant with key '$key' already exists")
        }
        
        // Generate unique schema name
        val schemaName = generateSchemaName()
        
        // 1. Create tenant record
        val tenant = tenantRepository.create(
            Tenant(
                key = key,
                name = name,
                plan = plan,
                status = TenantStatus.ACTIVE,
                schemaName = schemaName
            )
        )
        
        logger.info("Created tenant: id=${tenant.id}, schema=$schemaName")
        
        try {
            // 2. Create tenant schema
            createTenantSchema(schemaName)
            logger.info("Created schema: $schemaName")
            
            // 3. Run migrations
            runTenantMigrations(schemaName)
            logger.info("Ran migrations for schema: $schemaName")
            
            // 4. Add owner user
            val ownerUser = tenantRepository.addUser(
                TenantUser(
                    tenantId = tenant.id,
                    email = ownerEmail,
                    role = TenantRole.OWNER
                )
            )
            logger.info("Added owner user: email=$ownerEmail")
            
            // 5. Generate initial API key
            val (apiKey, apiKeyEntity) = generateApiKey(
                tenantId = tenant.id,
                name = "Default API Key",
                scopes = listOf(
                    ApiKeyScope.FLAGS_READ,
                    ApiKeyScope.FLAGS_WRITE,
                    ApiKeyScope.EVALUATE,
                    ApiKeyScope.ANALYTICS_READ
                )
            )
            logger.info("Generated API key: name=${apiKeyEntity.name}")
            
            logger.info("Tenant provisioning completed: id=${tenant.id}")
            
            return CreateTenantResult(
                tenant = tenant,
                ownerUser = ownerUser,
                apiKey = apiKey
            )
            
        } catch (e: Exception) {
            // Rollback: delete tenant and schema
            logger.error("Failed to provision tenant: ${e.message}", e)
            
            try {
                dropTenantSchema(schemaName)
                tenantRepository.delete(tenant.id)
            } catch (rollbackError: Exception) {
                logger.error("Failed to rollback tenant creation", rollbackError)
            }
            
            throw Exception("Failed to create tenant: ${e.message}", e)
        }
    }
    
    /**
     * Delete tenant and cleanup resources.
     *
     * Steps:
     * 1. Soft delete tenant (mark as CANCELLED)
     * 2. Schedule schema deletion after grace period
     * 3. Optionally drop schema immediately
     *
     * @param tenantId Tenant ID
     * @param immediate Drop schema immediately (default: false)
     */
    suspend fun deleteTenant(tenantId: Long, immediate: Boolean = false) {
        logger.info("Deleting tenant: id=$tenantId, immediate=$immediate")
        
        val tenant = tenantRepository.findById(tenantId)
            ?: throw IllegalArgumentException("Tenant not found: $tenantId")
        
        // 1. Soft delete tenant
        tenantRepository.delete(tenantId)
        logger.info("Soft deleted tenant: id=$tenantId")
        
        // 2. Drop schema (if immediate)
        if (immediate) {
            dropTenantSchema(tenant.schemaName)
            logger.info("Dropped schema: ${tenant.schemaName}")
        } else {
            logger.info("Schema will be deleted after grace period: ${tenant.schemaName}")
            // TODO: Implement scheduled cleanup job
        }
    }
    
    /**
     * Add user to tenant.
     *
     * @param tenantId Tenant ID
     * @param email User email
     * @param role User role
     * @return Created user
     */
    suspend fun addUser(
        tenantId: Long,
        email: String,
        role: TenantRole
    ): TenantUser {
        logger.info("Adding user to tenant: tenantId=$tenantId, email=$email, role=$role")
        
        // Check if tenant exists
        tenantRepository.findById(tenantId)
            ?: throw IllegalArgumentException("Tenant not found: $tenantId")
        
        // Check if user already exists
        val existing = tenantRepository.findUser(tenantId, email)
        if (existing != null) {
            throw IllegalArgumentException("User already exists in tenant: $email")
        }
        
        return tenantRepository.addUser(
            TenantUser(
                tenantId = tenantId,
                email = email,
                role = role
            )
        )
    }
    
    /**
     * Generate API key for tenant.
     *
     * @param tenantId Tenant ID
     * @param name API key name
     * @param scopes API key scopes
     * @param expiresAt Expiration timestamp (null = no expiration)
     * @return Pair of (raw API key, stored API key entity)
     */
    suspend fun generateApiKey(
        tenantId: Long,
        name: String,
        scopes: List<ApiKeyScope>,
        expiresAt: LocalDateTime? = null
    ): Pair<String, TenantApiKey> {
        logger.info("Generating API key: tenantId=$tenantId, name=$name")
        
        // Check if tenant exists
        tenantRepository.findById(tenantId)
            ?: throw IllegalArgumentException("Tenant not found: $tenantId")
        
        // Generate random API key
        val apiKey = generateRandomApiKey()
        
        // Hash API key
        val keyHash = hashApiKey(apiKey)
        
        // Store API key
        val apiKeyEntity = tenantRepository.createApiKey(
            TenantApiKey(
                tenantId = tenantId,
                keyHash = keyHash,
                name = name,
                scopes = scopes,
                expiresAt = expiresAt
            )
        )
        
        return Pair(apiKey, apiKeyEntity)
    }
    
    /**
     * List tenants with pagination.
     *
     * @param includeDeleted Include soft-deleted tenants
     * @return List of tenants
     */
    suspend fun listTenants(includeDeleted: Boolean = false): List<Tenant> {
        return tenantRepository.listAll(includeDeleted)
    }
    
    /**
     * Get tenant by key.
     *
     * @param key Tenant key
     * @return Tenant or null
     */
    suspend fun getTenant(key: String): Tenant? {
        return tenantRepository.findByKey(key)
    }
    
    /**
     * Update tenant plan.
     *
     * @param tenantId Tenant ID
     * @param plan New plan
     */
    suspend fun updateTenantPlan(tenantId: Long, plan: TenantPlan) {
        val tenant = tenantRepository.findById(tenantId)
            ?: throw IllegalArgumentException("Tenant not found: $tenantId")
        
        tenantRepository.update(tenant.copy(plan = plan))
        logger.info("Updated tenant plan: tenantId=$tenantId, plan=$plan")
    }
    
    /**
     * Suspend tenant (block access).
     *
     * @param tenantId Tenant ID
     * @param reason Suspension reason
     */
    suspend fun suspendTenant(tenantId: Long, reason: String) {
        val tenant = tenantRepository.findById(tenantId)
            ?: throw IllegalArgumentException("Tenant not found: $tenantId")
        
        tenantRepository.update(tenant.copy(status = TenantStatus.SUSPENDED))
        logger.warn("Suspended tenant: tenantId=$tenantId, reason=$reason")
    }
    
    /**
     * Reactivate suspended tenant.
     *
     * @param tenantId Tenant ID
     */
    suspend fun reactivateTenant(tenantId: Long) {
        val tenant = tenantRepository.findById(tenantId)
            ?: throw IllegalArgumentException("Tenant not found: $tenantId")
        
        tenantRepository.update(tenant.copy(status = TenantStatus.ACTIVE))
        logger.info("Reactivated tenant: tenantId=$tenantId")
    }
    
    // ============================================================================
    // PRIVATE HELPERS
    // ============================================================================
    
    /**
     * Create tenant schema in database.
     */
    private suspend fun createTenantSchema(schemaName: String) {
        suspendTransaction() {
            exec("CREATE SCHEMA IF NOT EXISTS $schemaName")
        }
    }
    
    /**
     * Run migrations for tenant schema.
     *
     * Creates all Flagent tables in tenant schema.
     */
    private suspend fun runTenantMigrations(schemaName: String) {
        suspendTransaction() {
            // Set search_path to tenant schema
            exec("SET search_path TO $schemaName, public")
            
            // Create all Flagent tables in tenant schema
            SchemaUtils.create(
                Flags,
                Segments,
                Variants,
                Constraints,
                Distributions,
                Tags,
                FlagsTags,
                FlagSnapshots,
                FlagEntityTypes,
                Users
            )
        }
    }
    
    /**
     * Drop tenant schema.
     */
    private suspend fun dropTenantSchema(schemaName: String) {
        suspendTransaction() {
            exec("DROP SCHEMA IF EXISTS $schemaName CASCADE")
        }
    }
    
    /**
     * Generate unique schema name.
     *
     * Format: tenant_<uuid>
     */
    private fun generateSchemaName(): String {
        val uuid = UUID.randomUUID().toString().replace("-", "")
        return "tenant_$uuid"
    }
    
    /**
     * Generate random API key.
     *
     * Format: fla_<random_base64>
     * Length: 40 characters
     */
    private fun generateRandomApiKey(): String {
        val random = SecureRandom()
        val bytes = ByteArray(30)
        random.nextBytes(bytes)
        val base64 = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
        return "fla_$base64"
    }
    
    /**
     * Hash API key using SHA-256.
     */
    private fun hashApiKey(apiKey: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(apiKey.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }
    
    /**
     * Validate tenant key format.
     *
     * Rules:
     * - Lowercase alphanumeric and hyphens only
     * - 3-64 characters
     * - No leading/trailing hyphens
     */
    private fun validateTenantKey(key: String) {
        val regex = Regex("^[a-z0-9][a-z0-9-]{1,62}[a-z0-9]$")
        if (!regex.matches(key)) {
            throw IllegalArgumentException(
                "Invalid tenant key format. Must be 3-64 lowercase alphanumeric characters with hyphens."
            )
        }
    }
}

/**
 * Result of tenant creation.
 */
data class CreateTenantResult(
    val tenant: Tenant,
    val ownerUser: TenantUser,
    val apiKey: String
)
