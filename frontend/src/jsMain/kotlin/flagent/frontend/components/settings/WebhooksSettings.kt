package flagent.frontend.components.settings

import androidx.compose.runtime.*
import flagent.frontend.api.ApiClient
import flagent.frontend.api.CreateWebhookRequest
import flagent.frontend.api.PutWebhookRequest
import flagent.frontend.api.WebhookResponse
import flagent.frontend.components.common.ConfirmDialog
import flagent.frontend.state.LocalThemeMode
import flagent.frontend.theme.FlagentTheme
import flagent.frontend.util.ErrorHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

private val WEBHOOK_EVENTS = listOf(
    "flag.created",
    "flag.updated",
    "flag.deleted",
    "flag.enabled",
    "flag.disabled",
    "anomaly.detected"
)

@Composable
fun WebhooksSettings() {
    val themeMode = LocalThemeMode.current
    val webhooks = remember { mutableStateOf<List<WebhookResponse>>(emptyList()) }
    val loading = remember { mutableStateOf(true) }
    val error = remember { mutableStateOf<String?>(null) }
    val scope = remember { CoroutineScope(Dispatchers.Main) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingWebhook by remember { mutableStateOf<WebhookResponse?>(null) }
    var deleteConfirm by remember { mutableStateOf<WebhookResponse?>(null) }

    fun loadWebhooks() {
        scope.launch {
            loading.value = true
            error.value = null
            try {
                webhooks.value = ApiClient.getWebhooks()
            } catch (e: Exception) {
                error.value = ErrorHandler.getUserMessage(ErrorHandler.handle(e))
            } finally {
                loading.value = false
            }
        }
    }

    LaunchedEffect(Unit) { loadWebhooks() }

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
                fontWeight("600")
                marginBottom(20.px)
                color(FlagentTheme.text(themeMode))
            }
        }) {
            Text("Webhooks")
        }
        P({
            style {
                color(FlagentTheme.textLight(themeMode))
                marginBottom(20.px)
                fontSize(14.px)
            }
        }) {
            Text("Notify external systems when flags change. Configure URLs to receive events (flag.created, flag.updated, etc.) with optional HMAC signature.")
        }
        if (error.value != null) {
            Div({
                style {
                    padding(12.px)
                    backgroundColor(FlagentTheme.errorBg(themeMode))
                    borderRadius(6.px)
                    color(FlagentTheme.errorText(themeMode))
                    marginBottom(15.px)
                }
            }) {
                Text(error.value!!)
            }
        }
        if (loading.value && webhooks.value.isEmpty()) {
            Div({
                style {
                    padding(20.px)
                    textAlign("center")
                    color(FlagentTheme.textLight(themeMode))
                }
            }) { Text("Loading...") }
        } else {
            Button({
                onClick { showAddDialog = true }
                style {
                    padding(10.px, 20.px)
                    backgroundColor(FlagentTheme.Primary)
                    color(Color.white)
                    border(0.px)
                    borderRadius(6.px)
                    cursor("pointer")
                    fontSize(14.px)
                    fontWeight("500")
                    marginBottom(20.px)
                }
            }) {
                Text("+ Add Webhook")
            }
            if (webhooks.value.isEmpty()) {
                Div({
                    style {
                        padding(20.px)
                        backgroundColor(FlagentTheme.inputBg(themeMode))
                        borderRadius(6.px)
                        textAlign("center")
                        color(FlagentTheme.textLight(themeMode))
                    }
                }) {
                    Text("No webhooks configured. Add one to receive flag change notifications.")
                }
            } else {
                webhooks.value.forEach { wh ->
                    Div({
                        style {
                            padding(12.px)
                            backgroundColor(FlagentTheme.inputBg(themeMode))
                            borderRadius(6.px)
                            marginBottom(10.px)
                        }
                    }) {
                        Div({
                            style {
                                display(DisplayStyle.Flex)
                                justifyContent(JustifyContent.SpaceBetween)
                                alignItems(AlignItems.Center)
                            }
                        }) {
                            Div {
                                Div({ style { fontWeight("500"); color(FlagentTheme.text(themeMode)) } }) { Text(wh.url) }
                                Div({ style { fontSize(13.px); color(FlagentTheme.textLight(themeMode)); marginTop(4.px) } }) {
                                    Text(wh.events.joinToString(", "))
                                }
                            }
                            Div({
                                style {
                                    display(DisplayStyle.Flex)
                                    gap(8.px)
                                }
                            }) {
                                Button({
                                    onClick { editingWebhook = wh }
                                    style {
                                        padding(6.px, 12.px)
                                        backgroundColor(FlagentTheme.inputBg(themeMode))
                                        color(FlagentTheme.text(themeMode))
                                        border(0.px)
                                        borderRadius(4.px)
                                        cursor("pointer")
                                        fontSize(13.px)
                                    }
                                }) { Text("Edit") }
                                Button({
                                    onClick { deleteConfirm = wh }
                                    style {
                                        padding(6.px, 12.px)
                                        backgroundColor(FlagentTheme.errorBg(themeMode))
                                        color(FlagentTheme.errorText(themeMode))
                                        border(0.px)
                                        borderRadius(4.px)
                                        cursor("pointer")
                                        fontSize(13.px)
                                    }
                                }) { Text("Delete") }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        WebhookEditDialog(
            onSave = { url, events, secret, enabled ->
                scope.launch {
                    try {
                        ApiClient.createWebhook(CreateWebhookRequest(url, events, secret.ifBlank { null }, enabled))
                        showAddDialog = false
                        loadWebhooks()
                    } catch (e: Exception) {
                        error.value = ErrorHandler.getUserMessage(ErrorHandler.handle(e))
                    }
                }
            },
            onCancel = { showAddDialog = false }
        )
    }
    editingWebhook?.let { wh ->
        WebhookEditDialog(
            initialUrl = wh.url,
            initialEvents = wh.events,
            initialSecret = wh.secret ?: "",
            initialEnabled = wh.enabled,
            onSave = { url, events, secret, enabled ->
                scope.launch {
                    try {
                        ApiClient.updateWebhook(wh.id, PutWebhookRequest(url, events, secret.ifBlank { null }, enabled))
                        editingWebhook = null
                        loadWebhooks()
                    } catch (e: Exception) {
                        error.value = ErrorHandler.getUserMessage(ErrorHandler.handle(e))
                    }
                }
            },
            onCancel = { editingWebhook = null }
        )
    }
    deleteConfirm?.let { wh ->
        ConfirmDialog(
            isOpen = true,
            title = "Delete webhook",
            message = "Delete webhook to ${wh.url}?",
            confirmLabel = "Delete",
            isDangerous = true,
            onConfirm = {
                scope.launch {
                    try {
                        ApiClient.deleteWebhook(wh.id)
                        deleteConfirm = null
                        loadWebhooks()
                    } catch (e: Exception) {
                        error.value = ErrorHandler.getUserMessage(ErrorHandler.handle(e))
                    }
                }
            },
            onCancel = { deleteConfirm = null }
        )
    }
}

@Composable
private fun WebhookEditDialog(
    initialUrl: String = "",
    initialEvents: List<String> = emptyList(),
    initialSecret: String = "",
    initialEnabled: Boolean = true,
    onSave: (url: String, events: List<String>, secret: String, enabled: Boolean) -> Unit,
    onCancel: () -> Unit
) {
    val themeMode = LocalThemeMode.current
    var url by remember { mutableStateOf(initialUrl) }
    var selectedEvents by remember { mutableStateOf(initialEvents.toSet()) }
    var secret by remember { mutableStateOf(initialSecret) }
    var enabled by remember { mutableStateOf(initialEnabled) }

    Div({
        style {
            position(Position.Fixed)
            top(0.px)
            left(0.px)
            right(0.px)
            bottom(0.px)
            backgroundColor(Color("rgba(0,0,0,0.5)"))
            display(DisplayStyle.Flex)
            alignItems(AlignItems.Center)
            justifyContent(JustifyContent.Center)
            property("z-index", "1000")
        }
        onClick { onCancel() }
    }) {
        Div({
            style {
                backgroundColor(FlagentTheme.cardBg(themeMode))
                borderRadius(8.px)
                padding(24.px)
                property("box-shadow", "0 4px 20px rgba(0,0,0,0.2)")
                maxWidth(480.px)
                width(100.percent)
            }
            onClick { it.stopPropagation() }
        }) {
            H3({
                style {
                    marginBottom(16.px)
                    color(FlagentTheme.text(themeMode))
                }
            }) { Text(if (initialUrl.isEmpty()) "Add Webhook" else "Edit Webhook") }
            Div({ style { marginBottom(12.px) } }) {
                Span({
                    style {
                        display(DisplayStyle.Block)
                        marginBottom(4.px)
                        fontWeight("500")
                        color(FlagentTheme.text(themeMode))
                    }
                }) { Text("URL") }
                Input(InputType.Text) {
                    value(url)
                    onInput { url = it.value }
                    style {
                        width(100.percent)
                        padding(10.px)
                        border(1.px, LineStyle.Solid, FlagentTheme.inputBorder(themeMode))
                        borderRadius(6.px)
                        backgroundColor(FlagentTheme.inputBg(themeMode))
                        color(FlagentTheme.text(themeMode))
                    }
                }
            }
            Div({ style { marginBottom(12.px) } }) {
                Span({
                    style {
                        display(DisplayStyle.Block)
                        marginBottom(4.px)
                        fontWeight("500")
                        color(FlagentTheme.text(themeMode))
                    }
                }) { Text("Events") }
                Div({
                    style {
                        display(DisplayStyle.Flex)
                        flexWrap(FlexWrap.Wrap)
                        gap(8.px)
                    }
                }) {
                    WEBHOOK_EVENTS.forEach { ev ->
                        Button({
                            val isSelected = ev in selectedEvents
                            onClick {
                                selectedEvents = if (isSelected) selectedEvents - ev else selectedEvents + ev
                            }
                            style {
                                padding(6.px, 12.px)
                                backgroundColor(if (ev in selectedEvents) FlagentTheme.Primary else FlagentTheme.inputBg(themeMode))
                                color(if (ev in selectedEvents) Color.white else FlagentTheme.text(themeMode))
                                border(0.px)
                                borderRadius(4.px)
                                cursor("pointer")
                                fontSize(12.px)
                            }
                        }) { Text(ev) }
                    }
                }
            }
            Div({ style { marginBottom(12.px) } }) {
                Span({
                    style {
                        display(DisplayStyle.Block)
                        marginBottom(4.px)
                        fontWeight("500")
                        color(FlagentTheme.text(themeMode))
                    }
                }) { Text("Secret (optional, for HMAC)") }
                Input(InputType.Password) {
                    value(secret)
                    onInput { secret = it.value }
                    style {
                        width(100.percent)
                        padding(10.px)
                        border(1.px, LineStyle.Solid, FlagentTheme.inputBorder(themeMode))
                        borderRadius(6.px)
                        backgroundColor(FlagentTheme.inputBg(themeMode))
                        color(FlagentTheme.text(themeMode))
                    }
                }
            }
            Div({ style { marginBottom(16.px); display(DisplayStyle.Flex); alignItems(AlignItems.Center); gap(8.px) } }) {
                Input(InputType.Checkbox) {
                    checked(enabled)
                    onInput { enabled = (it.target.asDynamic().checked as Boolean) }
                }
                Span({ style { color(FlagentTheme.text(themeMode)) } }) { Text("Enabled") }
            }
            Div({
                style {
                    display(DisplayStyle.Flex)
                    justifyContent(JustifyContent.FlexEnd)
                    gap(8.px)
                }
            }) {
                Button({
                    onClick { onCancel() }
                    style {
                        padding(10.px, 20.px)
                        backgroundColor(FlagentTheme.inputBg(themeMode))
                        color(FlagentTheme.text(themeMode))
                        border(0.px)
                        borderRadius(6.px)
                        cursor("pointer")
                    }
                }) { Text("Cancel") }
                Button({
                    onClick {
                        if (url.isNotBlank() && selectedEvents.isNotEmpty()) {
                            onSave(url, selectedEvents.toList(), secret, enabled)
                        }
                    }
                    style {
                        padding(10.px, 20.px)
                        backgroundColor(FlagentTheme.Primary)
                        color(Color.white)
                        border(0.px)
                        borderRadius(6.px)
                        cursor("pointer")
                    }
                }) { Text("Save") }
            }
        }
    }
}
