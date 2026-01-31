package com.flagent.enhanced.evaluator

import com.flagent.enhanced.model.*
import com.flagent.enhanced.evaluator.LocalToEvaluableAdapter.toEvaluable
import flagent.evaluator.FlagEvaluator

/**
 * LocalEvaluator - evaluates flags locally without API calls.
 *
 * Delegates to shared FlagEvaluator for consistent results with backend.
 *
 * @example
 * ```
 * val evaluator = LocalEvaluator()
 * val snapshot = flagStore.getSnapshot()
 * val result = evaluator.evaluate(
 *     flagKey = "new_feature",
 *     entityID = "user123",
 *     entityContext = mapOf("region" to "US"),
 *     snapshot = snapshot
 * )
 * ```
 */
class LocalEvaluator(
    private val flagEvaluator: FlagEvaluator = FlagEvaluator()
) {

    /**
     * Evaluate a flag using local snapshot.
     */
    fun evaluate(
        flagKey: String? = null,
        flagID: Long? = null,
        entityID: String,
        entityType: String? = null,
        entityContext: Map<String, Any> = emptyMap(),
        snapshot: FlagSnapshot,
        enableDebug: Boolean = false
    ): LocalEvaluationResult {
        val flag = when {
            flagKey != null -> snapshot.flags.values.firstOrNull { it.key == flagKey }
            flagID != null -> snapshot.flags[flagID]
            else -> null
        }

        if (flag == null) {
            return LocalEvaluationResult(
                flagID = flagID,
                flagKey = flagKey,
                variantID = null,
                variantKey = null,
                variantAttachment = null,
                segmentID = null,
                reason = "FLAG_NOT_FOUND",
                debugLogs = if (enableDebug) listOf("Flag not found in snapshot") else emptyList()
            )
        }

        val evaluableFlag = flag.toEvaluable()
        val context = FlagEvaluator.EvalContext(
            entityID = entityID,
            entityType = entityType,
            entityContext = entityContext.mapValues { (_, v) -> v.toString() }
        )

        val result = flagEvaluator.evaluate(
            flag = evaluableFlag,
            context = context,
            enableDebug = enableDebug
        )

        val variantIDLong = result.variantID?.toLong()
        val variant = variantIDLong?.let { id -> flag.variants.firstOrNull { it.id == id } }

        val reason = when {
            !flag.enabled -> "FLAG_DISABLED"
            flag.segments.isEmpty() -> "NO_SEGMENTS"
            result.variantID != null -> "MATCH"
            else -> "NO_MATCH"
        }

        val debugLogs = if (enableDebug) {
            result.debugLogs.map { "Segment ${it.segmentID}: ${it.message}" }
        } else {
            emptyList()
        }

        return LocalEvaluationResult(
            flagID = flag.id,
            flagKey = flag.key,
            variantID = variantIDLong,
            variantKey = variant?.key,
            variantAttachment = variant?.attachment,
            segmentID = result.segmentID?.toLong(),
            reason = reason,
            debugLogs = debugLogs
        )
    }

    /**
     * Batch evaluate multiple flags.
     */
    fun evaluateBatch(
        evaluations: List<BatchEvaluationRequest>,
        snapshot: FlagSnapshot
    ): List<LocalEvaluationResult> {
        return evaluations.map { request ->
            evaluate(
                flagKey = request.flagKey,
                flagID = request.flagID,
                entityID = request.entityID,
                entityType = request.entityType,
                entityContext = request.entityContext,
                snapshot = snapshot,
                enableDebug = request.enableDebug
            )
        }
    }
}

/**
 * Batch evaluation request.
 */
data class BatchEvaluationRequest(
    val flagKey: String? = null,
    val flagID: Long? = null,
    val entityID: String,
    val entityType: String? = null,
    val entityContext: Map<String, Any> = emptyMap(),
    val enableDebug: Boolean = false
)
