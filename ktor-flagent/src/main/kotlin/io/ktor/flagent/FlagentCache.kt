package io.ktor.flagent

import flagent.api.model.EvaluationRequest
import flagent.api.model.EvaluationResponse
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration
import kotlin.time.TimeMark
import kotlin.time.TimeSource

/**
 * FlagentCache - in-memory cache for evaluation results
 */
class FlagentCache(
    private val ttlMs: Long = 60000 // 1 minute default
) {
    private val cache = ConcurrentHashMap<String, CachedEvaluation>()
    private val mutex = Mutex()
    private val timeSource = TimeSource.Monotonic
    
    /**
     * Get cached evaluation result
     */
    suspend fun get(key: String): EvaluationResponse? {
        return mutex.withLock {
            val cached = cache[key]
            if (cached != null && !cached.isExpired()) {
                cached.value
            } else {
                cache.remove(key)
                null
            }
        }
    }
    
    /**
     * Put evaluation result in cache
     */
    suspend fun put(key: String, value: EvaluationResponse) {
        mutex.withLock {
            cache[key] = CachedEvaluation(value, timeSource.markNow())
        }
    }
    
    /**
     * Clear cache
     */
    suspend fun clear() {
        mutex.withLock {
            cache.clear()
        }
    }
    
    /**
     * Cached evaluation entry
     */
    private inner class CachedEvaluation(
        val value: EvaluationResponse,
        val timestamp: TimeMark
    ) {
        fun isExpired(): Boolean {
            return timestamp.elapsedNow().inWholeMilliseconds > ttlMs
        }
    }
    
    /**
     * Generate cache key from evaluation request
     */
    fun generateKey(request: EvaluationRequest): String {
        return "${request.flagID ?: request.flagKey ?: ""}_${request.entityID ?: ""}_${request.entityType ?: ""}"
    }
}
