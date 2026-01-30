package flagent.service.adapter

import flagent.domain.IFlagEvaluator
import flagent.domain.entity.Flag
import flagent.domain.usecase.EvaluateFlagUseCase
import flagent.domain.value.EvaluationContext
import flagent.evaluator.FlagEvaluator

/**
 * Adapter: implements domain IFlagEvaluator using shared FlagEvaluator.
 * Application/infra layer - domain does not depend on this.
 */
class SharedFlagEvaluatorAdapter : IFlagEvaluator {
    private val flagEvaluator = FlagEvaluator()

    override fun evaluate(
        flag: Flag,
        context: EvaluationContext,
        enableDebug: Boolean
    ): EvaluateFlagUseCase.EvaluationResult {
        val evaluableFlag = EvaluatorAdapter.toEvaluableFlag(flag)
        val evalContext = FlagEvaluator.EvalContext(
            entityID = context.entityID.value,
            entityType = context.entityType,
            entityContext = context.entityContext?.mapValues { it.value.toString() } ?: emptyMap()
        )
        val sharedResult = flagEvaluator.evaluate(evaluableFlag, evalContext, enableDebug)
        return EvaluateFlagUseCase.EvaluationResult(
            variantID = sharedResult.variantID,
            segmentID = sharedResult.segmentID,
            debugLogs = sharedResult.debugLogs.map { log ->
                EvaluateFlagUseCase.SegmentDebugLog(
                    segmentID = log.segmentID,
                    message = log.message
                )
            }
        )
    }
}
