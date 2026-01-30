package io.ktor.flagent

import flagent.api.model.EvalContextResponse
import flagent.api.model.EvaluationRequest
import flagent.api.model.EvaluationResponse
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FlagentCacheTest {

    private fun sampleResponse(flagID: Int = 1, flagKey: String = "test") = EvaluationResponse(
        flagID = flagID,
        flagKey = flagKey,
        flagSnapshotID = 1,
        flagTags = emptyList(),
        segmentID = null,
        variantID = null,
        variantKey = "control",
        variantAttachment = null,
        evalContext = EvalContextResponse(entityID = "e1", entityType = null, entityContext = null),
        evalDebugLog = null,
        timestamp = System.currentTimeMillis()
    )

    @Test
    fun `get returns null when empty`() = runTest {
        val cache = FlagentCache(ttlMs = 60_000)
        val result = cache.get("key1")
        assertNull(result)
    }

    @Test
    fun `put and get return same value`() = runTest {
        val cache = FlagentCache(ttlMs = 60_000)
        val response = sampleResponse()
        cache.put("key1", response)
        val result = cache.get("key1")
        assertEquals(response.flagID, result?.flagID)
        assertEquals(response.flagKey, result?.flagKey)
    }

    @Test
    fun `clear removes all entries`() = runTest {
        val cache = FlagentCache(ttlMs = 60_000)
        cache.put("key1", sampleResponse())
        cache.clear()
        assertNull(cache.get("key1"))
    }

    @Test
    fun `generateKey uses flagID entityID entityType`() {
        val cache = FlagentCache()
        val request = EvaluationRequest(entityID = "e1", entityType = "user", flagID = 10, flagKey = "f1")
        val key = cache.generateKey(request)
        assertTrue(key.contains("10") || key.contains("f1"))
        assertTrue(key.contains("e1"))
        assertTrue(key.contains("user"))
    }

    @Test
    fun `generateKey with flagKey only`() {
        val cache = FlagentCache()
        val request = EvaluationRequest(flagKey = "my_flag", entityID = "u1")
        val key = cache.generateKey(request)
        assertTrue(key.contains("my_flag"))
        assertTrue(key.contains("u1"))
    }
}
