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
import flagent.frontend.state.LocalThemeMode
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
    if (!AppConfig.Features.enableCrashAnalytics) return

    val themeMode = LocalThemeMode.current
    var flags by remember { mutableStateOf<List<FlagResponse>>(emptyList()) }
    var crashReports by remember { mutableStateOf<List<CrashReportResponse>>(emptyList()) }
    var crashTotal by remember { mutableStateOf(0L) }
    var selectedCrash by remember { mutableStateOf<CrashReportResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isLoadingCrashes by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var crashReportsError by remember { mutableStateOf<String?>(null) }
    var activeTab by remember { mutableStateOf("reports") }
    var reportsRefreshTrigger by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        isLoading = true
        error = null
        ErrorHandler.withErrorHandling(
            block = {
                flags = ApiClient.getFlags().first
            },
            onError = { err ->
                error = ErrorHandler.getUserMessage(err)
            }
        )
        isLoading = false
    }

    LaunchedEffect(activeTab, reportsRefreshTrigger) {
        if (activeTab == "reports") {
            isLoadingCrashes = true
            crashReportsError = null
            ErrorHandler.withErrorHandling(
                block = {
                    val resp = ApiClient.getCrashes(limit = 50)
                    crashReports = resp.items
                    crashTotal = resp.total
                },
                onError = { err ->
                    crashReportsError = ErrorHandler.getUserMessage(err)
                    crashReports = emptyList()
                    crashTotal = 0
                }
            )
            isLoadingCrashes = false
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
            Icon("bug_report", size = 28.px, color = Color("#DC2626"))
            H2({
                style {
                    fontSize(20.px)
                    fontWeight(600)
                    color(FlagentTheme.text(themeMode))
                    margin(0.px)
                }
            }) {
                Text("Crash Analytics")
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
            Text("Track crash reports from SDK and crash rate per flag. Integrates with Anomaly Detection and Smart Rollout.")
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
            }) { Text("Crash reports") }
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
            }) { Text("By flags (CRASH_RATE)") }
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
                            Text("Ensure X-API-Key is set (Settings or after creating a tenant).")
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
                        Text("Retry")
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
                        Text("$crashTotal crash reports")
                    }
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
                                        Text(crash.message.take(120).ifEmpty { "No message" } + if (crash.message.length > 120) "…" else "")
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
                                Pre({
                                    style {
                                        marginTop(12.px)
                                        padding(12.px)
                                        backgroundColor(FlagentTheme.inputBg(themeMode))
                                        color(FlagentTheme.text(themeMode))
                                        borderRadius(6.px)
                                        fontSize(12.px)
                                        overflow("auto")
                                        maxHeight(300.px)
                                        whiteSpace("pre-wrap")
                                        property("word-break", "break-all")
                                    }
                                }) {
                                    Text(crash.stackTrace)
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
                title = "No flags",
                description = "Create flags to track crash rates"
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
                            Text("View CRASH_RATE →")
                        }
                    }
                }
            }
        }
    }
}
