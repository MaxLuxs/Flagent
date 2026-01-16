package com.flagent.debug.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.flagent.enhanced.manager.FlagentManager

@Composable
internal fun FlagentDebugScreen(manager: FlagentManager) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text("Flagent Debug UI")
        // TODO: Implement flags list, details, overrides, logs
    }
}