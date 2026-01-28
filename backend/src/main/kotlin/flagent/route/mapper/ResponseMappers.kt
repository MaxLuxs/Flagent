package flagent.route.mapper

import flagent.api.model.*
import flagent.domain.entity.Flag as DomainFlag
import flagent.domain.entity.Segment as DomainSegment
import flagent.domain.entity.Variant as DomainVariant
import flagent.domain.entity.Constraint as DomainConstraint
import flagent.domain.entity.Distribution as DomainDistribution
import flagent.domain.entity.Tag as DomainTag

/**
 * ResponseMappers - centralized mappers from domain entities to API response models
 * Eliminates duplication across route files
 */
object ResponseMappers {
    
    /**
     * Map Flag entity to FlagResponse
     */
    fun mapFlagToResponse(flag: DomainFlag): FlagResponse {
        return FlagResponse(
            id = flag.id,
            key = flag.key,
            description = flag.description,
            createdBy = flag.createdBy,
            updatedBy = flag.updatedBy,
            enabled = flag.enabled,
            snapshotID = flag.snapshotId,
            notes = flag.notes,
            dataRecordsEnabled = flag.dataRecordsEnabled,
            entityType = flag.entityType,
            segments = flag.segments.map { mapSegmentToResponse(it) },
            variants = flag.variants.map { mapVariantToResponse(it) },
            tags = flag.tags.map { mapTagToResponse(it) },
            updatedAt = flag.updatedAt
        )
    }
    
    /**
     * Map Segment entity to SegmentResponse
     */
    fun mapSegmentToResponse(segment: DomainSegment): SegmentResponse {
        return SegmentResponse(
            id = segment.id,
            flagID = segment.flagId,
            description = segment.description,
            rank = segment.rank,
            rolloutPercent = segment.rolloutPercent,
            constraints = segment.constraints.map { mapConstraintToResponse(it) },
            distributions = segment.distributions.map { mapDistributionToResponse(it) }
        )
    }
    
    /**
     * Map Variant entity to VariantResponse
     */
    fun mapVariantToResponse(variant: DomainVariant): VariantResponse {
        return VariantResponse(
            id = variant.id,
            flagID = variant.flagId,
            key = variant.key,
            attachment = variant.attachment?.entries?.associate { it.key to it.value.toString() }
        )
    }
    
    /**
     * Map Constraint entity to ConstraintResponse
     */
    fun mapConstraintToResponse(constraint: DomainConstraint): ConstraintResponse {
        return ConstraintResponse(
            id = constraint.id,
            segmentID = constraint.segmentId,
            property = constraint.property,
            operator = constraint.operator,
            value = constraint.value
        )
    }
    
    /**
     * Map Distribution entity to DistributionResponse
     */
    fun mapDistributionToResponse(distribution: DomainDistribution): DistributionResponse {
        return DistributionResponse(
            id = distribution.id,
            segmentID = distribution.segmentId,
            variantID = distribution.variantId,
            variantKey = distribution.variantKey,
            percent = distribution.percent
        )
    }
    
    /**
     * Map Tag entity to TagResponse
     */
    fun mapTagToResponse(tag: DomainTag): TagResponse {
        return TagResponse(
            id = tag.id,
            value = tag.value
        )
    }
}
