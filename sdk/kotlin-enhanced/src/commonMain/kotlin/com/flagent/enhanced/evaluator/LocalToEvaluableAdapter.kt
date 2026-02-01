package com.flagent.enhanced.evaluator

import com.flagent.enhanced.model.*
import flagent.evaluator.FlagEvaluator

fun LocalFlag.toEvaluable(): FlagEvaluator.EvaluableFlag =
    FlagEvaluator.EvaluableFlag(
        id = id.toInt(),
        key = key,
        enabled = enabled,
        segments = segments.map { it.toEvaluable() }
    )

private fun LocalSegment.toEvaluable(): FlagEvaluator.EvaluableSegment =
    FlagEvaluator.EvaluableSegment(
        id = id.toInt(),
        rank = rank,
        rolloutPercent = rolloutPercent,
        constraints = constraints.map { it.toEvaluable() },
        distributions = distributions.map { it.toEvaluable() }
    )

private fun LocalConstraint.toEvaluable(): FlagEvaluator.EvaluableConstraint =
    FlagEvaluator.EvaluableConstraint(
        id = id.toInt(),
        property = property,
        operator = operator,
        value = value ?: ""
    )

private fun LocalDistribution.toEvaluable(): FlagEvaluator.EvaluableDistribution =
    FlagEvaluator.EvaluableDistribution(
        id = id.toInt(),
        variantId = variantID.toInt(),
        percent = percent
    )
