package flagent.frontend.components.metrics

import androidx.compose.runtime.*
import flagent.frontend.api.MetricDataPointResponse
import flagent.frontend.api.MetricType
import flagent.frontend.util.currentTimeMillis
import kotlinx.browser.document
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.w3c.dom.HTMLCanvasElement

/**
 * Metrics Chart component using Chart.js
 */
@Composable
fun MetricsChart(
    metrics: List<MetricDataPointResponse>,
    metricType: MetricType,
    title: String = "Metrics Over Time"
) {
    val canvasId = remember { "metrics-chart-${currentTimeMillis()}" }
    var chartInstance by remember { mutableStateOf<dynamic>(null) }
    
    LaunchedEffect(metrics, metricType) {
        val canvas = document.getElementById(canvasId) as? HTMLCanvasElement
        if (canvas != null) {
            destroyChartOnCanvas(canvas)
        }
        chartInstance = null
        if (canvas != null && metrics.isNotEmpty()) {
            chartInstance = createChart(canvas, metrics, metricType, title)
        }
    }
    
    DisposableEffect(canvasId) {
        onDispose {
            val canvas = document.getElementById(canvasId) as? HTMLCanvasElement
            if (canvas != null) destroyChartOnCanvas(canvas)
            chartInstance = null
        }
    }
    
    Div({
        style {
            width(100.percent)
            height(400.px)
            position(Position.Relative)
        }
    }) {
        Canvas({
            id(canvasId)
            style {
                width(100.percent)
                height(100.percent)
            }
        })
    }
}

/** Destroy any Chart.js instance on this canvas to avoid "Canvas is already in use". */
private fun destroyChartOnCanvas(canvas: HTMLCanvasElement) {
    val Chart = js("window.Chart")
    val existing = Chart.getChart(canvas)
    if (existing != null) existing.destroy()
}

/**
 * Create Chart.js chart
 */
private fun createChart(
    canvas: HTMLCanvasElement,
    metrics: List<MetricDataPointResponse>,
    metricType: MetricType,
    title: String
): dynamic {
    // Sort metrics by timestamp
    val sortedMetrics = metrics.sortedBy { it.timestamp }
    
    // Prepare data
    val labels = sortedMetrics.map { formatTimestamp(it.timestamp) }.toTypedArray()
    val data = sortedMetrics.map { it.metricValue }.toTypedArray()
    
    // Get Chart constructor
    val Chart = js("window.Chart")
    
    // Create chart configuration
    val config = js("""({
        type: 'line',
        data: {
            labels: labels,
            datasets: [{
                label: metricType,
                data: data,
                borderColor: 'rgb(14, 165, 233)',
                backgroundColor: 'rgba(14, 165, 233, 0.1)',
                borderWidth: 2,
                fill: true,
                tension: 0.4,
                pointRadius: 4,
                pointHoverRadius: 6
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                title: {
                    display: true,
                    text: title,
                    font: { size: 16, weight: 'bold' },
                    color: 'rgba(255,255,255,0.9)'
                },
                legend: {
                    display: true,
                    position: 'top',
                    labels: { color: 'rgba(255,255,255,0.7)' }
                },
                tooltip: {
                    mode: 'index',
                    intersect: false,
                    callbacks: {
                        label: function(context) {
                            return metricType + ': ' + context.parsed.y.toFixed(2);
                        }
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    grid: { color: 'rgba(255,255,255,0.1)' },
                    ticks: { color: 'rgba(255,255,255,0.7)' },
                    title: {
                        display: true,
                        text: 'Value',
                        color: 'rgba(255,255,255,0.7)'
                    }
                },
                x: {
                    grid: { color: 'rgba(255,255,255,0.1)' },
                    ticks: { color: 'rgba(255,255,255,0.7)' },
                    title: {
                        display: true,
                        text: 'Time',
                        color: 'rgba(255,255,255,0.7)'
                    }
                }
            },
            interaction: {
                mode: 'nearest',
                axis: 'x',
                intersect: false
            }
        }
    })""")
    
    config.data.labels = labels
    config.data.datasets[0].data = data
    config.data.datasets[0].label = metricType.toString()
    config.options.plugins.title.text = title
    config.options.plugins.tooltip.callbacks.label = { context: dynamic ->
        "${metricType}: ${context.parsed.y.toString().toDouble().format(2)}"
    }
    
    return js("new Chart(canvas, config)")
}

private fun formatTimestamp(timestamp: Long): String {
    val date: dynamic = js("new Date(timestamp)")
    val hours = date.getHours().toString().padStart(2, '0')
    val minutes = date.getMinutes().toString().padStart(2, '0')
    return "$hours:$minutes"
}

private fun Double.format(decimals: Int): String {
    return js("this.toFixed(decimals)") as String
}

/**
 * Overview chart: evaluations (metric count) per time bucket.
 */
@Composable
fun OverviewChart(
    timeSeries: List<flagent.frontend.api.TimeSeriesEntryResponse>,
    title: String = "Evaluations over time"
) {
    val canvasId = remember { "overview-chart-${currentTimeMillis()}" }
    var chartInstance by remember { mutableStateOf<dynamic>(null) }

    LaunchedEffect(timeSeries) {
        val canvas = document.getElementById(canvasId) as? HTMLCanvasElement
        if (canvas != null) destroyChartOnCanvas(canvas)
        chartInstance = null
        if (canvas != null && timeSeries.isNotEmpty()) {
            chartInstance = createOverviewChart(canvas, timeSeries, title)
        }
    }

    DisposableEffect(canvasId) {
        onDispose {
            val canvas = document.getElementById(canvasId) as? HTMLCanvasElement
            if (canvas != null) destroyChartOnCanvas(canvas)
            chartInstance = null
        }
    }

    Div({
        style {
            width(100.percent)
            height(280.px)
            position(Position.Relative)
        }
    }) {
        Canvas({
            id(canvasId)
            style { width(100.percent); height(100.percent) }
        })
    }
}

private fun createOverviewChart(
    canvas: HTMLCanvasElement,
    timeSeries: List<flagent.frontend.api.TimeSeriesEntryResponse>,
    title: String
): dynamic {
    val sorted = timeSeries.sortedBy { it.timestamp }
    val labels = sorted.map { formatTimestamp(it.timestamp) }.toTypedArray()
    val data = sorted.map { it.count.toInt() }.toTypedArray()
    val Chart = js("window.Chart")
    val config = js("""({
        type: 'bar',
        data: { labels: [], datasets: [{ label: 'Evaluations', data: [], backgroundColor: 'rgba(14, 165, 233, 0.6)', borderColor: 'rgb(14, 165, 233)', borderWidth: 1 }] },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
            title: { display: true, text: '', color: 'rgba(255,255,255,0.9)' },
            legend: { display: false }
        },
            scales: { y: { beginAtZero: true }, x: {} }
        }
    })""")
    config.data.labels = labels
    config.data.datasets[0].data = data
    config.options.plugins.title.text = title
    return js("new Chart(canvas, config)")
}
