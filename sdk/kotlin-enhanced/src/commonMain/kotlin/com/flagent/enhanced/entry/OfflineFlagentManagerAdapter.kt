package com.flagent.enhanced.entry

import com.flagent.client.models.EvalResult
import com.flagent.client.models.EvaluationEntity
import com.flagent.enhanced.evaluator.BatchEvaluationRequest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray

/**
 * Adapts offline backend ([OfflineFlagentManagerDelegate]) to [FlagentClient].
 * Converts [LocalEvaluationResult] to [EvalResult] and builds batch requests from entities × flags.
 */
class OfflineFlagentManagerAdapter(
    private val delegate: OfflineFlagentManagerDelegate
) : FlagentClient {

    override suspend fun initialize(forceRefresh: Boolean) {
        delegate.bootstrap(forceRefresh)
    }

    override suspend fun evaluate(
        flagKey: String?,
        flagID: Long?,
        entityID: String?,
        entityType: String?,
        entityContext: Map<String, Any>?,
        enableDebug: Boolean
    ): EvalResult {
        val eid = entityID ?: ""
        val result = delegate.evaluate(
            flagKey = flagKey,
            flagID = flagID,
            entityID = eid,
            entityType = entityType,
            entityContext = entityContext ?: emptyMap(),
            enableDebug = enableDebug
        )
        return result.toEvalResult()
    }

    override suspend fun evaluateBatch(
        flagKeys: List<String>?,
        flagIDs: List<Int>?,
        entities: List<EvaluationEntity>,
        enableDebug: Boolean
    ): List<EvalResult> {
        val requests = buildBatchRequests(flagKeys, flagIDs, entities, enableDebug)
        if (requests.isEmpty()) return emptyList()
        val results = delegate.evaluateBatch(requests)
        return results.map { it.toEvalResult() }
    }

    private fun buildBatchRequests(
        flagKeys: List<String>?,
        flagIDs: List<Int>?,
        entities: List<EvaluationEntity>,
        enableDebug: Boolean
    ): List<BatchEvaluationRequest> {
        val keys = flagKeys.orEmpty()
        val ids = flagIDs.orEmpty().map { it.toLong() }
        if ((keys.isEmpty() && ids.isEmpty()) || entities.isEmpty()) return emptyList()
        return entities.flatMap { entity ->
            val eid = entity.entityID ?: ""
            val ctx = entity.entityContext?.let { jsonObjectToMap(it) } ?: emptyMap()
            keys.map { flagKey ->
                BatchEvaluationRequest(
                    flagKey = flagKey,
                    flagID = null,
                    entityID = eid,
                    entityType = entity.entityType,
                    entityContext = ctx,
                    enableDebug = enableDebug
                )
            } + ids.map { flagID ->
                BatchEvaluationRequest(
                    flagKey = null,
                    flagID = flagID,
                    entityID = eid,
                    entityType = entity.entityType,
                    entityContext = ctx,
                    enableDebug = enableDebug
                )
            }
        }
    }
}

internal fun jsonObjectToMap(obj: JsonObject): Map<String, Any> = obj.mapValues { (_, v) ->
    when (v) {
        is JsonPrimitive -> v.content
        is JsonObject -> jsonObjectToMap(v)
        else -> v.toString()
    }
}
