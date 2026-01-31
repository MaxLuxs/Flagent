package com.flagent.enhanced.evaluator

import com.flagent.enhanced.model.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class LocalEvaluatorTest {
    
    private val evaluator = LocalEvaluator()

    @Test
    fun `evaluate returns FLAG_NOT_FOUND when flag not in snapshot`() {
        val snapshot = FlagSnapshot(flags = emptyMap())
        
        val result = evaluator.evaluate(
            flagKey = "nonexistent_flag",
            entityID = "user123",
            snapshot = snapshot
        )
        
        assertEquals("FLAG_NOT_FOUND", result.reason)
        assertNull(result.variantID)
    }

    @Test
    fun `evaluate returns FLAG_DISABLED when flag is disabled`() {
        val flag = LocalFlag(
            id = 1L,
            key = "test_flag",
            enabled = false,
            segments = emptyList(),
            variants = emptyList()
        )
        val snapshot = FlagSnapshot(flags = mapOf(1L to flag))
        
        val result = evaluator.evaluate(
            flagKey = "test_flag",
            entityID = "user123",
            snapshot = snapshot
        )
        
        assertEquals("FLAG_DISABLED", result.reason)
        assertNull(result.variantID)
    }

    @Test
    fun `evaluate returns NO_SEGMENTS when flag has no segments`() {
        val flag = LocalFlag(
            id = 1L,
            key = "test_flag",
            enabled = true,
            segments = emptyList(),
            variants = emptyList()
        )
        val snapshot = FlagSnapshot(flags = mapOf(1L to flag))
        
        val result = evaluator.evaluate(
            flagKey = "test_flag",
            entityID = "user123",
            snapshot = snapshot
        )
        
        assertEquals("NO_SEGMENTS", result.reason)
        assertNull(result.variantID)
    }

    @Test
    fun `evaluate matches simple segment without constraints`() {
        val variant = LocalVariant(id = 10L, key = "variant_a")
        val distribution = LocalDistribution(
            id = 100L,
            variantID = 10L,
            variantKey = "variant_a",
            percent = 100
        )
        val segment = LocalSegment(
            id = 5L,
            rank = 1,
            rolloutPercent = 100,
            constraints = emptyList(),
            distributions = listOf(distribution)
        )
        val flag = LocalFlag(
            id = 1L,
            key = "test_flag",
            enabled = true,
            segments = listOf(segment),
            variants = listOf(variant)
        )
        val snapshot = FlagSnapshot(flags = mapOf(1L to flag))
        
        val result = evaluator.evaluate(
            flagKey = "test_flag",
            entityID = "user123",
            snapshot = snapshot
        )
        
        assertEquals("MATCH", result.reason)
        assertEquals(10L, result.variantID)
        assertEquals("variant_a", result.variantKey)
        assertEquals(5L, result.segmentID)
    }

    @Test
    fun `evaluate matches segment with EQ constraint`() {
        val variant = LocalVariant(id = 10L, key = "premium_variant")
        val distribution = LocalDistribution(
            id = 100L,
            variantID = 10L,
            variantKey = "premium_variant",
            percent = 100
        )
        val constraint = LocalConstraint(
            id = 20L,
            property = "tier",
            operator = "EQ",
            value = "premium"
        )
        val segment = LocalSegment(
            id = 5L,
            rank = 1,
            rolloutPercent = 100,
            constraints = listOf(constraint),
            distributions = listOf(distribution)
        )
        val flag = LocalFlag(
            id = 1L,
            key = "test_flag",
            enabled = true,
            segments = listOf(segment),
            variants = listOf(variant)
        )
        val snapshot = FlagSnapshot(flags = mapOf(1L to flag))
        
        val result = evaluator.evaluate(
            flagKey = "test_flag",
            entityID = "user123",
            entityContext = mapOf("tier" to "premium"),
            snapshot = snapshot
        )
        
        assertEquals("MATCH", result.reason)
        assertEquals(10L, result.variantID)
    }

    @Test
    fun `evaluate does not match segment with failed EQ constraint`() {
        val variant = LocalVariant(id = 10L, key = "premium_variant")
        val distribution = LocalDistribution(
            id = 100L,
            variantID = 10L,
            variantKey = "premium_variant",
            percent = 100
        )
        val constraint = LocalConstraint(
            id = 20L,
            property = "tier",
            operator = "EQ",
            value = "premium"
        )
        val segment = LocalSegment(
            id = 5L,
            rank = 1,
            rolloutPercent = 100,
            constraints = listOf(constraint),
            distributions = listOf(distribution)
        )
        val flag = LocalFlag(
            id = 1L,
            key = "test_flag",
            enabled = true,
            segments = listOf(segment),
            variants = listOf(variant)
        )
        val snapshot = FlagSnapshot(flags = mapOf(1L to flag))
        
        val result = evaluator.evaluate(
            flagKey = "test_flag",
            entityID = "user123",
            entityContext = mapOf("tier" to "free"),
            snapshot = snapshot
        )
        
        assertEquals("NO_MATCH", result.reason)
        assertNull(result.variantID)
    }

    @Test
    fun `evaluate matches segment with IN constraint`() {
        val variant = LocalVariant(id = 10L, key = "regional_variant")
        val distribution = LocalDistribution(
            id = 100L,
            variantID = 10L,
            variantKey = "regional_variant",
            percent = 100
        )
        val constraint = LocalConstraint(
            id = 20L,
            property = "region",
            operator = "IN",
            value = "US,CA,MX"
        )
        val segment = LocalSegment(
            id = 5L,
            rank = 1,
            rolloutPercent = 100,
            constraints = listOf(constraint),
            distributions = listOf(distribution)
        )
        val flag = LocalFlag(
            id = 1L,
            key = "test_flag",
            enabled = true,
            segments = listOf(segment),
            variants = listOf(variant)
        )
        val snapshot = FlagSnapshot(flags = mapOf(1L to flag))
        
        val result = evaluator.evaluate(
            flagKey = "test_flag",
            entityID = "user123",
            entityContext = mapOf("region" to "CA"),
            snapshot = snapshot
        )
        
        assertEquals("MATCH", result.reason)
        assertEquals(10L, result.variantID)
    }

    @Test
    fun `evaluate respects rollout percentage`() {
        val variant = LocalVariant(id = 10L, key = "variant_a")
        val distribution = LocalDistribution(
            id = 100L,
            variantID = 10L,
            variantKey = "variant_a",
            percent = 100
        )
        val segment = LocalSegment(
            id = 5L,
            rank = 1,
            rolloutPercent = 1, // Only 1% rollout
            constraints = emptyList(),
            distributions = listOf(distribution)
        )
        val flag = LocalFlag(
            id = 1L,
            key = "test_flag",
            enabled = true,
            segments = listOf(segment),
            variants = listOf(variant)
        )
        val snapshot = FlagSnapshot(flags = mapOf(1L to flag))
        
        // Test with deterministic entity IDs
        var matchCount = 0
        for (i in 0..999) {
            val result = evaluator.evaluate(
                flagKey = "test_flag",
                entityID = "user$i",
                snapshot = snapshot
            )
            if (result.reason == "MATCH") {
                matchCount++
            }
        }
        
        // With 1% rollout, expect approximately 10 matches out of 1000 (1%)
        assertTrue(matchCount in 5..15, "Expected ~10 matches, got $matchCount")
    }

    @Test
    fun `evaluate with multiple segments respects rank order`() {
        val variantA = LocalVariant(id = 10L, key = "variant_a")
        val variantB = LocalVariant(id = 11L, key = "variant_b")
        
        val distA = LocalDistribution(id = 100L, variantID = 10L, variantKey = "variant_a", percent = 100)
        val distB = LocalDistribution(id = 101L, variantID = 11L, variantKey = "variant_b", percent = 100)
        
        val constraintPremium = LocalConstraint(id = 20L, property = "tier", operator = "EQ", value = "premium")
        val segmentPremium = LocalSegment(
            id = 5L,
            rank = 1, // Higher priority (evaluated first)
            rolloutPercent = 100,
            constraints = listOf(constraintPremium),
            distributions = listOf(distA)
        )
        
        val segmentDefault = LocalSegment(
            id = 6L,
            rank = 2, // Lower priority
            rolloutPercent = 100,
            constraints = emptyList(),
            distributions = listOf(distB)
        )
        
        val flag = LocalFlag(
            id = 1L,
            key = "test_flag",
            enabled = true,
            segments = listOf(segmentDefault, segmentPremium), // Intentionally out of order
            variants = listOf(variantA, variantB)
        )
        val snapshot = FlagSnapshot(flags = mapOf(1L to flag))
        
        // Premium user should match first segment (rank 1)
        val premiumResult = evaluator.evaluate(
            flagKey = "test_flag",
            entityID = "user123",
            entityContext = mapOf("tier" to "premium"),
            snapshot = snapshot
        )
        
        assertEquals("MATCH", premiumResult.reason)
        assertEquals("variant_a", premiumResult.variantKey)
        assertEquals(5L, premiumResult.segmentID)
        
        // Free user should match default segment (rank 2)
        val freeResult = evaluator.evaluate(
            flagKey = "test_flag",
            entityID = "user456",
            entityContext = mapOf("tier" to "free"),
            snapshot = snapshot
        )
        
        assertEquals("MATCH", freeResult.reason)
        assertEquals("variant_b", freeResult.variantKey)
        assertEquals(6L, freeResult.segmentID)
    }

    @Test
    fun `evaluate with variant attachment`() {
        val attachment = buildJsonObject {
            put("color", "#FF5733")
            put("size", "large")
        }
        val variant = LocalVariant(id = 10L, key = "variant_a", attachment = attachment)
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
            key = "test_flag",
            enabled = true,
            segments = listOf(segment),
            variants = listOf(variant)
        )
        val snapshot = FlagSnapshot(flags = mapOf(1L to flag))
        
        val result = evaluator.evaluate(
            flagKey = "test_flag",
            entityID = "user123",
            snapshot = snapshot
        )
        
        assertEquals("MATCH", result.reason)
        assertNotNull(result.variantAttachment)
        assertEquals("#FF5733", result.getAttachmentValue("color"))
        assertEquals("large", result.getAttachmentValue("size"))
    }

    @Test
    fun `evaluate with debug logs enabled`() {
        val variant = LocalVariant(id = 10L, key = "variant_a")
        val distribution = LocalDistribution(id = 100L, variantID = 10L, variantKey = "variant_a", percent = 100)
        val constraint = LocalConstraint(id = 20L, property = "tier", operator = "EQ", value = "premium")
        val segment = LocalSegment(
            id = 5L,
            rank = 1,
            rolloutPercent = 100,
            constraints = listOf(constraint),
            distributions = listOf(distribution)
        )
        val flag = LocalFlag(
            id = 1L,
            key = "test_flag",
            enabled = true,
            segments = listOf(segment),
            variants = listOf(variant)
        )
        val snapshot = FlagSnapshot(flags = mapOf(1L to flag))
        
        val result = evaluator.evaluate(
            flagKey = "test_flag",
            entityID = "user123",
            entityContext = mapOf("tier" to "premium"),
            snapshot = snapshot,
            enableDebug = true
        )
        
        assertEquals("MATCH", result.reason)
        assertTrue(result.debugLogs.isNotEmpty())
        assertTrue(result.debugLogs.any { it.contains("Segment") && it.contains("matched") })
    }

    @Test
    fun `evaluateBatch processes multiple requests`() {
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
            key = "test_flag",
            enabled = true,
            segments = listOf(segment),
            variants = listOf(variant)
        )
        val snapshot = FlagSnapshot(flags = mapOf(1L to flag))
        
        val requests = listOf(
            BatchEvaluationRequest(flagKey = "test_flag", entityID = "user1"),
            BatchEvaluationRequest(flagKey = "test_flag", entityID = "user2"),
            BatchEvaluationRequest(flagKey = "test_flag", entityID = "user3")
        )
        
        val results = evaluator.evaluateBatch(requests, snapshot)
        
        assertEquals(3, results.size)
        results.forEach { result ->
            assertEquals("MATCH", result.reason)
            assertEquals(10L, result.variantID)
        }
    }
}
