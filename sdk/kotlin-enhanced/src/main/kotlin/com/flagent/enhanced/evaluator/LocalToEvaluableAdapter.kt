package com.flagent.enhanced.evaluator

import com.flagent.enhanced.model.*
import flagent.evaluator.FlagEvaluator

/**
 * Converts LocalFlag snapshot models to shared EvaluableFlag format.
 * Enables LocalEvaluator to delegate to shared FlagEvaluator.
 */
object LocalToEvaluableAdapter {

    /**
     * Converts LocalFlag to shared EvaluableFlag.
     * ID overflow: Long IDs beyond Int.MAX_VALUE are truncated (uncommon for typical flag IDs).
     */
    fun LocalFlag.toEvaluable(): FlagEvaluator.EvaluableFlag {
        return FlagEvaluator.EvaluableFlag(
            id = id.toInt(),
            key = key,
            enabled = enabled,
            segments = segments.map { it.toEvaluable() }
        )
    }

    private fun LocalSegment.toEvaluable(): FlagEvaluator.EvaluableSegment {
        return FlagEvaluator.EvaluableSegment(
            id = id.toInt(),
            rank = rank,
            rolloutPercent = rolloutPercent,
            constraints = constraints.map { it.toEvaluable() },
            distributions = distributions.map { it.toEvaluable() }
        )
    }

    private fun LocalConstraint.toEvaluable(): FlagEvaluator.EvaluableConstraint {
        return FlagEvaluator.EvaluableConstraint(
            id = id.toInt(),
            property = property,
            operator = operator,
            value = value ?: ""
        )
    }

    private fun LocalDistribution.toEvaluable(): FlagEvaluator.EvaluableDistribution {
        return FlagEvaluator.EvaluableDistribution(
            id = id.toInt(),
            variantId = variantID.toInt(),
            percent = percent
        )
    }
}
