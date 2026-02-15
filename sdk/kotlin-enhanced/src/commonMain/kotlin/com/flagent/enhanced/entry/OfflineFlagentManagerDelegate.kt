package com.flagent.enhanced.entry

import com.flagent.enhanced.evaluator.BatchEvaluationRequest
import com.flagent.enhanced.model.LocalEvaluationResult

/**
 * Backend for offline client: evaluate and evaluateBatch using local snapshot.
 * Implemented per platform (JVM/Android/iOS) with [OfflineFlagentManager].
 */
interface OfflineFlagentManagerDelegate {

    suspend fun bootstrap(forceRefresh: Boolean = false) {}

    suspend fun evaluate(
        flagKey: String? = null,
        flagID: Long? = null,
        entityID: String,
        entityType: String? = null,
        entityContext: Map<String, Any> = emptyMap(),
        enableDebug: Boolean = false
    ): LocalEvaluationResult

    suspend fun evaluateBatch(requests: List<BatchEvaluationRequest>): List<LocalEvaluationResult>
}
