package flagent.domain.repository

import flagent.domain.entity.Segment

/**
 * Segment repository interface
 * Domain layer - no framework dependencies
 */
interface ISegmentRepository {
    suspend fun findByFlagId(flagId: Int): List<Segment>
    suspend fun findById(id: Int): Segment?
    suspend fun create(segment: Segment): Segment
    suspend fun update(segment: Segment): Segment
    suspend fun delete(id: Int)
    suspend fun reorder(flagId: Int, segmentIds: List<Int>)
}
