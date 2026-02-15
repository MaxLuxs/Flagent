package com.flagent.debug.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.flagent.client.models.EvalResult
import com.flagent.enhanced.manager.FlagentManager
import kotlinx.coroutines.launch

private const val LAST_EVALS_MAX = 10

@Composable
internal fun FlagentDebugScreen(manager: FlagentManager) {
    var flagKey by remember { mutableStateOf("") }
    var flagIDText by remember { mutableStateOf("") }
    var entityID by remember { mutableStateOf("") }
    var entityType by remember { mutableStateOf("") }
    var enableDebug by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<EvalResult?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var cacheMessage by remember { mutableStateOf<String?>(null) }
    val lastEvals = remember { mutableStateListOf<EvalResult>() }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Flagent Debug",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.testTag("debug-title")
        )

        // Evaluation form
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Evaluate", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = flagKey,
                    onValueChange = { flagKey = it },
                    label = { Text("Flag key") },
                    modifier = Modifier.fillMaxWidth(),
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
                                val fid = flagIDText.toLongOrNull()
                                val r = manager.evaluate(
                                    flagKey = flagKey.ifBlank { null },
                                    flagID = fid,
                                    entityID = entityID.ifBlank { null },
                                    entityType = entityType.ifBlank { null },
                                    entityContext = null,
                                    enableDebug = enableDebug
                                )
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
                                manager.clearCache()
                                cacheMessage = "Cache cleared"
                            }
                        },
                        modifier = Modifier.testTag("clear-cache-button")
                    ) { Text("Clear cache") }
                    Button(
                        onClick = {
                            cacheMessage = null
                            scope.launch {
                                manager.evictExpired()
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
