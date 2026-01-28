package flagent.evaluator

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class FlagEvaluatorTest {
    
    private val evaluator = FlagEvaluator()
    
    @Test
    fun testDisabledFlagReturnsNull() {
        val flag = FlagEvaluator.EvaluableFlag(
            id = 1,
            key = "test_flag",
            enabled = false,
            segments = emptyList()
        )
        
        val context = FlagEvaluator.EvalContext(
            entityID = "user123",
            entityType = "user",
            entityContext = mapOf("tier" to "premium")
        )
        
        val result = evaluator.evaluate(flag, context)
        
        assertNull(result.variantID)
        assertNull(result.segmentID)
    }
    
    @Test
    fun testFlagWithNoSegmentsReturnsNull() {
        val flag = FlagEvaluator.EvaluableFlag(
            id = 1,
            key = "test_flag",
            enabled = true,
            segments = emptyList()
        )
        
        val context = FlagEvaluator.EvalContext(
            entityID = "user123",
            entityType = "user",
            entityContext = mapOf("tier" to "premium")
        )
        
        val result = evaluator.evaluate(flag, context)
        
        assertNull(result.variantID)
        assertNull(result.segmentID)
    }
    
    @Test
    fun testSimpleEvaluation() {
        val flag = FlagEvaluator.EvaluableFlag(
            id = 1,
            key = "test_flag",
            enabled = true,
            segments = listOf(
                FlagEvaluator.EvaluableSegment(
                    id = 1,
                    rank = 0,
                    rolloutPercent = 100,
                    constraints = emptyList(),
                    distributions = listOf(
                        FlagEvaluator.EvaluableDistribution(
                            id = 1,
                            variantId = 10,
                            percent = 100
                        )
                    )
                )
            )
        )
        
        val context = FlagEvaluator.EvalContext(
            entityID = "user123",
            entityType = "user",
            entityContext = mapOf("tier" to "premium")
        )
        
        val result = evaluator.evaluate(flag, context)
        
        assertNotNull(result.variantID)
        assertEquals(10, result.variantID)
        assertEquals(1, result.segmentID)
    }
    
    @Test
    fun testConstraintEvaluation() {
        val flag = FlagEvaluator.EvaluableFlag(
            id = 1,
            key = "test_flag",
            enabled = true,
            segments = listOf(
                FlagEvaluator.EvaluableSegment(
                    id = 1,
                    rank = 0,
                    rolloutPercent = 100,
                    constraints = listOf(
                        FlagEvaluator.EvaluableConstraint(
                            id = 1,
                            property = "tier",
                            operator = "EQ",
                            value = "premium"
                        )
                    ),
                    distributions = listOf(
                        FlagEvaluator.EvaluableDistribution(
                            id = 1,
                            variantId = 10,
                            percent = 100
                        )
                    )
                )
            )
        )
        
        val context = FlagEvaluator.EvalContext(
            entityID = "user123",
            entityType = "user",
            entityContext = mapOf("tier" to "premium")
        )
        
        val result = evaluator.evaluate(flag, context)
        
        assertNotNull(result.variantID)
        assertEquals(10, result.variantID)
    }
    
    @Test
    fun testConstraintDoesNotMatch() {
        val flag = FlagEvaluator.EvaluableFlag(
            id = 1,
            key = "test_flag",
            enabled = true,
            segments = listOf(
                FlagEvaluator.EvaluableSegment(
                    id = 1,
                    rank = 0,
                    rolloutPercent = 100,
                    constraints = listOf(
                        FlagEvaluator.EvaluableConstraint(
                            id = 1,
                            property = "tier",
                            operator = "EQ",
                            value = "enterprise"
                        )
                    ),
                    distributions = listOf(
                        FlagEvaluator.EvaluableDistribution(
                            id = 1,
                            variantId = 10,
                            percent = 100
                        )
                    )
                )
            )
        )
        
        val context = FlagEvaluator.EvalContext(
            entityID = "user123",
            entityType = "user",
            entityContext = mapOf("tier" to "premium")
        )
        
        val result = evaluator.evaluate(flag, context)
        
        assertNull(result.variantID)
    }
    
    @Test
    fun testMultipleSegmentsWithRanking() {
        val flag = FlagEvaluator.EvaluableFlag(
            id = 1,
            key = "test_flag",
            enabled = true,
            segments = listOf(
                FlagEvaluator.EvaluableSegment(
                    id = 2,
                    rank = 1,
                    rolloutPercent = 100,
                    constraints = listOf(
                        FlagEvaluator.EvaluableConstraint(
                            id = 2,
                            property = "tier",
                            operator = "EQ",
                            value = "free"
                        )
                    ),
                    distributions = listOf(
                        FlagEvaluator.EvaluableDistribution(
                            id = 2,
                            variantId = 20,
                            percent = 100
                        )
                    )
                ),
                FlagEvaluator.EvaluableSegment(
                    id = 1,
                    rank = 0,
                    rolloutPercent = 100,
                    constraints = emptyList(),
                    distributions = listOf(
                        FlagEvaluator.EvaluableDistribution(
                            id = 1,
                            variantId = 10,
                            percent = 100
                        )
                    )
                )
            )
        )
        
        val context = FlagEvaluator.EvalContext(
            entityID = "user123",
            entityType = "user",
            entityContext = mapOf("tier" to "free")
        )
        
        val result = evaluator.evaluate(flag, context)
        
        // Should match segment with rank 0 first (no constraints)
        assertNotNull(result.variantID)
        assertEquals(10, result.variantID)
        assertEquals(1, result.segmentID)
    }
    
    @Test
    fun testDebugLogsEnabled() {
        val flag = FlagEvaluator.EvaluableFlag(
            id = 1,
            key = "test_flag",
            enabled = true,
            segments = listOf(
                FlagEvaluator.EvaluableSegment(
                    id = 1,
                    rank = 0,
                    rolloutPercent = 100,
                    constraints = emptyList(),
                    distributions = listOf(
                        FlagEvaluator.EvaluableDistribution(
                            id = 1,
                            variantId = 10,
                            percent = 100
                        )
                    )
                )
            )
        )
        
        val context = FlagEvaluator.EvalContext(
            entityID = "user123",
            entityType = "user",
            entityContext = mapOf("tier" to "premium")
        )
        
        val result = evaluator.evaluate(flag, context, enableDebug = true)
        
        assertNotNull(result.variantID)
        assertEquals(1, result.debugLogs.size)
        assertEquals("matched all constraints. rollout yes. variantID: 10", result.debugLogs[0].message)
    }
}
