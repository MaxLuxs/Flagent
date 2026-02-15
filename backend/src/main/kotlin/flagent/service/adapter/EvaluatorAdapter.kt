package flagent.service.adapter

import flagent.domain.entity.Constraint
import flagent.domain.entity.Distribution
import flagent.domain.entity.Flag
import flagent.domain.entity.Segment
import flagent.evaluator.FlagEvaluator
import flagent.evaluator.FlagEvaluator.EvalContext

/**
 * EvaluatorAdapter - converts domain entities to evaluable models for shared evaluator
 * This allows backend to use shared evaluation logic without code duplication
 */
object EvaluatorAdapter {

    /**
     * Convert domain Flag to EvaluableFlag
     */
    fun toEvaluableFlag(flag: Flag): FlagEvaluator.EvaluableFlag {
        return FlagEvaluator.EvaluableFlag(
            id = flag.id,
            key = flag.key,
            enabled = flag.enabled,
            segments = flag.segments.map { toEvaluableSegment(it) }
        )
    }

    /**
     * Convert domain Segment to EvaluableSegment
     */
    fun toEvaluableSegment(segment: Segment): FlagEvaluator.EvaluableSegment {
        return FlagEvaluator.EvaluableSegment(
            id = segment.id,
            rank = segment.rank,
            rolloutPercent = segment.rolloutPercent,
            constraints = segment.constraints.map { toEvaluableConstraint(it) },
            distributions = segment.distributions.map { toEvaluableDistribution(it) }
        )
    }

    /**
     * Convert domain Constraint to EvaluableConstraint
     */
    fun toEvaluableConstraint(constraint: Constraint): FlagEvaluator.EvaluableConstraint {
        return FlagEvaluator.EvaluableConstraint(
            id = constraint.id,
            property = constraint.property,
            operator = constraint.operator,
            value = constraint.value
        )
    }

    /**
     * Convert domain Distribution to EvaluableDistribution
     */
    fun toEvaluableDistribution(distribution: Distribution): FlagEvaluator.EvaluableDistribution {
        return FlagEvaluator.EvaluableDistribution(
            id = distribution.id,
            variantId = distribution.variantId,
            percent = distribution.percent
        )
    }

    /**
     * Create EvalContext from parameters
     */
    fun createEvalContext(
        entityID: String,
        entityType: String?,
        entityContext: Map<String, Any>?
    ): EvalContext {
        return EvalContext(
            entityID = entityID,
            entityType = entityType,
            entityContext = entityContext?.mapValues { it.value.toString() } ?: emptyMap()
        )
    }
}
