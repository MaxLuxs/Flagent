package flagent.frontend.components

import androidx.compose.runtime.*
import flagent.api.model.FlagResponse
import flagent.frontend.api.ApiClient
import flagent.frontend.api.GlobalMetricsOverviewResponse
import flagent.frontend.components.metrics.OverviewChart
import flagent.frontend.config.AppConfig
import flagent.frontend.navigation.Route
import flagent.frontend.navigation.Router
import flagent.frontend.state.BackendOnboardingState
import flagent.frontend.state.LocalThemeMode
import flagent.frontend.state.ThemeMode
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
    val themeMode = LocalThemeMode.current
    val scope = rememberCoroutineScope()
    var stats by remember { mutableStateOf<DashboardStats?>(null) }
    var flags by remember { mutableStateOf<List<FlagResponse>>(emptyList()) }
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
                val (loadedFlags, _) = ApiClient.getFlags()
                flags = loadedFlags
                stats = DashboardStats(
                    totalFlags = loadedFlags.size,
                    enabledFlags = loadedFlags.count { it.enabled },
                    disabledFlags = loadedFlags.count { !it.enabled },
                    flagsWithSegments = loadedFlags.count { it.segments.isNotEmpty() },
                    flagsWithVariants = loadedFlags.count { it.variants.isNotEmpty() }
                )
                
                // Load unresolved alerts
                anomalyViewModel?.loadUnresolvedAlerts()
                // Load global metrics overview when metrics feature enabled
                if (AppConfig.Features.enableMetrics) {
                    try {
                        overview = ApiClient.getMetricsOverview(topLimit = 10, timeBucketMs = 3600_000)
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
            padding(0.px)
        }
    }) {
        Div({
            style {
                marginBottom(16.px)
            }
        }) {
            H1({
                style {
                    fontSize(24.px)
                    fontWeight("bold")
                    color(FlagentTheme.text(themeMode))
                    margin(0.px)
                }
            }) {
                Text("Dashboard")
            }
            P({
                style {
                    color(FlagentTheme.textLight(themeMode))
                    fontSize(14.px)
                    marginTop(4.px)
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
            val isTenantOrApiKeyError = error!!.contains("tenant", ignoreCase = true) ||
                error!!.contains("Create tenant", ignoreCase = true) ||
                error!!.contains("X-API-Key", ignoreCase = true)
            if (isTenantOrApiKeyError) SideEffect { BackendOnboardingState.setBackendNeedsTenantOrAuth() }
            Div({
                style {
                    padding(20.px)
                    backgroundColor(Color("#FEE2E2"))
                    borderRadius(8.px)
                    color(Color("#DC2626"))
                }
            }) {
                Text(error!!)
                if (isTenantOrApiKeyError) {
                    P({
                        style {
                            marginTop(10.px)
                            fontSize(14.px)
                            display(DisplayStyle.Flex)
                            gap(12.px)
                            flexWrap(FlexWrap.Wrap)
                        }
                    }) {
                        Button({
                            style {
                                color(Color("#DC2626"))
                                textDecoration("underline")
                                fontWeight("600")
                                backgroundColor(Color("transparent"))
                                border(0.px)
                                cursor("pointer")
                                padding(0.px)
                                fontSize(14.px)
                            }
                            onClick { Router.navigateToTenantsWithCreate() }
                        }) {
                            Text("Create first tenant →")
                        }
                        Button({
                            style {
                                color(Color("#DC2626"))
                                textDecoration("underline")
                                fontWeight("600")
                                backgroundColor(Color("transparent"))
                                border(0.px)
                                cursor("pointer")
                                padding(0.px)
                                fontSize(14.px)
                            }
                            onClick { Router.navigateTo(Route.Login) }
                        }) {
                            Text("Log in (admin) →")
                        }
                    }
                }
            }
        } else if (stats != null) {
            // Stats Cards
            Div({
                style {
                    display(DisplayStyle.Grid)
                    property("grid-template-columns", "repeat(auto-fit, minmax(160px, 1fr))")
                    gap(16.px)
                    marginBottom(16.px)
                }
            }) {
                StatCard(themeMode, "Total Flags", stats!!.totalFlags.toString(), "flag", FlagentTheme.Primary)
                StatCard(themeMode, "Enabled", stats!!.enabledFlags.toString(), "check_circle", Color("#10B981"))
                StatCard(themeMode, "Disabled", stats!!.disabledFlags.toString(), "cancel", Color("#EF4444"))
                StatCard(themeMode, "With Segments", stats!!.flagsWithSegments.toString(), "dashboard", FlagentTheme.Secondary)
                StatCard(themeMode, "Experiments (A/B)", stats!!.flagsWithVariants.toString(), "science", FlagentTheme.Secondary)
            }

            // Quick links: Experiments, Analytics
            val experimentsCount = stats!!.flagsWithVariants
            Div({
                style {
                    display(DisplayStyle.Grid)
                    property("grid-template-columns", "repeat(auto-fit, minmax(160px, 1fr))")
                    gap(16.px)
                    marginBottom(16.px)
                }
            }) {
                QuickLinkCard(
                    themeMode,
                    title = flagent.frontend.i18n.LocalizedStrings.experimentsTitle,
                    value = experimentsCount.toString(),
                    icon = "science",
                    color = FlagentTheme.Secondary,
                    onClick = { Router.navigateTo(Route.Experiments) }
                )
                QuickLinkCard(
                    themeMode,
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
                        backgroundColor(FlagentTheme.cardBg(themeMode))
                        borderRadius(8.px)
                        padding(16.px)
                        property("border", "1px solid ${FlagentTheme.cardBorder(themeMode)}")
                        marginBottom(16.px)
                    }
                }) {
                    H2({
                        style {
                            fontSize(18.px)
                            fontWeight("600")
                            marginBottom(16.px)
                            color(FlagentTheme.text(themeMode))
                        }
                    }) {
                        Text(flagent.frontend.i18n.LocalizedStrings.evaluationsOverTime)
                    }
                    Div({
                        style {
                            display(DisplayStyle.Flex)
                            gap(24.px)
                            marginBottom(16.px)
                            flexWrap(FlexWrap.Wrap)
                        }
                    }) {
                        Span({ style { color(FlagentTheme.textLight(themeMode)); fontSize(14.px) } }) {
                            Text("Total: ${overview!!.totalEvaluations}")
                        }
                        Span({ style { color(FlagentTheme.textLight(themeMode)); fontSize(14.px) } }) {
                            Text("Unique flags: ${overview!!.uniqueFlags}")
                        }
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
                                    property("border-top", "1px solid ${FlagentTheme.cardBorder(themeMode)}")
                                }
                            }) {
                                Span({
                                    style {
                                        fontSize(13.px)
                                        color(FlagentTheme.textLight(themeMode))
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
                                                backgroundColor(FlagentTheme.inputBg(themeMode))
                                                borderRadius(6.px)
                                                color(FlagentTheme.Primary)
                                                textDecoration("none")
                                            }
                                            onClick { e ->
                                                e.preventDefault()
                                                Router.navigateTo(Route.FlagMetrics(tf.flagId))
                                            }
                                        }) {
                                            Text("${tf.flagKey}: ${tf.evaluationCount}")
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        P({
                            style {
                                color(FlagentTheme.textLight(themeMode))
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
                            backgroundColor(FlagentTheme.cardBg(themeMode))
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
                                    color(Color.white)
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
                                    backgroundColor(FlagentTheme.inputBg(themeMode))
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
                                        color(FlagentTheme.textLight(themeMode))
                                    }
                                }) {
                                    Text(alert.message)
                                }
                            }
                        }
                    }
                }
            }
            
            // Recent flags
            if (flags.isNotEmpty()) {
                val recentFlags = flags.sortedByDescending { it.updatedAt ?: "" }.take(5)
                Div({
                    style {
                        backgroundColor(FlagentTheme.cardBg(themeMode))
                        borderRadius(8.px)
                        padding(16.px)
                        property("border", "1px solid ${FlagentTheme.cardBorder(themeMode)}")
                        marginBottom(16.px)
                    }
                }) {
                    H2({
                        style {
                            fontSize(18.px)
                            fontWeight("600")
                            marginBottom(12.px)
                            color(FlagentTheme.text(themeMode))
                        }
                    }) {
                        Text(flagent.frontend.i18n.LocalizedStrings.recentFlags)
                    }
                    Table({
                        style {
                            width(100.percent)
                            property("border-collapse", "collapse")
                            fontSize(14.px)
                        }
                    }) {
                        Tbody({}) {
                            recentFlags.forEach { flag ->
                                Tr({
                                    style {
                                        property("border-bottom", "1px solid ${FlagentTheme.cardBorder(themeMode)}")
                                        cursor("pointer")
                                    }
                                    onClick { Router.navigateTo(Route.FlagDetail(flag.id)) }
                                }) {
                                    Td({
                                        style {
                                            padding(8.px, 12.px)
                                            color(FlagentTheme.Primary)
                                            fontWeight("500")
                                        }
                                    }) {
                                        Text(flag.key)
                                    }
                                    Td({
                                        style {
                                            padding(8.px, 12.px)
                                            color(FlagentTheme.textLight(themeMode))
                                            property("max-width", "200px")
                                            property("overflow", "hidden")
                                            property("text-overflow", "ellipsis")
                                            property("white-space", "nowrap")
                                        }
                                    }) {
                                        Text(flag.description?.let { d -> d.take(40) + if (d.length > 40) "…" else "" } ?: "—")
                                    }
                                    Td({
                                        style {
                                            padding(8.px, 12.px)
                                            color(FlagentTheme.textLight(themeMode))
                                            fontSize(12.px)
                                        }
                                    }) {
                                        Text(flag.updatedAt?.take(16) ?: "—")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Quick Actions
            Div({
                style {
                    backgroundColor(FlagentTheme.cardBg(themeMode))
                    borderRadius(8.px)
                    padding(16.px)
                    property("box-shadow", "0 1px 3px rgba(0,0,0,0.08)")
                }
            }) {
                H2({
                    style {
                        fontSize(18.px)
                        fontWeight("600")
                        marginBottom(12.px)
                    }
                }) {
                    Text(flagent.frontend.i18n.LocalizedStrings.quickAccess)
                }

                Div({
                    style {
                        display(DisplayStyle.Grid)
                        property("grid-template-columns", "repeat(auto-fit, minmax(140px, 1fr))")
                        gap(12.px)
                    }
                }) {
                    QuickActionButton(themeMode, "Create Flag", "add_circle") {
                        Router.navigateTo(Route.CreateFlag)
                    }
                    QuickActionButton(themeMode, "View Flags", "flag") {
                        Router.navigateTo(Route.FlagsList)
                    }
                    QuickActionButton(themeMode, flagent.frontend.i18n.LocalizedStrings.experimentsTitle, "science") {
                        Router.navigateTo(Route.Experiments)
                    }
                    QuickActionButton(themeMode, flagent.frontend.i18n.LocalizedStrings.analyticsTitle, "analytics") {
                        Router.navigateTo(Route.Analytics)
                    }
                    QuickActionButton(themeMode, "Debug Console", "bug_report") {
                        Router.navigateTo(Route.DebugConsole())
                    }
                    if (AppConfig.Features.enableMultiTenancy) {
                        QuickActionButton(themeMode, "Manage Tenants", "business") {
                            Router.navigateTo(Route.Tenants)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(themeMode: ThemeMode, title: String, value: String, icon: String, color: CSSColorValue) {
    Div({
        style {
            backgroundColor(FlagentTheme.cardBg(themeMode))
            borderRadius(8.px)
            padding(16.px)
            property("box-shadow", "0 1px 3px rgba(0,0,0,0.08)")
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
                    color(FlagentTheme.textLight(themeMode))
                    fontWeight("500")
                }
            }) {
                Text(title)
            }
        }
        Div({
            style {
                fontSize(24.px)
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
    themeMode: ThemeMode,
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
            backgroundColor(FlagentTheme.cardBg(themeMode))
            border(1.px, LineStyle.Solid, FlagentTheme.cardBorder(themeMode))
            borderRadius(8.px)
            padding(16.px)
            property("box-shadow", "0 1px 3px rgba(0,0,0,0.08)")
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
                    color(FlagentTheme.textLight(themeMode))
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
private fun QuickActionButton(themeMode: ThemeMode, label: String, icon: String, onClick: () -> Unit) {
    Button({
        style {
            display(DisplayStyle.Flex)
            alignItems(AlignItems.Center)
            gap(8.px)
            padding(12.px, 16.px)
            backgroundColor(FlagentTheme.inputBg(themeMode))
            border(1.px, LineStyle.Solid, FlagentTheme.cardBorder(themeMode))
            borderRadius(6.px)
            cursor("pointer")
            fontSize(14.px)
            fontWeight("500")
            property("transition", "all 0.2s")
            width(100.percent)
        }
        onMouseEnter {
            (it.target as org.w3c.dom.HTMLElement).style.backgroundColor = FlagentTheme.Primary.toString()
            (it.target as org.w3c.dom.HTMLElement).style.color = Color.white.toString()
        }
        onMouseLeave {
            (it.target as org.w3c.dom.HTMLElement).style.backgroundColor = FlagentTheme.inputBg(themeMode).toString()
            (it.target as org.w3c.dom.HTMLElement).style.color = FlagentTheme.text(themeMode).toString()
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
