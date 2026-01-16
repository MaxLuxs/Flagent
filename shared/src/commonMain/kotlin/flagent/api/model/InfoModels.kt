package flagent.api.model

import kotlinx.serialization.Serializable

/**
 * Info API models
 */

@Serializable
data class InfoResponse(
    val version: String,
    val buildTime: String? = null,
    val gitCommit: String? = null
)
