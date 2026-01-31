package flagent.application

import flagent.api.CoreSegmentService
import flagent.api.SegmentInfo
import flagent.service.SegmentService
import flagent.service.command.PutSegmentCommand

/**
 * Adapter from backend SegmentService to shared CoreSegmentService for enterprise.
 */
class CoreSegmentServiceAdapter(
    private val segmentService: SegmentService
) : CoreSegmentService {
    override suspend fun getSegment(id: Int): SegmentInfo? {
        return segmentService.getSegment(id)?.let {
            SegmentInfo(id = it.id, rolloutPercent = it.rolloutPercent)
        }
    }

    override suspend fun updateSegmentRollout(segmentId: Int, rolloutPercent: Int) {
        segmentService.getSegment(segmentId)?.let { segment ->
            val command = PutSegmentCommand(
                description = segment.description ?: "",
                rolloutPercent = rolloutPercent
            )
            segmentService.updateSegment(segmentId, command, null)
        }
    }
}
