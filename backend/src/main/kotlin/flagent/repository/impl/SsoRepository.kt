package flagent.repository.impl
import org.jetbrains.exposed.v1.jdbc.*

import flagent.domain.entity.*
import flagent.domain.repository.ISsoRepository
import flagent.repository.tables.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import java.time.LocalDateTime

/**
 * SsoRepository - implementation of SSO repository.
 *
 * All operations run in public schema (SSO metadata shared across tenants).
 */
class SsoRepository : ISsoRepository {
    
    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        withContext(Dispatchers.IO) {
            suspendTransaction { block() }
        }
    
    // ============================================================================
    // SSO PROVIDER OPERATIONS
    // ============================================================================
    
    override suspend fun createProvider(provider: SsoProvider): SsoProvider = dbQuery {
        val id = SsoProviders.insert {
            it[tenantId] = provider.tenantId
            it[name] = provider.name
            it[type] = provider.type.name
            it[enabled] = provider.enabled
            it[metadata] = serializeMetadata(provider.metadata)
            it[createdAt] = provider.createdAt
            it[updatedAt] = provider.updatedAt
        }[SsoProviders.id].value
        
        provider.copy(id = id)
    }
    
    override suspend fun findProviderById(providerId: Long): SsoProvider? = dbQuery {
        SsoProviders.selectAll().where { SsoProviders.id eq providerId }
            .map { it.toSsoProvider() }
            .singleOrNull()
    }
    
    override suspend fun listProviders(tenantId: Long, includeDisabled: Boolean): List<SsoProvider> = dbQuery {
        val query = if (includeDisabled) {
            SsoProviders.selectAll().where { SsoProviders.tenantId eq tenantId }
        } else {
            SsoProviders.selectAll().where { 
                (SsoProviders.tenantId eq tenantId) and (SsoProviders.enabled eq true) 
            }
        }
        
        query.map { it.toSsoProvider() }
    }
    
    override suspend fun updateProvider(provider: SsoProvider): SsoProvider = dbQuery {
        SsoProviders.update({ SsoProviders.id eq provider.id }) {
            it[name] = provider.name
            it[type] = provider.type.name
            it[enabled] = provider.enabled
            it[metadata] = serializeMetadata(provider.metadata)
            it[updatedAt] = LocalDateTime.now()
        }
        provider.copy(updatedAt = LocalDateTime.now())
    }
    
    override suspend fun deleteProvider(providerId: Long): Unit = dbQuery {
        SsoProviders.deleteWhere { SsoProviders.id eq providerId }
        Unit
    }
    
    // ============================================================================
    // SESSION OPERATIONS
    // ============================================================================
    
    override suspend fun createSession(session: SsoSession): SsoSession = dbQuery {
        val id = SsoSessions.insert {
            it[tenantId] = session.tenantId
            it[userId] = session.userId
            it[providerId] = session.providerId
            it[sessionToken] = session.sessionToken
            it[refreshToken] = session.refreshToken
            it[expiresAt] = session.expiresAt
            it[refreshExpiresAt] = session.refreshExpiresAt
            it[ipAddress] = session.ipAddress
            it[userAgent] = session.userAgent
            it[createdAt] = session.createdAt
            it[lastActivityAt] = session.lastActivityAt
        }[SsoSessions.id].value
        
        session.copy(id = id)
    }
    
    override suspend fun findSessionByToken(sessionToken: String): SsoSession? = dbQuery {
        SsoSessions.selectAll().where { SsoSessions.sessionToken eq sessionToken }
            .map { it.toSsoSession() }
            .singleOrNull()
    }
    
    override suspend fun updateSession(session: SsoSession): SsoSession = dbQuery {
        SsoSessions.update({ SsoSessions.id eq session.id }) {
            it[refreshToken] = session.refreshToken
            it[expiresAt] = session.expiresAt
            it[refreshExpiresAt] = session.refreshExpiresAt
            it[lastActivityAt] = LocalDateTime.now()
        }
        session.copy(lastActivityAt = LocalDateTime.now())
    }
    
    override suspend fun deleteSession(sessionId: Long): Unit = dbQuery {
        SsoSessions.deleteWhere { SsoSessions.id eq sessionId }
        Unit
    }
    
    override suspend fun deleteUserSessions(userId: Long): Unit = dbQuery {
        SsoSessions.deleteWhere { SsoSessions.userId eq userId }
        Unit
    }
    
    override suspend fun cleanupExpiredSessions(): Int = dbQuery {
        SsoSessions.deleteWhere { SsoSessions.expiresAt less LocalDateTime.now() }
    }
    
    // ============================================================================
    // LOGIN ATTEMPT OPERATIONS
    // ============================================================================
    
    override suspend fun logLoginAttempt(attempt: SsoLoginAttempt): SsoLoginAttempt = dbQuery {
        val id = SsoLoginAttempts.insert {
            it[tenantId] = attempt.tenantId
            it[providerId] = attempt.providerId
            it[userEmail] = attempt.userEmail
            it[success] = attempt.success
            it[failureReason] = attempt.failureReason
            it[ipAddress] = attempt.ipAddress
            it[userAgent] = attempt.userAgent
            it[createdAt] = attempt.createdAt
        }[SsoLoginAttempts.id].value
        
        attempt.copy(id = id)
    }
    
    override suspend fun getLoginAttempts(tenantId: Long, limit: Int): List<SsoLoginAttempt> = dbQuery {
        SsoLoginAttempts.selectAll().where { SsoLoginAttempts.tenantId eq tenantId }
            .orderBy(SsoLoginAttempts.createdAt, SortOrder.DESC)
            .limit(limit)
            .map { it.toSsoLoginAttempt() }
    }
    
    override suspend fun getFailedAttempts(
        tenantId: Long,
        userEmail: String,
        minutesAgo: Int
    ): List<SsoLoginAttempt> = dbQuery {
        val since = LocalDateTime.now().minusMinutes(minutesAgo.toLong())
        
        SsoLoginAttempts.selectAll().where {
            (SsoLoginAttempts.tenantId eq tenantId) and
                    (SsoLoginAttempts.userEmail eq userEmail) and
                    (SsoLoginAttempts.success eq false) and
                    (SsoLoginAttempts.createdAt greaterEq since)
        }
            .orderBy(SsoLoginAttempts.createdAt, SortOrder.DESC)
            .map { it.toSsoLoginAttempt() }
    }
    
    // ============================================================================
    // MAPPERS
    // ============================================================================
    
    private fun ResultRow.toSsoProvider() = SsoProvider(
        id = this[SsoProviders.id].value,
        tenantId = this[SsoProviders.tenantId],
        name = this[SsoProviders.name],
        type = SsoProviderType.valueOf(this[SsoProviders.type]),
        enabled = this[SsoProviders.enabled],
        metadata = deserializeMetadata(this[SsoProviders.metadata], this[SsoProviders.type]),
        createdAt = this[SsoProviders.createdAt],
        updatedAt = this[SsoProviders.updatedAt]
    )
    
    private fun ResultRow.toSsoSession() = SsoSession(
        id = this[SsoSessions.id].value,
        tenantId = this[SsoSessions.tenantId],
        userId = this[SsoSessions.userId],
        providerId = this[SsoSessions.providerId],
        sessionToken = this[SsoSessions.sessionToken],
        refreshToken = this[SsoSessions.refreshToken],
        expiresAt = this[SsoSessions.expiresAt],
        refreshExpiresAt = this[SsoSessions.refreshExpiresAt],
        ipAddress = this[SsoSessions.ipAddress],
        userAgent = this[SsoSessions.userAgent],
        createdAt = this[SsoSessions.createdAt],
        lastActivityAt = this[SsoSessions.lastActivityAt]
    )
    
    private fun ResultRow.toSsoLoginAttempt() = SsoLoginAttempt(
        id = this[SsoLoginAttempts.id].value,
        tenantId = this[SsoLoginAttempts.tenantId],
        providerId = this[SsoLoginAttempts.providerId],
        userEmail = this[SsoLoginAttempts.userEmail],
        success = this[SsoLoginAttempts.success],
        failureReason = this[SsoLoginAttempts.failureReason],
        ipAddress = this[SsoLoginAttempts.ipAddress],
        userAgent = this[SsoLoginAttempts.userAgent],
        createdAt = this[SsoLoginAttempts.createdAt]
    )
    
    // ============================================================================
    // SERIALIZATION
    // ============================================================================
    
    private fun serializeMetadata(metadata: SsoProviderMetadata): String {
        return when (metadata) {
            is SsoProviderMetadata.Saml -> Json.encodeToString(metadata)
            is SsoProviderMetadata.OAuth -> Json.encodeToString(metadata)
            is SsoProviderMetadata.Oidc -> Json.encodeToString(metadata)
        }
    }
    
    private fun deserializeMetadata(json: String, type: String): SsoProviderMetadata {
        return when (SsoProviderType.valueOf(type)) {
            SsoProviderType.SAML -> Json.decodeFromString<SsoProviderMetadata.Saml>(json)
            SsoProviderType.OAUTH -> Json.decodeFromString<SsoProviderMetadata.OAuth>(json)
            SsoProviderType.OIDC -> Json.decodeFromString<SsoProviderMetadata.Oidc>(json)
        }
    }
}
