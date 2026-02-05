package flagent.frontend.components.common

import androidx.compose.runtime.Composable
import flagent.frontend.theme.FlagentTheme
import flagent.frontend.util.borderBottom
import flagent.frontend.util.borderCollapse
import flagent.frontend.util.textTransform
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

/**
 * Compact data table with hover states. Use with Pagination for paged lists.
 */
@Composable
fun DataTable(
    headers: List<String>,
    rows: List<List<String>>,
    onRowClick: ((Int) -> Unit)? = null,
    compact: Boolean = true
) {
    val rowPadding = if (compact) 10.px else 12.px
    Div({
        style {
            backgroundColor(FlagentTheme.WorkspaceCardBg)
            borderRadius(8.px)
            overflow("hidden")
            property("border", "1px solid ${FlagentTheme.WorkspaceCardBorder}")
            property("backdrop-filter", "blur(12px)")
        }
    }) {
        Table({
            style {
                width(100.percent)
                borderCollapse("collapse")
            }
        }) {
            Thead {
                Tr({
                    style {
                        backgroundColor(FlagentTheme.WorkspaceInputBg)
                        borderBottom(1.px, LineStyle.Solid, FlagentTheme.WorkspaceBorder)
                    }
                }) {
                    headers.forEach { header ->
                        Th({
                            style {
                                padding(rowPadding)
                                textAlign("left")
                                fontSize(12.px)
                                fontWeight(600)
                                color(FlagentTheme.WorkspaceTextLight)
                                textTransform("uppercase")
                                property("letter-spacing", "0.03em")
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
                        if (onRowClick != null) onClick { onRowClick(index) }
                        style {
                            borderBottom(1.px, LineStyle.Solid, FlagentTheme.WorkspaceBorder)
                            property("transition", "background-color 0.15s")
                            if (onRowClick != null) cursor("pointer")
                        }
                        onMouseEnter {
                            (it.target as org.w3c.dom.HTMLElement).style.backgroundColor = FlagentTheme.WorkspaceInputBg.toString()
                        }
                        onMouseLeave {
                            (it.target as org.w3c.dom.HTMLElement).style.backgroundColor = "transparent"
                        }
                    }) {
                        row.forEach { cell ->
                            Td({
                                style {
                                    padding(rowPadding)
                                    fontSize(14.px)
                                    color(FlagentTheme.WorkspaceText)
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
