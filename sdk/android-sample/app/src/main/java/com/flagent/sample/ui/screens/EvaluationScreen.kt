package com.flagent.sample.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.flagent.sample.viewmodel.EvaluationViewModel

@Composable
fun EvaluationScreen(
    baseUrl: String,
    viewModel: EvaluationViewModel = viewModel()
) {
    viewModel.initialize(baseUrl)
    val state by viewModel.state.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Single Evaluation",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Base URL: $baseUrl",
            style = MaterialTheme.typography.bodyMedium
        )
        when (val s = state) {
            is com.flagent.sample.viewmodel.EvaluationState.Success ->
                Text(
                    text = "Result: variantKey=${s.result.variantKey}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            is com.flagent.sample.viewmodel.EvaluationState.Error ->
                Text(
                    text = "Error: ${s.message}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            else -> {}
        }
    }
}
