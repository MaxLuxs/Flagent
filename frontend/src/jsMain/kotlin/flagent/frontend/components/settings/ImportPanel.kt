package flagent.frontend.components.settings

import androidx.compose.runtime.*
import flagent.frontend.api.ApiClient
import flagent.frontend.api.ImportResult
import flagent.frontend.components.Icon
import flagent.frontend.state.LocalThemeMode
import flagent.frontend.theme.FlagentTheme
import flagent.frontend.util.ErrorHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

@Composable
fun ImportPanel() {
    val themeMode = LocalThemeMode.current
    val content = remember { mutableStateOf("") }
    val format = remember { mutableStateOf("yaml") }
    val loading = remember { mutableStateOf(false) }
    val result = remember { mutableStateOf<ImportResult?>(null) }
    val error = remember { mutableStateOf<String?>(null) }
    val scope = remember { CoroutineScope(Dispatchers.Main) }

    fun doImport() {
        if (content.value.isBlank()) {
            error.value = "Content is required"
            return
        }
        scope.launch {
            loading.value = true
            result.value = null
            error.value = null
            try {
                result.value = ApiClient.importFlags(format.value, content.value)
            } catch (e: Exception) {
                error.value = ErrorHandler.getUserMessage(ErrorHandler.handle(e))
            } finally {
                loading.value = false
            }
        }
    }

    Div({
        style {
            backgroundColor(FlagentTheme.cardBg(themeMode))
            borderRadius(8.px)
            padding(20.px)
            property("box-shadow", "0 2px 8px rgba(0,0,0,0.1)")
        }
    }) {
        H2({
            style {
                fontSize(20.px)
                fontWeight(600)
                marginBottom(20.px)
                color(FlagentTheme.text(themeMode))
            }
        }) {
            Text("Import Flags")
        }
        P({
            style {
                color(FlagentTheme.textLight(themeMode))
                marginBottom(20.px)
                fontSize(14.px)
            }
        }) {
            Text("Paste YAML or JSON in GitOps format (from Export). Existing flags with matching keys will be updated.")
        }

        error.value?.let { err ->
            Div({
                style {
                    padding(12.px)
                    backgroundColor(FlagentTheme.errorBg(themeMode))
                    borderRadius(6.px)
                    color(FlagentTheme.errorText(themeMode))
                    marginBottom(16.px)
                }
            }) {
                Text(err)
            }
        }

        Div({
            style {
                marginBottom(16.px)
                display(DisplayStyle.Flex)
                alignItems(AlignItems.Center)
                gap(12.px)
            }
        }) {
            Span({ style { fontWeight("500"); color(FlagentTheme.text(themeMode)) } }) { Text("Format:") }
            Select(attrs = {
                attr("value", format.value)
                onChange { event ->
                    format.value = (event.target as org.w3c.dom.HTMLSelectElement).value
                }
                style {
                    padding(8.px, 12.px)
                    border(1.px, LineStyle.Solid, FlagentTheme.inputBorder(themeMode))
                    borderRadius(6.px)
                    fontSize(14.px)
                    backgroundColor(FlagentTheme.inputBg(themeMode))
                    color(FlagentTheme.text(themeMode))
                    cursor("pointer")
                }
            }) {
                Option(value = "yaml") { Text("YAML") }
                Option(value = "json") { Text("JSON") }
            }
        }

        TextArea {
            value(content.value)
            onInput { content.value = it.value }
            attr("placeholder", "Paste GitOps YAML or JSON here...")
            style {
                width(100.percent)
                minHeight(200.px)
                padding(12.px)
                border(1.px, LineStyle.Solid, FlagentTheme.inputBorder(themeMode))
                borderRadius(6.px)
                fontSize(14.px)
                fontFamily("'Monaco', 'Courier New', monospace")
                backgroundColor(FlagentTheme.inputBg(themeMode))
                color(FlagentTheme.text(themeMode))
            }
        }

        Button({
            onClick { doImport() }
            if (loading.value) attr("disabled", "true")
            style {
                marginTop(16.px)
                padding(10.px, 20.px)
                backgroundColor(if (loading.value) FlagentTheme.NeutralLighter else FlagentTheme.Primary)
                color(Color.white)
                border(0.px)
                borderRadius(6.px)
                cursor(if (loading.value) "not-allowed" else "pointer")
                fontSize(14.px)
                fontWeight(500)
            }
        }) {
            Text(if (loading.value) "Importing..." else "Import")
        }

        result.value?.let { r ->
            Div({
                style {
                    marginTop(20.px)
                    padding(16.px)
                    backgroundColor(FlagentTheme.inputBg(themeMode))
                    borderRadius(6.px)
                    border(1.px, LineStyle.Solid, FlagentTheme.cardBorder(themeMode))
                }
            }) {
                Div({
                    style {
                        display(DisplayStyle.Flex)
                        alignItems(AlignItems.Center)
                        gap(8.px)
                        marginBottom(8.px)
                        color(FlagentTheme.Success)
                        fontWeight("600")
                    }
                }) {
                    Icon("check_circle", size = 20.px, color = FlagentTheme.Success)
                    Text("Import complete")
                }
                P({ style { margin(0.px); fontSize(14.px); color(FlagentTheme.text(themeMode)) } }) {
                    Text("Created: ${r.created}, Updated: ${r.updated}")
                }
                if (r.errors.isNotEmpty()) {
                    Div({
                        style {
                            marginTop(8.px)
                            padding(8.px)
                            backgroundColor(FlagentTheme.errorBg(themeMode))
                            borderRadius(4.px)
                            fontSize(13.px)
                            color(FlagentTheme.errorText(themeMode))
                        }
                    }) {
                        Text("Errors:")
                        r.errors.forEach { err ->
                            Div({ style { marginTop(4.px) } }) { Text(err) }
                        }
                    }
                }
            }
        }
    }
}
