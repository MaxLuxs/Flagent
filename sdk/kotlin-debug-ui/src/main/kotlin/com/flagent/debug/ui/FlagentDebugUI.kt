package com.flagent.debug.ui

import androidx.compose.runtime.Composable
import com.flagent.enhanced.manager.FlagentManager

/**
 * Main entry point for Flagent Debug UI
 */
object FlagentDebugUI {
    /**
     * Get debug screen composable
     */
    @Composable
    fun DebugScreen(manager: FlagentManager) {
        FlagentDebugScreen(manager = manager)
    }
}