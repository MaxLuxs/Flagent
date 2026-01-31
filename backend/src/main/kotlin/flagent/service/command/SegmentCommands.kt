package flagent.service.command

/**
 * Command objects for Segment operations.
 */

data class CreateSegmentCommand(
    val flagId: Int,
    val description: String,
    val rolloutPercent: Int
)

data class PutSegmentCommand(
    val description: String,
    val rolloutPercent: Int
)

data class PutSegmentReorderCommand(
    val segmentIDs: List<Int>
)
