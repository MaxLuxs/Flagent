package com.flagent.debug.ui

import androidx.compose.runtime.Composable
import com.flagent.enhanced.manager.FlagentManager
import com.flagent.enhanced.manager.IOfflineFlagentManager

/**
 * Main entry point for Flagent Debug UI.
 * Provides a Composable debug screen for evaluation, cache actions, flags list, and local overrides.
 */
object FlagentDebugUI {
    /**
     * Debug screen with server-side [FlagentManager]: evaluation form, result display,
     * local overrides, cache actions, last evaluations. Optional [flagsProvider] for flags list.
     *
     * @param manager FlagentManager for evaluate and cache
     * @param flagsProvider Optional. When provided, loads and shows all flags (e.g. from FlagApi.findFlags()
     *        or OfflineFlagentManager.getFlagsList() mapped to [FlagRow])
     */
    @Composable
    fun DebugScreen(
        manager: FlagentManager,
        flagsProvider: (suspend () -> List<FlagRow>)? = null
    ) {
        FlagentDebugTheme {
            FlagentDebugScreen(
                evaluateManager = FlagentManagerAdapter(manager),
                flagsProvider = flagsProvider
            )
        }
    }

    /**
     * Debug screen with [IOfflineFlagentManager]: uses offline evaluate and shows flags from
     * [IOfflineFlagentManager.getFlagsList]. No separate [flagsProvider] needed.
     */
    @Composable
    fun DebugScreen(offlineManager: IOfflineFlagentManager) {
        FlagentDebugTheme {
            FlagentDebugScreen(
                evaluateManager = OfflineFlagentManagerAdapter(offlineManager),
                flagsProvider = { offlineManager.getFlagsListAsFlagRows() }
            )
        }
    }
}
