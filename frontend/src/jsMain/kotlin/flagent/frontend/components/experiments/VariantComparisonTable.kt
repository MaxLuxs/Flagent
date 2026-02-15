package flagent.frontend.components.experiments

import androidx.compose.runtime.Composable
import flagent.api.model.DistributionResponse
import flagent.api.model.VariantResponse
import flagent.frontend.i18n.LocalizedStrings
import flagent.frontend.state.LocalThemeMode
import flagent.frontend.theme.FlagentTheme
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

/**
 * OSS variant comparison: table of variant key and distribution % from flag config.
 * No conversion/significance (that is Enterprise A/B Insights).
 */
@Composable
fun VariantComparisonTable(
    variants: List<VariantResponse>,
    distributions: List<DistributionResponse>
) {
    if (variants.isEmpty()) return
    val themeMode = LocalThemeMode.current
    val variantIds = variants.map { it.id }.toSet()
    val distByVariant = if (distributions.isEmpty()) {
        emptyMap<Int, Int>()
    } else {
        distributions
            .filter { it.variantID in variantIds }
            .groupBy { it.variantID }
            .mapValues { (_, list) -> list.sumOf { it.percent } }
    }
    val useEqual = distByVariant.isEmpty()
    val equalPct = 100 / variants.size.coerceAtLeast(1)

    Table({
        style {
            width(100.percent)
            fontSize(12.px)
            property("border-collapse", "collapse")
        }
    }) {
        Thead {
            Tr {
                Th({
                    style {
                        textAlign("left")
                        padding(4.px, 6.px)
                        color(FlagentTheme.textLight(themeMode))
                        fontWeight(600)
                        property("border-bottom", "1px solid ${FlagentTheme.cardBorder(themeMode)}")
                    }
                }) { Text(LocalizedStrings.variants) }
                Th({
                    style {
                        textAlign("right")
                        padding(4.px, 6.px)
                        color(FlagentTheme.textLight(themeMode))
                        fontWeight(600)
                        property("border-bottom", "1px solid ${FlagentTheme.cardBorder(themeMode)}")
                    }
                }) { Text(LocalizedStrings.distribution) }
            }
        }
        Tbody {
            variants.forEach { v ->
                val pct = if (useEqual) equalPct else (distByVariant[v.id] ?: 0).coerceIn(0, 100)
                Tr {
                    Td({
                        style {
                            padding(4.px, 6.px)
                            color(FlagentTheme.text(themeMode))
                            property("border-bottom", "1px solid ${FlagentTheme.cardBorder(themeMode)}")
                        }
                    }) { Text(v.key) }
                    Td({
                        style {
                            padding(4.px, 6.px)
                            textAlign("right")
                            color(FlagentTheme.text(themeMode))
                            property("border-bottom", "1px solid ${FlagentTheme.cardBorder(themeMode)}")
                        }
                    }) { Text("$pct%") }
                }
            }
        }
    }
}
