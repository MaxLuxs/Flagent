package flagent.frontend.components.common

import androidx.compose.runtime.Composable
import flagent.frontend.components.Modal
import flagent.frontend.theme.FlagentTheme
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

@Composable
fun ConfirmDialog(
    isOpen: Boolean,
    title: String,
    message: String,
    confirmLabel: String = "Confirm",
    cancelLabel: String = "Cancel",
    isDangerous: Boolean = false,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    if (!isOpen) return
    
    Modal(title = title, onClose = onCancel) {
        Div({
            style {
                backgroundColor(Color.white)
                borderRadius(8.px)
                padding(24.px)
                maxWidth(480.px)
                width(100.percent)
                property("box-shadow", "0 20px 25px -5px rgba(0, 0, 0, 0.1)")
            }
        }) {
            // Title
            H3({
                style {
                    fontSize(18.px)
                    fontWeight(600)
                    color(Color("#1E293B"))
                    margin(0.px)
                    marginBottom(16.px)
                }
            }) {
                Text(title)
            }
            
            // Message
            P({
                style {
                    fontSize(14.px)
                    color(Color("#64748B"))
                    margin(0.px)
                    marginBottom(24.px)
                    lineHeight("1.5")
                }
            }) {
                Text(message)
            }
            
            // Actions
            Div({
                style {
                    display(DisplayStyle.Flex)
                    justifyContent(JustifyContent.FlexEnd)
                    gap(12.px)
                }
            }) {
                Button({
                    onClick { onCancel() }
                    style {
                        padding(10.px, 20.px)
                        backgroundColor(Color.transparent)
                        color(Color("#64748B"))
                        border(1.px, LineStyle.Solid, Color("#E2E8F0"))
                        borderRadius(6.px)
                        cursor("pointer")
                        fontSize(14.px)
                        fontWeight(500)
                    }
                }) {
                    Text(cancelLabel)
                }
                
                Button({
                    onClick { onConfirm() }
                    style {
                        padding(10.px, 20.px)
                        backgroundColor(if (isDangerous) Color("#EF4444") else FlagentTheme.Primary)
                        color(Color.white)
                        border(0.px)
                        borderRadius(6.px)
                        cursor("pointer")
                        fontSize(14.px)
                        fontWeight(500)
                    }
                }) {
                    Text(confirmLabel)
                }
            }
        }
    }
}
