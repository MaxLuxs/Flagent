package com.flagent.sample.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.flagent.client.models.EvalResult

@Composable
fun EvaluationResultCard(
    result: EvalResult,
    fromCache: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Evaluation Result",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (fromCache) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "From Cache",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            result.flagKey?.let {
                InfoRow(label = "Flag Key", value = it)
            }

            result.flagID?.let {
                InfoRow(label = "Flag ID", value = it.toString())
            }

            result.variantKey?.let {
                InfoRow(
                    label = "Variant Key",
                    value = it,
                    highlight = true
                )
            }

            result.variantID?.let {
                InfoRow(label = "Variant ID", value = it.toString())
            }

            result.segmentID?.let {
                InfoRow(label = "Segment ID", value = it.toString())
            }

            result.flagSnapshotID?.let {
                InfoRow(label = "Snapshot ID", value = it.toString())
            }

            result.variantAttachment?.let { attachment ->
                if (attachment.isNotEmpty()) {
                    Text(
                        text = "Variant Attachment:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    attachment.forEach { (key, value) ->
                        InfoRow(
                            label = key,
                            value = value.toString(),
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }

            result.evalDebugLog?.let { debugLog ->
                Text(
                    text = "Debug Log:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    text = debugLog.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            if (fromCache) {
                Text(
                    text = "✓ Result from cache",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    highlight: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.Normal,
            color = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}
