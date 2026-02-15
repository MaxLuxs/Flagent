package com.flagent.debug.ui

import com.flagent.client.models.EvalResult
import com.flagent.enhanced.manager.FlagentManager

internal class FlagentManagerAdapter(private val manager: FlagentManager) : DebugEvaluateManager {
    override suspend fun evaluate(
        flagKey: String?,
        flagID: Long?,
        entityID: String?,
        entityType: String?,
        entityContext: Map<String, Any>?,
        enableDebug: Boolean
    ): EvalResult = manager.evaluate(
        flagKey = flagKey,
        flagID = flagID,
        entityID = entityID,
        entityType = entityType,
        entityContext = entityContext,
        enableDebug = enableDebug
    )

    override suspend fun clearCache() {
        manager.clearCache()
    }

    override suspend fun evictExpired() {
        manager.evictExpired()
    }
}
