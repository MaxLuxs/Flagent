package flagent.evaluator

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ConstraintEvaluatorTest {
    
    private val evaluator = ConstraintEvaluator()
    
    @Test
    fun testEqualOperator() {
        val constraints = listOf(
            FlagEvaluator.EvaluableConstraint(
                id = 1,
                property = "tier",
                operator = "EQ",
                value = "premium"
            )
        )
        
        val context = FlagEvaluator.EvalContext(
            entityID = "user123",
            entityType = "user",
            entityContext = mapOf("tier" to "premium")
        )
        
        assertTrue(evaluator.evaluate(constraints, context))
    }
    
    @Test
    fun testNotEqualOperator() {
        val constraints = listOf(
            FlagEvaluator.EvaluableConstraint(
                id = 1,
                property = "tier",
                operator = "NEQ",
                value = "free"
            )
        )
        
        val context = FlagEvaluator.EvalContext(
            entityID = "user123",
            entityType = "user",
            entityContext = mapOf("tier" to "premium")
        )
        
        assertTrue(evaluator.evaluate(constraints, context))
    }
    
    @Test
    fun testLessThanOperator() {
        val constraints = listOf(
            FlagEvaluator.EvaluableConstraint(
                id = 1,
                property = "age",
                operator = "LT",
                value = "30"
            )
        )
        
        val context = FlagEvaluator.EvalContext(
            entityID = "user123",
            entityType = "user",
            entityContext = mapOf("age" to "25")
        )
        
        assertTrue(evaluator.evaluate(constraints, context))
    }
    
    @Test
    fun testGreaterThanOperator() {
        val constraints = listOf(
            FlagEvaluator.EvaluableConstraint(
                id = 1,
                property = "age",
                operator = "GT",
                value = "18"
            )
        )
        
        val context = FlagEvaluator.EvalContext(
            entityID = "user123",
            entityType = "user",
            entityContext = mapOf("age" to "25")
        )
        
        assertTrue(evaluator.evaluate(constraints, context))
    }
    
    @Test
    fun testInListOperator() {
        val constraints = listOf(
            FlagEvaluator.EvaluableConstraint(
                id = 1,
                property = "country",
                operator = "IN",
                value = "US,CA,UK"
            )
        )
        
        val context = FlagEvaluator.EvalContext(
            entityID = "user123",
            entityType = "user",
            entityContext = mapOf("country" to "US")
        )
        
        assertTrue(evaluator.evaluate(constraints, context))
    }
    
    @Test
    fun testNotInListOperator() {
        val constraints = listOf(
            FlagEvaluator.EvaluableConstraint(
                id = 1,
                property = "country",
                operator = "NOTIN",
                value = "US,CA,UK"
            )
        )
        
        val context = FlagEvaluator.EvalContext(
            entityID = "user123",
            entityType = "user",
            entityContext = mapOf("country" to "FR")
        )
        
        assertTrue(evaluator.evaluate(constraints, context))
    }
    
    @Test
    fun testRegexOperator() {
        val constraints = listOf(
            FlagEvaluator.EvaluableConstraint(
                id = 1,
                property = "email",
                operator = "EREG",
                value = ".*@example\\.com"
            )
        )
        
        val context = FlagEvaluator.EvalContext(
            entityID = "user123",
            entityType = "user",
            entityContext = mapOf("email" to "test@example.com")
        )
        
        assertTrue(evaluator.evaluate(constraints, context))
    }
    
    @Test
    fun testContainsOperator() {
        val constraints = listOf(
            FlagEvaluator.EvaluableConstraint(
                id = 1,
                property = "features",
                operator = "CONTAINS",
                value = "beta"
            )
        )
        
        val context = FlagEvaluator.EvalContext(
            entityID = "user123",
            entityType = "user",
            entityContext = mapOf("features" to "alpha,beta,gamma")
        )
        
        assertTrue(evaluator.evaluate(constraints, context))
    }
    
    @Test
    fun testMultipleConstraintsAllMatch() {
        val constraints = listOf(
            FlagEvaluator.EvaluableConstraint(
                id = 1,
                property = "tier",
                operator = "EQ",
                value = "premium"
            ),
            FlagEvaluator.EvaluableConstraint(
                id = 2,
                property = "age",
                operator = "GT",
                value = "18"
            )
        )
        
        val context = FlagEvaluator.EvalContext(
            entityID = "user123",
            entityType = "user",
            entityContext = mapOf("tier" to "premium", "age" to "25")
        )
        
        assertTrue(evaluator.evaluate(constraints, context))
    }
    
    @Test
    fun testMultipleConstraintsOneDoesNotMatch() {
        val constraints = listOf(
            FlagEvaluator.EvaluableConstraint(
                id = 1,
                property = "tier",
                operator = "EQ",
                value = "premium"
            ),
            FlagEvaluator.EvaluableConstraint(
                id = 2,
                property = "age",
                operator = "GT",
                value = "30"
            )
        )
        
        val context = FlagEvaluator.EvalContext(
            entityID = "user123",
            entityType = "user",
            entityContext = mapOf("tier" to "premium", "age" to "25")
        )
        
        assertFalse(evaluator.evaluate(constraints, context))
    }
    
    @Test
    fun testEmptyConstraintsReturnsTrue() {
        val context = FlagEvaluator.EvalContext(
            entityID = "user123",
            entityType = "user",
            entityContext = mapOf("tier" to "premium")
        )
        
        assertTrue(evaluator.evaluate(emptyList(), context))
    }
    
    @Test
    fun testMissingPropertyReturnsFalse() {
        val constraints = listOf(
            FlagEvaluator.EvaluableConstraint(
                id = 1,
                property = "missing_property",
                operator = "EQ",
                value = "value"
            )
        )
        
        val context = FlagEvaluator.EvalContext(
            entityID = "user123",
            entityType = "user",
            entityContext = mapOf("tier" to "premium")
        )
        
        assertFalse(evaluator.evaluate(constraints, context))
    }
}
