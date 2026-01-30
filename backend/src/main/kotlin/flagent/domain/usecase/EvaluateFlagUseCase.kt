package flagent.domain.usecase

import flagent.domain.IFlagEvaluator
import flagent.domain.entity.Flag
import flagent.domain.value.EvaluationContext

/**
 * EvaluateFlagUseCase - evaluates a flag and returns variant assignment.
 * Domain layer - delegates to IFlagEvaluator (implemented in application/infra).
 */
class EvaluateFlagUseCase(
    private val evaluator: IFlagEvaluator
) {
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
     * Evaluate flag with context.
     * Returns evaluation result with variant ID and segment ID.
     */
    fun invoke(
        flag: Flag,
        context: EvaluationContext,
        enableDebug: Boolean = false
    ): EvaluationResult = evaluator.evaluate(flag, context, enableDebug)
}
