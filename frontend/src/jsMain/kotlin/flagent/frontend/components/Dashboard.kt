package flagent.frontend.components

import androidx.compose.runtime.*
import flagent.api.model.FlagResponse
import flagent.frontend.api.ApiClient
import flagent.frontend.config.AppConfig
import flagent.frontend.api.GlobalMetricsOverviewResponse
import flagent.frontend.components.dashboard.ActivityChart
import flagent.frontend.components.dashboard.StatusDistributionChart
import flagent.frontend.components.dashboard.StatusSlice
import flagent.frontend.navigation.Route
import flagent.frontend.navigation.Router
import flagent.frontend.state.BackendOnboardingState
import flagent.frontend.state.LocalThemeMode
import flagent.frontend.state.ThemeMode
import flagent.frontend.theme.FlagentTheme
import flagent.frontend.util.AppLogger
import flagent.frontend.util.ErrorHandler
import flagent.frontend.viewmodel.AnomalyViewModel
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
fun Dashboard() {
    val themeMode = LocalThemeMode.current
    var stats by remember { mutableStateOf<DashboardStats?>(null) }
    var flags by remember { mutableStateOf<List<FlagResponse>>(emptyList()) }
    var metricsOverview by remember { mutableStateOf<GlobalMetricsOverviewResponse?>(null) }
    var metricsLoadError by remember { mutableStateOf<String?>(null) }
    var metricsPeriod by remember { mutableStateOf(MetricsPeriod.P24H) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    val anomalyViewModel = if (AppConfig.Features.enableAnomalyDetection) {
        remember { AnomalyViewModel() }
    } else null

    LaunchedEffect(Unit) {
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

    LaunchedEffect(metricsPeriod) {
        if (!AppConfig.Features.enableMetrics) return@LaunchedEffect
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
        } catch (e: Throwable) {
            metricsLoadError = ErrorHandler.getUserMessage(ErrorHandler.handle(e, null))
            metricsOverview = null
        }
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
                Text(flagent.frontend.i18n.LocalizedStrings.dashboardNav)
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
                    color(FlagentTheme.textLight(themeMode))
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
                    backgroundColor(FlagentTheme.errorBg(themeMode))
                    borderRadius(8.px)
                    color(FlagentTheme.errorText(themeMode))
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
                            Text(flagent.frontend.i18n.LocalizedStrings.createFirstTenant)
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
                            Text(flagent.frontend.i18n.LocalizedStrings.logInAdmin)
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
                        Text(flagent.frontend.i18n.LocalizedStrings.createFirstFlag)
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
                        Text(flagent.frontend.i18n.LocalizedStrings.createFlag)
                    }
                }
            } else {
            // Stats Cards
            Div({
                style {
                    display(DisplayStyle.Grid)
                    property("grid-template-columns", "repeat(auto-fit, minmax(160px, 1fr))")
                    gap(16.px)
                    marginBottom(16.px)
                }
            }) {
                StatCard(themeMode, flagent.frontend.i18n.LocalizedStrings.totalFlagsStat, stats!!.totalFlags.toString(), "flag", FlagentTheme.Primary)
                StatCard(themeMode, flagent.frontend.i18n.LocalizedStrings.enabled, stats!!.enabledFlags.toString(), "check_circle", FlagentTheme.Success)
                StatCard(themeMode, flagent.frontend.i18n.LocalizedStrings.disabled, stats!!.disabledFlags.toString(), "cancel", FlagentTheme.Error)
                StatCard(themeMode, flagent.frontend.i18n.LocalizedStrings.withSegmentsStat, stats!!.flagsWithSegments.toString(), "dashboard", FlagentTheme.Secondary)
                StatCard(themeMode, flagent.frontend.i18n.LocalizedStrings.experimentsTitle, stats!!.flagsWithVariants.toString(), "science", FlagentTheme.Secondary)
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
                    title = flagent.frontend.i18n.LocalizedStrings.segments,
                    value = (stats?.flagsWithSegments ?: 0).toString(),
                    icon = "segment",
                    color = FlagentTheme.Primary,
                    onClick = { Router.navigateTo(Route.Segments) }
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

            // Metrics section: error message or charts
            if (AppConfig.Features.enableMetrics) {
                if (metricsLoadError != null && metricsOverview == null) {
                    Div({
                        style {
                            padding(16.px)
                            backgroundColor(FlagentTheme.warningBg(themeMode))
                            borderRadius(8.px)
                            marginBottom(16.px)
                            property("border", "1px solid ${FlagentTheme.cardBorder(themeMode)}")
                        }
                    }) {
                        Text(flagent.frontend.i18n.LocalizedStrings.metricsUnavailable + ": " + metricsLoadError)
                    }
                } else if (metricsOverview != null) {
                val overview = metricsOverview!!
                if (overview.timeSeries.isNotEmpty()) {
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
                                Text("${flagent.frontend.i18n.LocalizedStrings.totalEvaluationsLabel}: ${overview.totalEvaluations} · ${flagent.frontend.i18n.LocalizedStrings.uniqueFlagsLabel}: ${overview.uniqueFlags}")
                            }
                            Div({
                                style { display(DisplayStyle.Flex); gap(8.px) }
                            }) {
                                listOf(
                                    MetricsPeriod.P1H to flagent.frontend.i18n.LocalizedStrings.period1h,
                                    MetricsPeriod.P24H to flagent.frontend.i18n.LocalizedStrings.period24h,
                                    MetricsPeriod.P7D to flagent.frontend.i18n.LocalizedStrings.period7d
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
                        ActivityChart(
                            overview.timeSeries,
                            flagent.frontend.i18n.LocalizedStrings.evaluationsOverTime,
                            themeMode
                        )
                    }
                }
                // Status distribution (from current flags stats)
                val statusSlices = listOf(
                    StatusSlice(flagent.frontend.i18n.LocalizedStrings.statusEnabledLabel, stats!!.enabledFlags, FlagentTheme.Success.toString()),
                    StatusSlice(flagent.frontend.i18n.LocalizedStrings.statusDisabledLabel, stats!!.disabledFlags, FlagentTheme.Error.toString()),
                    StatusSlice(flagent.frontend.i18n.LocalizedStrings.statusWithSegments, stats!!.flagsWithSegments, FlagentTheme.Primary.toString()),
                    StatusSlice(flagent.frontend.i18n.LocalizedStrings.statusExperiments, stats!!.flagsWithVariants, FlagentTheme.Secondary.toString())
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
                            flagent.frontend.i18n.LocalizedStrings.statusDistribution,
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
                        Text(flagent.frontend.i18n.LocalizedStrings.topFlags)
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
                                attr("aria-label", "${tf.flagKey.ifBlank { "Flag #${tf.flagId}" }}: ${tf.evaluationCount} evaluations, ${if (flagEnabled == true) "enabled" else "disabled"}")
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
                                        Text(tf.flagKey.ifBlank { "Flag #${tf.flagId}" })
                                    }
                                }
                                Span({
                                    style {
                                        fontSize(12.px)
                                        color(FlagentTheme.textLight(themeMode))
                                    }
                                }) {
                                    Text("${tf.evaluationCount} ${flagent.frontend.i18n.LocalizedStrings.evaluations}")
                                }
                            }
                        }
                    }
                }
            }

            // Health Status
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
                        backgroundColor(FlagentTheme.Success)
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
                        Text(flagent.frontend.i18n.LocalizedStrings.healthStatus)
                    }
                    P({
                        style {
                            fontSize(13.px)
                            color(FlagentTheme.textLight(themeMode))
                            margin(0.px)
                            marginTop(2.px)
                        }
                    }) {
                        Text(flagent.frontend.i18n.LocalizedStrings.healthStatusOk)
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
                                Text("🚨 ${flagent.frontend.i18n.LocalizedStrings.unresolvedAlerts}")
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
                                Text(flagent.frontend.i18n.LocalizedStrings.viewAll)
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
            
            // Recently updated flags
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
                            Text(flagent.frontend.i18n.LocalizedStrings.recentlyUpdatedFlags)
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
                            Text(flagent.frontend.i18n.LocalizedStrings.viewAllFlags)
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
                    Text(flagent.frontend.i18n.LocalizedStrings.quickAccess)
                }

                Div({
                    style {
                        display(DisplayStyle.Grid)
                        property("grid-template-columns", "repeat(auto-fit, minmax(140px, 1fr))")
                        gap(12.px)
                    }
                }) {
                    QuickActionButton(themeMode, flagent.frontend.i18n.LocalizedStrings.createFlag, "add_circle") {
                        Router.navigateTo(Route.CreateFlag)
                    }
                    QuickActionButton(themeMode, flagent.frontend.i18n.LocalizedStrings.viewFlags, "flag") {
                        Router.navigateTo(Route.FlagsList)
                    }
                    QuickActionButton(themeMode, flagent.frontend.i18n.LocalizedStrings.experimentsTitle, "science") {
                        Router.navigateTo(Route.Experiments)
                    }
                    QuickActionButton(themeMode, flagent.frontend.i18n.LocalizedStrings.segmentsTitle, "segment") {
                        Router.navigateTo(Route.Segments)
                    }
                    QuickActionButton(themeMode, flagent.frontend.i18n.LocalizedStrings.analyticsTitle, "analytics") {
                        Router.navigateTo(Route.Analytics)
                    }
                    QuickActionButton(themeMode, flagent.frontend.i18n.LocalizedStrings.debugConsole, "bug_report") {
                        Router.navigateTo(Route.DebugConsole())
                    }
                    if (AppConfig.Features.enableMultiTenancy) {
                        QuickActionButton(themeMode, flagent.frontend.i18n.LocalizedStrings.manageTenantsLink, "business") {
                            Router.navigateTo(Route.Tenants)
                        }
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
