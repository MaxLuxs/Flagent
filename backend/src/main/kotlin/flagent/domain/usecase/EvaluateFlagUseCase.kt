package flagent.domain.usecase

import flagent.domain.IFlagEvaluator
import flagent.domain.entity.Flag
import flagent.domain.repository.IFlagRepository
import flagent.domain.value.EvaluationContext

/**
 * EvaluateFlagUseCase - evaluates a flag and returns variant assignment.
 * Domain layer - delegates to IFlagEvaluator (implemented in application/infra).
 * If flag has dependsOn, all dependencies must evaluate to a variant (enabled) for this flag to be evaluated.
 */
class EvaluateFlagUseCase(
    private val evaluator: IFlagEvaluator,
    private val flagRepository: IFlagRepository? = null
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
     * If flag.dependsOn is non-empty, each dependency is evaluated first; if any dependency returns no variant, this flag returns no variant.
     */
    suspend fun invoke(
        flag: Flag,
        context: EvaluationContext,
        enableDebug: Boolean = false
    ): EvaluationResult {
        if (flag.dependsOn.isEmpty()) {
            return evaluator.evaluate(flag, context, enableDebug)
        }
        val repo = flagRepository ?: return evaluator.evaluate(flag, context, enableDebug)
        for (depKey in flag.dependsOn) {
            val depFlag = repo.findByKey(depKey)
            if (depFlag == null) {
                return EvaluationResult(
                    variantID = null,
                    segmentID = null,
                    debugLogs = listOf(SegmentDebugLog(0, "dependency \"$depKey\" not found"))
                )
            }
            val depResult = invoke(depFlag, context, enableDebug)
            if (depResult.variantID == null) {
                return EvaluationResult(
                    variantID = null,
                    segmentID = null,
                    debugLogs = listOf(SegmentDebugLog(0, "dependency \"$depKey\" is disabled or has no variant"))
                )
            }
        }
        return evaluator.evaluate(flag, context, enableDebug)
    }
}
