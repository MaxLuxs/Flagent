package com.flagent.enhanced.evaluator

import com.flagent.enhanced.model.*
import kotlinx.serialization.json.JsonObject
import java.util.zip.CRC32

/**
 * LocalEvaluator - evaluates flags locally without API calls.
 *
 * Implements the same evaluation logic as the backend server, enabling:
 * - Offline evaluation
 * - Sub-millisecond latency
 * - Reduced server load
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
class LocalEvaluator {
    
    companion object {
        private const val TOTAL_BUCKET_NUM = 1000
        private const val PERCENT_MULTIPLIER = 10
    }

    /**
     * Evaluate a flag using local snapshot.
     *
     * @param flagKey The flag key to evaluate
     * @param flagID The flag ID (optional, used if flagKey not found)
     * @param entityID The entity ID for consistent hashing
     * @param entityType The entity type (optional)
     * @param entityContext Additional context for constraint evaluation
     * @param snapshot The flag snapshot containing all flag configurations
     * @param enableDebug Enable debug logging for troubleshooting
     * @return Evaluation result with variant assignment
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
        // Find flag by key or ID
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

        // Check if flag is enabled
        if (!flag.enabled) {
            return LocalEvaluationResult(
                flagID = flag.id,
                flagKey = flag.key,
                variantID = null,
                variantKey = null,
                variantAttachment = null,
                segmentID = null,
                reason = "FLAG_DISABLED",
                debugLogs = if (enableDebug) listOf("Flag is disabled") else emptyList()
            )
        }

        // Check if flag has segments
        if (flag.segments.isEmpty()) {
            return LocalEvaluationResult(
                flagID = flag.id,
                flagKey = flag.key,
                variantID = null,
                variantKey = null,
                variantAttachment = null,
                segmentID = null,
                reason = "NO_SEGMENTS",
                debugLogs = if (enableDebug) listOf("Flag has no segments") else emptyList()
            )
        }

        val debugLogs = mutableListOf<String>()
        
        // Evaluate segments in order (by rank)
        for (segment in flag.segments.sortedBy { it.rank }) {
            if (enableDebug) {
                debugLogs.add("Evaluating segment ${segment.id} (rank ${segment.rank})")
            }

            // Check constraints
            val constraintMatches = evaluateConstraints(segment.constraints, entityContext)
            
            if (!constraintMatches) {
                if (enableDebug) {
                    debugLogs.add("Segment ${segment.id}: constraints did not match")
                }
                continue
            }

            if (enableDebug) {
                debugLogs.add("Segment ${segment.id}: constraints matched")
            }

            // Check rollout
            val (variantID, inRollout) = selectVariant(
                segment = segment,
                entityID = entityID,
                flagID = flag.id
            )

            if (!inRollout) {
                if (enableDebug) {
                    debugLogs.add("Segment ${segment.id}: not in rollout percentage")
                }
                continue
            }

            // Find variant
            val variant = flag.variants.firstOrNull { it.id == variantID }
            
            return LocalEvaluationResult(
                flagID = flag.id,
                flagKey = flag.key,
                variantID = variantID,
                variantKey = variant?.key,
                variantAttachment = variant?.attachment,
                segmentID = segment.id,
                reason = "MATCH",
                debugLogs = if (enableDebug) {
                    debugLogs + "Segment ${segment.id}: matched, assigned variant $variantID"
                } else emptyList()
            )
        }

        // No segment matched
        return LocalEvaluationResult(
            flagID = flag.id,
            flagKey = flag.key,
            variantID = null,
            variantKey = null,
            variantAttachment = null,
            segmentID = null,
            reason = "NO_MATCH",
            debugLogs = if (enableDebug) {
                debugLogs + "No segment matched"
            } else emptyList()
        )
    }

    /**
     * Evaluate constraints against entity context.
     *
     * @param constraints List of constraints to evaluate
     * @param context Entity context map
     * @return true if all constraints match (AND logic)
     */
    private fun evaluateConstraints(
        constraints: List<LocalConstraint>,
        context: Map<String, Any>
    ): Boolean {
        if (constraints.isEmpty()) {
            return true
        }

        return constraints.all { constraint ->
            evaluateConstraint(constraint, context)
        }
    }

    /**
     * Evaluate a single constraint.
     *
     * @param constraint The constraint to evaluate
     * @param context Entity context map
     * @return true if constraint matches
     */
    private fun evaluateConstraint(
        constraint: LocalConstraint,
        context: Map<String, Any>
    ): Boolean {
        val contextValue = context[constraint.property]?.toString()
        val constraintValue = constraint.value

        return when (constraint.operator) {
            "EQ" -> contextValue == constraintValue
            "NEQ" -> contextValue != constraintValue
            "LT" -> (contextValue?.toDoubleOrNull() ?: 0.0) < (constraintValue?.toDoubleOrNull() ?: 0.0)
            "LTE" -> (contextValue?.toDoubleOrNull() ?: 0.0) <= (constraintValue?.toDoubleOrNull() ?: 0.0)
            "GT" -> (contextValue?.toDoubleOrNull() ?: 0.0) > (constraintValue?.toDoubleOrNull() ?: 0.0)
            "GTE" -> (contextValue?.toDoubleOrNull() ?: 0.0) >= (constraintValue?.toDoubleOrNull() ?: 0.0)
            "IN" -> {
                val values = constraintValue?.split(",")?.map { it.trim() } ?: emptyList()
                values.contains(contextValue)
            }
            "NOTIN" -> {
                val values = constraintValue?.split(",")?.map { it.trim() } ?: emptyList()
                !values.contains(contextValue)
            }
            "CONTAINS" -> contextValue?.contains(constraintValue ?: "") == true
            "NOTCONTAINS" -> contextValue?.contains(constraintValue ?: "") == false
            "EREG" -> {
                if (constraintValue == null) return false
                contextValue?.matches(Regex(constraintValue)) == true
            }
            "NEREG" -> {
                if (constraintValue == null) return true
                contextValue?.matches(Regex(constraintValue)) != true
            }
            else -> false
        }
    }

    /**
     * Select variant based on distribution and rollout.
     *
     * Uses consistent hashing (CRC32) for deterministic variant selection.
     *
     * @param segment Segment with distributions
     * @param entityID Entity ID for hashing
     * @param flagID Flag ID for salt
     * @return Pair of (variantID, inRollout)
     */
    private fun selectVariant(
        segment: LocalSegment,
        entityID: String,
        flagID: Long
    ): Pair<Long?, Boolean> {
        // Calculate bucket number using consistent hashing
        val salt = flagID.toString()
        val hashInput = "$entityID:$salt"
        val crc32 = CRC32()
        crc32.update(hashInput.toByteArray())
        val bucket = (crc32.value % TOTAL_BUCKET_NUM).toInt()

        // Check rollout percentage
        val rolloutBucket = (segment.rolloutPercent * PERCENT_MULTIPLIER).toInt()
        if (bucket >= rolloutBucket) {
            return Pair(null, false)
        }

        // Select variant based on distribution
        if (segment.distributions.isEmpty()) {
            return Pair(null, true)
        }

        var cumulativePercent = 0
        for (distribution in segment.distributions) {
            cumulativePercent += distribution.percent
            val distributionBucket = (cumulativePercent * PERCENT_MULTIPLIER).toInt()
            if (bucket < distributionBucket) {
                return Pair(distribution.variantID, true)
            }
        }

        // Default to first distribution
        return Pair(segment.distributions.firstOrNull()?.variantID, true)
    }

    /**
     * Batch evaluate multiple flags.
     *
     * @param evaluations List of evaluation requests
     * @param snapshot Flag snapshot
     * @return List of evaluation results
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
