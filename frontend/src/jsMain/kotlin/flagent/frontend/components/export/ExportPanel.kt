package flagent.frontend.components.export

import androidx.compose.runtime.*
import flagent.frontend.api.ApiClient
import flagent.frontend.config.AppConfig
import flagent.frontend.components.Icon
import flagent.frontend.theme.FlagentTheme
import kotlinx.browser.window
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

/**
 * Export Panel component (Phase 4)
 */
@Composable
fun ExportPanel() {
    val excludeSnapshots = remember { mutableStateOf(false) }
    
    Div({
        style {
            backgroundColor(Color.white)
            borderRadius(8.px)
            padding(24.px)
            property("box-shadow", "0 1px 3px rgba(0, 0, 0, 0.1)")
        }
    }) {
        H2({
            style {
                fontSize(20.px)
                fontWeight(600)
                color(Color("#1E293B"))
                margin(0.px)
                marginBottom(24.px)
            }
        }) {
            Text("Export Data")
        }
        
        // Export evaluation cache
        ExportCard(
            icon = "data_object",
            title = "Evaluation Cache (JSON)",
            description = "Export current evaluation cache as JSON file",
            buttonLabel = "Export JSON",
            onClick = {
                val url = "${AppConfig.apiBaseUrl}/api/v1/export/eval_cache/json"
                window.open(url, "_blank")
            }
        )
        
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
                onClick = {
                    val url = buildString {
                        append("${AppConfig.apiBaseUrl}/api/v1/export/sqlite")
                        if (excludeSnapshots.value) {
                            append("?exclude_snapshots=true")
                        }
                    }
                    window.open(url, "_blank")
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
                        color(Color("#64748B"))
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
    onClick: () -> Unit
) {
    Div({
        style {
            padding(16.px)
            border(1.px, LineStyle.Solid, Color("#E2E8F0"))
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
                        color(Color("#1E293B"))
                        margin(0.px)
                        marginBottom(4.px)
                    }
                }) {
                    Text(title)
                }
                P({
                    style {
                        fontSize(14.px)
                        color(Color("#64748B"))
                        margin(0.px)
                    }
                }) {
                    Text(description)
                }
            }
        }
        
        Button({
            onClick { onClick() }
            style {
                padding(10.px, 20.px)
                backgroundColor(FlagentTheme.Primary)
                color(Color.white)
                border(0.px)
                borderRadius(6.px)
                cursor("pointer")
                fontSize(14.px)
                fontWeight(500)
            }
        }) {
            Text(buttonLabel)
        }
    }
}
