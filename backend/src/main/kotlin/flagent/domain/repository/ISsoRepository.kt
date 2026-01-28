package flagent.domain.repository

import flagent.domain.entity.*

/**
 * ISsoRepository - repository interface for SSO management.
 *
 * Operations:
 * - SSO provider CRUD
 * - Session management
 * - Login attempt logging
 */
interface ISsoRepository {
    
    // ============================================================================
    // SSO PROVIDER OPERATIONS
    // ============================================================================
    
    /**
     * Create SSO provider for tenant.
     *
     * @param provider SSO provider configuration
     * @return Created provider with ID
     */
    suspend fun createProvider(provider: SsoProvider): SsoProvider
    
    /**
     * Find provider by ID.
     *
     * @param providerId Provider ID
     * @return Provider or null if not found
     */
    suspend fun findProviderById(providerId: Long): SsoProvider?
    
    /**
     * List providers for tenant.
     *
     * @param tenantId Tenant ID
     * @param includeDisabled Include disabled providers
     * @return List of providers
     */
    suspend fun listProviders(tenantId: Long, includeDisabled: Boolean = false): List<SsoProvider>
    
    /**
     * Update provider configuration.
     *
     * @param provider Updated provider
     * @return Updated provider
     */
    suspend fun updateProvider(provider: SsoProvider): SsoProvider
    
    /**
     * Delete provider.
     *
     * @param providerId Provider ID
     */
    suspend fun deleteProvider(providerId: Long)
    
    // ============================================================================
    // SESSION OPERATIONS
    // ============================================================================
    
    /**
     * Create SSO session.
     *
     * @param session Session data
     * @return Created session with ID
     */
    suspend fun createSession(session: SsoSession): SsoSession
    
    /**
     * Find session by token.
     *
     * @param sessionToken Session token (JWT)
     * @return Session or null if not found
     */
    suspend fun findSessionByToken(sessionToken: String): SsoSession?
    
    /**
     * Update session (last activity, refresh token).
     *
     * @param session Updated session
     * @return Updated session
     */
    suspend fun updateSession(session: SsoSession): SsoSession
    
    /**
     * Delete session (logout).
     *
     * @param sessionId Session ID
     */
    suspend fun deleteSession(sessionId: Long)
    
    /**
     * Delete all sessions for user (logout all devices).
     *
     * @param userId User ID
     */
    suspend fun deleteUserSessions(userId: Long)
    
    /**
     * Cleanup expired sessions.
     *
     * @return Number of deleted sessions
     */
    suspend fun cleanupExpiredSessions(): Int
    
    // ============================================================================
    // LOGIN ATTEMPT OPERATIONS
    // ============================================================================
    
    /**
     * Log login attempt.
     *
     * @param attempt Login attempt data
     * @return Created attempt with ID
     */
    suspend fun logLoginAttempt(attempt: SsoLoginAttempt): SsoLoginAttempt
    
    /**
     * Get login attempts for tenant.
     *
     * @param tenantId Tenant ID
     * @param limit Maximum number of attempts to return
     * @return List of login attempts
     */
    suspend fun getLoginAttempts(tenantId: Long, limit: Int = 100): List<SsoLoginAttempt>
    
    /**
     * Get failed login attempts for user (security monitoring).
     *
     * @param tenantId Tenant ID
     * @param userEmail User email
     * @param minutesAgo Time window (default: 30 minutes)
     * @return List of failed attempts
     */
    suspend fun getFailedAttempts(
        tenantId: Long,
        userEmail: String,
        minutesAgo: Int = 30
    ): List<SsoLoginAttempt>
}
