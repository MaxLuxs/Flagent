package com.flagent.sample.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.flagent.enhanced.config.FlagentConfig
import com.flagent.sample.di.AppModule
import com.flagent.sample.viewmodel.SettingsViewModel

@Composable
fun DebugScreen(
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val settings by settingsViewModel.settings.collectAsState()
    
    val manager = AppModule.getFlagentManager(
        settings.baseUrl,
        FlagentConfig(
            enableCache = settings.cacheEnabled,
            cacheTtlMs = settings.cacheTtlMs
        )
    )

    Scaffold { paddingValues ->
        // Note: FlagentDebugScreen is a placeholder in the current SDK implementation
        // It will be properly implemented in the future
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text("Debug UI")
            Text("Manager initialized with baseUrl: ${settings.baseUrl}")
            // FlagentDebugScreen(manager = manager)
        }
    }
}
