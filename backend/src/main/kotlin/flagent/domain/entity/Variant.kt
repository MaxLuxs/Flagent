package flagent.domain.entity

/**
 * Variant entity - represents the experience/variant of the evaluation entity
 *
 * Domain entity - no framework dependencies.
 * attachment: key-value config (e.g. for remote config); serialization in repository/route layer.
 */
data class Variant(
    val id: Int = 0,
    val flagId: Int,
    val key: String,
    val attachment: Map<String, String>? = null
)
