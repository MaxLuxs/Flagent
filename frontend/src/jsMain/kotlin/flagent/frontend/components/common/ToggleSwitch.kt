package flagent.frontend.components.common

import androidx.compose.runtime.Composable
import flagent.frontend.state.LocalThemeMode
import flagent.frontend.theme.FlagentTheme
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div

enum class ToggleSize {
    Small,
    Medium
}

/**
 * iOS/Android-style toggle: pill track (gray when off, green when on) + sliding white knob with animation.
 */
@Composable
fun ToggleSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
    size: ToggleSize = ToggleSize.Medium,
) {
    val themeMode = LocalThemeMode.current

    val trackWidth = when (size) {
        ToggleSize.Small -> 40.px
        ToggleSize.Medium -> 48.px
    }
    val trackHeight = when (size) {
        ToggleSize.Small -> 24.px
        ToggleSize.Medium -> 28.px
    }
    val knobSize = when (size) {
        ToggleSize.Small -> 18.px
        ToggleSize.Medium -> 22.px
    }
    val paddingPx = 3
    val translateX = when (size) {
        ToggleSize.Small -> 40 - 18 - paddingPx * 2  // trackWidth - knobSize - padding*2
        ToggleSize.Medium -> 48 - 22 - paddingPx * 2
    }

    Div({
        attr("role", "switch")
        attr("aria-checked", checked.toString())
        if (!enabled) {
            attr("aria-disabled", "true")
        }
        style {
            display(DisplayStyle.Flex)
            alignItems(AlignItems.Center)
            width(trackWidth)
            height(trackHeight)
            padding(paddingPx.px)
            borderRadius(999.px)
            backgroundColor(
                if (checked) FlagentTheme.Success
                else FlagentTheme.NeutralLighter
            )
            border(
                1.px,
                LineStyle.Solid,
                if (checked) FlagentTheme.Success else FlagentTheme.inputBorder(themeMode)
            )
            cursor(if (enabled) "pointer" else "not-allowed")
            property("transition", "background-color 0.22s ease, border-color 0.22s ease")
            property("overflow", "hidden")
        }
        if (enabled) {
            onClick {
                onCheckedChange(!checked)
            }
        }
    }) {
        Div({
            style {
                width(knobSize)
                height(knobSize)
                borderRadius(999.px)
                backgroundColor(Color.white)
                property(
                    "transform",
                    if (checked) "translateX(${translateX}px)" else "translateX(0px)"
                )
                property("transition", "transform 0.22s cubic-bezier(0.4, 0, 0.2, 1)")
                property("box-shadow", FlagentTheme.ShadowCard)
                property("flex-shrink", "0")
            }
        })
    }
}
