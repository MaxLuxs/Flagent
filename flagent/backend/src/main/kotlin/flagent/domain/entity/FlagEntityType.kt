package flagent.domain.entity

import kotlinx.serialization.Serializable

/**
 * FlagEntityType entity - entity_type that will overwrite into evaluation logs
 * 
 * Domain entity - no framework dependencies
 */
@Serializable
data class FlagEntityType(
    val id: Int = 0,
    val key: String
)
