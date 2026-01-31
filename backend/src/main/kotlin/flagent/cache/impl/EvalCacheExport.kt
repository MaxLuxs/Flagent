package flagent.cache.impl

import flagent.domain.entity.*

/**
 * Serializable DTOs for EvalCache JSON export/import.
 * Domain entities have no @Serializable; these types are used at cache/route boundary.
 */
@kotlinx.serialization.Serializable
data class EvalCacheFlagExport(
    val id: Int = 0,
    val key: String,
    val description: String,
    val createdBy: String? = null,
    val updatedBy: String? = null,
    val enabled: Boolean = false,
    val snapshotId: Int = 0,
    val notes: String? = null,
    val dataRecordsEnabled: Boolean = false,
    val entityType: String? = null,
    val segments: List<EvalCacheSegmentExport> = emptyList(),
    val variants: List<EvalCacheVariantExport> = emptyList(),
    val tags: List<EvalCacheTagExport> = emptyList(),
    val updatedAt: String? = null
)

@kotlinx.serialization.Serializable
data class EvalCacheSegmentExport(
    val id: Int = 0,
    val flagId: Int,
    val description: String? = null,
    val rank: Int = 999,
    val rolloutPercent: Int = 0,
    val constraints: List<EvalCacheConstraintExport> = emptyList(),
    val distributions: List<EvalCacheDistributionExport> = emptyList()
)

@kotlinx.serialization.Serializable
data class EvalCacheConstraintExport(
    val id: Int = 0,
    val segmentId: Int,
    val property: String,
    val operator: String,
    val value: String
)

@kotlinx.serialization.Serializable
data class EvalCacheDistributionExport(
    val id: Int = 0,
    val segmentId: Int,
    val variantId: Int,
    val variantKey: String? = null,
    val percent: Int = 0
)

@kotlinx.serialization.Serializable
data class EvalCacheVariantExport(
    val id: Int = 0,
    val flagId: Int,
    val key: String,
    val attachment: Map<String, String>? = null
)

@kotlinx.serialization.Serializable
data class EvalCacheTagExport(
    val id: Int = 0,
    val value: String
)

fun Flag.toEvalCacheExport(): EvalCacheFlagExport = EvalCacheFlagExport(
    id = id,
    key = key,
    description = description,
    createdBy = createdBy,
    updatedBy = updatedBy,
    enabled = enabled,
    snapshotId = snapshotId,
    notes = notes,
    dataRecordsEnabled = dataRecordsEnabled,
    entityType = entityType,
    segments = segments.map { it.toEvalCacheExport() },
    variants = variants.map { it.toEvalCacheExport() },
    tags = tags.map { it.toEvalCacheExport() },
    updatedAt = updatedAt
)

private fun Segment.toEvalCacheExport() = EvalCacheSegmentExport(
    id = id,
    flagId = flagId,
    description = description,
    rank = rank,
    rolloutPercent = rolloutPercent,
    constraints = constraints.map { it.toEvalCacheExport() },
    distributions = distributions.map { it.toEvalCacheExport() }
)

private fun Constraint.toEvalCacheExport() = EvalCacheConstraintExport(
    id = id,
    segmentId = segmentId,
    property = property,
    operator = operator,
    value = value
)

private fun Distribution.toEvalCacheExport() = EvalCacheDistributionExport(
    id = id,
    segmentId = segmentId,
    variantId = variantId,
    variantKey = variantKey,
    percent = percent
)

private fun Variant.toEvalCacheExport() = EvalCacheVariantExport(
    id = id,
    flagId = flagId,
    key = key,
    attachment = attachment
)

private fun Tag.toEvalCacheExport() = EvalCacheTagExport(id = id, value = value)

fun EvalCacheFlagExport.toFlag(): Flag = Flag(
    id = id,
    key = key,
    description = description,
    createdBy = createdBy,
    updatedBy = updatedBy,
    enabled = enabled,
    snapshotId = snapshotId,
    notes = notes,
    dataRecordsEnabled = dataRecordsEnabled,
    entityType = entityType,
    segments = segments.map { it.toSegment() },
    variants = variants.map { it.toVariant() },
    tags = tags.map { it.toTag() },
    updatedAt = updatedAt
)

private fun EvalCacheSegmentExport.toSegment() = Segment(
    id = id,
    flagId = flagId,
    description = description,
    rank = rank,
    rolloutPercent = rolloutPercent,
    constraints = constraints.map { it.toConstraint() },
    distributions = distributions.map { it.toDistribution() }
)

private fun EvalCacheConstraintExport.toConstraint() = Constraint(
    id = id,
    segmentId = segmentId,
    property = property,
    operator = operator,
    value = value
)

private fun EvalCacheDistributionExport.toDistribution() = Distribution(
    id = id,
    segmentId = segmentId,
    variantId = variantId,
    variantKey = variantKey,
    percent = percent
)

private fun EvalCacheVariantExport.toVariant() = Variant(
    id = id,
    flagId = flagId,
    key = key,
    attachment = attachment
)

private fun EvalCacheTagExport.toTag() = Tag(id = id, value = value)
