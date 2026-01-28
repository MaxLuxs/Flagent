package flagent.frontend.components.common

import androidx.compose.runtime.Composable
import flagent.frontend.components.Icon
import flagent.frontend.theme.FlagentTheme
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

@Composable
fun EmptyState(
    icon: String = "inbox",
    title: String,
    description: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Div({
        style {
            display(DisplayStyle.Flex)
            flexDirection(FlexDirection.Column)
            alignItems(AlignItems.Center)
            justifyContent(JustifyContent.Center)
            padding(48.px, 24.px)
            textAlign("center")
        }
    }) {
        // Icon (with margin via wrapper)
        Div({
            style {
                marginBottom(16.px)
            }
        }) {
            Icon(icon, size = 64.px, color = Color("#CBD5E1"))
        }
        
        // Title
        H3({
            style {
                fontSize(20.px)
                fontWeight(600)
                color(Color("#1E293B"))
                margin(0.px)
                marginBottom(8.px)
            }
        }) {
            Text(title)
        }
        
        // Description
        description?.let {
            P({
                style {
                    fontSize(14.px)
                    color(Color("#64748B"))
                    margin(0.px)
                    marginBottom(24.px)
                    maxWidth(400.px)
                }
            }) {
                Text(it)
            }
        }
        
        // Action button
        if (actionLabel != null && onAction != null) {
            Button({
                onClick { onAction() }
                style {
                    padding(10.px, 20.px)
                    backgroundColor(FlagentTheme.Primary)
                    color(Color.white)
                    border(0.px)
                    borderRadius(6.px)
                    cursor("pointer")
                    fontSize(14.px)
                    fontWeight(500)
                }
            }) {
                Text(actionLabel)
            }
        }
    }
}
