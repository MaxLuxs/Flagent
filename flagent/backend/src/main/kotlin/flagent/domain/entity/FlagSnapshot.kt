package flagent.domain.entity

import kotlinx.serialization.Serializable

/**
 * FlagSnapshot entity - snapshot of a flag
 * Any change of the flag will create a new snapshot
 * 
 * Domain entity - no framework dependencies
 */
@Serializable
data class FlagSnapshot(
    val id: Int = 0,
    val flagId: Int,
    val updatedBy: String? = null,
    val flag: String, // JSON stored as string
    val updatedAt: String? = null // ISO 8601 timestamp
)
