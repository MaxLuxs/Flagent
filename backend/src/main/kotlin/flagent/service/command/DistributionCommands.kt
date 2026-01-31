package flagent.service.command

/**
 * Command objects for Distribution operations.
 */

data class PutDistributionsCommand(
    val flagId: Int,
    val segmentId: Int,
    val distributions: List<DistributionItemCommand>
)

data class DistributionItemCommand(
    val variantID: Int,
    val variantKey: String? = null,
    val percent: Int
)
