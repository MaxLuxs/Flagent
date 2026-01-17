package com.flagent.enhanced.manager

import com.flagent.client.apis.EvaluationApi
import com.flagent.client.models.EvalContext
import com.flagent.client.models.EvalResult
import com.flagent.client.models.EvaluationBatchRequest
import com.flagent.client.models.EvaluationEntity
import com.flagent.enhanced.cache.CacheKey
import com.flagent.enhanced.cache.EvaluationCache
import com.flagent.enhanced.cache.InMemoryEvaluationCache
import com.flagent.enhanced.config.FlagentConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import kotlinx.serialization.json.JsonPrimitive

/**
 * Enhanced Flagent Manager with caching and convenient API.
 *
 * Provides high-level API for evaluating feature flags with automatic caching.
 * Results are cached with configurable TTL to reduce API calls and improve performance.
 *
 * @param evaluationApi The base evaluation API client
 * @param config Configuration for caching and behavior
 *
 * @example
 * ```
 * val manager = FlagentManager(evaluationApi, FlagentConfig(cacheTtlMs = 60000))
 * val result = manager.evaluate(flagKey = "new_feature", entityID = "user123")
 * ```
 */
class FlagentManager(
    private val evaluationApi: EvaluationApi,
    private val config: FlagentConfig = FlagentConfig()
) {
    private val cache: EvaluationCache? = if (config.enableCache) {
        InMemoryEvaluationCache(config.cacheTtlMs)
    } else {
        null
    }

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    init {
        // Periodic cache cleanup
        if (cache != null) {
            scope.launch {
                while (true) {
                    kotlinx.coroutines.delay(config.cacheTtlMs)
                    cache.evictExpired()
                }
            }
        }
    }

    /**
     * Evaluate a flag for a given entity context.
     *
     * Results are cached with TTL configured in [FlagentConfig]. Cached results are returned
     * if available and not expired.
     *
     * @param flagKey The flag key (optional if flagID is provided)
     * @param flagID The flag ID (optional if flagKey is provided)
     * @param entityID The entity ID to evaluate for
     * @param entityType The entity type (e.g., "user", "session")
     * @param entityContext Additional context for evaluation (e.g., region, tier)
     * @param enableDebug Enable debug mode for detailed evaluation logs
     * @return Evaluation result with assigned variant
     *
     * @throws Exception if API call fails or flag is not found
     */
    suspend fun evaluate(
        flagKey: String? = null,
        flagID: Long? = null,
        entityID: String? = null,
        entityType: String? = null,
        entityContext: Map<String, Any>? = null,
        enableDebug: Boolean = false
    ): EvalResult {
        return withContext(Dispatchers.Default) {
            val cacheKey = CacheKey(flagKey, flagID, entityID, entityType)

            // Try cache first
            val cached = cache?.get(cacheKey)
            if (cached != null) {
                return@withContext cached
            }

            // Evaluate via API
            val jsonEntityContext: JsonObject? = entityContext?.let { ctx ->
                buildJsonObject {
                    ctx.forEach { (key, value) ->
                        put(key, when (value) {
                            is String -> JsonPrimitive(value)
                            is Number -> JsonPrimitive(value)
                            is Boolean -> JsonPrimitive(value)
                            is JsonElement -> value
                            else -> JsonPrimitive(value.toString())
                        })
                    }
                }
            }
            val evalContext = EvalContext(
                flagKey = flagKey,
                flagID = flagID,
                entityID = entityID,
                entityType = entityType,
                entityContext = jsonEntityContext,
                enableDebug = enableDebug
            )

            val response = evaluationApi.postEvaluation(evalContext)
            val result = response.body()

            // Cache result
            cache?.put(cacheKey, result)

            result
        }
    }

    /**
     * Batch evaluate flags for multiple entities.
     *
     * Evaluates multiple flags for multiple entities in a single API call.
     * Batch results are not cached.
     *
     * @param flagKeys List of flag keys to evaluate (optional if flagIDs is provided)
     * @param flagIDs List of flag IDs to evaluate (optional if flagKeys is provided)
     * @param entities List of entities to evaluate for
     * @param enableDebug Enable debug mode for detailed evaluation logs
     * @return List of evaluation results
     *
     * @throws Exception if API call fails
     */
    suspend fun evaluateBatch(
        flagKeys: List<String>? = null,
        flagIDs: List<Int>? = null,
        entities: List<EvaluationEntity>,
        enableDebug: Boolean = false
    ): List<EvalResult> {
        return withContext(Dispatchers.Default) {
            val request = EvaluationBatchRequest(
                entities = entities,
                flagKeys = flagKeys,
                flagIDs = flagIDs,
                enableDebug = enableDebug
            )

            val response = evaluationApi.postEvaluationBatch(request)
            val batchResponse = response.body()

            batchResponse.evaluationResults
        }
    }

    /**
     * Clear all cached evaluation results.
     *
     * Removes all entries from the cache. This does not affect ongoing evaluations.
     */
    suspend fun clearCache() {
        cache?.clear()
    }

    /**
     * Evict expired entries from cache.
     *
     * Removes entries that have exceeded their TTL. This is called automatically
     * on a periodic basis, but can be called manually to force cleanup.
     */
    suspend fun evictExpired() {
        cache?.evictExpired()
    }
}