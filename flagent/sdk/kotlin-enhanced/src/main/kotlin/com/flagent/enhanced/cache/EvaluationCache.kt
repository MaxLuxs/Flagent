package com.flagent.enhanced.cache

import com.flagent.client.models.EvalResult
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap

/**
 * Cache key for evaluation results.
 * 
 * Used to uniquely identify cached evaluation results based on flag and entity.
 * 
 * @param flagKey The flag key (optional if flagID is provided)
 * @param flagID The flag ID (optional if flagKey is provided)
 * @param entityID The entity ID
 * @param entityType The entity type
 */
data class CacheKey(
    val flagKey: String?,
    val flagID: Long?,
    val entityID: String?,
    val entityType: String?
) {
    fun toKeyString(): String {
        return "${flagID ?: flagKey}_${entityID}_${entityType}"
    }
}

/**
 * Cached evaluation entry with expiration
 */
private data class CachedEntry(
    val result: EvalResult,
    val timestamp: Long
) {
    fun isExpired(ttlMs: Long, currentTime: Long): Boolean {
        return (currentTime - timestamp) > ttlMs
    }
}

/**
 * Cache interface for evaluation results.
 * 
 * Provides thread-safe caching of evaluation results with TTL-based expiration.
 */
interface EvaluationCache {
    /**
     * Get cached evaluation result.
     * 
     * @param key The cache key
     * @return Cached result if available and not expired, null otherwise
     */
    suspend fun get(key: CacheKey): EvalResult?
    
    /**
     * Put evaluation result in cache.
     * 
     * @param key The cache key
     * @param result The evaluation result to cache
     */
    suspend fun put(key: CacheKey, result: EvalResult)
    
    /**
     * Clear all cached entries.
     */
    suspend fun clear()
    
    /**
     * Remove expired entries from cache.
     */
    suspend fun evictExpired()
}

/**
 * Thread-safe in-memory cache implementation.
 * 
 * Uses ConcurrentHashMap for storage and Mutex for thread-safe operations.
 * Entries are automatically expired based on TTL.
 * 
 * @param ttlMs Time-to-live for cached entries in milliseconds
 */
class InMemoryEvaluationCache(
    private val ttlMs: Long
) : EvaluationCache {
    private val cache = ConcurrentHashMap<String, CachedEntry>()
    private val mutex = Mutex()
    
    override suspend fun get(key: CacheKey): EvalResult? {
        return mutex.withLock {
            val entry = cache[key.toKeyString()]
            if (entry != null && !entry.isExpired(ttlMs, System.currentTimeMillis())) {
                entry.result
            } else {
                cache.remove(key.toKeyString())
                null
            }
        }
    }
    
    override suspend fun put(key: CacheKey, result: EvalResult) {
        mutex.withLock {
            cache[key.toKeyString()] = CachedEntry(result, System.currentTimeMillis())
        }
    }
    
    override suspend fun clear() {
        mutex.withLock {
            cache.clear()
        }
    }
    
    override suspend fun evictExpired() {
        mutex.withLock {
            val currentTime = System.currentTimeMillis()
            val keysToRemove = cache.entries.filter { it.value.isExpired(ttlMs, currentTime) }.map { it.key }
            keysToRemove.forEach { cache.remove(it) }
        }
    }
}