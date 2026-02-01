package flagent.service

import flagent.domain.entity.Segment
import flagent.domain.repository.IFlagRepository
import flagent.domain.repository.ISegmentRepository
import flagent.route.RealtimeEventBus
import flagent.service.command.CreateSegmentCommand
import flagent.service.command.PutSegmentCommand

/**
 * Segment service - handles segment business logic
 * Maps to CRUD operations from pkg/handler/crud.go
 */
class SegmentService(
    private val segmentRepository: ISegmentRepository,
    private val flagSnapshotService: FlagSnapshotService? = null,
    private val flagRepository: IFlagRepository? = null,
    private val eventBus: RealtimeEventBus? = null
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
    
    suspend fun createSegment(command: CreateSegmentCommand, updatedBy: String? = null): Segment {
        val segment = Segment(
            flagId = command.flagId,
            description = command.description,
            rolloutPercent = command.rolloutPercent
        )
        val segmentWithDefaultRank = segment.copy(rank = SegmentDefaultRank)
        val created = segmentRepository.create(segmentWithDefaultRank)

        // Save flag snapshot after creating segment
        flagSnapshotService?.saveFlagSnapshot(command.flagId, updatedBy)
        flagRepository?.findById(command.flagId)?.let { flag ->
            eventBus?.publishSegmentUpdated(command.flagId.toLong(), flag.key, created.id.toLong())
        }

        return created
    }
    
    suspend fun updateSegment(segmentId: Int, command: PutSegmentCommand, updatedBy: String? = null): Segment {
        val existingSegment = segmentRepository.findById(segmentId)
            ?: throw IllegalArgumentException("error finding segmentID $segmentId")
        
        val updatedSegment = existingSegment.copy(
            description = command.description,
            rolloutPercent = command.rolloutPercent
        )
        val updated = segmentRepository.update(updatedSegment)

        // Save flag snapshot after updating segment
        flagSnapshotService?.saveFlagSnapshot(existingSegment.flagId, updatedBy)
        flagRepository?.findById(existingSegment.flagId)?.let { flag ->
            eventBus?.publishSegmentUpdated(existingSegment.flagId.toLong(), flag.key, updated.id.toLong())
        }

        return updated
    }
    
    suspend fun deleteSegment(id: Int, updatedBy: String? = null) {
        // Get segment to know flagId before deletion
        val segment = segmentRepository.findById(id)
            ?: throw IllegalArgumentException("error finding segmentID $id")
        
        val flagId = segment.flagId
        flagRepository?.findById(flagId)?.let { flag ->
            eventBus?.publishSegmentUpdated(flagId.toLong(), flag.key, id.toLong())
        }
        segmentRepository.delete(id)

        // Save flag snapshot after deleting segment
        flagSnapshotService?.saveFlagSnapshot(flagId, updatedBy)
    }
    
    suspend fun reorderSegments(flagId: Int, segmentIds: List<Int>, updatedBy: String? = null) {
        segmentRepository.reorder(flagId, segmentIds)
        flagRepository?.findById(flagId)?.let { flag ->
            segmentIds.firstOrNull()?.let { firstId ->
                eventBus?.publishSegmentUpdated(flagId.toLong(), flag.key, firstId.toLong())
            }
        }
        // Save flag snapshot after reordering segments
        flagSnapshotService?.saveFlagSnapshot(flagId, updatedBy)
    }
}