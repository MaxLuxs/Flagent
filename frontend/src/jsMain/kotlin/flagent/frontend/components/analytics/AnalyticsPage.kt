package flagent.frontend.components.analytics

import androidx.compose.runtime.*
import flagent.api.model.FlagResponse
import flagent.frontend.api.AnalyticsOverviewResponse
import flagent.frontend.api.ApiClient
import flagent.frontend.api.GlobalMetricsOverviewResponse
import flagent.frontend.api.TimeSeriesEntryResponse
import flagent.frontend.components.Icon
import flagent.frontend.components.common.PageHeader
import flagent.frontend.components.metrics.OverviewChart
import flagent.frontend.config.AppConfig
import flagent.frontend.i18n.LocalizedStrings
import flagent.frontend.navigation.Route
import flagent.frontend.navigation.Router
import flagent.frontend.state.LocalThemeMode
import flagent.frontend.state.ThemeMode
import flagent.frontend.theme.FlagentTheme
import flagent.frontend.util.ErrorHandler
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

/**
 * Analytics page: tabs Overview (when metrics enabled) / By flags. Compact layout.
 */
@Composable
fun AnalyticsPage() {
    val themeMode = LocalThemeMode.current
    var flags by remember { mutableStateOf<List<FlagResponse>>(emptyList()) }
    var overview by remember { mutableStateOf<GlobalMetricsOverviewResponse?>(null) }
    var analyticsOverview by remember { mutableStateOf<AnalyticsOverviewResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var activeTab by remember { mutableStateOf("overview") }

    var isLoadingEvents by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isLoading = true
        error = null
        ErrorHandler.withErrorHandling(
            block = {
                flags = ApiClient.getFlags().first
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
            }
        )
        isLoading = false
    }

    LaunchedEffect(activeTab) {
        if (activeTab == "events") {
            isLoadingEvents = true
            try {
                analyticsOverview = ApiClient.getAnalyticsOverview(topLimit = 20, timeBucketMs = 3600_000)
            } catch (_: Throwable) {
                analyticsOverview = null
            }
            isLoadingEvents = false
        }
    }

    val hasOverview = AppConfig.Features.enableMetrics && overview != null

    Div({
        style {
            padding(0.px)
        }
    }) {
        PageHeader(
            title = LocalizedStrings.analyticsTitle,
            subtitle = LocalizedStrings.analyticsSubtitle
        )

        Div({
            style {
                display(DisplayStyle.Flex)
                gap(4.px)
                marginBottom(16.px)
                property("border-bottom", "1px solid ${FlagentTheme.cardBorder(themeMode)}")
            }
        }) {
            TabButton(themeMode, "Overview", activeTab == "overview") { activeTab = "overview" }
            TabButton(themeMode, "Events", activeTab == "events") { activeTab = "events" }
            if (hasOverview) {
                TabButton(themeMode, "By flags", activeTab == "flags") { activeTab = "flags" }
            }
        }

        if (isLoading) {
            Div({
                style {
                    textAlign("center")
                    padding(40.px)
                }
            }) {
                Text(LocalizedStrings.loading)
            }
        } else if (error != null) {
            Div({
                style {
                    padding(20.px)
                    backgroundColor(FlagentTheme.Error)
                    borderRadius(8.px)
                    color(Color.white)
                }
            }) {
                Text(error!!)
            }
        } else if (hasOverview && activeTab == "overview") {
            Div({
                style {
                    display(DisplayStyle.Flex)
                    flexDirection(FlexDirection.Column)
                    gap(24.px)
                }
            }) {
                Div({
                    style {
                        display(DisplayStyle.Grid)
                        property("grid-template-columns", "repeat(auto-fit, minmax(200px, 1fr))")
                        gap(16.px)
                    }
                }) {
                    AnalyticsOverviewCard(themeMode, "Total evaluations", overview!!.totalEvaluations.toString())
                    AnalyticsOverviewCard(themeMode, "Unique flags", overview!!.uniqueFlags.toString())
                }
                if (overview!!.timeSeries.isNotEmpty()) {
                    Div({
                        style {
                            backgroundColor(FlagentTheme.cardBg(themeMode))
                            borderRadius(8.px)
                            padding(20.px)
                            property("box-shadow", "0 2px 8px rgba(0,0,0,0.1)")
                        }
                    }) {
                        OverviewChart(overview!!.timeSeries, LocalizedStrings.evaluationsOverTime)
                    }
                }
                if (overview!!.topFlags.isNotEmpty()) {
                    Div({
                        style {
                            backgroundColor(FlagentTheme.cardBg(themeMode))
                            borderRadius(8.px)
                            padding(20.px)
                            property("box-shadow", "0 2px 8px rgba(0,0,0,0.1)")
                        }
                    }) {
                        H3({
                            style {
                                fontSize(16.px)
                                fontWeight("600")
                                marginBottom(12.px)
                                color(FlagentTheme.text(themeMode))
                            }
                        }) {
                            Text(LocalizedStrings.topFlagsByEvaluations)
                        }
                        Div({
                            style {
                                display(DisplayStyle.Grid)
                                property("grid-template-columns", "repeat(auto-fill, minmax(280px, 1fr))")
                                gap(16.px)
                            }
                        }) {
                            overview!!.topFlags.forEach { tf ->
                                Div({
                                    style {
                                        backgroundColor(FlagentTheme.inputBg(themeMode))
                                        borderRadius(8.px)
                                        padding(16.px)
                                        property("box-shadow", "0 2px 8px rgba(0,0,0,0.1)")
                                    }
                                }) {
                                    Div({
                                        style {
                                            display(DisplayStyle.Flex)
                                            justifyContent(JustifyContent.SpaceBetween)
                                            alignItems(AlignItems.Center)
                                        }
                                    }) {
                                        Span({
                                            style {
                                                fontSize(15.px)
                                                fontWeight("600")
                                                color(FlagentTheme.text(themeMode))
                                            }
                                        }) {
                                            Text(tf.flagKey.ifBlank { "Flag #${tf.flagId}" })
                                        }
                                        if (AppConfig.Features.enableMetrics) {
                                            A(href = Route.FlagMetrics(tf.flagId).path(), attrs = {
                                                style {
                                                    display(DisplayStyle.Flex)
                                                    alignItems(AlignItems.Center)
                                                    gap(4.px)
                                                    padding(6.px, 10.px)
                                                    backgroundColor(FlagentTheme.Primary)
                                                    color(Color.white)
                                                    borderRadius(6.px)
                                                    fontSize(12.px)
                                                    textDecoration("none")
                                                }
                                                onClick { e ->
                                                    e.preventDefault()
                                                    Router.navigateTo(Route.FlagMetrics(tf.flagId))
                                                }
                                            }) {
                                                Icon("bar_chart", size = 14.px, color = FlagentTheme.Background)
                                                Text(LocalizedStrings.viewMetrics)
                                            }
                                        }
                                    }
                                    P({
                                        style {
                                            fontSize(13.px)
                                            color(FlagentTheme.textLight(themeMode))
                                            margin(0.px)
                                            marginTop(8.px)
                                        }
                                    }) {
                                        Text("${tf.evaluationCount} evaluations")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else if (activeTab == "events") {
            if (isLoadingEvents) {
                Div({
                    style {
                        textAlign("center")
                        padding(40.px)
                    }
                }) {
                    Text(LocalizedStrings.loading)
                }
            } else {
                val data = analyticsOverview
                if (data != null) {
                    Div({
                        style {
                            display(DisplayStyle.Flex)
                            flexDirection(FlexDirection.Column)
                            gap(24.px)
                        }
                    }) {
                        Div({
                            style {
                                display(DisplayStyle.Grid)
                                property("grid-template-columns", "repeat(auto-fit, minmax(200px, 1fr))")
                                gap(16.px)
                            }
                        }) {
                            AnalyticsOverviewCard(themeMode, "Total events", data.totalEvents.toString())
                            AnalyticsOverviewCard(themeMode, "Unique users", data.uniqueUsers.toString())
                        }
                        if (data.timeSeries.isNotEmpty()) {
                            Div({
                                style {
                                    backgroundColor(FlagentTheme.cardBg(themeMode))
                                    borderRadius(8.px)
                                    padding(20.px)
                                    property("box-shadow", "0 2px 8px rgba(0,0,0,0.1)")
                                }
                            }) {
                                OverviewChart(data.timeSeries, "Events over time")
                            }
                        }
                        if (data.dauByDay.isNotEmpty()) {
                            Div({
                                style {
                                    backgroundColor(FlagentTheme.cardBg(themeMode))
                                    borderRadius(8.px)
                                    padding(20.px)
                                    property("box-shadow", "0 2px 8px rgba(0,0,0,0.1)")
                                }
                            }) {
                                H3({
                                    style {
                                        fontSize(16.px)
                                        fontWeight("600")
                                        marginBottom(12.px)
                                        color(FlagentTheme.text(themeMode))
                                    }
                                }) {
                                    Text("DAU by day")
                                }
                                OverviewChart(
                                    data.dauByDay.map { TimeSeriesEntryResponse(it.timestamp, it.dau) },
                                    "Daily active users"
                                )
                            }
                        }
                        if (data.topEvents.isNotEmpty()) {
                            Div({
                                style {
                                    backgroundColor(FlagentTheme.cardBg(themeMode))
                                    borderRadius(8.px)
                                    padding(20.px)
                                    property("box-shadow", "0 2px 8px rgba(0,0,0,0.1)")
                                }
                            }) {
                                H3({
                                    style {
                                        fontSize(16.px)
                                        fontWeight("600")
                                        marginBottom(12.px)
                                        color(FlagentTheme.text(themeMode))
                                    }
                                }) {
                                    Text("Top events")
                                }
                                Div({
                                    style {
                                        display(DisplayStyle.Grid)
                                        property("grid-template-columns", "repeat(auto-fill, minmax(280px, 1fr))")
                                        gap(16.px)
                                    }
                                }) {
                                    data.topEvents.forEach { te ->
                                        Div({
                                            style {
                                                backgroundColor(FlagentTheme.inputBg(themeMode))
                                                borderRadius(8.px)
                                                padding(16.px)
                                                property("box-shadow", "0 2px 8px rgba(0,0,0,0.1)")
                                            }
                                        }) {
                                            Span({
                                                style {
                                                    fontSize(15.px)
                                                    fontWeight("600")
                                                    color(FlagentTheme.text(themeMode))
                                                }
                                            }) {
                                                Text(te.eventName)
                                            }
                                            P({
                                                style {
                                                    fontSize(13.px)
                                                    color(FlagentTheme.textLight(themeMode))
                                                    margin(0.px)
                                                    marginTop(8.px)
                                                }
                                            }) {
                                                Text("${te.count} events")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Div({
                        style {
                            backgroundColor(FlagentTheme.cardBg(themeMode))
                            borderRadius(8.px)
                            padding(40.px)
                            property("box-shadow", "0 2px 8px rgba(0,0,0,0.1)")
                            textAlign("center")
                        }
                    }) {
                        Icon("analytics", size = 48.px, color = FlagentTheme.textLight(themeMode))
                        P({
                            style {
                                marginTop(16.px)
                                color(FlagentTheme.text(themeMode))
                                fontSize(16.px)
                            }
                        }) {
                            Text("No analytics events yet. Use SDK logEvent() to send events.")
                        }
                    }
                }
            }
        } else if (flags.isEmpty() && (activeTab == "flags" || !hasOverview)) {
            Div({
                style {
                    backgroundColor(FlagentTheme.cardBg(themeMode))
                    borderRadius(8.px)
                    padding(40.px)
                    property("box-shadow", "0 2px 8px rgba(0,0,0,0.1)")
                    textAlign("center")
                }
            }) {
                Icon("analytics", size = 48.px, color = FlagentTheme.textLight(themeMode))
                P({
                    style {
                        marginTop(16.px)
                        color(FlagentTheme.text(themeMode))
                        fontSize(16.px)
                    }
                }) {
                    Text(LocalizedStrings.noFlags)
                }
                Button({
                    style {
                        marginTop(20.px)
                        padding(12.px, 24.px)
                        backgroundColor(FlagentTheme.Primary)
                        color(Color.white)
                        border(0.px)
                        borderRadius(6.px)
                        cursor("pointer")
                        fontSize(14.px)
                    }
                    onClick { Router.navigateTo(Route.FlagsList) }
                }) {
                    Text(LocalizedStrings.createFlag)
                }
            }
        } else if (activeTab == "flags" || !hasOverview) {
            val evalCountByFlagId = remember(overview) {
                overview?.topFlags?.associate { it.flagId to it.evaluationCount } ?: emptyMap()
            }
            val showEvaluations = AppConfig.Features.enableMetrics && overview != null
            Div({
                style {
                    backgroundColor(FlagentTheme.cardBg(themeMode))
                    borderRadius(8.px)
                    overflow("hidden")
                    overflowX("auto")
                    property("box-shadow", "0 1px 3px rgba(0, 0, 0, 0.08)")
                }
            }) {
                Table({
                    style {
                        width(100.percent)
                        property("border-collapse", "collapse")
                        property("min-width", "800px")
                    }
                }) {
                    Thead {
                        Tr({
                            style {
                                backgroundColor(FlagentTheme.inputBg(themeMode))
                                property("border-bottom", "1px solid ${FlagentTheme.cardBorder(themeMode)}")
                            }
                        }) {
                            listOf(
                                "Key",
                                LocalizedStrings.description,
                                LocalizedStrings.status,
                                LocalizedStrings.segments,
                                LocalizedStrings.variants,
                                if (showEvaluations) "Evaluations" else null,
                                LocalizedStrings.updatedAtUtc,
                                if (AppConfig.Features.enableMetrics) LocalizedStrings.action else null
                            ).filterNotNull().forEach { h ->
                                Th({
                                    style {
                                        padding(10.px, 12.px)
                                        textAlign("left")
                                        fontSize(12.px)
                                        fontWeight(600)
                                        color(FlagentTheme.textLight(themeMode))
                                        property("text-transform", "uppercase")
                                    }
                                }) { Text(h) }
                            }
                        }
                    }
                    Tbody {
                        flags.forEach { flag ->
                            val evalCount = evalCountByFlagId[flag.id]
                            val onRowClick: () -> Unit = {
                                if (AppConfig.Features.enableMetrics) {
                                    Router.navigateTo(Route.FlagMetrics(flag.id))
                                } else {
                                    Router.navigateTo(Route.FlagDetail(flag.id))
                                }
                            }
                            Tr({
                                style {
                                    property("border-bottom", "1px solid ${FlagentTheme.cardBorder(themeMode)}")
                                    property("transition", "background-color 0.15s")
                                    cursor("pointer")
                                }
                                onClick { onRowClick() }
                                onMouseEnter { (it.target as org.w3c.dom.HTMLElement).style.backgroundColor = FlagentTheme.inputBg(themeMode).toString() }
                                onMouseLeave { (it.target as org.w3c.dom.HTMLElement).style.backgroundColor = "transparent" }
                            }) {
                                Td({
                                    style {
                                        padding(10.px, 12.px)
                                        fontSize(14.px)
                                        color(FlagentTheme.Primary)
                                        fontWeight("600")
                                    }
                                }) {
                                    Text(flag.key.ifBlank { "Flag #${flag.id}" })
                                }
                                Td({
                                    style {
                                        padding(10.px, 12.px)
                                        fontSize(14.px)
                                        property("max-width", "200px")
                                        property("overflow", "hidden")
                                        property("text-overflow", "ellipsis")
                                        property("white-space", "nowrap")
                                    }
                                }) {
                                    Text(flag.description.ifBlank { "—" })
                                }
                                Td({ style { padding(10.px, 12.px); fontSize(12.px) } }) {
                                    Span({
                                        style {
                                            padding(4.px, 8.px)
                                            borderRadius(6.px)
                                            backgroundColor(if (flag.enabled) Color("#D1FAE5") else FlagentTheme.inputBg(themeMode))
                                            color(if (flag.enabled) Color("#065F46") else FlagentTheme.textLight(themeMode))
                                        }
                                    }) {
                                        Text(if (flag.enabled) LocalizedStrings.enabled else LocalizedStrings.disabled)
                                    }
                                }
                                Td({ style { padding(10.px, 12.px); fontSize(14.px) } }) {
                                    Text(flag.segments.size.toString())
                                }
                                Td({
                                    style {
                                        padding(10.px, 12.px)
                                        fontSize(14.px)
                                        property("max-width", "120px")
                                        property("overflow", "hidden")
                                        property("text-overflow", "ellipsis")
                                        property("white-space", "nowrap")
                                    }
                                }) {
                                    Text(
                                        if (flag.variants.isEmpty()) "—"
                                        else flag.variants.joinToString(", ") { it.key }
                                    )
                                }
                                if (showEvaluations) {
                                    Td({ style { padding(10.px, 12.px); fontSize(14.px) } }) {
                                        Text(evalCount?.toString() ?: "—")
                                    }
                                }
                                Td({
                                    style {
                                        padding(10.px, 12.px)
                                        fontSize(12.px)
                                        color(FlagentTheme.textLight(themeMode))
                                    }
                                }) {
                                    Text(flag.updatedAt?.take(16) ?: "—")
                                }
                                if (AppConfig.Features.enableMetrics) {
                                    Td({ style { padding(10.px, 12.px) } }) {
                                        A(href = Route.FlagMetrics(flag.id).path(), attrs = {
                                            style {
                                                display(DisplayStyle.Flex)
                                                alignItems(AlignItems.Center)
                                                gap(4.px)
                                                padding(6.px, 10.px)
                                                backgroundColor(FlagentTheme.Primary)
                                                color(Color.white)
                                                borderRadius(6.px)
                                                fontSize(12.px)
                                                textDecoration("none")
                                                property("width", "fit-content")
                                            }
                                            onClick { e ->
                                                e.preventDefault()
                                                e.stopPropagation()
                                                Router.navigateTo(Route.FlagMetrics(flag.id))
                                            }
                                        }) {
                                            Icon("bar_chart", size = 14.px, color = FlagentTheme.Background)
                                            Text(LocalizedStrings.viewMetrics)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TabButton(themeMode: ThemeMode, label: String, isActive: Boolean, onClick: () -> Unit) {
    Button({
        this.onClick { onClick() }
        style {
            padding(10.px, 16.px)
            backgroundColor(if (isActive) FlagentTheme.cardBg(themeMode) else Color.transparent)
            color(if (isActive) FlagentTheme.Primary else FlagentTheme.textLight(themeMode))
            border(0.px)
            property("border-bottom", if (isActive) "2px solid ${FlagentTheme.Primary}" else "2px solid transparent")
            borderRadius(0.px)
            cursor("pointer")
            fontSize(14.px)
            fontWeight(if (isActive) "600" else "500")
            property("margin-bottom", "-1px")
        }
    }) {
        Text(label)
    }
}

@Composable
private fun AnalyticsOverviewCard(themeMode: ThemeMode, label: String, value: String) {
    Div({
        style {
            backgroundColor(FlagentTheme.cardBg(themeMode))
            borderRadius(8.px)
            padding(16.px)
            property("box-shadow", "0 2px 8px rgba(0,0,0,0.1)")
        }
    }) {
        P({
            style {
                fontSize(12.px)
                color(FlagentTheme.textLight(themeMode))
                margin(0.px)
                marginBottom(4.px)
            }
        }) {
            Text(label)
        }
        P({
            style {
                fontSize(24.px)
                fontWeight("600")
                color(FlagentTheme.text(themeMode))
                margin(0.px)
            }
        }) {
            Text(value)
        }
    }
}

