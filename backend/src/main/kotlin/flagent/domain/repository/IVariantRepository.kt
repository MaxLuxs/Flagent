package flagent.domain.repository

import flagent.domain.entity.Variant

/**
 * Variant repository interface
 * Domain layer - no framework dependencies
 */
interface IVariantRepository {
    suspend fun findByFlagId(flagId: Int): List<Variant>
    suspend fun findById(id: Int): Variant?
    suspend fun create(variant: Variant): Variant
    suspend fun update(variant: Variant): Variant
    suspend fun delete(id: Int)
}
