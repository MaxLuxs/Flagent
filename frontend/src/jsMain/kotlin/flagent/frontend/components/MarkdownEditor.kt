package flagent.frontend.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import flagent.frontend.i18n.LocalizedStrings
import flagent.frontend.theme.FlagentTheme
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.FlexDirection
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.backgroundColor
import org.jetbrains.compose.web.css.border
import org.jetbrains.compose.web.css.borderRadius
import org.jetbrains.compose.web.css.color
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.flex
import org.jetbrains.compose.web.css.flexDirection
import org.jetbrains.compose.web.css.fontFamily
import org.jetbrains.compose.web.css.fontSize
import org.jetbrains.compose.web.css.gap
import org.jetbrains.compose.web.css.marginTop
import org.jetbrains.compose.web.css.minHeight
import org.jetbrains.compose.web.css.padding
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.style
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H4
import org.jetbrains.compose.web.dom.Label
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.TextArea

/**
 * MarkdownEditor component - markdown editor for descriptions with preview
 */
@Composable
fun MarkdownEditor(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = LocalizedStrings.markdownPlaceholder,
    showPreview: Boolean = true
) {
    LaunchedEffect(value) {
        // Simple markdown rendering (basic implementation)
        val html = renderMarkdown(value)
        val element = kotlinx.browser.document.getElementById("markdown-preview")
        element?.innerHTML = html
    }

    Div({
        style {
            display(DisplayStyle.Flex)
            flexDirection(FlexDirection.Column)
            gap(10.px)
        }
    }) {
        Div({
            style {
                display(DisplayStyle.Flex)
                gap(10.px)
            }
        }) {
            Div({
                style {
                    flex(1)
                }
            }) {
                Label {
                    Text(LocalizedStrings.markdownEditorLabel)
                    TextArea {
                        value(value)
                        onInput { event -> onValueChange(event.value) }
                        attr("placeholder", placeholder)
                        style {
                            width(100.percent)
                            padding(10.px)
                            minHeight(200.px)
                            fontFamily("monospace")
                            fontSize(14.px)
                            border {
                                width(1.px)
                                style(LineStyle.Solid)
                                color(FlagentTheme.WorkspaceInputBorder)
                            }
                            borderRadius(5.px)
                        }
                    }
                }
            }

            if (showPreview) {
                Div({
                    style {
                        flex(1)
                        padding(10.px)
                        backgroundColor(FlagentTheme.WorkspaceInputBg)
                        borderRadius(5.px)
                        border {
                            width(1.px)
                            style(LineStyle.Solid)
                            color(FlagentTheme.WorkspaceInputBorder)
                        }
                        minHeight(200.px)
                    }
                }) {
                    H4 { Text(LocalizedStrings.previewLabel) }
                    Div({
                        id("markdown-preview")
                        style {
                            marginTop(10.px)
                            padding(10.px)
                            backgroundColor(FlagentTheme.CodeBackground)
                            borderRadius(3.px)
                            minHeight(150.px)
                        }
                    }) {
                        if (value.isEmpty()) {
                            Text(LocalizedStrings.noContent)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Simple markdown renderer (basic implementation)
 * In production, you might want to use a proper markdown library
 */
private fun renderMarkdown(markdown: String): String {
    if (markdown.isEmpty()) return ""

    var html = markdown
        // Headers
        .replace(Regex("^### (.*)$", RegexOption.MULTILINE), "<h3>$1</h3>")
        .replace(Regex("^## (.*)$", RegexOption.MULTILINE), "<h2>$1</h2>")
        .replace(Regex("^# (.*)$", RegexOption.MULTILINE), "<h1>$1</h1>")
        // Bold
        .replace(Regex("\\*\\*(.*?)\\*\\*"), "<strong>$1</strong>")
        // Italic
        .replace(Regex("\\*(.*?)\\*"), "<em>$1</em>")
        // Code blocks
        .replace(Regex("```([\\s\\S]*?)```"), "<pre><code>$1</code></pre>")
        // Inline code
        .replace(Regex("`([^`]+)`"), "<code>$1</code>")
        // Links
        .replace(Regex("\\[([^\\]]+)\\]\\(([^\\)]+)\\)"), "<a href=\"$2\">$1</a>")
        // Line breaks
        .replace("\n", "<br>")

    return html
}
