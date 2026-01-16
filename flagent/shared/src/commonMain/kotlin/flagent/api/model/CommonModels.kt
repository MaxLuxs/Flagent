package flagent.api.model

import kotlinx.serialization.Serializable

/**
 * Common UI models (simplified models for frontend)
 */

@Serializable
data class Flag(
    val id: Int,
    val key: String,
    val description: String? = null,
    val enabled: Boolean = false,
    val tags: List<Tag> = emptyList()
)

@Serializable
data class Tag(
    val id: Int,
    val value: String
)

/**
 * Update flag request (frontend-specific, differs from PutFlagRequest)
 */
@Serializable
data class UpdateFlagRequest(
    val key: String? = null,
    val description: String? = null,
    val enabled: Boolean? = null
)
