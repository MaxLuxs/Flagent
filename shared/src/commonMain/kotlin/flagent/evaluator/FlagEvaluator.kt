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
        fun prepareDistribution(): DistributionArray {
            if (distributions.isEmpty()) {
                return DistributionArray(emptyList(), emptyList())
            }
            
            val sortedDist = distributions.sortedBy { it.percent }
            val variantIds = mutableListOf<Int>()
            val percentsAccumulated = mutableListOf<Int>()
            
            var accumulated = 0
            for (dist in sortedDist) {
                accumulated += dist.percent
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
     * Distribution array for fast variant selection
     */
    data class DistributionArray(
        val variantIds: List<Int>,
        val percentsAccumulated: List<Int>
    ) {
        companion object {
            const val TOTAL_BUCKET_NUM = 1000u
        }
        
        fun rollout(entityID: String, salt: String, rolloutPercent: Int): Pair<Int?, String> {
            if (entityID.isEmpty()) {
                return null to "rollout no. empty entityID"
            }
            
            if (rolloutPercent <= 0) {
                return null to "rollout no. invalid rolloutPercent: $rolloutPercent"
            }
            
            if (variantIds.isEmpty() || percentsAccumulated.isEmpty()) {
                return null to "rollout no. there's no distribution set"
            }
            
            val bucketNum = crc32Num(entityID, salt)
            val (variantID, index) = bucketByNum(bucketNum)
            
            if (bucketNum > TOTAL_BUCKET_NUM * rolloutPercent.toUInt() / 100u) {
                return null to "rollout no. entityID bucket: $bucketNum rolloutPercent: $rolloutPercent"
            }
            
            return variantID to "matched distribution variantID: $variantID index: $index"
        }
        
        private fun bucketByNum(num: UInt): Pair<Int, Int> {
            val bucket = (num % TOTAL_BUCKET_NUM).toInt()
            
            // Binary search for the variant
            var left = 0
            var right = percentsAccumulated.size - 1
            
            while (left <= right) {
                val mid = (left + right) / 2
                val midPercent = percentsAccumulated[mid] * 10 // Convert to per-mille
                
                if (bucket < midPercent) {
                    if (mid == 0 || bucket >= percentsAccumulated[mid - 1] * 10) {
                        return variantIds[mid] to mid
                    }
                    right = mid - 1
                } else {
                    left = mid + 1
                }
            }
            
            // If not found, return last variant
            return variantIds.last() to percentsAccumulated.size - 1
        }
        
        /**
         * CRC32 hash function for consistent bucketing
         * Uses Kotlin's built-in implementation
         */
        private fun crc32Num(entityID: String, salt: String): UInt {
            val input = "$entityID$salt"
            var crc = 0xFFFFFFFFu
            
            for (byte in input.encodeToByteArray()) {
                crc = crc xor byte.toUByte().toUInt()
                for (k in 0 until 8) {
                    crc = if ((crc and 1u) != 0u) {
                        (crc shr 1) xor 0xEDB88320u
                    } else {
                        crc shr 1
                    }
                }
            }
            
            return crc xor 0xFFFFFFFFu
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
