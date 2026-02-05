package flagent.frontend.components.metrics

import androidx.compose.runtime.*
import flagent.api.model.FlagResponse
import flagent.frontend.api.ApiClient
import flagent.frontend.components.Icon
import flagent.frontend.components.common.EmptyState
import flagent.frontend.components.common.SkeletonLoader
import flagent.frontend.api.MetricType
import flagent.frontend.config.AppConfig
import flagent.frontend.navigation.Route
import flagent.frontend.navigation.Router
import flagent.frontend.theme.FlagentTheme
import flagent.frontend.util.ErrorHandler
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

/**
 * Crash Analytics dashboard (Enterprise only).
 * Lists flags with links to per-flag metrics. Navigates to MetricsDashboard with CRASH_RATE preselected.
 */
@Composable
fun CrashDashboard() {
    if (!AppConfig.Features.enableCrashAnalytics) return

    var flags by remember { mutableStateOf<List<FlagResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

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

    Div({
        style {
            padding(24.px)
            backgroundColor(Color.white)
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
                    color(Color("#1E293B"))
                    margin(0.px)
                }
            }) {
                Text("Crash Analytics")
            }
        }
        P({
            style {
                fontSize(14.px)
                color(Color("#64748B"))
                margin(0.px)
                marginBottom(24.px)
            }
        }) {
            Text("Track crash rate per flag and variant. Select a flag to view CRASH_RATE metrics. Integrates with Anomaly Detection and Smart Rollout.")
        }

        if (isLoading) {
            SkeletonLoader(height = 200.px)
        } else if (error != null) {
            Div({
                style {
                    padding(16.px)
                    backgroundColor(Color("#FEE2E2"))
                    borderRadius(6.px)
                    color(Color("#991B1B"))
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
                            backgroundColor(Color("#F8FAFC"))
                            borderRadius(6.px)
                            border(1.px, LineStyle.Solid, Color("#E2E8F0"))
                            textDecoration("none")
                            color(FlagentTheme.WorkspaceText)
                            property("transition", "background-color 0.2s")
                        }
                        onMouseEnter {
                            (it.target as org.w3c.dom.HTMLElement).style.backgroundColor = "#F1F5F9"
                        }
                        onMouseLeave {
                            (it.target as org.w3c.dom.HTMLElement).style.backgroundColor = "#F8FAFC"
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
                                color(Color("#64748B"))
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
