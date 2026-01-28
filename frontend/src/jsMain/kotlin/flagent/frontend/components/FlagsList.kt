package flagent.frontend.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import flagent.api.model.CreateFlagRequest
import flagent.api.model.FlagResponse
import flagent.frontend.api.ApiClient
import flagent.frontend.config.AppConfig
import flagent.frontend.i18n.LocalizedStrings
import flagent.frontend.components.Icon
import flagent.frontend.navigation.Route
import flagent.frontend.navigation.Router
import flagent.frontend.service.RealtimeService
import flagent.frontend.theme.FlagentTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

/**
 * FlagsList component - displays list of all flags
 */
@Composable
fun FlagsList() {
    val flags = remember { mutableStateListOf<FlagResponse>() }
    val deletedFlags = remember { mutableStateListOf<FlagResponse>() }
    val loading = remember { mutableStateOf(true) }
    val deletedFlagsLoading = remember { mutableStateOf(false) }
    val error = remember { mutableStateOf<String?>(null) }
    val searchQuery = remember { mutableStateOf("") }
    val statusFilter = remember { mutableStateOf<Boolean?>(null) } // null = all, true = enabled, false = disabled
    val sortColumn = remember { mutableStateOf<String?>(null) }
    val sortAscending = remember { mutableStateOf(true) }
    val showDeletedFlags = remember { mutableStateOf(false) }
    val newFlagDescription = remember { mutableStateOf("") }
    val creatingFlag = remember { mutableStateOf(false) }

    fun loadFlags() {
        CoroutineScope(Dispatchers.Main).launch {
            loading.value = true
            error.value = null
            try {
                val fetchedFlags = ApiClient.getFlags()
                flags.clear()
                flags.addAll(fetchedFlags.reversed()) // Reverse to show newest first
            } catch (e: Exception) {
                error.value = e.message ?: LocalizedStrings.failedToLoadFlags
            } finally {
                loading.value = false
            }
        }
    }

    fun loadDeletedFlags() {
        CoroutineScope(Dispatchers.Main).launch {
            deletedFlagsLoading.value = true
            try {
                val fetchedDeletedFlags = ApiClient.getDeletedFlags()
                deletedFlags.clear()
                deletedFlags.addAll(fetchedDeletedFlags.reversed())
            } catch (e: Exception) {
                // Silent fail for deleted flags
                error.value = buildString {
                    append(LocalizedStrings.failedToLoadDeletedFlags)
                    e.message?.let {
                        append(": ")
                        append(it)
                    }
                }
            } finally {
                deletedFlagsLoading.value = false
            }
        }
    }

    fun restoreFlag(flag: FlagResponse) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val restoredFlag = ApiClient.restoreFlag(flag.id)
                deletedFlags.removeAll { it.id == flag.id }
                flags.add(0, restoredFlag)
            } catch (e: Exception) {
                error.value = buildString {
                    append(LocalizedStrings.failedToRestoreFlag)
                    e.message?.let {
                        append(": ")
                        append(it)
                    }
                }
            }
        }
    }

    fun createFlag(template: String? = null) {
        if (newFlagDescription.value.isBlank()) return
        
        CoroutineScope(Dispatchers.Main).launch {
            creatingFlag.value = true
            error.value = null
            try {
                val newFlag = ApiClient.createFlag(
                    CreateFlagRequest(
                        description = newFlagDescription.value,
                        template = template
                    )
                )
                newFlagDescription.value = ""
                flags.add(0, newFlag)
                Router.navigateTo(Route.FlagDetail(newFlag.id))
            } catch (e: Exception) {
                error.value = buildString {
                    append(LocalizedStrings.failedToCreateFlag)
                    e.message?.let {
                        append(": ")
                        append(it)
                    }
                }
            } finally {
                creatingFlag.value = false
            }
        }
    }

    fun sortFlags(column: String) {
        if (sortColumn.value == column) {
            sortAscending.value = !sortAscending.value
        } else {
            sortColumn.value = column
            sortAscending.value = true
        }
    }

    // Real-time refresh trigger: when SSE fires, increment to trigger loadFlags
    val realtimeRefreshTrigger = remember { mutableStateOf(0) }
    
    LaunchedEffect(Unit) {
        loadFlags()
    }
    
    // Real-time auto-refresh: connect to SSE and reload flags on any flag event
    DisposableEffect(AppConfig.Features.enableRealtime) {
        if (AppConfig.Features.enableRealtime) {
            val service = RealtimeService(
                onEvent = { realtimeRefreshTrigger.value++ },
                onConnectionChange = { }
            )
            service.connect(flagKeys = emptyList(), flagIds = emptyList())
            onDispose { service.disconnect() }
        } else {
            onDispose { }
        }
    }
    
    LaunchedEffect(realtimeRefreshTrigger.value) {
        if (realtimeRefreshTrigger.value > 0) loadFlags()
    }

    Div({
        style {
            marginTop(20.px)
            padding(20.px)
        }
    }) {
        // Create Flag Section
        Div({
            style {
                marginBottom(25.px)
                padding(20.px)
                backgroundColor(FlagentTheme.Background)
                border {
                    width(1.px)
                    style(LineStyle.Solid)
                    color(FlagentTheme.Border)
                }
                borderRadius(12.px)
                property("box-shadow", "0 4px 12px rgba(0, 0, 0, 0.08)")
                property("transition", "all 0.3s cubic-bezier(0.4, 0, 0.2, 1)")
            }
            onMouseEnter {
                val element = it.target as org.w3c.dom.HTMLElement
                element.style.setProperty("box-shadow", "0 8px 20px rgba(0, 0, 0, 0.12)")
                element.style.transform = "translateY(-2px)"
            }
            onMouseLeave {
                val element = it.target as org.w3c.dom.HTMLElement
                element.style.setProperty("box-shadow", "0 4px 12px rgba(0, 0, 0, 0.08)")
                element.style.transform = "translateY(0)"
            }
        }) {
            Div({
                style {
                    display(DisplayStyle.Flex)
                    alignItems(AlignItems.Center)
                    gap(8.px)
                    marginBottom(15.px)
                }
            }) {
                Icon(
                    name = "add_circle_outline",
                    size = 22.px,
                    color = FlagentTheme.Primary
                )
                H3({
                    style {
                        margin(0.px)
                        color(FlagentTheme.Text)
                        fontSize(18.px)
                        fontWeight("600")
                    }
                }) {
                    Text(LocalizedStrings.createNewFlag)
                }
                InfoTooltip(
                    title = LocalizedStrings.featureFlagsTooltipTitle,
                    description = LocalizedStrings.featureFlagsTooltipDescription,
                    details = LocalizedStrings.featureFlagsTooltipDetails
                )
            }
            Div({
                style {
                    display(DisplayStyle.Flex)
                    gap(10.px)
                    marginBottom(10.px)
                }
            }) {
                Input(InputType.Text) {
                    attr("placeholder", LocalizedStrings.enterFlagDescriptionPlaceholder)
                    value(newFlagDescription.value)
                    onInput { event -> newFlagDescription.value = event.value }
                    style {
                        flex(1)
                        padding(12.px, 16.px)
                        border {
                            width(1.px)
                            style(LineStyle.Solid)
                            color(FlagentTheme.Border)
                        }
                        borderRadius(6.px)
                        fontSize(14.px)
                        property("transition", "border-color 0.2s")
                    }
                    onFocus {
                        (it.target as org.w3c.dom.HTMLElement).style.borderColor = FlagentTheme.Primary.toString()
                    }
                    onBlur {
                        (it.target as org.w3c.dom.HTMLElement).style.borderColor = FlagentTheme.Border.toString()
                    }
                }
                Button({
                    onClick { createFlag() }
                    if (creatingFlag.value || newFlagDescription.value.isBlank()) {
                        attr("disabled", "true")
                    }
                    style {
                        padding(12.px, 20.px)
                        backgroundColor(if (creatingFlag.value || newFlagDescription.value.isBlank()) {
                            FlagentTheme.NeutralLighter
                        } else {
                            FlagentTheme.Success
                        })
                        color(FlagentTheme.Background)
                        border {
                            width(0.px)
                            style(LineStyle.None)
                        }
                        borderRadius(8.px)
                        cursor(if (creatingFlag.value || newFlagDescription.value.isBlank()) "not-allowed" else "pointer")
                        fontWeight("600")
                        fontSize(14.px)
                        property("transition", "all 0.3s cubic-bezier(0.4, 0, 0.2, 1)")
                        property("box-shadow", if (creatingFlag.value || newFlagDescription.value.isBlank()) "none" else "0 4px 12px rgba(16, 185, 129, 0.3)")
                    }
                    onMouseEnter {
                        if (!creatingFlag.value && newFlagDescription.value.isNotBlank()) {
                            val element = it.target as org.w3c.dom.HTMLElement
                            element.style.transform = "translateY(-2px) scale(1.02)"
                            element.style.setProperty("box-shadow", "0 6px 16px rgba(16, 185, 129, 0.4)")
                        }
                    }
                    onMouseLeave {
                        val element = it.target as org.w3c.dom.HTMLElement
                        element.style.transform = "translateY(0) scale(1)"
                        element.style.setProperty("box-shadow", if (creatingFlag.value || newFlagDescription.value.isBlank()) "none" else "0 4px 12px rgba(16, 185, 129, 0.3)")
                    }
                }) {
                    Icon(
                        name = if (creatingFlag.value) "hourglass_empty" else "auto_awesome",
                        size = 18.px,
                        color = FlagentTheme.Background
                    )
                    Text(if (creatingFlag.value) LocalizedStrings.creating else LocalizedStrings.createFlag)
                }
                Select({
                    onChange { event ->
                        val selected = (event.target as org.w3c.dom.HTMLSelectElement).value
                        when (selected) {
                            "simple_boolean_flag" -> createFlag("simple_boolean_flag")
                            "" -> Router.navigateTo(Route.CreateFlag)
                        }
                        // Reset select to default value after selection
                        (event.target as org.w3c.dom.HTMLSelectElement).value = ""
                    }
                    style {
                        padding(12.px, 16.px)
                        border {
                            width(1.px)
                            style(LineStyle.Solid)
                            color(FlagentTheme.Border)
                        }
                        borderRadius(6.px)
                        fontSize(14.px)
                        backgroundColor(FlagentTheme.Background)
                        cursor("pointer")
                    }
                }) {
                    Option(value = "") { Text(LocalizedStrings.moreOptions) }
                    Option(value = "simple_boolean_flag") { Text(LocalizedStrings.createSimpleBooleanFlag) }
                }
            }
        }

        // Search and Filter Section
        Div({
            style {
                display(DisplayStyle.Flex)
                gap(12.px)
                marginBottom(25.px)
                alignItems(AlignItems.Center)
                padding(15.px)
                backgroundColor(FlagentTheme.Background)
                border {
                    width(1.px)
                    style(LineStyle.Solid)
                    color(FlagentTheme.Border)
                }
                borderRadius(8.px)
                property("box-shadow", "0 1px 3px ${FlagentTheme.Shadow}")
            }
        }) {
            Icon(
                name = "search",
                size = 20.px,
                color = FlagentTheme.TextLight
            )
            Input(InputType.Text) {
                attr("placeholder", LocalizedStrings.searchFlagsDetailedPlaceholder)
                value(searchQuery.value)
                onInput { event -> searchQuery.value = event.value }
                style {
                    flex(1)
                    padding(12.px, 16.px)
                    border {
                        width(1.px)
                        style(LineStyle.Solid)
                        color(FlagentTheme.Border)
                    }
                    borderRadius(6.px)
                    fontSize(14.px)
                    property("transition", "border-color 0.2s")
                }
                onFocus {
                    (it.target as org.w3c.dom.HTMLElement).style.borderColor = FlagentTheme.Primary.toString()
                }
                onBlur {
                    (it.target as org.w3c.dom.HTMLElement).style.borderColor = FlagentTheme.Border.toString()
                }
            }
            Select({
                onChange { event ->
                    val selected = (event.target as org.w3c.dom.HTMLSelectElement).value
                    statusFilter.value = when (selected) {
                        "enabled" -> true
                        "disabled" -> false
                        else -> null
                    }
                }
                style {
                    padding(12.px, 16.px)
                    border {
                        width(1.px)
                        style(LineStyle.Solid)
                        color(FlagentTheme.Border)
                    }
                    borderRadius(6.px)
                    fontSize(14.px)
                    backgroundColor(FlagentTheme.Background)
                    cursor("pointer")
                    minWidth(140.px)
                }
            }) {
                Option(value = "all") { Text(LocalizedStrings.allStatus) }
                Option(value = "enabled") { Text(LocalizedStrings.enabledFlags) }
                Option(value = "disabled") { Text(LocalizedStrings.disabledFlags) }
            }
        }
        P({
            style {
                display(DisplayStyle.Flex)
                alignItems(AlignItems.Center)
                gap(6.px)
                marginTop(8.px)
                marginBottom(0.px)
                fontSize(13.px)
                color(FlagentTheme.TextLight)
            }
        }) {
            Icon("info", size = 16.px, color = FlagentTheme.TextLight)
            Text(LocalizedStrings.flagsListKeyHint)
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
        } else {
            // Filter and sort flags
            val filteredAndSortedFlags = remember(flags, searchQuery.value, statusFilter.value, sortColumn.value, sortAscending.value) {
                var result = flags.toList()
                
                // Filter by search query
                if (searchQuery.value.isNotBlank()) {
                    val terms = searchQuery.value.split(",").map { it.trim().lowercase() }
                    result = result.filter { flag ->
                        terms.all { term ->
                            flag.id.toString().contains(term) ||
                            flag.key.contains(term, ignoreCase = true) ||
                            (flag.description?.lowercase()?.contains(term) == true) ||
                            flag.tags.any { it.value.lowercase().contains(term) }
                        }
                    }
                }
                
                // Filter by enabled status
                statusFilter.value?.let { enabled ->
                    result = result.filter { it.enabled == enabled }
                }
                
                // Sort
                sortColumn.value?.let { column ->
                    result = when (column) {
                        "id" -> result.sortedBy { it.id }
                        "description" -> result.sortedBy { it.description ?: "" }
                        "updatedBy" -> result.sortedBy { it.updatedBy ?: "" }
                        "updatedAt" -> result.sortedBy { it.updatedAt ?: "" }
                        else -> result
                    }
                    if (!sortAscending.value) {
                        result = result.reversed()
                    }
                }
                
                result
            }

            if (filteredAndSortedFlags.isEmpty()) {
                Div({
                    style {
                        padding(40.px, 20.px)
                        textAlign("center")
                        color(FlagentTheme.TextLight)
                        backgroundColor(FlagentTheme.Background)
                        border {
                            width(1.px)
                            style(LineStyle.Solid)
                            color(FlagentTheme.Border)
                        }
                        borderRadius(8.px)
                    }
                }) {
                    Icon(
                        name = "search_off",
                        size = 48.px,
                        color = FlagentTheme.TextLighter
                    )
                    Div({
                        style {
                            fontSize(16.px)
                            fontWeight("500")
                        }
                    }) {
                        Text(LocalizedStrings.noFlags)
                    }
                    Div({
                        style {
                            fontSize(14.px)
                            marginTop(5.px)
                            opacity(0.7)
                        }
                    }) {
                        Text(LocalizedStrings.tryAdjustingSearchOrFilters)
                    }
                }
            } else {
                Div({
                    style {
                        backgroundColor(FlagentTheme.Background)
                        border {
                            width(1.px)
                            style(LineStyle.Solid)
                            color(FlagentTheme.Border)
                        }
                        borderRadius(12.px)
                        overflow("hidden")
                        overflowX("auto")
                        property("box-shadow", "0 4px 16px rgba(0, 0, 0, 0.08)")
                        property("transition", "box-shadow 0.3s ease")
                    }
                }) {
                    Table({
                        style {
                            width(100.percent)
                            property("border-collapse", "collapse")
                            property("min-width", "800px")
                        }
                    }) {
                        Thead {
                            Tr({
                                style {
                                    backgroundColor(FlagentTheme.BackgroundAlt)
                                    property("border-bottom", "2px solid ${FlagentTheme.Border}")
                                }
                            }) {
                                Th({
                                    onClick { sortFlags("id") }
                                    style {
                                        padding(16.px, 12.px)
                                        cursor("pointer")
                                        property("user-select", "none")
                                        fontWeight("600")
                                        fontSize(13.px)
                                        color(FlagentTheme.Text)
                                        textAlign("left")
                                        property("transition", "background-color 0.2s")
                                    }
                                    onMouseEnter {
                                        (it.target as org.w3c.dom.HTMLElement).style.backgroundColor = FlagentTheme.BackgroundDark.toString()
                                    }
                                    onMouseLeave {
                                        (it.target as org.w3c.dom.HTMLElement).style.backgroundColor = FlagentTheme.BackgroundAlt.toString()
                                    }
                                    }) {
                                        Span({
                                            style {
                                                display(DisplayStyle.Flex)
                                                alignItems(AlignItems.Center)
                                                gap(6.px)
                                            }
                                        }) {
                                            Icon(
                                                name = "tag",
                                                size = 16.px,
                                                color = FlagentTheme.Text
                                            )
                                            Text(LocalizedStrings.flagId)
                                        if (sortColumn.value == "id") {
                                            Span({
                                                style {
                                                    fontSize(12.px)
                                                    color(FlagentTheme.Primary)
                                                }
                                            }) {
                                                Text(if (sortAscending.value) "↑" else "↓")
                                            }
                                        }
                                    }
                                }
                                Th({
                                    style {
                                        padding(16.px, 12.px)
                                        fontWeight("600")
                                        fontSize(13.px)
                                        color(FlagentTheme.Text)
                                        textAlign("left")
                                    }
                                    }) { 
                                    Span({
                                        style {
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
                                        Text(LocalizedStrings.description)
                                    }
                                }
                                Th({
                                    style {
                                        padding(16.px, 12.px)
                                        fontWeight("600")
                                        fontSize(13.px)
                                        color(FlagentTheme.Text)
                                        textAlign("left")
                                    }
                                }) { 
                                    Span({
                                        style {
                                            display(DisplayStyle.Flex)
                                            alignItems(AlignItems.Center)
                                            gap(6.px)
                                        }
                                    }) {
                                        Icon(
                                            name = "local_offer",
                                            size = 16.px,
                                            color = FlagentTheme.Text
                                        )
                                        Text(LocalizedStrings.tags)
                                    }
                                }
                                Th({
                                    onClick { sortFlags("updatedBy") }
                                    style {
                                        padding(16.px, 12.px)
                                        cursor("pointer")
                                        property("user-select", "none")
                                        fontWeight("600")
                                        fontSize(13.px)
                                        color(FlagentTheme.Text)
                                        textAlign("left")
                                        property("transition", "background-color 0.2s")
                                    }
                                    onMouseEnter {
                                        (it.target as org.w3c.dom.HTMLElement).style.backgroundColor = FlagentTheme.BackgroundDark.toString()
                                    }
                                    onMouseLeave {
                                        (it.target as org.w3c.dom.HTMLElement).style.backgroundColor = FlagentTheme.BackgroundAlt.toString()
                                    }
                                    }) {
                                        Span({
                                            style {
                                                display(DisplayStyle.Flex)
                                                alignItems(AlignItems.Center)
                                                gap(6.px)
                                            }
                                        }) {
                                            Icon(
                                                name = "person",
                                                size = 16.px,
                                                color = FlagentTheme.Text
                                            )
                                            Text(LocalizedStrings.lastUpdatedBy)
                                        if (sortColumn.value == "updatedBy") {
                                            Span({
                                                style {
                                                    fontSize(12.px)
                                                    color(FlagentTheme.Primary)
                                                }
                                            }) {
                                                Text(if (sortAscending.value) "↑" else "↓")
                                            }
                                        }
                                    }
                                }
                                Th({
                                    onClick { sortFlags("updatedAt") }
                                    style {
                                        padding(16.px, 12.px)
                                        cursor("pointer")
                                        property("user-select", "none")
                                        fontWeight("600")
                                        fontSize(13.px)
                                        color(FlagentTheme.Text)
                                        textAlign("left")
                                        property("transition", "background-color 0.2s")
                                    }
                                    onMouseEnter {
                                        (it.target as org.w3c.dom.HTMLElement).style.backgroundColor = FlagentTheme.BackgroundDark.toString()
                                    }
                                    onMouseLeave {
                                        (it.target as org.w3c.dom.HTMLElement).style.backgroundColor = FlagentTheme.BackgroundAlt.toString()
                                    }
                                    }) {
                                        Span({
                                            style {
                                                display(DisplayStyle.Flex)
                                                alignItems(AlignItems.Center)
                                                gap(6.px)
                                            }
                                        }) {
                                            Icon(
                                                name = "schedule",
                                                size = 16.px,
                                                color = FlagentTheme.Text
                                            )
                                            Text(LocalizedStrings.updatedAt)
                                        if (sortColumn.value == "updatedAt") {
                                            Span({
                                                style {
                                                    fontSize(12.px)
                                                    color(FlagentTheme.Primary)
                                                }
                                            }) {
                                                Text(if (sortAscending.value) "↑" else "↓")
                                            }
                                        }
                                    }
                                }
                                Th({
                                    style {
                                        padding(16.px, 12.px)
                                        textAlign("center")
                                        fontWeight("600")
                                        fontSize(13.px)
                                        color(FlagentTheme.Text)
                                    }
                                }) { 
                                    Span({
                                        style {
                                            display(DisplayStyle.Flex)
                                            alignItems(AlignItems.Center)
                                            justifyContent(JustifyContent.Center)
                                            gap(6.px)
                                        }
                                    }) {
                                        Icon(
                                            name = "power_settings_new",
                                            size = 16.px,
                                            color = FlagentTheme.Text
                                        )
                                        Text(LocalizedStrings.status)
                                    }
                                }
                            }
                        }
                        Tbody {
                            filteredAndSortedFlags.forEachIndexed { index, flag ->
                                Tr({
                                    attr("class", "flag-row-hover")
                                    onClick { Router.navigateTo(Route.FlagDetail(flag.id)) }
                                    style {
                                        cursor("pointer")
                                        backgroundColor(if (index % 2 == 0) FlagentTheme.Background else FlagentTheme.BackgroundAlt)
                                        property("transition", "all 0.2s")
                                    }
                                    onMouseEnter {
                                        val element = it.target as org.w3c.dom.HTMLElement
                                        element.style.backgroundColor = FlagentTheme.BackgroundDark.toString()
                                        element.style.setProperty("box-shadow", "0 2px 8px ${FlagentTheme.Shadow}")
                                        element.style.transform = "translateY(-1px)"
                                    }
                                    onMouseLeave {
                                        val element = it.target as org.w3c.dom.HTMLElement
                                        element.style.backgroundColor = if (index % 2 == 0) FlagentTheme.Background.toString() else FlagentTheme.BackgroundAlt.toString()
                                        element.style.setProperty("box-shadow", "none")
                                        element.style.transform = "translateY(0)"
                                    }
                                }) {
                                    Td({
                                        style {
                                            padding(14.px, 12.px)
                                            textAlign("left")
                                            fontWeight("600")
                                            color(FlagentTheme.Primary)
                                        }
                                    }) { 
                                        Span({
                                            style {
                                                display(DisplayStyle.InlineBlock)
                                                padding(4.px, 8.px)
                                                backgroundColor(FlagentTheme.PrimaryLight)
                                                color(FlagentTheme.PrimaryDark)
                                                borderRadius(4.px)
                                                fontSize(12.px)
                                            }
                                        }) {
                                            Text("#${flag.id}")
                                        }
                                    }
                                    Td({
                                        style {
                                            padding(14.px, 12.px)
                                            maxWidth(300.px)
                                        }
                                    }) { 
                                        Div({
                                            style {
                                                fontWeight("500")
                                                color(FlagentTheme.Text)
                                                marginBottom(4.px)
                                            }
                                        }) {
                                            Text(flag.description ?: "No description")
                                        }
                                        if (flag.key.isNotEmpty()) {
                                            Div({
                                                style {
                                                    fontSize(12.px)
                                                    color(FlagentTheme.TextLight)
                                                    fontFamily("monospace")
                                                }
                                            }) {
                                                Text(flag.key)
                                            }
                                        }
                                    }
                                    Td({
                                        style {
                                            padding(14.px, 12.px)
                                        }
                                    }) {
                                        if (flag.tags.isEmpty()) {
                                            Span({
                                                style {
                                                    color(FlagentTheme.TextLighter)
                                                    fontSize(12.px)
                                                    fontStyle("italic")
                                                }
                                            }) {
                                                Text(LocalizedStrings.noTags)
                                            }
                                        } else {
                                            flag.tags.forEach { tag ->
                                                Span({
                                                    style {
                                                        display(DisplayStyle.InlineBlock)
                                                        padding(4.px, 10.px)
                                                        margin(2.px, 4.px)
                                                        backgroundColor(FlagentTheme.Accent)
                                                        color(FlagentTheme.Background)
                                                        borderRadius(12.px)
                                                        fontSize(11.px)
                                                        fontWeight("500")
                                                        property("box-shadow", "0 1px 2px ${FlagentTheme.Shadow}")
                                                    }
                                                }) {
                                                    Text(tag.value)
                                                }
                                            }
                                        }
                                    }
                                    Td({
                                        style {
                                            padding(14.px, 12.px)
                                            color(FlagentTheme.TextLight)
                                            fontSize(13.px)
                                        }
                                    }) { Text(flag.updatedBy ?: "—") }
                                    Td({
                                        style {
                                            padding(14.px, 12.px)
                                            color(FlagentTheme.TextLight)
                                            fontSize(13.px)
                                        }
                                    }) { Text(flag.updatedAt?.split(".")?.get(0) ?: "—") }
                                    Td({
                                        style {
                                            padding(14.px, 12.px)
                                            textAlign("center")
                                        }
                                    }) {
                                        Span({
                                            style {
                                                display(DisplayStyle.InlineBlock)
                                                padding(6.px, 12.px)
                                                borderRadius(16.px)
                                                fontSize(12.px)
                                                fontWeight("600")
                                                backgroundColor(if (flag.enabled) FlagentTheme.Success else FlagentTheme.Error)
                                                color(FlagentTheme.Background)
                                                property("box-shadow", "0 1px 3px ${FlagentTheme.Shadow}")
                                            }
                                        }) {
                                            Span({
                                                style {
                                                    display(DisplayStyle.Flex)
                                                    alignItems(AlignItems.Center)
                                                    gap(4.px)
                                                }
                                            }) {
                                                Icon(
                                                    name = if (flag.enabled) "check_circle" else "cancel",
                                                    size = 14.px,
                                                    color = FlagentTheme.Background
                                                )
                                                Text(if (flag.enabled) "ON" else "OFF")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Deleted Flags section
            Div({
                style {
                    marginTop(40.px)
                    padding(20.px)
                    backgroundColor(FlagentTheme.Background)
                    border {
                        width(1.px)
                        style(LineStyle.Solid)
                        color(FlagentTheme.Border)
                    }
                    borderRadius(8.px)
                    property("box-shadow", "0 2px 4px ${FlagentTheme.Shadow}")
                }
            }) {
                Button({
                    onClick {
                        showDeletedFlags.value = !showDeletedFlags.value
                        if (showDeletedFlags.value) {
                            loadDeletedFlags()
                        }
                    }
                    style {
                        padding(12.px, 20.px)
                        backgroundColor(FlagentTheme.Neutral)
                        color(FlagentTheme.Background)
                        border {
                            width(0.px)
                            style(LineStyle.None)
                        }
                        borderRadius(6.px)
                        cursor("pointer")
                        marginBottom(15.px)
                        fontWeight("600")
                        fontSize(14.px)
                        display(DisplayStyle.Flex)
                        alignItems(AlignItems.Center)
                        gap(8.px)
                        property("transition", "all 0.2s")
                    }
                    onMouseEnter {
                        val element = it.target as org.w3c.dom.HTMLElement
                        element.style.backgroundColor = FlagentTheme.NeutralLight.toString()
                                                    element.style.setProperty("box-shadow", "0 2px 4px ${FlagentTheme.Shadow}")
                    }
                    onMouseLeave {
                        val element = it.target as org.w3c.dom.HTMLElement
                        element.style.backgroundColor = FlagentTheme.Neutral.toString()
                        element.style.setProperty("box-shadow", "none")
                    }
                }) {
                    Icon(
                        name = if (showDeletedFlags.value) "expand_more" else "chevron_right",
                        size = 20.px,
                        color = FlagentTheme.Background
                    )
                    Text(if (showDeletedFlags.value) LocalizedStrings.deletedFlags else LocalizedStrings.showDeletedFlags)
                }

                if (showDeletedFlags.value) {
                    if (deletedFlagsLoading.value) {
                        Spinner()
                    } else if (deletedFlags.isEmpty()) {
                        Div({
                            style {
                                padding(30.px)
                                textAlign("center")
                                color(FlagentTheme.TextLight)
                                backgroundColor(FlagentTheme.BackgroundAlt)
                                borderRadius(6.px)
                            }
                        }) {
                            Icon(
                                name = "delete_outline",
                                size = 40.px,
                                color = FlagentTheme.TextLighter
                            )
                            Div({
                                style {
                                    fontSize(14.px)
                                }
                            }) {
                                Text(LocalizedStrings.noDeletedFlags)
                            }
                        }
                    } else {
                        Div({
                            style {
                                backgroundColor(FlagentTheme.BackgroundAlt)
                                border {
                                    width(1.px)
                                    style(LineStyle.Solid)
                                    color(FlagentTheme.Border)
                                }
                                borderRadius(8.px)
                                overflow("hidden")
                                overflowX("auto")
                            }
                        }) {
                            Table({
                                style {
                                    width(100.percent)
                                    property("border-collapse", "collapse")
                                    property("min-width", "600px")
                                }
                            }) {
                            Thead {
                                Tr({
                                    style {
                                        backgroundColor(FlagentTheme.BackgroundAlt)
                                    }
                                }) {
                                    Th({
                                        style {
                                            padding(10.px)
                                            border {
                                                width(1.px)
                                                style(LineStyle.Solid)
                                                color(FlagentTheme.Border)
                                            }
                                        }
                                    }) { Text(LocalizedStrings.flagId) }
                                    Th({
                                        style {
                                            padding(10.px)
                                            border {
                                                width(1.px)
                                                style(LineStyle.Solid)
                                                color(FlagentTheme.Border)
                                            }
                                        }
                                    }) { Text(LocalizedStrings.description) }
                                    Th({
                                        style {
                                            padding(10.px)
                                            border {
                                                width(1.px)
                                                style(LineStyle.Solid)
                                                color(FlagentTheme.Border)
                                            }
                                        }
                                    }) { Text(LocalizedStrings.tags) }
                                    Th({
                                        style {
                                            padding(10.px)
                                            border {
                                                width(1.px)
                                                style(LineStyle.Solid)
                                                color(FlagentTheme.Border)
                                            }
                                        }
                                    }) { Text(LocalizedStrings.lastUpdatedBy) }
                                    Th({
                                        style {
                                            padding(10.px)
                                            border {
                                                width(1.px)
                                                style(LineStyle.Solid)
                                                color(FlagentTheme.Border)
                                            }
                                        }
                                    }) { Text(LocalizedStrings.updatedAtUtc) }
                                    Th({
                                        style {
                                            padding(10.px)
                                            textAlign("center")
                                            border {
                                                width(1.px)
                                                style(LineStyle.Solid)
                                                color(FlagentTheme.Border)
                                            }
                                        }
                                    }) { Text(LocalizedStrings.action) }
                                }
                            }
                            Tbody {
                                deletedFlags.forEach { flag ->
                                    Tr({
                                        style {
                                            border {
                                                width(1.px)
                                                style(LineStyle.Solid)
                                                color(FlagentTheme.Border)
                                            }
                                        }
                                    }) {
                                        Td({
                                            style {
                                                padding(10.px)
                                                textAlign("center")
                                                border {
                                                    width(1.px)
                                                    style(LineStyle.Solid)
                                                    color(FlagentTheme.Border)
                                                }
                                            }
                                        }) { Text(flag.id.toString()) }
                                        Td({
                                            style {
                                                padding(10.px)
                                                border {
                                                    width(1.px)
                                                    style(LineStyle.Solid)
                                                    color(FlagentTheme.Border)
                                                }
                                            }
                                        }) { Text(flag.description ?: "") }
                                        Td({
                                            style {
                                                padding(10.px)
                                                border {
                                                    width(1.px)
                                                    style(LineStyle.Solid)
                                                    color(FlagentTheme.Border)
                                                }
                                            }
                                        }) {
                                            if (flag.tags.isEmpty()) {
                                                Text("")
                                            } else {
                                                flag.tags.forEach { tag ->
                                                    Span({
                                                        style {
                                                            display(DisplayStyle.InlineBlock)
                                                            padding(2.px, 8.px)
                                                            margin(2.px, 5.px)
                                                            backgroundColor(FlagentTheme.Accent)
                                                            color(FlagentTheme.Background)
                                                            borderRadius(3.px)
                                                            fontSize(12.px)
                                                        }
                                                    }) {
                                                        Text(tag.value)
                                                    }
                                                }
                                            }
                                        }
                                        Td({
                                            style {
                                                padding(10.px)
                                                border {
                                                    width(1.px)
                                                    style(LineStyle.Solid)
                                                    color(FlagentTheme.Border)
                                                }
                                            }
                                        }) { Text(flag.updatedBy ?: "") }
                                        Td({
                                            style {
                                                padding(10.px)
                                                border {
                                                    width(1.px)
                                                    style(LineStyle.Solid)
                                                    color(FlagentTheme.Border)
                                                }
                                            }
                                        }) { Text(flag.updatedAt?.split(".")?.get(0) ?: "") }
                                        Td({
                                            style {
                                                padding(14.px, 12.px)
                                                textAlign("center")
                                            }
                                        }) {
                                            Button({
                                                onClick { restoreFlag(flag) }
                                                style {
                                                    padding(8.px, 16.px)
                                                    backgroundColor(FlagentTheme.Success)
                                                    color(FlagentTheme.Background)
                                                    border {
                                                        width(0.px)
                                                        style(LineStyle.None)
                                                    }
                                                    borderRadius(6.px)
                                                    cursor("pointer")
                                                    fontWeight("600")
                                                    fontSize(13.px)
                                                    property("transition", "all 0.2s")
                                                    property("box-shadow", "0 2px 4px ${FlagentTheme.Shadow}")
                                                }
                                                onMouseEnter {
                                                    val element = it.target as org.w3c.dom.HTMLElement
                                                    element.style.transform = "translateY(-1px)"
                                                    element.style.setProperty("box-shadow", "0 4px 6px ${FlagentTheme.ShadowHover}")
                                                }
                                                onMouseLeave {
                                                    val element = it.target as org.w3c.dom.HTMLElement
                                                    element.style.transform = "translateY(0)"
                                                    element.style.setProperty("box-shadow", "0 2px 4px ${FlagentTheme.Shadow}")
                                                }
                                            }) {
                                                Icon(
                                                    name = "restore",
                                                    size = 18.px,
                                                    color = FlagentTheme.Background
                                                )
                                                Text(LocalizedStrings.restore)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    }
}
