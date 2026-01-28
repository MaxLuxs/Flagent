package flagent.evaluator

import flagent.evaluator.FlagEvaluator.EvalContext

/**
 * ConstraintEvaluator - evaluates if entity context matches constraint
 * Shared across all platforms (JVM, JS, Native)
 */
class ConstraintEvaluator {
    
    /**
     * Evaluate if all constraints match the evaluation context
     * Returns true if all constraints match, false otherwise
     */
    fun evaluate(
        constraints: List<FlagEvaluator.EvaluableConstraint>,
        context: EvalContext
    ): Boolean {
        if (constraints.isEmpty()) {
            return true
        }
        
        if (context.entityContext == null) {
            return false
        }
        
        return constraints.all { constraint ->
            evaluateConstraint(constraint, context)
        }
    }
    
    /**
     * Evaluate single constraint against context
     */
    private fun evaluateConstraint(
        constraint: FlagEvaluator.EvaluableConstraint,
        context: EvalContext
    ): Boolean {
        val propertyValue = context.entityContext?.get(constraint.property) ?: return false
        val constraintValue = constraint.value
        
        return when (constraint.operator) {
            "EQ" -> equals(propertyValue, constraintValue)
            "NEQ" -> !equals(propertyValue, constraintValue)
            "LT" -> lessThan(propertyValue, constraintValue)
            "LTE" -> lessThanOrEqual(propertyValue, constraintValue)
            "GT" -> greaterThan(propertyValue, constraintValue)
            "GTE" -> greaterThanOrEqual(propertyValue, constraintValue)
            "EREG" -> matchesRegex(propertyValue, constraintValue)
            "NEREG" -> !matchesRegex(propertyValue, constraintValue)
            "IN" -> inList(propertyValue, constraintValue)
            "NOTIN" -> !inList(propertyValue, constraintValue)
            "CONTAINS" -> contains(propertyValue, constraintValue)
            "NOTCONTAINS" -> !contains(propertyValue, constraintValue)
            else -> false
        }
    }
    
    private fun equals(propertyValue: Any, constraintValue: String): Boolean {
        return propertyValue.toString() == constraintValue
    }
    
    private fun lessThan(propertyValue: Any, constraintValue: String): Boolean {
        return compareNumbers(propertyValue, constraintValue) { a, b -> a < b }
    }
    
    private fun lessThanOrEqual(propertyValue: Any, constraintValue: String): Boolean {
        return compareNumbers(propertyValue, constraintValue) { a, b -> a <= b }
    }
    
    private fun greaterThan(propertyValue: Any, constraintValue: String): Boolean {
        return compareNumbers(propertyValue, constraintValue) { a, b -> a > b }
    }
    
    private fun greaterThanOrEqual(propertyValue: Any, constraintValue: String): Boolean {
        return compareNumbers(propertyValue, constraintValue) { a, b -> a >= b }
    }
    
    private fun compareNumbers(
        propertyValue: Any,
        constraintValue: String,
        comparator: (Double, Double) -> Boolean
    ): Boolean {
        return try {
            val propNum = propertyValue.toString().toDouble()
            val constraintNum = constraintValue.toDouble()
            comparator(propNum, constraintNum)
        } catch (e: NumberFormatException) {
            false
        }
    }
    
    private fun matchesRegex(propertyValue: Any, constraintValue: String): Boolean {
        return try {
            val regex = constraintValue.toRegex()
            regex.matches(propertyValue.toString())
        } catch (e: Exception) {
            false
        }
    }
    
    private fun inList(propertyValue: Any, constraintValue: String): Boolean {
        val values = constraintValue.split(",").map { it.trim() }
        return values.contains(propertyValue.toString())
    }
    
    private fun contains(propertyValue: Any, constraintValue: String): Boolean {
        return propertyValue.toString().contains(constraintValue)
    }
}
