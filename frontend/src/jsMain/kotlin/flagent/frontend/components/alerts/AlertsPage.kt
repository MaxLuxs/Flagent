package flagent.frontend.components.alerts

import androidx.compose.runtime.*
import flagent.frontend.components.CircularProgress
import flagent.frontend.components.Icon
import flagent.frontend.config.AppConfig
import flagent.frontend.navigation.Route
import flagent.frontend.navigation.Router
import flagent.frontend.state.LocalThemeMode
import flagent.frontend.theme.FlagentTheme
import flagent.frontend.viewmodel.AnomalyViewModel
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

/**
 * Alerts Page - глобальный список всех алертов
 */
@Composable
fun AlertsPage() {
    if (!AppConfig.Features.enableAnomalyDetection) {
        Div({
            style {
                padding(20.px)
                textAlign("center")
            }
        }) {
            Text("Anomaly Detection feature is not enabled")
        }
        return
    }
    
    val themeMode = LocalThemeMode.current
    val viewModel = remember { AnomalyViewModel() }
    
    LaunchedEffect(Unit) {
        viewModel.loadUnresolvedAlerts()
    }
    
    Div({
        style {
            padding(20.px)
        }
    }) {
        // Header
        Div({
            style {
                display(DisplayStyle.Flex)
                justifyContent(JustifyContent.SpaceBetween)
                alignItems(AlignItems.Center)
                marginBottom(30.px)
            }
        }) {
            Div {
                H1({
                    style {
                        fontSize(28.px)
                        fontWeight("bold")
                        color(FlagentTheme.text(themeMode))
                        margin(0.px)
                    }
                }) {
                    Text("🚨 Anomaly Alerts")
                }
                P({
                    style {
                        color(FlagentTheme.textLight(themeMode))
                        fontSize(14.px)
                        marginTop(5.px)
                    }
                }) {
                    Text("Monitor and resolve anomaly alerts across all flags")
                }
            }
            
            Button({
                style {
                    padding(10.px, 20.px)
                    backgroundColor(FlagentTheme.Primary)
                    color(Color.white)
                    border(0.px)
                    borderRadius(6.px)
                    cursor("pointer")
                    fontSize(14.px)
                    fontWeight("500")
                }
                onClick {
                    viewModel.loadUnresolvedAlerts()
                }
            }) {
                Icon("refresh", size = 16.px, color = FlagentTheme.Background)
                Text(" Refresh")
            }
        }
        
        if (viewModel.isLoading) {
            Div({
                style {
                    textAlign("center")
                    padding(40.px)
                }
            }) {
                Text("Loading...")
            }
        } else if (viewModel.error != null) {
            Div({
                style {
                    padding(20.px)
                    backgroundColor(FlagentTheme.errorBg(themeMode))
                    borderRadius(8.px)
                    color(FlagentTheme.errorText(themeMode))
                }
            }) {
                Text(viewModel.error!!)
            }
        } else if (viewModel.alerts.isEmpty()) {
            Div({
                style {
                    padding(40.px)
                    textAlign("center")
                    backgroundColor(FlagentTheme.inputBg(themeMode))
                    borderRadius(8.px)
                }
            }) {
                Icon("check_circle", size = 48.px, color = Color("#10B981"))
                P({
                    style {
                        fontSize(18.px)
                        fontWeight("500")
                        marginTop(15.px)
                        color(FlagentTheme.text(themeMode))
                    }
                }) {
                    Text("No unresolved alerts")
                }
                P({
                    style {
                        fontSize(14.px)
                        color(FlagentTheme.textLight(themeMode))
                    }
                }) {
                    Text("All anomalies have been resolved")
                }
            }
        } else {
            Div({
                style {
                    display(DisplayStyle.Flex)
                    property("flex-direction", "column")
                    gap(15.px)
                }
            }) {
                viewModel.alerts.forEach { alert ->
                    Div({
                        style {
                            backgroundColor(FlagentTheme.cardBg(themeMode))
                            borderRadius(8.px)
                            padding(20.px)
                            property("box-shadow", "0 2px 8px rgba(0,0,0,0.1)")
                            property("transition", "transform 0.2s")
                        }
                        onMouseEnter {
                            (it.target as org.w3c.dom.HTMLElement).style.transform = "translateX(4px)"
                        }
                        onMouseLeave {
                            (it.target as org.w3c.dom.HTMLElement).style.transform = "translateX(0)"
                        }
                    }) {
                        Div({
                            style {
                                display(DisplayStyle.Flex)
                                justifyContent(JustifyContent.SpaceBetween)
                                alignItems(AlignItems.FlexStart)
                            }
                        }) {
                            Div {
                                // Severity badge
                                Span({
                                    style {
                                        display(DisplayStyle.InlineBlock)
                                        padding(4.px, 12.px)
                                        borderRadius(12.px)
                                        fontSize(12.px)
                                        fontWeight("600")
                                        marginBottom(10.px)
                                        val severityColor = when (alert.severity) {
                                            flagent.frontend.api.AlertSeverity.CRITICAL -> Color("#DC2626")
                                            flagent.frontend.api.AlertSeverity.HIGH -> Color("#EA580C")
                                            flagent.frontend.api.AlertSeverity.MEDIUM -> Color("#F59E0B")
                                            flagent.frontend.api.AlertSeverity.LOW -> Color("#10B981")
                                        }
                                        backgroundColor(severityColor)
                                        color(Color.white)
                                    }
                                }) {
                                    Text(alert.severity.name)
                                }
                                
                                // Flag link
                                A(href = "/flags/${alert.flagId}", attrs = {
                                    style {
                                        fontSize(18.px)
                                        fontWeight("600")
                                        color(FlagentTheme.Primary)
                                        textDecoration("none")
                                        display(DisplayStyle.Block)
                                        marginBottom(8.px)
                                    }
                                }) {
                                    Text("Flag #${alert.flagId}")
                                }
                                
                                // Message
                                P({
                                    style {
                                        fontSize(14.px)
                                        color(FlagentTheme.textLight(themeMode))
                                        margin(0.px)
                                        marginBottom(8.px)
                                    }
                                }) {
                                    Text(alert.message)
                                }
                                
                                // Metric type
                                Div({
                                    style {
                                        fontSize(12.px)
                                        color(FlagentTheme.textLight(themeMode))
                                    }
                                }) {
                                    Text("Metric: ${alert.metricType} • Detected: ${formatTimestamp(alert.createdAt)}")
                                }
                            }
                            
                            // Resolve button
                            Button({
                                style {
                                    padding(8.px, 16.px)
                                    backgroundColor(Color("#10B981"))
                                    color(Color.white)
                                    border(0.px)
                                    borderRadius(6.px)
                                    cursor("pointer")
                                    fontSize(14.px)
                                    fontWeight("500")
                                }
                                onClick {
                                    viewModel.resolveAlert(alert.id) {
                                        // Success callback handled by viewModel
                                    }
                                }
                            }) {
                                Icon("check", size = 16.px, color = FlagentTheme.Background)
                                Text(" Resolve")
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    // Simple timestamp formatting for JS
    val date = kotlin.js.Date(timestamp.toDouble())
    return date.toISOString().substring(0, 19).replace("T", " ")
}
