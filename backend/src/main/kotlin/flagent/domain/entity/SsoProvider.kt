package flagent.domain.entity

import kotlinx.serialization.Serializable
import java.time.LocalDateTime

/**
 * SsoProvider entity - SSO/SAML configuration for tenant.
 *
 * Supported providers:
 * - SAML 2.0 (Okta, Auth0, Azure AD, OneLogin)
 * - OAuth 2.0 (Google, GitHub, Microsoft)
 * - OIDC (OpenID Connect)
 *
 * Security:
 * - Metadata stored securely (encrypted at rest)
 * - Certificate validation for SAML
 * - Token validation for OAuth
 */
data class SsoProvider(
    val id: Long = 0,
    val tenantId: Long,
    val name: String,
    val type: SsoProviderType,
    val enabled: Boolean = true,
    val metadata: SsoProviderMetadata,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

/**
 * SSO provider types.
 */
enum class SsoProviderType {
    /**
     * SAML 2.0 provider.
     * Used by enterprise IdPs (Okta, Auth0, Azure AD, OneLogin).
     */
    SAML,

    /**
     * OAuth 2.0 provider.
     * Used by social login (Google, GitHub, Microsoft).
     */
    OAUTH,

    /**
     * OpenID Connect provider.
     * Extension of OAuth 2.0 with standardized identity.
     */
    OIDC
}

/**
 * SSO provider metadata.
 *
 * Different providers require different configuration.
 */
@Serializable
sealed class SsoProviderMetadata {
    
    /**
     * SAML 2.0 metadata.
     *
     * @property entityId SAML entity ID (unique identifier)
     * @property ssoUrl Single Sign-On URL (IdP login endpoint)
     * @property certificate X.509 certificate (for signature verification)
     * @property signRequests Sign SAML requests (recommended)
     * @property wantAssertionsSigned Require signed assertions
     * @property attributeMappings Map SAML attributes to user fields
     */
    @Serializable
    data class Saml(
        val entityId: String,
        val ssoUrl: String,
        val sloUrl: String? = null, // Single Logout URL (optional)
        val certificate: String, // X.509 certificate (PEM format)
        val signRequests: Boolean = true,
        val wantAssertionsSigned: Boolean = true,
        val attributeMappings: Map<String, String> = mapOf(
            "email" to "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress",
            "firstName" to "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/givenname",
            "lastName" to "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/surname"
        )
    ) : SsoProviderMetadata()

    /**
     * OAuth 2.0 metadata.
     *
     * @property clientId OAuth client ID
     * @property clientSecret OAuth client secret (encrypted)
     * @property authorizationUrl Authorization endpoint
     * @property tokenUrl Token endpoint
     * @property userInfoUrl User info endpoint
     * @property scopes OAuth scopes (e.g., "openid email profile")
     */
    @Serializable
    data class OAuth(
        val clientId: String,
        val clientSecret: String, // Encrypted at rest
        val authorizationUrl: String,
        val tokenUrl: String,
        val userInfoUrl: String? = null,
        val scopes: List<String> = listOf("openid", "email", "profile")
    ) : SsoProviderMetadata()

    /**
     * OIDC metadata.
     *
     * @property issuer OIDC issuer URL
     * @property clientId OIDC client ID
     * @property clientSecret OIDC client secret (encrypted)
     * @property discoveryUrl OIDC discovery URL (.well-known/openid-configuration)
     */
    @Serializable
    data class Oidc(
        val issuer: String,
        val clientId: String,
        val clientSecret: String, // Encrypted at rest
        val discoveryUrl: String? = null, // Auto-discover endpoints
        val scopes: List<String> = listOf("openid", "email", "profile")
    ) : SsoProviderMetadata()
}

/**
 * SsoConnection entity - tracks SSO login session.
 *
 * Used for:
 * - Session management
 * - Audit logging
 * - Usage analytics
 */
data class SsoConnection(
    val id: Long = 0,
    val tenantId: Long,
    val providerId: Long,
    val userId: Long,
    val userEmail: String,
    val sessionToken: String, // JWT token
    val expiresAt: LocalDateTime,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

/**
 * Helper to check if SSO provider is enterprise.
 */
fun SsoProvider.isEnterprise(): Boolean {
    return type == SsoProviderType.SAML || type == SsoProviderType.OIDC
}

/**
 * Helper to get provider display name.
 */
fun SsoProvider.displayName(): String {
    return when (type) {
        SsoProviderType.SAML -> "SAML 2.0 ($name)"
        SsoProviderType.OAUTH -> "OAuth 2.0 ($name)"
        SsoProviderType.OIDC -> "OpenID Connect ($name)"
    }
}
