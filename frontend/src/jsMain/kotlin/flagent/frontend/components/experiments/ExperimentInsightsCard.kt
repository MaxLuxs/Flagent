package flagent.frontend.components.experiments

import androidx.compose.runtime.*
import flagent.frontend.api.ExperimentInsightsResponse
import flagent.frontend.state.LocalThemeMode
import flagent.frontend.theme.FlagentTheme
import flagent.frontend.util.borderBottom
import flagent.frontend.util.borderCollapse
import flagent.frontend.util.format
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

/**
 * A/B experiment insights: conversion by variant, confidence intervals, significance.
 */
@Composable
fun ExperimentInsightsCard(insights: ExperimentInsightsResponse?) {
    if (insights == null) return
    val themeMode = LocalThemeMode.current
    Div({
        style {
            backgroundColor(FlagentTheme.cardBg(themeMode))
            borderRadius(8.px)
            padding(20.px)
            marginBottom(20.px)
            property("box-shadow", "0 2px 8px rgba(0,0,0,0.1)")
        }
    }) {
        H3({
            style {
                fontSize(18.px)
                fontWeight("600")
                color(FlagentTheme.text(themeMode))
                margin(0.px)
                marginBottom(16.px)
            }
        }) {
            Text("A/B Statistics")
        }
        if (insights.variantStats.isEmpty()) {
            P({
                style {
                    color(FlagentTheme.textLight(themeMode))
                    fontSize(14.px)
                    margin(0.px)
                }
            }) {
                Text("No conversion data. Send CONVERSION_RATE metrics with variantId to see A/B stats.")
            }
        } else {
            // Variant stats table
            Div({
                style {
                    marginBottom(16.px)
                    overflowX("auto")
                }
            }) {
                Table({
                    style {
                        width(100.percent)
                        borderCollapse("collapse")
                        fontSize(14.px)
                    }
                }) {
                    Thead {
                        Tr {
                            Th({
                                style {
                                    textAlign("left")
                                    padding(8.px)
                                    borderBottom(1.px, LineStyle.Solid, FlagentTheme.cardBorder(themeMode))
                                    color(FlagentTheme.textLight(themeMode))
                                    fontWeight("600")
                                }
                            }) { Text("Variant") }
                            Th({
                                style {
                                    textAlign("right")
                                    padding(8.px)
                                    borderBottom(1.px, LineStyle.Solid, FlagentTheme.cardBorder(themeMode))
                                    color(FlagentTheme.textLight(themeMode))
                                    fontWeight("600")
                                }
                            }) { Text("Sample") }
                            Th({
                                style {
                                    textAlign("right")
                                    padding(8.px)
                                    borderBottom(1.px, LineStyle.Solid, FlagentTheme.cardBorder(themeMode))
                                    color(FlagentTheme.textLight(themeMode))
                                    fontWeight("600")
                                }
                            }) { Text("Conversions") }
                            Th({
                                style {
                                    textAlign("right")
                                    padding(8.px)
                                    borderBottom(1.px, LineStyle.Solid, FlagentTheme.cardBorder(themeMode))
                                    color(FlagentTheme.textLight(themeMode))
                                    fontWeight("600")
                                }
                            }) { Text("Rate") }
                            Th({
                                style {
                                    textAlign("left")
                                    padding(8.px)
                                    borderBottom(1.px, LineStyle.Solid, FlagentTheme.cardBorder(themeMode))
                                    color(FlagentTheme.textLight(themeMode))
                                    fontWeight("600")
                                }
                            }) { Text("95% CI") }
                        }
                    }
                    Tbody {
                        for (stat in insights.variantStats) {
                            Tr({
                                style {
                                    borderBottom(1.px, LineStyle.Solid, Color("#f1f5f9"))
                                }
                            }) {
                                Td({
                                    style {
                                        padding(8.px)
                                        borderBottom(1.px, LineStyle.Solid, Color("#f1f5f9"))
                                        color(FlagentTheme.text(themeMode))
                                    }
                                }) {
                                    Span({
                                        style {
                                            if (stat.variantId == insights.winnerVariantId) {
                                                fontWeight("600")
                                                color(FlagentTheme.Primary)
                                            }
                                        }
                                    }) {
                                        Text(stat.variantKey ?: "variant ${stat.variantId}")
                                        if (stat.variantId == insights.winnerVariantId) {
                                            Span({
                                                style {
                                                    marginLeft(6.px)
                                                    fontSize(12.px)
                                                    color(FlagentTheme.Primary)
                                                }
                                            }) { Text("★ winner") }
                                        }
                                    }
                                }
                                Td({
                                    style {
                                        padding(8.px)
                                        borderBottom(1.px, LineStyle.Solid, Color("#f1f5f9"))
                                        textAlign("right")
                                        color(FlagentTheme.text(themeMode))
                                    }
                                }) { Text(stat.sampleSize.toString()) }
                                Td({
                                    style {
                                        padding(8.px)
                                        borderBottom(1.px, LineStyle.Solid, Color("#f1f5f9"))
                                        textAlign("right")
                                        color(FlagentTheme.text(themeMode))
                                    }
                                }) { Text(stat.conversions.toString()) }
                                Td({
                                    style {
                                        padding(8.px)
                                        borderBottom(1.px, LineStyle.Solid, Color("#f1f5f9"))
                                        textAlign("right")
                                        color(FlagentTheme.text(themeMode))
                                    }
                                }) { Text("${(stat.conversionRate * 100).format(2)}%") }
                                Td({
                                    style {
                                        padding(8.px)
                                        borderBottom(1.px, LineStyle.Solid, Color("#f1f5f9"))
                                        color(FlagentTheme.textLight(themeMode))
                                        fontSize(13.px)
                                    }
                                }) {
                                    Text("[${(stat.confidenceIntervalLow * 100).format(1)}% – ${(stat.confidenceIntervalHigh * 100).format(1)}%]")
                                }
                            }
                        }
                    }
                }
            }
            // Significance & recommendation
            Div({
                style {
                    display(DisplayStyle.Flex)
                    gap(16.px)
                    flexWrap(FlexWrap.Wrap)
                    alignItems(AlignItems.Center)
                }
            }) {
                insights.pValue?.let { pv ->
                    Span({
                        style {
                            padding(6.px, 12.px)
                            borderRadius(6.px)
                            backgroundColor(if (insights.isSignificant) FlagentTheme.successBg(themeMode) else FlagentTheme.warningBg(themeMode))
                            color(if (insights.isSignificant) FlagentTheme.Success else FlagentTheme.Warning)
                            fontSize(13.px)
                            fontWeight("500")
                        }
                    }) {
                        Text(if (insights.isSignificant) "Significant (p=${pv.format(4)})" else "Not significant (p=${pv.format(4)})")
                    }
                }
                Span({
                    style {
                        color(FlagentTheme.text(themeMode))
                        fontSize(14.px)
                    }
                }) {
                    Text(insights.recommendation)
                }
            }
        }
    }
}
