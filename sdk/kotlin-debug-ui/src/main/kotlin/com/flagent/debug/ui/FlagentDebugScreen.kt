package com.flagent.debug.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.flagent.client.models.EvalResult
import com.flagent.design.tokens.FlagentDesignTokens
import kotlinx.coroutines.launch

private const val LAST_EVALS_MAX = 10
private const val FLAGS_VISIBLE_INITIAL = 10
private const val NO_TAG_KEY = "\u0000"

private enum class FlagsGroupBy { None, ByTag, ByKeyPrefix }

private sealed class FlagsListEntry {
    data class Group(val name: String) : FlagsListEntry()
    data class Flag(val row: FlagRow) : FlagsListEntry()
}

@Composable
internal fun FlagentDebugScreen(
    evaluateManager: DebugEvaluateManager,
    flagsProvider: (suspend () -> List<FlagRow>)? = null
) {
    var flagKey by remember { mutableStateOf("") }
    var flagIDText by remember { mutableStateOf("") }
    var entityID by remember { mutableStateOf("") }
    var entityType by remember { mutableStateOf("") }
    var enableDebug by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<EvalResult?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var cacheMessage by remember { mutableStateOf<String?>(null) }
    val lastEvals = remember { mutableStateListOf<EvalResult>() }
    val flagsList = remember { mutableStateListOf<FlagRow>() }
    val overrides = remember { mutableStateMapOf<String, String>() }
    var flagsLoading by remember { mutableStateOf(false) }
    var flagsSearchQuery by remember { mutableStateOf("") }
    var flagsSortAscending by remember { mutableStateOf(true) }
    var flagsGroupBy by remember { mutableStateOf(FlagsGroupBy.None) }
    var flagsListExpanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    LaunchedEffect(flagsProvider) {
        if (flagsProvider != null) {
            flagsLoading = true
            try {
                flagsList.clear()
                flagsList.addAll(flagsProvider())
            } catch (_: Exception) {
                // Network/server unavailable: show empty list, don't crash
            } finally {
                flagsLoading = false
            }
        }
    }

    val spacing = FlagentDesignTokens.Spacing
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(spacing.size16.dp),
        verticalArrangement = Arrangement.spacedBy(spacing.size12.dp)
    ) {
        Text(
            "Flagent Debug",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.testTag("debug-title")
        )

        // Flags list (when flagsProvider is set)
        if (flagsProvider != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("flags-card")
                    .animateContentSize()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Flags", style = MaterialTheme.typography.titleMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    scope.launch {
                                        flagsLoading = true
                                        try {
                                            flagsList.clear()
                                            flagsList.addAll(flagsProvider())
                                        } catch (_: Exception) {
                                            // Network/server unavailable
                                        } finally {
                                            flagsLoading = false
                                        }
                                    }
                                },
                                modifier = Modifier.testTag("refresh-flags-button")
                            ) {
                                Text("Refresh")
                            }
                            if (overrides.isNotEmpty()) {
                                TextButton(onClick = { overrides.clear() }) {
                                    Text("Clear all overrides")
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    if (flagsLoading && flagsList.isEmpty()) {
                        Text("Loading…", style = MaterialTheme.typography.bodySmall)
                    } else if (flagsList.isEmpty()) {
                        Text("No flags", style = MaterialTheme.typography.bodySmall)
                    } else {
                        val listSnapshot = flagsList.toList()
                        val filtered = remember(flagsList.size, flagsList.joinToString { it.key }, flagsSearchQuery) {
                            val q = flagsSearchQuery.trim().lowercase()
                            if (q.isEmpty()) listSnapshot
                            else listSnapshot.filter { row ->
                                row.key.lowercase().contains(q) ||
                                    (row.description?.lowercase()?.contains(q) == true) ||
                                    row.tags.any { it.lowercase().contains(q) }
                            }
                        }
                        val sorted = remember(filtered, flagsSortAscending) {
                            if (flagsSortAscending) filtered.sortedBy { it.key }
                            else filtered.sortedByDescending { it.key }
                        }
                        val entries = remember(sorted, flagsGroupBy) {
                            when (flagsGroupBy) {
                                FlagsGroupBy.None -> sorted.map { FlagsListEntry.Flag(it) }
                                FlagsGroupBy.ByTag -> {
                                    val noTag = "No tag"
                                    sorted
                                        .groupBy { row -> row.tags.firstOrNull() ?: NO_TAG_KEY }
                                        .toList()
                                        .sortedBy { (k, _) -> if (k == NO_TAG_KEY) "zzz" else k }
                                        .flatMap { (k, list) ->
                                            listOf(FlagsListEntry.Group(if (k == NO_TAG_KEY) noTag else k)) +
                                                list.map { FlagsListEntry.Flag(it) }
                                        }
                                }
                                FlagsGroupBy.ByKeyPrefix -> {
                                    sorted
                                        .groupBy { row ->
                                            row.key.split("_", ".").firstOrNull()?.ifEmpty { "other" } ?: "other"
                                        }
                                        .toList()
                                        .sortedBy { it.first }
                                        .flatMap { (prefix, list) ->
                                            listOf(FlagsListEntry.Group(prefix)) + list.map { FlagsListEntry.Flag(it) }
                                        }
                                }
                            }
                        }
                        val flagCount = entries.count { it is FlagsListEntry.Flag }
                        val hasMoreThanInitial = flagCount > FLAGS_VISIBLE_INITIAL
                        var seen = 0
                        val visibleEntries = entries.takeWhile { entry ->
                            when (entry) {
                                is FlagsListEntry.Group -> true
                                is FlagsListEntry.Flag -> {
                                    seen++
                                    seen <= FLAGS_VISIBLE_INITIAL
                                }
                            }
                        }
                        val extraEntries = if (flagsListExpanded && flagCount > FLAGS_VISIBLE_INITIAL) {
                            seen = 0
                            entries.dropWhile { entry ->
                                when (entry) {
                                    is FlagsListEntry.Group -> true
                                    is FlagsListEntry.Flag -> {
                                        seen++
                                        seen <= FLAGS_VISIBLE_INITIAL
                                    }
                                }
                            }
                        } else emptyList()

                        OutlinedTextField(
                            value = flagsSearchQuery,
                            onValueChange = { flagsSearchQuery = it },
                            label = { Text("Search by key, description, tag") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("flags-search"),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Sort:", style = MaterialTheme.typography.labelMedium)
                            FilterChip(
                                selected = flagsSortAscending,
                                onClick = { flagsSortAscending = true },
                                label = { Text("A–Z") },
                                modifier = Modifier.testTag("sort-asc")
                            )
                            FilterChip(
                                selected = !flagsSortAscending,
                                onClick = { flagsSortAscending = false },
                                label = { Text("Z–A") },
                                modifier = Modifier.testTag("sort-desc")
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text("Group:", style = MaterialTheme.typography.labelMedium)
                            FilterChip(
                                selected = flagsGroupBy == FlagsGroupBy.None,
                                onClick = { flagsGroupBy = FlagsGroupBy.None },
                                label = { Text("None") },
                                modifier = Modifier.testTag("group-none")
                            )
                            FilterChip(
                                selected = flagsGroupBy == FlagsGroupBy.ByTag,
                                onClick = { flagsGroupBy = FlagsGroupBy.ByTag },
                                label = { Text("By tag") },
                                modifier = Modifier.testTag("group-tag")
                            )
                            FilterChip(
                                selected = flagsGroupBy == FlagsGroupBy.ByKeyPrefix,
                                onClick = { flagsGroupBy = FlagsGroupBy.ByKeyPrefix },
                                label = { Text("By key prefix") },
                                modifier = Modifier.testTag("group-prefix")
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        Column(modifier = Modifier.animateContentSize()) {
                            visibleEntries.forEach { entry ->
                                when (entry) {
                                    is FlagsListEntry.Group -> Text(
                                        entry.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(vertical = 4.dp).testTag("group-${entry.name}")
                                    )
                                    is FlagsListEntry.Flag -> FlagRowItem(
                                        flagRow = entry.row,
                                        overrideValue = overrides[entry.row.key],
                                        onSetOverride = { overrides[entry.row.key] = it },
                                        onClearOverride = { overrides.remove(entry.row.key) },
                                        modifier = Modifier.testTag("flag-row-${entry.row.key}")
                                    )
                                }
                            }
                            AnimatedVisibility(
                                visible = hasMoreThanInitial,
                                enter = expandVertically(),
                                exit = shrinkVertically()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    TextButton(
                                        onClick = { flagsListExpanded = !flagsListExpanded },
                                        modifier = Modifier.testTag("flags-expand-toggle")
                                    ) {
                                        Text(
                                            if (flagsListExpanded)
                                                "Show less"
                                            else
                                                "Show more (${flagCount - FLAGS_VISIBLE_INITIAL})"
                                        )
                                    }
                                }
                            }
                            AnimatedVisibility(
                                visible = flagsListExpanded && extraEntries.isNotEmpty(),
                                enter = expandVertically(),
                                exit = shrinkVertically()
                            ) {
                                Column {
                                    extraEntries.forEach { entry ->
                                        when (entry) {
                                            is FlagsListEntry.Group -> Text(
                                                entry.name,
                                                style = MaterialTheme.typography.titleSmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(vertical = 4.dp).testTag("group-${entry.name}")
                                            )
                                            is FlagsListEntry.Flag -> FlagRowItem(
                                                flagRow = entry.row,
                                                overrideValue = overrides[entry.row.key],
                                                onSetOverride = { overrides[entry.row.key] = it },
                                                onClearOverride = { overrides.remove(entry.row.key) },
                                                modifier = Modifier.testTag("flag-row-${entry.row.key}")
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Evaluation form
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Evaluate", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = flagKey,
                    onValueChange = { flagKey = it },
                    label = { Text("Flag key") },
                    modifier = Modifier.fillMaxWidth().testTag("eval-flag-key"),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = flagIDText,
                    onValueChange = { flagIDText = it },
                    label = { Text("Flag ID (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text("Current entity (used for evaluate)", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = entityID,
                    onValueChange = { entityID = it },
                    label = { Text("Entity ID") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = entityType,
                    onValueChange = { entityType = it },
                    label = { Text("Entity type") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = enableDebug, onCheckedChange = { enableDebug = it })
                    Text("Enable debug (evalDebugLog)")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        error = null
                        result = null
                        scope.launch {
                            try {
                                val key = flagKey.ifBlank { null }
                                val overrideVariant = key?.let { overrides[it] }
                                val r = if (overrideVariant != null) {
                                    EvalResult(
                                        flagKey = key,
                                        variantKey = overrideVariant
                                    )
                                } else {
                                    val fid = flagIDText.toLongOrNull()
                                    evaluateManager.evaluate(
                                        flagKey = key,
                                        flagID = fid,
                                        entityID = entityID.ifBlank { null },
                                        entityType = entityType.ifBlank { null },
                                        entityContext = null,
                                        enableDebug = enableDebug
                                    )
                                }
                                result = r
                                lastEvals.add(0, r)
                                if (lastEvals.size > LAST_EVALS_MAX) lastEvals.removeAt(lastEvals.lastIndex)
                            } catch (e: Exception) {
                                error = e.message ?: "Evaluation failed"
                            }
                        }
                    },
                    modifier = Modifier.testTag("evaluate-button")
                ) {
                    Text("Evaluate")
                }
            }
        }

        error?.let { msg ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    msg,
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        result?.let { r ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("result-card")
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Result", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("flagKey: ${r.flagKey ?: "—"}")
                    Text("variantKey: ${r.variantKey ?: "—"}")
                    Text("segmentID: ${r.segmentID?.toString() ?: "—"}")
                    Text("variantAttachment: ${r.variantAttachment?.toString() ?: "—"}")
                    if (enableDebug && r.evalDebugLog != null) {
                        Text("evalDebugLog: ${r.evalDebugLog?.msg ?: ""}")
                        r.evalDebugLog?.segmentDebugLogs?.forEach { seg ->
                            Text("  segment ${seg.segmentID}: ${seg.msg ?: ""}")
                        }
                    }
                }
            }
        }

        // Cache actions
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Cache", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            cacheMessage = null
                            scope.launch {
                                evaluateManager.clearCache()
                                cacheMessage = "Cache cleared"
                            }
                        },
                        modifier = Modifier.testTag("clear-cache-button")
                    ) { Text("Clear cache") }
                    Button(
                        onClick = {
                            cacheMessage = null
                            scope.launch {
                                evaluateManager.evictExpired()
                                cacheMessage = "Evict expired done"
                            }
                        },
                        modifier = Modifier.testTag("evict-expired-button")
                    ) { Text("Evict expired") }
                }
                cacheMessage?.let { Text(it, modifier = Modifier.padding(top = 4.dp)) }
            }
        }

        // Last evaluations
        if (lastEvals.isNotEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Last evaluations", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    lastEvals.take(5).forEachIndexed { index, r ->
                        Text(
                            "${index + 1}. ${r.flagKey ?: "id=${r.flagID}"} → ${r.variantKey ?: "—"}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FlagRowItem(
    flagRow: FlagRow,
    overrideValue: String?,
    onSetOverride: (String) -> Unit,
    onClearOverride: () -> Unit,
    modifier: Modifier = Modifier
) {
    var optionsExpanded by remember { mutableStateOf(false) }
    val options = listOf("disabled") + flagRow.variantKeys
    Column(modifier = modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(flagRow.key, style = MaterialTheme.typography.bodyMedium)
                Text(
                    "enabled=${flagRow.enabled}, variants=${flagRow.variantKeys.joinToString()}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                overrideValue ?: "—",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(
                    onClick = { optionsExpanded = !optionsExpanded },
                    modifier = Modifier.testTag("override-${flagRow.key}")
                ) {
                    Text(if (overrideValue != null) "Change" else "Override")
                }
                if (overrideValue != null) {
                    TextButton(
                        onClick = onClearOverride,
                        modifier = Modifier.testTag("clear-override-${flagRow.key}")
                    ) { Text("Clear") }
                }
            }
        }
        AnimatedVisibility(
            visible = optionsExpanded,
            enter = expandVertically(),
            exit = shrinkVertically(),
            modifier = Modifier.animateContentSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                options.forEach { opt ->
                    Button(
                        onClick = {
                            onSetOverride(opt)
                            optionsExpanded = false
                        },
                        modifier = Modifier.testTag("override-option-$opt"),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(opt, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}
