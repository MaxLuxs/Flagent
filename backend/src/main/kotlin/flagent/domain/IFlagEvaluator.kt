package flagent.domain

import flagent.domain.entity.Flag
import flagent.domain.usecase.EvaluateFlagUseCase
import flagent.domain.value.EvaluationContext

/**
 * Port for flag evaluation - domain layer defines interface, infrastructure implements.
 * Enables DIP: domain does not depend on shared evaluator or service adapter.
 */
interface IFlagEvaluator {
    fun evaluate(
        flag: Flag,
        context: EvaluationContext,
        enableDebug: Boolean = false
    ): EvaluateFlagUseCase.EvaluationResult
}
