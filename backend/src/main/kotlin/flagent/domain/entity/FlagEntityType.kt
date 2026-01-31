package flagent.domain.entity

/**
 * FlagEntityType entity - entity_type that will overwrite into evaluation logs
 *
 * Domain entity - no framework dependencies
 */
data class FlagEntityType(
    val id: Int = 0,
    val key: String
)
