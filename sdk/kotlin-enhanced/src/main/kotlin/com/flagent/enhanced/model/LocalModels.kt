package com.flagent.enhanced.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * Local flag snapshot for client-side evaluation.
 *
 * Contains all flag configurations needed for offline evaluation.
 */
@Serializable
data class FlagSnapshot(
    val flags: Map<Long, LocalFlag>,
    val revision: String? = null,
    val fetchedAt: Long = System.currentTimeMillis(),
    val ttlMs: Long = 300000 // 5 minutes default
) {
    fun isExpired(): Boolean {
        return System.currentTimeMillis() - fetchedAt > ttlMs
    }
}

/**
 * Local flag model for client-side evaluation.
 */
@Serializable
data class LocalFlag(
    val id: Long,
    val key: String,
    val enabled: Boolean,
    val segments: List<LocalSegment> = emptyList(),
    val variants: List<LocalVariant> = emptyList(),
    val description: String? = null,
    val entityType: String? = null,
    val updatedAt: Long? = null
)

/**
 * Local segment model.
 */
@Serializable
data class LocalSegment(
    val id: Long,
    val rank: Int,
    val rolloutPercent: Int,
    val constraints: List<LocalConstraint> = emptyList(),
    val distributions: List<LocalDistribution> = emptyList(),
    val description: String? = null
)

/**
 * Local variant model.
 */
@Serializable
data class LocalVariant(
    val id: Long,
    val key: String,
    val attachment: JsonObject? = null
)

/**
 * Local constraint model.
 */
@Serializable
data class LocalConstraint(
    val id: Long,
    val property: String,
    val operator: String,
    val value: String?
)

/**
 * Local distribution model.
 */
@Serializable
data class LocalDistribution(
    val id: Long,
    val variantID: Long,
    val variantKey: String,
    val percent: Int
)

/**
 * Local evaluation result.
 */
@Serializable
data class LocalEvaluationResult(
    val flagID: Long?,
    val flagKey: String?,
    val variantID: Long?,
    val variantKey: String?,
    val variantAttachment: JsonObject?,
    val segmentID: Long?,
    val reason: String,
    val debugLogs: List<String> = emptyList()
) {
    /**
     * Check if flag is enabled and has a variant assigned.
     */
    fun isEnabled(): Boolean = variantKey != null
    
    /**
     * Get attachment value by key.
     */
    fun getAttachmentValue(key: String): String? {
        return variantAttachment?.get(key)?.toString()?.trim('"')
    }
}
