package flagent.domain.usecase

import flagent.domain.entity.Flag
import flagent.domain.value.EvaluationContext
import flagent.evaluator.FlagEvaluator
import flagent.service.adapter.EvaluatorAdapter
import flagent.evaluator.FlagEvaluator.EvalContext

/**
 * EvaluateFlagUseCase - evaluates a flag and returns variant assignment
 * Domain layer - delegates to shared FlagEvaluator
 */
class EvaluateFlagUseCase {
    private val flagEvaluator = FlagEvaluator()
    
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
     * 
     * Now delegates to shared FlagEvaluator for consistent evaluation logic
     */
    fun invoke(
        flag: Flag,
        context: EvaluationContext,
        enableDebug: Boolean = false
    ): EvaluationResult {
        // Convert domain Flag to EvaluableFlag
        val evaluableFlag = EvaluatorAdapter.toEvaluableFlag(flag)
        
        // Convert EvaluationContext to EvalContext
        val evalContext = FlagEvaluator.EvalContext(
            entityID = context.entityID.value,
            entityType = context.entityType,
            entityContext = context.entityContext?.mapValues { it.value.toString() } ?: emptyMap()
        )
        
        // Delegate to shared evaluator
        val sharedResult = flagEvaluator.evaluate(evaluableFlag, evalContext, enableDebug)
        
        // Convert back to domain EvaluationResult
        return EvaluationResult(
            variantID = sharedResult.variantID,
            segmentID = sharedResult.segmentID,
            debugLogs = sharedResult.debugLogs.map { log ->
                SegmentDebugLog(
                    segmentID = log.segmentID,
                    message = log.message
                )
            }
        )
    }
}
