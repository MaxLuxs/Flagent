package com.flagent.debug.ui

/**
 * Display model for a flag in the Debug UI flags list.
 * Can be built from [com.flagent.client.models.Flag] (FlagApi) or
 * [com.flagent.enhanced.model.LocalFlag] (OfflineFlagentManager.getFlagsList()).
 */
data class FlagRow(
    val key: String,
    val id: Long,
    val enabled: Boolean,
    val variantKeys: List<String> = emptyList(),
    val description: String? = null,
    val tags: List<String> = emptyList()
)
