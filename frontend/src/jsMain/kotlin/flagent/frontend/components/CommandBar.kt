package flagent.frontend.components

import androidx.compose.runtime.*
import flagent.api.model.FlagResponse
import flagent.frontend.api.ApiClient
import flagent.frontend.config.AppConfig
import flagent.frontend.i18n.LocalizedStrings
import flagent.frontend.navigation.Route
import flagent.frontend.navigation.Router
import flagent.frontend.state.LocalThemeMode
import flagent.frontend.theme.FlagentTheme
import flagent.frontend.util.ErrorHandler
import kotlinx.browser.document
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.KeyboardEvent

/**
 * Command bar item types for display
 */
private sealed class CommandBarItem {
    data class Flag(val flag: FlagResponse) : CommandBarItem()
    data class Action(val label: String, val icon: String, val action: () -> Unit) : CommandBarItem()
    data class Nav(val label: String, val icon: String, val route: Route) : CommandBarItem()
}

private const val COMMAND_BAR_INPUT_ID = "command-bar-input"
private const val MAX_FLAGS_SEARCH = 100

/**
 * Global Command Bar (Cmd+K / Ctrl+K) - LaunchDarkly-style quick search and navigation.
 * - Search flags by key or description
 * - Quick actions: Create Flag, Go to Analytics, Go to Debug
 * - Navigation to main sections
 */
@Composable
fun CommandBar(
    isOpen: Boolean,
    onClose: () -> Unit
) {
    if (!isOpen) return

    val themeMode = LocalThemeMode.current
    val scope = rememberCoroutineScope()
    var query by remember { mutableStateOf("") }
    var flags by remember { mutableStateOf<List<FlagResponse>>(emptyList()) }
    var flagsLoading by remember { mutableStateOf(false) }
    var selectedIndex by remember { mutableStateOf(0) }

    val quickActions = remember {
        buildList {
            add(CommandBarItem.Action(LocalizedStrings.createFlag, "add_circle") {
                Router.navigateTo(Route.CreateFlag)
                onClose()
            })
            add(CommandBarItem.Action(LocalizedStrings.analyticsTitle, "analytics") {
                Router.navigateTo(Route.Analytics)
                onClose()
            })
            add(CommandBarItem.Action(LocalizedStrings.debugConsole, "bug_report") {
                Router.navigateTo(Route.DebugConsole())
                onClose()
            })
        }
    }

    val navItems = remember {
        buildList {
            add(CommandBarItem.Nav(LocalizedStrings.dashboardNav, "dashboard", Route.Dashboard))
            add(CommandBarItem.Nav(LocalizedStrings.flagsNav, "flag", Route.FlagsList))
            add(CommandBarItem.Nav(LocalizedStrings.experimentsTitle, "science", Route.Experiments))
            add(CommandBarItem.Nav(LocalizedStrings.segmentsTitle, "segment", Route.Segments))
            add(CommandBarItem.Nav(LocalizedStrings.analyticsTitle, "analytics", Route.Analytics))
            if (AppConfig.Features.enableCrashAnalytics) {
                add(CommandBarItem.Nav(LocalizedStrings.crashNav, "bug_report", Route.Crash))
            }
            if (AppConfig.Features.enableAnomalyDetection) {
                add(CommandBarItem.Nav(LocalizedStrings.alertsNav, "notifications", Route.Alerts))
            }
            if (AppConfig.Features.enableMultiTenancy) {
                add(CommandBarItem.Nav(LocalizedStrings.tenantsNav, "business", Route.Tenants))
            }
            add(CommandBarItem.Nav(LocalizedStrings.settingsNav, "settings", Route.Settings))
        }
    }

    val flagItems = flags.map { CommandBarItem.Flag(it) }
    val hasQuery = query.isNotBlank()
    val filteredFlags = if (hasQuery) {
        val q = query.lowercase().trim()
        flagItems.filter {
            it is CommandBarItem.Flag && (
                it.flag.key.lowercase().contains(q) ||
                it.flag.description.lowercase().contains(q)
            )
        }
    } else emptyList()

    val filteredNav = if (hasQuery) {
        val q = query.lowercase().trim()
        navItems.filter {
            it is CommandBarItem.Nav && it.label.lowercase().contains(q)
        }
    } else navItems

    val filteredActions = if (hasQuery) {
        val q = query.lowercase().trim()
        quickActions.filter {
            it is CommandBarItem.Action && it.label.lowercase().contains(q)
        }
    } else quickActions

    val allItems = buildList {
        if (filteredActions.isNotEmpty()) addAll(filteredActions)
        if (filteredFlags.isNotEmpty()) addAll(filteredFlags)
        if (filteredNav.isNotEmpty()) addAll(filteredNav)
    }

    val maxIndex = (allItems.size - 1).coerceAtLeast(0)
    selectedIndex = selectedIndex.coerceIn(0, maxIndex)

    LaunchedEffect(isOpen) {
        if (isOpen) {
            query = ""
            selectedIndex = 0
            flags = emptyList()
            flagsLoading = true
            scope.launch {
                ErrorHandler.withErrorHandling(
                    block = {
                        val (loadedFlags, _) = ApiClient.getFlags(limit = MAX_FLAGS_SEARCH)
                        flags = loadedFlags
                    },
                    onError = { /* silent */ }
                )
                flagsLoading = false
            }
            delay(50)
            document.getElementById(COMMAND_BAR_INPUT_ID)?.unsafeCast<HTMLElement>()?.focus()
        }
    }


    DisposableEffect(selectedIndex, allItems.size) {
        val handler: (KeyboardEvent) -> Unit = { event ->
            if (event.key == "Escape") {
                event.preventDefault()
                onClose()
            } else if (event.key == "ArrowDown") {
                event.preventDefault()
                selectedIndex = (selectedIndex + 1).coerceAtMost(maxIndex)
            } else if (event.key == "ArrowUp") {
                event.preventDefault()
                selectedIndex = (selectedIndex - 1).coerceAtLeast(0)
            } else if (event.key == "Enter" && allItems.isNotEmpty()) {
                event.preventDefault()
                val item = allItems.getOrNull(selectedIndex)
                when (item) {
                    is CommandBarItem.Flag -> {
                        Router.navigateTo(Route.FlagDetail(item.flag.id))
                        onClose()
                    }
                    is CommandBarItem.Action -> item.action()
                    is CommandBarItem.Nav -> {
                        Router.navigateTo(item.route)
                        onClose()
                    }
                    null -> {}
                }
            }
        }
        val wrappedHandler: (Event) -> Unit = { handler(it.unsafeCast<KeyboardEvent>()) }
        document.addEventListener("keydown", wrappedHandler)
        onDispose { document.removeEventListener("keydown", wrappedHandler) }
    }

    Div({
        style {
            position(Position.Fixed)
            top(0.px)
            left(0.px)
            right(0.px)
            bottom(0.px)
            backgroundColor(FlagentTheme.Overlay)
            property("backdrop-filter", "blur(4px)")
            display(DisplayStyle.Flex)
            justifyContent(JustifyContent.Center)
            alignItems(AlignItems.FlexStart)
            paddingTop(15.vh)
            property("z-index", "1100")
        }
        onClick { event ->
            if (event.target == event.currentTarget) onClose()
        }
    }) {
        Div({
            style {
                backgroundColor(FlagentTheme.cardBg(themeMode))
                borderRadius(12.px)
                width(100.percent)
                maxWidth(560.px)
                maxHeight(70.vh)
                overflow("hidden")
                border(1.px, LineStyle.Solid, FlagentTheme.cardBorder(themeMode))
                property("box-shadow", "0 25px 50px -12px rgba(0, 0, 0, 0.4)")
            }
            onClick { it.stopPropagation() }
        }) {
            Div({
                style {
                    display(DisplayStyle.Flex)
                    alignItems(AlignItems.Center)
                    gap(12.px)
                    padding(12.px, 16.px)
                    property("border-bottom", "1px solid ${FlagentTheme.cardBorder(themeMode)}")
                }
            }) {
                Icon("search", size = 22.px, color = FlagentTheme.textLight(themeMode))
                Input(InputType.Text) {
                    id(COMMAND_BAR_INPUT_ID)
                    attr("placeholder", LocalizedStrings.commandBarPlaceholder)
                    value(query)
                    onInput { event -> query = event.value }
                    style {
                        flex(1)
                        backgroundColor(Color.transparent)
                        border(0.px)
                        outline("none")
                        fontSize(16.px)
                        color(FlagentTheme.text(themeMode))
                    }
                }
                Span({
                    style {
                        fontSize(12.px)
                        color(FlagentTheme.textLight(themeMode))
                        padding(4.px, 8.px)
                        backgroundColor(FlagentTheme.inputBg(themeMode))
                        borderRadius(6.px)
                    }
                }) {
                    Text(if ((js("typeof navigator !== 'undefined' && /Mac|iPhone|iPad/.test(navigator.platform)") as Boolean)) "⌘K" else "Ctrl+K")
                }
            }

            Div({
                style {
                    maxHeight(50.vh)
                    overflowY("auto")
                    padding(8.px)
                }
            }) {
                if (flagsLoading) {
                    Div({
                        style {
                            padding(24.px)
                            textAlign("center")
                            color(FlagentTheme.textLight(themeMode))
                        }
                    }) {
                        Text(LocalizedStrings.loading)
                    }
                } else if (allItems.isEmpty() && !flagsLoading) {
                    Div({
                        style {
                            padding(24.px)
                            textAlign("center")
                            color(FlagentTheme.textLight(themeMode))
                            fontSize(14.px)
                        }
                    }) {
                        Text(LocalizedStrings.commandBarNoResults)
                    }
                } else {
                    allItems.forEachIndexed { index, item ->
                        val isSelected = index == selectedIndex
                        val bg = if (isSelected) "rgba(14, 165, 233, 0.15)" else "transparent"
                        Div({
                            style {
                                display(DisplayStyle.Flex)
                                alignItems(AlignItems.Center)
                                gap(12.px)
                                padding(10.px, 12.px)
                                borderRadius(8.px)
                                property("background", bg)
                                cursor("pointer")
                            }
                            onClick {
                                when (item) {
                                    is CommandBarItem.Flag -> {
                                        Router.navigateTo(Route.FlagDetail(item.flag.id))
                                        onClose()
                                    }
                                    is CommandBarItem.Action -> item.action()
                                    is CommandBarItem.Nav -> {
                                        Router.navigateTo(item.route)
                                        onClose()
                                    }
                                }
                            }
                            onMouseEnter { selectedIndex = index }
                        }) {
                            when (item) {
                                is CommandBarItem.Flag -> {
                                    Icon("flag", size = 20.px, color = FlagentTheme.Primary)
                                    Div({
                                        style {
                                            flex(1)
                                            property("min-width", "0")
                                        }
                                    }) {
                                        Span({
                                            style {
                                                fontWeight("500")
                                                color(FlagentTheme.text(themeMode))
                                            }
                                        }) {
                                            Text(item.flag.key)
                                        }
                                        if (item.flag.description.isNotBlank()) {
                                            Span({
                                                style {
                                                    display(DisplayStyle.Block)
                                                    fontSize(12.px)
                                                    color(FlagentTheme.textLight(themeMode))
                                                    property("overflow", "hidden")
                                                    property("text-overflow", "ellipsis")
                                                    property("white-space", "nowrap")
                                                }
                                            }) {
                                                Text(item.flag.description.take(60) + if (item.flag.description.length > 60) "…" else "")
                                            }
                                        }
                                    }
                                }
                                is CommandBarItem.Action -> {
                                    Icon(item.icon, size = 20.px, color = FlagentTheme.Primary)
                                    Span({
                                        style {
                                            fontWeight("500")
                                            color(FlagentTheme.text(themeMode))
                                        }
                                    }) {
                                        Text(item.label)
                                    }
                                }
                                is CommandBarItem.Nav -> {
                                    Icon(item.icon, size = 20.px, color = FlagentTheme.textLight(themeMode))
                                    Span({
                                        style {
                                            color(FlagentTheme.text(themeMode))
                                        }
                                    }) {
                                        Text(item.label)
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
