package flagent.service.command

/**
 * Command objects for Constraint operations.
 */

data class CreateConstraintCommand(
    val segmentId: Int,
    val property: String,
    val operator: String,
    val value: String
)

data class PutConstraintCommand(
    val property: String,
    val operator: String,
    val value: String
)
