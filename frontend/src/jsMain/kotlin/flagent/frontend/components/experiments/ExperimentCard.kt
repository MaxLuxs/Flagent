package flagent.frontend.components.experiments

import androidx.compose.runtime.Composable
import flagent.api.model.FlagResponse
import flagent.frontend.components.Icon
import flagent.frontend.config.AppConfig
import flagent.frontend.i18n.LocalizedStrings
import flagent.frontend.navigation.Route
import flagent.frontend.navigation.Router
import flagent.frontend.state.LocalThemeMode
import flagent.frontend.theme.FlagentTheme
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

/**
 * Card view for a single A/B experiment (flag with 2+ variants).
 * Shows key, description, variant chips, status, and actions.
 */
@Composable
fun ExperimentCard(flag: FlagResponse) {
    val themeMode = LocalThemeMode.current
    Div({
        style {
            backgroundColor(FlagentTheme.cardBg(themeMode))
            borderRadius(8.px)
            padding(16.px)
            property("box-shadow", "0 1px 3px rgba(0, 0, 0, 0.08)")
            property("border", "1px solid ${FlagentTheme.cardBorder(themeMode)}")
            property("transition", "box-shadow 0.2s, border-color 0.2s")
        }
        onMouseEnter {
            (it.target as org.w3c.dom.HTMLElement).style.setProperty("box-shadow", "0 4px 12px rgba(0, 0, 0, 0.12)")
            (it.target as org.w3c.dom.HTMLElement).style.setProperty("border-color", FlagentTheme.Primary.toString())
        }
        onMouseLeave {
            (it.target as org.w3c.dom.HTMLElement).style.setProperty("box-shadow", "0 1px 3px rgba(0, 0, 0, 0.08)")
            (it.target as org.w3c.dom.HTMLElement).style.setProperty("border-color", FlagentTheme.cardBorder(themeMode).toString())
        }
    }) {
        Div({
            style {
                display(DisplayStyle.Flex)
                justifyContent(JustifyContent.SpaceBetween)
                alignItems(AlignItems.FlexStart)
                marginBottom(8.px)
            }
        }) {
            Div({
                style {
                    flex(1)
                    property("min-width", "0")
                    cursor("pointer")
                }
                onClick { Router.navigateTo(Route.FlagDetail(flag.id)) }
            }) {
                H3({
                    style {
                        fontSize(16.px)
                        fontWeight("600")
                        color(FlagentTheme.Primary)
                        margin(0.px)
                        property("overflow", "hidden")
                        property("text-overflow", "ellipsis")
                        property("white-space", "nowrap")
                    }
                }) {
                    Text(flag.key.ifBlank { "Flag #${flag.id}" })
                }
                Span({
                    style {
                        padding(2.px, 8.px)
                        borderRadius(6.px)
                        fontSize(11.px)
                        fontWeight("500")
                        marginTop(6.px)
                        display(DisplayStyle.InlineBlock)
                        backgroundColor(if (flag.enabled) FlagentTheme.successBg(themeMode) else FlagentTheme.inputBg(themeMode))
                        color(if (flag.enabled) FlagentTheme.Success else FlagentTheme.textLight(themeMode))
                    }
                }) {
                    Text(if (flag.enabled) LocalizedStrings.enabled else LocalizedStrings.disabled)
                }
            }
        }
        if (flag.description.isNotBlank()) {
            P({
                style {
                    fontSize(13.px)
                    color(FlagentTheme.textLight(themeMode))
                    margin(0.px)
                    marginBottom(10.px)
                    property("display", "-webkit-box")
                    property("-webkit-line-clamp", "2")
                    property("-webkit-box-orient", "vertical")
                    overflow("hidden")
                }
            }) {
                Text(flag.description)
            }
        }
        val firstSegmentDistributions = flag.segments.firstOrNull()?.distributions ?: emptyList()
        if (flag.variants.isNotEmpty()) {
            VariantDistributionChart(
                variants = flag.variants,
                distributions = firstSegmentDistributions,
                height = 6.px
            )
            Div({ style { marginTop(8.px) } }) {}
            VariantComparisonTable(
                variants = flag.variants,
                distributions = firstSegmentDistributions
            )
            Div({
                style { marginTop(8.px) }
            }) {}
        }
        Div({
            style {
                display(DisplayStyle.Flex)
                flexWrap(FlexWrap.Wrap)
                gap(6.px)
                marginBottom(12.px)
            }
        }) {
            flag.variants.forEach { v ->
                Span({
                    style {
                        padding(4.px, 8.px)
                        borderRadius(6.px)
                        fontSize(12.px)
                        backgroundColor(FlagentTheme.inputBg(themeMode))
                        color(FlagentTheme.text(themeMode))
                        border(1.px, LineStyle.Solid, FlagentTheme.cardBorder(themeMode))
                    }
                }) {
                    Text(v.key)
                }
            }
        }
        Div({
            style {
                display(DisplayStyle.Flex)
                gap(8.px)
                flexWrap(FlexWrap.Wrap)
            }
        }) {
            if (AppConfig.Features.enableMetrics) {
                Button({
                    style {
                        padding(6.px, 10.px)
                        backgroundColor(FlagentTheme.Primary)
                        color(Color.white)
                        border(0.px)
                        borderRadius(6.px)
                        cursor("pointer")
                        fontSize(12.px)
                    }
                    onClick { Router.navigateTo(Route.FlagMetrics(flag.id)) }
                }) {
                    Icon("bar_chart", size = 14.px, color = FlagentTheme.Background)
                    Text(" ${LocalizedStrings.viewMetrics}")
                }
            }
            Button({
                style {
                    padding(6.px, 10.px)
                    backgroundColor(FlagentTheme.inputBg(themeMode))
                    color(FlagentTheme.text(themeMode))
                    border(1.px, LineStyle.Solid, FlagentTheme.cardBorder(themeMode))
                    borderRadius(6.px)
                    cursor("pointer")
                    fontSize(12.px)
                }
                onClick { Router.navigateTo(Route.FlagDetail(flag.id)) }
            }) {
                Icon("settings", size = 14.px)
                Text(" ${LocalizedStrings.edit}")
            }
        }
    }
}
