package flagent.service.command

/**
 * Command objects for Tag operations.
 */

data class CreateTagCommand(
    val flagId: Int,
    val value: String
)
