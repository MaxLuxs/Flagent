package com.flagent.enhanced.evaluator

import com.flagent.enhanced.model.*
import com.flagent.enhanced.evaluator.toEvaluable
import flagent.evaluator.FlagEvaluator
import flagent.evaluator.RolloutAlgorithm
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Contract test: kotlin-enhanced LocalEvaluator must produce same results as shared FlagEvaluator.
 * When backend uses shared (via EvaluateFlagUseCase), backend and kotlin-enhanced will match.
 *
 * EVALUATION_SPEC: hashInput = salt + entityID, CRC32, bucket = crc32 % 1000
 */
class EvaluationContractTest {

    private val localEvaluator = LocalEvaluator()
    private val sharedEvaluator = FlagEvaluator()

    @Test
    fun `LocalEvaluator matches shared FlagEvaluator for same inputs`() {
        val variantA = LocalVariant(id = 10L, key = "variant_a")
        val variantB = LocalVariant(id = 11L, key = "variant_b")
        val distA = LocalDistribution(id = 100L, variantID = 10L, variantKey = "variant_a", percent = 50)
        val distB = LocalDistribution(id = 101L, variantID = 11L, variantKey = "variant_b", percent = 50)
        val segment = LocalSegment(
            id = 5L,
            rank = 1,
            rolloutPercent = 100,
            constraints = emptyList(),
            distributions = listOf(distA, distB)
        )
        val flag = LocalFlag(
            id = 1L,
            key = "contract_test_flag",
            enabled = true,
            segments = listOf(segment),
            variants = listOf(variantA, variantB)
        )
        val snapshot = FlagSnapshot(flags = mapOf(1L to flag))

        val entityID = "test_entity_123"
        val context = mapOf<String, Any>()

        val localResult = localEvaluator.evaluate(
            flagKey = "contract_test_flag",
            entityID = entityID,
            entityContext = context,
            snapshot = snapshot
        )

        val evaluableFlag = flag.toEvaluable()
        val evalContext = FlagEvaluator.EvalContext(
            entityID = entityID,
            entityContext = context.mapValues { it.value.toString() }
        )
        val sharedResult = sharedEvaluator.evaluate(evaluableFlag, evalContext)

        assertEquals(sharedResult.variantID?.toLong(), localResult.variantID)
        assertEquals(sharedResult.segmentID?.toLong(), localResult.segmentID)
    }

    @Test
    fun `evaluation is deterministic for fixed entityID`() {
        val variant = LocalVariant(id = 10L, key = "variant_a")
        val distribution = LocalDistribution(id = 100L, variantID = 10L, variantKey = "variant_a", percent = 100)
        val segment = LocalSegment(
            id = 5L,
            rank = 1,
            rolloutPercent = 100,
            constraints = emptyList(),
            distributions = listOf(distribution)
        )
        val flag = LocalFlag(
            id = 1L,
            key = "determinism_flag",
            enabled = true,
            segments = listOf(segment),
            variants = listOf(variant)
        )
        val snapshot = FlagSnapshot(flags = mapOf(1L to flag))

        val results = (1..10).map {
            localEvaluator.evaluate(
                flagKey = "determinism_flag",
                entityID = "test_entity_123",
                snapshot = snapshot
            )
        }

        val firstVariant = results.first().variantID
        assertTrue(results.all { it.variantID == firstVariant })
    }

    @Test
    fun `variant selection matches RolloutAlgorithm for entityID test_entity_123`() {
        val bucket = RolloutAlgorithm.bucket("test_entity_123", "1")
        val bucketInt = (bucket + 1u).toInt()
        val variantIds = listOf(10, 11)
        val percentsAccumulated = listOf(500, 1000)
        val (expectedVariantId, _) = RolloutAlgorithm.rollout(
            entityID = "test_entity_123",
            salt = "1",
            rolloutPercent = 100,
            variantIds = variantIds,
            percentsAccumulated = percentsAccumulated
        )

        val variantA = LocalVariant(id = 10L, key = "a")
        val variantB = LocalVariant(id = 11L, key = "b")
        val distA = LocalDistribution(id = 1L, variantID = 10L, variantKey = "a", percent = 50)
        val distB = LocalDistribution(id = 2L, variantID = 11L, variantKey = "b", percent = 50)
        val segment = LocalSegment(
            id = 5L,
            rank = 1,
            rolloutPercent = 100,
            constraints = emptyList(),
            distributions = listOf(distA, distB)
        )
        val flag = LocalFlag(
            id = 1L,
            key = "rollout_contract",
            enabled = true,
            segments = listOf(segment),
            variants = listOf(variantA, variantB)
        )
        val snapshot = FlagSnapshot(flags = mapOf(1L to flag))

        val result = localEvaluator.evaluate(
            flagKey = "rollout_contract",
            entityID = "test_entity_123",
            snapshot = snapshot
        )

        assertEquals("MATCH", result.reason)
        assertEquals(expectedVariantId?.toLong(), result.variantID)
    }
}
