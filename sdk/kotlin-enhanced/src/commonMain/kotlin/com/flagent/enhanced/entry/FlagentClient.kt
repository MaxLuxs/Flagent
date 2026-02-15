package com.flagent.enhanced.entry

import com.flagent.client.models.EvalResult
import com.flagent.client.models.EvaluationEntity

/**
 * Unified client interface for flag evaluation.
 * Implemented by server-side [FlagentManager] and offline [OfflineFlagentManager] via adapters.
 */
interface FlagentClient {

    /**
     * For offline mode: loads snapshot (bootstrap). No-op for server mode.
     * Call once before first [evaluate] when using [FlagentMode.OFFLINE].
     */
    suspend fun initialize(forceRefresh: Boolean = false) {}

    /**
     * Evaluates a single flag for the given entity.
     * @param entityID Required for offline mode; optional for server (context identifier).
     */
    suspend fun evaluate(
        flagKey: String? = null,
        flagID: Long? = null,
        entityID: String? = null,
        entityType: String? = null,
        entityContext: Map<String, Any>? = null,
        enableDebug: Boolean = false
    ): EvalResult

    /**
     * Returns true if the flag evaluates to an enabled variant (variantKey != null).
     */
    suspend fun isEnabled(
        flagKey: String,
        entityID: String? = null,
        entityType: String? = null,
        entityContext: Map<String, Any>? = null
    ): Boolean =
        evaluate(
            flagKey = flagKey,
            entityID = entityID,
            entityType = entityType,
            entityContext = entityContext,
            enableDebug = false
        ).variantKey != null

    /**
     * Batch evaluation: multiple flags × entities.
     * Result order matches server contract (entities × flags).
     */
    suspend fun evaluateBatch(
        flagKeys: List<String>? = null,
        flagIDs: List<Int>? = null,
        entities: List<EvaluationEntity>,
        enableDebug: Boolean = false
    ): List<EvalResult>
}
