package flagent.frontend.components.common

import androidx.compose.runtime.Composable
import flagent.frontend.i18n.LocalizedStrings
import flagent.frontend.state.LocalThemeMode
import flagent.frontend.theme.FlagentTheme
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

@Composable
fun Pagination(
    currentPage: Int,
    totalPages: Int,
    onPageChange: (Int) -> Unit
) {
    val themeMode = LocalThemeMode.current
    if (totalPages <= 1) return
    
    Div({
        style {
            display(DisplayStyle.Flex)
            alignItems(AlignItems.Center)
            gap(8.px)
            justifyContent(JustifyContent.Center)
            padding(16.px, 0.px)
        }
    }) {
        // Previous button
        Button({
            if (currentPage == 1) {
                attr("disabled", "")
            }
            onClick { onPageChange(currentPage - 1) }
            style {
                padding(8.px, 16.px)
                backgroundColor(if (currentPage == 1) FlagentTheme.inputBg(themeMode) else FlagentTheme.Primary)
                color(if (currentPage == 1) FlagentTheme.textLight(themeMode) else Color.white)
                border(0.px)
                borderRadius(6.px)
                cursor(if (currentPage == 1) "not-allowed" else "pointer")
                fontWeight(500)
            }
        }) {
            Text(LocalizedStrings.previous)
        }
        
        // Page numbers
        for (page in getPageRange(currentPage, totalPages)) {
            if (page == -1) {
                Span({
                    style {
                        padding(8.px, 12.px)
                        color(FlagentTheme.textLight(themeMode))
                    }
                }) {
                    Text("...")
                }
            } else {
                Button({
                    onClick { onPageChange(page) }
                    style {
                        padding(8.px, 12.px)
                        backgroundColor(if (page == currentPage) FlagentTheme.Primary else Color.transparent)
                        color(if (page == currentPage) Color.white else FlagentTheme.textLight(themeMode))
                        border(1.px, LineStyle.Solid, FlagentTheme.inputBorder(themeMode))
                        borderRadius(6.px)
                        cursor("pointer")
                        fontWeight(if (page == currentPage) 600 else 400)
                        minWidth(40.px)
                    }
                }) {
                    Text("$page")
                }
            }
        }
        
        // Next button
        Button({
            if (currentPage == totalPages) {
                attr("disabled", "")
            }
            onClick { onPageChange(currentPage + 1) }
            style {
                padding(8.px, 16.px)
                backgroundColor(if (currentPage == totalPages) FlagentTheme.inputBg(themeMode) else FlagentTheme.Primary)
                color(if (currentPage == totalPages) FlagentTheme.textLight(themeMode) else Color.white)
                border(0.px)
                borderRadius(6.px)
                cursor(if (currentPage == totalPages) "not-allowed" else "pointer")
                fontWeight(500)
            }
        }) {
            Text(LocalizedStrings.next)
        }
    }
}

private fun getPageRange(current: Int, total: Int): List<Int> {
    val range = mutableListOf<Int>()
    
    when {
        total <= 7 -> {
            // Show all pages
            range.addAll(1..total)
        }
        current <= 4 -> {
            // Near beginning
            range.addAll(1..5)
            range.add(-1) // ellipsis
            range.add(total)
        }
        current >= total - 3 -> {
            // Near end
            range.add(1)
            range.add(-1) // ellipsis
            range.addAll(total - 4..total)
        }
        else -> {
            // Middle
            range.add(1)
            range.add(-1) // ellipsis
            range.addAll(current - 1..current + 1)
            range.add(-1) // ellipsis
            range.add(total)
        }
    }
    
    return range
}
