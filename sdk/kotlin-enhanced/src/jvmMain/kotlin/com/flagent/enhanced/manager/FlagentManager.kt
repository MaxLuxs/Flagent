package com.flagent.enhanced.manager

import com.flagent.client.apis.EvaluationApi
import com.flagent.client.models.EvalContext
import io.ktor.client.statement.bodyAsText
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class FlagentManager(
    private val evaluationApi: EvaluationApi,
    private val config: FlagentConfig = FlagentConfig()
) {
    private val cache: EvaluationCache? = if (config.enableCache) {
        InMemoryEvaluationCache(config.cacheTtlMs)
    } else null

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    init {
        if (cache != null) {
            scope.launch {
                while (true) {
                    delay(config.cacheTtlMs)
                    cache.evictExpired()
                }
            }
        }
    }

    suspend fun evaluate(
        flagKey: String? = null,
        flagID: Long? = null,
        entityID: String? = null,
        entityType: String? = null,
        entityContext: Map<String, Any>? = null,
        enableDebug: Boolean = false
    ): EvalResult = withContext(Dispatchers.Default) {
        val cacheKey = CacheKey(flagKey, flagID, entityID, entityType)
        val cached = cache?.get(cacheKey)
        if (cached != null) return@withContext cached
        val jsonEntityContext: JsonObject? = entityContext?.let { ctx ->
            buildJsonObject {
                ctx.forEach { (key, value) ->
                    put(key, when (value) {
                        is String -> JsonPrimitive(value)
                        is Number -> JsonPrimitive(value)
                        is Boolean -> JsonPrimitive(value)
                        is kotlinx.serialization.json.JsonElement -> value
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
        if (!response.success) {
            val body = response.response.bodyAsText()
            throw IllegalStateException("Evaluation failed: ${response.status} - $body")
        }
        val result = response.body()
        cache?.put(cacheKey, result)
        result
    }

    suspend fun evaluateBatch(
        flagKeys: List<String>? = null,
        flagIDs: List<Int>? = null,
        entities: List<EvaluationEntity>,
        enableDebug: Boolean = false
    ): List<EvalResult> = withContext(Dispatchers.Default) {
        val request = EvaluationBatchRequest(
            entities = entities,
            flagKeys = flagKeys,
            flagIDs = flagIDs,
            enableDebug = enableDebug
        )
        val response = evaluationApi.postEvaluationBatch(request)
        if (!response.success) {
            val body = response.response.bodyAsText()
            throw IllegalStateException("Batch evaluation failed: ${response.status} - $body")
        }
        response.body().evaluationResults
    }

    suspend fun clearCache() {
        cache?.clear()
    }

    suspend fun evictExpired() {
        cache?.evictExpired()
    }
}
