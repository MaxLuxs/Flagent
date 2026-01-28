package flagent.domain.value

import flagent.domain.entity.TenantPlan
import flagent.domain.entity.TenantRole

/**
 * TenantContext - holds tenant information for current request.
 *
 * Extracted from API key or JWT token.
 * Used to:
 * - Set database schema (SET search_path)
 * - Enforce tenant isolation
 * - Track usage per tenant
 * - Apply plan limits
 */
data class TenantContext(
    val tenantId: Long,
    val tenantKey: String,
    val schemaName: String,
    val plan: TenantPlan,
    val userId: Long? = null,
    val userEmail: String? = null,
    val role: TenantRole? = null
) {
    /**
     * Check if tenant has specific plan.
     */
    fun hasPlan(vararg plans: TenantPlan): Boolean {
        return plans.contains(plan)
    }
    
    /**
     * Check if user has specific role.
     */
    fun hasRole(vararg roles: TenantRole): Boolean {
        return role != null && roles.contains(role)
    }
    
    /**
     * Check if user can write (create/update/delete).
     */
    fun canWrite(): Boolean {
        return hasRole(TenantRole.OWNER, TenantRole.ADMIN, TenantRole.MEMBER)
    }
    
    /**
     * Check if user can read.
     */
    fun canRead(): Boolean {
        return role != null // All roles can read
    }
    
    /**
     * Check if user can manage users.
     */
    fun canManageUsers(): Boolean {
        return hasRole(TenantRole.OWNER, TenantRole.ADMIN)
    }
    
    /**
     * Check if user can manage billing.
     */
    fun canManageBilling(): Boolean {
        return hasRole(TenantRole.OWNER)
    }
}
