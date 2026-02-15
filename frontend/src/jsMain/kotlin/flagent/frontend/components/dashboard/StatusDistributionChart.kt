package flagent.frontend.components.dashboard

import androidx.compose.runtime.*
import flagent.frontend.state.ThemeMode
import flagent.frontend.theme.FlagentTheme
import flagent.frontend.util.currentTimeMillis
import kotlinx.browser.document
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.w3c.dom.HTMLCanvasElement

/**
 * Data for a single pie slice: label and count.
 */
data class StatusSlice(val label: String, val count: Int, val color: String)

/** Hex palette for Chart.js (does not resolve CSS vars). Matches design-system tokens: success, error, primary, secondary. */
private val CHART_PALETTE = arrayOf("#10B981", "#EF4444", "#0EA5E9", "#14B8A6")

/**
 * Pie/doughnut chart for flag status distribution (enabled, disabled, with segments, experiments).
 */
@Composable
fun StatusDistributionChart(
    slices: List<StatusSlice>,
    title: String,
    themeMode: ThemeMode
) {
    val canvasId = remember { "status-chart-${currentTimeMillis()}" }
    var chartInstance by remember { mutableStateOf<dynamic>(null) }

    LaunchedEffect(slices, themeMode) {
        val canvas = document.getElementById(canvasId) as? HTMLCanvasElement
        if (canvas != null) destroyChartOnCanvas(canvas)
        chartInstance = null
        if (canvas != null && slices.isNotEmpty() && slices.any { it.count > 0 }) {
            chartInstance = createPieChart(canvas, slices, title, themeMode)
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
            height(260.px)
            position(Position.Relative)
        }
    }) {
        Canvas({
            id(canvasId)
            style { width(100.percent); height(100.percent) }
        })
    }
}

private fun destroyChartOnCanvas(canvas: HTMLCanvasElement) {
    val Chart = js("window.Chart")
    val existing = Chart.getChart(canvas)
    if (existing != null) existing.destroy()
}

private fun createPieChart(
    canvas: HTMLCanvasElement,
    slices: List<StatusSlice>,
    title: String,
    themeMode: ThemeMode
): dynamic {
    val labels = slices.map { it.label }.toTypedArray()
    val data = slices.map { it.count }.toTypedArray()
    // Chart.js does not resolve CSS variables (var(--flagent-*)); use hex palette by index
    val colors = slices.mapIndexed { i, s ->
        if (s.color.startsWith("var(")) CHART_PALETTE[i % CHART_PALETTE.size] else s.color
    }.toTypedArray()
    val Chart = js("window.Chart")
    val isDark = themeMode == ThemeMode.Dark
    val titleColor = if (isDark) "rgba(255,255,255,0.9)" else "rgba(0,0,0,0.85)"
    val legendColor = if (isDark) "rgba(255,255,255,0.7)" else "rgba(0,0,0,0.6)"

    val config = js("""({
        type: 'doughnut',
        data: { labels: [], datasets: [{ data: [], backgroundColor: [], borderColor: 'transparent', borderWidth: 2, hoverOffset: 4 }] },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                title: { display: true, text: '', font: { size: 16, weight: 'bold' }, color: '' },
                legend: { display: true, position: 'bottom', labels: { color: '', padding: 16 } }
            },
            cutout: '55%'
        }
    })""")
    config.data.labels = labels
    config.data.datasets[0].data = data
    config.data.datasets[0].backgroundColor = colors
    config.options.plugins.title.text = title
    config.options.plugins.title.color = titleColor
    config.options.plugins.legend.labels.color = legendColor
    return js("new Chart(canvas, config)")
}
