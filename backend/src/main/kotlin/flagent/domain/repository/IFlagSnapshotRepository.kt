package flagent.domain.repository

import flagent.domain.entity.FlagSnapshot

/**
 * FlagSnapshot repository interface
 * Domain layer - no framework dependencies
 */
interface IFlagSnapshotRepository {
    suspend fun findByFlagId(flagId: Int, limit: Int? = null, offset: Int = 0, sortDesc: Boolean = true): List<FlagSnapshot>
    suspend fun findAll(): List<FlagSnapshot>
    suspend fun create(snapshot: FlagSnapshot): FlagSnapshot
}
