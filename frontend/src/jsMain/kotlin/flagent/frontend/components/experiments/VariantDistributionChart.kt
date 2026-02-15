package flagent.frontend.components.experiments

import androidx.compose.runtime.Composable
import flagent.api.model.DistributionResponse
import flagent.api.model.VariantResponse
import flagent.frontend.state.LocalThemeMode
import flagent.frontend.theme.FlagentTheme
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

/**
 * Simple horizontal bar showing variant distribution (percent per variant).
 * If distributions are empty, shows equal segments by variant count.
 */
@Composable
fun VariantDistributionChart(
    variants: List<VariantResponse>,
    distributions: List<DistributionResponse>,
    height: CSSSizeValue<CSSUnit.px> = 8.px
) {
    if (variants.isEmpty()) return
    val themeMode = LocalThemeMode.current
    val variantIds = variants.map { it.id }.toSet()
    val distByVariant = if (distributions.isEmpty()) {
        emptyMap()
    } else {
        distributions
            .filter { it.variantID in variantIds }
            .groupBy { it.variantID }
            .mapValues { (_, list) -> list.sumOf { it.percent } }
    }
    val useEqual = distByVariant.isEmpty()
    val equalPct = 100.0 / variants.size.coerceAtLeast(1)
    val colors = listOf(
        FlagentTheme.Primary,
        FlagentTheme.Info,
        FlagentTheme.Success,
        FlagentTheme.Warning,
        FlagentTheme.Secondary
    )
    Div({
        style {
            display(DisplayStyle.Flex)
            width(100.percent)
            borderRadius(4.px)
            overflow("hidden")
            property("min-height", height.toString())
        }
    }) {
        variants.forEachIndexed { index, v ->
            val pct = if (useEqual) equalPct else (distByVariant[v.id] ?: 0).toDouble().coerceIn(0.0, 100.0)
            Div({
                style {
                    if (useEqual) flex(1) else width(pct.percent)
                    backgroundColor(colors.getOrElse(index) { colors[index % colors.size] })
                    opacity(0.85)
                }
            }) {}
        }
    }
}
