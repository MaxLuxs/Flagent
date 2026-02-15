package flagent.frontend.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import flagent.frontend.state.LocalThemeMode
import flagent.frontend.state.ThemeMode
import flagent.frontend.theme.FlagentTheme
import kotlinx.browser.document
import kotlinx.browser.window
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.jetbrains.compose.web.dom.H4

/**
 * Preferred position for tooltip relative to the trigger.
 * Used to avoid tooltip going under navbar or off-screen.
 */
enum class TooltipPosition {
    /** Prefer above the trigger (default for elements in lower half of viewport) */
    Top,
    /** Prefer below the trigger */
    Bottom,
    /** Prefer left of the trigger */
    Left,
    /** Prefer right of the trigger */
    Right
}

/**
 * InfoTooltip component - shows information icon with tooltip on hover/click.
 * Uses z-index 10000 so it appears above Navbar (z-index 100) and modals.
 * Uses fixed positioning with viewport margin so it never goes under navigation.
 */
private const val TOOLTIP_WIDTH_PX = 320
private const val TOOLTIP_OFFSET_PX = 8

@Composable
fun InfoTooltip(
    title: String,
    description: String,
    details: String? = null,
    preferredPosition: TooltipPosition = TooltipPosition.Top
) {
    val themeMode = LocalThemeMode.current
    val showTooltip = remember { mutableStateOf(false) }
    val triggerId = remember { "info-tooltip-${kotlin.random.Random.nextLong().toString(36)}" }
    val tooltipPosition = remember { mutableStateOf<Pair<Int, Int>?>(null) }

    LaunchedEffect(showTooltip.value) {
        if (showTooltip.value) {
            window.setTimeout({
                val el = document.getElementById(triggerId) ?: return@setTimeout
                val rect = (el as org.w3c.dom.HTMLElement).getBoundingClientRect()
                val vw = window.innerWidth
                val vh = window.innerHeight
                val tw = TOOLTIP_WIDTH_PX
                val maxH = (vh * 0.8).toInt()
                val (left, top) = when (preferredPosition) {
                    TooltipPosition.Top -> {
                        val l = (rect.left + rect.width / 2 - tw / 2).toInt().coerceIn(TOOLTIP_OFFSET_PX, vw - tw - TOOLTIP_OFFSET_PX)
                        val t = (rect.top - maxH - TOOLTIP_OFFSET_PX).toInt()
                        if (t < TOOLTIP_OFFSET_PX) {
                            val tBottom = (rect.bottom + TOOLTIP_OFFSET_PX).toInt()
                            Pair(l, tBottom)
                        } else Pair(l, t)
                    }
                    TooltipPosition.Bottom -> {
                        val l = (rect.left + rect.width / 2 - tw / 2).toInt().coerceIn(TOOLTIP_OFFSET_PX, vw - tw - TOOLTIP_OFFSET_PX)
                        val t = (rect.bottom + TOOLTIP_OFFSET_PX).toInt()
                        if (t + maxH > vh - TOOLTIP_OFFSET_PX) {
                            val tTop = (rect.top - maxH - TOOLTIP_OFFSET_PX).toInt().coerceAtLeast(TOOLTIP_OFFSET_PX)
                            Pair(l, tTop)
                        } else Pair(l, t)
                    }
                    TooltipPosition.Left -> {
                        val l = (rect.left - tw - TOOLTIP_OFFSET_PX).toInt().coerceAtLeast(TOOLTIP_OFFSET_PX)
                        val t = (rect.top + rect.height / 2 - maxH / 2).toInt().coerceIn(TOOLTIP_OFFSET_PX, vh - maxH - TOOLTIP_OFFSET_PX)
                        Pair(l, t)
                    }
                    TooltipPosition.Right -> {
                        val l = (rect.right + TOOLTIP_OFFSET_PX).toInt()
                        if (l + tw > vw - TOOLTIP_OFFSET_PX) {
                            val lLeft = (rect.left - tw - TOOLTIP_OFFSET_PX).toInt().coerceAtLeast(TOOLTIP_OFFSET_PX)
                            Pair(lLeft, (rect.top + rect.height / 2 - maxH / 2).toInt().coerceIn(TOOLTIP_OFFSET_PX, vh - maxH - TOOLTIP_OFFSET_PX))
                        } else Pair(l, (rect.top + rect.height / 2 - maxH / 2).toInt().coerceIn(TOOLTIP_OFFSET_PX, vh - maxH - TOOLTIP_OFFSET_PX))
                    }
                }
                tooltipPosition.value = Pair(left, top)
            }, 0)
        } else {
            tooltipPosition.value = null
        }
    }

    Span({
        id(triggerId)
        style {
            position(Position.Relative)
            display(DisplayStyle.InlineBlock)
            marginLeft(6.px)
            cursor("pointer")
            if (showTooltip.value) {
                property("z-index", "99999")
            }
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
                color(Color.white)
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
            val pos = tooltipPosition.value
            Div({
                style {
                    position(Position.Fixed)
                    if (pos != null) {
                        left(pos.first.px)
                        top(pos.second.px)
                    } else {
                        left(50.percent)
                        top(50.percent)
                        property("transform", "translate(-50%, -50%)")
                    }
                    property("margin", "min(20px, 5vh) auto")
                    width(320.px)
                    maxWidth(90.vw)
                    maxHeight(80.vh)
                    property("overflow-y", "auto")
                    padding(16.px)
                    backgroundColor(FlagentTheme.cardBg(themeMode))
                    border {
                        width(1.px)
                        style(LineStyle.Solid)
                        color(FlagentTheme.cardBorder(themeMode))
                    }
                    borderRadius(8.px)
                    property("box-shadow", "0 4px 20px rgba(0,0,0,0.25)")
                    property("z-index", "99999")
                    property("isolation", "isolate")
                    property("-webkit-backdrop-filter", "blur(12px)")
                    property("backdrop-filter", "blur(12px)")
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
                            color(FlagentTheme.text(themeMode))
                            fontSize(15.px)
                            fontWeight("600")
                            if (themeMode == ThemeMode.Dark) {
                                property("text-shadow", "0 0 10px rgba(0,0,0,0.9), 0 1px 3px rgba(0,0,0,0.8)")
                            }
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
                            color(FlagentTheme.textLight(themeMode))
                            cursor("pointer")
                            fontSize(18.px)
                            property("line-height", "1")
                            property("transition", "color 0.2s")
                            if (themeMode == ThemeMode.Dark) {
                                property("text-shadow", "0 0 8px rgba(0,0,0,0.9)")
                            }
                        }
                        onMouseEnter {
                            (it.target as org.w3c.dom.HTMLElement).style.color = FlagentTheme.text(themeMode).toString()
                        }
                        onMouseLeave {
                            (it.target as org.w3c.dom.HTMLElement).style.color = FlagentTheme.textLight(themeMode).toString()
                        }
                    }) {
                        Text("×")
                    }
                }
                
                Div({
                    style {
                        color(FlagentTheme.text(themeMode))
                        marginBottom(if (details != null) 10.px else 0.px)
                        if (themeMode == ThemeMode.Dark) {
                            property("text-shadow", "0 0 10px rgba(0,0,0,0.9), 0 1px 3px rgba(0,0,0,0.8)")
                        }
                    }
                }) {
                    Text(description)
                }
                
                if (details != null) {
                    Div({
                        style {
                            marginTop(10.px)
                            paddingTop(10.px)
                            property("border-top", "1px solid ${FlagentTheme.cardBorder(themeMode)}")
                            color(FlagentTheme.textLight(themeMode))
                            fontSize(12.px)
                            if (themeMode == ThemeMode.Dark) {
                                property("text-shadow", "0 0 8px rgba(0,0,0,0.9), 0 1px 2px rgba(0,0,0,0.8)")
                            }
                        }
                    }) {
                        Text(details)
                    }
                }
            }
        }
    }
}
