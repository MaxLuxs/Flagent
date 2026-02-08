package flagent.frontend.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import flagent.api.model.FlagSnapshotResponse
import flagent.frontend.api.ApiClient
import flagent.frontend.i18n.LocalizedStrings
import flagent.frontend.state.LocalThemeMode
import flagent.frontend.theme.FlagentTheme
import kotlinx.browser.document
import kotlinx.serialization.json.Json
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

/**
 * FlagHistory component - flag change history with diff visualization
 */
@Composable
fun FlagHistory(flagId: Int) {
    val themeMode = LocalThemeMode.current
    val history = remember { mutableStateListOf<FlagSnapshotResponse>() }
    val loading = remember { mutableStateOf(true) }
    val error = remember { mutableStateOf<String?>(null) }

    LaunchedEffect(flagId) {
        loading.value = true
        error.value = null
        try {
            val snapshots = ApiClient.getFlagSnapshots(flagId, limit = 50)
            history.clear()
            history.addAll(snapshots)
        } catch (e: Exception) {
            error.value = e.message ?: LocalizedStrings.failedToLoadFlagHistory
        } finally {
            loading.value = false
        }
    }

    Div({
        style {
            padding(20.px)
        }
    }) {
        H3({
            style {
                margin(0.px, 0.px, 20.px, 0.px)
                color(FlagentTheme.text(themeMode))
            }
        }) {
            Text(LocalizedStrings.flagHistory)
        }
        InfoTooltip(
            title = LocalizedStrings.flagHistory,
            description = LocalizedStrings.flagHistoryTooltipDescription,
            details = LocalizedStrings.flagHistoryTooltipDetails
        )

        if (loading.value) {
            Spinner()
        } else if (error.value != null) {
            Div({
                style {
                    color(FlagentTheme.errorText(themeMode))
                    padding(10.px)
                    backgroundColor(FlagentTheme.errorBg(themeMode))
                    borderRadius(5.px)
                }
            }) {
                Text("${LocalizedStrings.error}: ${error.value}")
            }
        } else if (history.isEmpty()) {
            Div({
                style {
                    padding(20.px)
                    textAlign("center")
                    color(FlagentTheme.textLight(themeMode))
                }
            }) {
                Text(LocalizedStrings.noHistory)
            }
        } else {
            // Create diffs between snapshots
            val diffs = remember(history) {
                val result = mutableListOf<DiffItem>()
                val snapshots = history.toList()
                snapshots.forEachIndexed { index, snapshot ->
                    val oldSnapshot = if (index < snapshots.size - 1) snapshots[index + 1] else null
                    result.add(
                        DiffItem(
                            newSnapshot = snapshot,
                            oldSnapshot = oldSnapshot,
                            diff = if (oldSnapshot != null) {
                                computeDiff(oldSnapshot.flag, snapshot.flag)
                            } else null
                        )
                    )
                }
                result
            }
            
            diffs.forEach { diff ->
                SnapshotDiffCard(diff)
            }
        }
    }
}

/**
 * Diff item - contains old and new snapshots with computed diff
 */
private data class DiffItem(
    val newSnapshot: FlagSnapshotResponse,
    val oldSnapshot: FlagSnapshotResponse?,
    val diff: String?
)

/**
 * Snapshot Diff Card - displays snapshot with diff visualization
 */
@Composable
private fun SnapshotDiffCard(diff: DiffItem) {
    val themeMode = LocalThemeMode.current
    Div({
        style {
            padding(20.px)
            border {
                width(1.px)
                style(LineStyle.Solid)
                color(FlagentTheme.cardBorder(themeMode))
            }
            borderRadius(5.px)
            backgroundColor(FlagentTheme.cardBg(themeMode))
            marginBottom(20.px)
        }
    }) {
        // Header
        Div({
            style {
                display(DisplayStyle.Flex)
                justifyContent(JustifyContent.SpaceBetween)
                alignItems(AlignItems.Center)
                marginBottom(15.px)
                paddingBottom(15.px)
                property("border-bottom", "1px solid ${FlagentTheme.Secondary}")
            }
        }) {
            Div({
                style {
                    display(DisplayStyle.Flex)
                    alignItems(AlignItems.Center)
                    gap(10.px)
                }
            }) {
                Span({
                    style {
                        padding(5.px, 10.px)
                        backgroundColor(FlagentTheme.Primary)
                        color(Color.white)
                        borderRadius(3.px)
                        fontSize(12.px)
                    }
                }) {
                    Text("${LocalizedStrings.snapshotId}: ${diff.oldSnapshot?.id ?: "NULL"}")
                }
                Span({
                    style {
                        color(FlagentTheme.text(themeMode))
                        fontSize(16.px)
                    }
                }) {
                    Text("→")
                }
                Span({
                    style {
                        padding(5.px, 10.px)
                        backgroundColor(FlagentTheme.Primary)
                        color(Color.white)
                        borderRadius(3.px)
                        fontSize(12.px)
                    }
                }) {
                    Text("${LocalizedStrings.snapshotId}: ${diff.newSnapshot.id}")
                }
            }
            Div({
                style {
                    display(DisplayStyle.Flex)
                    flexDirection(FlexDirection.Column)
                    alignItems(AlignItems.FlexEnd)
                    color(FlagentTheme.text(themeMode))
                }
            }) {
                Span({
                    style {
                        fontSize(12.px)
                    }
                }) {
                    Text(diff.newSnapshot.updatedAt.split(".")[0])
                }
                diff.newSnapshot.updatedBy?.let { updatedBy ->
                    Span({
                        style {
                            fontSize(11.px)
                            marginTop(5.px)
                        }
                    }) {
                        Text("${LocalizedStrings.updatedByUpper}: $updatedBy")
                    }
                }
            }
        }
        
        // Diff content
        if (diff.diff == null || diff.diff == "No changes") {
            Div({
                style {
                    padding(15.px)
                    backgroundColor(FlagentTheme.inputBg(themeMode))
                    borderRadius(5.px)
                    color(FlagentTheme.textLight(themeMode))
                }
            }) {
                Text(LocalizedStrings.noChanges)
            }
        } else {
            Div({
                id("diff-${diff.newSnapshot.id}")
                style {
                    padding(15.px)
                    backgroundColor(FlagentTheme.inputBg(themeMode))
                    border {
                        width(1.px)
                        style(LineStyle.Solid)
                        color(FlagentTheme.inputBorder(themeMode))
                    }
                    borderRadius(5.px)
                    overflow("auto")
                    maxHeight(500.px)
                    fontFamily("monospace")
                    fontSize(12.px)
                    whiteSpace("pre-wrap")
                    property("word-wrap", "break-word")
                }
            }) {
                // Render diff - will be set via innerHTML
            }
            
            // Set innerHTML via LaunchedEffect
            LaunchedEffect(diff.diff) {
                val element = kotlinx.browser.document.getElementById("diff-${diff.newSnapshot.id}")
                element?.innerHTML = diff.diff
            }
        }
    }
}

/**
 * Compute diff between two flag objects
 */
private fun computeDiff(oldFlag: flagent.api.model.FlagResponse, newFlag: flagent.api.model.FlagResponse): String {
    val json = Json { prettyPrint = true }
    val oldJson = try {
        json.encodeToString(flagent.api.model.FlagResponse.serializer(), oldFlag)
    } catch (e: Exception) {
        "{}"
    }
    val newJson = try {
        json.encodeToString(flagent.api.model.FlagResponse.serializer(), newFlag)
    } catch (e: Exception) {
        "{}"
    }
    
    if (oldJson == newJson) {
        return "No changes"
    }
    
    // Parse JSON to compare field by field
    val oldObj = try {
        Json.parseToJsonElement(oldJson) as? kotlinx.serialization.json.JsonObject
    } catch (e: Exception) {
        null
    }
    val newObj = try {
        Json.parseToJsonElement(newJson) as? kotlinx.serialization.json.JsonObject
    } catch (e: Exception) {
        null
    }
    
    if (oldObj == null || newObj == null) {
        // Fallback to line-by-line diff
        return computeLineDiff(oldJson, newJson)
    }
    
    // Field-by-field comparison
    val diff = mutableListOf<String>()
    val allKeys = (oldObj.keys + newObj.keys).sorted().distinct()
    
    diff.add("<div style='font-family: monospace; font-size: 12px;'>")
    
    allKeys.forEach { key ->
        val oldValue = oldObj[key]
        val newValue = newObj[key]
        
        when {
            oldValue == null && newValue != null -> {
                // Added - green background
                diff.add("<div style='margin: 3px 0; padding: 8px 12px; background-color: #d4edda; border-left: 4px solid #28a745; border-radius: 4px;'>")
                diff.add("<span style='color: #155724; font-weight: bold; margin-right: 8px;'>+</span>")
                diff.add("<span style='color: #155724; font-weight: 600;'>$key:</span> ")
                diff.add("<span style='color: #155724;'>${formatJsonValue(newValue)}</span>")
                diff.add("</div>")
            }
            oldValue != null && newValue == null -> {
                // Removed - red background
                diff.add("<div style='margin: 3px 0; padding: 8px 12px; background-color: #f8d7da; border-left: 4px solid #dc3545; border-radius: 4px;'>")
                diff.add("<span style='color: #721c24; font-weight: bold; margin-right: 8px;'>-</span>")
                diff.add("<span style='color: #721c24; font-weight: 600;'>$key:</span> ")
                diff.add("<span style='color: #721c24; text-decoration: line-through;'>${formatJsonValue(oldValue)}</span>")
                diff.add("</div>")
            }
            oldValue != null && newValue != null && oldValue != newValue -> {
                // Changed - yellow background with before/after
                diff.add("<div style='margin: 3px 0; padding: 8px 12px; background-color: #fff3cd; border-left: 4px solid #ffc107; border-radius: 4px;'>")
                diff.add("<div style='margin-bottom: 4px;'>")
                diff.add("<span style='color: #856404; font-weight: bold; margin-right: 8px;'>~</span>")
                diff.add("<span style='color: #856404; font-weight: 600;'>$key:</span>")
                diff.add("</div>")
                diff.add("<div style='margin-left: 24px;'>")
                diff.add("<div style='padding: 4px 8px; margin: 2px 0; background-color: #f7b3b3; border-radius: 3px; display: inline-block;'>")
                diff.add("<span style='color: #721c24; text-decoration: line-through;'>${formatJsonValue(oldValue)}</span>")
                diff.add("</div>")
                diff.add("<span style='margin: 0 8px; color: #856404;'>→</span>")
                diff.add("<div style='padding: 4px 8px; margin: 2px 0; background-color: #b6ddc6; border-radius: 3px; display: inline-block;'>")
                diff.add("<span style='color: #155724;'>${formatJsonValue(newValue)}</span>")
                diff.add("</div>")
                diff.add("</div>")
                diff.add("</div>")
            }
            else -> {
                // Unchanged (optional - can be hidden)
                // diff.add("<div style='color: #666;'>$key: ${formatJsonValue(newValue)}</div>")
            }
        }
    }
    
    diff.add("</div>")
    return diff.joinToString("")
}

/**
 * Format JSON value for display
 */
private fun formatJsonValue(value: kotlinx.serialization.json.JsonElement): String {
    return when (value) {
        is kotlinx.serialization.json.JsonPrimitive -> {
            if (value.isString) {
                "\"${value.content}\""
            } else {
                value.content
            }
        }
        is kotlinx.serialization.json.JsonObject -> {
            "{ ... }"
        }
        is kotlinx.serialization.json.JsonArray -> {
            "[ ... ]"
        }
        else -> value.toString()
    }
}

/**
 * Fallback line-by-line diff
 */
private fun computeLineDiff(oldJson: String, newJson: String): String {
    val oldLines = oldJson.lines()
    val newLines = newJson.lines()
    val diff = mutableListOf<String>()
    
    diff.add("<div style='font-family: monospace; font-size: 12px;'>")
    
    var oldIndex = 0
    var newIndex = 0
    
    while (oldIndex < oldLines.size || newIndex < newLines.size) {
        val oldLine = oldLines.getOrNull(oldIndex) ?: ""
        val newLine = newLines.getOrNull(newIndex) ?: ""
        
        when {
            oldLine == newLine -> {
                diff.add("<div style='color: #666; padding: 4px 8px; font-family: monospace;'>$oldLine</div>")
                oldIndex++
                newIndex++
            }
            oldLine.isEmpty() -> {
                diff.add("<div style='background-color: #d4edda; padding: 6px 12px; border-left: 4px solid #28a745; border-radius: 4px; margin: 2px 0;'>")
                diff.add("<span style='color: #155724; font-weight: bold; margin-right: 8px;'>+</span>")
                diff.add("<span style='color: #155724; font-family: monospace;'>$newLine</span>")
                diff.add("</div>")
                newIndex++
            }
            newLine.isEmpty() -> {
                diff.add("<div style='background-color: #f8d7da; padding: 6px 12px; border-left: 4px solid #dc3545; border-radius: 4px; margin: 2px 0;'>")
                diff.add("<span style='color: #721c24; font-weight: bold; margin-right: 8px;'>-</span>")
                diff.add("<span style='color: #721c24; text-decoration: line-through; font-family: monospace;'>$oldLine</span>")
                diff.add("</div>")
                oldIndex++
            }
            else -> {
                diff.add("<div style='background-color: #f8d7da; padding: 6px 12px; border-left: 4px solid #dc3545; border-radius: 4px; margin: 2px 0;'>")
                diff.add("<span style='color: #721c24; font-weight: bold; margin-right: 8px;'>-</span>")
                diff.add("<span style='color: #721c24; text-decoration: line-through; font-family: monospace;'>$oldLine</span>")
                diff.add("</div>")
                diff.add("<div style='background-color: #d4edda; padding: 6px 12px; border-left: 4px solid #28a745; border-radius: 4px; margin: 2px 0;'>")
                diff.add("<span style='color: #155724; font-weight: bold; margin-right: 8px;'>+</span>")
                diff.add("<span style='color: #155724; font-family: monospace;'>$newLine</span>")
                diff.add("</div>")
                oldIndex++
                newIndex++
            }
        }
    }
    
    diff.add("</div>")
    return diff.joinToString("")
}

