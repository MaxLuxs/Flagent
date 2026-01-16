package flagent.domain.repository

import flagent.domain.entity.Constraint

/**
 * Constraint repository interface
 * Domain layer - no framework dependencies
 */
interface IConstraintRepository {
    suspend fun findBySegmentId(segmentId: Int): List<Constraint>
    suspend fun findById(id: Int): Constraint?
    suspend fun create(constraint: Constraint): Constraint
    suspend fun update(constraint: Constraint): Constraint
    suspend fun delete(id: Int)
}
