package com.flagent.sample.ui.screens

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.flagent.sample.ui.components.EvaluationResultCard
import com.flagent.sample.viewmodel.EvaluationState
import com.flagent.sample.viewmodel.EvaluationViewModel
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

enum class SdkType {
    BASIC, ENHANCED
}

@Composable
fun EvaluationScreen(
    baseUrl: String,
    viewModel: EvaluationViewModel = viewModel()
) {
    viewModel.initialize(baseUrl)
    
    val state by viewModel.state.collectAsState()
    
    var sdkType by remember { mutableStateOf(SdkType.ENHANCED) }
    var flagKey by remember { mutableStateOf("") }
    var flagID by remember { mutableStateOf("") }
    var entityID by remember { mutableStateOf("user123") }
    var entityType by remember { mutableStateOf("user") }
    var entityContextJson by remember { mutableStateOf("{\"region\": \"US\", \"tier\": \"premium\"}") }
    var enableDebug by remember { mutableStateOf(false) }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Flag Evaluation",
                style = androidx.compose.material3.MaterialTheme.typography.headlineMedium
            )

            Divider()

            Text("SDK Type:")
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = sdkType == SdkType.BASIC,
                        onClick = { sdkType = SdkType.BASIC }
                    )
                    Text("Basic SDK")
                }
                Spacer(modifier = Modifier.padding(horizontal = 16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = sdkType == SdkType.ENHANCED,
                        onClick = { sdkType = SdkType.ENHANCED }
                    )
                    Text("Enhanced SDK (with cache)")
                }
            }

            OutlinedTextField(
                value = flagKey,
                onValueChange = { flagKey = it },
                label = { Text("Flag Key (optional if Flag ID provided)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = flagID,
                onValueChange = { flagID = it },
                label = { Text("Flag ID (optional if Flag Key provided)") },
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
                label = { Text("Entity Type") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = entityContextJson,
                onValueChange = { entityContextJson = it },
                label = { Text("Entity Context (JSON)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Enable Debug")
                Switch(
                    checked = enableDebug,
                    onCheckedChange = { enableDebug = it }
                )
            }

            Button(
                onClick = {
                    val flagIdLong = flagID.toLongOrNull()
                    val context = parseEntityContext(entityContextJson)
                    
                    when (sdkType) {
                        SdkType.BASIC -> {
                            viewModel.evaluateBasic(
                                flagKey = flagKey.ifBlank { null },
                                flagID = flagIdLong,
                                entityID = entityID.ifBlank { null },
                                entityType = entityType.ifBlank { null },
                                entityContext = context,
                                enableDebug = enableDebug
                            )
                        }
                        SdkType.ENHANCED -> {
                            viewModel.evaluateEnhanced(
                                flagKey = flagKey.ifBlank { null },
                                flagID = flagIdLong,
                                entityID = entityID.ifBlank { null },
                                entityType = entityType.ifBlank { null },
                                entityContext = context,
                                enableDebug = enableDebug
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = state !is EvaluationState.Loading
            ) {
                if (state is EvaluationState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .height(20.dp)
                            .padding(end = 8.dp)
                    )
                }
                Text("Evaluate")
            }

            when (val currentState = state) {
                is EvaluationState.Success -> {
                    EvaluationResultCard(
                        result = currentState.result,
                        fromCache = currentState.fromCache
                    )
                }
                is EvaluationState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        androidx.compose.material3.Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = androidx.compose.material3.CardDefaults.cardColors(
                                containerColor = androidx.compose.material3.MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "Error",
                                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                                    color = androidx.compose.material3.MaterialTheme.colorScheme.onErrorContainer
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = currentState.message,
                                    color = androidx.compose.material3.MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                        Button(
                            onClick = {
                                val flagIdLong = flagID.toLongOrNull()
                                val context = parseEntityContext(entityContextJson)
                                
                                when (sdkType) {
                                    SdkType.BASIC -> {
                                        viewModel.evaluateBasic(
                                            flagKey = flagKey.ifBlank { null },
                                            flagID = flagIdLong,
                                            entityID = entityID.ifBlank { null },
                                            entityType = entityType.ifBlank { null },
                                            entityContext = context,
                                            enableDebug = enableDebug
                                        )
                                    }
                                    SdkType.ENHANCED -> {
                                        viewModel.evaluateEnhanced(
                                            flagKey = flagKey.ifBlank { null },
                                            flagID = flagIdLong,
                                            entityID = entityID.ifBlank { null },
                                            entityType = entityType.ifBlank { null },
                                            entityContext = context,
                                            enableDebug = enableDebug
                                        )
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Retry")
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

private fun parseEntityContext(jsonString: String): Map<String, Any>? {
    return try {
        if (jsonString.isBlank()) return null
        val json = Json.parseToJsonElement(jsonString).jsonObject
        json.mapValues { it.value.jsonPrimitive.content }
    } catch (e: Exception) {
        null
    }
}
