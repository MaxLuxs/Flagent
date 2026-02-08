package flagent.frontend.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import flagent.frontend.i18n.LocalizedStrings
import flagent.frontend.state.LocalThemeMode
import flagent.frontend.theme.FlagentTheme
import kotlinx.browser.document
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

/**
 * JsonEditor component - enhanced JSON editor with syntax highlighting and validation
 * 
 * Supports editing JSON key/value pairs (without outer braces) for variant attachments
 */
@Composable
fun JsonEditor(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "\"key1\": \"value1\",\n\"key2\": \"value2\"",
    minHeight: CSSSizeValue<CSSUnit.px> = 150.px,
    showValidation: Boolean = true
) {
    val themeMode = LocalThemeMode.current
    val isValid = remember { mutableStateOf(true) }
    val errorMessage = remember { mutableStateOf<String?>(null) }
    val editorId = remember { "json-editor-${kotlin.random.Random.nextInt()}" }
    
    fun validateJson(jsonStr: String): Boolean {
        if (jsonStr.isBlank()) {
            isValid.value = true
            errorMessage.value = null
            return true
        }
        
        return try {
            // Wrap in braces to make valid JSON object
            val wrappedJson = "{${jsonStr}}"
            val json = Json { ignoreUnknownKeys = true; isLenient = true }
            json.parseToJsonElement(wrappedJson) as? JsonObject
            isValid.value = true
            errorMessage.value = null
            true
        } catch (e: Exception) {
            isValid.value = false
            errorMessage.value = LocalizedStrings.invalidJsonWithReason(e.message)
            false
        }
    }
    
    Div({
        style {
            display(DisplayStyle.Flex)
            flexDirection(FlexDirection.Column)
            gap(8.px)
        }
    }) {
        Div({
            id(editorId)
            style {
                position(Position.Relative)
            }
        }) {
            TextArea {
                value(value)
                onInput { event ->
                    val newValue = event.value
                    onValueChange(newValue)
                    validateJson(newValue)
                }
                attr("placeholder", placeholder)
                style {
                    width(100.percent)
                    padding(12.px)
                    minHeight(minHeight)
                    fontFamily("'Monaco', 'Menlo', 'Ubuntu Mono', 'Consolas', 'source-code-pro', monospace")
                    fontSize(13.px)
                    property("line-height", "1.5")
                    color(FlagentTheme.CodeText)
                    backgroundColor(FlagentTheme.CodeBackground)
                    border {
                        width(2.px)
                        style(LineStyle.Solid)
                        color(if (isValid.value) FlagentTheme.inputBorder(themeMode) else FlagentTheme.Error)
                    }
                    borderRadius(5.px)
                    property("resize", "vertical")
                    property("tab-size", "2")
                    property("white-space", "pre")
                    property("word-wrap", "normal")
                    property("overflow-wrap", "normal")
                    property("transition", "border-color 0.2s ease")
                    
                    // Syntax highlighting hints via CSS
                    property("caret-color", FlagentTheme.Primary.toString())
                }
                
                // Add focus styles
                onFocus {
                    val element = it.target as org.w3c.dom.HTMLElement
                    element.style.borderColor = if (isValid.value) FlagentTheme.Primary.toString() else FlagentTheme.Error.toString()
                }
                onBlur {
                    val element = it.target as org.w3c.dom.HTMLElement
                    element.style.borderColor = if (isValid.value) FlagentTheme.inputBorder(themeMode).toString() else FlagentTheme.Error.toString()
                }
            }
        }
        
        // Validation message
        if (showValidation) {
            if (!isValid.value && errorMessage.value != null) {
                Div({
                    style {
                        display(DisplayStyle.Flex)
                        alignItems(AlignItems.Center)
                        gap(6.px)
                        padding(8.px, 12.px)
                        property("background-color", "rgba(239, 68, 68, 0.1)")
                        border {
                            width(1.px)
                            style(LineStyle.Solid)
                            color(FlagentTheme.Error)
                        }
                        borderRadius(4.px)
                        fontSize(12.px)
                        color(FlagentTheme.Error)
                    }
                }) {
                    Icon(
                        name = "error",
                        size = 16.px,
                        color = FlagentTheme.Error
                    )
                    Text(errorMessage.value ?: LocalizedStrings.jsonInvalid)
                }
            } else if (isValid.value && value.isNotBlank()) {
                Div({
                    style {
                        display(DisplayStyle.Flex)
                        alignItems(AlignItems.Center)
                        gap(6.px)
                        padding(6.px, 12.px)
                        fontSize(11.px)
                        color(FlagentTheme.Success)
                    }
                }) {
                    Icon(
                        name = "check_circle",
                        size = 14.px,
                        color = FlagentTheme.Success
                    )
                    Text(LocalizedStrings.jsonValid)
                }
            }
        }
        
        // Helper text
        Span({
            style {
                fontSize(11.px)
                color(FlagentTheme.textLight(themeMode))
                property("line-height", "1.4")
            }
        }) {
            Text(LocalizedStrings.jsonHelperText)
        }
    }
}
