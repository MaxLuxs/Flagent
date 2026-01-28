package flagent.frontend.api

import kotlinx.serialization.Serializable

@Serializable
data class SsoProviderResponse(
    val id: Long,
    val tenantId: Long,
    val name: String,
    val type: String,
    val enabled: Boolean,
    val metadata: String,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class CreateSsoProviderRequest(
    val name: String,
    val type: String,
    val enabled: Boolean? = true,
    val metadata: String
)
