package com.flagent.enhanced.entry

import com.flagent.client.models.EvalResult
import com.flagent.enhanced.model.LocalEvaluationResult

/**
 * Converts local (offline) evaluation result to the shared [EvalResult] type.
 */
fun LocalEvaluationResult.toEvalResult(): EvalResult = EvalResult(
    flagID = flagID,
    flagKey = flagKey,
    flagSnapshotID = null,
    flagTags = null,
    segmentID = segmentID,
    variantID = variantID,
    variantKey = variantKey,
    variantAttachment = variantAttachment,
    evalContext = null,
    timestamp = null,
    evalDebugLog = null
)
