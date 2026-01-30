package flagent.service.adapter

import flagent.domain.entity.*
import flagent.evaluator.FlagEvaluator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class EvaluatorAdapterTest {

    @Test
    fun toEvaluableFlagMapsFlagCorrectly() {
        val constraint = Constraint(id = 1, segmentId = 1, property = "region", operator = "EQ", value = "EU")
        val distribution = Distribution(id = 1, segmentId = 1, variantId = 10, percent = 100)
        val segment = Segment(
            id = 1,
            flagId = 1,
            rank = 1,
            rolloutPercent = 50,
            constraints = listOf(constraint),
            distributions = listOf(distribution)
        )
        val flag = Flag(
            id = 1,
            key = "test_flag",
            description = "Test",
            enabled = true,
            segments = listOf(segment)
        )

        val evaluable = EvaluatorAdapter.toEvaluableFlag(flag)

        assertEquals(flag.id, evaluable.id)
        assertEquals(flag.key, evaluable.key)
        assertEquals(flag.enabled, evaluable.enabled)
        assertEquals(1, evaluable.segments.size)
        assertEquals(1, evaluable.segments[0].id)
        assertEquals(50, evaluable.segments[0].rolloutPercent)
        assertEquals(1, evaluable.segments[0].constraints.size)
        assertEquals("region", evaluable.segments[0].constraints[0].property)
        assertEquals(1, evaluable.segments[0].distributions.size)
        assertEquals(10, evaluable.segments[0].distributions[0].variantId)
    }

    @Test
    fun toEvaluableSegmentMapsSegmentCorrectly() {
        val constraint = Constraint(id = 2, segmentId = 2, property = "tier", operator = "IN", value = "premium")
        val distribution = Distribution(id = 2, segmentId = 2, variantId = 20, percent = 50)
        val segment = Segment(
            id = 2,
            flagId = 1,
            rank = 2,
            rolloutPercent = 25,
            constraints = listOf(constraint),
            distributions = listOf(distribution)
        )

        val evaluable = EvaluatorAdapter.toEvaluableSegment(segment)

        assertEquals(segment.id, evaluable.id)
        assertEquals(segment.rank, evaluable.rank)
        assertEquals(segment.rolloutPercent, evaluable.rolloutPercent)
        assertEquals(1, evaluable.constraints.size)
        assertEquals("tier", evaluable.constraints[0].property)
        assertEquals("IN", evaluable.constraints[0].operator)
        assertEquals(1, evaluable.distributions.size)
        assertEquals(20, evaluable.distributions[0].variantId)
        assertEquals(50, evaluable.distributions[0].percent)
    }

    @Test
    fun toEvaluableConstraintMapsConstraintCorrectly() {
        val constraint = Constraint(id = 3, segmentId = 1, property = "version", operator = "GTE", value = "2.0")

        val evaluable = EvaluatorAdapter.toEvaluableConstraint(constraint)

        assertEquals(constraint.id, evaluable.id)
        assertEquals(constraint.property, evaluable.property)
        assertEquals(constraint.operator, evaluable.operator)
        assertEquals(constraint.value, evaluable.value)
    }

    @Test
    fun toEvaluableDistributionMapsDistributionCorrectly() {
        val distribution = Distribution(id = 4, segmentId = 1, variantId = 30, percent = 75)

        val evaluable = EvaluatorAdapter.toEvaluableDistribution(distribution)

        assertEquals(distribution.id, evaluable.id)
        assertEquals(distribution.variantId, evaluable.variantId)
        assertEquals(distribution.percent, evaluable.percent)
    }

    @Test
    fun createEvalContextMapsParametersCorrectly() {
        val ctx = EvaluatorAdapter.createEvalContext(
            entityID = "user-123",
            entityType = "user",
            entityContext = mapOf("region" to "EU", "tier" to "premium")
        )

        assertEquals("user-123", ctx.entityID)
        assertEquals("user", ctx.entityType)
        assertEquals(2, ctx.entityContext.size)
        assertEquals("EU", ctx.entityContext["region"])
        assertEquals("premium", ctx.entityContext["tier"])
    }

    @Test
    fun createEvalContextWithNullEntityType() {
        val ctx = EvaluatorAdapter.createEvalContext(
            entityID = "anon",
            entityType = null,
            entityContext = null
        )

        assertEquals("anon", ctx.entityID)
        assertEquals(null, ctx.entityType)
        assertEquals(emptyMap<String, String>(), ctx.entityContext)
    }

    @Test
    fun createEvalContextConvertsContextValuesToString() {
        val ctx = EvaluatorAdapter.createEvalContext(
            entityID = "e1",
            entityType = null,
            entityContext = mapOf("count" to 42, "active" to true)
        )

        assertNotNull(ctx.entityContext["count"])
        assertEquals("42", ctx.entityContext["count"])
        assertEquals("true", ctx.entityContext["active"])
    }
}
