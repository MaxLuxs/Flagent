package flagent.domain.entity

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * Variant entity - represents the experience/variant of the evaluation entity
 * 
 * Domain entity - no framework dependencies
 */
@Serializable
data class Variant(
    val id: Int = 0,
    val flagId: Int,
    val key: String,
    val attachment: JsonObject? = null // Dynamic configuration stored as JSON
)
