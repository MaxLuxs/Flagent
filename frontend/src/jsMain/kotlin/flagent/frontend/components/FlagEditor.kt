package flagent.frontend.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import flagent.frontend.api.ApiClient
import flagent.frontend.components.Icon
import flagent.frontend.components.Slider
import flagent.frontend.i18n.LocalizedStrings
import flagent.frontend.state.LocalThemeMode
import flagent.frontend.theme.FlagentTheme
import flagent.api.constants.ConstraintOperators
import flagent.api.model.FlagResponse
import flagent.api.model.CreateFlagRequest
import flagent.api.model.PutFlagRequest
import flagent.frontend.navigation.Route
import flagent.frontend.navigation.Router
import flagent.frontend.navigation.flagTabName
import flagent.frontend.navigation.isFlagDetailRoute
import flagent.frontend.util.Validation
import flagent.frontend.util.ValidationResult
import flagent.frontend.util.createSortable
import flagent.frontend.util.SortableInstance
import kotlinx.browser.document
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.H3
import org.jetbrains.compose.web.dom.H5

/**
 * FlagEditor component - create/edit flag with tabs
 */
@Composable
fun FlagEditor(flagId: Int?) {
    val themeMode = LocalThemeMode.current
    val route = Router.currentRoute
    val activeTab = when {
        flagId != null && route.isFlagDetailRoute() -> route.flagTabName()
        else -> "Config"
    }
    val flag = remember { mutableStateOf<FlagResponse?>(null) }
    val loading = remember { mutableStateOf(flagId != null) }
    val error = remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(flagId) {
        if (flagId != null) {
            loading.value = true
            error.value = null
            try {
                val fetchedFlag = ApiClient.getFlag(flagId)
                flag.value = fetchedFlag
            } catch (e: Exception) {
                error.value = e.message ?: LocalizedStrings.failedToLoadFlag
            } finally {
                loading.value = false
            }
        }
    }
    
    Div({
        style {
            marginTop(20.px)
            padding(20.px)
        }
    }) {
        // Breadcrumbs
        Div({
            style {
                marginBottom(25.px)
                display(DisplayStyle.Flex)
                alignItems(AlignItems.Center)
                gap(12.px)
            }
        }) {
            Button({
                onClick { Router.navigateBack() }
                style {
                    padding(10.px, 16.px)
                    backgroundColor(FlagentTheme.Neutral)
                    color(Color.white)
                    border {
                        width(0.px)
                        style(LineStyle.None)
                    }
                    borderRadius(6.px)
                    cursor("pointer")
                    fontWeight("600")
                    fontSize(14.px)
                    display(DisplayStyle.Flex)
                    alignItems(AlignItems.Center)
                    gap(6.px)
                    property("transition", "all 0.2s")
                    property("box-shadow", "0 2px 4px ${FlagentTheme.Shadow}")
                }
                onMouseEnter {
                    val element = it.target as org.w3c.dom.HTMLElement
                    element.style.backgroundColor = FlagentTheme.NeutralLight.toString()
                    element.style.transform = "translateX(-2px)"
                }
                onMouseLeave {
                    val element = it.target as org.w3c.dom.HTMLElement
                    element.style.backgroundColor = FlagentTheme.Neutral.toString()
                    element.style.transform = "translateX(0)"
                }
            }) {
                Span { Text("←") }
                Text(LocalizedStrings.backToFlags)
            }
            if (flagId != null) {
                Span({
                    style {
                        padding(6.px, 12.px)
                        backgroundColor(FlagentTheme.PrimaryLight)
                        color(FlagentTheme.PrimaryDark)
                        borderRadius(6.px)
                        fontSize(13.px)
                        fontWeight("600")
                    }
                }) {
                    Text("${LocalizedStrings.flagId}: $flagId")
                }
            }
        }
        
        if (loading.value) {
            Spinner()
        } else if (error.value != null) {
            Div({
                style {
                    color(FlagentTheme.Error)
                    padding(10.px)
                    property("background-color", "rgba(239, 68, 68, 0.1)")
                    borderRadius(5.px)
                }
            }) {
                Text("${LocalizedStrings.error}: ${error.value}")
            }
        } else if (flagId == null) {
            // Create new flag - simple form
            CreateFlagForm()
        } else if (flag.value != null) {
            // Edit existing flag - tabs interface
            Div({
                style {
                    width(100.percent)
                }
            }) {
                // Tabs navigation
                Div({
                    style {
                        display(DisplayStyle.Flex)
                        gap(8.px)
                        property("border-bottom", "2px solid ${FlagentTheme.inputBorder(themeMode)}")
                        marginBottom(25.px)
                        paddingBottom(0.px)
                    }
                }) {
                    Button({
                        onClick { Router.navigateTo(Route.FlagDetail(flagId!!), addToHistory = false) }
                        style {
                            padding(12.px, 24.px)
                            property("background-color", "transparent")
                            color(if (activeTab == "Config") FlagentTheme.Primary else FlagentTheme.textLight(themeMode))
                            border {
                                width(0.px)
                                style(LineStyle.None)
                            }
                            property("border-bottom", if (activeTab == "Config") "3px solid ${FlagentTheme.Primary}" else "3px solid transparent")
                            cursor("pointer")
                            fontWeight(if (activeTab == "Config") "600" else "500")
                            fontSize(15.px)
                            display(DisplayStyle.Flex)
                            alignItems(AlignItems.Center)
                            gap(8.px)
                            property("transition", "all 0.2s")
                            marginBottom(-2.px)
                        }
                        onMouseEnter {
                            if (activeTab != "Config") {
                                (it.target as org.w3c.dom.HTMLElement).style.color = FlagentTheme.Text.toString()
                            }
                        }
                        onMouseLeave {
                            if (activeTab != "Config") {
                                (it.target as org.w3c.dom.HTMLElement).style.color = FlagentTheme.textLight(themeMode).toString()
                            }
                        }
                    }) {
                        Icon(
                            name = "settings",
                            size = 18.px,
                            color = if (activeTab == "Config") FlagentTheme.Primary else FlagentTheme.textLight(themeMode)
                        )
                        Text(LocalizedStrings.config)
                    }
                    Button({
                        onClick { Router.navigateTo(Route.FlagHistory(flagId), addToHistory = false) }
                        style {
                            padding(12.px, 24.px)
                            property("background-color", "transparent")
                            color(if (activeTab == "History") FlagentTheme.Primary else FlagentTheme.textLight(themeMode))
                            border {
                                width(0.px)
                                style(LineStyle.None)
                            }
                            property("border-bottom", if (activeTab == "History") "3px solid ${FlagentTheme.Primary}" else "3px solid transparent")
                            cursor("pointer")
                            fontWeight(if (activeTab == "History") "600" else "500")
                            fontSize(15.px)
                            display(DisplayStyle.Flex)
                            alignItems(AlignItems.Center)
                            gap(8.px)
                            property("transition", "all 0.2s")
                            marginBottom(-2.px)
                        }
                        onMouseEnter {
                            if (activeTab != "History") {
                                (it.target as org.w3c.dom.HTMLElement).style.color = FlagentTheme.Text.toString()
                            }
                        }
                        onMouseLeave {
                            if (activeTab != "History") {
                                (it.target as org.w3c.dom.HTMLElement).style.color = FlagentTheme.textLight(themeMode).toString()
                            }
                        }
                    }) {
                        Icon(
                            name = "history",
                            size = 18.px,
                            color = if (activeTab == "History") FlagentTheme.Primary else FlagentTheme.textLight(themeMode)
                        )
                        Text(LocalizedStrings.history)
                    }
                    if (flagent.frontend.config.AppConfig.Features.enableMetrics) {
                        FlagEditorTabButton(themeMode, "Metrics", "analytics", activeTab == "Metrics") {
                            Router.navigateTo(Route.FlagMetrics(flagId), addToHistory = false)
                        }
                    }
                    if (flagent.frontend.config.AppConfig.Features.enableSmartRollout) {
                        FlagEditorTabButton(themeMode, "Rollout", "trending_up", activeTab == "Rollout") {
                            Router.navigateTo(Route.FlagRollout(flagId), addToHistory = false)
                        }
                    }
                    if (flagent.frontend.config.AppConfig.Features.enableAnomalyDetection) {
                        FlagEditorTabButton(themeMode, "Anomalies", "warning", activeTab == "Anomalies") {
                            Router.navigateTo(Route.FlagAnomalies(flagId), addToHistory = false)
                        }
                    }
                }
                
                // Tab content
                val metricsMetricType = (route as? Route.FlagMetrics)?.metricType
                when (activeTab) {
                    "Config" -> FlagConfigTab(flag.value!!, flagId)
                    "History" -> FlagHistory(flagId)
                    "Metrics" -> flagent.frontend.components.metrics.MetricsDashboard(flagId, metricsMetricType)
                    "Rollout" -> flagent.frontend.components.rollout.SmartRolloutConfig(flagId)
                    "Anomalies" -> flagent.frontend.components.anomaly.AnomalyAlertsList(flagId)
                    else -> FlagConfigTab(flag.value!!, flagId)
                }
            }
        }
    }
}

/**
 * Reusable tab button for flag detail tabs
 */
@Composable
private fun FlagEditorTabButton(themeMode: flagent.frontend.state.ThemeMode, label: String, icon: String, isActive: Boolean, onClick: () -> Unit) {
    Button({
        onClick { onClick() }
        style {
            padding(12.px, 24.px)
            property("background-color", "transparent")
            color(if (isActive) FlagentTheme.Primary else FlagentTheme.textLight(themeMode))
            border { width(0.px); style(LineStyle.None) }
            property("border-bottom", if (isActive) "3px solid ${FlagentTheme.Primary}" else "3px solid transparent")
            cursor("pointer")
            fontWeight(if (isActive) "600" else "500")
            fontSize(15.px)
            display(DisplayStyle.Flex)
            alignItems(AlignItems.Center)
            gap(8.px)
            property("transition", "all 0.2s")
            marginBottom(-2.px)
        }
        onMouseEnter {
            if (!isActive) (it.target as org.w3c.dom.HTMLElement).style.color = FlagentTheme.Text.toString()
        }
        onMouseLeave {
            if (!isActive) (it.target as org.w3c.dom.HTMLElement).style.color = FlagentTheme.textLight(themeMode).toString()
        }
    }) {
        Icon(name = icon, size = 18.px, color = if (isActive) FlagentTheme.Primary else FlagentTheme.textLight(themeMode))
        Text(label)
    }
}

/**
 * Create flag form - simple form for creating new flags
 */
@Composable
private fun CreateFlagForm() {
    val themeMode = LocalThemeMode.current
    val description = remember { mutableStateOf("") }
    val key = remember { mutableStateOf("") }
    val saving = remember { mutableStateOf(false) }
    val error = remember { mutableStateOf<String?>(null) }
    
    Div({
        style {
            maxWidth(600.px)
        }
    }) {
        Div({
            style {
                display(DisplayStyle.Flex)
                flexDirection(FlexDirection.Column)
                gap(15.px)
            }
        }) {
            Label {
                Text("${LocalizedStrings.description}:")
                Input(InputType.Text) {
                    attr("placeholder", LocalizedStrings.flagDescriptionPlaceholder)
                    value(description.value)
                    onInput { event -> description.value = event.value }
                    style {
                        width(100.percent)
                        padding(8.px)
                        border {
                            width(1.px)
                            style(LineStyle.Solid)
                            color(FlagentTheme.inputBorder(themeMode))
                        }
                        borderRadius(4.px)
                    }
                }
            }
            
            Label {
                Text(LocalizedStrings.keyOptional)
                Input(InputType.Text) {
                    attr("placeholder", LocalizedStrings.flagKeyPlaceholder)
                    value(key.value)
                    onInput { event -> key.value = event.value }
                    style {
                        width(100.percent)
                        padding(8.px)
                        border {
                            width(1.px)
                            style(LineStyle.Solid)
                            color(FlagentTheme.inputBorder(themeMode))
                        }
                        borderRadius(4.px)
                    }
                }
            }
            
            if (error.value != null) {
                Div({
                    style {
                        color(FlagentTheme.Error)
                        padding(10.px)
                        property("background-color", "rgba(239, 68, 68, 0.1)")
                        borderRadius(5.px)
                    }
                }) {
                    Text("${LocalizedStrings.error}: ${error.value}")
                }
            }
            
            Button({
                onClick {
                    if (description.value.isBlank()) {
                        error.value = LocalizedStrings.descriptionIsRequired
                        return@onClick
                    }
                    
                    saving.value = true
                    error.value = null
                    
                    CoroutineScope(Dispatchers.Main).launch {
                        try {
                            val newFlag = ApiClient.createFlag(
                                CreateFlagRequest(
                                    description = description.value,
                                    key = key.value.takeIf { it.isNotEmpty() }
                                )
                            )
                            Router.navigateTo(Route.FlagDetail(newFlag.id))
                        } catch (e: Exception) {
                            error.value = e.message ?: LocalizedStrings.failedToCreateFlag
                        } finally {
                            saving.value = false
                        }
                    }
                }
                if (saving.value || description.value.isBlank()) {
                    attr("disabled", "true")
                }
                style {
                    padding(10.px, 20.px)
                    backgroundColor(if (saving.value || description.value.isBlank()) FlagentTheme.NeutralLighter else FlagentTheme.Success)
                    color(Color.white)
                    border {
                        width(0.px)
                        style(LineStyle.None)
                    }
                    borderRadius(5.px)
                    cursor(if (saving.value || description.value.isBlank()) "not-allowed" else "pointer")
                }
            }) {
                Text(if (saving.value) LocalizedStrings.creating else LocalizedStrings.createFlag)
            }
        }
    }
}

/**
 * Flag Config Tab - main configuration tab
 * Contains: Flag Settings, Variants, Segments, Debug Console, Delete Flag
 */
@Composable
private fun FlagConfigTab(flag: FlagResponse, flagId: Int) {
    val themeMode = LocalThemeMode.current
    val flagState = remember { mutableStateOf(flag) }
    val showMdEditor = remember { mutableStateOf(false) }
    val allTags = remember { mutableStateOf<List<flagent.api.model.TagResponse>>(emptyList()) }
    val tagInputVisible = remember { mutableStateOf(false) }
    val newTagValue = remember { mutableStateOf("") }
    val entityTypes = remember { mutableStateOf<List<String>>(emptyList()) }
    
    // Load all tags and entity types
    LaunchedEffect(Unit) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                allTags.value = ApiClient.getAllTags()
                entityTypes.value = ApiClient.getEntityTypes()
            } catch (e: Exception) {
                // Silent fail
            }
        }
    }
    
    // Refresh flag when needed
    fun refreshFlag() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val updated = ApiClient.getFlag(flagId)
                flagState.value = updated
            } catch (e: Exception) {
                // Silent fail
            }
        }
    }
    
    Div({
        style {
            display(DisplayStyle.Flex)
            flexDirection(FlexDirection.Column)
            gap(20.px)
        }
    }) {
        // Flag Settings Card
        FlagSettingsCard(
            flag = flagState.value,
            flagId = flagId,
            showMdEditor = showMdEditor,
            allTags = allTags.value,
            tagInputVisible = tagInputVisible,
            newTagValue = newTagValue,
            entityTypes = entityTypes.value,
            onFlagUpdated = { refreshFlag() }
        )
        
        VariantsSection(flagId, flagState.value.variants, onUpdate = { refreshFlag() })
        SegmentsSection(flagId, flagState.value.segments, flagState.value.variants, onUpdate = { refreshFlag() })
        
        // Debug Console
        DebugConsole(flagState.value.key)
        
        // Flag Settings (Delete)
        FlagDeleteCard(flagId)
    }
}

/**
 * Flag Settings Card - main flag configuration
 */
@Composable
private fun FlagSettingsCard(
    flag: FlagResponse,
    flagId: Int,
    showMdEditor: androidx.compose.runtime.MutableState<Boolean>,
    allTags: List<flagent.api.model.TagResponse>,
    tagInputVisible: androidx.compose.runtime.MutableState<Boolean>,
    newTagValue: androidx.compose.runtime.MutableState<String>,
    entityTypes: List<String>,
    onFlagUpdated: () -> Unit
) {
    val themeMode = LocalThemeMode.current
    val key = remember { mutableStateOf(flag.key) }
    val description = remember { mutableStateOf(flag.description) }
    val dataRecordsEnabled = remember { mutableStateOf(flag.dataRecordsEnabled) }
    val entityType = remember { mutableStateOf(flag.entityType ?: "") }
    val notes = remember { mutableStateOf(flag.notes ?: "") }
    val saving = remember { mutableStateOf(false) }
    val error = remember { mutableStateOf<String?>(null) }
    
    // Update local state when flag changes
    LaunchedEffect(flag) {
        key.value = flag.key
        description.value = flag.description
        dataRecordsEnabled.value = flag.dataRecordsEnabled
        entityType.value = flag.entityType ?: ""
        notes.value = flag.notes ?: ""
    }
    
    fun saveFlag() {
        saving.value = true
        error.value = null
        
        CoroutineScope(Dispatchers.Main).launch {
            try {
                ApiClient.updateFlagFull(
                    flagId,
                    PutFlagRequest(
                        key = key.value.takeIf { it.isNotEmpty() },
                        description = description.value.takeIf { it.isNotEmpty() },
                        dataRecordsEnabled = dataRecordsEnabled.value,
                        entityType = entityType.value.takeIf { it.isNotEmpty() },
                        notes = notes.value.takeIf { it.isNotEmpty() }
                    )
                )
                onFlagUpdated()
            } catch (e: Exception) {
                error.value = e.message ?: LocalizedStrings.failedToSaveFlag
            } finally {
                saving.value = false
            }
        }
    }
    
    fun setEnabled(enabled: Boolean) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                ApiClient.setFlagEnabled(flagId, enabled)
                onFlagUpdated()
            } catch (e: Exception) {
                error.value = e.message ?: LocalizedStrings.failedToUpdateFlag
            }
        }
    }
    
    fun addTag() {
        if (newTagValue.value.isBlank()) return
        
        CoroutineScope(Dispatchers.Main).launch {
            try {
                ApiClient.addTagToFlag(flagId, newTagValue.value)
                newTagValue.value = ""
                tagInputVisible.value = false
                onFlagUpdated()
            } catch (e: Exception) {
                error.value = e.message ?: LocalizedStrings.failedToAddTag
            }
        }
    }
    
    fun removeTag(tagId: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                ApiClient.removeTagFromFlag(flagId, tagId)
                onFlagUpdated()
            } catch (e: Exception) {
                error.value = e.message ?: LocalizedStrings.failedToRemoveTag
            }
        }
    }
    
    Div({
        style {
            padding(25.px)
            border {
                width(1.px)
                style(LineStyle.Solid)
                color(FlagentTheme.inputBorder(themeMode))
            }
            borderRadius(10.px)
            backgroundColor(FlagentTheme.cardBg(themeMode))
            property("box-shadow", "0 2px 8px ${FlagentTheme.Shadow}")
        }
    }) {
        // Header with Enable/Disable switch
        Div({
            style {
                display(DisplayStyle.Flex)
                justifyContent(JustifyContent.SpaceBetween)
                alignItems(AlignItems.Center)
                marginBottom(25.px)
                paddingBottom(20.px)
                property("border-bottom", "2px solid ${FlagentTheme.Secondary}")
            }
        }) {
            Div({
                style {
                    display(DisplayStyle.Flex)
                    alignItems(AlignItems.Center)
                    gap(12.px)
                }
            }) {
                Span({
                    style {
                        fontSize(28.px)
                    }
                }) {
                    Text("🚩")
                }
                H2({
                    style {
                        margin(0.px)
                        color(FlagentTheme.text(themeMode))
                        fontSize(24.px)
                        fontWeight("700")
                    }
                }) {
                    Text(LocalizedStrings.flagSettings)
                }
                InfoTooltip(
                    title = "Flag Settings",
                    description = "Configure the basic properties of your feature flag: description, key, enabled status, and entity type. The key is used to reference the flag in your code.",
                    details = "Entity Type defines what kind of entity this flag applies to (e.g., 'user', 'report', 'order'). Tags help organize and filter flags."
                )
            }
            Div({
                style {
                    display(DisplayStyle.Flex)
                    alignItems(AlignItems.Center)
                    gap(12.px)
                    padding(8.px, 16.px)
                    backgroundColor(if (flag.enabled) FlagentTheme.Success else FlagentTheme.Error)
                    borderRadius(20.px)
                    property("box-shadow", "0 2px 4px ${FlagentTheme.Shadow}")
                }
            }) {
                Label(attrs = {
                    style {
                        display(DisplayStyle.Flex)
                        alignItems(AlignItems.Center)
                        gap(8.px)
                        cursor("pointer")
                        color(Color.white)
                        fontWeight("600")
                    }
                }) {
                    Input(InputType.Checkbox) {
                        checked(flag.enabled)
                        onChange { event ->
                            val checked = (event.target as org.w3c.dom.HTMLInputElement).checked
                            setEnabled(checked)
                        }
                        style {
                            width(18.px)
                            height(18.px)
                            cursor("pointer")
                        }
                    }
                    Span({
                        style {
                            fontSize(16.px)
                        }
                    }) {
                        Text(if (flag.enabled) "✓" else "✗")
                    }
                    Text(if (flag.enabled) "Enabled" else "Disabled")
                }
            }
        }
        
        if (error.value != null) {
            Div({
                style {
                    color(FlagentTheme.Error)
                    padding(10.px)
                    property("background-color", "rgba(239, 68, 68, 0.1)")
                    borderRadius(5.px)
                    marginBottom(15.px)
                }
            }) {
                Text("${LocalizedStrings.error}: ${error.value}")
            }
        }
        
        // Flag ID and Save button
        Div({
            style {
                display(DisplayStyle.Flex)
                justifyContent(JustifyContent.SpaceBetween)
                alignItems(AlignItems.Center)
                marginBottom(20.px)
                padding(12.px, 16.px)
                backgroundColor(FlagentTheme.inputBg(themeMode))
                borderRadius(8.px)
            }
        }) {
            Span({
                style {
                    padding(6.px, 12.px)
                    backgroundColor(FlagentTheme.Primary)
                    color(Color.white)
                    borderRadius(6.px)
                    fontSize(13.px)
                    fontWeight("600")
                    display(DisplayStyle.Flex)
                    alignItems(AlignItems.Center)
                    gap(6.px)
                }
            }) {
                Text("🆔")
                Text("${LocalizedStrings.flagId}: $flagId")
            }
            Button({
                onClick { saveFlag() }
                if (saving.value) {
                    attr("disabled", "true")
                }
                style {
                    padding(10.px, 20.px)
                    backgroundColor(if (saving.value) FlagentTheme.NeutralLighter else FlagentTheme.Primary)
                    color(Color.white)
                    border {
                        width(0.px)
                        style(LineStyle.None)
                    }
                    borderRadius(6.px)
                    cursor(if (saving.value) "not-allowed" else "pointer")
                    fontSize(14.px)
                    fontWeight("600")
                    display(DisplayStyle.Flex)
                    alignItems(AlignItems.Center)
                    gap(8.px)
                    property("transition", "all 0.2s")
                    property("box-shadow", if (saving.value) "none" else "0 2px 4px ${FlagentTheme.Shadow}")
                }
                onMouseEnter {
                    if (!saving.value) {
                        val element = it.target as org.w3c.dom.HTMLElement
                        element.style.transform = "translateY(-1px)"
                        element.style.setProperty("box-shadow", "0 4px 6px ${FlagentTheme.ShadowHover}")
                    }
                }
                onMouseLeave {
                    if (!saving.value) {
                        val element = it.target as org.w3c.dom.HTMLElement
                        element.style.transform = "translateY(0)"
                        element.style.setProperty("box-shadow", "0 2px 4px ${FlagentTheme.Shadow}")
                    }
                }
            }) {
                Span {
                    Text(if (saving.value) "⏳" else "💾")
                }
                Text(if (saving.value) LocalizedStrings.saving else LocalizedStrings.saveFlag)
            }
        }
        
        // Key and Data Records
        Div({
            style {
                display(DisplayStyle.Flex)
                gap(15.px)
                marginBottom(15.px)
                alignItems(AlignItems.Center)
            }
        }) {
            Div({
                style {
                    flex(1)
                }
            }) {
                Label {
                    Div({
                        style {
                            marginBottom(6.px)
                            fontWeight("600")
                            fontSize(13.px)
                            color(FlagentTheme.text(themeMode))
                            display(DisplayStyle.Flex)
                            alignItems(AlignItems.Center)
                            gap(6.px)
                        }
                    }) {
                        Text("🔑")
                        Text(LocalizedStrings.flagKeyWithColon)
                        InfoTooltip(
                            title = LocalizedStrings.flagKey,
                            description = "The key is used in code to evaluate this flag (e.g. isEnabled(\"my_flag_key\")). Leave empty to auto-generate from description.",
                            details = null
                        )
                    }
                    Input(InputType.Text) {
                        value(key.value)
                        onInput { event -> key.value = event.value }
                        style {
                            width(100.percent)
                            padding(12.px, 16.px)
                            border {
                                width(1.px)
                                style(LineStyle.Solid)
                                color(FlagentTheme.inputBorder(themeMode))
                            }
                            borderRadius(6.px)
                            fontSize(14.px)
                            property("transition", "border-color 0.2s")
                        }
                        onFocus {
                            (it.target as org.w3c.dom.HTMLElement).style.borderColor = FlagentTheme.Primary.toString()
                        }
                        onBlur {
                            (it.target as org.w3c.dom.HTMLElement).style.borderColor = FlagentTheme.inputBorder(themeMode).toString()
                        }
                    }
                }
            }
            Div({
                style {
                    display(DisplayStyle.Flex)
                    alignItems(AlignItems.FlexStart)
                    gap(10.px)
                    alignSelf(AlignSelf.FlexStart)
                    flexShrink(0)
                    paddingTop(28.px)
                }
            }) {
                Label(attrs = {
                    style {
                        display(DisplayStyle.Flex)
                        alignItems(AlignItems.Center)
                        gap(5.px)
                        cursor("pointer")
                        whiteSpace("nowrap")
                    }
                }) {
                    Input(InputType.Checkbox) {
                        checked(dataRecordsEnabled.value)
                        onChange { event ->
                            dataRecordsEnabled.value = (event.target as org.w3c.dom.HTMLInputElement).checked
                        }
                        style {
                            cursor("pointer")
                            flexShrink(0)
                        }
                    }
                    Text(LocalizedStrings.dataRecords)
                }
            }
        }
        
        // Description and Entity Type
        Div({
            style {
                display(DisplayStyle.Flex)
                gap(15.px)
                marginBottom(15.px)
                alignItems(AlignItems.FlexStart)
                flexWrap(FlexWrap.Wrap)
            }
        }) {
            Div({
                style {
                    flex(1)
                }
            }) {
                Label {
                    Div({
                        style {
                            marginBottom(6.px)
                            fontWeight("600")
                            fontSize(13.px)
                            color(FlagentTheme.text(themeMode))
                            display(DisplayStyle.Flex)
                            alignItems(AlignItems.Center)
                            gap(6.px)
                        }
                    }) {
                        Icon(
                            name = "description",
                            size = 16.px,
                            color = FlagentTheme.Text
                        )
                        Text(LocalizedStrings.flagDescriptionWithColon)
                    }
                    Input(InputType.Text) {
                        value(description.value)
                        onInput { event -> description.value = event.value }
                        style {
                            width(100.percent)
                            padding(12.px, 16.px)
                            border {
                                width(1.px)
                                style(LineStyle.Solid)
                                color(FlagentTheme.inputBorder(themeMode))
                            }
                            borderRadius(6.px)
                            fontSize(14.px)
                            property("transition", "border-color 0.2s")
                        }
                        onFocus {
                            (it.target as org.w3c.dom.HTMLElement).style.borderColor = FlagentTheme.Primary.toString()
                        }
                        onBlur {
                            (it.target as org.w3c.dom.HTMLElement).style.borderColor = FlagentTheme.inputBorder(themeMode).toString()
                        }
                    }
                }
            }
            if (dataRecordsEnabled.value) {
                Div({
                    style {
                        display(DisplayStyle.Flex)
                        flexDirection(FlexDirection.Column)
                        gap(5.px)
                        flexShrink(0)
                        minWidth(200.px)
                        position(Position.Relative)
                        property("z-index", "1")
                    }
                }) {
                    Label {
                        Div({
                            style {
                                marginBottom(6.px)
                                fontWeight("600")
                                fontSize(13.px)
                                color(FlagentTheme.text(themeMode))
                                display(DisplayStyle.Flex)
                                alignItems(AlignItems.Center)
                                gap(6.px)
                            }
                        }) {
                            Icon(
                                name = "category",
                                size = 16.px,
                                color = FlagentTheme.Text
                            )
                            Text(LocalizedStrings.entityType)
                            InfoTooltip(
                                title = LocalizedStrings.entityType,
                                description = "Entity type defines what kind of entity this flag applies to (e.g. user, report, order). Used for evaluation context.",
                                details = null
                            )
                        }
                        Div({
                            style {
                                position(Position.Relative)
                            }
                        }) {
                            Input(InputType.Text) {
                                value(entityType.value)
                                onInput { event -> entityType.value = event.value }
                                attr("list", "entity-type-suggestions")
                                attr("placeholder", LocalizedStrings.enterOrSelectEntityTypePlaceholder)
                                style {
                                    width(100.percent)
                                    padding(12.px, 16.px)
                                    border {
                                        width(1.px)
                                        style(LineStyle.Solid)
                                        color(FlagentTheme.inputBorder(themeMode))
                                    }
                                    borderRadius(6.px)
                                    fontSize(14.px)
                                    property("transition", "border-color 0.2s")
                                }
                                onFocus {
                                    (it.target as org.w3c.dom.HTMLElement).style.borderColor = FlagentTheme.Primary.toString()
                                }
                                onBlur {
                                    (it.target as org.w3c.dom.HTMLElement).style.borderColor = FlagentTheme.inputBorder(themeMode).toString()
                                }
                            }
                            // Autocomplete suggestions
                            Datalist({
                                id("entity-type-suggestions")
                            }) {
                                Option(value = "")
                                entityTypes.forEach { et ->
                                    Option(value = et)
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Notes with Markdown Editor
        Div({
            style {
                marginBottom(15.px)
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
                H5({
                    style {
                        margin(0.px)
                    }
                }) {
                    Text(LocalizedStrings.flagNotes)
                }
                Button({
                    onClick { showMdEditor.value = !showMdEditor.value }
                    style {
                        padding(5.px, 10.px)
                        backgroundColor(FlagentTheme.Neutral)
                        color(Color.white)
                        border {
                            width(0.px)
                            style(LineStyle.None)
                        }
                        borderRadius(3.px)
                        cursor("pointer")
                        fontSize(12.px)
                    }
                }) {
                    Text(if (showMdEditor.value) "view" else "edit")
                }
            }
            if (showMdEditor.value || notes.value.isNotEmpty()) {
                MarkdownEditor(
                    value = notes.value,
                    onValueChange = { notes.value = it },
                    placeholder = "Enter markdown notes...",
                    showPreview = showMdEditor.value
                )
            }
        }
        
        // Tags
        Div({
            style {
                marginBottom(15.px)
            }
        }) {
            H5({
                style {
                    margin(0.px, 0.px, 10.px, 0.px)
                    display(DisplayStyle.Flex)
                    alignItems(AlignItems.Center)
                    gap(6.px)
                }
            }) {
                Text(LocalizedStrings.tags)
                InfoTooltip(
                    title = LocalizedStrings.tags,
                    description = "Tags help organize and filter flags (e.g. by team, feature area). Used for search and grouping.",
                    details = null
                )
            }
            Div({
                style {
                    display(DisplayStyle.Flex)
                    flexWrap(FlexWrap.Wrap)
                    gap(5.px)
                    marginBottom(10.px)
                }
            }) {
                flag.tags.forEach { tag ->
                    Span({
                        style {
                            display(DisplayStyle.InlineBlock)
                            padding(5.px, 10.px)
                            backgroundColor(FlagentTheme.Accent)
                            color(Color.white)
                            borderRadius(3.px)
                            fontSize(12.px)
                            display(DisplayStyle.Flex)
                            alignItems(AlignItems.Center)
                            gap(5.px)
                        }
                    }) {
                        Text(tag.value)
                        Button({
                            onClick { removeTag(tag.id) }
                            style {
                                padding(0.px)
                                property("background-color", "transparent")
                                color(Color.white)
                                border {
                                    width(0.px)
                                    style(LineStyle.None)
                                }
                                cursor("pointer")
                                fontSize(14.px)
                            }
                        }) {
                            Text("×")
                        }
                    }
                }
            }
            if (tagInputVisible.value) {
                Div({
                    style {
                        display(DisplayStyle.Flex)
                        flexDirection(FlexDirection.Column)
                        gap(5.px)
                    }
                }) {
                    Div({
                        style {
                            position(Position.Relative)
                        }
                    }) {
                        Input(InputType.Text) {
                            value(newTagValue.value)
                            onInput { event -> newTagValue.value = event.value }
                            onKeyDown { event ->
                                if (event.key == "Enter") {
                                    addTag()
                                } else if (event.key == "Escape") {
                                    tagInputVisible.value = false
                                    newTagValue.value = ""
                                }
                            }
                            attr("list", "tag-suggestions")
                            style {
                                flex(1)
                                width(100.percent)
                                padding(5.px)
                                border {
                                    width(1.px)
                                    style(LineStyle.Solid)
                                    color(FlagentTheme.inputBorder(themeMode))
                                }
                                borderRadius(3.px)
                            }
                        }
                        // Autocomplete suggestions
                        Datalist({
                            id("tag-suggestions")
                        }) {
                            allTags.forEach { tag ->
                                Option(value = tag.value)
                            }
                        }
                    }
                    Div({
                        style {
                            display(DisplayStyle.Flex)
                            gap(5.px)
                        }
                    }) {
                        Button({
                            onClick { addTag() }
                            style {
                                padding(5.px, 10.px)
                                backgroundColor(FlagentTheme.Success)
                                color(Color.white)
                                border {
                                    width(0.px)
                                    style(LineStyle.None)
                                }
                                borderRadius(3.px)
                                cursor("pointer")
                            }
                        }) {
                            Text(LocalizedStrings.add)
                        }
                        Button({
                            onClick {
                                tagInputVisible.value = false
                                newTagValue.value = ""
                            }
                            style {
                                padding(5.px, 10.px)
                                backgroundColor(FlagentTheme.Neutral)
                                color(Color.white)
                                border {
                                    width(0.px)
                                    style(LineStyle.None)
                                }
                                borderRadius(3.px)
                                cursor("pointer")
                            }
                        }) {
                            Text(LocalizedStrings.cancel)
                        }
                    }
                }
            } else {
                Button({
                    onClick { tagInputVisible.value = true }
                    style {
                        padding(5.px, 10.px)
                        backgroundColor(FlagentTheme.Neutral)
                        color(Color.white)
                        border {
                            width(0.px)
                            style(LineStyle.None)
                        }
                        borderRadius(3.px)
                        cursor("pointer")
                        fontSize(12.px)
                    }
                }) {
                    Text(LocalizedStrings.newTag)
                }
            }
        }
    }
}

/**
 * Variants Section - manage flag variants
 */
@Composable
private fun VariantsSection(
    flagId: Int,
    variants: List<flagent.api.model.VariantResponse>,
    onUpdate: () -> Unit
) {
    val themeMode = LocalThemeMode.current
    val newVariantKey = remember { mutableStateOf("") }
    val creating = remember { mutableStateOf(false) }
    val error = remember { mutableStateOf<String?>(null) }
    
    fun createVariant() {
        if (newVariantKey.value.isBlank()) return
        
        creating.value = true
        error.value = null
        
        CoroutineScope(Dispatchers.Main).launch {
            try {
                ApiClient.createVariant(flagId, flagent.api.model.CreateVariantRequest(key = newVariantKey.value))
                newVariantKey.value = ""
                onUpdate()
            } catch (e: Exception) {
                error.value = e.message ?: LocalizedStrings.failedToCreateVariant
            } finally {
                creating.value = false
            }
        }
    }
    
    Div({
        style {
            padding(25.px)
            border {
                width(1.px)
                style(LineStyle.Solid)
                color(FlagentTheme.inputBorder(themeMode))
            }
            borderRadius(10.px)
            backgroundColor(FlagentTheme.cardBg(themeMode))
            property("box-shadow", "0 2px 8px ${FlagentTheme.Shadow}")
        }
    }) {
        Div({
            style {
                display(DisplayStyle.Flex)
                alignItems(AlignItems.Center)
                gap(12.px)
                marginBottom(20.px)
                paddingBottom(15.px)
                property("border-bottom", "2px solid ${FlagentTheme.Secondary}")
            }
        }) {
            Icon(
                name = "adjust",
                size = 28.px,
                color = FlagentTheme.Primary
            )
            H2({
                style {
                    margin(0.px)
                    color(FlagentTheme.text(themeMode))
                    fontSize(22.px)
                    fontWeight("700")
                }
            }) {
                Text(LocalizedStrings.variants)
            }
            InfoTooltip(
                title = "Variants",
                description = "Variants are the different values your feature flag can return. For boolean flags, you typically have 'true' and 'false'. For multi-variant flags, you can create multiple variants with different values.",
                details = "Each variant can have an attachment (JSON data) that is returned when that variant is selected. Variants are assigned to segments through distributions."
            )
        }
        
        if (error.value != null) {
            Div({
                style {
                    color(FlagentTheme.Error)
                    padding(10.px)
                    property("background-color", "rgba(239, 68, 68, 0.1)")
                    borderRadius(5.px)
                    marginBottom(15.px)
                }
            }) {
                Text("${LocalizedStrings.error}: ${error.value}")
            }
        }
        
        if (variants.isEmpty()) {
            Div({
                style {
                    padding(20.px)
                    backgroundColor(FlagentTheme.inputBg(themeMode))
                    borderRadius(5.px)
                    textAlign("center")
                    color(FlagentTheme.textLight(themeMode))
                    marginBottom(15.px)
                }
            }) {
                Text(LocalizedStrings.noVariantsYet)
            }
        } else {
            variants.forEach { variant ->
                VariantCard(flagId, variant, onUpdate)
            }
        }
        
        // Create new variant
        Div({
            style {
                marginTop(25.px)
                padding(20.px)
                backgroundColor(FlagentTheme.inputBg(themeMode))
                borderRadius(8.px)
                border {
                    width(2.px)
                    style(LineStyle.Dashed)
                    color(FlagentTheme.inputBorder(themeMode))
                }
            }
        }) {
            Div({
                style {
                    display(DisplayStyle.Flex)
                    gap(12.px)
                    alignItems(AlignItems.Center)
                }
            }) {
                Input(InputType.Text) {
                    attr("placeholder", LocalizedStrings.enterVariantKeyPlaceholder)
                    value(newVariantKey.value)
                    onInput { event -> newVariantKey.value = event.value }
                    onKeyDown { event ->
                        if (event.key == "Enter") {
                            createVariant()
                        }
                    }
                    style {
                        flex(1)
                        padding(12.px, 16.px)
                        border {
                            width(1.px)
                            style(LineStyle.Solid)
                            color(FlagentTheme.inputBorder(themeMode))
                        }
                        borderRadius(6.px)
                        fontSize(14.px)
                        property("transition", "border-color 0.2s")
                    }
                    onFocus {
                        (it.target as org.w3c.dom.HTMLElement).style.borderColor = FlagentTheme.Primary.toString()
                    }
                    onBlur {
                        (it.target as org.w3c.dom.HTMLElement).style.borderColor = FlagentTheme.inputBorder(themeMode).toString()
                    }
                }
                Button({
                    onClick { createVariant() }
                    if (creating.value || newVariantKey.value.isBlank()) {
                        attr("disabled", "true")
                    }
                    style {
                        padding(12.px, 20.px)
                        backgroundColor(if (creating.value || newVariantKey.value.isBlank()) FlagentTheme.NeutralLighter else FlagentTheme.Success)
                        color(Color.white)
                        border {
                            width(0.px)
                            style(LineStyle.None)
                        }
                        borderRadius(6.px)
                        cursor(if (creating.value || newVariantKey.value.isBlank()) "not-allowed" else "pointer")
                        fontWeight("600")
                        fontSize(14.px)
                        display(DisplayStyle.Flex)
                        alignItems(AlignItems.Center)
                        gap(8.px)
                        property("transition", "all 0.2s")
                        property("box-shadow", if (creating.value || newVariantKey.value.isBlank()) "none" else "0 2px 4px ${FlagentTheme.Shadow}")
                    }
                    onMouseEnter {
                        if (!creating.value && newVariantKey.value.isNotBlank()) {
                            val element = it.target as org.w3c.dom.HTMLElement
                            element.style.transform = "translateY(-1px)"
                            element.style.setProperty("box-shadow", "0 4px 6px ${FlagentTheme.ShadowHover}")
                        }
                    }
                    onMouseLeave {
                        if (!creating.value && newVariantKey.value.isNotBlank()) {
                            val element = it.target as org.w3c.dom.HTMLElement
                            element.style.transform = "translateY(0)"
                            element.style.setProperty("box-shadow", "0 2px 4px ${FlagentTheme.Shadow}")
                        }
                    }
                }) {
                    Icon(
                        name = if (creating.value) "hourglass_empty" else "add_circle",
                        size = 18.px,
                        color = FlagentTheme.Background
                    )
                    Text(if (creating.value) LocalizedStrings.creating else LocalizedStrings.createVariant)
                }
            }
        }
    }
}

/**
 * Variant Card - individual variant editor
 */
@Composable
private fun VariantCard(
    flagId: Int,
    variant: flagent.api.model.VariantResponse,
    onUpdate: () -> Unit
) {
    val themeMode = LocalThemeMode.current
    val key = remember { mutableStateOf(variant.key) }
    val attachmentJson = remember { mutableStateOf(
        variant.attachment?.entries?.joinToString(",\n") { "\"${it.key}\": \"${it.value}\"" } ?: ""
    ) }
    val saving = remember { mutableStateOf(false) }
    val deleting = remember { mutableStateOf(false) }
    val error = remember { mutableStateOf<String?>(null) }
    val showAttachment = remember { mutableStateOf(false) }
    val attachmentValid = remember { mutableStateOf(true) }
    
    LaunchedEffect(variant) {
        key.value = variant.key
        attachmentJson.value = variant.attachment?.entries?.joinToString(",\n") { "\"${it.key}\": \"${it.value}\"" } ?: ""
        attachmentValid.value = true
    }
    
    fun validateAndSaveAttachment(): Map<String, String>? {
        if (attachmentJson.value.isBlank()) {
            return null
        }
        
        return try {
            val jsonStr = "{${attachmentJson.value}}"
            val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
            (json.parseToJsonElement(jsonStr) as? kotlinx.serialization.json.JsonObject)
                ?.entries?.associate { (key, value) -> key to value.toString().trim('"') }
        } catch (e: Exception) {
            null
        }
    }
    
    fun saveVariant() {
        saving.value = true
        error.value = null
        
        // Parse attachment JSON
        val attachment = validateAndSaveAttachment()
        if (attachmentJson.value.isNotBlank() && attachment == null) {
            error.value = "Invalid JSON in attachment. Please fix the JSON syntax."
            saving.value = false
            attachmentValid.value = false
            return
        }
        
        attachmentValid.value = true
        
        CoroutineScope(Dispatchers.Main).launch {
            try {
                ApiClient.updateVariant(
                    flagId,
                    variant.id,
                    flagent.api.model.PutVariantRequest(
                        key = key.value,
                        attachment = attachment
                    )
                )
                onUpdate()
            } catch (e: Exception) {
                error.value = e.message ?: LocalizedStrings.failedToUpdateVariant
            } finally {
                saving.value = false
            }
        }
    }
    
    fun deleteVariant() {
        if (!kotlinx.browser.window.confirm(LocalizedStrings.confirmDeleteVariant(variant.id, variant.key))) {
            return
        }
        
        deleting.value = true
        error.value = null
        
        CoroutineScope(Dispatchers.Main).launch {
            try {
                ApiClient.deleteVariant(flagId, variant.id)
                onUpdate()
            } catch (e: Exception) {
                val errorMsg = e.message ?: LocalizedStrings.failedToDeleteVariant
                error.value = if (errorMsg.contains("being used", ignoreCase = true)) {
                    "This variant is being used by a segment distribution. Please remove the segment or edit the distribution in order to remove this variant."
                } else {
                    errorMsg
                }
            } finally {
                deleting.value = false
            }
        }
    }
    
    Div({
        style {
            padding(20.px)
            border {
                width(1.px)
                style(LineStyle.Solid)
                color(FlagentTheme.inputBorder(themeMode))
            }
            borderRadius(8.px)
            marginBottom(20.px)
            backgroundColor(FlagentTheme.inputBg(themeMode))
            property("box-shadow", "0 1px 3px ${FlagentTheme.Shadow}")
            property("transition", "all 0.2s")
        }
        onMouseEnter {
            val element = it.target as org.w3c.dom.HTMLElement
            element.style.setProperty("box-shadow", "0 2px 6px ${FlagentTheme.ShadowHover}")
            element.style.transform = "translateY(-2px)"
        }
        onMouseLeave {
            val element = it.target as org.w3c.dom.HTMLElement
            element.style.setProperty("box-shadow", "0 1px 3px ${FlagentTheme.Shadow}")
            element.style.transform = "translateY(0)"
        }
    }) {
        // Variant header
        Div({
            style {
                display(DisplayStyle.Flex)
                justifyContent(JustifyContent.SpaceBetween)
                alignItems(AlignItems.Center)
                marginBottom(15.px)
            }
        }) {
            Span({
                style {
                    padding(5.px, 10.px)
                    backgroundColor(FlagentTheme.Primary)
                    color(Color.white)
                    borderRadius(3.px)
                    fontSize(12.px)
                }
            }) {
                Text("${LocalizedStrings.variantId}: ${variant.id}")
            }
            Div({
                style {
                    display(DisplayStyle.Flex)
                    gap(5.px)
                }
            }) {
                Button({
                    onClick { saveVariant() }
                    if (saving.value || !attachmentValid.value) {
                        attr("disabled", "true")
                    }
                    style {
                        padding(5.px, 10.px)
                        backgroundColor(if (saving.value || !attachmentValid.value) FlagentTheme.NeutralLighter else FlagentTheme.Success)
                        color(Color.white)
                        border {
                            width(0.px)
                            style(LineStyle.None)
                        }
                        borderRadius(3.px)
                        cursor(if (saving.value || !attachmentValid.value) "not-allowed" else "pointer")
                        fontSize(12.px)
                    }
                }) {
                    Text(if (saving.value) LocalizedStrings.saving else LocalizedStrings.saveVariant)
                }
                Button({
                    onClick { deleteVariant() }
                    if (deleting.value) {
                        attr("disabled", "true")
                    }
                    style {
                        padding(5.px, 10.px)
                        backgroundColor(if (deleting.value) FlagentTheme.NeutralLighter else FlagentTheme.Error)
                        color(Color.white)
                        border {
                            width(0.px)
                            style(LineStyle.None)
                        }
                        borderRadius(3.px)
                        cursor(if (deleting.value) "not-allowed" else "pointer")
                        fontSize(12.px)
                    }
                }) {
                    Text(if (deleting.value) LocalizedStrings.deleting else LocalizedStrings.delete)
                }
            }
        }
        
        if (error.value != null) {
            Div({
                style {
                    color(FlagentTheme.Error)
                    padding(10.px)
                    property("background-color", "rgba(239, 68, 68, 0.1)")
                    borderRadius(5.px)
                    marginBottom(15.px)
                }
            }) {
                Text("${LocalizedStrings.error}: ${error.value}")
            }
        }
        
        // Key input
        Label {
            Text(LocalizedStrings.keyWithColon)
            Input(InputType.Text) {
                value(key.value)
                onInput { event -> key.value = event.value }
                style {
                    width(100.percent)
                    padding(5.px)
                    border {
                        width(1.px)
                        style(LineStyle.Solid)
                        color(FlagentTheme.inputBorder(themeMode))
                    }
                    borderRadius(3.px)
                }
            }
        }
        
        // Attachment (collapsible)
        Div({
            style {
                marginTop(15.px)
            }
        }) {
            Button({
                onClick { showAttachment.value = !showAttachment.value }
                style {
                    padding(5.px, 10.px)
                    backgroundColor(FlagentTheme.Neutral)
                    color(Color.white)
                    border {
                        width(0.px)
                        style(LineStyle.None)
                    }
                    borderRadius(3.px)
                    cursor("pointer")
                    fontSize(12.px)
                }
            }) {
                Text(if (showAttachment.value) "Hide Attachment" else "Show Attachment")
            }
            
            if (showAttachment.value) {
                Div({
                    style {
                        marginTop(10.px)
                    }
                }) {
                    JsonEditor(
                        value = attachmentJson.value,
                        onValueChange = { newValue ->
                            attachmentJson.value = newValue
                            // Validate JSON
                            val attachment = validateAndSaveAttachment()
                            attachmentValid.value = attachmentJson.value.isBlank() || attachment != null
                        },
                        placeholder = "\"key1\": \"value1\",\n\"key2\": \"value2\"",
                        minHeight = 150.px,
                        showValidation = true
                    )
                }
            }
        }
    }
}

/**
 * Segments Section - manage flag segments
 */
@Composable
private fun SegmentsSection(
    flagId: Int,
    segments: List<flagent.api.model.SegmentResponse>,
    variants: List<flagent.api.model.VariantResponse>,
    onUpdate: () -> Unit
) {
    val themeMode = LocalThemeMode.current
    val newSegmentDescription = remember { mutableStateOf("") }
    val newSegmentRollout = remember { mutableStateOf(50) }
    val creating = remember { mutableStateOf(false) }
    val reordering = remember { mutableStateOf(false) }
    val error = remember { mutableStateOf<String?>(null) }
    val reorderedSegments = remember { mutableStateListOf<flagent.api.model.SegmentResponse>() }
    val showCreateSegmentModal = remember { mutableStateOf(false) }
    val sortableInstance = remember { mutableStateOf<SortableInstance?>(null) }
    val segmentsContainerId = remember { "segments-container-${flagId}" }
    
    // Define reorderSegments function before it's used
    fun reorderSegments() {
        reordering.value = true
        error.value = null
        
        CoroutineScope(Dispatchers.Main).launch {
            try {
                ApiClient.reorderSegments(
                    flagId,
                    flagent.api.model.PutSegmentReorderRequest(
                        segmentIDs = reorderedSegments.map { it.id }
                    )
                )
                onUpdate()
            } catch (e: Exception) {
                error.value = e.message ?: LocalizedStrings.failedToReorderSegments
            } finally {
                reordering.value = false
            }
        }
    }
    
    // Initialize reordered segments
    LaunchedEffect(segments) {
        reorderedSegments.clear()
        reorderedSegments.addAll(segments)
    }
    
    // Initialize SortableJS when segments container is ready
    LaunchedEffect(reorderedSegments.size, segmentsContainerId) {
        if (reorderedSegments.isNotEmpty()) {
            // Use setTimeout to ensure DOM is ready
            kotlinx.browser.window.setTimeout({
                val container = document.getElementById(segmentsContainerId)
                if (container != null && sortableInstance.value == null) {
                    sortableInstance.value = createSortable(
                        element = container,
                        onUpdate = { oldIndex, newIndex ->
                            if (oldIndex != newIndex && oldIndex >= 0 && newIndex >= 0) {
                                val item = reorderedSegments.removeAt(oldIndex)
                                reorderedSegments.add(newIndex, item)
                                // Auto-save new order
                                reorderSegments()
                            }
                        },
                        handle = ".segment-drag-handle",
                        ghostClass = "sortable-ghost",
                        chosenClass = "sortable-chosen",
                        dragClass = "sortable-drag"
                    )
                }
            }, 100)
        }
    }
    
    fun createSegment() {
        if (newSegmentDescription.value.isBlank()) return
        
        creating.value = true
        error.value = null
        
        CoroutineScope(Dispatchers.Main).launch {
            try {
                ApiClient.createSegment(
                    flagId,
                    flagent.api.model.CreateSegmentRequest(
                        description = newSegmentDescription.value,
                        rolloutPercent = newSegmentRollout.value
                    )
                )
                newSegmentDescription.value = ""
                newSegmentRollout.value = 50
                showCreateSegmentModal.value = false
                onUpdate()
            } catch (e: Exception) {
                error.value = e.message ?: LocalizedStrings.failedToCreateSegment
            } finally {
                creating.value = false
            }
        }
    }
    
    
    Div({
        style {
            padding(25.px)
            border {
                width(1.px)
                style(LineStyle.Solid)
                color(FlagentTheme.inputBorder(themeMode))
            }
            borderRadius(10.px)
            backgroundColor(FlagentTheme.cardBg(themeMode))
            property("box-shadow", "0 2px 8px ${FlagentTheme.Shadow}")
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
                property("border-bottom", "2px solid ${FlagentTheme.Secondary}")
            }
        }) {
            Div({
                style {
                    display(DisplayStyle.Flex)
                    alignItems(AlignItems.Center)
                    gap(12.px)
                }
            }) {
                Icon(
                    name = "account_tree",
                    size = 28.px,
                    color = FlagentTheme.Primary
                )
                H2({
                    style {
                        margin(0.px)
                        color(FlagentTheme.text(themeMode))
                        fontSize(22.px)
                        fontWeight("700")
                    }
                }) {
                    Text(LocalizedStrings.segments)
                }
                InfoTooltip(
                    title = "Segments",
                    description = "Segments define groups of entities that should receive specific variant assignments. Each segment can have constraints (targeting rules) and distributions (which variants to assign).",
                    details = "Segments are evaluated in order. The first segment that matches an entity's context will determine which variant is assigned. Use constraints to target specific users, regions, or other criteria."
                )
            }
            Div({
                style {
                    display(DisplayStyle.Flex)
                    gap(10.px)
                }
            }) {
                Button({
                    onClick { reorderSegments() }
                    if (reordering.value) {
                        attr("disabled", "true")
                    }
                    style {
                        padding(5.px, 10.px)
                        backgroundColor(if (reordering.value) FlagentTheme.NeutralLighter else FlagentTheme.Neutral)
                        color(Color.white)
                        border {
                            width(0.px)
                            style(LineStyle.None)
                        }
                        borderRadius(3.px)
                        cursor(if (reordering.value) "not-allowed" else "pointer")
                        fontSize(12.px)
                    }
                }) {
                    Text(if (reordering.value) "Reordering..." else "Reorder")
                }
                Button({
                    onClick { 
                        newSegmentDescription.value = ""
                        newSegmentRollout.value = 50
                        error.value = null
                        showCreateSegmentModal.value = true 
                    }
                    style {
                        padding(5.px, 10.px)
                        backgroundColor(FlagentTheme.Success)
                        color(Color.white)
                        border {
                            width(0.px)
                            style(LineStyle.None)
                        }
                        borderRadius(3.px)
                        cursor("pointer")
                        fontSize(12.px)
                    }
                }) {
                    Text(LocalizedStrings.newSegment)
                }
            }
        }
        
        if (error.value != null) {
            Div({
                style {
                    color(FlagentTheme.Error)
                    padding(10.px)
                    property("background-color", "rgba(239, 68, 68, 0.1)")
                    borderRadius(5.px)
                    marginBottom(15.px)
                }
            }) {
                Text("${LocalizedStrings.error}: ${error.value}")
            }
        }
        
        if (reorderedSegments.isEmpty()) {
            Div({
                style {
                    padding(20.px)
                    backgroundColor(FlagentTheme.inputBg(themeMode))
                    borderRadius(5.px)
                    textAlign("center")
                    color(FlagentTheme.textLight(themeMode))
                    marginBottom(15.px)
                }
            }) {
                Text(LocalizedStrings.noSegmentsYet)
            }
        } else {
            Div({
                id(segmentsContainerId)
                style {
                    display(DisplayStyle.Flex)
                    flexDirection(FlexDirection.Column)
                    gap(15.px)
                }
            }) {
                reorderedSegments.forEach { segment ->
                    SegmentCard(
                        flagId = flagId,
                        segment = segment,
                        variants = variants,
                        onUpdate = onUpdate
                    )
                }
            }
        }
        
        // Create Segment Modal
        if (showCreateSegmentModal.value) {
            Modal(
                title = LocalizedStrings.createSegment,
                onClose = { 
                    showCreateSegmentModal.value = false
                    newSegmentDescription.value = ""
                    newSegmentRollout.value = 50
                    error.value = null
                },
                onConfirm = { createSegment() },
                confirmText = LocalizedStrings.create,
                cancelText = LocalizedStrings.cancel,
                showCancel = true,
                confirmDisabled = creating.value || newSegmentDescription.value.isBlank(),
                confirmLoading = creating.value
            ) {
                if (error.value != null) {
                    Div({
                        style {
                            color(FlagentTheme.Error)
                            padding(10.px)
                            property("background-color", "rgba(239, 68, 68, 0.1)")
                            borderRadius(5.px)
                            marginBottom(15.px)
                        }
                    }) {
                        Text("${LocalizedStrings.error}: ${error.value}")
                    }
                }
                
                Div({
                    style {
                        display(DisplayStyle.Flex)
                        flexDirection(FlexDirection.Column)
                        gap(15.px)
                    }
                }) {
                    Div({
                        style {
                            display(DisplayStyle.Flex)
                            flexDirection(FlexDirection.Column)
                            gap(5.px)
                        }
                    }) {
                        Label {
                            Text("${LocalizedStrings.description}:")
                        }
                        Input(InputType.Text) {
                            attr("placeholder", LocalizedStrings.enterSegmentDescriptionPlaceholder)
                            value(newSegmentDescription.value)
                            onInput { event -> newSegmentDescription.value = event.value }
                            style {
                                width(100.percent)
                                padding(8.px)
                                border {
                                    width(1.px)
                                    style(LineStyle.Solid)
                                    color(FlagentTheme.inputBorder(themeMode))
                                }
                                borderRadius(5.px)
                            }
                        }
                    }
                    
                    Div({
                        style {
                            display(DisplayStyle.Flex)
                            flexDirection(FlexDirection.Column)
                            gap(5.px)
                        }
                    }) {
                        Label {
                            Text("${LocalizedStrings.rolloutPercent}: ${newSegmentRollout.value}%")
                        }
                        Input(InputType.Range) {
                            value(newSegmentRollout.value.toString())
                            onInput { event ->
                                newSegmentRollout.value = (event.target as org.w3c.dom.HTMLInputElement).value.toIntOrNull() ?: 50
                            }
                            attr("min", "0")
                            attr("max", "100")
                            style {
                                width(100.percent)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Segment Card - individual segment editor
 */
@Composable
private fun SegmentCard(
    flagId: Int,
    segment: flagent.api.model.SegmentResponse,
    variants: List<flagent.api.model.VariantResponse>,
    onUpdate: () -> Unit
) {
    val themeMode = LocalThemeMode.current
    val description = remember { mutableStateOf(segment.description ?: "") }
    val rolloutPercent = remember { mutableStateOf(segment.rolloutPercent) }
    val saving = remember { mutableStateOf(false) }
    val deleting = remember { mutableStateOf(false) }
    val error = remember { mutableStateOf<String?>(null) }
    val showDistributionDialog = remember { mutableStateOf(false) }
    
    LaunchedEffect(segment) {
        description.value = segment.description ?: ""
        rolloutPercent.value = segment.rolloutPercent
    }
    
    fun saveSegment() {
        saving.value = true
        error.value = null
        
        CoroutineScope(Dispatchers.Main).launch {
            try {
                ApiClient.updateSegment(
                    flagId,
                    segment.id,
                    flagent.api.model.PutSegmentRequest(
                        description = description.value,
                        rolloutPercent = rolloutPercent.value
                    )
                )
                onUpdate()
            } catch (e: Exception) {
                error.value = e.message ?: LocalizedStrings.failedToSaveSegment
            } finally {
                saving.value = false
            }
        }
    }
    
    fun deleteSegment() {
        if (!kotlinx.browser.window.confirm(LocalizedStrings.confirmDeleteSegment)) {
            return
        }
        
        deleting.value = true
        error.value = null
        
        CoroutineScope(Dispatchers.Main).launch {
            try {
                ApiClient.deleteSegment(flagId, segment.id)
                onUpdate()
            } catch (e: Exception) {
                error.value = e.message ?: LocalizedStrings.failedToDeleteSegment
            } finally {
                deleting.value = false
            }
        }
    }
    
    Div({
        style {
            padding(15.px)
            border {
                width(1.px)
                style(LineStyle.Solid)
                color(FlagentTheme.inputBorder(themeMode))
            }
            borderRadius(5.px)
            backgroundColor(FlagentTheme.inputBg(themeMode))
            property("transition", "all 0.2s ease")
        }
    }) {
        // Drag handle
        Div({
            classes("segment-drag-handle")
            style {
                display(DisplayStyle.Flex)
                alignItems(AlignItems.Center)
                gap(5.px)
                marginBottom(10.px)
                color(FlagentTheme.textLight(themeMode))
                fontSize(12.px)
                cursor("grab")
                property("user-select", "none")
            }
        }) {
            Span {
                Text("☰")
            }
            Span {
                Text(LocalizedStrings.dragToReorder)
            }
        }
        
        // Segment header
        Div({
            style {
                display(DisplayStyle.Flex)
                justifyContent(JustifyContent.SpaceBetween)
                alignItems(AlignItems.Center)
                marginBottom(15.px)
            }
        }) {
            Span({
                style {
                    padding(5.px, 10.px)
                    backgroundColor(FlagentTheme.Primary)
                    color(Color.white)
                    borderRadius(3.px)
                    fontSize(12.px)
                }
            }) {
                Text("${LocalizedStrings.segmentId}: ${segment.id}")
            }
            Div({
                style {
                    display(DisplayStyle.Flex)
                    gap(5.px)
                }
            }) {
                Button({
                    onClick { saveSegment() }
                    if (saving.value) {
                        attr("disabled", "true")
                    }
                    style {
                        padding(5.px, 10.px)
                        backgroundColor(if (saving.value) FlagentTheme.NeutralLighter else FlagentTheme.Success)
                        color(Color.white)
                        border {
                            width(0.px)
                            style(LineStyle.None)
                        }
                        borderRadius(3.px)
                        cursor(if (saving.value) "not-allowed" else "pointer")
                        fontSize(12.px)
                    }
                }) {
                    Text(if (saving.value) LocalizedStrings.saving else LocalizedStrings.saveSegmentSetting)
                }
                Button({
                    onClick { deleteSegment() }
                    if (deleting.value) {
                        attr("disabled", "true")
                    }
                    style {
                        padding(5.px, 10.px)
                        backgroundColor(if (deleting.value) FlagentTheme.NeutralLighter else FlagentTheme.Error)
                        color(Color.white)
                        border {
                            width(0.px)
                            style(LineStyle.None)
                        }
                        borderRadius(3.px)
                        cursor(if (deleting.value) "not-allowed" else "pointer")
                        fontSize(12.px)
                    }
                }) {
                    Text(if (deleting.value) LocalizedStrings.deleting else LocalizedStrings.delete)
                }
            }
        }
        
        if (error.value != null) {
            Div({
                style {
                    color(FlagentTheme.Error)
                    padding(10.px)
                    property("background-color", "rgba(239, 68, 68, 0.1)")
                    borderRadius(5.px)
                    marginBottom(15.px)
                }
            }) {
                Text("${LocalizedStrings.error}: ${error.value}")
            }
        }
        
        // Description and Rollout
        Div({
            style {
                display(DisplayStyle.Flex)
                gap(15.px)
                marginBottom(15.px)
            }
        }) {
            Div({
                style {
                    flex(1)
                }
            }) {
                Label {
                    Text("${LocalizedStrings.description}:")
                    Input(InputType.Text) {
                        value(description.value)
                        onInput { event -> description.value = event.value }
                        style {
                            width(100.percent)
                            padding(5.px)
                            border {
                                width(1.px)
                                style(LineStyle.Solid)
                                color(FlagentTheme.inputBorder(themeMode))
                            }
                            borderRadius(3.px)
                        }
                    }
                }
            }
            Div({
                style {
                    width(200.px)
                }
            }) {
                Label {
                    Span({
                        style {
                            display(DisplayStyle.Flex)
                            alignItems(AlignItems.Center)
                            gap(6.px)
                        }
                    }) {
                        Text("${LocalizedStrings.rollout}: ${rolloutPercent.value}%")
                        InfoTooltip(
                            title = LocalizedStrings.rolloutPercent,
                            description = "Percentage of matching entities that will be assigned a variant from this segment. Remaining share gets default/fallback.",
                            details = null
                        )
                    }
                    Input(InputType.Number) {
                        value(rolloutPercent.value.toString())
                        onInput { event ->
                            rolloutPercent.value = (event.target as org.w3c.dom.HTMLInputElement).value.toIntOrNull() ?: 0
                        }
                        attr("min", "0")
                        attr("max", "100")
                        style {
                            width(100.percent)
                            padding(5.px)
                            border {
                                width(1.px)
                                style(LineStyle.Solid)
                                color(FlagentTheme.inputBorder(themeMode))
                            }
                            borderRadius(3.px)
                        }
                    }
                }
            }
        }
        
        // Constraints section
        ConstraintsSection(flagId, segment, onUpdate)
        
        // Distributions section
        DistributionsSection(flagId, segment, variants, onUpdate)
    }
}

/**
 * Constraints Section - inline constraints management
 */
@Composable
private fun ConstraintsSection(
    flagId: Int,
    segment: flagent.api.model.SegmentResponse,
    onUpdate: () -> Unit
) {
    val themeMode = LocalThemeMode.current
    val newConstraintProperty = remember { mutableStateOf("") }
    val newConstraintOperator = remember { mutableStateOf("EQ") }
    val newConstraintValue = remember { mutableStateOf("") }
    val creating = remember { mutableStateOf(false) }
    val error = remember { mutableStateOf<String?>(null) }
    
    val operators = ConstraintOperators.OPERATOR_DISPLAY_PAIRS
    
    fun createConstraint() {
        if (newConstraintProperty.value.isBlank() || newConstraintValue.value.isBlank()) return
        
        creating.value = true
        error.value = null
        
        CoroutineScope(Dispatchers.Main).launch {
            try {
                ApiClient.createConstraint(
                    flagId,
                    segment.id,
                    flagent.api.model.CreateConstraintRequest(
                        property = newConstraintProperty.value.trim(),
                        operator = newConstraintOperator.value,
                        value = newConstraintValue.value.trim()
                    )
                )
                newConstraintProperty.value = ""
                newConstraintValue.value = ""
                newConstraintOperator.value = "EQ"
                onUpdate()
            } catch (e: Exception) {
                error.value = e.message ?: LocalizedStrings.failedToCreateConstraint
            } finally {
                creating.value = false
            }
        }
    }
    
    fun updateConstraint(constraint: flagent.api.model.ConstraintResponse) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                ApiClient.updateConstraint(
                    flagId,
                    segment.id,
                    constraint.id,
                    flagent.api.model.PutConstraintRequest(
                        property = constraint.property.trim(),
                        operator = constraint.operator,
                        value = constraint.value.trim()
                    )
                )
                onUpdate()
            } catch (e: Exception) {
                // Silent fail
            }
        }
    }
    
    fun deleteConstraint(constraint: flagent.api.model.ConstraintResponse) {
        if (!kotlinx.browser.window.confirm(LocalizedStrings.confirmDeleteConstraint)) return
        
        CoroutineScope(Dispatchers.Main).launch {
            try {
                ApiClient.deleteConstraint(flagId, segment.id, constraint.id)
                onUpdate()
            } catch (e: Exception) {
                // Silent fail
            }
        }
    }
    
    Div({
        style {
            marginBottom(15.px)
        }
    }) {
        H5({
            style {
                margin(0.px, 0.px, 10.px, 0.px)
                display(DisplayStyle.Flex)
                alignItems(AlignItems.Center)
                gap(6.px)
            }
        }) {
            Text(LocalizedStrings.constraintsMatchAll)
            InfoTooltip(
                title = LocalizedStrings.constraintsTooltipTitle,
                description = LocalizedStrings.constraintsTooltipDescription,
                details = LocalizedStrings.constraintsTooltipDetails
            )
        }
        
        if (segment.constraints.isEmpty()) {
            Div({
                style {
                    padding(10.px)
                    backgroundColor(FlagentTheme.inputBg(themeMode))
                    borderRadius(5.px)
                    textAlign("center")
                    color(FlagentTheme.textLight(themeMode))
                    marginBottom(10.px)
                }
            }) {
                Text(LocalizedStrings.noConstraintsAllPass)
            }
        } else {
            segment.constraints.forEach { constraint ->
                ConstraintRow(
                    constraint = constraint,
                    operators = operators,
                    onUpdate = { updateConstraint(constraint) },
                    onDelete = { deleteConstraint(constraint) }
                )
            }
        }
        
        // Add new constraint
        Div({
            style {
                display(DisplayStyle.Flex)
                gap(5.px)
                marginTop(10.px)
            }
        }) {
            Input(InputType.Text) {
                attr("placeholder", LocalizedStrings.propertyPlaceholder)
                value(newConstraintProperty.value)
                onInput { event -> newConstraintProperty.value = event.value }
                style {
                    flex(2)
                    padding(5.px)
                    border {
                        width(1.px)
                        style(LineStyle.Solid)
                        color(FlagentTheme.inputBorder(themeMode))
                    }
                    borderRadius(3.px)
                }
            }
            Select(attrs = {
                onChange { event ->
                    newConstraintOperator.value = (event.target as org.w3c.dom.HTMLSelectElement).value
                }
                attr("value", newConstraintOperator.value)
                style {
                    flexGrow(1)
                    padding(5.px)
                    border {
                        width(1.px)
                        style(LineStyle.Solid)
                        color(FlagentTheme.inputBorder(themeMode))
                    }
                    borderRadius(3.px)
                }
            }) {
                operators.forEach { (value, label) ->
                    Option(value = value) { Text(label) }
                }
            }
            Input(InputType.Text) {
                attr("placeholder", LocalizedStrings.valuePlaceholder)
                value(newConstraintValue.value)
                onInput { event -> newConstraintValue.value = event.value }
                style {
                    flex(2)
                    padding(5.px)
                    border {
                        width(1.px)
                        style(LineStyle.Solid)
                        color(FlagentTheme.inputBorder(themeMode))
                    }
                    borderRadius(3.px)
                }
            }
            Button({
                onClick { createConstraint() }
                if (creating.value || newConstraintProperty.value.isBlank() || newConstraintValue.value.isBlank()) {
                    attr("disabled", "true")
                }
                style {
                    padding(5.px, 10.px)
                    backgroundColor(if (creating.value || newConstraintProperty.value.isBlank() || newConstraintValue.value.isBlank()) FlagentTheme.NeutralLighter else FlagentTheme.Primary)
                    color(Color.white)
                    border {
                        width(0.px)
                        style(LineStyle.None)
                    }
                    borderRadius(3.px)
                    cursor(if (creating.value || newConstraintProperty.value.isBlank() || newConstraintValue.value.isBlank()) "not-allowed" else "pointer")
                    fontSize(12.px)
                }
            }) {
                Text(if (creating.value) "Adding..." else "Add Constraint")
            }
        }
    }
}

/**
 * Constraint Row - individual constraint editor with card visualization
 */
@Composable
private fun ConstraintRow(
    constraint: flagent.api.model.ConstraintResponse,
    operators: List<Pair<String, String>>,
    onUpdate: () -> Unit,
    onDelete: () -> Unit
) {
    val themeMode = LocalThemeMode.current
    val property = remember { mutableStateOf(constraint.property) }
    val operator = remember { mutableStateOf(constraint.operator) }
    val value = remember { mutableStateOf(constraint.value) }
    val saving = remember { mutableStateOf(false) }
    
    LaunchedEffect(constraint) {
        property.value = constraint.property
        operator.value = constraint.operator
        value.value = constraint.value
    }
    
    val operatorSymbol = when (operator.value) {
        "EQ" -> "="
        "NEQ" -> "≠"
        "GT" -> ">"
        "GTE" -> "≥"
        "LT" -> "<"
        "LTE" -> "≤"
        "IN" -> "∈"
        "NOT_IN" -> "∉"
        "CONTAINS" -> "⊃"
        "NOT_CONTAINS" -> "⊅"
        "STARTS_WITH" -> "→"
        "ENDS_WITH" -> "←"
        else -> operator.value
    }
    
    Div({
        style {
            padding(12.px)
            border {
                width(1.px)
                style(LineStyle.Solid)
                color(FlagentTheme.inputBorder(themeMode))
            }
            borderRadius(6.px)
            backgroundColor(FlagentTheme.cardBg(themeMode))
            marginBottom(10.px)
            property("box-shadow", "0 1px 3px ${FlagentTheme.Shadow}")
        }
    }) {
        // Visual constraint display
        Div({
            style {
                display(DisplayStyle.Flex)
                alignItems(AlignItems.Center)
                gap(8.px)
                marginBottom(10.px)
                padding(8.px)
                backgroundColor(FlagentTheme.inputBg(themeMode))
                borderRadius(4.px)
            }
        }) {
            Span({
                style {
                    padding(4.px, 8.px)
                    backgroundColor(FlagentTheme.Primary)
                    color(Color.white)
                    borderRadius(4.px)
                    fontSize(12.px)
                    fontWeight("600")
                    minWidth(60.px)
                    textAlign("center")
                }
            }) {
                Text(property.value.ifBlank { "property" })
            }
            Span({
                style {
                    padding(4.px, 10.px)
                    backgroundColor(FlagentTheme.Secondary)
                    color(Color.white)
                    borderRadius(4.px)
                    fontSize(14.px)
                    fontWeight("bold")
                    minWidth(40.px)
                    textAlign("center")
                    fontFamily("monospace")
                }
            }) {
                Text(operatorSymbol)
            }
            Span({
                style {
                    padding(4.px, 8.px)
                    backgroundColor(FlagentTheme.Neutral)
                    color(Color.white)
                    borderRadius(4.px)
                    fontSize(12.px)
                    fontWeight("500")
                    flex(1)
                    textAlign("center")
                }
            }) {
                Text(value.value.ifBlank { "value" })
            }
        }
        
        // Edit form
        Div({
            style {
                display(DisplayStyle.Flex)
                gap(5.px)
                alignItems(AlignItems.Center)
            }
        }) {
            Input(InputType.Text) {
                value(property.value)
                onInput { event -> property.value = event.value }
                attr("placeholder", LocalizedStrings.propertyPlaceholder)
                style {
                    flex(2)
                    padding(6.px, 8.px)
                    border {
                        width(1.px)
                        style(LineStyle.Solid)
                        color(FlagentTheme.inputBorder(themeMode))
                    }
                    borderRadius(4.px)
                    fontSize(12.px)
                }
            }
            Select(attrs = {
                onChange { event ->
                    operator.value = (event.target as org.w3c.dom.HTMLSelectElement).value
                }
                attr("value", operator.value)
                style {
                    flexGrow(1)
                    padding(6.px, 8.px)
                    border {
                        width(1.px)
                        style(LineStyle.Solid)
                        color(FlagentTheme.inputBorder(themeMode))
                    }
                    borderRadius(4.px)
                    fontSize(12.px)
                }
            }) {
                operators.forEach { (opValue, label) ->
                    Option(value = opValue) { Text(label) }
                }
            }
            Input(InputType.Text) {
                value(value.value)
                onInput { event -> value.value = event.value }
                attr("placeholder", LocalizedStrings.valuePlaceholder)
                style {
                    flex(2)
                    padding(6.px, 8.px)
                    border {
                        width(1.px)
                        style(LineStyle.Solid)
                        color(FlagentTheme.inputBorder(themeMode))
                    }
                    borderRadius(4.px)
                    fontSize(12.px)
                }
            }
            Button({
                onClick {
                    saving.value = true
                    onUpdate()
                    saving.value = false
                }
                if (saving.value) {
                    attr("disabled", "true")
                }
                style {
                    padding(6.px, 12.px)
                    backgroundColor(if (saving.value) FlagentTheme.NeutralLighter else FlagentTheme.Success)
                    color(Color.white)
                    border {
                        width(0.px)
                        style(LineStyle.None)
                    }
                    borderRadius(4.px)
                    cursor(if (saving.value) "not-allowed" else "pointer")
                    fontSize(12.px)
                    fontWeight("500")
                }
            }) {
                Text(if (saving.value) LocalizedStrings.saving else LocalizedStrings.save)
            }
            Button({
                onClick { onDelete() }
                style {
                    padding(6.px, 12.px)
                    backgroundColor(FlagentTheme.Error)
                    color(Color.white)
                    border {
                        width(0.px)
                        style(LineStyle.None)
                    }
                    borderRadius(4.px)
                    cursor("pointer")
                    fontSize(12.px)
                    fontWeight("500")
                }
            }) {
                Text(LocalizedStrings.delete)
            }
        }
    }
}

/**
 * Distributions Section - manage variant distributions
 */
@Composable
private fun DistributionsSection(
    flagId: Int,
    segment: flagent.api.model.SegmentResponse,
    variants: List<flagent.api.model.VariantResponse>,
    onUpdate: () -> Unit
) {
    val themeMode = LocalThemeMode.current
    val showEditDialog = remember { mutableStateOf(false) }
    
    Div({
        style {
            marginTop(15.px)
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
            H5({
                style {
                    margin(0.px)
                }
            }) {
                Text(LocalizedStrings.distribution)
            }
            Button({
                onClick { showEditDialog.value = true }
                style {
                    padding(5.px, 10.px)
                    backgroundColor(FlagentTheme.Neutral)
                    color(Color.white)
                    border {
                        width(0.px)
                        style(LineStyle.None)
                    }
                    borderRadius(3.px)
                    cursor("pointer")
                    fontSize(12.px)
                }
            }) {
                Text(LocalizedStrings.edit)
            }
        }
        
        if (segment.distributions.isEmpty()) {
            Div({
                style {
                    padding(10.px)
                    property("background-color", "rgba(239, 68, 68, 0.1)")
                    borderRadius(5.px)
                    textAlign("center")
                    color(FlagentTheme.textLight(themeMode))
                }
            }) {
                Text(LocalizedStrings.noDistributionYet)
            }
        } else {
            Div({
                style {
                    display(DisplayStyle.Flex)
                    flexWrap(FlexWrap.Wrap)
                    gap(15.px)
                }
            }) {
                segment.distributions.forEach { distribution ->
                    DistributionCard(distribution)
                }
            }
        }
        
        if (showEditDialog.value) {
            DistributionEditDialog(
                flagId = flagId,
                segmentId = segment.id,
                variants = variants,
                currentDistributions = segment.distributions,
                onClose = { showEditDialog.value = false },
                onUpdate = {
                    showEditDialog.value = false
                    onUpdate()
                }
            )
        }
    }
}

/**
 * Distribution Card - visual representation with circular progress
 */
@Composable
private fun DistributionCard(distribution: flagent.api.model.DistributionResponse) {
    val themeMode = LocalThemeMode.current
    // Color mapping for variants (cycling through theme colors)
    val variantColors = listOf(
        FlagentTheme.Primary,
        FlagentTheme.Secondary,
        FlagentTheme.Accent,
        FlagentTheme.Success,
        FlagentTheme.Info
    )
    val variantIndex = (distribution.variantKey?.hashCode() ?: 0) % variantColors.size
    val variantColor = variantColors[kotlin.math.abs(variantIndex)]
    
    Div({
        style {
            padding(20.px)
            border {
                width(1.px)
                style(LineStyle.Solid)
                color(FlagentTheme.inputBorder(themeMode))
            }
            borderRadius(8.px)
            backgroundColor(FlagentTheme.cardBg(themeMode))
            textAlign("center")
            minWidth(140.px)
            display(DisplayStyle.Flex)
            flexDirection(FlexDirection.Column)
            alignItems(AlignItems.Center)
            gap(15.px)
            property("box-shadow", "0 2px 4px ${FlagentTheme.Shadow}")
            property("transition", "transform 0.2s, box-shadow 0.2s")
        }
        onMouseEnter {
            val element = it.target as org.w3c.dom.HTMLElement
            element.style.transform = "translateY(-2px)"
            element.style.setProperty("box-shadow", "0 4px 8px ${FlagentTheme.ShadowHover}")
        }
        onMouseLeave {
            val element = it.target as org.w3c.dom.HTMLElement
            element.style.transform = "translateY(0)"
            element.style.setProperty("box-shadow", "0 2px 4px ${FlagentTheme.Shadow}")
        }
    }) {
        // Variant name
        Div({
            style {
                fontSize(14.px)
                fontWeight("bold")
                color(FlagentTheme.text(themeMode))
                marginBottom(5.px)
            }
        }) {
            Text(distribution.variantKey ?: "Unknown")
        }
        
        // Circular progress
        CircularProgress(
            percentage = distribution.percent,
            size = 100.px,
            strokeWidth = 10.px,
            color = variantColor,
            backgroundColor = FlagentTheme.inputBg(themeMode),
            showLabel = true,
            labelSize = 18.px,
            labelColor = FlagentTheme.Text
        )
    }
}

/**
 * Distribution Edit Dialog - using Modal component
 */
@Composable
private fun DistributionEditDialog(
    flagId: Int,
    segmentId: Int,
    variants: List<flagent.api.model.VariantResponse>,
    currentDistributions: List<flagent.api.model.DistributionResponse>,
    onClose: () -> Unit,
    onUpdate: () -> Unit
) {
    val themeMode = LocalThemeMode.current
    val distributions = remember {
        mutableStateMapOf<Int, Int>().apply {
            currentDistributions.forEach { dist ->
                this[dist.variantID] = dist.percent
            }
        }
    }
    val saving = remember { mutableStateOf(false) }
    val error = remember { mutableStateOf<String?>(null) }
    
    fun saveDistributions() {
        val total = distributions.values.sum()
        if (total != 100) {
            error.value = LocalizedStrings.percentagesMustAddUpTo100(total)
            return
        }
        
        saving.value = true
        error.value = null
        
        CoroutineScope(Dispatchers.Main).launch {
            try {
                ApiClient.updateDistributions(
                    flagId,
                    segmentId,
                    flagent.api.model.PutDistributionsRequest(
                        distributions = distributions.map { (variantId, percent) ->
                            flagent.api.model.DistributionRequest(
                                variantID = variantId,
                                percent = percent
                            )
                        }
                    )
                )
                onUpdate()
            } catch (e: Exception) {
                error.value = e.message ?: LocalizedStrings.failedToSaveDistributions
            } finally {
                saving.value = false
            }
        }
    }
    
    val total = distributions.values.sum()
    
    Modal(
        title = LocalizedStrings.editDistribution,
        onClose = onClose,
        onConfirm = { saveDistributions() },
        confirmText = LocalizedStrings.save,
        cancelText = LocalizedStrings.cancel,
        showCancel = true,
        confirmDisabled = saving.value || total != 100,
        confirmLoading = saving.value
    ) {
        if (error.value != null) {
            Div({
                style {
                    color(FlagentTheme.Error)
                    padding(10.px)
                    property("background-color", "rgba(239, 68, 68, 0.1)")
                    borderRadius(5.px)
                    marginBottom(15.px)
                }
            }) {
                Text("${LocalizedStrings.error}: ${error.value}")
            }
        }
        
        if (total != 100) {
            Div({
                style {
                    color(FlagentTheme.Error)
                    padding(10.px)
                    property("background-color", "rgba(239, 68, 68, 0.1)")
                    borderRadius(5.px)
                    marginBottom(15.px)
                }
            }) {
                Text(LocalizedStrings.percentagesMustAddUpTo100(total))
            }
        }
        
        Div({
            style {
                display(DisplayStyle.Flex)
                flexDirection(FlexDirection.Column)
                gap(15.px)
            }
        }) {
            variants.forEach { variant ->
                Div({
                    style {
                        padding(15.px)
                        border {
                            width(1.px)
                            style(LineStyle.Solid)
                            color(FlagentTheme.inputBorder(themeMode))
                        }
                        borderRadius(5.px)
                        backgroundColor(FlagentTheme.inputBg(themeMode))
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
                        Label(attrs = {
                            style {
                                display(DisplayStyle.Flex)
                                alignItems(AlignItems.Center)
                                gap(10.px)
                                cursor("pointer")
                            }
                        }) {
                            Input(InputType.Checkbox) {
                                checked(distributions.containsKey(variant.id))
                                onChange { event ->
                                    val checked = (event.target as org.w3c.dom.HTMLInputElement).checked
                                    if (checked) {
                                        distributions[variant.id] = distributions[variant.id] ?: 0
                                    } else {
                                        distributions.remove(variant.id)
                                    }
                                }
                                style {
                                    cursor("pointer")
                                }
                            }
                            Text(variant.key)
                        }
                        if (distributions.containsKey(variant.id)) {
                            Span({
                                style {
                                    fontWeight("bold")
                                    color(FlagentTheme.Primary)
                                    fontSize(18.px)
                                }
                            }) {
                                Text("${distributions[variant.id]}%")
                            }
                        }
                    }
                    if (distributions.containsKey(variant.id)) {
                        Slider(
                            value = distributions[variant.id] ?: 0,
                            onValueChange = { newValue ->
                                distributions[variant.id] = newValue
                            },
                            min = 0,
                            max = 100,
                            step = 1,
                            label = null,
                            showValue = false,
                            disabled = false
                        )
                    }
                }
            }
        }
    }
}

/**
 * Flag Delete Card
 */
@Composable
private fun FlagDeleteCard(flagId: Int) {
    val themeMode = LocalThemeMode.current
    val showConfirm = remember { mutableStateOf(false) }
    val deleting = remember { mutableStateOf(false) }
    val error = remember { mutableStateOf<String?>(null) }
    
    Div({
        style {
            padding(20.px)
            border {
                width(1.px)
                style(LineStyle.Solid)
                color(FlagentTheme.inputBorder(themeMode))
            }
            borderRadius(5.px)
            backgroundColor(FlagentTheme.cardBg(themeMode))
        }
    }) {
        H2({
            style {
                margin(0.px, 0.px, 15.px, 0.px)
                color(FlagentTheme.text(themeMode))
            }
        }) {
            Text(LocalizedStrings.flagSettings)
        }
        
        if (showConfirm.value) {
            Div({
                style {
                    padding(15.px)
                    property("background-color", "rgba(245, 158, 11, 0.1)")
                    border {
                        width(1.px)
                        style(LineStyle.Solid)
                        color(FlagentTheme.Warning)
                    }
                    borderRadius(5.px)
                    marginBottom(15.px)
                }
            }) {
                Text(LocalizedStrings.deleteFlagConfirm)
                Div({
                    style {
                        display(DisplayStyle.Flex)
                        gap(10.px)
                        marginTop(10.px)
                    }
                }) {
                    Button({
                        onClick {
                            deleting.value = true
                            error.value = null
                            CoroutineScope(Dispatchers.Main).launch {
                                try {
                                    ApiClient.deleteFlag(flagId)
                                    Router.navigateTo(Route.FlagsList)
                                } catch (e: Exception) {
                                    error.value = e.message ?: LocalizedStrings.failedToDeleteFlag
                                    deleting.value = false
                                }
                            }
                        }
                        if (deleting.value) {
                            attr("disabled", "true")
                        }
                        style {
                            padding(8.px, 16.px)
                            backgroundColor(if (deleting.value) FlagentTheme.NeutralLighter else FlagentTheme.Error)
                            color(Color.white)
                            border {
                                width(0.px)
                                style(LineStyle.None)
                            }
                            borderRadius(5.px)
                            cursor(if (deleting.value) "not-allowed" else "pointer")
                        }
                    }) {
                        Text(if (deleting.value) LocalizedStrings.deleting else LocalizedStrings.confirmDelete)
                    }
                    Button({
                        onClick { showConfirm.value = false }
                        style {
                            padding(8.px, 16.px)
                            backgroundColor(FlagentTheme.Neutral)
                            color(Color.white)
                            border {
                                width(0.px)
                                style(LineStyle.None)
                            }
                            borderRadius(5.px)
                            cursor("pointer")
                        }
                    }) {
                        Text(LocalizedStrings.cancel)
                    }
                }
                if (error.value != null) {
                    Div({
                        style {
                            color(FlagentTheme.Error)
                            marginTop(10.px)
                        }
                    }) {
                        Text("${LocalizedStrings.error}: ${error.value}")
                    }
                }
            }
        } else {
            Button({
                onClick { showConfirm.value = true }
                style {
                    padding(8.px, 16.px)
                    backgroundColor(FlagentTheme.Error)
                    color(Color.white)
                    border {
                        width(0.px)
                        style(LineStyle.None)
                    }
                    borderRadius(5.px)
                    cursor("pointer")
                }
            }) {
                Text(LocalizedStrings.deleteFlag)
            }
        }
    }
}
