package flagent.repository.impl
import org.jetbrains.exposed.v1.jdbc.*

import flagent.domain.entity.*
import flagent.domain.repository.ITenantRepository
import flagent.repository.tables.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

/**
 * TenantRepository - implementation of tenant management repository.
 *
 * All operations run in public schema (tenant metadata).
 * Tenant data is stored in separate schemas (tenant_<id>).
 */
class TenantRepository : ITenantRepository {
    
    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        withContext(Dispatchers.IO) {
            suspendTransaction { block() }
        }
    
    // ============================================================================
    // TENANT OPERATIONS
    // ============================================================================
    
    override suspend fun create(tenant: Tenant): Tenant = dbQuery {
        val id = Tenants.insert {
            it[key] = tenant.key
            it[name] = tenant.name
            it[plan] = tenant.plan.name
            it[status] = tenant.status.name
            it[tenantSchemaName] = tenant.schemaName
            it[stripeCustomerId] = tenant.stripeCustomerId
            it[stripeSubscriptionId] = tenant.stripeSubscriptionId
            it[billingEmail] = tenant.billingEmail
            it[createdAt] = tenant.createdAt
            it[updatedAt] = tenant.updatedAt
            it[deletedAt] = tenant.deletedAt
        }[Tenants.id].value
        
        tenant.copy(id = id)
    }
    
    override suspend fun findById(id: Long): Tenant? = dbQuery {
        Tenants.selectAll().where { Tenants.id eq id }
            .map { it.toTenant() }
            .singleOrNull()
    }
    
    override suspend fun findByKey(key: String): Tenant? = dbQuery {
        Tenants.selectAll().where { Tenants.key eq key }
            .map { it.toTenant() }
            .singleOrNull()
    }
    
    override suspend fun findBySchemaName(schemaName: String): Tenant? = dbQuery {
        Tenants.selectAll().where { Tenants.tenantSchemaName eq schemaName }
            .map { it.toTenant() }
            .singleOrNull()
    }
    
    override suspend fun findByStripeCustomerId(stripeCustomerId: String): Tenant? = dbQuery {
        Tenants.selectAll().where { Tenants.stripeCustomerId eq stripeCustomerId }
            .map { it.toTenant() }
            .singleOrNull()
    }
    
    override suspend fun update(tenant: Tenant): Tenant = dbQuery {
        Tenants.update({ Tenants.id eq tenant.id }) {
            it[key] = tenant.key
            it[name] = tenant.name
            it[plan] = tenant.plan.name
            it[status] = tenant.status.name
            it[tenantSchemaName] = tenant.schemaName
            it[stripeCustomerId] = tenant.stripeCustomerId
            it[stripeSubscriptionId] = tenant.stripeSubscriptionId
            it[billingEmail] = tenant.billingEmail
            it[updatedAt] = LocalDateTime.now()
            it[deletedAt] = tenant.deletedAt
        }
        tenant.copy(updatedAt = LocalDateTime.now())
    }
    
    override suspend fun delete(id: Long): Unit = dbQuery {
        Tenants.update({ Tenants.id eq id }) {
            it[status] = TenantStatus.CANCELLED.name
            it[deletedAt] = LocalDateTime.now()
            it[updatedAt] = LocalDateTime.now()
        }
        Unit
    }
    
    override suspend fun listAll(includeDeleted: Boolean): List<Tenant> = dbQuery {
        val query = if (includeDeleted) {
            Tenants.selectAll()
        } else {
            Tenants.selectAll().where { Tenants.deletedAt.isNull() }
        }
        
        query.map { it.toTenant() }
    }
    
    // ============================================================================
    // API KEY OPERATIONS
    // ============================================================================
    
    override suspend fun createApiKey(apiKey: TenantApiKey): TenantApiKey = dbQuery {
        val id = TenantApiKeys.insert {
            it[tenantId] = apiKey.tenantId
            it[keyHash] = apiKey.keyHash
            it[name] = apiKey.name
            it[scopes] = Json.encodeToString(apiKey.scopes.map { scope -> scope.name })
            it[expiresAt] = apiKey.expiresAt
            it[createdAt] = apiKey.createdAt
            it[lastUsedAt] = apiKey.lastUsedAt
        }[TenantApiKeys.id].value
        
        apiKey.copy(id = id)
    }
    
    override suspend fun findApiKeyByHash(keyHash: String): TenantApiKey? = dbQuery {
        TenantApiKeys.selectAll().where { TenantApiKeys.keyHash eq keyHash }
            .map { it.toTenantApiKey() }
            .singleOrNull()
    }
    
    override suspend fun listApiKeys(tenantId: Long): List<TenantApiKey> = dbQuery {
        TenantApiKeys.selectAll().where { TenantApiKeys.tenantId eq tenantId }
            .map { it.toTenantApiKey() }
    }
    
    override suspend fun updateApiKeyLastUsed(apiKeyId: Long): Unit = dbQuery {
        TenantApiKeys.update({ TenantApiKeys.id eq apiKeyId }) {
            it[lastUsedAt] = LocalDateTime.now()
        }
        Unit
    }
    
    override suspend fun deleteApiKey(apiKeyId: Long): Unit = dbQuery {
        TenantApiKeys.deleteWhere { TenantApiKeys.id eq apiKeyId }
        Unit
    }
    
    // ============================================================================
    // USER OPERATIONS
    // ============================================================================
    
    override suspend fun addUser(user: TenantUser): TenantUser = dbQuery {
        val id = TenantUsers.insert {
            it[tenantId] = user.tenantId
            it[email] = user.email
            it[role] = user.role.name
            it[createdAt] = user.createdAt
        }[TenantUsers.id].value
        
        user.copy(id = id)
    }
    
    override suspend fun findUser(tenantId: Long, email: String): TenantUser? = dbQuery {
        TenantUsers.selectAll().where { 
            (TenantUsers.tenantId eq tenantId) and (TenantUsers.email eq email) 
        }
            .map { it.toTenantUser() }
            .singleOrNull()
    }
    
    override suspend fun listUsers(tenantId: Long): List<TenantUser> = dbQuery {
        TenantUsers.selectAll().where { TenantUsers.tenantId eq tenantId }
            .map { it.toTenantUser() }
    }
    
    override suspend fun updateUserRole(userId: Long, role: TenantRole): Unit = dbQuery {
        TenantUsers.update({ TenantUsers.id eq userId }) {
            it[TenantUsers.role] = role.name
        }
        Unit
    }
    
    override suspend fun removeUser(userId: Long): Unit = dbQuery {
        TenantUsers.deleteWhere { TenantUsers.id eq userId }
        Unit
    }
    
    // ============================================================================
    // USAGE TRACKING
    // ============================================================================
    
    override suspend fun incrementEvaluations(tenantId: Long, count: Long): Unit = dbQuery {
        val today = LocalDate.now()
        
        // Upsert usage record
        val existing = TenantUsage.selectAll().where {
            (TenantUsage.tenantId eq tenantId) and (TenantUsage.periodStart eq today)
        }.singleOrNull()
        
        if (existing != null) {
            TenantUsage.update({
                (TenantUsage.tenantId eq tenantId) and (TenantUsage.periodStart eq today)
            }) {
                it[evaluationsCount] = evaluationsCount + count
            }
        } else {
            TenantUsage.insert {
                it[TenantUsage.tenantId] = tenantId
                it[periodStart] = today
                it[periodEnd] = today
                it[evaluationsCount] = count
                it[flagsCount] = 0
                it[apiCallsCount] = 0
                it[createdAt] = LocalDateTime.now()
            }
        }
    }
    
    override suspend fun incrementApiCalls(tenantId: Long, count: Long): Unit = dbQuery {
        val today = LocalDate.now()
        
        val existing = TenantUsage.selectAll().where {
            (TenantUsage.tenantId eq tenantId) and (TenantUsage.periodStart eq today)
        }.singleOrNull()
        
        if (existing != null) {
            TenantUsage.update({
                (TenantUsage.tenantId eq tenantId) and (TenantUsage.periodStart eq today)
            }) {
                it[apiCallsCount] = apiCallsCount + count
            }
        } else {
            TenantUsage.insert {
                it[TenantUsage.tenantId] = tenantId
                it[periodStart] = today
                it[periodEnd] = today
                it[evaluationsCount] = 0
                it[flagsCount] = 0
                it[apiCallsCount] = count
                it[createdAt] = LocalDateTime.now()
            }
        }
    }
    
    override suspend fun updateFlagsCount(tenantId: Long, count: Int): Unit = dbQuery {
        val today = LocalDate.now()
        
        val existing = TenantUsage.selectAll().where {
            (TenantUsage.tenantId eq tenantId) and (TenantUsage.periodStart eq today)
        }.singleOrNull()
        
        if (existing != null) {
            TenantUsage.update({
                (TenantUsage.tenantId eq tenantId) and (TenantUsage.periodStart eq today)
            }) {
                it[flagsCount] = count
            }
        } else {
            TenantUsage.insert {
                it[TenantUsage.tenantId] = tenantId
                it[periodStart] = today
                it[periodEnd] = today
                it[evaluationsCount] = 0
                it[TenantUsage.flagsCount] = count
                it[apiCallsCount] = 0
                it[createdAt] = LocalDateTime.now()
            }
        }
    }
    
    override suspend fun getUsage(
        tenantId: Long,
        periodStart: LocalDate,
        periodEnd: LocalDate
    ): List<flagent.domain.entity.TenantUsage> = dbQuery {
        TenantUsage.selectAll().where {
            (TenantUsage.tenantId eq tenantId) and
                    (TenantUsage.periodStart greaterEq periodStart) and
                    (TenantUsage.periodEnd lessEq periodEnd)
        }
            .map { it.toTenantUsage() }
    }
    
    override suspend fun getTotalUsage(tenantId: Long): flagent.domain.entity.TenantUsage = dbQuery {
        val results = TenantUsage.selectAll().where { TenantUsage.tenantId eq tenantId }
        
        val totalEvaluations = results.sumOf { it[TenantUsage.evaluationsCount] }
        val totalApiCalls = results.sumOf { it[TenantUsage.apiCallsCount] }
        val currentFlags = results.maxOfOrNull { it[TenantUsage.flagsCount] } ?: 0
        
        flagent.domain.entity.TenantUsage(
            id = 0,
            tenantId = tenantId,
            periodStart = LocalDate.now(),
            periodEnd = LocalDate.now(),
            evaluationsCount = totalEvaluations,
            flagsCount = currentFlags,
            apiCallsCount = totalApiCalls,
            createdAt = LocalDateTime.now()
        )
    }
    
    // ============================================================================
    // MAPPERS
    // ============================================================================
    
    private fun ResultRow.toTenant() = Tenant(
        id = this[Tenants.id].value,
        key = this[Tenants.key],
        name = this[Tenants.name],
        plan = TenantPlan.valueOf(this[Tenants.plan]),
        status = TenantStatus.valueOf(this[Tenants.status]),
        schemaName = this[Tenants.tenantSchemaName],
        stripeCustomerId = this[Tenants.stripeCustomerId],
        stripeSubscriptionId = this[Tenants.stripeSubscriptionId],
        billingEmail = this[Tenants.billingEmail],
        createdAt = this[Tenants.createdAt],
        updatedAt = this[Tenants.updatedAt],
        deletedAt = this[Tenants.deletedAt]
    )
    
    private fun ResultRow.toTenantApiKey() = TenantApiKey(
        id = this[TenantApiKeys.id].value,
        tenantId = this[TenantApiKeys.tenantId],
        keyHash = this[TenantApiKeys.keyHash],
        name = this[TenantApiKeys.name],
        scopes = Json.decodeFromString<List<String>>(this[TenantApiKeys.scopes])
            .map { ApiKeyScope.valueOf(it) },
        expiresAt = this[TenantApiKeys.expiresAt],
        createdAt = this[TenantApiKeys.createdAt],
        lastUsedAt = this[TenantApiKeys.lastUsedAt]
    )
    
    private fun ResultRow.toTenantUser() = TenantUser(
        id = this[TenantUsers.id].value,
        tenantId = this[TenantUsers.tenantId],
        email = this[TenantUsers.email],
        role = TenantRole.valueOf(this[TenantUsers.role]),
        createdAt = this[TenantUsers.createdAt]
    )
    
    private fun ResultRow.toTenantUsage() = flagent.domain.entity.TenantUsage(
        id = this[TenantUsage.id].value,
        tenantId = this[TenantUsage.tenantId],
        periodStart = this[TenantUsage.periodStart],
        periodEnd = this[TenantUsage.periodEnd],
        evaluationsCount = this[TenantUsage.evaluationsCount],
        flagsCount = this[TenantUsage.flagsCount],
        apiCallsCount = this[TenantUsage.apiCallsCount],
        createdAt = this[TenantUsage.createdAt]
    )
}
