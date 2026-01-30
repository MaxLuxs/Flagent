package flagent.domain.usecase

import flagent.domain.entity.Flag
import flagent.domain.value.EvaluationContext
import flagent.domain.value.EntityID

/**
 * EvaluateBatchUseCase - evaluates multiple flags in batch
 * Domain layer - no framework dependencies
 */
class EvaluateBatchUseCase(
    private val evaluateFlagUseCase: EvaluateFlagUseCase
) {
    /**
     * Batch evaluation result
     */
    data class BatchEvaluationResult(
        val flagID: Int,
        val flagKey: String,
        val result: EvaluateFlagUseCase.EvaluationResult
    )
    
    /**
     * Evaluate multiple flags for a single entity
     */
    fun invoke(
        flags: List<Flag>,
        entityID: EntityID,
        entityType: String?,
        entityContext: Map<String, Any>?,
        enableDebug: Boolean = false
    ): List<BatchEvaluationResult> {
        val context = EvaluationContext(
            entityID = entityID,
            entityType = entityType,
            entityContext = entityContext
        )
        
        return flags.map { flag ->
            val result = evaluateFlagUseCase.invoke(
                flag = flag,
                context = context,
                enableDebug = enableDebug
            )
            
            BatchEvaluationResult(
                flagID = flag.id,
                flagKey = flag.key,
                result = result
            )
        }
    }
}
