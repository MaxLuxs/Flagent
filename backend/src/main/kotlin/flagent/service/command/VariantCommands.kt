package flagent.service.command

/**
 * Command objects for Variant operations.
 */

data class CreateVariantCommand(
    val flagId: Int,
    val key: String,
    val attachment: Map<String, String>? = null
)

data class PutVariantCommand(
    val flagId: Int,
    val variantId: Int,
    val key: String,
    val attachment: Map<String, String>? = null
)
