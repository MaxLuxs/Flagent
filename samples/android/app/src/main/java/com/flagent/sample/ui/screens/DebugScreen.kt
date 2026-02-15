package com.flagent.sample.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.flagent.debug.ui.FlagRow
import com.flagent.debug.ui.FlagentDebugUI
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

    val flagApi = remember(settings.baseUrl) { AppModule.getFlagApi(settings.baseUrl) }
    val flagsProvider: (suspend () -> List<FlagRow>) = {
        try {
            val response = flagApi.findFlags(
                limit = 1000,
                offset = null,
                enabled = null,
                description = null,
                key = null,
                descriptionLike = null,
                preload = null,
                deleted = null,
                tags = null
            )
            response.body().map { f ->
                FlagRow(
                    key = f.key,
                    id = f.id,
                    enabled = f.enabled,
                    variantKeys = f.variants?.map { it.key } ?: emptyList(),
                    description = f.description,
                    tags = f.tags?.map { it.`value` } ?: emptyList()
                )
            }
        } catch (_: Exception) {
            // Server unreachable (e.g. backend not running): return empty list
            emptyList()
        }
    }

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            FlagentDebugUI.DebugScreen(manager = manager, flagsProvider = flagsProvider)
        }
    }
}
