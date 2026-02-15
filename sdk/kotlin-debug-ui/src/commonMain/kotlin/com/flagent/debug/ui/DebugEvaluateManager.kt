package com.flagent.debug.ui

import com.flagent.client.models.EvalResult

/**
 * Internal interface for debug screen: evaluate returns [EvalResult] and cache actions.
 * Implemented by adapter over [com.flagent.enhanced.manager.FlagentManager] or
 * [com.flagent.enhanced.manager.OfflineFlagentManager].
 */
internal interface DebugEvaluateManager {
    suspend fun evaluate(
        flagKey: String?,
        flagID: Long?,
        entityID: String?,
        entityType: String?,
        entityContext: Map<String, Any>?,
        enableDebug: Boolean
    ): EvalResult

    suspend fun clearCache()
    suspend fun evictExpired()
}
