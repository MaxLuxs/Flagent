package flagent.frontend.components

import androidx.compose.runtime.Composable
import flagent.frontend.i18n.LocalizedStrings
import flagent.frontend.state.LocalThemeMode
import flagent.frontend.theme.FlagentTheme
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

/**
 * Modal component - reusable modal dialog
 * 
 * Usage:
 * ```
 * val showModal = remember { mutableStateOf(false) }
 * 
 * if (showModal.value) {
 *     Modal(
 *         title = "Create Segment",
 *         onClose = { showModal.value = false },
 *         onConfirm = { 
 *             // handle confirm
 *             showModal.value = false 
 *         },
 *         confirmText = "Create",
 *         cancelText = "Cancel",
 *         showCancel = true
 *     ) {
 *         // modal content
 *     }
 * }
 * ```
 */
@Composable
fun Modal(
    title: String,
    onClose: () -> Unit,
    onConfirm: (() -> Unit)? = null,
    confirmText: String = LocalizedStrings.save,
    cancelText: String = LocalizedStrings.cancel,
    showCancel: Boolean = true,
    confirmDisabled: Boolean = false,
    confirmLoading: Boolean = false,
    width: CSSSizeValue<CSSUnit.px> = 500.px,
    maxHeight: CSSSizeValue<CSSUnit.percent> = 80.percent,
    content: @Composable () -> Unit
) {
    val themeMode = LocalThemeMode.current
    Div({
        style {
            position(Position.Fixed)
            top(0.px)
            left(0.px)
            right(0.px)
            bottom(0.px)
            property("background-color", "rgba(0,0,0,0.5)")
            display(DisplayStyle.Flex)
            justifyContent(JustifyContent.Center)
            alignItems(AlignItems.Center)
            property("z-index", "1000")
        }
        onClick { event ->
            // Close modal when clicking on backdrop
            if (event.target == event.currentTarget) {
                onClose()
            }
        }
    }) {
        Div({
            style {
                backgroundColor(FlagentTheme.cardBg(themeMode))
                padding(30.px)
                borderRadius(10.px)
                maxWidth(width)
                width(90.percent)
                maxHeight(maxHeight)
                overflow("auto")
                border(1.px, LineStyle.Solid, FlagentTheme.cardBorder(themeMode))
                property("backdrop-filter", "blur(12px)")
                property("box-shadow", "0 10px 25px rgba(0, 0, 0, 0.3)")
            }
            onClick { event ->
                // Prevent closing when clicking inside modal
                event.stopPropagation()
            }
        }) {
            // Header
            Div({
                style {
                    display(DisplayStyle.Flex)
                    justifyContent(JustifyContent.SpaceBetween)
                    alignItems(AlignItems.Center)
                    marginBottom(20.px)
                    paddingBottom(15.px)
                    border {
                        width(0.px)
                        style(LineStyle.None)
                    }
                    property("border-bottom", "1px solid ${FlagentTheme.cardBorder(themeMode)}")
                }
            }) {
                H3({
                    style {
                        margin(0.px)
                        color(FlagentTheme.text(themeMode))
                    }
                }) {
                    Text(title)
                }
                Button({
                    onClick { onClose() }
                    style {
                        padding(4.px, 8.px)
                        property("background-color", "transparent")
                        color(FlagentTheme.textLight(themeMode))
                        border {
                            width(0.px)
                            style(LineStyle.None)
                        }
                        borderRadius(4.px)
                        cursor("pointer")
                        fontSize(20.px)
                        property("line-height", "1")
                        property("transition", "color 0.2s")
                    }
                    onMouseEnter {
                        val element = it.target as org.w3c.dom.HTMLElement
                        element.style.color = FlagentTheme.text(themeMode).toString()
                    }
                    onMouseLeave {
                        val element = it.target as org.w3c.dom.HTMLElement
                        element.style.color = FlagentTheme.textLight(themeMode).toString()
                    }
                }) {
                    Text("×")
                }
            }
            
            // Content
            Div({
                style {
                    marginBottom(20.px)
                }
            }) {
                content()
            }
            
            // Footer with buttons
            Div({
                style {
                    display(DisplayStyle.Flex)
                    gap(10.px)
                    justifyContent(JustifyContent.FlexEnd)
                    paddingTop(15.px)
                    border {
                        width(0.px)
                        style(LineStyle.None)
                    }
                    property("border-top", "1px solid ${FlagentTheme.cardBorder(themeMode)}")
                }
            }) {
                if (showCancel) {
                    Button({
                        onClick { onClose() }
                        style {
                            padding(8.px, 16.px)
                            backgroundColor(FlagentTheme.inputBg(themeMode))
                            color(FlagentTheme.text(themeMode))
                            border {
                                width(0.px)
                                style(LineStyle.None)
                            }
                            borderRadius(5.px)
                            cursor("pointer")
                            property("transition", "background-color 0.2s")
                        }
                        onMouseEnter {
                            val element = it.target as org.w3c.dom.HTMLElement
                            element.style.backgroundColor = FlagentTheme.inputBorder(themeMode).toString()
                        }
                        onMouseLeave {
                            val element = it.target as org.w3c.dom.HTMLElement
                            element.style.backgroundColor = FlagentTheme.inputBg(themeMode).toString()
                        }
                    }) {
                        Text(cancelText)
                    }
                }
                if (onConfirm != null) {
                    Button({
                        onClick { onConfirm() }
                        if (confirmDisabled || confirmLoading) {
                            attr("disabled", "true")
                        }
                        style {
                            padding(8.px, 16.px)
                            backgroundColor(
                                if (confirmDisabled || confirmLoading) FlagentTheme.inputBg(themeMode)
                                else FlagentTheme.Primary
                            )
                            color(if (confirmDisabled || confirmLoading) FlagentTheme.textLight(themeMode) else Color.white)
                            border {
                                width(0.px)
                                style(LineStyle.None)
                            }
                            borderRadius(5.px)
                            cursor(if (confirmDisabled || confirmLoading) "not-allowed" else "pointer")
                            property("transition", "background-color 0.2s")
                        }
                        onMouseEnter {
                            if (!confirmDisabled && !confirmLoading) {
                                val element = it.target as org.w3c.dom.HTMLElement
                                element.style.backgroundColor = FlagentTheme.PrimaryDark.toString()
                            }
                        }
                        onMouseLeave {
                            if (!confirmDisabled && !confirmLoading) {
                                val element = it.target as org.w3c.dom.HTMLElement
                                element.style.backgroundColor = FlagentTheme.Primary.toString()
                            }
                        }
                    }) {
                        if (confirmLoading) {
                            Span({
                                style {
                                    display(DisplayStyle.Flex)
                            property("display", "inline-flex")
                                    alignItems(AlignItems.Center)
                                    gap(8.px)
                                }
                            }) {
                                Div({
                                    style {
                                        width(12.px)
                                        height(12.px)
                                        border {
                                            width(2.px)
                                            style(LineStyle.Solid)
                                            color(Color.white)
                                        }
                                        property("border-top-color", "transparent")
                                        borderRadius(50.percent)
                                        property("animation", "spin 0.8s linear infinite")
                                    }
                                }) {}
                                Text(LocalizedStrings.saving)
                            }
                        } else {
                            Text(confirmText)
                        }
                    }
                }
            }
        }
    }
    
    // Add spin animation for loading indicator
    Style {
        """
        @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
        }
        """.trimIndent()
    }
}
