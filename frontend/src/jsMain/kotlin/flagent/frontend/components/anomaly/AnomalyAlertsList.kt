package flagent.frontend.components.anomaly

import androidx.compose.runtime.*
import flagent.frontend.api.AlertSeverity
import flagent.frontend.components.Icon
import flagent.frontend.components.common.EmptyState
import flagent.frontend.components.common.SkeletonLoader
import flagent.frontend.state.LocalThemeMode
import flagent.frontend.state.ThemeMode
import flagent.frontend.theme.FlagentTheme
import flagent.frontend.util.format
import flagent.frontend.viewmodel.AnomalyViewModel
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

/**
 * Anomaly Alerts List component (Phase 3)
 */
@Composable
fun AnomalyAlertsList(flagId: Int? = null) {
    val themeMode = LocalThemeMode.current
    val viewModel = remember { AnomalyViewModel(flagId) }
    
    LaunchedEffect(flagId) {
        viewModel.loadAlerts()
    }
    
    Div({
        style {
            backgroundColor(FlagentTheme.cardBg(themeMode))
            borderRadius(8.px)
            padding(24.px)
            property("box-shadow", "0 1px 3px ${FlagentTheme.Shadow}")
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
            Text("Anomaly Alerts")
        }
        
        // Loading state
        if (viewModel.isLoading) {
            SkeletonLoader(height = 200.px)
        } else if (viewModel.alerts.isEmpty()) {
            EmptyState(
                icon = "check_circle",
                title = "No anomalies detected",
                description = "Your flags are performing as expected"
            )
        } else {
            // Alerts list
            viewModel.alerts.forEach { alert ->
                AlertCard(alert, viewModel)
            }
        }
        
        // Error state
        viewModel.error?.let { error ->
            Div({
                style {
                    marginTop(16.px)
                    padding(12.px)
                    backgroundColor(FlagentTheme.errorBg(themeMode))
                    borderRadius(6.px)
                    color(FlagentTheme.errorText(themeMode))
                    fontSize(14.px)
                }
            }) {
                Text(error)
            }
        }
    }
}

@Composable
private fun AlertCard(alert: flagent.frontend.api.AnomalyAlertResponse, viewModel: AnomalyViewModel) {
    val themeMode = LocalThemeMode.current
    Div({
        style {
            padding(16.px)
            border(1.px, LineStyle.Solid, getSeverityColor(alert.severity, themeMode))
            backgroundColor(getSeverityBgColor(alert.severity, themeMode))
            borderRadius(6.px)
            marginBottom(12.px)
            opacity(if (alert.resolved) 0.6 else 1.0)
        }
    }) {
        Div({
            style {
                display(DisplayStyle.Flex)
                justifyContent(JustifyContent.SpaceBetween)
                alignItems(AlignItems.FlexStart)
                marginBottom(12.px)
            }
        }) {
            Div {
                Div({
                    style {
                        display(DisplayStyle.Flex)
                        alignItems(AlignItems.Center)
                        gap(8.px)
                        marginBottom(8.px)
                    }
                }) {
                    Icon("warning", size = 20.px, color = getSeverityColor(alert.severity, themeMode))
                    Span({
                        style {
                            padding(4.px, 8.px)
                            backgroundColor(getSeverityColor(alert.severity, themeMode))
                            color(Color.white)
                            borderRadius(4.px)
                            fontSize(12.px)
                            fontWeight(600)
                        }
                    }) {
                        Text(alert.severity.name)
                    }
                    if (alert.resolved) {
                        Span({
                            style {
                                padding(4.px, 8.px)
                                backgroundColor(FlagentTheme.Success)
                                color(Color.white)
                                borderRadius(4.px)
                                fontSize(12.px)
                                fontWeight(500)
                            }
                        }) {
                            Text("Resolved")
                        }
                    }
                }
                
                P({
                    style {
                        fontSize(14.px)
                        fontWeight(600)
                        color(FlagentTheme.text(themeMode))
                        margin(0.px)
                        marginBottom(4.px)
                    }
                }) {
                    Text(alert.message)
                }
                
                P({
                    style {
                        fontSize(12.px)
                        color(FlagentTheme.textLight(themeMode))
                        margin(0.px)
                    }
                }) {
                    Text("Flag #${alert.flagId} | ${alert.metricType} | Value: ${alert.actualValue.format(2)}")
                }
            }
            
            if (!alert.resolved) {
                Button({
                    onClick { viewModel.resolveAlert(alert.id) }
                    style {
                        padding(8.px, 16.px)
                        backgroundColor(FlagentTheme.Primary)
                        color(Color.white)
                        border(0.px)
                        borderRadius(6.px)
                        cursor("pointer")
                        fontSize(12.px)
                    }
                }) {
                    Text("Resolve")
                }
            }
        }
    }
}

private fun getSeverityColor(severity: AlertSeverity, themeMode: ThemeMode): CSSColorValue {
    return when (severity) {
        AlertSeverity.LOW -> FlagentTheme.Info
        AlertSeverity.MEDIUM -> FlagentTheme.Warning
        AlertSeverity.HIGH -> FlagentTheme.Error
        AlertSeverity.CRITICAL -> FlagentTheme.errorText(themeMode)
    }
}

private fun getSeverityBgColor(severity: AlertSeverity, themeMode: ThemeMode): CSSColorValue {
    return when (themeMode) {
        ThemeMode.Dark -> when (severity) {
            AlertSeverity.LOW -> FlagentTheme.badgeBg(themeMode)
            AlertSeverity.MEDIUM -> FlagentTheme.warningBg(themeMode)
            AlertSeverity.HIGH -> FlagentTheme.errorBg(themeMode)
            AlertSeverity.CRITICAL -> FlagentTheme.errorBg(themeMode)
        }
        ThemeMode.Light -> when (severity) {
            AlertSeverity.LOW -> FlagentTheme.badgeBg(themeMode)
            AlertSeverity.MEDIUM -> FlagentTheme.warningBg(themeMode)
            AlertSeverity.HIGH -> FlagentTheme.errorBg(themeMode)
            AlertSeverity.CRITICAL -> FlagentTheme.errorBg(themeMode)
        }
    }
}
