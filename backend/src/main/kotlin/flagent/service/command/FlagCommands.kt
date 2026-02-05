package flagent.service.command

/**
 * Command objects for Flag operations.
 * Routes map API requests to Commands; Services accept Commands and build domain entities.
 */

data class CreateFlagCommand(
    val key: String?,
    val description: String,
    val template: String? = null,
    val environmentId: Long? = null
)

data class PutFlagCommand(
    val description: String?,
    val key: String?,
    val dataRecordsEnabled: Boolean?,
    val entityType: String?,
    val notes: String?,
    val environmentId: Long? = null
)

data class SetFlagEnabledCommand(
    val enabled: Boolean
)
