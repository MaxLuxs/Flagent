package flagent.repository

import flagent.domain.value.TenantContext
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction

/**
 * TenantAwareRepository - base class for tenant-aware repositories.
 *
 * Implements schema-per-tenant isolation by:
 * 1. Setting search_path to tenant schema before query
 * 2. Ensuring all operations execute in tenant context
 * 3. Preventing cross-tenant data access
 *
 * Usage:
 * ```kotlin
 * class FlagRepository : TenantAwareRepository() {
 *     suspend fun getFlags(tenantContext: TenantContext): List<Flag> {
 *         return tenantTransaction(tenantContext) {
 *             // Query executes in tenant schema
 *             Flags.selectAll().map { it.toFlag() }
 *         }
 *     }
 * }
 * ```
 *
 * Security:
 * - Every repository method MUST accept TenantContext
 * - All database operations MUST use tenantTransaction
 * - Never bypass tenant context (security risk!)
 */
abstract class TenantAwareRepository {
    
    /**
     * Execute database query in tenant schema.
     *
     * Sets search_path to tenant schema before executing query.
     * Ensures strong tenant isolation.
     *
     * @param tenantContext Tenant context with schema name
     * @param statement Query to execute
     * @return Query result
     */
    protected suspend fun <T> tenantTransaction(
        tenantContext: TenantContext,
        statement: suspend () -> T
    ): T {
        return suspendTransaction() {
            // Set search_path for this transaction
            // Format: SET search_path TO tenant_schema, public
            // - tenant_schema: tenant tables
            // - public: shared tables (if needed)
            exec("SET search_path TO ${tenantContext.schemaName}, public")
            
            // Execute query in tenant schema
            statement()
        }
    }
    
    /**
     * Execute database query without tenant context.
     *
     * Use ONLY for:
     * - Public schema operations (tenant metadata)
     * - Admin operations
     * - Cross-tenant queries
     *
     * WARNING: Be careful with this method!
     * Most operations should use tenantTransaction.
     */
    protected suspend fun <T> publicTransaction(
        statement: suspend () -> T
    ): T {
        return suspendTransaction() {
            // Explicitly set search_path to public schema
            exec("SET search_path TO public")
            
            statement()
        }
    }
}

/**
 * Helper to validate tenant context.
 *
 * Throws exception if tenant context is invalid.
 */
fun validateTenantContext(tenantContext: TenantContext?) {
    if (tenantContext == null) {
        throw IllegalStateException("Tenant context is required")
    }
    
    if (tenantContext.schemaName.isBlank()) {
        throw IllegalStateException("Tenant schema name is empty")
    }
}
