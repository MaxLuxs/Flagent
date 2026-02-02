package flagent.service.adapter

import flagent.domain.entity.*
import flagent.domain.value.EntityID
import flagent.domain.value.EvaluationContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SharedFlagEvaluatorAdapterTest {

    private val adapter = SharedFlagEvaluatorAdapter()

    @Test
    fun evaluate_simpleFlagWithMatchingSegment_returnsVariantIdAndSegmentId() {
        val flag = Flag(
            id = 1,
            key = "adapter_test_flag",
            description = "Test",
            enabled = true,
            segments = listOf(
                Segment(
                    id = 1,
                    flagId = 1,
                    rank = 1,
                    rolloutPercent = 100,
                    constraints = emptyList(),
                    distributions = listOf(
                        Distribution(id = 1, segmentId = 1, variantId = 42, percent = 100)
                    )
                )
            ),
            variants = listOf(Variant(id = 42, flagId = 1, key = "variant_a"))
        )
        val context = EvaluationContext(entityID = EntityID("user123"))

        val result = adapter.evaluate(flag, context, enableDebug = false)

        assertNotNull(result.variantID)
        assertEquals(42, result.variantID)
        assertEquals(1, result.segmentID)
        assertTrue(result.debugLogs.isEmpty())
    }

    @Test
    fun evaluate_disabledFlag_returnsNullVariant() {
        val flag = Flag(
            id = 1,
            key = "disabled_flag",
            description = "",
            enabled = false,
            segments = emptyList()
        )
        val context = EvaluationContext(entityID = EntityID("user123"))

        val result = adapter.evaluate(flag, context, enableDebug = false)

        assertNull(result.variantID)
        assertNull(result.segmentID)
    }

    @Test
    fun evaluate_enableDebugTrue_returnsDebugLogs() {
        val flag = Flag(
            id = 1,
            key = "debug_flag",
            description = "",
            enabled = false,
            segments = emptyList()
        )
        val context = EvaluationContext(entityID = EntityID("user123"))

        val result = adapter.evaluate(flag, context, enableDebug = true)

        assertTrue(result.debugLogs.isNotEmpty())
        assertTrue(result.debugLogs.any { it.message.contains("not enabled") })
    }

    @Test
    fun evaluate_enableDebugFalse_returnsEmptyDebugLogs() {
        val flag = Flag(
            id = 1,
            key = "flag",
            description = "",
            enabled = false,
            segments = emptyList()
        )
        val context = EvaluationContext(entityID = EntityID("user123"))

        val result = adapter.evaluate(flag, context, enableDebug = false)

        assertTrue(result.debugLogs.isEmpty())
    }

    @Test
    fun evaluate_convertsEntityContextToStringValues() {
        val constraint = Constraint(
            id = 1,
            segmentId = 1,
            property = "region",
            operator = "EQ",
            value = "US"
        )
        val segment = Segment(
            id = 1,
            flagId = 1,
            rank = 1,
            rolloutPercent = 100,
            constraints = listOf(constraint),
            distributions = listOf(
                Distribution(id = 1, segmentId = 1, variantId = 1, percent = 100)
            )
        )
        val flag = Flag(
            id = 1,
            key = "context_flag",
            description = "",
            enabled = true,
            segments = listOf(segment),
            variants = listOf(Variant(id = 1, flagId = 1, key = "v1"))
        )
        val context = EvaluationContext(
            entityID = EntityID("user1"),
            entityType = "user",
            entityContext = mapOf("region" to "US")
        )

        val result = adapter.evaluate(flag, context, enableDebug = false)

        assertNotNull(result.variantID)
        assertEquals(1, result.variantID)
    }

    @Test
    fun evaluate_withEntityType_passesThrough() {
        val segment = Segment(
            id = 1,
            flagId = 1,
            rank = 1,
            rolloutPercent = 100,
            constraints = emptyList(),
            distributions = listOf(
                Distribution(id = 1, segmentId = 1, variantId = 1, percent = 100)
            )
        )
        val flag = Flag(
            id = 1,
            key = "entity_type_flag",
            description = "",
            enabled = true,
            segments = listOf(segment),
            variants = listOf(Variant(id = 1, flagId = 1, key = "v1"))
        )
        val context = EvaluationContext(
            entityID = EntityID("device_abc"),
            entityType = "device",
            entityContext = null
        )

        val result = adapter.evaluate(flag, context, enableDebug = false)

        assertNotNull(result.variantID)
    }
}
