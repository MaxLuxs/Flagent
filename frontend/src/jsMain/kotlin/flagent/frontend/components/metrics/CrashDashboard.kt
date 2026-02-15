package flagent.frontend.components.metrics

import androidx.compose.runtime.*
import flagent.api.model.FlagResponse
import flagent.frontend.api.ApiClient
import flagent.frontend.api.CrashReportResponse
import flagent.frontend.components.Icon
import flagent.frontend.components.common.EmptyState
import flagent.frontend.components.common.SkeletonLoader
import flagent.frontend.api.MetricType
import flagent.frontend.config.AppConfig
import flagent.frontend.navigation.Route
import flagent.frontend.state.BackendOnboardingState
import flagent.frontend.navigation.Router
import flagent.frontend.i18n.LocalizedStrings
import flagent.frontend.state.LocalThemeMode
import org.jetbrains.compose.web.attributes.InputType
import flagent.frontend.state.ThemeMode
import flagent.frontend.theme.FlagentTheme
import flagent.frontend.util.ErrorHandler
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

/**
 * Crash Analytics dashboard (Enterprise only).
 * Lists crash reports with stack traces and flags with links to per-flag CRASH_RATE metrics.
 */
@Composable
fun CrashDashboard() {
    val themeMode = LocalThemeMode.current
    if (!AppConfig.Features.enableCrashAnalytics) {
        Div({
            style {
                padding(24.px)
                color(FlagentTheme.textLight(themeMode))
                fontSize(14.px)
            }
        }) {
            Text("Crash Analytics is not available for this edition.")
        }
        return
    }

    var flags by remember { mutableStateOf<List<FlagResponse>>(emptyList()) }
    var crashReports by remember { mutableStateOf<List<CrashReportResponse>>(emptyList()) }
    var crashTotal by remember { mutableStateOf(0L) }
    var selectedCrash by remember { mutableStateOf<CrashReportResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isLoadingCrashes by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var crashReportsError by remember { mutableStateOf<String?>(null) }
    var activeTab by remember { mutableStateOf("reports") }
    var reportsRefreshTrigger by remember { mutableStateOf(0) }
    var timeRange by remember { mutableStateOf("24h") }
    var groupByType by remember { mutableStateOf(false) }

    val endTime = remember(timeRange) { (kotlin.js.Date().getTime() as Number).toLong() }
    val startTime = remember(timeRange) {
        val ms = when (timeRange) {
            "24h" -> 86400_000L
            "7d" -> 7 * 86400_000L
            "30d" -> 30 * 86400_000L
            else -> 86400_000L
        }
        endTime - ms
    }

    LaunchedEffect(Unit) {
        isLoading = true
        error = null
        try {
            ErrorHandler.withErrorHandling(
                block = {
                    flags = ApiClient.getFlags().first
                },
                onError = { err ->
                    error = ErrorHandler.getUserMessage(err)
                }
            )
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(activeTab, reportsRefreshTrigger, timeRange) {
        if (activeTab == "reports") {
            isLoadingCrashes = true
            crashReportsError = null
            try {
                ErrorHandler.withErrorHandling(
                    block = {
                        val resp = ApiClient.getCrashes(limit = 50, startTime = startTime, endTime = endTime)
                        crashReports = resp.items
                        crashTotal = resp.total
                    },
                    onError = { err ->
                        crashReportsError = ErrorHandler.getUserMessage(err)
                        crashReports = emptyList()
                        crashTotal = 0
                    }
                )
            } finally {
                isLoadingCrashes = false
            }
        }
    }

    Div({
        style {
            padding(24.px)
            backgroundColor(FlagentTheme.cardBg(themeMode))
            borderRadius(8.px)
            property("box-shadow", "0 1px 3px rgba(0, 0, 0, 0.1)")
        }
    }) {
        Div({
            style {
                display(DisplayStyle.Flex)
                alignItems(AlignItems.Center)
                gap(12.px)
                marginBottom(24.px)
            }
        }) {
            Icon("bug_report", size = 28.px, color = FlagentTheme.Error)
            H2({
                style {
                    fontSize(20.px)
                    fontWeight(600)
                    color(FlagentTheme.text(themeMode))
                    margin(0.px)
                }
            }) {
                Text(flagent.frontend.i18n.LocalizedStrings.crashAnalytics)
            }
        }
        P({
            style {
                fontSize(14.px)
                color(FlagentTheme.textLight(themeMode))
                margin(0.px)
                marginBottom(16.px)
            }
        }) {
            Text(LocalizedStrings.crashAnalyticsDescription)
        }

        Div({
            style {
                display(DisplayStyle.Flex)
                gap(8.px)
                marginBottom(16.px)
                property("border-bottom", "1px solid ${FlagentTheme.cardBorder(themeMode)}")
            }
        }) {
            Button({
                style {
                    padding(8.px, 16.px)
                    border(0.px)
                    borderRadius(6.px)
                    cursor("pointer")
                    fontSize(14.px)
                    backgroundColor(if (activeTab == "reports") FlagentTheme.Primary else Color.transparent)
                    color(if (activeTab == "reports") Color.white else FlagentTheme.text(themeMode))
                }
                onClick {
                    if (activeTab == "reports") {
                        reportsRefreshTrigger++
                    } else {
                        activeTab = "reports"
                    }
                }
            }) { Text(LocalizedStrings.crashReportsCount) }
            Button({
                style {
                    padding(8.px, 16.px)
                    border(0.px)
                    borderRadius(6.px)
                    cursor("pointer")
                    fontSize(14.px)
                    backgroundColor(if (activeTab == "flags") FlagentTheme.Primary else Color.transparent)
                    color(if (activeTab == "flags") Color.white else FlagentTheme.text(themeMode))
                }
                onClick { activeTab = "flags" }
            }) { Text(flagent.frontend.i18n.LocalizedStrings.byFlagsCrashRate) }
        }

        // Time range and group-by (reports tab)
        if (activeTab == "reports") {
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
                    }
                }) {
                    Text(LocalizedStrings.filterByTime)
                }
                listOf("24h" to LocalizedStrings.today, "7d" to LocalizedStrings.week, "30d" to LocalizedStrings.month).forEach { (value, label) ->
                    Button({
                        style {
                            padding(6.px, 12.px)
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
                Input(InputType.Checkbox) {
                    id("crash-group-by")
                    checked(groupByType)
                    onInput { groupByType = (it.target as? org.w3c.dom.HTMLInputElement)?.checked ?: false }
                }
                Label(attrs = {
                    attr("for", "crash-group-by")
                    style {
                        fontSize(13.px)
                        color(FlagentTheme.text(themeMode))
                        cursor("pointer")
                    }
                }) {
                    Text(LocalizedStrings.groupByType)
                }
            }
        }

        if (activeTab == "reports") {
            if (crashReportsError != null) {
                Div({
                    style {
                        padding(16.px)
                        backgroundColor(FlagentTheme.errorBg(themeMode))
                        borderRadius(6.px)
                        color(FlagentTheme.errorText(themeMode))
                        fontSize(14.px)
                        marginBottom(16.px)
                    }
                }) {
                    val isTenantError = crashReportsError!!.contains("tenant", ignoreCase = true) ||
                        crashReportsError!!.contains("X-API-Key", ignoreCase = true) ||
                        crashReportsError!!.contains("Create tenant", ignoreCase = true)
                    if (isTenantError) {
                        SideEffect { BackendOnboardingState.setBackendNeedsTenantOrAuth() }
                    }
                    Text(crashReportsError!!)
                    if (isTenantError) {
                        P({
                            style {
                                marginTop(8.px)
                                fontSize(13.px)
                                opacity(0.9)
                            }
                        }) {
                            Text(flagent.frontend.i18n.LocalizedStrings.ensureApiKeySet)
                        }
                    }
                    Button({
                        style {
                            marginTop(12.px)
                            padding(8.px, 16.px)
                            backgroundColor(FlagentTheme.Primary)
                            color(Color.white)
                            border(0.px)
                            borderRadius(6.px)
                            cursor("pointer")
                            fontSize(14.px)
                        }
                        onClick { reportsRefreshTrigger++ }
                    }) {
                        Text(flagent.frontend.i18n.LocalizedStrings.retry)
                    }
                }
            } else if (isLoadingCrashes) {
                SkeletonLoader(height = 200.px)
            } else if (crashReports.isEmpty()) {
                EmptyState(
                    icon = "bug_report",
                    title = "No crash reports",
                    description = "Crash reports from SDK will appear here. Use FlagentCrashReporter.install() in your app."
                )
            } else {
                Div({
                    style {
                        display(DisplayStyle.Flex)
                        flexDirection(FlexDirection.Column)
                        gap(8.px)
                    }
                }) {
                    P({
                        style {
                            fontSize(13.px)
                            color(FlagentTheme.textLight(themeMode))
                            margin(0.px)
                            marginBottom(8.px)
                        }
                    }) {
                        Text("$crashTotal ${LocalizedStrings.crashReportsCount}")
                    }
                    if (groupByType) {
                        val grouped = crashReports.groupBy { c -> c.message.take(120).ifEmpty { LocalizedStrings.noMessage }.lines().firstOrNull()?.take(80) ?: LocalizedStrings.other }
                        grouped.entries.sortedByDescending { it.value.size }.forEach { (groupKey, groupCrashes) ->
                            var expanded by remember { mutableStateOf(false) }
                            Div({
                                style {
                                    marginBottom(8.px)
                                    backgroundColor(FlagentTheme.inputBg(themeMode))
                                    borderRadius(6.px)
                                    border(1.px, LineStyle.Solid, FlagentTheme.cardBorder(themeMode))
                                }
                            }) {
                                Div({
                                    style {
                                        padding(12.px, 16.px)
                                        display(DisplayStyle.Flex)
                                        justifyContent(JustifyContent.SpaceBetween)
                                        alignItems(AlignItems.Center)
                                        cursor("pointer")
                                    }
                                    onClick { expanded = !expanded }
                                }) {
                                    Span({
                                        style {
                                            fontWeight(600)
                                            fontSize(14.px)
                                            color(FlagentTheme.text(themeMode))
                                            property("flex", "1")
                                            overflow("hidden")
                                            property("text-overflow", "ellipsis")
                                            property("white-space", "nowrap")
                                        }
                                    }) {
                                        Text("$groupKey (${groupCrashes.size})")
                                    }
                                    Icon(if (expanded) "expand_less" else "expand_more", size = 20.px, color = FlagentTheme.textLight(themeMode))
                                }
                                if (expanded) {
                                    groupCrashes.forEach { crash ->
                                        Div({
                                            style {
                                                padding(12.px, 16.px)
                                                marginLeft(16.px)
                                                marginBottom(8.px)
                                                backgroundColor(FlagentTheme.cardBg(themeMode))
                                                borderRadius(6.px)
                                                border(1.px, LineStyle.Solid, FlagentTheme.cardBorder(themeMode))
                                                cursor("pointer")
                                            }
                                            onClick { selectedCrash = if (selectedCrash?.id == crash.id) null else crash }
                                        }) {
                                            Div({
                                                style {
                                                    display(DisplayStyle.Flex)
                                                    justifyContent(JustifyContent.SpaceBetween)
                                                    alignItems(AlignItems.FlexStart)
                                                }
                                            }) {
                                                Div({
                                                    style {
                                                        flex(1)
                                                        overflow("hidden")
                                                        property("text-overflow", "ellipsis")
                                                    }
                                                }) {
                                                    Span({
                                                        style {
                                                            fontWeight(600)
                                                            fontSize(14.px)
                                                            color(FlagentTheme.text(themeMode))
                                                        }
                                                    }) {
                                                        Text(crash.message.take(120).ifEmpty { LocalizedStrings.noMessage } + if (crash.message.length > 120) "…" else "")
                                                    }
                                                    Div({
                                                        style {
                                                            display(DisplayStyle.Flex)
                                                            gap(12.px)
                                                            marginTop(4.px)
                                                            fontSize(12.px)
                                                            color(FlagentTheme.textLight(themeMode))
                                                        }
                                                    }) {
                                                        Span { Text(crash.platform) }
                                                        Span { Text((kotlin.js.Date(crash.timestamp.toDouble()).toISOString().substring(0, 16).replace("T", " "))) }
                                                        crash.appVersion?.let { Span { Text(it) } }
                                                    }
                                                }
                                                Icon(if (selectedCrash?.id == crash.id) "expand_less" else "expand_more", size = 20.px, color = FlagentTheme.textLight(themeMode))
                                            }
                                            if (selectedCrash?.id == crash.id) {
                                                CrashStackSection(themeMode, crash.stackTrace)
                                                crash.activeFlagKeys?.takeIf { it.isNotEmpty() }?.let { keys ->
                                                    CrashActiveFlagsSection(themeMode, keys, flags)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        crashReports.forEach { crash ->
                            Div({
                                style {
                                    padding(16.px)
                                    backgroundColor(FlagentTheme.inputBg(themeMode))
                                    borderRadius(6.px)
                                    border(1.px, LineStyle.Solid, FlagentTheme.cardBorder(themeMode))
                                    cursor("pointer")
                                }
                                onClick { selectedCrash = if (selectedCrash?.id == crash.id) null else crash }
                            }) {
                                Div({
                                    style {
                                        display(DisplayStyle.Flex)
                                        justifyContent(JustifyContent.SpaceBetween)
                                        alignItems(AlignItems.FlexStart)
                                    }
                                }) {
                                    Div({
                                        style {
                                            flex(1)
                                            overflow("hidden")
                                            property("text-overflow", "ellipsis")
                                        }
                                    }) {
                                        Span({
                                            style {
                                                fontWeight(600)
                                                fontSize(14.px)
                                                color(FlagentTheme.text(themeMode))
                                            }
                                        }) {
                                            Text(crash.message.take(120).ifEmpty { LocalizedStrings.noMessage } + if (crash.message.length > 120) "…" else "")
                                        }
                                        Div({
                                            style {
                                                display(DisplayStyle.Flex)
                                                gap(12.px)
                                                marginTop(4.px)
                                                fontSize(12.px)
                                                color(FlagentTheme.textLight(themeMode))
                                            }
                                        }) {
                                            Span { Text(crash.platform) }
                                            Span { Text((kotlin.js.Date(crash.timestamp.toDouble()).toISOString().substring(0, 16).replace("T", " "))) }
                                            crash.appVersion?.let { Span { Text(it) } }
                                        }
                                    }
                                    Icon(if (selectedCrash?.id == crash.id) "expand_less" else "expand_more", size = 20.px, color = FlagentTheme.textLight(themeMode))
                                }
                                if (selectedCrash?.id == crash.id) {
                                    CrashStackSection(themeMode, crash.stackTrace)
                                    crash.activeFlagKeys?.takeIf { it.isNotEmpty() }?.let { keys ->
                                        CrashActiveFlagsSection(themeMode, keys, flags)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else if (isLoading) {
            SkeletonLoader(height = 200.px)
        } else if (error != null) {
            Div({
                style {
                    padding(16.px)
                    backgroundColor(FlagentTheme.errorBg(themeMode))
                    borderRadius(6.px)
                    color(FlagentTheme.errorText(themeMode))
                    fontSize(14.px)
                }
            }) {
                Text(error!!)
            }
        } else if (flags.isEmpty()) {
            EmptyState(
                icon = "bug_report",
                title = LocalizedStrings.noFlags,
                description = LocalizedStrings.noFlagsDescription
            )
        } else {
            Div({
                style {
                    display(DisplayStyle.Flex)
                    flexDirection(FlexDirection.Column)
                    gap(8.px)
                }
            }) {
                flags.forEach { flag ->
                    A(href = Route.FlagMetrics(flag.id, MetricType.CRASH_RATE.name).path(), attrs = {
                        style {
                            display(DisplayStyle.Flex)
                            alignItems(AlignItems.Center)
                            justifyContent(JustifyContent.SpaceBetween)
                            padding(16.px)
                            backgroundColor(FlagentTheme.inputBg(themeMode))
                            borderRadius(6.px)
                            border(1.px, LineStyle.Solid, FlagentTheme.cardBorder(themeMode))
                            textDecoration("none")
                            color(FlagentTheme.text(themeMode))
                            property("transition", "background-color 0.2s")
                        }
                        onMouseEnter {
                            (it.target as org.w3c.dom.HTMLElement).style.backgroundColor = FlagentTheme.inputBorder(themeMode).toString()
                        }
                        onMouseLeave {
                            (it.target as org.w3c.dom.HTMLElement).style.backgroundColor = FlagentTheme.inputBg(themeMode).toString()
                        }
                        onClick { e ->
                            e.preventDefault()
                            Router.navigateTo(Route.FlagMetrics(flag.id, MetricType.CRASH_RATE.name))
                        }
                    }) {
                        Span({
                            style {
                                fontWeight(600)
                                fontSize(14.px)
                            }
                        }) {
                            Text(flag.key)
                        }
                        Span({
                            style {
                                fontSize(12.px)
                                color(FlagentTheme.textLight(themeMode))
                            }
                        }) {
                            Text(LocalizedStrings.viewCrashRate)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CrashActiveFlagsSection(themeMode: ThemeMode, activeFlagKeys: List<String>, flags: List<FlagResponse>) {
    Div({
        style {
            marginTop(12.px)
        }
    }) {
        Span({
            style {
                fontSize(12.px)
                fontWeight(600)
                color(FlagentTheme.textLight(themeMode))
                display(DisplayStyle.Block)
                marginBottom(6.px)
            }
        }) {
            Text(LocalizedStrings.activeFlags)
        }
        Div({
            style {
                display(DisplayStyle.Flex)
                flexWrap(FlexWrap.Wrap)
                gap(8.px)
            }
        }) {
            activeFlagKeys.forEach { key ->
                val flag = flags.find { it.key == key }
                if (flag != null) {
                    A(href = Route.FlagDetail(flag.id).path(), attrs = {
                        style {
                            padding(6.px, 10.px)
                            backgroundColor(FlagentTheme.Primary)
                            color(Color.white)
                            borderRadius(6.px)
                            fontSize(12.px)
                            textDecoration("none")
                        }
                        onClick { e ->
                            e.preventDefault()
                            Router.navigateTo(Route.FlagDetail(flag.id))
                        }
                    }) {
                        Text(flag.key)
                    }
                } else {
                    Span({
                        style {
                            padding(6.px, 10.px)
                            backgroundColor(FlagentTheme.inputBg(themeMode))
                            color(FlagentTheme.text(themeMode))
                            borderRadius(6.px)
                            fontSize(12.px)
                        }
                    }) {
                        Text(key)
                    }
                }
            }
        }
    }
}

@Composable
private fun CrashStackSection(themeMode: ThemeMode, stackTrace: String) {
    Div({
        style {
            marginTop(12.px)
        }
    }) {
        Div({
            style {
                display(DisplayStyle.Flex)
                justifyContent(JustifyContent.SpaceBetween)
                alignItems(AlignItems.Center)
                marginBottom(6.px)
            }
        }) {
            Span({
                style {
                    fontSize(12.px)
                    fontWeight(600)
                    color(FlagentTheme.textLight(themeMode))
                }
            }) {
                Text(LocalizedStrings.stackTrace)
            }
            Button({
                style {
                    padding(4.px, 10.px)
                    border(1.px, LineStyle.Solid, FlagentTheme.cardBorder(themeMode))
                    borderRadius(4.px)
                    cursor("pointer")
                    fontSize(12.px)
                    backgroundColor(FlagentTheme.inputBg(themeMode))
                    color(FlagentTheme.text(themeMode))
                }
                onClick {
                    try {
                        val clip = js("navigator.clipboard")
                        if (clip != null && clip != js("undefined")) {
                            clip.writeText(stackTrace)
                        }
                    } catch (_: Throwable) { }
                }
            }) {
                Text(LocalizedStrings.copyLabel)
            }
        }
        Pre({
            style {
                padding(12.px)
                backgroundColor(FlagentTheme.cardBg(themeMode))
                color(FlagentTheme.text(themeMode))
                borderRadius(6.px)
                fontSize(12.px)
                overflow("auto")
                maxHeight(300.px)
                whiteSpace("pre-wrap")
                property("word-break", "break-all")
                margin(0.px)
            }
        }) {
            Text(stackTrace)
        }
    }
}
