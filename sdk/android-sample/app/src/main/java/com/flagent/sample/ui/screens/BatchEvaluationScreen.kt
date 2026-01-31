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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.flagent.client.models.EvaluationEntity
import com.flagent.sample.viewmodel.BatchEvaluationState
import com.flagent.sample.viewmodel.BatchEvaluationViewModel
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

@Composable
fun BatchEvaluationScreen(
    baseUrl: String,
    viewModel: BatchEvaluationViewModel = viewModel()
) {
    viewModel.initialize(baseUrl)
    
    val state by viewModel.state.collectAsState()
    
    var flagKeysText by remember { mutableStateOf("flag1,flag2,flag3") }
    var entity1Id by remember { mutableStateOf("user1") }
    var entity1Type by remember { mutableStateOf("user") }
    var entity1Context by remember { mutableStateOf("{\"region\": \"US\"}") }
    var entity2Id by remember { mutableStateOf("user2") }
    var entity2Type by remember { mutableStateOf("user") }
    var entity2Context by remember { mutableStateOf("{\"region\": \"EU\"}") }

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
                text = "Batch Evaluation",
                style = MaterialTheme.typography.headlineMedium
            )

            Divider()

            OutlinedTextField(
                value = flagKeysText,
                onValueChange = { flagKeysText = it },
                label = { Text("Flag Keys (comma-separated)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Text(
                text = "Entity 1",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = entity1Id,
                onValueChange = { entity1Id = it },
                label = { Text("Entity ID") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = entity1Type,
                onValueChange = { entity1Type = it },
                label = { Text("Entity Type") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = entity1Context,
                onValueChange = { entity1Context = it },
                label = { Text("Entity Context (JSON)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Entity 2",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = entity2Id,
                onValueChange = { entity2Id = it },
                label = { Text("Entity ID") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = entity2Type,
                onValueChange = { entity2Type = it },
                label = { Text("Entity Type") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = entity2Context,
                onValueChange = { entity2Context = it },
                label = { Text("Entity Context (JSON)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            Button(
                onClick = {
                    val flagKeys = flagKeysText.split(",").map { it.trim() }.filter { it.isNotBlank() }
                    val entities = mutableListOf<EvaluationEntity>()
                    
                    if (entity1Id.isNotBlank()) {
                        entities.add(
                            EvaluationEntity(
                                entityID = entity1Id,
                                entityType = entity1Type.ifBlank { "user" },
                                entityContext = parseEntityContext(entity1Context)
                            )
                        )
                    }
                    
                    if (entity2Id.isNotBlank()) {
                        entities.add(
                            EvaluationEntity(
                                entityID = entity2Id,
                                entityType = entity2Type.ifBlank { "user" },
                                entityContext = parseEntityContext(entity2Context)
                            )
                        )
                    }
                    
                    viewModel.evaluateBatch(
                        flagKeys = flagKeys,
                        entities = entities
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = state !is BatchEvaluationState.Loading
            ) {
                if (state is BatchEvaluationState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .height(20.dp)
                            .padding(end = 8.dp)
                    )
                }
                Text("Evaluate Batch")
            }

            when (val currentState = state) {
                is BatchEvaluationState.Success -> {
                    Text(
                        text = "Results (${currentState.results.size}):",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    currentState.results.forEachIndexed { index, result ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "Result ${index + 1}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                result.flagKey?.let {
                                    Text("Flag: $it")
                                }
                                result.evalContext?.entityID?.let {
                                    Text("Entity: $it")
                                }
                                result.variantKey?.let {
                                    Text(
                                        text = "Variant: $it",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
                is BatchEvaluationState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
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
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = currentState.message,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                        Button(
                            onClick = {
                                val flagKeys = flagKeysText.split(",").map { it.trim() }.filter { it.isNotBlank() }
                                val entities = mutableListOf<EvaluationEntity>()
                                
                                if (entity1Id.isNotBlank()) {
                                    entities.add(
                                        EvaluationEntity(
                                            entityID = entity1Id,
                                            entityType = entity1Type.ifBlank { "user" },
                                            entityContext = parseEntityContext(entity1Context)
                                        )
                                    )
                                }
                                
                                if (entity2Id.isNotBlank()) {
                                    entities.add(
                                        EvaluationEntity(
                                            entityID = entity2Id,
                                            entityType = entity2Type.ifBlank { "user" },
                                            entityContext = parseEntityContext(entity2Context)
                                        )
                                    )
                                }
                                
                                viewModel.evaluateBatch(
                                    flagKeys = flagKeys,
                                    entities = entities
                                )
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

private fun parseEntityContext(jsonString: String): kotlinx.serialization.json.JsonObject? {
    return try {
        if (jsonString.isBlank()) return null
        Json.parseToJsonElement(jsonString).jsonObject
    } catch (e: Exception) {
        null
    }
}
