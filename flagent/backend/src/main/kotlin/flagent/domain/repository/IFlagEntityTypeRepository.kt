package flagent.domain.repository

import flagent.domain.entity.FlagEntityType

/**
 * FlagEntityType repository interface
 * Domain layer - no framework dependencies
 */
interface IFlagEntityTypeRepository {
    suspend fun findAll(): List<FlagEntityType>
    suspend fun findByKey(key: String): FlagEntityType?
    suspend fun create(entityType: FlagEntityType): FlagEntityType
}
