package flagent.frontend.components.metrics

import androidx.compose.runtime.*
import flagent.frontend.api.MetricType
import flagent.frontend.components.common.EmptyState
import flagent.frontend.components.experiments.ExperimentInsightsCard
import flagent.frontend.components.common.SkeletonLoader
import flagent.frontend.config.AppConfig
import flagent.frontend.i18n.LocalizedStrings
import flagent.frontend.state.LocalThemeMode
import flagent.frontend.theme.FlagentTheme
import flagent.frontend.util.currentTimeMillis
import flagent.frontend.util.format
import flagent.frontend.util.textTransform
import flagent.frontend.viewmodel.MetricsViewModel
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

/**
 * Metrics Dashboard component (Phase 3)
 * @param initialMetricType Optional metric type from URL query (e.g. CRASH_RATE when navigating from Crash page)
 */
@Composable
fun MetricsDashboard(flagId: Int, initialMetricType: String? = null) {
    val themeMode = LocalThemeMode.current
    val viewModel = remember { MetricsViewModel(flagId) }
    
    LaunchedEffect(flagId, initialMetricType) {
        val metricType = initialMetricType?.let { runCatching { MetricType.valueOf(it) }.getOrNull() }
        if (metricType != null) {
            viewModel.selectedMetricType = metricType
        }
        if (AppConfig.isOpenSource) {
            viewModel.loadCoreStats()
        } else {
            viewModel.loadMetrics()
            viewModel.loadAggregation()
            viewModel.loadExperimentInsights()
        }
    }
    
    val isOss = AppConfig.isOpenSource

    Div({
        style {
            backgroundColor(FlagentTheme.cardBg(themeMode))
            borderRadius(8.px)
            padding(24.px)
            property("border", "1px solid ${FlagentTheme.cardBorder(themeMode)}")
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
            Text(if (isOss) "API Evaluation Stats" else "Metrics & Analytics")
        }

        if (!isOss) {
            ExperimentInsightsCard(viewModel.experimentInsights)
        }
        
        // Filters
        Div({
            style {
                display(DisplayStyle.Flex)
                gap(16.px)
                marginBottom(24.px)
                flexWrap(FlexWrap.Wrap)
            }
        }) {
            // Metric type selector (Enterprise only)
            if (!isOss) Select({
                onChange { event ->
                    val value = event.value
                    viewModel.selectedMetricType = if (value.isNullOrBlank()) null else MetricType.valueOf(value)
                    viewModel.loadMetrics()
                    viewModel.loadAggregation()
                }
                style {
                    padding(10.px, 16.px)
                    border(1.px, LineStyle.Solid, Color("#E2E8F0"))
                    borderRadius(6.px)
                    fontSize(14.px)
                }
            }) {
                Option("", { 
                    if (viewModel.selectedMetricType == null) attr("selected", "")
                }) {
                    Text("All Metrics")
                }
                MetricType.values().forEach { type ->
                    Option(type.name, { 
                        if (viewModel.selectedMetricType == type) attr("selected", "")
                    }) {
                        Text(type.name.replace("_", " "))
                    }
                }
            }
            
            // Time range buttons
            Button({
                onClick {
                    viewModel.startTime = currentTimeMillis() - 3600000 // 1 hour
                    viewModel.endTime = currentTimeMillis()
                    if (isOss) viewModel.loadCoreStats() else {
                        viewModel.loadMetrics()
                        viewModel.loadAggregation()
                        viewModel.loadExperimentInsights()
                    }
                }
                style {
                    padding(10.px, 16.px)
                    backgroundColor(FlagentTheme.Primary)
                    color(Color.white)
                    border(0.px)
                    borderRadius(6.px)
                    cursor("pointer")
                    fontSize(14.px)
                }
            }) {
                Text("Last Hour")
            }
            
            Button({
                onClick {
                    viewModel.startTime = currentTimeMillis() - 86400000 // 24 hours
                    viewModel.endTime = currentTimeMillis()
                    if (isOss) viewModel.loadCoreStats() else {
                        viewModel.loadMetrics()
                        viewModel.loadAggregation()
                        viewModel.loadExperimentInsights()
                    }
                }
                style {
                    padding(10.px, 16.px)
                    backgroundColor(FlagentTheme.Primary)
                    color(Color.white)
                    border(0.px)
                    borderRadius(6.px)
                    cursor("pointer")
                    fontSize(14.px)
                }
            }) {
                Text("Last 24 Hours")
            }
        }
        
        // OSS: Core evaluation stats
        if (isOss) {
            viewModel.coreStats?.let { stats ->
                Div({
                    style {
                        display(DisplayStyle.Grid)
                        property("grid-template-columns", "repeat(auto-fit, minmax(200px, 1fr))")
                        gap(16.px)
                        marginBottom(24.px)
                    }
                }) {
                    MetricCard(themeMode, "Total evaluations", stats.evaluationCount.toString())
                }
                if (stats.timeSeries.isNotEmpty()) {
                    Div({
                        style {
                            marginBottom(24.px)
                            padding(20.px)
                            backgroundColor(FlagentTheme.cardBg(themeMode))
                            borderRadius(8.px)
                            property("box-shadow", "0 2px 8px rgba(0,0,0,0.1)")
                        }
                    }) {
                        OverviewChart(
                            timeSeries = stats.timeSeries,
                            title = LocalizedStrings.evaluationsOverTime
                        )
                    }
                }
            }
        }
        
        // Aggregation stats (Enterprise only)
        if (!isOss) viewModel.aggregation?.let { agg ->
            Div({
                style {
                    display(DisplayStyle.Grid)
                    property("grid-template-columns", "repeat(auto-fit, minmax(200px, 1fr))")
                    gap(16.px)
                    marginBottom(24.px)
                }
            }) {
                MetricCard(themeMode, "Count", agg.count.toString())
                MetricCard(themeMode, "Average", agg.avg.format(2))
                MetricCard(themeMode, "Min", agg.min.format(2))
                MetricCard(themeMode, "Max", agg.max.format(2))
            }
        }
        
        // Loading state
        if (viewModel.isLoading) {
            SkeletonLoader(height = 200.px)
        } else if (isOss && viewModel.coreStats == null && viewModel.error == null) {
            SkeletonLoader(height = 200.px)
        } else if (!isOss && viewModel.metrics.isEmpty()) {
            EmptyState(
                icon = "analytics",
                title = "No metrics available",
                description = "Start sending metrics to see analytics here"
            )
        } else if (isOss && viewModel.coreStats != null && viewModel.coreStats!!.evaluationCount == 0L) {
            EmptyState(
                icon = "analytics",
                title = "No evaluations yet",
                description = "API evaluation calls for this flag will appear here"
            )
        } else if (!isOss) {
            // Metrics chart (Enterprise only)
            if (viewModel.selectedMetricType != null) {
                MetricsChart(
                    metrics = viewModel.metrics,
                    metricType = viewModel.selectedMetricType!!,
                    title = "${viewModel.selectedMetricType!!.name} Over Time"
                )
            } else {
                P({
                    style {
                        fontSize(14.px)
                        color(FlagentTheme.textLight(themeMode))
                        margin(0.px)
                        marginTop(16.px)
                    }
                }) {
                    Text("${viewModel.metrics.size} data points collected. Select a metric type to view chart.")
                }
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
private fun MetricCard(themeMode: flagent.frontend.state.ThemeMode, label: String, value: String) {
    Div({
        style {
            padding(16.px)
            backgroundColor(FlagentTheme.inputBg(themeMode))
            borderRadius(6.px)
            border(1.px, LineStyle.Solid, FlagentTheme.inputBorder(themeMode))
        }
    }) {
        P({
            style {
                fontSize(12.px)
                color(FlagentTheme.textLight(themeMode))
                margin(0.px)
                marginBottom(8.px)
                textTransform("uppercase")
                property("letter-spacing", "0.05em")
            }
        }) {
            Text(label)
        }
        P({
            style {
                fontSize(24.px)
                fontWeight(600)
                color(FlagentTheme.text(themeMode))
                margin(0.px)
            }
        }) {
            Text(value)
        }
    }
}
