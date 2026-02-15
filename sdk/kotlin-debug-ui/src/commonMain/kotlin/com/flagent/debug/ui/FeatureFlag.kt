package com.flagent.debug.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import com.flagent.enhanced.manager.FlagentManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * CompositionLocal for FlagentManager. Use [FlagentProvider] to provide the manager.
 */
val LocalFlagentManager = compositionLocalOf<FlagentManager> {
    error("No FlagentManager provided. Wrap with FlagentProvider(manager = ...)")
}

/**
 * Provides FlagentManager to the composition tree.
 */
@Composable
fun FlagentProvider(
    manager: FlagentManager,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalFlagentManager provides manager) {
        content()
    }
}

/**
 * Declarative wrapper for feature flag evaluation.
 * Shows [content] when flag is enabled, [fallback] otherwise.
 *
 * @param key Flag key
 * @param manager FlagentManager (or use from [LocalFlagentManager] when inside [FlagentProvider])
 * @param entityID Entity ID for evaluation
 * @param entityType Entity type (e.g. "user")
 * @param entityContext Context for constraints
 * @param fallback Content shown when flag is disabled or loading
 * @param content Content shown when flag is enabled
 */
@Composable
fun FeatureFlag(
    key: String,
    manager: FlagentManager = LocalFlagentManager.current,
    entityID: String = "default",
    entityType: String? = null,
    entityContext: Map<String, Any>? = null,
    fallback: @Composable () -> Unit = {},
    content: @Composable () -> Unit
) {
    val enabled by produceState<Boolean?>(initialValue = null, key, entityID) {
        value = withContext(Dispatchers.Default) {
            try {
                val result = manager.evaluate(
                    flagKey = key,
                    entityID = entityID,
                    entityType = entityType,
                    entityContext = entityContext
                )
                result.variantKey != "disabled"
            } catch (_: Exception) {
                false
            }
        }
    }
    when (enabled) {
        true -> content()
        false, null -> fallback()
    }
}
