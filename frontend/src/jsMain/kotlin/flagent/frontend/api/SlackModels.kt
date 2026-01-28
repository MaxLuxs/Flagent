package flagent.frontend.api

import kotlinx.serialization.Serializable

@Serializable
data class SlackStatusResponse(
    val enabled: Boolean,
    val configured: Boolean
)

@Serializable
data class SlackTestResponse(
    val success: Boolean,
    val message: String? = null,
    val error: String? = null
)
