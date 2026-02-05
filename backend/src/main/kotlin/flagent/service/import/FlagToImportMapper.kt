package flagent.service.import

import flagent.domain.entity.Flag

/**
 * Maps Flag (domain) to FlagImportItem (GitOps import format).
 */
fun Flag.toFlagImportItem(): FlagImportItem = FlagImportItem(
    key = key,
    description = description,
    enabled = enabled,
    notes = notes,
    dataRecordsEnabled = dataRecordsEnabled,
    entityType = entityType,
    segments = segments.map { seg ->
        SegmentImportItem(
            rank = seg.rank,
            description = seg.description,
            rolloutPercent = seg.rolloutPercent,
            constraints = seg.constraints.map { c ->
                ConstraintImportItem(property = c.property, operator = c.operator, value = c.value)
            },
            distributions = seg.distributions.map { d ->
                DistributionImportItem(
                    variantKey = d.variantKey ?: variants.find { it.id == d.variantId }?.key ?: "",
                    percent = d.percent
                )
            }
        )
    },
    variants = variants.map { v ->
        VariantImportItem(key = v.key, attachment = v.attachment)
    },
    tags = tags.map { it.value }
)
