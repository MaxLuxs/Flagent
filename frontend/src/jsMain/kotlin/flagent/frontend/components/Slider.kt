package flagent.frontend.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import flagent.frontend.theme.FlagentTheme
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

/**
 * Slider component - enhanced range input slider with visual feedback
 * 
 * Provides a styled range slider with value display and smooth animations
 */
@Composable
fun Slider(
    value: Int,
    onValueChange: (Int) -> Unit,
    min: Int = 0,
    max: Int = 100,
    step: Int = 1,
    label: String? = null,
    showValue: Boolean = true,
    disabled: Boolean = false
) {
    Div({
        style {
            display(DisplayStyle.Flex)
            flexDirection(FlexDirection.Column)
            gap(8.px)
            width(100.percent)
        }
    }) {
        // Label and value display
        if (label != null || showValue) {
            Div({
                style {
                    display(DisplayStyle.Flex)
                    justifyContent(JustifyContent.SpaceBetween)
                    alignItems(AlignItems.Center)
                }
            }) {
                if (label != null) {
                    Div({
                        style {
                            fontSize(14.px)
                            fontWeight("500")
                            color(if (disabled) FlagentTheme.WorkspaceTextLight else FlagentTheme.WorkspaceText)
                            margin(0.px)
                            cursor(if (disabled) "not-allowed" else "pointer")
                        }
                    }) {
                        Label {
                            Text(label)
                        }
                    }
                }
                if (showValue) {
                    Span({
                        style {
                            fontSize(14.px)
                            fontWeight("600")
                            color(if (disabled) FlagentTheme.TextLighter else FlagentTheme.Primary)
                            padding(4.px, 8.px)
                            backgroundColor(if (disabled) FlagentTheme.BackgroundDark else Color("rgba(14, 165, 233, 0.1)"))
                            borderRadius(4.px)
                            minWidth(50.px)
                            textAlign("center")
                            property("display", "inline-block")
                        }
                    }) {
                        Text("$value%")
                    }
                }
            }
        }
        
        // Slider input
        val sliderId = remember { "slider-${kotlin.random.Random.nextInt()}" }
        
        Div({
            style {
                position(Position.Relative)
                width(100.percent)
            }
        }) {
            Input(InputType.Range) {
                id(sliderId)
                value(value.toString())
                onInput { event ->
                    val inputValue = (event.target as org.w3c.dom.HTMLInputElement).value.toIntOrNull() ?: min
                    onValueChange(inputValue.coerceIn(min, max))
                }
                attr("min", min.toString())
                attr("max", max.toString())
                attr("step", step.toString())
                if (disabled) {
                    attr("disabled", "true")
                }
                style {
                    width(100.percent)
                    height(8.px)
                    borderRadius(4.px)
                    backgroundColor(if (disabled) FlagentTheme.WorkspaceInputBg else FlagentTheme.WorkspaceInputBorder)
                    property("appearance", "none")
                    cursor(if (disabled) "not-allowed" else "pointer")
                    property("outline", "none")
                    property("transition", "background 0.2s ease")
                    property("-webkit-appearance", "none")
                    property("-moz-appearance", "none")
                }
            }
            
            // Apply custom thumb styles via CSS
            LaunchedEffect(sliderId, value, disabled) {
                val styleId = "slider-thumb-style-$sliderId"
                var styleElement = kotlinx.browser.document.getElementById(styleId) as? org.w3c.dom.HTMLStyleElement
                if (styleElement == null) {
                    styleElement = kotlinx.browser.document.createElement("style") as org.w3c.dom.HTMLStyleElement
                    styleElement.id = styleId
                    kotlinx.browser.document.head?.appendChild(styleElement)
                }
                
                val thumbColor = if (disabled) FlagentTheme.WorkspaceInputBg.toString() else FlagentTheme.Primary.toString()
                styleElement.textContent = """
                    #$sliderId::-webkit-slider-thumb {
                        -webkit-appearance: none;
                        appearance: none;
                        width: 20px;
                        height: 20px;
                        border-radius: 50%;
                        background: $thumbColor;
                        border: 2px solid rgba(255,255,255,0.3);
                        box-shadow: 0 2px 4px ${FlagentTheme.Shadow};
                        cursor: ${if (disabled) "not-allowed" else "pointer"};
                    }
                    #$sliderId::-moz-range-thumb {
                        width: 20px;
                        height: 20px;
                        border-radius: 50%;
                        background: $thumbColor;
                        border: 2px solid rgba(255,255,255,0.3);
                        box-shadow: 0 2px 4px ${FlagentTheme.Shadow};
                        cursor: ${if (disabled) "not-allowed" else "pointer"};
                    }
                """.trimIndent()
            }
            
            // Custom progress bar overlay (visual feedback)
            Div({
                style {
                    position(Position.Absolute)
                    top(0.px)
                    left(0.px)
                    height(8.px)
                    width(((value - min).toFloat() / (max - min) * 100).percent)
                    backgroundColor(if (disabled) FlagentTheme.NeutralLighter else FlagentTheme.Primary)
                    borderRadius(4.px)
                    property("pointer-events", "none")
                    property("transition", "width 0.2s ease")
                    property("z-index", "1")
                }
            }) {}
        }
        
        // Min/Max labels (optional)
        Div({
            style {
                display(DisplayStyle.Flex)
                justifyContent(JustifyContent.SpaceBetween)
                fontSize(11.px)
                color(FlagentTheme.WorkspaceTextLight)
                marginTop(-4.px)
            }
        }) {
            Span { Text("$min%") }
            Span { Text("$max%") }
        }
    }
}
