package flagent.frontend.components.common

import androidx.compose.runtime.Composable
import flagent.frontend.util.borderCollapse
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

@Composable
fun SkeletonLoader(
    width: CSSNumeric = 100.percent,
    height: CSSNumeric = 20.px,
    borderRadius: CSSNumeric = 4.px
) {
    Div({
        classes("skeleton-loader")
        style {
            width(width)
            height(height)
            borderRadius(borderRadius)
            backgroundColor(Color("#E2E8F0"))
            property("animation", "shimmer 2s infinite linear")
            property("background", "linear-gradient(90deg, #E2E8F0 25%, #F1F5F9 50%, #E2E8F0 75%)")
            property("background-size", "200% 100%")
        }
    })
}

@Composable
fun TableSkeletonLoader(rows: Int = 5, columns: Int = 4) {
    Table({
        style {
            width(100.percent)
            borderCollapse("collapse")
        }
    }) {
        Tbody {
            repeat(rows) {
                Tr {
                    repeat(columns) {
                        Td({
                            style {
                                padding(12.px)
                            }
                        }) {
                            SkeletonLoader(height = 16.px)
                        }
                    }
                }
            }
        }
    }
}
