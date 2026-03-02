package flagent.frontend.components

import androidx.compose.runtime.*
import flagent.api.model.FlagResponse
import flagent.frontend.api.ApiClient
import flagent.frontend.api.AlertSeverity
import flagent.frontend.api.CrashListResponse
import flagent.frontend.config.AppConfig
import flagent.frontend.api.GlobalMetricsOverviewResponse
import flagent.frontend.components.dashboard.ActivityChart
import flagent.frontend.components.dashboard.StatusDistributionChart
import flagent.frontend.components.dashboard.StatusSlice
import flagent.frontend.components.dashboard.TopFlagsBarChart
import flagent.frontend.i18n.LocalizedStrings
import flagent.frontend.navigation.Route
import flagent.frontend.navigation.Router
import flagent.frontend.state.BackendOnboardingState
import flagent.frontend.state.LocalThemeMode
import flagent.frontend.state.ThemeMode
import flagent.frontend.theme.FlagentTheme
import flagent.frontend.util.AppLogger
import flagent.frontend.util.ErrorHandler
import flagent.frontend.util.currentTimeMillis
import flagent.frontend.viewmodel.AnomalyViewModel
import flagent.frontend.viewmodel.TenantViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

/** Period for metrics charts: range and bucket size. */
private enum class MetricsPeriod(val rangeMs: Long, val bucketMs: Long) {
    P1H(3600_000, 300_000),
    P24H(86400_000, 3600_000),
    P7D(7 * 86400_000, 6 * 3600_000)
}

/**
 * Dashboard - главная страница с общей статистикой
 */
@Composable
fun Dashboard(tenantViewModel: TenantViewModel? = null) {
    val themeMode = LocalThemeMode.current
    var stats by remember { mutableStateOf<DashboardStats?>(null) }
    var flags by remember { mutableStateOf<List<FlagResponse>>(emptyList()) }
    var metricsOverview by remember { mutableStateOf<GlobalMetricsOverviewResponse?>(null) }
    var metricsLoadError by remember { mutableStateOf<String?>(null) }
    var metricsPeriod by remember { mutableStateOf(MetricsPeriod.P24H) }
    var metricsRefreshTrigger by remember { mutableStateOf(0) }
    var isMetricsLoading by remember { mutableStateOf(false) }
    var lastFlagsRefreshMs by remember { mutableStateOf<Long?>(null) }
    var lastMetricsRefreshMs by remember { mutableStateOf<Long?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var refreshTrigger by remember { mutableStateOf(0) }
    var crashOverview by remember { mutableStateOf<CrashListResponse?>(null) }

    val anomalyViewModel = if (AppConfig.Features.enableAnomalyDetection) {
        remember { AnomalyViewModel() }
    } else null

    LaunchedEffect(Unit, refreshTrigger) {
        isLoading = true
        error = null
        metricsLoadError = null
        ErrorHandler.withErrorHandling(
            block = {
                coroutineScope {
                    val flagsDeferred = async {
                        val (loadedFlags, _) = ApiClient.getFlags()
                        loadedFlags
                    }
                    val alertsDeferred = anomalyViewModel?.let { vm -> async { vm.loadUnresolvedAlerts() } }

                    val loadedFlags = flagsDeferred.await()
                    flags = loadedFlags
                    stats = DashboardStats(
                        totalFlags = loadedFlags.size,
                        enabledFlags = loadedFlags.count { it.enabled },
                        disabledFlags = loadedFlags.count { !it.enabled },
                        flagsWithSegments = loadedFlags.count { it.segments.isNotEmpty() },
                        flagsWithVariants = loadedFlags.count { it.variants.isNotEmpty() }
                    )
                    lastFlagsRefreshMs = currentTimeMillis()
                    alertsDeferred?.await()
                }
            },
            onError = { err ->
                error = ErrorHandler.getUserMessage(err)
                AppLogger.error("Dashboard", "Failed to load dashboard", err.cause)
            }
        )
        isLoading = false
    }

    LaunchedEffect(metricsPeriod, metricsRefreshTrigger) {
        if (!AppConfig.Features.enableMetrics) return@LaunchedEffect
        isMetricsLoading = true
        metricsLoadError = null
        try {
            val end = kotlin.js.Date().getTime().toLong()
            val start = end - metricsPeriod.rangeMs
            metricsOverview = ApiClient.getMetricsOverview(
                startTime = start,
                endTime = end,
                topLimit = 10,
                timeBucketMs = metricsPeriod.bucketMs
            )
            lastMetricsRefreshMs = currentTimeMillis()
        } catch (e: Throwable) {
            metricsLoadError = ErrorHandler.getUserMessage(ErrorHandler.handle(e, null))
            metricsOverview = null
        } finally {
            isMetricsLoading = false
        }
    }

    LaunchedEffect(Unit) {
        if (!AppConfig.Features.enableCrashAnalytics) return@LaunchedEffect
        try {
            val end = kotlin.js.Date().getTime().toLong()
            val start = end - 86400_000L
            crashOverview = ApiClient.getCrashes(startTime = start, endTime = end, limit = 5)
        } catch (_: Throwable) {
            crashOverview = null
        }
    }
    
    Div({
        style {
            padding(0.px)
        }
    }) {
        DashboardHeader(themeMode, tenantViewModel?.currentTenant?.name, lastFlagsRefreshMs)
        
        if (isLoading) {
            Div({
                style {
                    textAlign("center")
                    padding(40.px)
                    color(FlagentTheme.textLight(themeMode))
                }
            }) {
                Text(LocalizedStrings.loading)
            }
        } else if (error != null) {
            val isTenantOrApiKeyError = error!!.contains("tenant", ignoreCase = true) ||
                error!!.contains("Create tenant", ignoreCase = true) ||
                error!!.contains("X-API-Key", ignoreCase = true)
            if (isTenantOrApiKeyError) SideEffect { BackendOnboardingState.setBackendNeedsTenantOrAuth() }
            Div({
                style {
                    padding(20.px)
                    backgroundColor(FlagentTheme.errorBg(themeMode))
                    borderRadius(8.px)
                    color(FlagentTheme.errorText(themeMode))
                }
            }) {
                Text(error!!)
                P({
                    style {
                        marginTop(10.px)
                        fontSize(14.px)
                        display(DisplayStyle.Flex)
                        gap(12.px)
                        flexWrap(FlexWrap.Wrap)
                        alignItems(AlignItems.Center)
                    }
                }) {
                    Button({
                        style {
                            color(FlagentTheme.errorText(themeMode))
                            textDecoration("underline")
                            fontWeight("600")
                            backgroundColor(Color("transparent"))
                            border(0.px)
                            cursor("pointer")
                            padding(0.px)
                            fontSize(14.px)
                        }
                        onClick { refreshTrigger++ }
                    }) {
                        Text(LocalizedStrings.retry)
                    }
                    if (isTenantOrApiKeyError) {
                        Button({
                            style {
                                color(FlagentTheme.errorText(themeMode))
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
                            Text(LocalizedStrings.createFirstTenant)
                        }
                        Button({
                            style {
                                color(FlagentTheme.errorText(themeMode))
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
                            Text(LocalizedStrings.logInAdmin)
                        }
                    }
                }
            }
        } else if (stats != null) {
            if (stats!!.totalFlags == 0) {
                Div({
                    style {
                        textAlign("center")
                        padding(48.px, 24.px)
                        backgroundColor(FlagentTheme.cardBg(themeMode))
                        borderRadius(8.px)
                        property("border", "1px solid ${FlagentTheme.cardBorder(themeMode)}")
                    }
                }) {
                    P({
                        style {
                            fontSize(18.px)
                            color(FlagentTheme.text(themeMode))
                            marginBottom(16.px)
                        }
                    }) {
                        Text(LocalizedStrings.createFirstFlag)
                    }
                    Button({
                        style {
                            padding(12.px, 24.px)
                            backgroundColor(FlagentTheme.Primary)
                            color(Color.white)
                            border(0.px)
                            borderRadius(6.px)
                            cursor("pointer")
                            fontSize(14.px)
                            fontWeight("600")
                        }
                        onClick { Router.navigateTo(Route.CreateFlag) }
                    }) {
                        Text(LocalizedStrings.createFlag)
                    }
                }
            } else {
            val s = stats!!
            StatsRow(themeMode, s)
            QuickLinksRow(themeMode, s)

            // Metrics section: error message or charts
            if (AppConfig.Features.enableMetrics) {
                if (isMetricsLoading && metricsOverview == null && metricsLoadError == null) {
                    Div({
                        style {
                            padding(24.px)
                            textAlign("center")
                            color(FlagentTheme.textLight(themeMode))
                            fontSize(14.px)
                            marginBottom(16.px)
                        }
                    }) {
                        Text(LocalizedStrings.loading)
                    }
                } else if (metricsLoadError != null && metricsOverview == null) {
                    Div({
                        style {
                            padding(16.px)
                            backgroundColor(FlagentTheme.warningBg(themeMode))
                            borderRadius(8.px)
                            marginBottom(16.px)
                            property("border", "1px solid ${FlagentTheme.cardBorder(themeMode)}")
                            display(DisplayStyle.Flex)
                            flexDirection(FlexDirection.Column)
                            gap(8.px)
                        }
                    }) {
                        Text(LocalizedStrings.metricsUnavailable + ": " + metricsLoadError!!)
                        Button({
                            style {
                                padding(6.px, 12.px)
                                fontSize(14.px)
                                cursor("pointer")
                                alignSelf(AlignSelf.FlexStart)
                            }
                            onClick { metricsRefreshTrigger++ }
                        }) {
                            Text(LocalizedStrings.retry)
                        }
                    }
                } else if (metricsOverview != null) {
                val overview = metricsOverview!!
                if (overview.timeSeries.isNotEmpty() || overview.topFlags.isNotEmpty()) {
                    Div({
                        style {
                            backgroundColor(FlagentTheme.cardBg(themeMode))
                            borderRadius(8.px)
                            padding(16.px)
                            property("box-shadow", FlagentTheme.ShadowCard)
                            property("border", "1px solid ${FlagentTheme.cardBorder(themeMode)}")
                            marginBottom(20.px)
                        }
                    }) {
                        Div({
                            style {
                                display(DisplayStyle.Flex)
                                justifyContent(JustifyContent.SpaceBetween)
                                alignItems(AlignItems.Center)
                                marginBottom(12.px)
                                flexWrap(org.jetbrains.compose.web.css.FlexWrap.Wrap)
                                gap(8.px)
                            }
                        }) {
                            Span({
                                style {
                                    fontSize(14.px)
                                    color(FlagentTheme.textLight(themeMode))
                                }
                            }) {
                                Text("${LocalizedStrings.totalEvaluationsLabel}: ${overview.totalEvaluations} · ${LocalizedStrings.uniqueFlagsLabel}: ${overview.uniqueFlags}")
                                lastMetricsRefreshMs?.let { ts ->
                                    val minAgo = ((currentTimeMillis() - ts) / 60_000).toInt().coerceAtLeast(0)
                                    Text(" · ${LocalizedStrings.dataUpdatedMinutesAgo(minAgo)}")
                                }
                            }
                            Div({
                                style { display(DisplayStyle.Flex); gap(8.px) }
                            }) {
                                listOf(
                                    MetricsPeriod.P1H to LocalizedStrings.period1h,
                                    MetricsPeriod.P24H to LocalizedStrings.period24h,
                                    MetricsPeriod.P7D to LocalizedStrings.period7d
                                ).forEach { (period, label) ->
                                    Button({
                                        style {
                                            padding(6.px, 12.px)
                                            fontSize(12.px)
                                            borderRadius(6.px)
                                            border(1.px, LineStyle.Solid, FlagentTheme.cardBorder(themeMode))
                                            backgroundColor(if (metricsPeriod == period) FlagentTheme.Primary else FlagentTheme.inputBg(themeMode))
                                            color(if (metricsPeriod == period) Color.white else FlagentTheme.text(themeMode))
                                            cursor("pointer")
                                        }
                                        onClick { metricsPeriod = period }
                                    }) {
                                        Text(label)
                                    }
                                }
                            }
                        }
                        Div({
                            style {
                                display(DisplayStyle.Grid)
                                property("grid-template-columns", "repeat(auto-fit, minmax(320px, 1fr))")
                                gap(20.px)
                                alignItems(AlignItems.Start)
                            }
                        }) {
                            if (overview.timeSeries.isNotEmpty()) {
                                Div({ style { minHeight(280.px) } }) {
                                    ActivityChart(
                                        overview.timeSeries,
                                        LocalizedStrings.evaluationsOverTime,
                                        themeMode
                                    )
                                }
                            }
                            if (overview.topFlags.isNotEmpty()) {
                                Div({ style { minHeight(280.px) } }) {
                                    TopFlagsBarChart(
                                        overview.topFlags,
                                        LocalizedStrings.topFlagsByEvaluations,
                                        themeMode,
                                        maxBars = 8
                                    )
                                }
                            }
                        }
                    }
                }
                // Status distribution (from current flags stats)
                val statusSlices = listOf(
                    StatusSlice(LocalizedStrings.statusEnabledLabel, stats!!.enabledFlags, FlagentTheme.Success.toString()),
                    StatusSlice(LocalizedStrings.statusDisabledLabel, stats!!.disabledFlags, FlagentTheme.Error.toString()),
                    StatusSlice(LocalizedStrings.statusWithSegments, stats!!.flagsWithSegments, FlagentTheme.Primary.toString()),
                    StatusSlice(LocalizedStrings.statusExperiments, stats!!.flagsWithVariants, FlagentTheme.Secondary.toString())
                ).filter { it.count > 0 }
                if (statusSlices.isNotEmpty()) {
                    Div({
                        style {
                            backgroundColor(FlagentTheme.cardBg(themeMode))
                            borderRadius(8.px)
                            padding(16.px)
                            property("box-shadow", FlagentTheme.ShadowCard)
                            property("border", "1px solid ${FlagentTheme.cardBorder(themeMode)}")
                            marginBottom(20.px)
                            property("max-width", "400px")
                        }
                    }) {
                        StatusDistributionChart(
                            statusSlices,
                            LocalizedStrings.statusDistribution,
                            themeMode
                        )
                    }
                }
                }
            }

            // Top Flags (from metrics)
            if (AppConfig.Features.enableMetrics && metricsOverview?.topFlags?.isNotEmpty() == true) {
                val topFlagsList = metricsOverview!!.topFlags.take(5)
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
                        Text(LocalizedStrings.topFlags)
                    }
                    Div({
                        style {
                            display(DisplayStyle.Flex)
                            flexDirection(FlexDirection.Column)
                            gap(8.px)
                        }
                    }) {
                        topFlagsList.forEach { tf ->
                            val flagEnabled = flags.find { it.id == tf.flagId }?.enabled
                            Div({
                                style {
                                    display(DisplayStyle.Flex)
                                    justifyContent(JustifyContent.SpaceBetween)
                                    alignItems(AlignItems.Center)
                                    padding(8.px, 12.px)
                                    backgroundColor(FlagentTheme.inputBg(themeMode))
                                    borderRadius(6.px)
                                    cursor("pointer")
                                }
                                onClick { Router.navigateTo(Route.FlagDetail(tf.flagId)) }
                                attr("aria-label", "${tf.flagKey.ifBlank { LocalizedStrings.flagNumber(tf.flagId) }}: ${tf.evaluationCount} evaluations, ${if (flagEnabled == true) "enabled" else "disabled"}")
                            }) {
                                Span({
                                    style {
                                        display(DisplayStyle.Flex)
                                        alignItems(AlignItems.Center)
                                        gap(8.px)
                                    }
                                }) {
                                    Span({
                                        style {
                                            width(8.px)
                                            height(8.px)
                                            borderRadius(50.percent)
                                            backgroundColor(if (flagEnabled == true) FlagentTheme.Success else FlagentTheme.textLight(themeMode))
                                            property("flex-shrink", "0")
                                        }
                                    }) {}
                                    Span({
                                        style {
                                            color(FlagentTheme.Primary)
                                            fontWeight("500")
                                        }
                                    }) {
                                        Text(tf.flagKey.ifBlank { LocalizedStrings.flagNumber(tf.flagId) })
                                    }
                                }
                                Span({
                                    style {
                                        fontSize(12.px)
                                        color(FlagentTheme.textLight(themeMode))
                                    }
                                }) {
                                    Text("${tf.evaluationCount} ${LocalizedStrings.evaluations}")
                                }
                            }
                        }
                    }
                }
            }

            HealthStatusCard(
                themeMode = themeMode,
                stats = s,
                metricsOverview = metricsOverview,
                anomalyViewModel = anomalyViewModel,
                enableMetrics = AppConfig.Features.enableMetrics,
                enableAnomalyDetection = AppConfig.Features.enableAnomalyDetection
            )

            if (AppConfig.Features.enableCrashAnalytics) {
                CrashOverviewSection(themeMode, crashOverview)
            }

            // Unresolved Alerts
            if (AppConfig.Features.enableAnomalyDetection && anomalyViewModel != null) {
                if (anomalyViewModel.alerts.isNotEmpty()) {
                    Div({
                        style {
                            backgroundColor(FlagentTheme.cardBg(themeMode))
                            borderRadius(8.px)
                            padding(20.px)
                            property("box-shadow", FlagentTheme.ShadowCard)
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
                                    color(FlagentTheme.text(themeMode))
                                }
                            }) {
                                Text("🚨 ${LocalizedStrings.unresolvedAlerts}")
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
                                Text(LocalizedStrings.viewAll)
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
                                    Text("${LocalizedStrings.flagNumber(alert.flagId)}: ${alert.severity}")
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
            
            // Recently updated flags
            if (flags.isNotEmpty()) {
                val recentFlags = flags.sortedByDescending { f ->
                    f.updatedAt?.let { kotlin.js.Date(it).getTime().toLong() } ?: 0L
                }.take(5)
                Div({
                    style {
                        backgroundColor(FlagentTheme.cardBg(themeMode))
                        borderRadius(8.px)
                        padding(16.px)
                        property("border", "1px solid ${FlagentTheme.cardBorder(themeMode)}")
                        marginBottom(16.px)
                    }
                }) {
                    Div({
                        style {
                            display(DisplayStyle.Flex)
                            justifyContent(JustifyContent.SpaceBetween)
                            alignItems(AlignItems.Center)
                            marginBottom(12.px)
                        }
                    }) {
                        H2({
                            style {
                                fontSize(18.px)
                                fontWeight("600")
                                margin(0.px)
                                color(FlagentTheme.text(themeMode))
                            }
                        }) {
                            Text(LocalizedStrings.recentlyUpdatedFlags)
                        }
                        Button({
                            style {
                                padding(4.px, 8.px)
                                fontSize(12.px)
                                color(FlagentTheme.Primary)
                                backgroundColor(Color.transparent)
                                border(0.px)
                                cursor("pointer")
                                textDecoration("underline")
                            }
                            onClick { Router.navigateTo(Route.FlagsList) }
                        }) {
                            Text(LocalizedStrings.viewAllFlags)
                        }
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
                                        Text(flag.description.let { d -> if (d.length > 40) d.take(40) + "…" else d })
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

            QuickActionsSection(themeMode)
            }
        }
    }
}

@Composable
private fun DashboardHeader(themeMode: ThemeMode, tenantName: String?, lastFlagsRefreshMs: Long?) {
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
            Text(LocalizedStrings.dashboardNav)
        }
        P({
            style {
                color(FlagentTheme.textLight(themeMode))
                fontSize(14.px)
                marginTop(4.px)
            }
        }) {
            Text(LocalizedStrings.dashboardOverview)
        }
        if (tenantName != null) {
            P({
                style {
                    color(FlagentTheme.textLight(themeMode))
                    fontSize(12.px)
                    marginTop(2.px)
                }
            }) {
                Text(LocalizedStrings.dashboardTenantLabel(tenantName))
            }
        }
        lastFlagsRefreshMs?.let { ts ->
            val minAgo = ((currentTimeMillis() - ts) / 60_000).toInt().coerceAtLeast(0)
            P({
                style {
                    color(FlagentTheme.textLight(themeMode))
                    fontSize(12.px)
                    marginTop(2.px)
                }
            }) {
                Text(LocalizedStrings.dataUpdatedMinutesAgo(minAgo))
            }
        }
    }
}

@Composable
private fun StatsRow(themeMode: ThemeMode, stats: DashboardStats) {
    Div({
        style {
            display(DisplayStyle.Grid)
            property("grid-template-columns", "repeat(auto-fit, minmax(160px, 1fr))")
            gap(16.px)
            marginBottom(16.px)
        }
    }) {
        StatCard(themeMode, LocalizedStrings.totalFlagsStat, stats.totalFlags.toString(), "flag", FlagentTheme.Primary)
        StatCard(themeMode, LocalizedStrings.enabled, stats.enabledFlags.toString(), "check_circle", FlagentTheme.Success)
        StatCard(themeMode, LocalizedStrings.disabled, stats.disabledFlags.toString(), "cancel", FlagentTheme.Error)
        StatCard(themeMode, LocalizedStrings.withSegmentsStat, stats.flagsWithSegments.toString(), "dashboard", FlagentTheme.Secondary)
    }
}

@Composable
private fun QuickLinksRow(themeMode: ThemeMode, stats: DashboardStats) {
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
            title = LocalizedStrings.experimentsTitle,
            value = stats.flagsWithVariants.toString(),
            subtitle = "${stats.flagsWithVariants} ${LocalizedStrings.experimentsCount}",
            icon = "science",
            color = FlagentTheme.Secondary,
            onClick = { Router.navigateTo(Route.Experiments) }
        )
        QuickLinkCard(
            themeMode,
            title = LocalizedStrings.segments,
            value = stats.flagsWithSegments.toString(),
            icon = "segment",
            color = FlagentTheme.Primary,
            onClick = { Router.navigateTo(Route.Segments) }
        )
        QuickLinkCard(
            themeMode,
            title = LocalizedStrings.analyticsTitle,
            value = LocalizedStrings.viewMetrics,
            icon = "analytics",
            color = FlagentTheme.Primary,
            onClick = { Router.navigateTo(Route.Analytics) }
        )
    }
}

@Composable
private fun HealthStatusCard(
    themeMode: ThemeMode,
    stats: DashboardStats,
    metricsOverview: GlobalMetricsOverviewResponse?,
    anomalyViewModel: AnomalyViewModel?,
    enableMetrics: Boolean,
    enableAnomalyDetection: Boolean
) {
    val hasHighSeverityAlerts = enableAnomalyDetection && anomalyViewModel?.alerts?.any {
        it.severity == AlertSeverity.HIGH || it.severity == AlertSeverity.CRITICAL
    } == true
    val noEvaluationsInPeriod = enableMetrics && stats.totalFlags > 0 &&
        (metricsOverview?.totalEvaluations ?: 0L) == 0L

    val (indicatorColor, statusText) = when {
        hasHighSeverityAlerts -> FlagentTheme.Warning to LocalizedStrings.healthAnomaliesCount(anomalyViewModel!!.alerts.size)
        noEvaluationsInPeriod -> FlagentTheme.Warning to LocalizedStrings.healthNoEvaluations
        else -> FlagentTheme.Success to LocalizedStrings.healthStatusOk
    }

    Div({
        style {
            backgroundColor(FlagentTheme.cardBg(themeMode))
            borderRadius(8.px)
            padding(16.px)
            property("border", "1px solid ${FlagentTheme.cardBorder(themeMode)}")
            marginBottom(16.px)
            display(DisplayStyle.Flex)
            alignItems(AlignItems.Center)
            gap(12.px)
        }
    }) {
        Span({
            style {
                width(12.px)
                height(12.px)
                borderRadius(50.percent)
                backgroundColor(indicatorColor)
                property("flex-shrink", "0")
            }
        }) {}
        Div({}) {
            H3({
                style {
                    fontSize(14.px)
                    fontWeight("600")
                    margin(0.px)
                    color(FlagentTheme.text(themeMode))
                }
            }) {
                Text(LocalizedStrings.healthStatus)
            }
            P({
                style {
                    fontSize(13.px)
                    color(FlagentTheme.textLight(themeMode))
                    margin(0.px)
                    marginTop(2.px)
                }
            }) {
                Text(statusText)
                if (hasHighSeverityAlerts) {
                    Span({
                        style { marginLeft(4.px); textDecoration("underline"); cursor("pointer") }
                        onClick { Router.navigateTo(Route.Alerts) }
                    }) {
                        Text(" · ${LocalizedStrings.viewAll}")
                    }
                }
            }
        }
    }
}

@Composable
private fun CrashOverviewSection(themeMode: ThemeMode, crashOverview: CrashListResponse?) {
    val total = crashOverview?.total ?: 0L
    val items = crashOverview?.items?.take(3) ?: emptyList()
    Div({
        style {
            backgroundColor(FlagentTheme.cardBg(themeMode))
            borderRadius(8.px)
            padding(16.px)
            property("border", "1px solid ${FlagentTheme.cardBorder(themeMode)}")
            marginBottom(16.px)
        }
    }) {
        Div({
            style {
                display(DisplayStyle.Flex)
                justifyContent(JustifyContent.SpaceBetween)
                alignItems(AlignItems.Center)
                marginBottom(12.px)
            }
        }) {
            Span({
                style {
                    fontSize(16.px)
                    fontWeight("600")
                    color(FlagentTheme.text(themeMode))
                }
            }) {
                Text(LocalizedStrings.crashesInPeriod(total.toInt(), LocalizedStrings.period24h))
            }
            Button({
                style {
                    padding(6.px, 12.px)
                    fontSize(14.px)
                    cursor("pointer")
                    backgroundColor(FlagentTheme.Primary)
                    color(Color.white)
                    border(0.px)
                    borderRadius(6.px)
                }
                onClick { Router.navigateTo(Route.Crash) }
            }) {
                Text(LocalizedStrings.openCrashDashboard)
            }
        }
        if (items.isNotEmpty()) {
            Div({
                style {
                    display(DisplayStyle.Flex)
                    flexDirection(FlexDirection.Column)
                    gap(8.px)
                }
            }) {
                items.forEach { crash ->
                    Div({
                        style {
                            padding(8.px, 12.px)
                            backgroundColor(FlagentTheme.inputBg(themeMode))
                            borderRadius(6.px)
                            fontSize(13.px)
                            color(FlagentTheme.textLight(themeMode))
                        }
                    }) {
                        Text(crash.message.ifBlank { LocalizedStrings.noMessage })
                        val flagKeys = crash.activeFlagKeys?.take(3) ?: emptyList()
                        if (flagKeys.isNotEmpty()) {
                            Span({ style { display(DisplayStyle.Block); marginTop(4.px); fontSize(12.px) } }) {
                                Text("${LocalizedStrings.activeFlags}: ${flagKeys.joinToString(", ")}")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionsSection(themeMode: ThemeMode) {
    Div({
        style {
            backgroundColor(FlagentTheme.cardBg(themeMode))
            borderRadius(8.px)
            padding(16.px)
            property("box-shadow", FlagentTheme.ShadowCard)
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
            Text(LocalizedStrings.quickAccess)
        }
        Div({
            style {
                display(DisplayStyle.Grid)
                property("grid-template-columns", "repeat(auto-fit, minmax(140px, 1fr))")
                gap(12.px)
            }
        }) {
            QuickActionButton(themeMode, LocalizedStrings.createFlag, "add_circle") {
                Router.navigateTo(Route.CreateFlag)
            }
            QuickActionButton(themeMode, LocalizedStrings.viewFlags, "flag") {
                Router.navigateTo(Route.FlagsList)
            }
            QuickActionButton(themeMode, LocalizedStrings.experimentsTitle, "science") {
                Router.navigateTo(Route.Experiments)
            }
            QuickActionButton(themeMode, LocalizedStrings.segmentsTitle, "segment") {
                Router.navigateTo(Route.Segments)
            }
            QuickActionButton(themeMode, LocalizedStrings.analyticsTitle, "analytics") {
                Router.navigateTo(Route.Analytics)
            }
            QuickActionButton(themeMode, LocalizedStrings.debugConsole, "bug_report") {
                Router.navigateTo(Route.DebugConsole())
            }
            if (AppConfig.Features.enableMultiTenancy) {
                QuickActionButton(themeMode, LocalizedStrings.manageTenantsLink, "business") {
                    Router.navigateTo(Route.Tenants)
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
            property("box-shadow", FlagentTheme.ShadowCard)
            property("transition", "transform 0.2s, box-shadow 0.2s")
        }
        attr("aria-label", "$title: $value")
        onMouseEnter {
            val el = it.target as org.w3c.dom.HTMLElement
            el.style.transform = "translateY(-4px)"
            el.style.setProperty("box-shadow", "0 4px 16px $FlagentTheme.ShadowHover")
        }
        onMouseLeave {
            val el = it.target as org.w3c.dom.HTMLElement
            el.style.transform = "translateY(0)"
            el.style.setProperty("box-shadow", FlagentTheme.ShadowCard)
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
    subtitle: String? = null,
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
            property("box-shadow", FlagentTheme.ShadowCard)
            property("transition", "transform 0.2s, box-shadow 0.2s")
            cursor("pointer")
            width(100.percent)
        }
        attr("aria-label", "$title: $value")
        onMouseEnter {
            val el = it.target as org.w3c.dom.HTMLElement
            el.style.transform = "translateY(-2px)"
            el.style.setProperty("box-shadow", "0 4px 12px $FlagentTheme.ShadowHover")
        }
        onMouseLeave {
            val el = it.target as org.w3c.dom.HTMLElement
            el.style.transform = "translateY(0)"
            el.style.setProperty("box-shadow", FlagentTheme.ShadowCard)
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
        subtitle?.let { sub ->
            Div({
                style {
                    fontSize(12.px)
                    color(FlagentTheme.textLight(themeMode))
                    marginTop(4.px)
                }
            }) {
                Text(sub)
            }
        }
    }
}

@Composable
private fun QuickActionButton(themeMode: ThemeMode, label: String, icon: String, onClick: () -> Unit) {
    val textColor = FlagentTheme.text(themeMode)
    Button({
        attr("aria-label", label)
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
            color(textColor)
            property("transition", "all 0.2s")
            width(100.percent)
        }
        onMouseEnter {
            (it.target as org.w3c.dom.HTMLElement).style.backgroundColor = FlagentTheme.Primary.toString()
            (it.target as org.w3c.dom.HTMLElement).style.color = Color.white.toString()
        }
        onMouseLeave {
            (it.target as org.w3c.dom.HTMLElement).style.backgroundColor = FlagentTheme.inputBg(themeMode).toString()
            (it.target as org.w3c.dom.HTMLElement).style.color = textColor.toString()
        }
        onClick { onClick() }
    }) {
        Icon(icon, size = 20.px, color = textColor)
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
