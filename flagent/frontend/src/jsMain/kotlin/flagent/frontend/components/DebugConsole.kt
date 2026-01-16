package flagent.frontend.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import flagent.frontend.api.ApiClient
import flagent.frontend.components.Icon
import flagent.frontend.theme.FlagentTheme
import flagent.api.model.EvaluationRequest
import flagent.api.model.EvaluationResponse
import flagent.api.model.EvaluationBatchRequest
import flagent.api.model.EvaluationBatchResponse
import flagent.api.model.EntityRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

/**
 * DebugConsole component - console for testing evaluation
 */
@Composable
fun DebugConsole(initialFlagKey: String? = null) {
    val apiClient = remember { ApiClient("http://localhost:18000") }
    val activeSection = remember { mutableStateOf("single") } // "single" or "batch"
    
    Div({
        style {
            padding(25.px)
            border {
                width(1.px)
                style(LineStyle.Solid)
                color(FlagentTheme.Border)
            }
            borderRadius(10.px)
            backgroundColor(FlagentTheme.Background)
            property("box-shadow", "0 2px 8px ${FlagentTheme.Shadow}")
        }
    }) {
        Div({
            style {
                display(DisplayStyle.Flex)
                alignItems(AlignItems.Center)
                gap(12.px)
                marginBottom(25.px)
                paddingBottom(20.px)
                property("border-bottom", "2px solid ${FlagentTheme.Secondary}")
            }
        }) {
            Icon(
                name = "bug_report",
                size = 32.px,
                color = FlagentTheme.Secondary
            )
            H2({
                style {
                    margin(0.px)
                    color(FlagentTheme.Text)
                    fontSize(24.px)
                    fontWeight("700")
                }
            }) {
                Text("Debug Console")
            }
            InfoTooltip(
                title = "Debug Console",
                description = "The Debug Console allows you to test flag evaluation in real-time. Enter an entity ID, type, and context to see which variant would be assigned and why.",
                details = "Use this to verify that your segments, constraints, and distributions are working correctly before deploying to production. Enable debug mode to see detailed evaluation information."
            )
        }
        
        // Tabs for Single/Batch evaluation
        Div({
            style {
                display(DisplayStyle.Flex)
                gap(8.px)
                marginBottom(25.px)
                property("border-bottom", "2px solid ${FlagentTheme.Border}")
                paddingBottom(0.px)
            }
        }) {
            Button({
                onClick { activeSection.value = "single" }
                style {
                    padding(12.px, 24.px)
                    property("background-color", "transparent")
                    color(if (activeSection.value == "single") FlagentTheme.Primary else FlagentTheme.TextLight)
                    border {
                        width(0.px)
                        style(LineStyle.None)
                    }
                    property("border-bottom", if (activeSection.value == "single") "3px solid ${FlagentTheme.Primary}" else "3px solid transparent")
                    cursor("pointer")
                    fontWeight(if (activeSection.value == "single") "600" else "500")
                    fontSize(15.px)
                    display(DisplayStyle.Flex)
                    alignItems(AlignItems.Center)
                    gap(8.px)
                    property("transition", "all 0.2s")
                    marginBottom(-2.px)
                }
                onMouseEnter {
                    if (activeSection.value != "single") {
                        (it.target as org.w3c.dom.HTMLElement).style.color = FlagentTheme.Text.toString()
                    }
                }
                onMouseLeave {
                    if (activeSection.value != "single") {
                        (it.target as org.w3c.dom.HTMLElement).style.color = FlagentTheme.TextLight.toString()
                    }
                }
            }) {
                Icon(
                    name = "search",
                    size = 18.px,
                    color = if (activeSection.value == "single") FlagentTheme.Primary else FlagentTheme.TextLight
                )
                Text("Evaluation")
            }
            Button({
                onClick { activeSection.value = "batch" }
                style {
                    padding(12.px, 24.px)
                    property("background-color", "transparent")
                    color(if (activeSection.value == "batch") FlagentTheme.Primary else FlagentTheme.TextLight)
                    border {
                        width(0.px)
                        style(LineStyle.None)
                    }
                    property("border-bottom", if (activeSection.value == "batch") "3px solid ${FlagentTheme.Primary}" else "3px solid transparent")
                    cursor("pointer")
                    fontWeight(if (activeSection.value == "batch") "600" else "500")
                    fontSize(15.px)
                    display(DisplayStyle.Flex)
                    alignItems(AlignItems.Center)
                    gap(8.px)
                    property("transition", "all 0.2s")
                    marginBottom(-2.px)
                }
                onMouseEnter {
                    if (activeSection.value != "batch") {
                        (it.target as org.w3c.dom.HTMLElement).style.color = FlagentTheme.Text.toString()
                    }
                }
                onMouseLeave {
                    if (activeSection.value != "batch") {
                        (it.target as org.w3c.dom.HTMLElement).style.color = FlagentTheme.TextLight.toString()
                    }
                }
            }) {
                Icon(
                    name = if (activeSection.value == "batch") "inventory_2" else "list",
                    size = 18.px,
                    color = if (activeSection.value == "batch") FlagentTheme.Primary else FlagentTheme.TextLight
                )
                Text("Batch Evaluation")
            }
        }
        
        when (activeSection.value) {
            "single" -> SingleEvaluationSection(apiClient, initialFlagKey)
            "batch" -> BatchEvaluationSection(apiClient, initialFlagKey)
        }
    }
}

/**
 * Single Evaluation Section
 */
@Composable
private fun SingleEvaluationSection(apiClient: ApiClient, initialFlagKey: String?) {
    val flagKey = remember { mutableStateOf(initialFlagKey ?: "") }
    val entityID = remember { mutableStateOf("a1234") }
    val entityType = remember { mutableStateOf("report") }
    val entityContextJson = remember { mutableStateOf("{\n  \"hello\": \"world\"\n}") }
    val enableDebug = remember { mutableStateOf(true) }
    val result = remember { mutableStateOf<String?>(null) }
    val loading = remember { mutableStateOf(false) }
    val error = remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(initialFlagKey) {
        if (initialFlagKey != null) {
            flagKey.value = initialFlagKey
        }
    }
    
    fun formatJson(json: String): String {
        return try {
            val jsonObj = Json.parseToJsonElement(json) as? kotlinx.serialization.json.JsonObject
            Json { prettyPrint = true }.encodeToString(jsonObj)
        } catch (e: Exception) {
            json
        }
    }
    
    Div({
        style {
            display(DisplayStyle.Flex)
            flexDirection(FlexDirection.Column)
            gap(15.px)
        }
    }) {
        // Request section
        Div({
            style {
                flex(1)
            }
        }) {
            Div({
                style {
                    display(DisplayStyle.Flex)
                    justifyContent(JustifyContent.SpaceBetween)
                    alignItems(AlignItems.Center)
                    marginBottom(10.px)
                }
            }) {
                Span {
                    Text("Request")
                }
                Button({
                    onClick {
                        loading.value = true
                        error.value = null
                        result.value = null
                        
                        CoroutineScope(Dispatchers.Main).launch {
                            try {
                                val context = try {
                                    if (entityContextJson.value.isNotBlank()) {
                                        val json = Json { ignoreUnknownKeys = true }
                                        val jsonObj = json.parseToJsonElement(entityContextJson.value) as? kotlinx.serialization.json.JsonObject
                                        jsonObj?.entries?.associate { entry ->
                                            entry.key to entry.value.toString().trim('"')
                                        }
                                    } else null
                                } catch (e: Exception) {
                                    error.value = "Invalid JSON in entity context: ${e.message}"
                                    loading.value = false
                                    return@launch
                                }
                                
                                val evalRequest = EvaluationRequest(
                                    entityID = entityID.value.ifEmpty { null },
                                    entityType = entityType.value.ifEmpty { null },
                                    entityContext = context,
                                    flagKey = flagKey.value,
                                    enableDebug = enableDebug.value
                                )
                                val evalResult = apiClient.evaluate(evalRequest)
                                result.value = formatJson(Json.encodeToString(EvaluationResponse.serializer(), evalResult))
                            } catch (e: Exception) {
                                error.value = e.message ?: "Failed to evaluate flag"
                            } finally {
                                loading.value = false
                            }
                        }
                    }
                    if (loading.value || flagKey.value.isBlank()) {
                        attr("disabled", "true")
                    }
                    style {
                        padding(10.px, 20.px)
                        backgroundColor(if (loading.value || flagKey.value.isBlank()) FlagentTheme.NeutralLighter else FlagentTheme.Primary)
                        color(FlagentTheme.Background)
                        border {
                            width(0.px)
                            style(LineStyle.None)
                        }
                        borderRadius(6.px)
                        cursor(if (loading.value || flagKey.value.isBlank()) "not-allowed" else "pointer")
                        fontSize(14.px)
                        fontWeight("600")
                        display(DisplayStyle.Flex)
                        alignItems(AlignItems.Center)
                        gap(8.px)
                        property("transition", "all 0.2s")
                        property("box-shadow", if (loading.value || flagKey.value.isBlank()) "none" else "0 2px 4px ${FlagentTheme.Shadow}")
                    }
                    onMouseEnter {
                        if (!loading.value && flagKey.value.isNotBlank()) {
                            val element = it.target as org.w3c.dom.HTMLElement
                            element.style.transform = "translateY(-1px)"
                            element.style.setProperty("box-shadow", "0 4px 6px ${FlagentTheme.ShadowHover}")
                        }
                    }
                    onMouseLeave {
                        if (!loading.value && flagKey.value.isNotBlank()) {
                            val element = it.target as org.w3c.dom.HTMLElement
                            element.style.transform = "translateY(0)"
                            element.style.setProperty("box-shadow", "0 2px 4px ${FlagentTheme.Shadow}")
                        }
                    }
                }) {
                    Icon(
                        name = if (loading.value) "hourglass_empty" else "send",
                        size = 18.px,
                        color = FlagentTheme.Background
                    )
                    Text("POST /api/v1/evaluation")
                }
            }
            
            Div({
                style {
                    display(DisplayStyle.Flex)
                    flexDirection(FlexDirection.Column)
                    gap(10.px)
                }
            }) {
                Label {
                    Text("Flag Key:")
                    Input(InputType.Text) {
                        value(flagKey.value)
                        onInput { event -> flagKey.value = event.value }
                        style {
                            width(100.percent)
                            padding(5.px)
                            border {
                                width(1.px)
                                style(LineStyle.Solid)
                                color(FlagentTheme.Border)
                            }
                            borderRadius(3.px)
                        }
                    }
                }
                Label {
                    Text("Entity ID:")
                    Input(InputType.Text) {
                        value(entityID.value)
                        onInput { event -> entityID.value = event.value }
                        style {
                            width(100.percent)
                            padding(5.px)
                            border {
                                width(1.px)
                                style(LineStyle.Solid)
                                color(FlagentTheme.Border)
                            }
                            borderRadius(3.px)
                        }
                    }
                }
                Label {
                    Text("Entity Type:")
                    Input(InputType.Text) {
                        value(entityType.value)
                        onInput { event -> entityType.value = event.value }
                        style {
                            width(100.percent)
                            padding(5.px)
                            border {
                                width(1.px)
                                style(LineStyle.Solid)
                                color(FlagentTheme.Border)
                            }
                            borderRadius(3.px)
                        }
                    }
                }
                Label {
                    Text("Entity Context (JSON):")
                    TextArea {
                        value(entityContextJson.value)
                        onInput { event -> entityContextJson.value = event.value }
                        style {
                            width(100.percent)
                            padding(10.px)
                            minHeight(150.px)
                            fontFamily("monospace")
                            fontSize(12.px)
                            border {
                                width(1.px)
                                style(LineStyle.Solid)
                                color(FlagentTheme.Border)
                            }
                            borderRadius(3.px)
                        }
                    }
                }
                Div({
                    style {
                        display(DisplayStyle.Flex)
                        alignItems(AlignItems.Center)
                        gap(5.px)
                        cursor("pointer")
                    }
                }) {
                    Label {
                        Input(InputType.Checkbox) {
                            checked(enableDebug.value)
                            onChange { event ->
                                enableDebug.value = (event.target as org.w3c.dom.HTMLInputElement).checked
                            }
                            style {
                                cursor("pointer")
                            }
                        }
                        Text("Enable Debug")
                    }
                }
            }
        }
        
        // Response section
        Div({
            style {
                flex(1)
            }
        }) {
            Span({
                style {
                    marginBottom(10.px)
                    display(DisplayStyle.Block)
                }
            }) {
                Text("Response")
            }
            if (loading.value) {
                Spinner()
            } else if (error.value != null) {
                Div({
                    style {
                        color(Color.red)
                        padding(10.px)
                        property("background-color", "rgba(239, 68, 68, 0.1)")
                        borderRadius(5.px)
                    }
                }) {
                    Text("Error: ${error.value}")
                }
            } else if (result.value != null) {
                Pre({
                    style {
                        padding(15.px)
                        backgroundColor(FlagentTheme.BackgroundAlt)
                        border {
                            width(1.px)
                            style(LineStyle.Solid)
                            color(FlagentTheme.Border)
                        }
                        borderRadius(5.px)
                        overflow("auto")
                        maxHeight(400.px)
                        fontFamily("monospace")
                        fontSize(12.px)
                        whiteSpace("pre-wrap")
                        property("word-wrap", "break-word")
                    }
                }) {
                    Text(result.value!!)
                }
            } else {
                Div({
                    style {
                        padding(15.px)
                        backgroundColor(FlagentTheme.BackgroundAlt)
                        border {
                            width(1.px)
                            style(LineStyle.Solid)
                            color(FlagentTheme.Border)
                        }
                        borderRadius(5.px)
                        minHeight(200.px)
                        color(FlagentTheme.TextLight)
                    }
                }) {
                    Text("No response yet")
                }
            }
        }
    }
}

/**
 * Batch Evaluation Section
 */
@Composable
private fun BatchEvaluationSection(apiClient: ApiClient, initialFlagKey: String?) {
    val entitiesJson = remember { mutableStateOf("""[
  {
    "entityID": "a1234",
    "entityType": "report",
    "entityContext": {
      "hello": "world"
    }
  },
  {
    "entityID": "a5678",
    "entityType": "report",
    "entityContext": {
      "hello": "world"
    }
  }
]""") }
    val flagIDs = remember { mutableStateOf("") }
    val flagKeys = remember { mutableStateOf(initialFlagKey ?: "") }
    val enableDebug = remember { mutableStateOf(true) }
    val result = remember { mutableStateOf<String?>(null) }
    val loading = remember { mutableStateOf(false) }
    val error = remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(initialFlagKey) {
        if (initialFlagKey != null) {
            flagKeys.value = initialFlagKey
        }
    }
    
    fun formatJson(json: String): String {
        return try {
            val jsonObj = Json.parseToJsonElement(json)
            Json { prettyPrint = true }.encodeToString(jsonObj)
        } catch (e: Exception) {
            json
        }
    }
    
    Div({
        style {
            display(DisplayStyle.Flex)
            flexDirection(FlexDirection.Column)
            gap(15.px)
        }
    }) {
        // Request section
        Div({
            style {
                flex(1)
            }
        }) {
            Div({
                style {
                    display(DisplayStyle.Flex)
                    justifyContent(JustifyContent.SpaceBetween)
                    alignItems(AlignItems.Center)
                    marginBottom(10.px)
                }
            }) {
                Span {
                    Text("Request")
                }
                Button({
                    onClick {
                        loading.value = true
                        error.value = null
                        result.value = null
                        
                        CoroutineScope(Dispatchers.Main).launch {
                            try {
                                // Parse entities
                                val entities = try {
                                    val json = Json { ignoreUnknownKeys = true }
                                    val entitiesList = json.parseToJsonElement(entitiesJson.value) as? kotlinx.serialization.json.JsonArray
                                    entitiesList?.mapNotNull { element ->
                                        val obj = element as? kotlinx.serialization.json.JsonObject
                                        obj?.let {
                                            val entityID = it["entityID"]?.toString()?.trim('"')
                                            val entityType = it["entityType"]?.toString()?.trim('"')
                                            val entityContext = it["entityContext"]?.let { ctx ->
                                                (ctx as? kotlinx.serialization.json.JsonObject)?.entries?.associate { entry ->
                                                    entry.key to entry.value.toString().trim('"')
                                                }
                                            }
                                            EntityRequest(
                                                entityID = entityID,
                                                entityType = entityType,
                                                entityContext = entityContext
                                            )
                                        }
                                    } ?: emptyList()
                                } catch (e: Exception) {
                                    error.value = "Invalid JSON in entities: ${e.message}"
                                    loading.value = false
                                    return@launch
                                }
                                
                                // Parse flag IDs
                                val flagIDList = flagIDs.value.split(",")
                                    .mapNotNull { it.trim().toIntOrNull() }
                                
                                // Parse flag keys
                                val flagKeyList = flagKeys.value.split(",")
                                    .map { it.trim() }
                                    .filter { it.isNotEmpty() }
                                
                                val batchRequest = EvaluationBatchRequest(
                                    entities = entities,
                                    flagIDs = flagIDList,
                                    flagKeys = flagKeyList,
                                    enableDebug = enableDebug.value
                                )
                                
                                val batchResult = apiClient.evaluateBatch(batchRequest)
                                result.value = formatJson(Json.encodeToString(EvaluationBatchResponse.serializer(), batchResult))
                            } catch (e: Exception) {
                                error.value = e.message ?: "Failed to evaluate batch"
                            } finally {
                                loading.value = false
                            }
                        }
                    }
                    if (loading.value) {
                        attr("disabled", "true")
                    }
                    style {
                        padding(5.px, 10.px)
                        backgroundColor(if (loading.value) FlagentTheme.NeutralLighter else FlagentTheme.Primary)
                        color(FlagentTheme.Background)
                        border {
                            width(0.px)
                            style(LineStyle.None)
                        }
                        borderRadius(3.px)
                        cursor(if (loading.value) "not-allowed" else "pointer")
                        fontSize(12.px)
                    }
                }) {
                    Text("POST /api/v1/evaluation/batch")
                }
            }
            
            Div({
                style {
                    display(DisplayStyle.Flex)
                    flexDirection(FlexDirection.Column)
                    gap(10.px)
                }
            }) {
                Label {
                    Text("Entities (JSON array):")
                    TextArea {
                        value(entitiesJson.value)
                        onInput { event -> entitiesJson.value = event.value }
                        style {
                            width(100.percent)
                            padding(10.px)
                            minHeight(200.px)
                            fontFamily("monospace")
                            fontSize(12.px)
                            border {
                                width(1.px)
                                style(LineStyle.Solid)
                                color(FlagentTheme.Border)
                            }
                            borderRadius(3.px)
                        }
                    }
                }
                Label {
                    Text("Flag IDs (comma-separated):")
                    Input(InputType.Text) {
                        attr("placeholder", "1, 2, 3")
                        value(flagIDs.value)
                        onInput { event -> flagIDs.value = event.value }
                        style {
                            width(100.percent)
                            padding(5.px)
                            border {
                                width(1.px)
                                style(LineStyle.Solid)
                                color(FlagentTheme.Border)
                            }
                            borderRadius(3.px)
                        }
                    }
                }
                Label {
                    Text("Flag Keys (comma-separated):")
                    Input(InputType.Text) {
                        attr("placeholder", "flag1, flag2")
                        value(flagKeys.value)
                        onInput { event -> flagKeys.value = event.value }
                        style {
                            width(100.percent)
                            padding(5.px)
                            border {
                                width(1.px)
                                style(LineStyle.Solid)
                                color(FlagentTheme.Border)
                            }
                            borderRadius(3.px)
                        }
                    }
                }
                Div({
                    style {
                        display(DisplayStyle.Flex)
                        alignItems(AlignItems.Center)
                        gap(5.px)
                        cursor("pointer")
                    }
                }) {
                    Label {
                        Input(InputType.Checkbox) {
                            checked(enableDebug.value)
                            onChange { event ->
                                enableDebug.value = (event.target as org.w3c.dom.HTMLInputElement).checked
                            }
                            style {
                                cursor("pointer")
                            }
                        }
                        Text("Enable Debug")
                    }
                }
            }
        }
        
        // Response section
        Div({
            style {
                flex(1)
            }
        }) {
            Span({
                style {
                    marginBottom(10.px)
                    display(DisplayStyle.Block)
                }
            }) {
                Text("Response")
            }
            if (loading.value) {
                Spinner()
            } else if (error.value != null) {
                Div({
                    style {
                        color(Color.red)
                        padding(10.px)
                        property("background-color", "rgba(239, 68, 68, 0.1)")
                        borderRadius(5.px)
                    }
                }) {
                    Text("Error: ${error.value}")
                }
            } else if (result.value != null) {
                Pre({
                    style {
                        padding(15.px)
                        backgroundColor(FlagentTheme.BackgroundAlt)
                        border {
                            width(1.px)
                            style(LineStyle.Solid)
                            color(FlagentTheme.Border)
                        }
                        borderRadius(5.px)
                        overflow("auto")
                        maxHeight(400.px)
                        fontFamily("monospace")
                        fontSize(12.px)
                        whiteSpace("pre-wrap")
                        property("word-wrap", "break-word")
                    }
                }) {
                    Text(result.value!!)
                }
            } else {
                Div({
                    style {
                        padding(15.px)
                        backgroundColor(FlagentTheme.BackgroundAlt)
                        border {
                            width(1.px)
                            style(LineStyle.Solid)
                            color(FlagentTheme.Border)
                        }
                        borderRadius(5.px)
                        minHeight(200.px)
                        color(FlagentTheme.TextLight)
                    }
                }) {
                    Text("No response yet")
                }
            }
        }
    }
}
