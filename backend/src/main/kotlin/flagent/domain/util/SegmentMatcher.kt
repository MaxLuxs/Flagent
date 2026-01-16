package flagent.domain.util

import flagent.domain.entity.Constraint
import flagent.domain.entity.Segment
import flagent.domain.usecase.ConstraintEvaluationUseCase
import flagent.domain.value.EvaluationContext

/**
 * SegmentMatcher - matches evaluation context against segment constraints
 * Domain layer - no framework dependencies
 */
class SegmentMatcher(
    private val constraintEvaluator: ConstraintEvaluationUseCase = ConstraintEvaluationUseCase()
) {
    /**
     * Check if evaluation context matches segment constraints
     * Returns true if all constraints match or if there are no constraints
     */
    fun matches(segment: Segment, context: EvaluationContext): Boolean {
        if (segment.constraints.isEmpty()) {
            return true
        }
        
        return constraintEvaluator.evaluate(segment.constraints, context)
    }
}
