package flagent.frontend.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import flagent.frontend.theme.FlagentTheme
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.jetbrains.compose.web.dom.H4

/**
 * InfoTooltip component - shows information icon with tooltip on hover/click
 */
@Composable
fun InfoTooltip(
    title: String,
    description: String,
    details: String? = null
) {
    val showTooltip = remember { mutableStateOf(false) }
    
    Span({
        style {
            position(Position.Relative)
            display(DisplayStyle.InlineBlock)
            marginLeft(6.px)
            cursor("pointer")
        }
        onClick {
            showTooltip.value = !showTooltip.value
        }
    }) {
        Span({
            style {
                display(DisplayStyle.Flex)
                alignItems(AlignItems.Center)
                justifyContent(JustifyContent.Center)
                width(18.px)
                height(18.px)
                borderRadius(50.percent)
                backgroundColor(FlagentTheme.Info)
                color(FlagentTheme.Background)
                fontSize(12.px)
                fontWeight("bold")
                property("transition", "all 0.2s")
                property("user-select", "none")
                property("display", "inline-flex")
            }
            onMouseEnter {
                val element = it.target as org.w3c.dom.HTMLElement
                element.style.transform = "scale(1.1)"
                element.style.backgroundColor = FlagentTheme.Primary.toString()
            }
            onMouseLeave {
                val element = it.target as org.w3c.dom.HTMLElement
                element.style.transform = "scale(1)"
                element.style.backgroundColor = FlagentTheme.Info.toString()
            }
        }) {
            Text("!")
        }
        
        if (showTooltip.value) {
            Div({
                style {
                    position(Position.Absolute)
                    bottom(100.percent)
                    left(50.percent)
                    property("transform", "translateX(-50%)")
                    marginBottom(8.px)
                    width(320.px)
                    maxWidth(90.vw)
                    padding(16.px)
                    backgroundColor(FlagentTheme.Background)
                    border {
                        width(1.px)
                        style(LineStyle.Solid)
                        color(FlagentTheme.Border)
                    }
                    borderRadius(8.px)
                    property("box-shadow", "0 4px 12px ${FlagentTheme.ShadowHover}")
                    property("z-index", "1000")
                    fontSize(13.px)
                    property("line-height", "1.5")
                    property("pointer-events", "auto")
                }
            }) {
                Div({
                    style {
                        display(DisplayStyle.Flex)
                        justifyContent(JustifyContent.SpaceBetween)
                        alignItems(AlignItems.FlexStart)
                        marginBottom(10.px)
                    }
                }) {
                    H4({
                        style {
                            margin(0.px)
                            color(FlagentTheme.Text)
                            fontSize(15.px)
                            fontWeight("600")
                        }
                    }) {
                        Text(title)
                    }
                    Button({
                        onClick {
                            showTooltip.value = false
                        }
                        style {
                            padding(0.px)
                            property("background-color", "transparent")
                            border {
                                width(0.px)
                                style(LineStyle.None)
                            }
                            color(FlagentTheme.TextLight)
                            cursor("pointer")
                            fontSize(18.px)
                            property("line-height", "1")
                            property("transition", "color 0.2s")
                        }
                        onMouseEnter {
                            (it.target as org.w3c.dom.HTMLElement).style.color = FlagentTheme.Text.toString()
                        }
                        onMouseLeave {
                            (it.target as org.w3c.dom.HTMLElement).style.color = FlagentTheme.TextLight.toString()
                        }
                    }) {
                        Text("×")
                    }
                }
                
                Div({
                    style {
                        color(FlagentTheme.Text)
                        marginBottom(if (details != null) 10.px else 0.px)
                    }
                }) {
                    Text(description)
                }
                
                if (details != null) {
                    Div({
                        style {
                            marginTop(10.px)
                            paddingTop(10.px)
                            property("border-top", "1px solid ${FlagentTheme.Border}")
                            color(FlagentTheme.TextLight)
                            fontSize(12.px)
                        }
                    }) {
                        Text(details)
                    }
                }
                
                // Arrow pointing down
                Div({
                    style {
                        position(Position.Absolute)
                        bottom((-6).px)
                        left(50.percent)
                        property("transform", "translateX(-50%)")
                        width(0.px)
                        height(0.px)
                        property("border-left", "6px solid transparent")
                        property("border-right", "6px solid transparent")
                        property("border-top", "6px solid ${FlagentTheme.Border}")
                    }
                }) {}
            }
        }
    }
}
