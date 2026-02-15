package com.flagent.enhanced.entry

import com.flagent.client.models.EvaluationEntity
import com.flagent.enhanced.evaluator.BatchEvaluationRequest
import com.flagent.enhanced.model.LocalEvaluationResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class OfflineFlagentManagerAdapterTest {

    @Test
    fun `initialize calls delegate bootstrap`() = runBlocking {
        val delegate = mockk<OfflineFlagentManagerDelegate>()
        coEvery { delegate.bootstrap(any()) } returns Unit
        val client = OfflineFlagentManagerAdapter(delegate)
        client.initialize()
        coVerify { delegate.bootstrap(false) }
        client.initialize(forceRefresh = true)
        coVerify { delegate.bootstrap(true) }
    }

    @Test
    fun `evaluate converts LocalEvaluationResult to EvalResult`() = runBlocking {
        val delegate = mockk<OfflineFlagentManagerDelegate>()
        coEvery {
            delegate.evaluate(any(), any(), any(), any(), any(), any())
        } returns LocalEvaluationResult(
            flagID = 5L,
            flagKey = "my_flag",
            variantID = 10L,
            variantKey = "treatment",
            variantAttachment = null,
            segmentID = 1L,
            reason = "MATCH"
        )
        val client = OfflineFlagentManagerAdapter(delegate)
        val result = client.evaluate(flagKey = "my_flag", entityID = "user1")
        assertEquals(5L, result.flagID)
        assertEquals("my_flag", result.flagKey)
        assertEquals("treatment", result.variantKey)
    }

    @Test
    fun `isEnabled returns true when variantKey not null`() = runBlocking {
        val delegate = mockk<OfflineFlagentManagerDelegate>()
        coEvery { delegate.evaluate(any(), any(), any(), any(), any(), any()) } returns
            LocalEvaluationResult(flagID = 1L, flagKey = "f", variantID = 10L, variantKey = "on", variantAttachment = null, segmentID = null, reason = "MATCH")
        val client = OfflineFlagentManagerAdapter(delegate)
        assertTrue(client.isEnabled("f", "user1"))
    }

    @Test
    fun `isEnabled returns false when variantKey null`() = runBlocking {
        val delegate = mockk<OfflineFlagentManagerDelegate>()
        coEvery { delegate.evaluate(any(), any(), any(), any(), any(), any()) } returns
            LocalEvaluationResult(flagID = null, flagKey = "f", variantID = null, variantKey = null, variantAttachment = null, segmentID = null, reason = "FLAG_NOT_FOUND")
        val client = OfflineFlagentManagerAdapter(delegate)
        assertFalse(client.isEnabled("f", "user1"))
    }

    @Test
    fun `evaluateBatch builds requests and converts results`() = runBlocking {
        val delegate = mockk<OfflineFlagentManagerDelegate>()
        coEvery { delegate.evaluateBatch(any()) } returns listOf(
            LocalEvaluationResult(flagID = 1L, flagKey = "a", variantID = 10L, variantKey = "x", variantAttachment = null, segmentID = null, reason = "MATCH"),
            LocalEvaluationResult(flagID = 2L, flagKey = "b", variantID = 20L, variantKey = "y", variantAttachment = null, segmentID = null, reason = "MATCH")
        )
        val client = OfflineFlagentManagerAdapter(delegate)
        val entities = listOf(
            EvaluationEntity(entityID = "u1", entityType = "user")
        )
        val results = client.evaluateBatch(flagKeys = listOf("a", "b"), entities = entities)
        assertEquals(2, results.size)
        assertEquals("a", results[0].flagKey)
        assertEquals("b", results[1].flagKey)
        coVerify { delegate.evaluateBatch(match { it.size == 2 && it.all { r -> r.entityID == "u1" } }) }
    }
}
