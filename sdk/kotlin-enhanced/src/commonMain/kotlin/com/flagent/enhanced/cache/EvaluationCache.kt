package com.flagent.enhanced.cache

import com.flagent.client.models.EvalResult
import com.flagent.enhanced.platform.currentTimeMs
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class CacheKey(
    val flagKey: String?,
    val flagID: Long?,
    val entityID: String?,
    val entityType: String?
) {
    fun toKeyString(): String = "${flagID ?: flagKey}_${entityID}_${entityType}"
}

private data class CachedEntry(
    val result: EvalResult,
    val timestamp: Long
) {
    fun isExpired(ttlMs: Long, currentTime: Long): Boolean =
        (currentTime - timestamp) > ttlMs
}

interface EvaluationCache {
    suspend fun get(key: CacheKey): EvalResult?
    suspend fun put(key: CacheKey, result: EvalResult)
    suspend fun clear()
    suspend fun evictExpired()
}

class InMemoryEvaluationCache(
    private val ttlMs: Long
) : EvaluationCache {
    private val cache = mutableMapOf<String, CachedEntry>()
    private val mutex = Mutex()

    override suspend fun get(key: CacheKey): EvalResult? = mutex.withLock {
        val entry = cache[key.toKeyString()]
        if (entry != null && !entry.isExpired(ttlMs, currentTimeMs())) {
            entry.result
        } else {
            cache.remove(key.toKeyString())
            null
        }
    }

    override suspend fun put(key: CacheKey, result: EvalResult) = mutex.withLock {
        cache[key.toKeyString()] = CachedEntry(result, currentTimeMs())
    }

    override suspend fun clear() = mutex.withLock { cache.clear() }

    override suspend fun evictExpired() = mutex.withLock {
        val currentTime = currentTimeMs()
        cache.entries.filter { it.value.isExpired(ttlMs, currentTime) }.map { it.key }.forEach { cache.remove(it) }
    }
}
