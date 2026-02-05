package flagent.domain.entity

import kotlinx.serialization.Serializable

/**
 * Webhook - configuration for outgoing webhook notifications.
 */
@Serializable
data class Webhook(
    val id: Int = 0,
    val url: String,
    val events: List<String>,
    val secret: String? = null,
    val enabled: Boolean = true,
    val tenantId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun subscribesTo(event: String): Boolean = events.contains(event)
}

object WebhookEvents {
    const val FLAG_CREATED = "flag.created"
    const val FLAG_UPDATED = "flag.updated"
    const val FLAG_DELETED = "flag.deleted"
    const val FLAG_ENABLED = "flag.enabled"
    const val FLAG_DISABLED = "flag.disabled"
    const val ANOMALY_DETECTED = "anomaly.detected"

    val ALL_FLAG_EVENTS = listOf(FLAG_CREATED, FLAG_UPDATED, FLAG_DELETED, FLAG_ENABLED, FLAG_DISABLED)
    val ALL = ALL_FLAG_EVENTS + ANOMALY_DETECTED
}
