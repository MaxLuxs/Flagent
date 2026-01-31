package flagent.api

/**
 * Minimal segment service interface for enterprise (smart rollout).
 * Implemented by backend; enterprise uses it via CoreDependencies.
 */
interface CoreSegmentService {
    suspend fun getSegment(id: Int): SegmentInfo?
    suspend fun updateSegmentRollout(segmentId: Int, rolloutPercent: Int)
}

data class SegmentInfo(
    val id: Int,
    val rolloutPercent: Int
)
