package flagent.service.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * Variant DTO for service layer
 */
@Serializable
data class VariantDTO(
    val id: Int = 0,
    val flagId: Int,
    val key: String,
    val attachment: JsonObject? = null
)
