package com.flagent.debug.ui

// TODO: Implement with Compose Multiplatform when ready
// import androidx.compose.runtime.Composable
import com.flagent.enhanced.manager.FlagentManager

/**
 * Main entry point for Flagent Debug UI
 */
object FlagentDebugUI {
    /**
     * Get debug screen composable
     * TODO: Uncomment when Compose Multiplatform is configured
     */
    // @Composable
    fun DebugScreen(manager: FlagentManager) {
        FlagentDebugScreen(manager = manager)
    }
}