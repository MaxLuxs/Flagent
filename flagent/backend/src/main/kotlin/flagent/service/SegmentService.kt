package flagent.service

import flagent.domain.entity.Segment
import flagent.domain.repository.ISegmentRepository

/**
 * Segment service - handles segment business logic
 * Maps to CRUD operations from pkg/handler/crud.go
 */
class SegmentService(
    private val segmentRepository: ISegmentRepository,
    private val flagSnapshotService: FlagSnapshotService? = null
) {
    companion object {
        const val SegmentDefaultRank = 999
    }
    
    suspend fun findSegmentsByFlagId(flagId: Int): List<Segment> {
        return segmentRepository.findByFlagId(flagId)
    }
    
    suspend fun getSegment(id: Int): Segment? {
        return segmentRepository.findById(id)
    }
    
    suspend fun createSegment(segment: Segment, updatedBy: String? = null): Segment {
        val segmentWithDefaultRank = segment.copy(rank = SegmentDefaultRank)
        val created = segmentRepository.create(segmentWithDefaultRank)
        
        // Save flag snapshot after creating segment
        flagSnapshotService?.saveFlagSnapshot(segment.flagId, updatedBy)
        
        return created
    }
    
    suspend fun updateSegment(segment: Segment, updatedBy: String? = null): Segment {
        val updated = segmentRepository.update(segment)
        
        // Save flag snapshot after updating segment
        flagSnapshotService?.saveFlagSnapshot(segment.flagId, updatedBy)
        
        return updated
    }
    
    suspend fun deleteSegment(id: Int, updatedBy: String? = null) {
        // Get segment to know flagId before deletion
        val segment = segmentRepository.findById(id)
            ?: throw IllegalArgumentException("error finding segmentID $id")
        
        val flagId = segment.flagId
        
        segmentRepository.delete(id)
        
        // Save flag snapshot after deleting segment
        flagSnapshotService?.saveFlagSnapshot(flagId, updatedBy)
    }
    
    suspend fun reorderSegments(flagId: Int, segmentIds: List<Int>, updatedBy: String? = null) {
        segmentRepository.reorder(flagId, segmentIds)
        
        // Save flag snapshot after reordering segments
        flagSnapshotService?.saveFlagSnapshot(flagId, updatedBy)
    }
}