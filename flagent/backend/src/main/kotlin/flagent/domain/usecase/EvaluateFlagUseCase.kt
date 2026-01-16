package flagent.domain.usecase

import flagent.domain.entity.Flag
import flagent.domain.util.SegmentMatcher
import flagent.domain.util.VariantSelector
import flagent.domain.value.EvaluationContext
import java.util.*

/**
 * EvaluateFlagUseCase - evaluates a flag and returns variant assignment
 * Domain layer - no framework dependencies
 */
class EvaluateFlagUseCase(
    private val segmentMatcher: SegmentMatcher = SegmentMatcher(),
    private val variantSelector: VariantSelector = VariantSelector()
) {
    private val random = Random()
    
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
     * Evaluate flag with context
     * Returns evaluation result with variant ID and segment ID
     */
    fun invoke(
        flag: Flag,
        context: EvaluationContext,
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
        
        // Prepare flag for evaluation
        val flagEvaluation = flag.prepareEvaluation()
        
        // Evaluate segments in order (by rank)
        val sortedSegments = flag.segments.sortedBy { it.rank }
        val debugLogs = mutableListOf<SegmentDebugLog>()
        var variantID: Int? = null
        var segmentID: Int? = null
        
        for (segment in sortedSegments) {
            segmentID = segment.id
            
            // Check if segment matches constraints
            val matches = segmentMatcher.matches(segment, context)
            
            if (!matches) {
                if (enableDebug) {
                    debugLogs.add(
                        SegmentDebugLog(
                            segmentID = segment.id,
                            message = "segment_id ${segment.id} did not match constraints"
                        )
                    )
                }
                // Continue to next segment
                continue
            }
            
            // Select variant based on distribution
            val selectedVariantID = variantSelector.selectVariant(
                segment = segment,
                entityID = context.entityID.value,
                flagID = flag.id
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
                // Found matching segment, stop evaluation
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
                // Continue to next segment
            }
        }
        
        return EvaluationResult(
            variantID = variantID,
            segmentID = segmentID,
            debugLogs = debugLogs
        )
    }
}
