package flagent.frontend.components.common

import androidx.compose.runtime.Composable
import flagent.frontend.theme.FlagentTheme
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

/**
 * Page header: title + subtitle + optional actions.
 * Compact Unleash-style header.
 */
@Composable
fun PageHeader(
    title: String,
    subtitle: String? = null,
    actions: @Composable () -> Unit = {}
) {
    Div({
        style {
            display(DisplayStyle.Flex)
            justifyContent(JustifyContent.SpaceBetween)
            alignItems(AlignItems.FlexStart)
            marginBottom(20.px)
            flexWrap(FlexWrap.Wrap)
            gap(12.px)
        }
    }) {
        Div({
            style {
                flex(1)
                property("min-width", "0")
            }
        }) {
            H1({
                style {
                    fontSize(24.px)
                    fontWeight("bold")
                    color(FlagentTheme.WorkspaceText)
                    margin(0.px)
                }
            }) {
                Text(title)
            }
            if (subtitle != null) {
                P({
                    style {
                        color(FlagentTheme.WorkspaceTextLight)
                        fontSize(14.px)
                        marginTop(4.px)
                        margin(0.px)
                    }
                }) {
                    Text(subtitle)
                }
            }
        }
        Div({
            style {
                display(DisplayStyle.Flex)
                alignItems(AlignItems.Center)
                gap(8.px)
            }
        }) {
            actions()
        }
    }
}
