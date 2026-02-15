package com.flagent.debug.ui

import androidx.compose.runtime.Composable
import com.flagent.enhanced.manager.FlagentManager

/**
 * Main entry point for Flagent Debug UI.
 * Provides a Composable debug screen for evaluation and cache actions.
 */
object FlagentDebugUI {
    /**
     * Debug screen composable: evaluation form, result display, cache actions, last evaluations.
     */
    @Composable
    fun DebugScreen(manager: FlagentManager) {
        FlagentDebugScreen(manager = manager)
    }
}
