package com.flagent.enhanced.cache

import com.flagent.client.models.EvalResult
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class EvaluationCacheTest {
    @Test
    fun `test cache put and get`() = runBlocking {
        val cache = InMemoryEvaluationCache(ttlMs = 60000)
        val key = CacheKey(flagKey = "test_flag", flagID = null, entityID = "user1", entityType = "user")
        
        val result = createTestEvalResult()
        cache.put(key, result)
        
        val cached = cache.get(key)
        assertEquals(result.flagKey, cached?.flagKey)
        assertEquals(result.variantKey, cached?.variantKey)
    }
    
    @Test
    fun `test cache expiration`() = runBlocking {
        val cache = InMemoryEvaluationCache(ttlMs = 100) // 100ms TTL
        val key = CacheKey(flagKey = "test_flag", flagID = null, entityID = "user1", entityType = "user")
        
        val result = createTestEvalResult()
        cache.put(key, result)
        
        // Should be cached immediately
        assertNotNull(cache.get(key))
        
        // Wait for expiration
        kotlinx.coroutines.delay(150)
        
        // Should be expired
        assertNull(cache.get(key))
    }
    
    @Test
    fun `test cache clear`() = runBlocking {
        val cache = InMemoryEvaluationCache(ttlMs = 60000)
        val key = CacheKey(flagKey = "test_flag", flagID = null, entityID = "user1", entityType = "user")
        
        cache.put(key, createTestEvalResult())
        assertNotNull(cache.get(key))
        
        cache.clear()
        assertNull(cache.get(key))
    }
    
    @Test
    fun `test cache key string generation`() {
        val key1 = CacheKey(flagKey = "test", flagID = null, entityID = "user1", entityType = "user")
        val key2 = CacheKey(flagKey = "test", flagID = null, entityID = "user1", entityType = "user")
        
        assertEquals(key1.toKeyString(), key2.toKeyString())
    }
    
    @Test
    fun `test evict expired`() = runBlocking {
        val cache = InMemoryEvaluationCache(ttlMs = 100)
        val key1 = CacheKey(flagKey = "flag1", flagID = null, entityID = "user1", entityType = "user")
        val key2 = CacheKey(flagKey = "flag2", flagID = null, entityID = "user2", entityType = "user")
        
        cache.put(key1, createTestEvalResult("flag1"))
        cache.put(key2, createTestEvalResult("flag2"))
        
        kotlinx.coroutines.delay(150)
        
        cache.evictExpired()
        
        assertNull(cache.get(key1))
        assertNull(cache.get(key2))
    }
    
    private fun createTestEvalResult(flagKey: String = "test_flag"): EvalResult {
        return EvalResult(
            flagID = 1,
            flagKey = flagKey,
            variantKey = "control"
        )
    }
    
}