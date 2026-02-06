package flagent.frontend.components.common

import androidx.compose.runtime.Composable
import flagent.frontend.state.LocalThemeMode
import flagent.frontend.theme.FlagentTheme
import flagent.frontend.util.borderBottom
import flagent.frontend.util.borderCollapse
import flagent.frontend.util.textTransform
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

/**
 * Responsive table wrapper component
 * On mobile, converts table to card layout
 */
@Composable
fun ResponsiveTable(
    headers: List<String>,
    rows: List<List<String>>,
    onRowClick: ((Int) -> Unit)? = null
) {
    val themeMode = LocalThemeMode.current
    // Desktop table
    Div({
        classes("responsive-table-desktop")
        style {
            display(DisplayStyle.Block)
            property("@media (max-width: 768px)", "{ display: none; }")
        }
    }) {
        Table({
            style {
                width(100.percent)
                borderCollapse("collapse")
                backgroundColor(FlagentTheme.cardBg(themeMode))
                borderRadius(8.px)
                overflow("hidden")
                property("border", "1px solid ${FlagentTheme.cardBorder(themeMode)}")
                property("backdrop-filter", "blur(12px)")
            }
        }) {
            Thead {
                Tr({
                    style {
                        backgroundColor(FlagentTheme.inputBg(themeMode))
                        borderBottom(2.px, LineStyle.Solid, FlagentTheme.cardBorder(themeMode))
                    }
                }) {
                    headers.forEach { header ->
                        Th({
                            style {
                                padding(12.px)
                                textAlign("left")
                                fontSize(12.px)
                                fontWeight(600)
                                color(FlagentTheme.textLight(themeMode))
                                textTransform("uppercase")
                            }
                        }) {
                            Text(header)
                        }
                    }
                }
            }
            
            Tbody {
                rows.forEachIndexed { index, row ->
                    Tr({
                        if (onRowClick != null) {
                            onClick { onRowClick(index) }
                            style {
                                cursor("pointer")
                            }
                        }
                        style {
                            borderBottom(1.px, LineStyle.Solid, FlagentTheme.cardBorder(themeMode))
                            property("transition", "background-color 0.2s")
                        }
                        // Hover effects handled via CSS
                    }) {
                        row.forEach { cell ->
                            Td({
                                style {
                                    padding(12.px)
                                    fontSize(14.px)
                                    color(FlagentTheme.text(themeMode))
                                }
                            }) {
                                Text(cell)
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Mobile cards
    Div({
        classes("responsive-table-mobile")
        style {
            display(DisplayStyle.None)
            property("@media (max-width: 768px)", "{ display: block; }")
        }
    }) {
        rows.forEachIndexed { index, row ->
            Div({
                if (onRowClick != null) {
                    onClick { onRowClick(index) }
                }
                style {
                    backgroundColor(FlagentTheme.cardBg(themeMode))
                    borderRadius(8.px)
                    padding(16.px)
                    marginBottom(12.px)
                    property("border", "1px solid ${FlagentTheme.cardBorder(themeMode)}")
                    if (onRowClick != null) {
                        cursor("pointer")
                    }
                }
            }) {
                row.forEachIndexed { cellIndex, cell ->
                    if (cellIndex < headers.size) {
                        Div({
                            style {
                                display(DisplayStyle.Flex)
                                justifyContent(JustifyContent.SpaceBetween)
                                marginBottom(8.px)
                            }
                        }) {
                            Span({
                                style {
                                    fontSize(12.px)
                                    fontWeight(600)
                                    color(FlagentTheme.textLight(themeMode))
                                    textTransform("uppercase")
                                }
                            }) {
                                Text(headers[cellIndex])
                            }
                            Span({
                                style {
                                    fontSize(14.px)
                                    color(FlagentTheme.text(themeMode))
                                }
                            }) {
                                Text(cell)
                            }
                        }
                    }
                }
            }
        }
    }
}
