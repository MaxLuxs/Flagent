package flagent.frontend.api

import kotlinx.serialization.Serializable

@Serializable
data class PublicSignupRequest(
    val key: String,
    val name: String,
    val plan: String? = null,
    val ownerEmail: String
)

@Serializable
data class PublicSignupResponse(
    val tenantKey: String,
    val message: String
)

@Serializable
data class MagicLinkVerifyResponse(
    val sessionToken: String,
    val tenant: TenantResponse,
    val user: TenantUserResponse
)

