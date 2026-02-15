package flagent.frontend.components.dashboard

import androidx.compose.runtime.*
import flagent.frontend.api.TimeSeriesEntryResponse
import flagent.frontend.state.ThemeMode
import flagent.frontend.theme.FlagentTheme
import flagent.frontend.util.currentTimeMillis
import kotlinx.browser.document
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.w3c.dom.HTMLCanvasElement

/**
 * Line chart for flag activity (evaluations) over time.
 * Uses Chart.js; theme-aware colors.
 */
@Composable
fun ActivityChart(
    timeSeries: List<TimeSeriesEntryResponse>,
    title: String,
    themeMode: ThemeMode
) {
    val canvasId = remember { "activity-chart-${currentTimeMillis()}" }
    var chartInstance by remember { mutableStateOf<dynamic>(null) }

    LaunchedEffect(timeSeries, themeMode) {
        chartInstance?.destroy()
        val canvas = document.getElementById(canvasId) as? HTMLCanvasElement
        if (canvas != null && timeSeries.isNotEmpty()) {
            chartInstance = createActivityChart(canvas, timeSeries, title, themeMode)
        }
    }

    DisposableEffect(canvasId) {
        onDispose { chartInstance?.destroy() }
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

private fun createActivityChart(
    canvas: HTMLCanvasElement,
    timeSeries: List<TimeSeriesEntryResponse>,
    title: String,
    themeMode: ThemeMode
): dynamic {
    val sorted = timeSeries.sortedBy { it.timestamp }
    val labels = sorted.map { formatTime(it.timestamp) }.toTypedArray()
    val data = sorted.map { it.count.toInt() }.toTypedArray()
    val Chart = js("window.Chart")
    val isDark = themeMode == ThemeMode.Dark
    val gridColor = if (isDark) "rgba(255,255,255,0.1)" else "rgba(0,0,0,0.08)"
    val tickColor = if (isDark) "rgba(255,255,255,0.7)" else "rgba(0,0,0,0.6)"
    val titleColor = if (isDark) "rgba(255,255,255,0.9)" else "rgba(0,0,0,0.85)"
    val borderColor = FlagentTheme.Primary.toString()
    val fillColor = "rgba(14, 165, 233, 0.15)"

    val config = js("""({
        type: 'line',
        data: { labels: [], datasets: [{ label: 'Activity', data: [], borderColor: '', backgroundColor: '', borderWidth: 2, fill: true, tension: 0.4, pointRadius: 3, pointHoverRadius: 5 }] },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                title: { display: true, text: '', font: { size: 16, weight: 'bold' }, color: '' },
                legend: { display: false }
            },
            scales: {
                y: { beginAtZero: true, grid: { color: '' }, ticks: { color: '' } },
                x: { grid: { color: '' }, ticks: { color: '', maxTicksLimit: 12 } }
            },
            interaction: { mode: 'nearest', axis: 'x', intersect: false }
        }
    })""")
    config.data.labels = labels
    config.data.datasets[0].data = data
    config.data.datasets[0].borderColor = borderColor
    config.data.datasets[0].backgroundColor = fillColor
    config.options.plugins.title.text = title
    config.options.plugins.title.color = titleColor
    config.options.scales.y.grid.color = gridColor
    config.options.scales.y.ticks.color = tickColor
    config.options.scales.x.grid.color = gridColor
    config.options.scales.x.ticks.color = tickColor
    return js("new Chart(canvas, config)")
}

private fun formatTime(timestamp: Long): String {
    val date: dynamic = js("new Date(timestamp)")
    val month = (date.getMonth().toString().toIntOrNull() ?: 0) + 1
    val day = date.getDate()
    val hours = date.getHours().toString().padStart(2, '0')
    val minutes = date.getMinutes().toString().padStart(2, '0')
    return "$day/$month $hours:$minutes"
}
