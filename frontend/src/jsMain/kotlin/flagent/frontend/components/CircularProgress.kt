package flagent.frontend.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import flagent.frontend.theme.FlagentTheme
import kotlin.math.PI
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

/**
 * CircularProgress component - displays percentage as circular progress bar
 * 
 * Usage:
 * ```
 * CircularProgress(
 *     percentage = 75,
 *     size = 80.px,
 *     strokeWidth = 8.px,
 *     color = FlagentTheme.Primary
 * )
 * ```
 */
@Composable
fun CircularProgress(
    percentage: Int,
    size: CSSSizeValue<CSSUnit.px> = 80.px,
    strokeWidth: CSSSizeValue<CSSUnit.px> = 8.px,
    color: CSSColorValue = FlagentTheme.Primary,
    backgroundColor: CSSColorValue = FlagentTheme.BackgroundDark,
    showLabel: Boolean = true,
    labelSize: CSSSizeValue<CSSUnit.px> = 16.px,
    labelColor: CSSColorValue = FlagentTheme.Text
) {
    val clampedPercentage = percentage.coerceIn(0, 100)
    val radius = (size.value.toDouble() / 2 - strokeWidth.value.toDouble() / 2)
    val circumference = 2 * PI * radius
    val offset = circumference - (clampedPercentage / 100.0) * circumference
    
    Div({
        style {
            position(Position.Relative)
            display(DisplayStyle.Flex)
            alignItems(AlignItems.Center)
            justifyContent(JustifyContent.Center)
            width(size)
            height(size)
        }
    }) {
        // SVG for circular progress - using innerHTML approach for simplicity
        val svgId = remember { "circular-progress-${kotlin.random.Random.nextInt()}" }
        Div({
            id(svgId)
            style {
                position(Position.Absolute)
                top(0.px)
                left(0.px)
                width(size)
                height(size)
            }
        }) {}
        
        LaunchedEffect(svgId, size, radius, strokeWidth, backgroundColor, color, circumference, offset) {
            val element = kotlinx.browser.document.getElementById(svgId)
            element?.innerHTML = """
                <svg width="${size.value}" height="${size.value}" style="transform: rotate(270deg); position: absolute; top: 0; left: 0;">
                    <circle cx="${size.value / 2}" cy="${size.value / 2}" r="$radius" 
                            fill="none" 
                            stroke="${backgroundColor}" 
                            stroke-width="${strokeWidth.value}"/>
                    <circle cx="${size.value / 2}" cy="${size.value / 2}" r="$radius" 
                            fill="none" 
                            stroke="${color}" 
                            stroke-width="${strokeWidth.value}"
                            stroke-linecap="round"
                            stroke-dasharray="$circumference"
                            stroke-dashoffset="$offset"
                            style="transition: stroke-dashoffset 0.3s ease-in-out;"/>
                </svg>
            """.trimIndent()
        }
        
        // Label (percentage text)
        if (showLabel) {
            Span({
                style {
                    position(Position.Relative)
                    fontSize(labelSize)
                    fontWeight("bold")
                    color(labelColor)
                    property("z-index", "1")
                }
            }) {
                Text("$clampedPercentage%")
            }
        }
    }
}
