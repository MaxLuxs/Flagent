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
import flagent.frontend.util.flexShrink
import flagent.frontend.util.triggerDownloadFromString
import kotlinx.serialization.json.Json
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

private fun timeRangeToMs(range: String): Long = when (range) {
    "24h" -> 86400_000L
    "7d" -> 7 * 86400_000L
    "30d" -> 30 * 86400_000L
    else -> 86400_000L
}

/**
 * Analytics page: tabs Overview (when metrics enabled) / Events / By flags. Time filters, export, period comparison.
 */
@Composable
fun AnalyticsPage() {
    val themeMode = LocalThemeMode.current
    var flags by remember { mutableStateOf<List<FlagResponse>>(emptyList()) }
    var overview by remember { mutableStateOf<GlobalMetricsOverviewResponse?>(null) }
    var overviewPrevious by remember { mutableStateOf<GlobalMetricsOverviewResponse?>(null) }
    var analyticsOverview by remember { mutableStateOf<AnalyticsOverviewResponse?>(null) }
    var analyticsOverviewPrevious by remember { mutableStateOf<AnalyticsOverviewResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var activeTab by remember { mutableStateOf("overview") }
    var timeRange by remember { mutableStateOf("24h") }
    var compareWithPrevious by remember { mutableStateOf(false) }

    var isLoadingEvents by remember { mutableStateOf(false) }

    val endTime = remember(timeRange) { (kotlin.js.Date().getTime() as Number).toLong() }
    val startTime = remember(timeRange) { endTime - timeRangeToMs(timeRange) }
    val bucketMs = when (timeRange) {
        "24h" -> 3600_000L
        "7d" -> 86400_000L
        "30d" -> 86400_000L
        else -> 3600_000L
    }

    LaunchedEffect(Unit) {
        ErrorHandler.withErrorHandling(
            block = { flags = ApiClient.getFlags().first },
            onError = { err -> error = ErrorHandler.getUserMessage(err) }
        )
    }

    LaunchedEffect(activeTab, timeRange, compareWithPrevious) {
        when (activeTab) {
            "overview" -> {
                if (AppConfig.Features.enableMetrics) {
                    isLoading = true
                    error = null
                    ErrorHandler.withErrorHandling(
                        block = {
                            overview = ApiClient.getMetricsOverview(
                                startTime = startTime,
                                endTime = endTime,
                                topLimit = 10,
                                timeBucketMs = bucketMs
                            )
                            if (compareWithPrevious) {
                                val prevEnd = startTime
                                val prevStart = prevEnd - timeRangeToMs(timeRange)
                                overviewPrevious = ApiClient.getMetricsOverview(
                                    startTime = prevStart,
                                    endTime = prevEnd,
                                    topLimit = 10,
                                    timeBucketMs = bucketMs
                                )
                            } else {
                                overviewPrevious = null
                            }
                        },
                        onError = { err -> error = ErrorHandler.getUserMessage(err) }
                    )
                    isLoading = false
                }
            }
            "events" -> {
                isLoadingEvents = true
                try {
                    analyticsOverview = ApiClient.getAnalyticsOverview(
                        startTime = startTime,
                        endTime = endTime,
                        topLimit = 20,
                        timeBucketMs = bucketMs
                    )
                    if (compareWithPrevious) {
                        val prevEnd = startTime
                        val prevStart = prevEnd - timeRangeToMs(timeRange)
                        analyticsOverviewPrevious = ApiClient.getAnalyticsOverview(
                            startTime = prevStart,
                            endTime = prevEnd,
                            topLimit = 20,
                            timeBucketMs = bucketMs
                        )
                    } else {
                        analyticsOverviewPrevious = null
                    }
                } catch (_: Throwable) {
                    analyticsOverview = null
                    analyticsOverviewPrevious = null
                }
                isLoadingEvents = false
            }
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
            TabButton(themeMode, LocalizedStrings.overviewTab, activeTab == "overview") { activeTab = "overview" }
            TabButton(themeMode, LocalizedStrings.eventsTab, activeTab == "events") { activeTab = "events" }
            if (hasOverview) {
                TabButton(themeMode, LocalizedStrings.byFlagsTab, activeTab == "flags") { activeTab = "flags" }
            }
        }

        // Time range and actions (Overview / Events)
        if (activeTab == "overview" || activeTab == "events") {
            Div({
                style {
                    display(DisplayStyle.Flex)
                    flexWrap(FlexWrap.Wrap)
                    alignItems(AlignItems.Center)
                    gap(12.px)
                    marginBottom(16.px)
                }
            }) {
                Span({
                    style {
                        fontSize(13.px)
                        color(FlagentTheme.textLight(themeMode))
                        marginRight(4.px)
                    }
                }) {
                    Text(LocalizedStrings.filterByTime)
                }
                listOf("24h" to LocalizedStrings.today, "7d" to LocalizedStrings.week, "30d" to LocalizedStrings.month).forEach { (value, label) ->
                    Button({
                        style {
                            padding(8.px, 14.px)
                            border(1.px, LineStyle.Solid, FlagentTheme.cardBorder(themeMode))
                            borderRadius(6.px)
                            cursor("pointer")
                            fontSize(13.px)
                            backgroundColor(if (timeRange == value) FlagentTheme.Primary else Color.transparent)
                            color(if (timeRange == value) Color.white else FlagentTheme.text(themeMode))
                        }
                        onClick { timeRange = value }
                    }) {
                        Text(label)
                    }
                }
                Div({
                    style {
                        display(DisplayStyle.Flex)
                        alignItems(AlignItems.Center)
                        gap(8.px)
                        marginLeft(8.px)
                        paddingLeft(8.px)
                        property("border-left", "1px solid ${FlagentTheme.cardBorder(themeMode)}")
                    }
                }) {
                    Input(InputType.Checkbox) {
                        id("analytics-compare")
                        checked(compareWithPrevious)
                        onInput { compareWithPrevious = (it.target as? org.w3c.dom.HTMLInputElement)?.checked ?: false }
                    }
                    Label(attrs = {
                        attr("for", "analytics-compare")
                        style {
                            fontSize(13.px)
                            color(FlagentTheme.text(themeMode))
                            cursor("pointer")
                        }
                    }) {
                        Text(LocalizedStrings.compareWithPreviousPeriod)
                    }
                }
                if (activeTab == "overview" && overview != null) {
                    AnalyticsExportButtons(themeMode, overview!!, overviewPrevious, timeRange)
                }
                if (activeTab == "events" && analyticsOverview != null) {
                    AnalyticsEventsExportButtons(themeMode, analyticsOverview!!, analyticsOverviewPrevious)
                }
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
                    val prev = overviewPrevious
                    val totalComp = if (prev != null && prev.totalEvaluations > 0) {
                        val pct = ((overview!!.totalEvaluations - prev.totalEvaluations).toDouble() / prev.totalEvaluations * 100).toInt()
                        if (pct >= 0) "↑ $pct% vs prev." else "↓ ${-pct}% vs prev."
                    } else null
                    val uniqueComp = if (prev != null && prev.uniqueFlags > 0) {
                        val delta = overview!!.uniqueFlags - prev.uniqueFlags
                        if (delta >= 0) "↑ $delta vs prev." else "↓ ${-delta} vs prev."
                    } else null
                    AnalyticsOverviewCard(themeMode, LocalizedStrings.totalEvaluationsLabel, overview!!.totalEvaluations.toString(), totalComp)
                    AnalyticsOverviewCard(themeMode, LocalizedStrings.uniqueFlagsLabel, overview!!.uniqueFlags.toString(), uniqueComp)
                }
                if (overview!!.timeSeries.isNotEmpty()) {
                    Div({
                        style {
                            backgroundColor(FlagentTheme.cardBg(themeMode))
                            borderRadius(8.px)
                            padding(20.px)
                            property("box-shadow", FlagentTheme.ShadowCard)
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
                            property("box-shadow", FlagentTheme.ShadowCard)
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
                                        property("box-shadow", FlagentTheme.ShadowCard)
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
                                                minWidth(0.px)
                                                overflow("hidden")
                                                property("text-overflow", "ellipsis")
                                                property("white-space", "nowrap")
                                            }
                                        }) {
                                            Text(tf.flagKey.ifBlank { "Flag #${tf.flagId}" })
                                        }
                                        if (AppConfig.Features.enableMetrics) {
                                            A(href = Route.FlagMetrics(tf.flagId).path(), attrs = {
                                                style {
                                                    display(DisplayStyle.Flex)
                                                    flexShrink(0)
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
                            AnalyticsOverviewCard(themeMode, LocalizedStrings.totalEventsLabel, data.totalEvents.toString())
                            AnalyticsOverviewCard(themeMode, LocalizedStrings.uniqueUsersLabel, data.uniqueUsers.toString())
                        }
                        if (data.timeSeries.isNotEmpty()) {
                            Div({
                                style {
                                    backgroundColor(FlagentTheme.cardBg(themeMode))
                                    borderRadius(8.px)
                                    padding(20.px)
                                    property("box-shadow", FlagentTheme.ShadowCard)
                                }
                            }) {
                                OverviewChart(data.timeSeries, LocalizedStrings.eventsOverTime)
                            }
                        }
                        if (data.dauByDay.isNotEmpty()) {
                            Div({
                                style {
                                    backgroundColor(FlagentTheme.cardBg(themeMode))
                                    borderRadius(8.px)
                                    padding(20.px)
                                    property("box-shadow", FlagentTheme.ShadowCard)
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
                                    Text(LocalizedStrings.dauByDay)
                                }
                                OverviewChart(
                                    data.dauByDay.map { TimeSeriesEntryResponse(it.timestamp, it.dau) },
                                    LocalizedStrings.dailyActiveUsers
                                )
                            }
                        }
                        if (data.topEvents.isNotEmpty()) {
                            Div({
                                style {
                                    backgroundColor(FlagentTheme.cardBg(themeMode))
                                    borderRadius(8.px)
                                    padding(20.px)
                                    property("box-shadow", FlagentTheme.ShadowCard)
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
                                    Text(LocalizedStrings.topEvents)
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
                                                property("box-shadow", FlagentTheme.ShadowCard)
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
                            property("box-shadow", FlagentTheme.ShadowCard)
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
                            Text(LocalizedStrings.noAnalyticsEventsYet)
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
                    property("box-shadow", FlagentTheme.ShadowCard)
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
                    property("box-shadow", "0 1px 3px ${FlagentTheme.Shadow}")
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
                                            backgroundColor(if (flag.enabled) FlagentTheme.successBg(themeMode) else FlagentTheme.inputBg(themeMode))
                                            color(if (flag.enabled) FlagentTheme.Success else FlagentTheme.textLight(themeMode))
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
private fun AnalyticsOverviewCard(themeMode: ThemeMode, label: String, value: String, comparisonText: String? = null) {
    Div({
        style {
            backgroundColor(FlagentTheme.cardBg(themeMode))
            borderRadius(8.px)
            padding(16.px)
            property("box-shadow", FlagentTheme.ShadowCard)
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
        if (comparisonText != null) {
            P({
                style {
                    fontSize(12.px)
                    color(FlagentTheme.textLight(themeMode))
                    margin(0.px)
                    marginTop(4.px)
                }
            }) {
                Text(comparisonText)
            }
        }
    }
}

@Composable
private fun AnalyticsExportButtons(
    themeMode: ThemeMode,
    overview: GlobalMetricsOverviewResponse,
    overviewPrevious: GlobalMetricsOverviewResponse?,
    timeRange: String
) {
    val suffix = when (timeRange) {
        "24h" -> "today"
        "7d" -> "week"
        "30d" -> "month"
        else -> "export"
    }
    Div({
        style {
            display(DisplayStyle.Flex)
            gap(8.px)
            marginLeft(8.px)
            paddingLeft(8.px)
            property("border-left", "1px solid ${FlagentTheme.cardBorder(themeMode)}")
        }
    }) {
        Button({
            style {
                padding(8.px, 14.px)
                border(1.px, LineStyle.Solid, FlagentTheme.cardBorder(themeMode))
                borderRadius(6.px)
                cursor("pointer")
                fontSize(13.px)
                backgroundColor(Color.transparent)
                color(FlagentTheme.text(themeMode))
            }
            onClick {
                val header = "timestamp,count"
                val rows = overview.timeSeries.sortedBy { it.timestamp }.joinToString("\n") { "${it.timestamp},${it.count}" }
                val topHeader = "flagId,flagKey,evaluationCount"
                val topRows = overview.topFlags.joinToString("\n") { "${it.flagId},${it.flagKey},${it.evaluationCount}" }
                val csv = listOf(header, rows, "", topHeader, topRows).joinToString("\n")
                triggerDownloadFromString(csv, "analytics-overview-$suffix.csv", "text/csv;charset=utf-8")
            }
        }) {
            Text(LocalizedStrings.exportCsv)
        }
        Button({
            style {
                padding(8.px, 14.px)
                border(1.px, LineStyle.Solid, FlagentTheme.cardBorder(themeMode))
                borderRadius(6.px)
                cursor("pointer")
                fontSize(13.px)
                backgroundColor(Color.transparent)
                color(FlagentTheme.text(themeMode))
            }
            onClick {
                val json = Json.encodeToString(overview)
                triggerDownloadFromString(json, "analytics-overview-$suffix.json", "application/json")
            }
        }) {
            Text(LocalizedStrings.exportJson)
        }
    }
}

@Composable
private fun AnalyticsEventsExportButtons(
    themeMode: ThemeMode,
    data: AnalyticsOverviewResponse,
    dataPrevious: AnalyticsOverviewResponse?
) {
    Div({
        style {
            display(DisplayStyle.Flex)
            gap(8.px)
            marginLeft(8.px)
            paddingLeft(8.px)
            property("border-left", "1px solid ${FlagentTheme.cardBorder(themeMode)}")
        }
    }) {
        Button({
            style {
                padding(8.px, 14.px)
                border(1.px, LineStyle.Solid, FlagentTheme.cardBorder(themeMode))
                borderRadius(6.px)
                cursor("pointer")
                fontSize(13.px)
                backgroundColor(Color.transparent)
                color(FlagentTheme.text(themeMode))
            }
            onClick {
                val header = "timestamp,count"
                val rows = data.timeSeries.sortedBy { it.timestamp }.joinToString("\n") { "${it.timestamp},${it.count}" }
                val eventHeader = "eventName,count"
                val eventRows = data.topEvents.joinToString("\n") { "${it.eventName},${it.count}" }
                val csv = listOf(header, rows, "", eventHeader, eventRows).joinToString("\n")
                triggerDownloadFromString(csv, "analytics-events.csv", "text/csv;charset=utf-8")
            }
        }) {
            Text(LocalizedStrings.exportCsv)
        }
        Button({
            style {
                padding(8.px, 14.px)
                border(1.px, LineStyle.Solid, FlagentTheme.cardBorder(themeMode))
                borderRadius(6.px)
                cursor("pointer")
                fontSize(13.px)
                backgroundColor(Color.transparent)
                color(FlagentTheme.text(themeMode))
            }
            onClick {
                val json = Json.encodeToString(data)
                triggerDownloadFromString(json, "analytics-events.json", "application/json")
            }
        }) {
            Text(LocalizedStrings.exportJson)
        }
    }
}

