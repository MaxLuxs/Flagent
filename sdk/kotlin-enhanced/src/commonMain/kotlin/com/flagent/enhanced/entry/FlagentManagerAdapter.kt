package com.flagent.enhanced.entry

import com.flagent.client.models.EvalResult
import com.flagent.client.models.EvaluationEntity
import com.flagent.enhanced.manager.FlagentManager

/**
 * Adapts [FlagentManager] (server-side) to [FlagentClient].
 */
class FlagentManagerAdapter(
    private val manager: FlagentManager
) : FlagentClient {

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

    override suspend fun evaluateBatch(
        flagKeys: List<String>?,
        flagIDs: List<Int>?,
        entities: List<EvaluationEntity>,
        enableDebug: Boolean
    ): List<EvalResult> = manager.evaluateBatch(
        flagKeys = flagKeys,
        flagIDs = flagIDs,
        entities = entities,
        enableDebug = enableDebug
    )
}
