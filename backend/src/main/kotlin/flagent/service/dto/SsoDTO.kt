package flagent.service.dto.sso

import flagent.domain.entity.*
import kotlinx.serialization.Serializable

/**
 * CreateSsoProviderRequest - request to create SSO provider.
 */
@Serializable
data class CreateSsoProviderRequest(
    val name: String,
    val type: String, // SAML, OAUTH, OIDC
    val enabled: Boolean? = true,
    val metadata: String // JSON metadata (type-specific)
)

/**
 * UpdateSsoProviderRequest - request to update SSO provider.
 */
@Serializable
data class UpdateSsoProviderRequest(
    val name: String? = null,
    val enabled: Boolean? = null,
    val metadata: String? = null
)

/**
 * SsoProviderDTO - SSO provider information.
 */
@Serializable
data class SsoProviderDTO(
    val id: Long,
    val tenantId: Long,
    val name: String,
    val type: String,
    val enabled: Boolean,
    val metadata: String, // JSON (sensitive fields redacted)
    val createdAt: String,
    val updatedAt: String
)

/**
 * SsoSessionDTO - SSO session information.
 */
@Serializable
data class SsoSessionDTO(
    val id: Long,
    val tenantId: Long,
    val userId: Long,
    val providerId: Long,
    val sessionToken: String,
    val expiresAt: String,
    val ipAddress: String? = null,
    val userAgent: String? = null,
    val createdAt: String,
    val lastActivityAt: String
)

/**
 * ValidateTokenRequest - request to validate session token.
 */
@Serializable
data class ValidateTokenRequest(
    val sessionToken: String
)

/**
 * SsoLoginAttemptDTO - login attempt information.
 */
@Serializable
data class SsoLoginAttemptDTO(
    val id: Long,
    val tenantId: Long,
    val providerId: Long,
    val userEmail: String? = null,
    val success: Boolean,
    val failureReason: String? = null,
    val ipAddress: String? = null,
    val userAgent: String? = null,
    val createdAt: String
)

// ============================================================================
// MAPPERS
// ============================================================================

fun SsoProvider.toDTO() = SsoProviderDTO(
    id = id,
    tenantId = tenantId,
    name = name,
    type = type.name,
    enabled = enabled,
    metadata = serializeMetadataForDTO(metadata), // Redact sensitive fields
    createdAt = createdAt.toString(),
    updatedAt = updatedAt.toString()
)

fun SsoSession.toDTO() = SsoSessionDTO(
    id = id,
    tenantId = tenantId,
    userId = userId,
    providerId = providerId,
    sessionToken = sessionToken.take(20) + "...", // Don't expose full token
    expiresAt = expiresAt.toString(),
    ipAddress = ipAddress,
    userAgent = userAgent,
    createdAt = createdAt.toString(),
    lastActivityAt = lastActivityAt.toString()
)

fun SsoLoginAttempt.toDTO() = SsoLoginAttemptDTO(
    id = id,
    tenantId = tenantId,
    providerId = providerId,
    userEmail = userEmail,
    success = success,
    failureReason = failureReason,
    ipAddress = ipAddress,
    userAgent = userAgent,
    createdAt = createdAt.toString()
)

/**
 * Serialize metadata for DTO (redact sensitive fields).
 */
private fun serializeMetadataForDTO(metadata: SsoProviderMetadata): String {
    return when (metadata) {
        is SsoProviderMetadata.Saml -> {
            // Redact certificate (too long for DTO)
            """
            {
              "entityId": "${metadata.entityId}",
              "ssoUrl": "${metadata.ssoUrl}",
              "signRequests": ${metadata.signRequests},
              "wantAssertionsSigned": ${metadata.wantAssertionsSigned},
              "certificate": "***REDACTED***"
            }
            """.trimIndent()
        }
        is SsoProviderMetadata.OAuth -> {
            // Redact client secret
            """
            {
              "clientId": "${metadata.clientId}",
              "clientSecret": "***REDACTED***",
              "authorizationUrl": "${metadata.authorizationUrl}",
              "tokenUrl": "${metadata.tokenUrl}",
              "scopes": ${metadata.scopes}
            }
            """.trimIndent()
        }
        is SsoProviderMetadata.Oidc -> {
            // Redact client secret
            """
            {
              "issuer": "${metadata.issuer}",
              "clientId": "${metadata.clientId}",
              "clientSecret": "***REDACTED***",
              "discoveryUrl": "${metadata.discoveryUrl}"
            }
            """.trimIndent()
        }
    }
}
