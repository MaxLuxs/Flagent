package flagent.frontend.components

import androidx.compose.runtime.*
import flagent.frontend.api.ApiClient
import flagent.frontend.api.GlobalMetricsOverviewResponse
import flagent.frontend.components.metrics.OverviewChart
import flagent.frontend.config.AppConfig
import flagent.frontend.navigation.Route
import flagent.frontend.navigation.Router
import flagent.frontend.theme.FlagentTheme
import flagent.frontend.util.AppLogger
import flagent.frontend.util.ErrorHandler
import flagent.frontend.viewmodel.AnomalyViewModel
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

/**
 * Dashboard - главная страница с общей статистикой
 */
@Composable
fun Dashboard() {
    val scope = rememberCoroutineScope()
    var stats by remember { mutableStateOf<DashboardStats?>(null) }
    var overview by remember { mutableStateOf<GlobalMetricsOverviewResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    // Load anomaly alerts if feature enabled
    val anomalyViewModel = if (AppConfig.Features.enableAnomalyDetection) {
        remember { AnomalyViewModel() }
    } else null
    
    LaunchedEffect(Unit) {
        isLoading = true
        error = null
        
        ErrorHandler.withErrorHandling(
            block = {
                // Load dashboard stats
                val flags = ApiClient.getFlags()
                stats = DashboardStats(
                    totalFlags = flags.size,
                    enabledFlags = flags.count { it.enabled },
                    disabledFlags = flags.count { !it.enabled },
                    flagsWithSegments = flags.count { it.segments.isNotEmpty() },
                    flagsWithVariants = flags.count { it.variants.isNotEmpty() }
                )
                
                // Load unresolved alerts
                anomalyViewModel?.loadUnresolvedAlerts()
                // Load global metrics overview when metrics feature enabled
                if (AppConfig.Features.enableMetrics) {
                    try {
                        overview = ApiClient.getMetricsOverview(bucketMinutes = 60, topFlagsLimit = 10)
                    } catch (_: Throwable) {
                        overview = null
                    }
                }
            },
            onError = { err ->
                error = ErrorHandler.getUserMessage(err)
                AppLogger.error("Dashboard", "Failed to load dashboard", err.cause)
            }
        )
        
        isLoading = false
    }
    
    Div({
        style {
            padding(20.px)
        }
    }) {
        // Header
        Div({
            style {
                marginBottom(30.px)
            }
        }) {
            H1({
                style {
                    fontSize(28.px)
                    fontWeight("bold")
                    color(FlagentTheme.Text)
                    margin(0.px)
                }
            }) {
                Text("Dashboard")
            }
            P({
                style {
                    color(FlagentTheme.TextLight)
                    fontSize(14.px)
                    marginTop(5.px)
                }
            }) {
                Text(flagent.frontend.i18n.LocalizedStrings.dashboardOverview)
            }
        }
        
        if (isLoading) {
            Div({
                style {
                    textAlign("center")
                    padding(40.px)
                }
            }) {
                Text(flagent.frontend.i18n.LocalizedStrings.loading)
            }
        } else if (error != null) {
            Div({
                style {
                    padding(20.px)
                    backgroundColor(Color("#FEE2E2"))
                    borderRadius(8.px)
                    color(Color("#DC2626"))
                }
            }) {
                Text(error!!)
            }
        } else if (stats != null) {
            // Stats Cards
            Div({
                style {
                    display(DisplayStyle.Grid)
                    property("grid-template-columns", "repeat(auto-fit, minmax(250px, 1fr))")
                    gap(20.px)
                    marginBottom(30.px)
                }
            }) {
                StatCard("Total Flags", stats!!.totalFlags.toString(), "flag", FlagentTheme.Primary)
                StatCard("Enabled", stats!!.enabledFlags.toString(), "check_circle", Color("#10B981"))
                StatCard("Disabled", stats!!.disabledFlags.toString(), "cancel", Color("#EF4444"))
                StatCard("With Segments", stats!!.flagsWithSegments.toString(), "dashboard", FlagentTheme.Secondary)
                StatCard("Experiments (A/B)", stats!!.flagsWithVariants.toString(), "science", FlagentTheme.Secondary)
            }

            // Quick links: Experiments, Analytics
            val experimentsCount = stats!!.flagsWithVariants
            Div({
                style {
                    display(DisplayStyle.Grid)
                    property("grid-template-columns", "repeat(auto-fit, minmax(200px, 1fr))")
                    gap(20.px)
                    marginBottom(30.px)
                }
            }) {
                QuickLinkCard(
                    title = flagent.frontend.i18n.LocalizedStrings.experimentsTitle,
                    value = experimentsCount.toString(),
                    icon = "science",
                    color = FlagentTheme.Secondary,
                    onClick = { Router.navigateTo(Route.Experiments) }
                )
                QuickLinkCard(
                    title = flagent.frontend.i18n.LocalizedStrings.analyticsTitle,
                    value = flagent.frontend.i18n.LocalizedStrings.viewMetrics,
                    icon = "analytics",
                    color = FlagentTheme.Primary,
                    onClick = { Router.navigateTo(Route.Analytics) }
                )
            }

            // Global metrics chart (evaluations over time)
            if (AppConfig.Features.enableMetrics && overview != null) {
                Div({
                    style {
                        backgroundColor(FlagentTheme.Background)
                        borderRadius(8.px)
                        padding(20.px)
                        property("box-shadow", "0 2px 8px rgba(0,0,0,0.1)")
                        marginBottom(30.px)
                    }
                }) {
                    H2({
                        style {
                            fontSize(18.px)
                            fontWeight("600")
                            marginBottom(16.px)
                            color(FlagentTheme.Text)
                        }
                    }) {
                        Text(flagent.frontend.i18n.LocalizedStrings.evaluationsOverTime)
                    }
                    if (overview!!.timeSeries.isNotEmpty()) {
                        OverviewChart(
                            timeSeries = overview!!.timeSeries,
                            title = flagent.frontend.i18n.LocalizedStrings.evaluationsOverTime
                        )
                        if (overview!!.topFlags.isNotEmpty()) {
                            Div({
                                style {
                                    marginTop(16.px)
                                    paddingTop(16.px)
                                    property("border-top", "1px solid ${FlagentTheme.Border}")
                                }
                            }) {
                                Span({
                                    style {
                                        fontSize(13.px)
                                        color(FlagentTheme.TextLight)
                                        fontWeight("600")
                                    }
                                }) {
                                    Text(flagent.frontend.i18n.LocalizedStrings.topFlagsByEvaluations)
                                }
                                Div({
                                    style {
                                        display(DisplayStyle.Flex)
                                        flexWrap(FlexWrap.Wrap)
                                        gap(8.px)
                                        marginTop(8.px)
                                    }
                                }) {
                                    overview!!.topFlags.take(5).forEach { tf ->
                                        A(href = Route.FlagMetrics(tf.flagId).path(), attrs = {
                                            style {
                                                fontSize(12.px)
                                                padding(6.px, 10.px)
                                                backgroundColor(FlagentTheme.BackgroundAlt)
                                                borderRadius(6.px)
                                                color(FlagentTheme.Primary)
                                                textDecoration("none")
                                            }
                                            onClick { e ->
                                                e.preventDefault()
                                                Router.navigateTo(Route.FlagMetrics(tf.flagId))
                                            }
                                        }) {
                                            Text("${tf.flagKey}: ${tf.count}")
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        P({
                            style {
                                color(FlagentTheme.TextLight)
                                fontSize(14.px)
                            }
                        }) {
                            Text(flagent.frontend.i18n.LocalizedStrings.noMetricsData)
                        }
                    }
                }
            }
            
            // Unresolved Alerts
            if (AppConfig.Features.enableAnomalyDetection && anomalyViewModel != null) {
                if (anomalyViewModel.alerts.isNotEmpty()) {
                    Div({
                        style {
                            backgroundColor(FlagentTheme.Background)
                            borderRadius(8.px)
                            padding(20.px)
                            property("box-shadow", "0 2px 8px rgba(0,0,0,0.1)")
                            marginBottom(20.px)
                        }
                    }) {
                        Div({
                            style {
                                display(DisplayStyle.Flex)
                                justifyContent(JustifyContent.SpaceBetween)
                                alignItems(AlignItems.Center)
                                marginBottom(15.px)
                            }
                        }) {
                            H2({
                                style {
                                    fontSize(20.px)
                                    fontWeight("600")
                                    margin(0.px)
                                }
                            }) {
                                Text("🚨 Unresolved Alerts")
                            }
                            Button({
                                style {
                                    padding(8.px, 16.px)
                                    backgroundColor(FlagentTheme.Primary)
                                    color(FlagentTheme.Background)
                                    border(0.px)
                                    borderRadius(6.px)
                                    cursor("pointer")
                                    fontSize(14.px)
                                }
                                onClick {
                                    Router.navigateTo(Route.Alerts)
                                }
                            }) {
                                Text("View All")
                            }
                        }
                        
                        anomalyViewModel.alerts.take(5).forEach { alert ->
                            Div({
                                style {
                                    padding(12.px)
                                    backgroundColor(FlagentTheme.BackgroundAlt)
                                    borderRadius(6.px)
                                    marginBottom(10.px)
                                }
                            }) {
                                Div({
                                    style {
                                        fontWeight("500")
                                        marginBottom(5.px)
                                    }
                                }) {
                                    Text("Flag #${alert.flagId}: ${alert.severity}")
                                }
                                Div({
                                    style {
                                        fontSize(14.px)
                                        color(FlagentTheme.TextLight)
                                    }
                                }) {
                                    Text(alert.message)
                                }
                            }
                        }
                    }
                }
            }
            
            // Quick Actions
            Div({
                style {
                    backgroundColor(FlagentTheme.Background)
                    borderRadius(8.px)
                    padding(20.px)
                    property("box-shadow", "0 2px 8px rgba(0,0,0,0.1)")
                }
            }) {
                H2({
                    style {
                        fontSize(20.px)
                        fontWeight("600")
                        marginBottom(15.px)
                    }
                }) {
                    Text(flagent.frontend.i18n.LocalizedStrings.quickAccess)
                }

                Div({
                    style {
                        display(DisplayStyle.Grid)
                        property("grid-template-columns", "repeat(auto-fit, minmax(200px, 1fr))")
                        gap(15.px)
                    }
                }) {
                    QuickActionButton("Create Flag", "add_circle") {
                        Router.navigateTo(Route.CreateFlag)
                    }
                    QuickActionButton("View Flags", "flag") {
                        Router.navigateTo(Route.Home)
                    }
                    QuickActionButton(flagent.frontend.i18n.LocalizedStrings.experimentsTitle, "science") {
                        Router.navigateTo(Route.Experiments)
                    }
                    QuickActionButton(flagent.frontend.i18n.LocalizedStrings.analyticsTitle, "analytics") {
                        Router.navigateTo(Route.Analytics)
                    }
                    QuickActionButton("Debug Console", "bug_report") {
                        Router.navigateTo(Route.DebugConsole())
                    }
                    if (AppConfig.Features.enableMultiTenancy) {
                        QuickActionButton("Manage Tenants", "business") {
                            Router.navigateTo(Route.Tenants)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(title: String, value: String, icon: String, color: CSSColorValue) {
    Div({
        style {
            backgroundColor(FlagentTheme.Background)
            borderRadius(8.px)
            padding(20.px)
            property("box-shadow", "0 2px 8px rgba(0,0,0,0.1)")
            property("transition", "transform 0.2s, box-shadow 0.2s")
        }
        onMouseEnter {
            (it.target as org.w3c.dom.HTMLElement).style.transform = "translateY(-4px)"
            (it.target as org.w3c.dom.HTMLElement).style.boxShadow = "0 4px 16px rgba(0,0,0,0.15)"
        }
        onMouseLeave {
            (it.target as org.w3c.dom.HTMLElement).style.transform = "translateY(0)"
            (it.target as org.w3c.dom.HTMLElement).style.boxShadow = "0 2px 8px rgba(0,0,0,0.1)"
        }
    }) {
        Div({
            style {
                display(DisplayStyle.Flex)
                alignItems(AlignItems.Center)
                gap(10.px)
                marginBottom(10.px)
            }
        }) {
            Icon(icon, size = 24.px, color = color)
            Div({
                style {
                    fontSize(14.px)
                    color(FlagentTheme.TextLight)
                    fontWeight("500")
                }
            }) {
                Text(title)
            }
        }
        Div({
            style {
                fontSize(32.px)
                fontWeight("bold")
                color(color)
            }
        }) {
            Text(value)
        }
    }
}

@Composable
private fun QuickLinkCard(
    title: String,
    value: String,
    icon: String,
    color: CSSColorValue,
    onClick: () -> Unit
) {
    Button({
        style {
            display(DisplayStyle.Block)
            textAlign("left")
            backgroundColor(FlagentTheme.Background)
            border(1.px, LineStyle.Solid, FlagentTheme.Border)
            borderRadius(8.px)
            padding(20.px)
            property("box-shadow", "0 2px 8px rgba(0,0,0,0.1)")
            property("transition", "transform 0.2s, box-shadow 0.2s")
            cursor("pointer")
            width(100.percent)
        }
        onMouseEnter {
            (it.target as org.w3c.dom.HTMLElement).style.transform = "translateY(-2px)"
            (it.target as org.w3c.dom.HTMLElement).style.boxShadow = "0 4px 12px rgba(0,0,0,0.12)"
        }
        onMouseLeave {
            (it.target as org.w3c.dom.HTMLElement).style.transform = "translateY(0)"
            (it.target as org.w3c.dom.HTMLElement).style.boxShadow = "0 2px 8px rgba(0,0,0,0.1)"
        }
        onClick { onClick() }
    }) {
        Div({
            style {
                display(DisplayStyle.Flex)
                alignItems(AlignItems.Center)
                gap(10.px)
                marginBottom(10.px)
            }
        }) {
            Icon(icon, size = 24.px, color = color)
            Span({
                style {
                    fontSize(14.px)
                    color(FlagentTheme.TextLight)
                    fontWeight("500")
                }
            }) {
                Text(title)
            }
        }
        Div({
            style {
                fontSize(20.px)
                fontWeight("bold")
                color(color)
            }
        }) {
            Text(value)
        }
    }
}

@Composable
private fun QuickActionButton(label: String, icon: String, onClick: () -> Unit) {
    Button({
        style {
            display(DisplayStyle.Flex)
            alignItems(AlignItems.Center)
            gap(8.px)
            padding(12.px, 16.px)
            backgroundColor(FlagentTheme.BackgroundAlt)
            border(1.px, LineStyle.Solid, FlagentTheme.Border)
            borderRadius(6.px)
            cursor("pointer")
            fontSize(14.px)
            fontWeight("500")
            property("transition", "all 0.2s")
            width(100.percent)
        }
        onMouseEnter {
            (it.target as org.w3c.dom.HTMLElement).style.backgroundColor = FlagentTheme.Primary.toString()
            (it.target as org.w3c.dom.HTMLElement).style.color = FlagentTheme.Background.toString()
        }
        onMouseLeave {
            (it.target as org.w3c.dom.HTMLElement).style.backgroundColor = FlagentTheme.BackgroundAlt.toString()
            (it.target as org.w3c.dom.HTMLElement).style.color = FlagentTheme.Text.toString()
        }
        onClick { onClick() }
    }) {
        Icon(icon, size = 20.px)
        Text(label)
    }
}

private data class DashboardStats(
    val totalFlags: Int,
    val enabledFlags: Int,
    val disabledFlags: Int,
    val flagsWithSegments: Int,
    val flagsWithVariants: Int
)
