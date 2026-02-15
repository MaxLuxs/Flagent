package com.flagent.sample.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.flagent.sample.viewmodel.EvaluationState
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import com.flagent.sample.viewmodel.EvaluationViewModel

@Composable
fun EvaluationScreen(
    baseUrl: String,
    viewModel: EvaluationViewModel = viewModel()
) {
    viewModel.initialize(baseUrl)
    val state by viewModel.state.collectAsState()

    var flagKey by remember { mutableStateOf("my_feature_flag") }
    var entityID by remember { mutableStateOf("user1") }
    var entityType by remember { mutableStateOf("user") }
    var entityContextJson by remember { mutableStateOf("{\"region\": \"US\", \"tier\": \"premium\"}") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Single evaluation",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Evaluate one flag for an entity. Backend: $baseUrl",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Divider()

        OutlinedTextField(
            value = flagKey,
            onValueChange = { flagKey = it },
            label = { Text("Flag key") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = entityID,
            onValueChange = { entityID = it },
            label = { Text("Entity ID") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = entityType,
            onValueChange = { entityType = it },
            label = { Text("Entity type") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = entityContextJson,
            onValueChange = { entityContextJson = it },
            label = { Text("Entity context (JSON, optional)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )

        Button(
            onClick = {
                viewModel.evaluateBasic(
                    flagKey = flagKey.takeIf { it.isNotBlank() },
                    entityID = entityID.takeIf { it.isNotBlank() },
                    entityType = entityType.ifBlank { "user" },
                    entityContext = parseContextOrNull(entityContextJson)
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = state !is EvaluationState.Loading
        ) {
            if (state is EvaluationState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(if (state is EvaluationState.Loading) "Evaluating…" else "Evaluate (Basic API)")
        }

        Button(
            onClick = {
                viewModel.evaluateEnhanced(
                    flagKey = flagKey.takeIf { it.isNotBlank() },
                    entityID = entityID.takeIf { it.isNotBlank() },
                    entityType = entityType.ifBlank { "user" },
                    entityContext = parseContextMapOrNull(entityContextJson)
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = state !is EvaluationState.Loading
        ) {
            Text("Evaluate (Enhanced SDK)")
        }

        when (val s = state) {
            is EvaluationState.Success -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Result",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Flag: ${s.result.flagKey}",
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Variant: ${s.result.variantKey}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        s.result.segmentID?.let {
                            Text(
                                text = "Segment ID: $it",
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        if (s.fromCache) {
                            Text(
                                text = "(from cache)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
            is EvaluationState.Error -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Error",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = s.message,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = "Ensure the backend is running and the flag exists (e.g. run scripts/seed-demo-data.sh).",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
            else -> {}
        }
    }
}

private fun parseContextOrNull(json: String): Map<String, Any>? = parseContextMapOrNull(json)

private fun parseContextMapOrNull(json: String): Map<String, Any>? {
    if (json.isBlank()) return null
    return try {
        val element = Json.parseToJsonElement(json)
        (element as? JsonObject)?.mapValues { (_, v) ->
            when (v) {
                is JsonPrimitive -> when {
                    v.content == "true" || v.content == "false" -> v.content.toBooleanStrictOrNull() ?: v.content
                    v.content.toLongOrNull() != null -> v.content.toLong()
                    v.content.toDoubleOrNull() != null -> v.content.toDouble()
                    else -> v.content
                }
                else -> v.toString()
            }
        }
    } catch (e: Exception) {
        null
    }
}
