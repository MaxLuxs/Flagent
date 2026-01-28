package flagent.domain.entity

import java.time.LocalDateTime

/**
 * SsoSession entity - represents active SSO session.
 *
 * Session lifecycle:
 * 1. User initiates SSO login
 * 2. Redirect to IdP (SAML/OAuth)
 * 3. IdP authenticates user
 * 4. Redirect back with assertion/token
 * 5. Validate and create session
 * 6. Issue JWT token
 *
 * Security:
 * - JWT tokens with expiration
 * - Refresh tokens for long sessions
 * - Session revocation support
 */
data class SsoSession(
    val id: Long = 0,
    val tenantId: Long,
    val userId: Long,
    val providerId: Long,
    val sessionToken: String, // JWT token (opaque)
    val refreshToken: String? = null, // Refresh token (for re-authentication)
    val expiresAt: LocalDateTime,
    val refreshExpiresAt: LocalDateTime? = null,
    val ipAddress: String? = null,
    val userAgent: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val lastActivityAt: LocalDateTime = LocalDateTime.now()
)

/**
 * SsoLoginAttempt entity - audit log for SSO login attempts.
 *
 * Used for:
 * - Security monitoring
 * - Troubleshooting failed logins
 * - Compliance (audit logs)
 */
data class SsoLoginAttempt(
    val id: Long = 0,
    val tenantId: Long,
    val providerId: Long,
    val userEmail: String? = null,
    val success: Boolean,
    val failureReason: String? = null,
    val ipAddress: String? = null,
    val userAgent: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

/**
 * Helper to check if session is expired.
 */
fun SsoSession.isExpired(): Boolean {
    return expiresAt.isBefore(LocalDateTime.now())
}

/**
 * Helper to check if refresh token is expired.
 */
fun SsoSession.isRefreshExpired(): Boolean {
    return refreshExpiresAt?.isBefore(LocalDateTime.now()) ?: true
}

/**
 * Helper to check if session needs refresh.
 *
 * @param thresholdMinutes Refresh if expiring within N minutes (default: 5)
 */
fun SsoSession.needsRefresh(thresholdMinutes: Long = 5): Boolean {
    return expiresAt.isBefore(LocalDateTime.now().plusMinutes(thresholdMinutes))
}
