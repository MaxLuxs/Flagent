package flagent.evaluator

import kotlin.random.Random

/**
 * FlagEvaluator - core evaluation logic for feature flags
 * Shared across all platforms (JVM, JS, Native)
 * 
 * This is the single source of truth for evaluation logic.
 */
class FlagEvaluator(
    private val constraintEvaluator: ConstraintEvaluator = ConstraintEvaluator()
) {
    private val random = Random.Default
    
    /**
     * Evaluation context for flag evaluation
     */
    data class EvalContext(
        val entityID: String = "",
        val entityType: String? = null,
        val entityContext: Map<String, String> = emptyMap()
    )
    
    /**
     * Evaluation result
     */
    data class EvaluationResult(
        val variantID: Int?,
        val segmentID: Int?,
        val debugLogs: List<SegmentDebugLog> = emptyList()
    )
    
    /**
     * Segment debug log
     */
    data class SegmentDebugLog(
        val segmentID: Int,
        val message: String
    )
    
    /**
     * Minimal flag model for evaluation
     */
    data class EvaluableFlag(
        val id: Int,
        val key: String,
        val enabled: Boolean,
        val segments: List<EvaluableSegment>
    )
    
    /**
     * Minimal segment model for evaluation
     */
    data class EvaluableSegment(
        val id: Int,
        val rank: Int,
        val rolloutPercent: Int,
        val constraints: List<EvaluableConstraint>,
        val distributions: List<EvaluableDistribution>
    ) {
        /**
         * Build distribution array with accumulated percents in 0-1000 scale (EVALUATION_SPEC).
         */
        fun prepareDistribution(): DistributionArray {
            if (distributions.isEmpty()) {
                return DistributionArray(emptyList(), emptyList())
            }
            val sortedDist = distributions.sortedBy { it.percent }
            val variantIds = mutableListOf<Int>()
            val percentsAccumulated = mutableListOf<Int>()
            var accumulated = 0
            for (dist in sortedDist) {
                accumulated += dist.percent * 10 // 0-1000 scale
                variantIds.add(dist.variantId)
                percentsAccumulated.add(accumulated)
            }
            return DistributionArray(variantIds, percentsAccumulated)
        }
    }
    
    /**
     * Minimal constraint model for evaluation
     */
    data class EvaluableConstraint(
        val id: Int,
        val property: String,
        val operator: String,
        val value: String
    )
    
    /**
     * Minimal distribution model for evaluation
     */
    data class EvaluableDistribution(
        val id: Int,
        val variantId: Int,
        val percent: Int
    )
    
    /**
     * Distribution array for fast variant selection. Uses RolloutAlgorithm (salt+entityID, EVALUATION_SPEC).
     */
    data class DistributionArray(
        val variantIds: List<Int>,
        val percentsAccumulated: List<Int>
    ) {
        fun rollout(entityID: String, salt: String, rolloutPercent: Int): Pair<Int?, String> {
            return RolloutAlgorithm.rollout(
                entityID = entityID,
                salt = salt,
                rolloutPercent = rolloutPercent,
                variantIds = variantIds,
                percentsAccumulated = percentsAccumulated
            )
        }
    }
    
    /**
     * Evaluate flag with context
     * Returns evaluation result with variant ID and segment ID
     */
    fun evaluate(
        flag: EvaluableFlag,
        context: EvalContext,
        enableDebug: Boolean = false
    ): EvaluationResult {
        // Validate flag
        if (!flag.enabled) {
            return EvaluationResult(
                variantID = null,
                segmentID = null,
                debugLogs = if (enableDebug) {
                    listOf(SegmentDebugLog(0, "flagID ${flag.id} is not enabled"))
                } else emptyList()
            )
        }
        
        if (flag.segments.isEmpty()) {
            return EvaluationResult(
                variantID = null,
                segmentID = null,
                debugLogs = if (enableDebug) {
                    listOf(SegmentDebugLog(0, "flagID ${flag.id} has no segments"))
                } else emptyList()
            )
        }
        
        // Evaluate segments in order (by rank)
        val sortedSegments = flag.segments.sortedBy { it.rank }
        val debugLogs = mutableListOf<SegmentDebugLog>()
        var variantID: Int? = null
        var segmentID: Int? = null
        
        for (segment in sortedSegments) {
            segmentID = segment.id
            
            // Check if segment matches constraints
            val matches = constraintEvaluator.evaluate(segment.constraints, context)
            
            if (!matches) {
                if (enableDebug) {
                    debugLogs.add(
                        SegmentDebugLog(
                            segmentID = segment.id,
                            message = "segment_id ${segment.id} did not match constraints"
                        )
                    )
                }
                continue
            }
            
            // Select variant based on distribution
            val distributionArray = segment.prepareDistribution()
            val (selectedVariantID, rolloutMessage) = distributionArray.rollout(
                entityID = context.entityID,
                salt = flag.id.toString(),
                rolloutPercent = segment.rolloutPercent
            )
            
            if (selectedVariantID != null) {
                variantID = selectedVariantID
                if (enableDebug) {
                    debugLogs.add(
                        SegmentDebugLog(
                            segmentID = segment.id,
                            message = "matched all constraints. rollout yes. variantID: $selectedVariantID"
                        )
                    )
                }
                break
            } else {
                if (enableDebug) {
                    debugLogs.add(
                        SegmentDebugLog(
                            segmentID = segment.id,
                            message = "matched all constraints. rollout no."
                        )
                    )
                }
            }
        }
        
        return EvaluationResult(
            variantID = variantID,
            segmentID = segmentID,
            debugLogs = debugLogs
        )
    }
}
