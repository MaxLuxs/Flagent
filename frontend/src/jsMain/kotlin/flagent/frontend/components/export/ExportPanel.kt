package flagent.frontend.components.export

import androidx.compose.runtime.*
import flagent.frontend.api.ApiClient
import flagent.frontend.components.Icon
import flagent.frontend.state.LocalThemeMode
import flagent.frontend.theme.FlagentTheme
import flagent.frontend.util.ErrorHandler
import flagent.frontend.util.triggerDownload
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

/**
 * Export Panel component - downloads with auth headers
 */
@Composable
fun ExportPanel() {
    val themeMode = LocalThemeMode.current
    val excludeSnapshots = remember { mutableStateOf(false) }
    val scope = remember { CoroutineScope(Dispatchers.Main) }
    val loadingExport = remember { mutableStateOf<String?>(null) }
    val exportError = remember { mutableStateOf<String?>(null) }
    
    Div({
        style {
            backgroundColor(FlagentTheme.cardBg(themeMode))
            borderRadius(8.px)
            padding(24.px)
            property("box-shadow", "0 1px 3px rgba(0, 0, 0, 0.1)")
        }
    }) {
        H2({
            style {
                fontSize(20.px)
                fontWeight(600)
                color(FlagentTheme.text(themeMode))
                margin(0.px)
                marginBottom(24.px)
            }
        }) {
            Text("Export Data")
        }
        
        exportError.value?.let { err ->
            Div({
                style {
                    padding(12.px)
                    backgroundColor(FlagentTheme.errorBg(themeMode))
                    borderRadius(6.px)
                    color(FlagentTheme.errorText(themeMode))
                    marginBottom(16.px)
                }
            }) {
                Text(err)
            }
        }
        
        // Export evaluation cache
        ExportCard(
            icon = "data_object",
            title = "Evaluation Cache (JSON)",
            description = "Export current evaluation cache as JSON file",
            buttonLabel = "Export JSON",
            loading = loadingExport.value == "eval_cache",
            onClick = {
                scope.launch {
                    loadingExport.value = "eval_cache"
                    exportError.value = null
                    try {
                        val bytes = ApiClient.downloadExport("/export/eval_cache/json")
                        triggerDownload(bytes, "flagent_eval_cache.json", "application/json")
                    } catch (e: Exception) {
                        exportError.value = ErrorHandler.getUserMessage(ErrorHandler.handle(e))
                    } finally {
                        loadingExport.value = null
                    }
                }
            }
        )

        // Export GitOps format (YAML/JSON) - import-compatible
        Div({
            style {
                marginTop(16.px)
            }
        }) {
            ExportCard(
                icon = "code",
                title = "GitOps (YAML/JSON)",
                description = "Export flags in GitOps format for import/version control",
                buttonLabel = "Export YAML",
                loading = loadingExport.value == "gitops_yaml",
                onClick = {
                    scope.launch {
                        loadingExport.value = "gitops_yaml"
                        exportError.value = null
                        try {
                            val bytes = ApiClient.downloadExport("/export/gitops?format=yaml")
                            triggerDownload(bytes, "flagent_gitops.yaml", "text/yaml")
                        } catch (e: Exception) {
                            exportError.value = ErrorHandler.getUserMessage(ErrorHandler.handle(e))
                        } finally {
                            loadingExport.value = null
                        }
                    }
                }
            )
            Div({ style { marginTop(16.px) } }) {
            ExportCard(
                icon = "code",
                title = "GitOps JSON",
                description = "Export flags in GitOps JSON format",
                buttonLabel = "Export JSON",
                loading = loadingExport.value == "gitops_json",
                onClick = {
                    scope.launch {
                        loadingExport.value = "gitops_json"
                        exportError.value = null
                        try {
                            val bytes = ApiClient.downloadExport("/export/gitops?format=json")
                            triggerDownload(bytes, "flagent_gitops.json", "application/json")
                        } catch (e: Exception) {
                            exportError.value = ErrorHandler.getUserMessage(ErrorHandler.handle(e))
                        } finally {
                            loadingExport.value = null
                        }
                    }
                }
            )
            }
        }
        
        // Export database
        Div({
            style {
                marginTop(16.px)
            }
        }) {
            ExportCard(
                icon = "storage",
                title = "Database (SQLite)",
                description = "Export entire database as SQLite file",
                buttonLabel = "Export SQLite",
                loading = loadingExport.value == "sqlite",
                onClick = {
                    scope.launch {
                        loadingExport.value = "sqlite"
                        exportError.value = null
                        try {
                            val path = if (excludeSnapshots.value) "/export/sqlite?exclude_snapshots=true" else "/export/sqlite"
                            val bytes = ApiClient.downloadExport(path)
                            triggerDownload(bytes, "flagent_export.sqlite", "application/octet-stream")
                        } catch (e: Exception) {
                            exportError.value = ErrorHandler.getUserMessage(ErrorHandler.handle(e))
                        } finally {
                            loadingExport.value = null
                        }
                    }
                }
            )
            
            // Checkbox option
            Div({
                style {
                    display(DisplayStyle.Flex)
                    alignItems(AlignItems.Center)
                    gap(8.px)
                    marginTop(12.px)
                    cursor("pointer")
                }
            }) {
                Input(org.jetbrains.compose.web.attributes.InputType.Checkbox) {
                    checked(excludeSnapshots.value)
                    onChange { excludeSnapshots.value = it.value }
                    style {
                        cursor("pointer")
                    }
                }
                Span({
                    style {
                        fontSize(14.px)
                        color(FlagentTheme.textLight(themeMode))
                    }
                }) {
                    Text("Exclude snapshots (reduce file size)")
                }
            }
        }
    }
}

@Composable
private fun ExportCard(
    icon: String,
    title: String,
    description: String,
    buttonLabel: String,
    loading: Boolean = false,
    onClick: () -> Unit
) {
    val themeMode = LocalThemeMode.current
    Div({
        style {
            padding(16.px)
            border(1.px, LineStyle.Solid, FlagentTheme.cardBorder(themeMode))
            borderRadius(6.px)
            display(DisplayStyle.Flex)
            justifyContent(JustifyContent.SpaceBetween)
            alignItems(AlignItems.Center)
        }
    }) {
        Div({
            style {
                display(DisplayStyle.Flex)
                gap(16.px)
                alignItems(AlignItems.Center)
            }
        }) {
            Icon(icon, size = 32.px, color = FlagentTheme.Primary)
            Div {
                P({
                    style {
                        fontSize(16.px)
                        fontWeight(600)
                        color(FlagentTheme.text(themeMode))
                        margin(0.px)
                        marginBottom(4.px)
                    }
                }) {
                    Text(title)
                }
                P({
                    style {
                        fontSize(14.px)
                        color(FlagentTheme.textLight(themeMode))
                        margin(0.px)
                    }
                }) {
                    Text(description)
                }
            }
        }
        
        Button({
            onClick { if (!loading) onClick() }
            if (loading) attr("disabled", "true")
            style {
                padding(10.px, 20.px)
                backgroundColor(if (loading) FlagentTheme.NeutralLighter else FlagentTheme.Primary)
                color(if (loading) FlagentTheme.Neutral else Color.white)
                border(0.px)
                borderRadius(6.px)
                cursor(if (loading) "not-allowed" else "pointer")
                fontSize(14.px)
                fontWeight(500)
            }
        }) {
            Text(if (loading) "..." else buttonLabel)
        }
    }
}
