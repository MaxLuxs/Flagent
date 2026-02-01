package com.flagent.enhanced.model

import com.flagent.enhanced.platform.currentTimeMs
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class FlagSnapshot(
    val flags: Map<Long, LocalFlag>,
    val revision: String? = null,
    val fetchedAt: Long = currentTimeMs(),
    val ttlMs: Long = 300000
) {
    fun isExpired(): Boolean = currentTimeMs() - fetchedAt > ttlMs
}

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

@Serializable
data class LocalSegment(
    val id: Long,
    val rank: Int,
    val rolloutPercent: Int,
    val constraints: List<LocalConstraint> = emptyList(),
    val distributions: List<LocalDistribution> = emptyList(),
    val description: String? = null
)

@Serializable
data class LocalVariant(
    val id: Long,
    val key: String,
    val attachment: JsonObject? = null
)

@Serializable
data class LocalConstraint(
    val id: Long,
    val property: String,
    val operator: String,
    val value: String?
)

@Serializable
data class LocalDistribution(
    val id: Long,
    val variantID: Long,
    val variantKey: String,
    val percent: Int
)

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
    fun isEnabled(): Boolean = variantKey != null
    fun getAttachmentValue(key: String): String? =
        variantAttachment?.get(key)?.toString()?.trim('"')
}
