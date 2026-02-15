package flagent.frontend.components.common

import androidx.compose.runtime.Composable
import flagent.frontend.components.Modal
import flagent.frontend.state.LocalThemeMode
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
    errorMessage: String? = null,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    if (!isOpen) return
    
    val themeMode = LocalThemeMode.current
    Modal(title = title, onClose = onCancel) {
        Div({
            style {
                padding(0.px)
                maxWidth(480.px)
                width(100.percent)
            }
        }) {
            // Title
            H3({
                style {
                    fontSize(18.px)
                    fontWeight(600)
                    color(FlagentTheme.text(themeMode))
                    margin(0.px)
                    marginBottom(16.px)
                }
            }) {
                Text(title)
            }

            // Optional error (e.g. delete failed)
            errorMessage?.let { err ->
                Div({
                    style {
                        padding(12.px)
                        marginBottom(16.px)
                        backgroundColor(FlagentTheme.errorBg(themeMode))
                        borderRadius(6.px)
                        color(FlagentTheme.errorText(themeMode))
                        fontSize(14.px)
                    }
                }) {
                    Text(err)
                }
            }
            
            // Message
            P({
                style {
                    fontSize(14.px)
                    color(FlagentTheme.textLight(themeMode))
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
                        backgroundColor(FlagentTheme.inputBg(themeMode))
                        color(FlagentTheme.text(themeMode))
                        border(1.px, LineStyle.Solid, FlagentTheme.inputBorder(themeMode))
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
