package flagent.frontend.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import flagent.api.model.CreateFlagRequest
import flagent.api.model.FlagResponse
import flagent.api.model.TagResponse
import flagent.frontend.api.ApiClient
import flagent.frontend.components.common.ConfirmDialog
import flagent.frontend.components.common.Pagination
import flagent.frontend.config.AppConfig
import flagent.frontend.i18n.LocalizedStrings
import flagent.frontend.navigation.Route
import flagent.frontend.navigation.Router
import flagent.frontend.service.RealtimeService
import flagent.frontend.state.LocalGlobalState
import flagent.frontend.state.LocalThemeMode
import flagent.frontend.state.ThemeMode
import flagent.frontend.state.Notification
import flagent.frontend.state.NotificationType
import flagent.frontend.theme.FlagentTheme
import flagent.frontend.util.ErrorHandler
import kotlinx.browser.localStorage
import kotlinx.browser.window
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.FlexDirection
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.FlexWrap
import org.jetbrains.compose.web.css.JustifyContent
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.alignItems
import org.jetbrains.compose.web.css.backgroundColor
import org.jetbrains.compose.web.css.border
import org.jetbrains.compose.web.css.borderRadius
import org.jetbrains.compose.web.css.color
import org.jetbrains.compose.web.css.cursor
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.flex
import org.jetbrains.compose.web.css.flexWrap
import org.jetbrains.compose.web.css.fontFamily
import org.jetbrains.compose.web.css.fontSize
import org.jetbrains.compose.web.css.fontStyle
import org.jetbrains.compose.web.css.fontWeight
import org.jetbrains.compose.web.css.gap
import org.jetbrains.compose.web.css.justifyContent
import org.jetbrains.compose.web.css.margin
import org.jetbrains.compose.web.css.marginBottom
import org.jetbrains.compose.web.css.marginTop
import org.jetbrains.compose.web.css.maxWidth
import org.jetbrains.compose.web.css.minWidth
import org.jetbrains.compose.web.css.opacity
import org.jetbrains.compose.web.css.overflow
import org.jetbrains.compose.web.css.overflowX
import org.jetbrains.compose.web.css.padding
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.style
import org.jetbrains.compose.web.css.textAlign
import org.jetbrains.compose.web.css.textDecoration
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H3
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Option
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Select
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Table
import org.jetbrains.compose.web.dom.Tbody
import org.jetbrains.compose.web.dom.Td
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.Th
import org.jetbrains.compose.web.dom.Thead
import org.jetbrains.compose.web.dom.Tr

/**
 * FlagsList component - displays list of all flags
 */
private const val PAGE_SIZE = 25
private const val SAVED_VIEWS_KEY = "flagent_saved_views"

private fun encodeUriComponent(str: String): String = js("encodeURIComponent")(str).toString()
private fun decodeUriComponent(str: String): String = js("decodeURIComponent")(str).toString()

private data class SavedView(
    val name: String,
    val searchQuery: String,
    val keyFilter: String,
    val statusFilter: Boolean?,
    val tags: List<String>,
    val quickFilterExperiments: Boolean = false,
    val quickFilterWithSegments: Boolean = false,
    val groupByTag: Boolean = false
)

@Composable
private fun FlagTableRow(
    flag: FlagResponse,
    rowIndex: Int,
    themeMode: ThemeMode,
    selectedIds: MutableList<Int>,
    selectedTags: MutableList<String>
) {
    Tr({
        attr("class", "flag-row-hover")
        onClick { Router.navigateTo(Route.FlagDetail(flag.id)) }
        style {
            cursor("pointer")
            backgroundColor(if (rowIndex % 2 == 0) FlagentTheme.cardBg(themeMode) else FlagentTheme.inputBg(themeMode))
            property("transition", "all 0.2s")
        }
        onMouseEnter {
            val element = it.target as org.w3c.dom.HTMLElement
            element.style.backgroundColor = FlagentTheme.inputBg(themeMode).toString()
            element.style.setProperty("box-shadow", "0 2px 8px ${FlagentTheme.Shadow}")
            element.style.transform = "translateY(-1px)"
        }
        onMouseLeave {
            val element = it.target as org.w3c.dom.HTMLElement
            element.style.backgroundColor =
                if (rowIndex % 2 == 0) FlagentTheme.cardBg(themeMode).toString() else FlagentTheme.inputBg(themeMode).toString()
            element.style.setProperty("box-shadow", "none")
            element.style.transform = "translateY(0)"
        }
    }) {
        Td({
            style { padding(14.px, 12.px); textAlign("center"); width(40.px) }
            onClick { (it as org.w3c.dom.events.Event).stopPropagation() }
        }) {
            Input(InputType.Checkbox) {
                checked(flag.id in selectedIds)
                onChange { event ->
                    if ((event.target as org.w3c.dom.HTMLInputElement).checked) selectedIds.add(flag.id)
                    else selectedIds.remove(flag.id)
                }
                style { cursor("pointer") }
            }
        }
        Td({ style { padding(14.px, 12.px); textAlign("left"); fontWeight("600"); color(FlagentTheme.Primary) } }) {
            Span({ style { display(DisplayStyle.InlineBlock); padding(4.px, 8.px); backgroundColor(FlagentTheme.PrimaryLight); color(FlagentTheme.PrimaryDark); borderRadius(4.px); fontSize(12.px) } }) {
                Text("#${flag.id}")
            }
        }
        Td({ style { padding(14.px, 12.px); maxWidth(300.px) } }) {
            Div({ style { fontWeight("500"); color(FlagentTheme.Text); marginBottom(4.px) } }) { Text(flag.description) }
            if (flag.key.isNotEmpty()) {
                Div({ style { fontSize(12.px); color(FlagentTheme.textLight(themeMode)); fontFamily("monospace") } }) {
                    Text(flag.key)
                }
            }
        }
        Td({ style { padding(14.px, 12.px) } }) {
            if (flag.tags.isEmpty()) {
                Span({ style { color(FlagentTheme.textLight(themeMode)); fontSize(12.px); fontStyle("italic") } }) {
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
                            color(Color.white)
                            borderRadius(12.px)
                            fontSize(11.px)
                            fontWeight("500")
                            cursor("pointer")
                            property("box-shadow", "0 1px 2px ${FlagentTheme.Shadow}")
                        }
                        onClick { if (tag.value !in selectedTags) selectedTags.add(tag.value) }
                    }) {
                        Text(tag.value)
                    }
                }
            }
        }
        Td({ style { padding(14.px, 12.px); color(FlagentTheme.textLight(themeMode)); fontSize(13.px) } }) {
            Text(flag.updatedBy ?: "—")
        }
        Td({ style { padding(14.px, 12.px); color(FlagentTheme.textLight(themeMode)); fontSize(13.px) } }) {
            Text(flag.updatedAt?.split(".")?.get(0) ?: "—")
        }
        Td({ style { padding(14.px, 12.px); textAlign("center") } }) {
            Span({
                style {
                    display(DisplayStyle.InlineBlock)
                    padding(6.px, 12.px)
                    borderRadius(16.px)
                    fontSize(12.px)
                    fontWeight("600")
                    backgroundColor(if (flag.enabled) FlagentTheme.Success else FlagentTheme.Error)
                    color(Color.white)
                    property("box-shadow", "0 1px 3px ${FlagentTheme.Shadow}")
                }
            }) {
                Span({ style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); gap(4.px) } }) {
                    Icon(
                        name = if (flag.enabled) "check_circle" else "cancel",
                        size = 14.px,
                        color = FlagentTheme.cardBg(themeMode)
                    )
                    Text(if (flag.enabled) "ON" else "OFF")
                }
            }
        }
    }
}

@Composable
private fun GroupedFlagsTableBody(
    groupedByTag: Map<String, List<FlagResponse>>,
    themeMode: ThemeMode,
    selectedIds: MutableList<Int>,
    selectedTags: MutableList<String>
) {
    val noTagsKey = "\u0000"
    groupedByTag.entries.forEach { entry ->
        val tagKey = entry.key
        val flagsInGroup = entry.value
        Tr({
            style {
                backgroundColor(FlagentTheme.inputBg(themeMode))
                property("border-bottom", "2px solid ${FlagentTheme.cardBorder(themeMode)}")
            }
        }) {
            Td({
                attr("colspan", "7")
                style {
                    padding(12.px, 16.px)
                    fontWeight("600")
                    fontSize(13.px)
                    color(FlagentTheme.Text)
                }
            }) {
                Span({
                    style {
                        display(DisplayStyle.Flex)
                        alignItems(AlignItems.Center)
                        gap(8.px)
                    }
                }) {
                    Icon("local_offer", size = 18.px, color = FlagentTheme.Primary)
                    Text(if (tagKey == noTagsKey) LocalizedStrings.noTagsGroup else tagKey)
                }
            }
        }
        flagsInGroup.forEachIndexed { idx: Int, f: FlagResponse ->
            FlagTableRow(
                flag = f,
                rowIndex = idx,
                themeMode = themeMode,
                selectedIds = selectedIds,
                selectedTags = selectedTags
            )
        }
    }
}

@Composable
fun FlagsList() {
    val themeMode = LocalThemeMode.current
    val globalState = LocalGlobalState.current
    val flags = remember { mutableStateListOf<FlagResponse>() }
    val totalCount = remember { mutableStateOf(0L) }
    val currentPage = remember { mutableStateOf(1) }
    val deletedFlags = remember { mutableStateListOf<FlagResponse>() }
    val loading = remember { mutableStateOf(true) }
    val deletedFlagsLoading = remember { mutableStateOf(false) }
    val error = remember { mutableStateOf<String?>(null) }
    val searchQuery = remember { mutableStateOf("") }
    val keyFilter = remember { mutableStateOf("") }
    val statusFilter =
        remember { mutableStateOf<Boolean?>(null) } // null = all, true = enabled, false = disabled
    val selectedTags = remember { mutableStateListOf<String>() }
    val availableTags = remember { mutableStateListOf<TagResponse>() }
    val sortColumn = remember { mutableStateOf<String?>(null) }
    val sortAscending = remember { mutableStateOf(true) }
    val showDeletedFlags = remember { mutableStateOf(false) }
    val newFlagDescription = remember { mutableStateOf("") }
    val creatingFlag = remember { mutableStateOf(false) }
    val selectedIds = remember { mutableStateListOf<Int>() }
    val bulkConfirmOpen = remember { mutableStateOf(false) }
    val bulkConfirmEnabled = remember { mutableStateOf(true) }
    val savedViewsList = remember { mutableStateListOf<SavedView>() }
    val showSaveViewDialog = remember { mutableStateOf(false) }
    val saveViewName = remember { mutableStateOf("") }
    val quickFilterExperiments = remember { mutableStateOf(false) }
    val quickFilterWithSegments = remember { mutableStateOf(false) }
    val groupByTag = remember { mutableStateOf(false) }
    val allFlagsForClientFilter = remember { mutableStateListOf<FlagResponse>() }
    val lastFetchFilterKey = remember { mutableStateOf("") }

    fun loadFlags() {
        CoroutineScope(Dispatchers.Main).launch {
            loading.value = true
            error.value = null
            val needsPreload = quickFilterExperiments.value || quickFilterWithSegments.value
            val filterKey = "${statusFilter.value}|${searchQuery.value}|${keyFilter.value}|${selectedTags.joinToString(",")}|${quickFilterExperiments.value}|${quickFilterWithSegments.value}"
            ErrorHandler.withErrorHandling(
                block = {
                    if (needsPreload) {
                        if (allFlagsForClientFilter.isNotEmpty() && lastFetchFilterKey.value == filterKey) {
                            val offset = (currentPage.value - 1) * PAGE_SIZE
                            flags.clear()
                            flags.addAll(allFlagsForClientFilter.drop(offset).take(PAGE_SIZE))
                            totalCount.value = allFlagsForClientFilter.size.toLong()
                        } else {
                            val (fetchedFlags, _) = ApiClient.getFlags(
                                limit = 500,
                                offset = 0,
                                enabled = statusFilter.value,
                                descriptionLike = searchQuery.value.takeIf { it.isNotBlank() },
                                key = keyFilter.value.takeIf { it.isNotBlank() },
                                tags = selectedTags.takeIf { it.isNotEmpty() }?.joinToString(","),
                                preload = true
                            )
                            var filtered = fetchedFlags
                            if (quickFilterExperiments.value) {
                                filtered = filtered.filter { it.variants.size >= 2 }
                            }
                            if (quickFilterWithSegments.value) {
                                filtered = filtered.filter { it.segments.isNotEmpty() }
                            }
                            allFlagsForClientFilter.clear()
                            allFlagsForClientFilter.addAll(filtered)
                            lastFetchFilterKey.value = filterKey
                            totalCount.value = filtered.size.toLong()
                            val offset = (currentPage.value - 1) * PAGE_SIZE
                            flags.clear()
                            flags.addAll(filtered.drop(offset).take(PAGE_SIZE))
                        }
                    } else {
                        allFlagsForClientFilter.clear()
                        lastFetchFilterKey.value = ""
                        val offset = (currentPage.value - 1) * PAGE_SIZE
                        val (fetchedFlags, total) = ApiClient.getFlags(
                            limit = PAGE_SIZE,
                            offset = offset,
                            enabled = statusFilter.value,
                            descriptionLike = searchQuery.value.takeIf { it.isNotBlank() },
                            key = keyFilter.value.takeIf { it.isNotBlank() },
                            tags = selectedTags.takeIf { it.isNotEmpty() }?.joinToString(",")
                        )
                        flags.clear()
                        flags.addAll(fetchedFlags)
                        totalCount.value = total
                    }
                },
                onError = { err ->
                    error.value = ErrorHandler.getUserMessage(err)
                }
            )
            loading.value = false
        }
    }

    fun loadTags() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val tags = ApiClient.getTags()
                availableTags.clear()
                availableTags.addAll(tags)
            } catch (_: Exception) { /* silent */ }
        }
    }

    fun loadSavedViews() {
        try {
            val json = localStorage.getItem(SAVED_VIEWS_KEY) ?: "[]"
            val arr = kotlinx.serialization.json.Json.parseToJsonElement(json).jsonArray
            savedViewsList.clear()
            arr.forEach { el ->
                val obj = el.jsonObject
                savedViewsList.add(SavedView(
                    name = obj["name"]?.jsonPrimitive?.content ?: "",
                    searchQuery = obj["searchQuery"]?.jsonPrimitive?.content ?: "",
                    keyFilter = obj["keyFilter"]?.jsonPrimitive?.content ?: "",
                    statusFilter = obj["statusFilter"]?.jsonPrimitive?.content?.let {
                        when (it) { "true" -> true; "false" -> false; else -> null }
                    },
                    tags = (obj["tags"]?.jsonArray ?: buildJsonArray { }).map {
                        it.jsonPrimitive.content
                    },
                    quickFilterExperiments = obj["quickFilterExperiments"]?.jsonPrimitive?.content?.toBoolean() ?: false,
                    quickFilterWithSegments = obj["quickFilterWithSegments"]?.jsonPrimitive?.content?.toBoolean() ?: false,
                    groupByTag = obj["groupByTag"]?.jsonPrimitive?.content?.toBoolean() ?: false
                ))
            }
        } catch (_: Exception) { /* silent */ }
    }

    fun saveCurrentView(name: String) {
        val view = SavedView(
            name = name,
            searchQuery = searchQuery.value,
            keyFilter = keyFilter.value,
            statusFilter = statusFilter.value,
            tags = selectedTags.toList(),
            quickFilterExperiments = quickFilterExperiments.value,
            quickFilterWithSegments = quickFilterWithSegments.value,
            groupByTag = groupByTag.value
        )
        savedViewsList.removeAll { it.name == name }
        savedViewsList.add(view)
        val json = buildJsonArray {
            savedViewsList.forEach { v ->
                add(buildJsonObject {
                    put("name", v.name)
                    put("searchQuery", v.searchQuery)
                    put("keyFilter", v.keyFilter)
                    put("statusFilter", v.statusFilter?.toString() ?: "null")
                    put("tags", buildJsonArray { v.tags.forEach { add(kotlinx.serialization.json.JsonPrimitive(it)) } })
                    put("quickFilterExperiments", v.quickFilterExperiments)
                    put("quickFilterWithSegments", v.quickFilterWithSegments)
                    put("groupByTag", v.groupByTag)
                })
            }
        }
        localStorage.setItem(SAVED_VIEWS_KEY, json.toString())
        showSaveViewDialog.value = false
        saveViewName.value = ""
    }

    fun applySavedView(view: SavedView) {
        searchQuery.value = view.searchQuery
        keyFilter.value = view.keyFilter
        statusFilter.value = view.statusFilter
        selectedTags.clear()
        selectedTags.addAll(view.tags)
        quickFilterExperiments.value = view.quickFilterExperiments
        quickFilterWithSegments.value = view.quickFilterWithSegments
        groupByTag.value = view.groupByTag
    }

    fun buildFiltersJson(): String {
        val obj = buildJsonObject {
            put("searchQuery", searchQuery.value)
            put("keyFilter", keyFilter.value)
            put("statusFilter", statusFilter.value?.toString() ?: "null")
            put("tags", buildJsonArray { selectedTags.forEach { add(kotlinx.serialization.json.JsonPrimitive(it)) } })
            put("quickFilterExperiments", quickFilterExperiments.value)
            put("quickFilterWithSegments", quickFilterWithSegments.value)
            put("groupByTag", groupByTag.value)
        }
        return kotlinx.serialization.json.Json.encodeToString(kotlinx.serialization.json.JsonElement.serializer(), obj)
    }

    fun buildShareUrl(): String {
        val base = "${window.location.origin}${Route.FlagsList.PATH}"
        val params = mutableListOf<String>()
        if (searchQuery.value.isNotBlank()) params.add("q=${encodeUriComponent(searchQuery.value)}")
        if (keyFilter.value.isNotBlank()) params.add("key=${encodeUriComponent(keyFilter.value)}")
        statusFilter.value?.let { params.add("status=$it") }
        if (selectedTags.isNotEmpty()) params.add("tags=${encodeUriComponent(selectedTags.joinToString(","))}")
        if (quickFilterExperiments.value) params.add("experiments=1")
        if (quickFilterWithSegments.value) params.add("segments=1")
        if (groupByTag.value) params.add("groupByTag=1")
        return if (params.isEmpty()) base else "$base?${params.joinToString("&")}"
    }

    fun copyToClipboard(text: String, onSuccess: () -> Unit) {
        try {
            val clip = js("navigator.clipboard")
            if (clip != null && clip != js("undefined")) {
                clip.writeText(text)
            }
            onSuccess()
        } catch (_: Throwable) {
            onSuccess()
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
                ApiClient.restoreFlag(flag.id)
                deletedFlags.removeAll { it.id == flag.id }
                loadFlags()
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
                currentPage.value = 1
                loadFlags()
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

    fun performBulkEnable(enabled: Boolean) {
        CoroutineScope(Dispatchers.Main).launch {
            bulkConfirmOpen.value = false
            loading.value = true
            error.value = null
            try {
                ApiClient.batchSetFlagEnabled(selectedIds.toList(), enabled)
                selectedIds.clear()
                loadFlags()
            } catch (e: Exception) {
                error.value = ErrorHandler.getUserMessage(ErrorHandler.handle(e))
            } finally {
                loading.value = false
            }
        }
    }

    val realtimeRefreshTrigger = remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        loadTags()
        loadSavedViews()
        val search = window.location.search
        if (Router.currentRoute == Route.FlagsList && search.isNotBlank()) {
            val params: Map<String, String?> = search.removePrefix("?").split("&").associate { part ->
                val parts = part.split("=", limit = 2)
                val k = parts[0]
                val v = parts.getOrNull(1)?.let { decodeUriComponent(it) }
                k to v
            }
            (params["q"] ?: "").takeIf { it.isNotBlank() }?.let { searchQuery.value = it }
            (params["key"] ?: "").takeIf { it.isNotBlank() }?.let { keyFilter.value = it }
            params["status"]?.let { statusFilter.value = it.toBooleanStrictOrNull() }
            (params["tags"] ?: "").takeIf { it.isNotBlank() }?.let { t ->
                selectedTags.clear()
                selectedTags.addAll(t.split(",").map { it.trim() }.filter { it.isNotBlank() })
            }
            if (params["experiments"] == "1") quickFilterExperiments.value = true
            if (params["segments"] == "1") quickFilterWithSegments.value = true
            if (params["groupByTag"] == "1") groupByTag.value = true
        }
    }

    LaunchedEffect(
        currentPage.value,
        searchQuery.value,
        keyFilter.value,
        statusFilter.value,
        selectedTags.joinToString(","),
        quickFilterExperiments.value,
        quickFilterWithSegments.value,
        realtimeRefreshTrigger.value
    ) {
        loadFlags()
    }

    LaunchedEffect(
        searchQuery.value,
        keyFilter.value,
        statusFilter.value,
        selectedTags.joinToString(","),
        quickFilterExperiments.value,
        quickFilterWithSegments.value
    ) {
        currentPage.value = 1
    }

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

    Div({
        style {
            marginTop(0.px)
            padding(0.px)
        }
    }) {
        // Create Flag Section
        Div({
            style {
                marginBottom(16.px)
                padding(16.px)
                backgroundColor(FlagentTheme.cardBg(themeMode))
                border {
                    width(1.px)
                    style(LineStyle.Solid)
                    color(FlagentTheme.cardBorder(themeMode))
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
                            color(FlagentTheme.cardBorder(themeMode))
                        }
                        borderRadius(6.px)
                        fontSize(14.px)
                        property("transition", "border-color 0.2s")
                    }
                    onFocus {
                        (it.target as org.w3c.dom.HTMLElement).style.borderColor =
                            FlagentTheme.Primary.toString()
                    }
                    onBlur {
                        (it.target as org.w3c.dom.HTMLElement).style.borderColor =
                            FlagentTheme.cardBorder(themeMode).toString()
                    }
                }
                Button({
                    onClick { createFlag() }
                    if (creatingFlag.value || newFlagDescription.value.isBlank()) {
                        attr("disabled", "true")
                    }
                    style {
                        padding(12.px, 20.px)
                        backgroundColor(
                            if (creatingFlag.value || newFlagDescription.value.isBlank()) {
                                FlagentTheme.inputBg(themeMode)
                            } else {
                                FlagentTheme.Success
                            }
                        )
                        color(Color.white)
                        border {
                            width(0.px)
                            style(LineStyle.None)
                        }
                        borderRadius(8.px)
                        cursor(if (creatingFlag.value || newFlagDescription.value.isBlank()) "not-allowed" else "pointer")
                        fontWeight("600")
                        fontSize(14.px)
                        property("transition", "all 0.3s cubic-bezier(0.4, 0, 0.2, 1)")
                        property(
                            "box-shadow",
                            if (creatingFlag.value || newFlagDescription.value.isBlank()) "none" else "0 4px 12px rgba(16, 185, 129, 0.3)"
                        )
                    }
                    onMouseEnter {
                        if (!creatingFlag.value && newFlagDescription.value.isNotBlank()) {
                            val element = it.target as org.w3c.dom.HTMLElement
                            element.style.transform = "translateY(-2px) scale(1.02)"
                            element.style.setProperty(
                                "box-shadow",
                                "0 6px 16px rgba(16, 185, 129, 0.4)"
                            )
                        }
                    }
                    onMouseLeave {
                        val element = it.target as org.w3c.dom.HTMLElement
                        element.style.transform = "translateY(0) scale(1)"
                        element.style.setProperty(
                            "box-shadow",
                            if (creatingFlag.value || newFlagDescription.value.isBlank()) "none" else "0 4px 12px rgba(16, 185, 129, 0.3)"
                        )
                    }
                }) {
                    Icon(
                        name = if (creatingFlag.value) "hourglass_empty" else "auto_awesome",
                        size = 18.px,
                        color = FlagentTheme.cardBg(themeMode)
                    )
                    Text(if (creatingFlag.value) LocalizedStrings.creating else LocalizedStrings.createFlag)
                }
                Select({
                    onChange { event ->
                        val selected = event.target.value
                        when (selected) {
                            "simple_boolean_flag" -> createFlag("simple_boolean_flag")
                            "" -> Router.navigateTo(Route.CreateFlag)
                        }
                        // Reset select to default value after selection
                        event.target.value = ""
                    }
                    style {
                        padding(12.px, 16.px)
                        border {
                            width(1.px)
                            style(LineStyle.Solid)
                            color(FlagentTheme.cardBorder(themeMode))
                        }
                        borderRadius(6.px)
                        fontSize(14.px)
                        backgroundColor(FlagentTheme.cardBg(themeMode))
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
                marginBottom(16.px)
                alignItems(AlignItems.Center)
                padding(12.px)
                backgroundColor(FlagentTheme.cardBg(themeMode))
                border {
                    width(1.px)
                    style(LineStyle.Solid)
                    color(FlagentTheme.cardBorder(themeMode))
                }
                borderRadius(8.px)
                property("box-shadow", "0 1px 3px ${FlagentTheme.Shadow}")
            }
        }) {
            Icon(
                name = "search",
                size = 20.px,
                color = FlagentTheme.textLight(themeMode)
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
                        color(FlagentTheme.cardBorder(themeMode))
                    }
                    borderRadius(6.px)
                    fontSize(14.px)
                    property("transition", "border-color 0.2s")
                }
                onFocus {
                    (it.target as org.w3c.dom.HTMLElement).style.borderColor =
                        FlagentTheme.Primary.toString()
                }
                onBlur {
                    (it.target as org.w3c.dom.HTMLElement).style.borderColor =
                        FlagentTheme.cardBorder(themeMode).toString()
                }
            }
            Input(InputType.Text) {
                attr("placeholder", "Key (exact)")
                value(keyFilter.value)
                onInput { event -> keyFilter.value = event.value }
                style {
                    minWidth(120.px)
                    padding(12.px, 16.px)
                    border {
                        width(1.px)
                        style(LineStyle.Solid)
                        color(FlagentTheme.cardBorder(themeMode))
                    }
                    borderRadius(6.px)
                    fontSize(14.px)
                    property("transition", "border-color 0.2s")
                }
                onFocus {
                    (it.target as org.w3c.dom.HTMLElement).style.borderColor =
                        FlagentTheme.Primary.toString()
                }
                onBlur {
                    (it.target as org.w3c.dom.HTMLElement).style.borderColor =
                        FlagentTheme.cardBorder(themeMode).toString()
                }
            }
            Select({
                onChange { event ->
                    val selected = event.target.value
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
                        color(FlagentTheme.cardBorder(themeMode))
                    }
                    borderRadius(6.px)
                    fontSize(14.px)
                    backgroundColor(FlagentTheme.cardBg(themeMode))
                    cursor("pointer")
                    minWidth(140.px)
                }
            }) {
                Option(value = "all") { Text(LocalizedStrings.allStatus) }
                Option(value = "enabled") { Text(LocalizedStrings.enabledFlags) }
                Option(value = "disabled") { Text(LocalizedStrings.disabledFlags) }
            }
            Span({
                style {
                    display(DisplayStyle.Flex)
                    alignItems(AlignItems.Center)
                    gap(6.px)
                }
            }) {
                Span({
                    style {
                        padding(6.px, 12.px)
                        backgroundColor(if (quickFilterExperiments.value) FlagentTheme.Primary else FlagentTheme.inputBg(themeMode))
                        color(if (quickFilterExperiments.value) Color.white else FlagentTheme.Text)
                        border { width(1.px); style(LineStyle.Solid); color(if (quickFilterExperiments.value) FlagentTheme.Primary else FlagentTheme.cardBorder(themeMode)) }
                        borderRadius(16.px)
                        fontSize(12.px)
                        cursor("pointer")
                    }
                    onClick { quickFilterExperiments.value = !quickFilterExperiments.value }
                }) {
                    Text(LocalizedStrings.quickFilterExperiments)
                }
                Span({
                    style {
                        padding(6.px, 12.px)
                        backgroundColor(if (quickFilterWithSegments.value) FlagentTheme.Primary else FlagentTheme.inputBg(themeMode))
                        color(if (quickFilterWithSegments.value) Color.white else FlagentTheme.Text)
                        border { width(1.px); style(LineStyle.Solid); color(if (quickFilterWithSegments.value) FlagentTheme.Primary else FlagentTheme.cardBorder(themeMode)) }
                        borderRadius(16.px)
                        fontSize(12.px)
                        cursor("pointer")
                    }
                    onClick { quickFilterWithSegments.value = !quickFilterWithSegments.value }
                }) {
                    Text(LocalizedStrings.quickFilterWithSegments)
                }
                Span({
                    style {
                        padding(6.px, 12.px)
                        backgroundColor(if (groupByTag.value) FlagentTheme.Primary else FlagentTheme.inputBg(themeMode))
                        color(if (groupByTag.value) Color.white else FlagentTheme.Text)
                        border { width(1.px); style(LineStyle.Solid); color(if (groupByTag.value) FlagentTheme.Primary else FlagentTheme.cardBorder(themeMode)) }
                        borderRadius(16.px)
                        fontSize(12.px)
                        cursor("pointer")
                    }
                    onClick { groupByTag.value = !groupByTag.value }
                }) {
                    Text(LocalizedStrings.groupByTags)
                }
            }
            Div({
                style {
                    display(DisplayStyle.Flex)
                    flexWrap(FlexWrap.Wrap)
                    gap(6.px)
                    alignItems(AlignItems.Center)
                }
            }) {
                if (selectedTags.isNotEmpty()) {
                    selectedTags.forEach { tagValue ->
                        Span({
                            style {
                                display(DisplayStyle.Flex)
                                alignItems(AlignItems.Center)
                                gap(4.px)
                                padding(6.px, 10.px)
                                backgroundColor(FlagentTheme.PrimaryLight)
                                color(FlagentTheme.PrimaryDark)
                                borderRadius(16.px)
                                fontSize(12.px)
                                cursor("pointer")
                            }
                            onClick { selectedTags.remove(tagValue) }
                        }) {
                            Text(tagValue)
                            Icon("close", size = 14.px, color = FlagentTheme.PrimaryDark)
                        }
                    }
                }
                Select({
                    onChange { event ->
                        val name = event.target.value
                        if (name.isNotBlank()) {
                            savedViewsList.find { it.name == name }?.let { applySavedView(it) }
                        }
                        event.target.value = ""
                    }
                    style {
                        padding(8.px, 12.px)
                        border { width(1.px); style(LineStyle.Solid); color(FlagentTheme.cardBorder(themeMode)) }
                        borderRadius(6.px)
                        fontSize(13.px)
                        backgroundColor(FlagentTheme.cardBg(themeMode))
                        cursor("pointer")
                        minWidth(120.px)
                    }
                }) {
                    Option(value = "") { Text(LocalizedStrings.savedViews) }
                    savedViewsList.forEach { v ->
                        Option(value = v.name) { Text(v.name) }
                    }
                }
                Button({
                    onClick { showSaveViewDialog.value = true }
                    style {
                        padding(8.px, 12.px)
                        backgroundColor(FlagentTheme.inputBg(themeMode))
                        color(FlagentTheme.Text)
                        border { width(1.px); style(LineStyle.Solid); color(FlagentTheme.cardBorder(themeMode)) }
                        borderRadius(6.px)
                        cursor("pointer")
                        fontSize(13.px)
                    }
                }) {
                    Text(LocalizedStrings.saveCurrentView)
                }
                availableTags.filter { it.value !in selectedTags }.take(8).forEach { tag ->
                    Span({
                        style {
                            display(DisplayStyle.InlineBlock)
                            padding(6.px, 10.px)
                            backgroundColor(FlagentTheme.inputBg(themeMode))
                            color(FlagentTheme.Text)
                            border { width(1.px); style(LineStyle.Solid); color(FlagentTheme.cardBorder(themeMode)) }
                            borderRadius(16.px)
                            fontSize(12.px)
                            cursor("pointer")
                        }
                        onClick { selectedTags.add(tag.value) }
                    }) {
                        Text("+ ${tag.value}")
                    }
                }
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
                color(FlagentTheme.textLight(themeMode))
            }
        }) {
            Icon("info", size = 16.px, color = FlagentTheme.textLight(themeMode))
            Text(LocalizedStrings.flagsListKeyHint)
        }

        if (selectedIds.isNotEmpty()) {
            Div({
                style {
                    display(DisplayStyle.Flex)
                    alignItems(AlignItems.Center)
                    gap(12.px)
                    padding(12.px, 16.px)
                    marginBottom(12.px)
                    backgroundColor(FlagentTheme.PrimaryLight)
                    borderRadius(8.px)
                    border { width(1.px); style(LineStyle.Solid); color(FlagentTheme.Primary) }
                }
            }) {
                Span({
                    style {
                        fontSize(14.px)
                        fontWeight("600")
                        color(FlagentTheme.PrimaryDark)
                    }
                }) {
                    Text("${selectedIds.size} ${LocalizedStrings.featureFlags.lowercase()} selected")
                }
                Button({
                    onClick {
                        bulkConfirmEnabled.value = true
                        bulkConfirmOpen.value = true
                    }
                    style {
                        padding(8.px, 16.px)
                        backgroundColor(FlagentTheme.Success)
                        color(Color.white)
                        border(0.px)
                        borderRadius(6.px)
                        cursor("pointer")
                        fontSize(13.px)
                        fontWeight("600")
                    }
                }) {
                    Text(LocalizedStrings.enableSelected)
                }
                Button({
                    onClick {
                        bulkConfirmEnabled.value = false
                        bulkConfirmOpen.value = true
                    }
                    style {
                        padding(8.px, 16.px)
                        backgroundColor(FlagentTheme.Error)
                        color(Color.white)
                        border(0.px)
                        borderRadius(6.px)
                        cursor("pointer")
                        fontSize(13.px)
                        fontWeight("600")
                    }
                }) {
                    Text(LocalizedStrings.disableSelected)
                }
                Button({
                    onClick { selectedIds.clear() }
                    style {
                        padding(8.px, 16.px)
                        backgroundColor(FlagentTheme.cardBg(themeMode))
                        color(FlagentTheme.Text)
                        border(1.px, LineStyle.Solid, FlagentTheme.cardBorder(themeMode))
                        borderRadius(6.px)
                        cursor("pointer")
                        fontSize(13.px)
                    }
                }) {
                    Text(LocalizedStrings.clearSelection)
                }
            }
        }

        if (showSaveViewDialog.value) {
            Modal(title = LocalizedStrings.saveCurrentView, onClose = {
                showSaveViewDialog.value = false
                saveViewName.value = ""
            }) {
                Div({
                    style {
                        padding(16.px)
                        display(DisplayStyle.Flex)
                        property("flex-direction", "column")
                        gap(12.px)
                    }
                }) {
                    Input(InputType.Text) {
                        attr("placeholder", LocalizedStrings.viewName)
                        value(saveViewName.value)
                        onInput { saveViewName.value = it.value }
                        style {
                            padding(10.px, 14.px)
                            border { width(1.px); style(LineStyle.Solid); color(FlagentTheme.cardBorder(themeMode)) }
                            borderRadius(6.px)
                            fontSize(14.px)
                        }
                    }
                    Button({
                        onClick {
                            if (saveViewName.value.isNotBlank()) saveCurrentView(saveViewName.value)
                        }
                        style {
                            padding(10.px, 20.px)
                            backgroundColor(FlagentTheme.Primary)
                            color(Color.white)
                            border(0.px)
                            borderRadius(6.px)
                            cursor("pointer")
                            fontWeight("600")
                        }
                    }) {
                        Text(LocalizedStrings.saveCurrentView)
                    }
                    Div({
                        style {
                            display(DisplayStyle.Flex)
                            gap(8.px)
                            marginTop(8.px)
                            property("padding-top", "12px")
                            property("border-top", "1px solid ${FlagentTheme.cardBorder(themeMode)}")
                        }
                    }) {
                        Button({
                            onClick {
                                copyToClipboard(buildFiltersJson()) {
                                    globalState.addNotification(Notification(message = LocalizedStrings.filtersCopied, type = NotificationType.SUCCESS))
                                }
                            }
                            style {
                                padding(8.px, 14.px)
                                backgroundColor(FlagentTheme.inputBg(themeMode))
                                color(FlagentTheme.Text)
                                border { width(1.px); style(LineStyle.Solid); color(FlagentTheme.cardBorder(themeMode)) }
                                borderRadius(6.px)
                                cursor("pointer")
                                fontSize(13.px)
                            }
                        }) {
                            Text(LocalizedStrings.exportFilters)
                        }
                        Button({
                            onClick {
                                copyToClipboard(buildShareUrl()) {
                                    globalState.addNotification(Notification(message = LocalizedStrings.filtersCopied, type = NotificationType.SUCCESS))
                                }
                            }
                            style {
                                padding(8.px, 14.px)
                                backgroundColor(FlagentTheme.inputBg(themeMode))
                                color(FlagentTheme.Text)
                                border { width(1.px); style(LineStyle.Solid); color(FlagentTheme.cardBorder(themeMode)) }
                                borderRadius(6.px)
                                cursor("pointer")
                                fontSize(13.px)
                            }
                        }) {
                            Text(LocalizedStrings.copyShareLink)
                        }
                    }
                }
            }
        }

        ConfirmDialog(
            isOpen = bulkConfirmOpen.value,
            title = if (bulkConfirmEnabled.value) LocalizedStrings.enableSelected else LocalizedStrings.disableSelected,
            message = if (bulkConfirmEnabled.value) LocalizedStrings.bulkEnableConfirm else LocalizedStrings.bulkDisableConfirm,
            confirmLabel = if (bulkConfirmEnabled.value) LocalizedStrings.enableSelected else LocalizedStrings.disableSelected,
            cancelLabel = LocalizedStrings.cancel,
            onConfirm = { performBulkEnable(bulkConfirmEnabled.value) },
            onCancel = { bulkConfirmOpen.value = false }
        )

        if (loading.value) {
            Spinner()
        } else if (error.value != null) {
            val err = error.value!!
            val isTenantOrApiKeyError = err.contains("tenant", ignoreCase = true) ||
                    err.contains("Create tenant", ignoreCase = true) ||
                    err.contains("X-API-Key", ignoreCase = true)
            if (isTenantOrApiKeyError) SideEffect { flagent.frontend.state.BackendOnboardingState.setBackendNeedsTenantOrAuth() }
            Div({
                style {
                    color(FlagentTheme.Error)
                    padding(10.px)
                    property("background-color", "rgba(239, 68, 68, 0.1)")
                    borderRadius(5.px)
                }
            }) {
                Text("${LocalizedStrings.error}: $err")
                if (isTenantOrApiKeyError) {
                    P({
                        style {
                            marginTop(8.px)
                            fontSize(14.px)
                            display(DisplayStyle.Flex)
                            gap(12.px)
                            flexWrap(FlexWrap.Wrap)
                        }
                    }) {
                        Button({
                            style {
                                color(FlagentTheme.Primary)
                                textDecoration("underline")
                                fontWeight("600")
                                backgroundColor(Color("transparent"))
                                border(0.px)
                                cursor("pointer")
                                padding(0.px)
                                fontSize(14.px)
                            }
                            onClick { Router.navigateToTenantsWithCreate() }
                        }) {
                            Text("Create first tenant →")
                        }
                        Button({
                            style {
                                color(FlagentTheme.Primary)
                                textDecoration("underline")
                                fontWeight("600")
                                backgroundColor(Color("transparent"))
                                border(0.px)
                                cursor("pointer")
                                padding(0.px)
                                fontSize(14.px)
                            }
                            onClick { Router.navigateTo(Route.Login) }
                        }) {
                            Text("Log in (admin) →")
                        }
                    }
                }
            }
        } else {
            val sortedFlags = remember(flags, sortColumn.value, sortAscending.value) {
                var result = flags.toList()
                sortColumn.value?.let { column ->
                    result = when (column) {
                        "id" -> result.sortedBy { it.id }
                        "description" -> result.sortedBy { it.description }
                        "key" -> result.sortedBy { it.key }
                        "updatedBy" -> result.sortedBy { it.updatedBy ?: "" }
                        "updatedAt" -> result.sortedBy { it.updatedAt ?: "" }
                        else -> result
                    }
                    if (!sortAscending.value) result = result.reversed()
                }
                result
            }

            val groupedByTag = remember(sortedFlags, groupByTag.value) {
                if (!groupByTag.value) null
                else {
                    val noTagsKey = "\u0000"
                    sortedFlags
                        .groupBy { f -> f.tags.firstOrNull()?.value ?: noTagsKey }
                        .toList()
                        .sortedBy { (k, _) -> if (k == noTagsKey) "zzz" else k }
                        .associate { it.first to it.second }
                }
            }

            if (sortedFlags.isEmpty()) {
                Div({
                    style {
                        padding(40.px, 20.px)
                        textAlign("center")
                        color(FlagentTheme.textLight(themeMode))
                        backgroundColor(FlagentTheme.cardBg(themeMode))
                        border {
                            width(1.px)
                            style(LineStyle.Solid)
                            color(FlagentTheme.cardBorder(themeMode))
                        }
                        borderRadius(8.px)
                    }
                }) {
                    Icon(
                        name = "search_off",
                        size = 48.px,
                        color = FlagentTheme.textLight(themeMode)
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
                val totalPages = maxOf(1, ((totalCount.value + PAGE_SIZE - 1) / PAGE_SIZE).toInt())
                Div({
                    style {
                        backgroundColor(FlagentTheme.cardBg(themeMode))
                        border {
                            width(1.px)
                            style(LineStyle.Solid)
                            color(FlagentTheme.cardBorder(themeMode))
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
                                    backgroundColor(FlagentTheme.inputBg(themeMode))
                                    property("border-bottom", "2px solid ${FlagentTheme.cardBorder(themeMode)}")
                                }
                            }) {
                                Th({
                                    style {
                                        padding(10.px, 12.px)
                                        width(40.px)
                                        textAlign("center")
                                    }
                                }) {
                                    Input(InputType.Checkbox) {
                                        checked(sortedFlags.isNotEmpty() && sortedFlags.all { it.id in selectedIds })
                                        onChange { event ->
                                            if ((event.target as org.w3c.dom.HTMLInputElement).checked) {
                                                sortedFlags.forEach { selectedIds.add(it.id) }
                                            } else {
                                                sortedFlags.forEach { selectedIds.remove(it.id) }
                                            }
                                        }
                                        onClick { (it as org.w3c.dom.events.Event).stopPropagation() }
                                        style { cursor("pointer") }
                                    }
                                }
                                Th({
                                    onClick { sortFlags("id") }
                                    style {
                                        padding(10.px, 12.px)
                                        cursor("pointer")
                                        property("user-select", "none")
                                        fontWeight("600")
                                        fontSize(13.px)
                                        color(FlagentTheme.Text)
                                        textAlign("left")
                                        property("transition", "background-color 0.2s")
                                    }
                                    onMouseEnter {
                                        (it.target as org.w3c.dom.HTMLElement).style.backgroundColor =
                                            FlagentTheme.inputBg(themeMode).toString()
                                    }
                                    onMouseLeave {
                                        (it.target as org.w3c.dom.HTMLElement).style.backgroundColor =
                                            FlagentTheme.inputBg(themeMode).toString()
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
                                    onClick { sortFlags("description") }
                                    style {
                                        padding(10.px, 12.px)
                                        cursor("pointer")
                                        property("user-select", "none")
                                        fontWeight("600")
                                        fontSize(13.px)
                                        color(FlagentTheme.Text)
                                        textAlign("left")
                                        property("transition", "background-color 0.2s")
                                    }
                                    onMouseEnter {
                                        (it.target as org.w3c.dom.HTMLElement).style.backgroundColor =
                                            FlagentTheme.inputBg(themeMode).toString()
                                    }
                                    onMouseLeave {
                                        (it.target as org.w3c.dom.HTMLElement).style.backgroundColor = ""
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
                                        if (sortColumn.value == "description") {
                                            Span({ style { fontSize(12.px); color(FlagentTheme.Primary) } }) {
                                                Text(if (sortAscending.value) "↑" else "↓")
                                            }
                                        }
                                    }
                                }
                                Th({
                                    style {
                                        padding(10.px, 12.px)
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
                                        padding(10.px, 12.px)
                                        cursor("pointer")
                                        property("user-select", "none")
                                        fontWeight("600")
                                        fontSize(13.px)
                                        color(FlagentTheme.Text)
                                        textAlign("left")
                                        property("transition", "background-color 0.2s")
                                    }
                                    onMouseEnter {
                                        (it.target as org.w3c.dom.HTMLElement).style.backgroundColor =
                                            FlagentTheme.inputBg(themeMode).toString()
                                    }
                                    onMouseLeave {
                                        (it.target as org.w3c.dom.HTMLElement).style.backgroundColor =
                                            FlagentTheme.inputBg(themeMode).toString()
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
                                        padding(10.px, 12.px)
                                        cursor("pointer")
                                        property("user-select", "none")
                                        fontWeight("600")
                                        fontSize(13.px)
                                        color(FlagentTheme.Text)
                                        textAlign("left")
                                        property("transition", "background-color 0.2s")
                                    }
                                    onMouseEnter {
                                        (it.target as org.w3c.dom.HTMLElement).style.backgroundColor =
                                            FlagentTheme.inputBg(themeMode).toString()
                                    }
                                    onMouseLeave {
                                        (it.target as org.w3c.dom.HTMLElement).style.backgroundColor =
                                            FlagentTheme.inputBg(themeMode).toString()
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
                                        padding(10.px, 12.px)
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
                            if (groupedByTag != null) {
                                GroupedFlagsTableBody(
                                    groupedByTag = groupedByTag,
                                    themeMode = themeMode,
                                    selectedIds = selectedIds,
                                    selectedTags = selectedTags
                                )
                            } else {
                                sortedFlags.forEachIndexed { index, flag ->
                                    FlagTableRow(
                                        flag = flag,
                                        rowIndex = index,
                                        themeMode = themeMode,
                                        selectedIds = selectedIds,
                                        selectedTags = selectedTags
                                    )
                                }
                            }
                        }
                    }
                }
                Div({
                    style {
                        display(DisplayStyle.Flex)
                        alignItems(AlignItems.Center)
                        justifyContent(JustifyContent.SpaceBetween)
                        padding(12.px, 16.px)
                        border {
                            width(1.px)
                            style(LineStyle.Solid)
                            color(FlagentTheme.cardBorder(themeMode))
                        }
                        property("border-top", "none")
                        backgroundColor(FlagentTheme.inputBg(themeMode))
                    }
                }) {
                    Span({
                        style {
                            fontSize(13.px)
                            color(FlagentTheme.textLight(themeMode))
                        }
                    }) {
                        Text("${totalCount.value} ${LocalizedStrings.featureFlags.lowercase()}")
                    }
                    Pagination(
                        currentPage = currentPage.value,
                        totalPages = totalPages,
                        onPageChange = { currentPage.value = it }
                    )
                }
            }

            // Deleted Flags section
            Div({
                style {
                    marginTop(40.px)
                    padding(20.px)
                    backgroundColor(FlagentTheme.cardBg(themeMode))
                    border {
                        width(1.px)
                        style(LineStyle.Solid)
                        color(FlagentTheme.cardBorder(themeMode))
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
                        color(Color.white)
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
                        color = FlagentTheme.cardBg(themeMode)
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
                                color(FlagentTheme.textLight(themeMode))
                                backgroundColor(FlagentTheme.inputBg(themeMode))
                                borderRadius(6.px)
                            }
                        }) {
                            Icon(
                                name = "delete_outline",
                                size = 40.px,
                                color = FlagentTheme.textLight(themeMode)
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
                                backgroundColor(FlagentTheme.inputBg(themeMode))
                                border {
                                    width(1.px)
                                    style(LineStyle.Solid)
                                    color(FlagentTheme.cardBorder(themeMode))
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
                                            backgroundColor(FlagentTheme.inputBg(themeMode))
                                        }
                                    }) {
                                        Th({
                                            style {
                                                padding(10.px)
                                                border {
                                                    width(1.px)
                                                    style(LineStyle.Solid)
                                                    color(FlagentTheme.cardBorder(themeMode))
                                                }
                                            }
                                        }) { Text(LocalizedStrings.flagId) }
                                        Th({
                                            style {
                                                padding(10.px)
                                                border {
                                                    width(1.px)
                                                    style(LineStyle.Solid)
                                                    color(FlagentTheme.cardBorder(themeMode))
                                                }
                                            }
                                        }) { Text(LocalizedStrings.description) }
                                        Th({
                                            style {
                                                padding(10.px)
                                                border {
                                                    width(1.px)
                                                    style(LineStyle.Solid)
                                                    color(FlagentTheme.cardBorder(themeMode))
                                                }
                                            }
                                        }) { Text(LocalizedStrings.tags) }
                                        Th({
                                            style {
                                                padding(10.px)
                                                border {
                                                    width(1.px)
                                                    style(LineStyle.Solid)
                                                    color(FlagentTheme.cardBorder(themeMode))
                                                }
                                            }
                                        }) { Text(LocalizedStrings.lastUpdatedBy) }
                                        Th({
                                            style {
                                                padding(10.px)
                                                border {
                                                    width(1.px)
                                                    style(LineStyle.Solid)
                                                    color(FlagentTheme.cardBorder(themeMode))
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
                                                    color(FlagentTheme.cardBorder(themeMode))
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
                                                    color(FlagentTheme.cardBorder(themeMode))
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
                                                        color(FlagentTheme.cardBorder(themeMode))
                                                    }
                                                }
                                            }) { Text(flag.id.toString()) }
                                            Td({
                                                style {
                                                    padding(10.px)
                                                    border {
                                                        width(1.px)
                                                        style(LineStyle.Solid)
                                                        color(FlagentTheme.cardBorder(themeMode))
                                                    }
                                                }
                                            }) { Text(flag.description) }
                                            Td({
                                                style {
                                                    padding(10.px)
                                                    border {
                                                        width(1.px)
                                                        style(LineStyle.Solid)
                                                        color(FlagentTheme.cardBorder(themeMode))
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
                                                                color(Color.white)
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
                                                        color(FlagentTheme.cardBorder(themeMode))
                                                    }
                                                }
                                            }) { Text(flag.updatedBy ?: "") }
                                            Td({
                                                style {
                                                    padding(10.px)
                                                    border {
                                                        width(1.px)
                                                        style(LineStyle.Solid)
                                                        color(FlagentTheme.cardBorder(themeMode))
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
                                                        color(Color.white)
                                                        border {
                                                            width(0.px)
                                                            style(LineStyle.None)
                                                        }
                                                        borderRadius(6.px)
                                                        cursor("pointer")
                                                        fontWeight("600")
                                                        fontSize(13.px)
                                                        property("transition", "all 0.2s")
                                                        property(
                                                            "box-shadow",
                                                            "0 2px 4px ${FlagentTheme.Shadow}"
                                                        )
                                                    }
                                                    onMouseEnter {
                                                        val element =
                                                            it.target as org.w3c.dom.HTMLElement
                                                        element.style.transform = "translateY(-1px)"
                                                        element.style.setProperty(
                                                            "box-shadow",
                                                            "0 4px 6px ${FlagentTheme.ShadowHover}"
                                                        )
                                                    }
                                                    onMouseLeave {
                                                        val element =
                                                            it.target as org.w3c.dom.HTMLElement
                                                        element.style.transform = "translateY(0)"
                                                        element.style.setProperty(
                                                            "box-shadow",
                                                            "0 2px 4px ${FlagentTheme.Shadow}"
                                                        )
                                                    }
                                                }) {
                                                    Icon(
                                                        name = "restore",
                                                        size = 18.px,
                                                        color = FlagentTheme.cardBg(themeMode)
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
