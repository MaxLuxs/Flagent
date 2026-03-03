package flagent.frontend.components.dashboard

import androidx.compose.runtime.*
import flagent.frontend.api.TopFlagEntryResponse
import flagent.frontend.i18n.LocalizedStrings
import flagent.frontend.state.ThemeMode
import flagent.frontend.theme.FlagentTheme
import flagent.frontend.util.currentTimeMillis
import kotlinx.browser.document
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.w3c.dom.HTMLCanvasElement

/**
 * Horizontal bar chart for top flags by evaluation count.
 * Uses Chart.js; theme-aware. Limits labels for readability.
 */
@Composable
fun TopFlagsBarChart(
    topFlags: List<TopFlagEntryResponse>,
    title: String,
    themeMode: ThemeMode,
    maxBars: Int = 8
) {
    val canvasId = remember { "top-flags-bar-${currentTimeMillis()}" }
    var chartInstance by remember { mutableStateOf<dynamic>(null) }
    val limited = topFlags.take(maxBars)

    LaunchedEffect(limited, themeMode) {
        val canvas = document.getElementById(canvasId) as? HTMLCanvasElement
        if (canvas != null) destroyChartOnCanvas(canvas)
        chartInstance = null
        if (canvas != null && limited.isNotEmpty()) {
            chartInstance = createBarChart(canvas, limited, title, themeMode)
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

private fun destroyChartOnCanvas(canvas: HTMLCanvasElement) {
    val Chart = js("window.Chart")
    val existing = Chart.getChart(canvas)
    if (existing != null) existing.destroy()
}

private fun createBarChart(
    canvas: HTMLCanvasElement,
    topFlags: List<TopFlagEntryResponse>,
    title: String,
    themeMode: ThemeMode
): dynamic {
    val labels = topFlags.map { entry ->
        val key = entry.flagKey.ifBlank { LocalizedStrings.flagNumber(entry.flagId) }
        if (key.length > 20) key.take(17) + "…" else key
    }.toTypedArray()
    val data = topFlags.map { it.evaluationCount.toInt() }.toTypedArray()
    val Chart = js("window.Chart")
    val gridColor = FlagentTheme.cardBorder(themeMode).toString()
    val tickColor = FlagentTheme.text(themeMode).toString()
    val titleColor = FlagentTheme.text(themeMode).toString()
    val barColor = FlagentTheme.Primary.toString()

    val config = js("""({
        type: 'bar',
        data: { labels: [], datasets: [{ label: 'Evaluations', data: [], backgroundColor: '', borderColor: '', borderWidth: 1 }] },
        options: {
            indexAxis: 'y',
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                title: { display: true, text: '', font: { size: 16, weight: 'bold' }, color: '' },
                legend: { display: false }
            },
            scales: {
                x: { beginAtZero: true, grid: { color: '' }, ticks: { color: '' } },
                y: { grid: { color: '' }, ticks: { color: '', maxTicksLimit: 12 } }
            }
        }
    })""")
    config.data.labels = labels
    config.data.datasets[0].data = data
    config.data.datasets[0].backgroundColor = barColor
    config.data.datasets[0].borderColor = barColor
    config.options.plugins.title.text = title
    config.options.plugins.title.color = titleColor
    config.options.scales.x.grid.color = gridColor
    config.options.scales.x.ticks.color = tickColor
    config.options.scales.y.grid.color = gridColor
    config.options.scales.y.ticks.color = tickColor
    return js("new Chart(canvas, config)")
}
