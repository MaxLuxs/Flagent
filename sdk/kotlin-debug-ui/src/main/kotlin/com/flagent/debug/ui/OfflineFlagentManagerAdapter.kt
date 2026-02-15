package com.flagent.debug.ui

import com.flagent.client.models.EvalDebugLog
import com.flagent.client.models.EvalResult
import com.flagent.enhanced.manager.OfflineFlagentManager
import com.flagent.enhanced.model.LocalVariant

internal class OfflineFlagentManagerAdapter(private val offline: OfflineFlagentManager) : DebugEvaluateManager {

    override suspend fun evaluate(
        flagKey: String?,
        flagID: Long?,
        entityID: String?,
        entityType: String?,
        entityContext: Map<String, Any>?,
        enableDebug: Boolean
    ): EvalResult {
        val r = offline.evaluate(
            flagKey = flagKey,
            flagID = flagID,
            entityID = entityID ?: "default",
            entityType = entityType,
            entityContext = entityContext?.orEmpty() ?: emptyMap(),
            enableDebug = enableDebug
        )
        return EvalResult(
            flagID = r.flagID,
            flagKey = r.flagKey,
            variantID = r.variantID,
            variantKey = r.variantKey,
            variantAttachment = r.variantAttachment,
            segmentID = r.segmentID,
            evalDebugLog = if (r.debugLogs.isNotEmpty()) EvalDebugLog(msg = r.debugLogs.joinToString("\n"), segmentDebugLogs = null) else null
        )
    }

    override suspend fun clearCache() {
        offline.clearCache()
    }

    override suspend fun evictExpired() {
        // Offline manager has no TTL-based eval cache; no-op
    }
}

internal fun OfflineFlagentManager.getFlagsListAsFlagRows(): List<FlagRow> =
    getFlagsList().map { f ->
        FlagRow(
            key = f.key,
            id = f.id,
            enabled = f.enabled,
            variantKeys = f.variants.map(LocalVariant::key),
            description = f.description
        )
    }
