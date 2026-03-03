package flagent.frontend.components.experiments

import androidx.compose.runtime.*
import flagent.api.model.FlagResponse
import flagent.api.model.TagResponse
import flagent.frontend.api.ApiClient
import flagent.frontend.components.Icon
import flagent.frontend.components.common.FilterChip
import flagent.frontend.components.common.PageHeader
import flagent.frontend.config.AppConfig
import flagent.frontend.i18n.LocalizedStrings
import flagent.frontend.navigation.Route
import flagent.frontend.navigation.Router
import flagent.frontend.state.LocalThemeMode
import flagent.frontend.theme.FlagentTheme
import flagent.frontend.util.ErrorHandler
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

/** Status filter for experiments list */
private enum class ExperimentStatusFilter { All, Enabled, Disabled }

private const val NO_TAGS_GROUP_KEY = "\u0000"

/**
 * Experiments (A/B) page: flags with 2+ variants. Card grid or table layout with status filter,
 * search, tags filter, group by tags, and sortable table.
 */
@Composable
fun ExperimentsPage() {
    val themeMode = LocalThemeMode.current
    var flags by remember { mutableStateOf<List<FlagResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var statusFilter by remember { mutableStateOf(ExperimentStatusFilter.All) }
    var viewCards by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    val selectedTags = remember { mutableStateListOf<String>() }
    val availableTagsState = remember { mutableStateOf<List<TagResponse>>(emptyList()) }
    var groupByTag by remember { mutableStateOf(false) }
    var sortColumn by remember { mutableStateOf<String?>(null) }
    var sortAscending by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        isLoading = true
        error = null
        ErrorHandler.withErrorHandling(
            block = {
                val (all, _) = ApiClient.getFlags(preload = true)
                flags = all.filter { it.variants.size >= 2 }
                try {
                    availableTagsState.value = ApiClient.getTags()
                } catch (_: Throwable) { /* ignore */ }
            },
            onError = { err ->
                error = ErrorHandler.getUserMessage(err)
            }
        )
        isLoading = false
    }

    val filteredFlags = remember(flags, statusFilter, searchQuery, selectedTags) {
        var list = when (statusFilter) {
            ExperimentStatusFilter.All -> flags
            ExperimentStatusFilter.Enabled -> flags.filter { it.enabled }
            ExperimentStatusFilter.Disabled -> flags.filter { !it.enabled }
        }
        val q = searchQuery.trim().lowercase()
        if (q.isNotEmpty()) {
            list = list.filter { f ->
                f.key.lowercase().contains(q) || f.description.lowercase().contains(q)
            }
        }
        if (selectedTags.isNotEmpty()) {
            list = list.filter { f -> f.tags.any { it.value in selectedTags } }
        }
        list
    }

    val sortedFlags = remember(filteredFlags, sortColumn, sortAscending) {
        var result = filteredFlags
        sortColumn?.let { col ->
            result = when (col) {
                "key" -> result.sortedBy { it.key }
                "description" -> result.sortedBy { it.description }
                "updatedAt" -> result.sortedBy { it.updatedAt ?: "" }
                "status" -> result.sortedBy { it.enabled }
                else -> result
            }
            if (!sortAscending) result = result.reversed()
        }
        result
    }

    val groupedByTag = remember(sortedFlags, groupByTag) {
        if (!groupByTag) null
        else sortedFlags
            .groupBy { f -> f.tags.firstOrNull()?.value ?: NO_TAGS_GROUP_KEY }
            .toList()
            .sortedBy { (k, _) -> if (k == NO_TAGS_GROUP_KEY) "zzz" else k }
            .associate { it.first to it.second }
    }

    fun sortBy(column: String) {
        if (sortColumn == column) sortAscending = !sortAscending
        else { sortColumn = column; sortAscending = true }
    }

    val hasActiveFilters = searchQuery.isNotBlank() || selectedTags.isNotEmpty()

    Div({
        style {
            padding(0.px)
        }
    }) {
        PageHeader(
            title = LocalizedStrings.experimentsTitle,
            subtitle = LocalizedStrings.experimentsSubtitle
        )

        if (isLoading) {
            Div({
                style {
                    textAlign("center")
                    padding(40.px)
                }
            }) {
                Text(LocalizedStrings.loading)
            }
        } else if (error != null) {
            Div({
                style {
                    padding(20.px)
                    backgroundColor(FlagentTheme.Error)
                    borderRadius(8.px)
                    color(Color.white)
                }
            }) {
                Text(error!!)
                if (error!!.contains("tenant", ignoreCase = true) || error!!.contains("Create tenant", ignoreCase = true) || error!!.contains("X-API-Key", ignoreCase = true)) {
                    SideEffect { flagent.frontend.state.BackendOnboardingState.setBackendNeedsTenantOrAuth() }
                    P({
                        style {
                            marginTop(10.px)
                            fontSize(14.px)
                            display(DisplayStyle.Flex)
                            gap(12.px)
                            flexWrap(FlexWrap.Wrap)
                        }
                    }) {
                        Button({
                            style {
                                color(Color.white)
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
                            Text(flagent.frontend.i18n.LocalizedStrings.createFirstTenant)
                        }
                        Button({
                            style {
                                color(Color.white)
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
                            Text(flagent.frontend.i18n.LocalizedStrings.logInAdmin)
                        }
                    }
                }
            }
        } else {
            Div({
                style {
                    marginBottom(16.px)
                    display(DisplayStyle.Flex)
                    flexWrap(FlexWrap.Wrap)
                    alignItems(AlignItems.Center)
                    gap(12.px)
                }
            }) {
                Div({
                    style {
                        display(DisplayStyle.Flex)
                        alignItems(AlignItems.Center)
                        gap(8.px)
                        flex(1)
                        property("min-width", "200px")
                    }
                }) {
                    Span({
                        classes("material-icons")
                        style { fontSize(20.px); color(FlagentTheme.textLight(themeMode)) }
                    }) { Text("search") }
                    Input(InputType.Text) {
                        value(searchQuery)
                        onInput { event -> searchQuery = event.value }
                        attr("placeholder", LocalizedStrings.searchExperimentsPlaceholder)
                        style {
                            flex(1)
                            padding(8.px, 12.px)
                            borderRadius(6.px)
                            border(1.px, LineStyle.Solid, FlagentTheme.inputBorder(themeMode))
                            backgroundColor(FlagentTheme.inputBg(themeMode))
                            color(FlagentTheme.text(themeMode))
                            fontSize(14.px)
                        }
                    }
                }
                listOf(
                    ExperimentStatusFilter.All to LocalizedStrings.filterAll,
                    ExperimentStatusFilter.Enabled to LocalizedStrings.enabled,
                    ExperimentStatusFilter.Disabled to LocalizedStrings.disabled
                ).forEach { (filter, label) ->
                    Button({
                        style {
                            padding(6.px, 12.px)
                            fontSize(13.px)
                            borderRadius(6.px)
                            border(1.px, LineStyle.Solid, if (statusFilter == filter) FlagentTheme.Primary else FlagentTheme.cardBorder(themeMode))
                            backgroundColor(if (statusFilter == filter) FlagentTheme.Primary else FlagentTheme.inputBg(themeMode))
                            color(if (statusFilter == filter) Color.white else FlagentTheme.text(themeMode))
                            cursor("pointer")
                        }
                        onClick { statusFilter = filter }
                    }) {
                        Text(label)
                    }
                }
                FilterChip(themeMode, LocalizedStrings.groupByTags, groupByTag, onClick = { groupByTag = !groupByTag })
                if (hasActiveFilters) {
                    Button({
                        style {
                            padding(6.px, 12.px)
                            fontSize(12.px)
                            backgroundColor(FlagentTheme.inputBg(themeMode))
                            color(FlagentTheme.text(themeMode))
                            border(1.px, LineStyle.Solid, FlagentTheme.cardBorder(themeMode))
                            borderRadius(16.px)
                            cursor("pointer")
                        }
                        onClick {
                            searchQuery = ""
                            selectedTags.clear()
                        }
                    }) {
                        Text(LocalizedStrings.clearFilters)
                    }
                }
                Span({ style { marginLeft(8.px); color(FlagentTheme.textLight(themeMode)); fontSize(13.px) } }) { Text("•") }
                Button({
                    style {
                        padding(6.px, 12.px)
                        fontSize(13.px)
                        borderRadius(6.px)
                        border(1.px, LineStyle.Solid, if (viewCards) FlagentTheme.Primary else FlagentTheme.cardBorder(themeMode))
                        backgroundColor(if (viewCards) FlagentTheme.Primary else FlagentTheme.inputBg(themeMode))
                        color(if (viewCards) Color.white else FlagentTheme.text(themeMode))
                        cursor("pointer")
                    }
                    onClick { viewCards = true }
                }) {
                    Icon("grid_view", size = 16.px)
                    Text(" ${LocalizedStrings.cardsView}")
                }
                Button({
                    style {
                        padding(6.px, 12.px)
                        fontSize(13.px)
                        borderRadius(6.px)
                        border(1.px, LineStyle.Solid, if (!viewCards) FlagentTheme.Primary else FlagentTheme.cardBorder(themeMode))
                        backgroundColor(if (!viewCards) FlagentTheme.Primary else FlagentTheme.inputBg(themeMode))
                        color(if (!viewCards) Color.white else FlagentTheme.text(themeMode))
                        cursor("pointer")
                    }
                    onClick { viewCards = false }
                }) {
                    Icon("table_rows", size = 16.px)
                    Text(" ${LocalizedStrings.tableView}")
                }
                Span({
                    style {
                        marginLeft(8.px)
                        color(FlagentTheme.textLight(themeMode))
                        fontSize(14.px)
                    }
                }) {
                    Text("${sortedFlags.size} ${LocalizedStrings.experimentsCount}")
                }
            }
            if (availableTagsState.value.isNotEmpty() && selectedTags.size < 12) {
                Div({
                    style {
                        display(DisplayStyle.Flex)
                        flexWrap(FlexWrap.Wrap)
                        gap(6.px)
                        alignItems(AlignItems.Center)
                        marginBottom(12.px)
                    }
                }) {
                    selectedTags.forEach { tagValue: String ->
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
                    availableTagsState.value.filter { it.value !in selectedTags }.take(8).forEach { tag ->
                        Span({
                            style {
                                display(DisplayStyle.InlineBlock)
                                padding(6.px, 10.px)
                                backgroundColor(FlagentTheme.inputBg(themeMode))
                                color(FlagentTheme.text(themeMode))
                                border(1.px, LineStyle.Solid, FlagentTheme.cardBorder(themeMode))
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
            if (sortedFlags.isEmpty()) {
                Div({
                    style {
                        backgroundColor(FlagentTheme.cardBg(themeMode))
                        borderRadius(8.px)
                        padding(40.px)
                        property("box-shadow", FlagentTheme.ShadowCard)
                        textAlign("center")
                    }
                }) {
                    Icon("science", size = 48.px, color = FlagentTheme.textLight(themeMode))
                    P({
                        style {
                            marginTop(16.px)
                            color(FlagentTheme.text(themeMode))
                            fontSize(16.px)
                        }
                    }) {
                        Text(
                            if (flags.isEmpty()) LocalizedStrings.noExperiments
                            else LocalizedStrings.noExperimentsMatchFilter
                        )
                    }
                    P({
                        style {
                            color(FlagentTheme.textLight(themeMode))
                            fontSize(14.px)
                            marginTop(8.px)
                        }
                    }) {
                        Text(
                            if (flags.isEmpty()) LocalizedStrings.noExperimentsHint
                            else if (hasActiveFilters) LocalizedStrings.noExperimentsChangeFilterHint
                            else LocalizedStrings.noExperimentsChangeFilterHint
                        )
                    }
                    if (hasActiveFilters) {
                        Button({
                            style {
                                marginTop(12.px)
                                marginRight(8.px)
                                padding(8.px, 16.px)
                                backgroundColor(FlagentTheme.inputBg(themeMode))
                                color(FlagentTheme.text(themeMode))
                                border(0.px)
                                borderRadius(6.px)
                                cursor("pointer")
                                fontSize(14.px)
                            }
                            onClick { searchQuery = ""; selectedTags.clear() }
                        }) {
                            Text(LocalizedStrings.clearSearch)
                        }
                    }
                    Button({
                        style {
                            marginTop(20.px)
                            padding(12.px, 24.px)
                            backgroundColor(FlagentTheme.Primary)
                            color(Color.white)
                            border(0.px)
                            borderRadius(6.px)
                            cursor("pointer")
                            fontSize(14.px)
                        }
                        onClick { Router.navigateTo(Route.FlagsList) }
                    }) {
                        Text(LocalizedStrings.viewFlags)
                    }
                }
            } else if (groupedByTag != null) {
                groupedByTag.entries.forEach { (tagKey, list) ->
                    Div({
                        style {
                            marginBottom(24.px)
                        }
                    }) {
                        Div({
                            style {
                                display(DisplayStyle.Flex)
                                alignItems(AlignItems.Center)
                                gap(8.px)
                                marginBottom(12.px)
                                padding(12.px, 16.px)
                                backgroundColor(FlagentTheme.inputBg(themeMode))
                                borderRadius(8.px)
                                property("border-left", "4px solid ${FlagentTheme.Primary}")
                            }
                        }) {
                            Icon("local_offer", size = 18.px, color = FlagentTheme.Primary)
                            Text(if (tagKey == NO_TAGS_GROUP_KEY) LocalizedStrings.noTagsGroup else tagKey)
                            Span({ style { color(FlagentTheme.textLight(themeMode)); fontSize(13.px) } }) {
                                Text(" (${list.size})")
                            }
                        }
                        if (viewCards) {
                            Div({
                                style {
                                    display(DisplayStyle.Grid)
                                    property("grid-template-columns", "repeat(auto-fill, minmax(280px, 1fr))")
                                    gap(16.px)
                                }
                            }) {
                                list.forEach { flag -> ExperimentCard(flag = flag) }
                            }
                        } else {
                            Div({
                                style {
                                    backgroundColor(FlagentTheme.cardBg(themeMode))
                                    borderRadius(8.px)
                                    overflow("hidden")
                                    property("box-shadow", "0 1px 3px ${FlagentTheme.Shadow}")
                                }
                            }) {
                                Table({
                                    style { width(100.percent); property("border-collapse", "collapse") }
                                }) {
                                    Thead {
                                        Tr({
                                            style {
                                                backgroundColor(FlagentTheme.inputBg(themeMode))
                                                property("border-bottom", "1px solid ${FlagentTheme.cardBorder(themeMode)}")
                                            }
                                        }) {
                                            listOf(LocalizedStrings.flagKey, LocalizedStrings.description, LocalizedStrings.variants, LocalizedStrings.status, LocalizedStrings.action).forEach { h ->
                                                Th({
                                                    style {
                                                        padding(10.px, 12.px)
                                                        textAlign("left")
                                                        fontSize(12.px)
                                                        fontWeight(600)
                                                        color(FlagentTheme.textLight(themeMode))
                                                        property("text-transform", "uppercase")
                                                    }
                                                }) { Text(h) }
                                            }
                                        }
                                    }
                                    Tbody {
                                        list.forEach { flag ->
                                            Tr({
                                                style {
                                                    property("border-bottom", "1px solid ${FlagentTheme.cardBorder(themeMode)}")
                                                    property("transition", "background-color 0.15s")
                                                }
                                                onMouseEnter { (it.target as org.w3c.dom.HTMLElement).style.backgroundColor = FlagentTheme.inputBg(themeMode).toString() }
                                                onMouseLeave { (it.target as org.w3c.dom.HTMLElement).style.backgroundColor = "transparent" }
                                            }) {
                                                Td({
                                                    style { padding(10.px, 12.px); fontSize(14.px); color(FlagentTheme.Primary); cursor("pointer") }
                                                    onClick { Router.navigateTo(Route.FlagDetail(flag.id)) }
                                                }) { Text(flag.key.ifBlank { "Flag #${flag.id}" }) }
                                                Td({ style { padding(10.px, 12.px); fontSize(14.px); property("max-width", "200px"); property("overflow", "hidden"); property("text-overflow", "ellipsis"); property("white-space", "nowrap") } }) {
                                                    Text(flag.description.ifBlank { "—" })
                                                }
                                                Td({ style { padding(10.px, 12.px); fontSize(14.px) } }) {
                                                    Text(flag.variants.joinToString(", ") { it.key })
                                                }
                                                Td({ style { padding(10.px, 12.px); fontSize(12.px) } }) {
                                                    Span({
                                                        style {
                                                            padding(4.px, 8.px)
                                                            borderRadius(6.px)
                                                            backgroundColor(if (flag.enabled) FlagentTheme.successBg(themeMode) else FlagentTheme.inputBg(themeMode))
                                                            color(if (flag.enabled) FlagentTheme.Success else FlagentTheme.textLight(themeMode))
                                                        }
                                                    }) { Text(if (flag.enabled) LocalizedStrings.enabled else LocalizedStrings.disabled) }
                                                }
                                                Td({ style { padding(10.px, 12.px) } }) {
                                                    Div({ style { display(DisplayStyle.Flex); gap(8.px); flexWrap(FlexWrap.Wrap) } }) {
                                                        if (AppConfig.Features.enableMetrics) {
                                                            Button({
                                                                style { padding(6.px, 10.px); backgroundColor(FlagentTheme.Primary); color(Color.white); border(0.px); borderRadius(6.px); cursor("pointer"); fontSize(12.px) }
                                                                onClick { Router.navigateTo(Route.FlagMetrics(flag.id)) }
                                                            }) {
                                                                Icon("bar_chart", size = 14.px, color = FlagentTheme.Background)
                                                                Text(LocalizedStrings.viewMetrics)
                                                            }
                                                        }
                                                        Button({
                                                            style { padding(6.px, 10.px); backgroundColor(FlagentTheme.inputBg(themeMode)); color(FlagentTheme.text(themeMode)); border(1.px, LineStyle.Solid, FlagentTheme.cardBorder(themeMode)); borderRadius(6.px); cursor("pointer"); fontSize(12.px) }
                                                            onClick { Router.navigateTo(Route.FlagDetail(flag.id)) }
                                                        }) {
                                                            Icon("settings", size = 14.px)
                                                            Text(LocalizedStrings.edit)
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
            } else if (viewCards) {
                Div({
                    style {
                        display(DisplayStyle.Grid)
                        property("grid-template-columns", "repeat(auto-fill, minmax(280px, 1fr))")
                        gap(16.px)
                    }
                }) {
                    sortedFlags.forEach { flag ->
                        ExperimentCard(flag = flag)
                    }
                }
            } else {
            Div({
                style {
                    backgroundColor(FlagentTheme.cardBg(themeMode))
                    borderRadius(8.px)
                    overflow("hidden")
                    property("box-shadow", "0 1px 3px ${FlagentTheme.Shadow}")
                }
            }) {
                Table({
                    style {
                        width(100.percent)
                        property("border-collapse", "collapse")
                    }
                }) {
                    Thead {
                        Tr({
                            style {
                                backgroundColor(FlagentTheme.inputBg(themeMode))
                                property("border-bottom", "1px solid ${FlagentTheme.cardBorder(themeMode)}")
                            }
                        }) {
                            Th({
                                style {
                                    padding(10.px, 12.px)
                                    textAlign("left")
                                    fontSize(12.px)
                                    fontWeight(600)
                                    color(FlagentTheme.textLight(themeMode))
                                    property("text-transform", "uppercase")
                                    cursor("pointer")
                                }
                                onClick { sortBy("key") }
                            }) {
                                Span({ style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); gap(4.px) } }) {
                                    Text(LocalizedStrings.flagKey)
                                    if (sortColumn == "key") {
                                        Span({ style { fontSize(12.px); color(FlagentTheme.Primary) } }) {
                                            Text(if (sortAscending) "↑" else "↓")
                                        }
                                    }
                                }
                            }
                            Th({
                                style {
                                    padding(10.px, 12.px)
                                    textAlign("left")
                                    fontSize(12.px)
                                    fontWeight(600)
                                    color(FlagentTheme.textLight(themeMode))
                                    property("text-transform", "uppercase")
                                    cursor("pointer")
                                }
                                onClick { sortBy("description") }
                            }) {
                                Span({ style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); gap(4.px) } }) {
                                    Text(LocalizedStrings.description)
                                    if (sortColumn == "description") {
                                        Span({ style { fontSize(12.px); color(FlagentTheme.Primary) } }) {
                                            Text(if (sortAscending) "↑" else "↓")
                                        }
                                    }
                                }
                            }
                            Th({
                                style {
                                    padding(10.px, 12.px)
                                    textAlign("left")
                                    fontSize(12.px)
                                    fontWeight(600)
                                    color(FlagentTheme.textLight(themeMode))
                                    property("text-transform", "uppercase")
                                }
                            }) { Text(LocalizedStrings.variants) }
                            Th({
                                style {
                                    padding(10.px, 12.px)
                                    textAlign("left")
                                    fontSize(12.px)
                                    fontWeight(600)
                                    color(FlagentTheme.textLight(themeMode))
                                    property("text-transform", "uppercase")
                                    cursor("pointer")
                                }
                                onClick { sortBy("status") }
                            }) {
                                Span({ style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); gap(4.px) } }) {
                                    Text(LocalizedStrings.status)
                                    if (sortColumn == "status") {
                                        Span({ style { fontSize(12.px); color(FlagentTheme.Primary) } }) {
                                            Text(if (sortAscending) "↑" else "↓")
                                        }
                                    }
                                }
                            }
                            Th({
                                style {
                                    padding(10.px, 12.px)
                                    textAlign("left")
                                    fontSize(12.px)
                                    fontWeight(600)
                                    color(FlagentTheme.textLight(themeMode))
                                    property("text-transform", "uppercase")
                                }
                            }) { Text(LocalizedStrings.action) }
                        }
                    }
                    Tbody {
                        sortedFlags.forEach { flag ->
                            Tr({
                                style {
                                    property("border-bottom", "1px solid ${FlagentTheme.cardBorder(themeMode)}")
                                    property("transition", "background-color 0.15s")
                                }
                                onMouseEnter { (it.target as org.w3c.dom.HTMLElement).style.backgroundColor = FlagentTheme.inputBg(themeMode).toString() }
                                onMouseLeave { (it.target as org.w3c.dom.HTMLElement).style.backgroundColor = "transparent"
                                }
                            }) {
                                Td({
                                    style {
                                        padding(10.px, 12.px)
                                        fontSize(14.px)
                                        color(FlagentTheme.Primary)
                                        cursor("pointer")
                                    }
                                    onClick { Router.navigateTo(Route.FlagDetail(flag.id)) }
                                }) {
                                    Text(flag.key.ifBlank { "Flag #${flag.id}" })
                                }
                                Td({ style { padding(10.px, 12.px); fontSize(14.px); property("max-width", "200px"); property("overflow", "hidden"); property("text-overflow", "ellipsis"); property("white-space", "nowrap") } }) {
                                    Text(flag.description.ifBlank { "—" })
                                }
                                Td({ style { padding(10.px, 12.px); fontSize(14.px) } }) {
                                    Text(flag.variants.joinToString(", ") { it.key })
                                }
                                Td({ style { padding(10.px, 12.px); fontSize(12.px) } }) {
                                    Span({
                                        style {
                                            padding(4.px, 8.px)
                                            borderRadius(6.px)
                                            backgroundColor(if (flag.enabled) FlagentTheme.successBg(themeMode) else FlagentTheme.inputBg(themeMode))
                                            color(if (flag.enabled) FlagentTheme.Success else FlagentTheme.textLight(themeMode))
                                        }
                                    }) { Text(if (flag.enabled) LocalizedStrings.enabled else LocalizedStrings.disabled) }
                                }
                                Td({
                                    style { padding(10.px, 12.px) }
                                }) {
                                    Div({
                                        style {
                                            display(DisplayStyle.Flex)
                                            gap(8.px)
                                            flexWrap(FlexWrap.Wrap)
                                        }
                                    }) {
                                        if (AppConfig.Features.enableMetrics) {
                                            Button({
                                                style {
                                                    padding(6.px, 10.px)
                                                    backgroundColor(FlagentTheme.Primary)
                                                    color(Color.white)
                                                    border(0.px)
                                                    borderRadius(6.px)
                                                    cursor("pointer")
                                                    fontSize(12.px)
                                                }
                                                onClick { Router.navigateTo(Route.FlagMetrics(flag.id)) }
                                            }) {
                                                Icon("bar_chart", size = 14.px, color = FlagentTheme.Background)
                                                Text(LocalizedStrings.viewMetrics)
                                            }
                                        }
                                        Button({
                                            style {
                                                padding(6.px, 10.px)
                                                backgroundColor(FlagentTheme.inputBg(themeMode))
                                                color(FlagentTheme.text(themeMode))
                                                border(1.px, LineStyle.Solid, FlagentTheme.cardBorder(themeMode))
                                                borderRadius(6.px)
                                                cursor("pointer")
                                                fontSize(12.px)
                                            }
                                            onClick { Router.navigateTo(Route.FlagDetail(flag.id)) }
                                        }) {
                                            Icon("settings", size = 14.px)
                                            Text(LocalizedStrings.edit)
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

