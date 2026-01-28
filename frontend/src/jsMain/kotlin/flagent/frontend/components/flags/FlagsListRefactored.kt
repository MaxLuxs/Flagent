package flagent.frontend.components.flags

import androidx.compose.runtime.*
import flagent.api.model.FlagResponse
import flagent.frontend.components.Icon
import flagent.frontend.components.common.EmptyState
import flagent.frontend.components.common.Pagination
import flagent.frontend.components.common.SkeletonLoader
import flagent.frontend.i18n.LocalizedStrings
import flagent.frontend.navigation.Route
import flagent.frontend.navigation.Router
import flagent.frontend.state.LocalGlobalState
import flagent.frontend.state.Notification
import flagent.frontend.state.NotificationType
import flagent.frontend.theme.FlagentTheme
import flagent.frontend.util.borderBottom
import flagent.frontend.util.borderCollapse
import flagent.frontend.util.textTransform
import flagent.frontend.util.userSelect
import flagent.frontend.viewmodel.FlagsViewModel
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

/**
 * Refactored Flags List component using ViewModel pattern
 */
@Composable
fun FlagsListRefactored() {
    val viewModel = remember { FlagsViewModel() }
    val globalState = LocalGlobalState.current
    val showDeletedFlags = remember { mutableStateOf(false) }
    val currentPage = remember { mutableStateOf(1) }
    val pageSize = 20
    
    // Load flags on mount
    LaunchedEffect(Unit) {
        viewModel.loadFlags()
    }
    
    // Load deleted flags when toggled
    LaunchedEffect(showDeletedFlags.value) {
        if (showDeletedFlags.value) {
            viewModel.loadDeletedFlags()
        }
    }
    
    Div({
        style {
            property("animation", "fadeIn 0.3s ease-out")
        }
    }) {
        // Header with create button
        FlagsHeader(viewModel, globalState)
        
        // Search and filters
        FlagsFilters(viewModel)
        
        // Error message
        viewModel.error?.let { error ->
            ErrorBanner(error) {
                viewModel.clearError()
            }
        }
        
        // Loading state
        if (viewModel.isLoading) {
            SkeletonLoader(height = 400.px)
        } else {
            // Flags table
            val paginatedFlags = viewModel.filteredFlags
                .drop((currentPage.value - 1) * pageSize)
                .take(pageSize)
            
            if (paginatedFlags.isEmpty()) {
                EmptyState(
                    icon = "flag",
                    title = "No flags found",
                    description = "Create your first flag to get started",
                    actionLabel = "Create Flag",
                    onAction = {
                        viewModel.createFlag("My First Flag") { flag ->
                            Router.navigateTo(Route.FlagDetail(flag.id))
                        }
                    }
                )
            } else {
                FlagsTable(paginatedFlags, viewModel, globalState)
                
                // Pagination
                val totalPages = (viewModel.filteredFlags.size + pageSize - 1) / pageSize
                if (totalPages > 1) {
                    Pagination(
                        currentPage = currentPage.value,
                        totalPages = totalPages,
                        onPageChange = { page -> currentPage.value = page }
                    )
                }
            }
        }
        
        // Deleted flags section
        if (showDeletedFlags.value && viewModel.deletedFlags.isNotEmpty()) {
            DeletedFlagsSection(viewModel, globalState)
        }
        
        // Toggle deleted flags
        Div({
            style {
                marginTop(24.px)
                textAlign("center")
            }
        }) {
            Button({
                onClick { showDeletedFlags.value = !showDeletedFlags.value }
                style {
                    padding(10.px, 20.px)
                    backgroundColor(Color.transparent)
                    color(FlagentTheme.Primary)
                    border(1.px, LineStyle.Solid, FlagentTheme.Primary)
                    borderRadius(6.px)
                    cursor("pointer")
                    fontSize(14.px)
                }
            }) {
                Text(if (showDeletedFlags.value) "Hide Deleted Flags" else "Show Deleted Flags")
            }
        }
    }
}

@Composable
private fun FlagsHeader(viewModel: FlagsViewModel, globalState: flagent.frontend.state.GlobalState) {
    Div({
        style {
            display(DisplayStyle.Flex)
            justifyContent(JustifyContent.SpaceBetween)
            alignItems(AlignItems.Center)
            marginBottom(24.px)
        }
    }) {
        H1({
            style {
                fontSize(28.px)
                fontWeight(600)
                color(Color("#1E293B"))
                margin(0.px)
            }
        }) {
            Text("Feature Flags")
        }
        
        Button({
            onClick {
                viewModel.createFlag("New Feature Flag") { flag ->
                    globalState.addNotification(
                        Notification(
                            message = "Flag created successfully",
                            type = NotificationType.SUCCESS
                        )
                    )
                    Router.navigateTo(Route.FlagDetail(flag.id))
                }
            }
            style {
                padding(12.px, 24.px)
                backgroundColor(FlagentTheme.Primary)
                color(Color.white)
                border(0.px)
                borderRadius(6.px)
                cursor("pointer")
                fontSize(14.px)
                fontWeight(500)
                display(DisplayStyle.Flex)
                alignItems(AlignItems.Center)
                gap(8.px)
            }
        }) {
            Icon("add", size = 20.px)
            Text("Create Flag")
        }
    }
}

@Composable
private fun FlagsFilters(viewModel: FlagsViewModel) {
    Div({
        style {
            display(DisplayStyle.Flex)
            gap(16.px)
            marginBottom(24.px)
            flexWrap(FlexWrap.Wrap)
        }
    }) {
        // Search
        Input(InputType.Search) {
            value(viewModel.searchQuery)
            onInput { viewModel.searchQuery = it.value }
            attr("placeholder", "Search flags...")
            style {
                padding(10.px, 16.px)
                border(1.px, LineStyle.Solid, Color("#E2E8F0"))
                borderRadius(6.px)
                fontSize(14.px)
                minWidth(300.px)
            }
        }
        
        // Status filter
        Select({
            onChange { event ->
                viewModel.statusFilter = when (event.value) {
                    "enabled" -> true
                    "disabled" -> false
                    else -> null
                }
            }
            style {
                padding(10.px, 16.px)
                border(1.px, LineStyle.Solid, Color("#E2E8F0"))
                borderRadius(6.px)
                fontSize(14.px)
            }
        }) {
            Option("all") { Text("All Status") }
            Option("enabled") { Text("Enabled") }
            Option("disabled") { Text("Disabled") }
        }
    }
}

@Composable
private fun FlagsTable(
    flags: List<FlagResponse>,
    viewModel: FlagsViewModel,
    globalState: flagent.frontend.state.GlobalState
) {
    Table({
        style {
            width(100.percent)
            borderCollapse("collapse")
            backgroundColor(Color.white)
            borderRadius(8.px)
            overflow("hidden")
            property("box-shadow", "0 1px 3px rgba(0, 0, 0, 0.1)")
        }
    }) {
        Thead {
            Tr({
                style {
                    backgroundColor(Color("#F8FAFC"))
                    borderBottom(2.px, LineStyle.Solid, Color("#E2E8F0"))
                }
            }) {
                SortableHeader("ID", "id", viewModel)
                SortableHeader("Description", "description", viewModel)
                Th({ style { padding(12.px); textAlign("center") } }) { Text("Status") }
                Th({ style { padding(12.px); textAlign("center") } }) { Text("Updated") }
                Th({ style { padding(12.px); textAlign("right") } }) { Text("Actions") }
            }
        }
        
        Tbody {
            flags.forEach { flag ->
                FlagRow(flag, viewModel, globalState)
            }
        }
    }
}

@Composable
private fun SortableHeader(label: String, column: String, viewModel: FlagsViewModel) {
    Th({
        onClick { viewModel.setSortColumn(column) }
        style {
            padding(12.px)
            textAlign("left")
            fontSize(12.px)
            fontWeight(600)
            color(Color("#64748B"))
            textTransform("uppercase")
            cursor("pointer")
            userSelect("none")
        }
    }) {
        Div({
            style {
                display(DisplayStyle.Flex)
                alignItems(AlignItems.Center)
                gap(4.px)
            }
        }) {
            Text(label)
            if (viewModel.sortColumn == column) {
                Icon(if (viewModel.sortAscending) "arrow_upward" else "arrow_downward", size = 16.px)
            }
        }
    }
}

@Composable
private fun FlagRow(
    flag: FlagResponse,
    viewModel: FlagsViewModel,
    globalState: flagent.frontend.state.GlobalState
) {
    Tr({
        style {
            borderBottom(1.px, LineStyle.Solid, Color("#E2E8F0"))
            property("transition", "background-color 0.2s")
        }
        // Hover effects handled via CSS
    }) {
        Td({
            onClick { Router.navigateTo(Route.FlagDetail(flag.id)) }
            style {
                padding(12.px)
                fontSize(14.px)
                fontWeight(600)
                color(FlagentTheme.Primary)
                cursor("pointer")
            }
        }) {
            Text("#${flag.id}")
        }
        
        Td({
            onClick { Router.navigateTo(Route.FlagDetail(flag.id)) }
            style {
                padding(12.px)
                fontSize(14.px)
                color(Color("#1E293B"))
                cursor("pointer")
            }
        }) {
            Text(flag.description)
        }
        
        Td({
            style {
                padding(12.px)
                textAlign("center")
            }
        }) {
            Span({
                style {
                    padding(4.px, 12.px)
                    backgroundColor(if (flag.enabled) Color("#DCFCE7") else Color("#FEE2E2"))
                    color(if (flag.enabled) Color("#166534") else Color("#991B1B"))
                    borderRadius(12.px)
                    fontSize(12.px)
                    fontWeight(500)
                }
            }) {
                Text(if (flag.enabled) "Enabled" else "Disabled")
            }
        }
        
        Td({
            style {
                padding(12.px)
                textAlign("center")
                fontSize(14.px)
                color(Color("#64748B"))
            }
        }) {
            Text(flag.updatedBy ?: "Unknown")
        }
        
        Td({
            style {
                padding(12.px)
                textAlign("right")
            }
        }) {
            Div({
                style {
                    display(DisplayStyle.Flex)
                    gap(8.px)
                    justifyContent(JustifyContent.FlexEnd)
                }
            }) {
                // Toggle button
                Button({
                    onClick {
                        viewModel.toggleFlag(flag.id, !flag.enabled)
                        globalState.addNotification(
                            Notification(
                                message = "Flag ${if (flag.enabled) "disabled" else "enabled"}",
                                type = NotificationType.SUCCESS
                            )
                        )
                    }
                    style {
                        padding(6.px, 12.px)
                        backgroundColor(Color.transparent)
                        color(FlagentTheme.Primary)
                        border(1.px, LineStyle.Solid, FlagentTheme.Primary)
                        borderRadius(4.px)
                        cursor("pointer")
                        fontSize(12.px)
                    }
                }) {
                    Text(if (flag.enabled) "Disable" else "Enable")
                }
                
                // Delete button
                Button({
                    onClick {
                        if (kotlinx.browser.window.confirm("Delete flag #${flag.id}?")) {
                            viewModel.deleteFlag(flag.id) {
                                globalState.addNotification(
                                    Notification(
                                        message = "Flag deleted successfully",
                                        type = NotificationType.SUCCESS
                                    )
                                )
                            }
                        }
                    }
                    style {
                        padding(6.px, 12.px)
                        backgroundColor(Color.transparent)
                        color(Color("#EF4444"))
                        border(1.px, LineStyle.Solid, Color("#EF4444"))
                        borderRadius(4.px)
                        cursor("pointer")
                        fontSize(12.px)
                    }
                }) {
                    Text("Delete")
                }
            }
        }
    }
}

@Composable
private fun DeletedFlagsSection(viewModel: FlagsViewModel, globalState: flagent.frontend.state.GlobalState) {
    Div({
        style {
            marginTop(48.px)
        }
    }) {
        H2({
            style {
                fontSize(20.px)
                fontWeight(600)
                color(Color("#64748B"))
                marginBottom(16.px)
            }
        }) {
            Text("Deleted Flags")
        }
        
        viewModel.deletedFlags.forEach { flag ->
            Div({
                style {
                    padding(16.px)
                    backgroundColor(Color("#FEF2F2"))
                    border(1.px, LineStyle.Solid, Color("#FEE2E2"))
                    borderRadius(6.px)
                    marginBottom(12.px)
                    display(DisplayStyle.Flex)
                    justifyContent(JustifyContent.SpaceBetween)
                    alignItems(AlignItems.Center)
                }
            }) {
                Span({
                    style {
                        fontSize(14.px)
                        color(Color("#991B1B"))
                    }
                }) {
                    Text("#${flag.id} - ${flag.description}")
                }
                
                Button({
                    onClick {
                        viewModel.restoreFlag(flag.id) {
                            globalState.addNotification(
                                Notification(
                                    message = "Flag restored successfully",
                                    type = NotificationType.SUCCESS
                                )
                            )
                        }
                    }
                    style {
                        padding(8.px, 16.px)
                        backgroundColor(Color("#10B981"))
                        color(Color.white)
                        border(0.px)
                        borderRadius(4.px)
                        cursor("pointer")
                        fontSize(12.px)
                    }
                }) {
                    Text("Restore")
                }
            }
        }
    }
}

@Composable
private fun ErrorBanner(error: String, onDismiss: () -> Unit) {
    Div({
        style {
            padding(16.px)
            backgroundColor(Color("#FEE2E2"))
            borderRadius(8.px)
            marginBottom(24.px)
            display(DisplayStyle.Flex)
            justifyContent(JustifyContent.SpaceBetween)
            alignItems(AlignItems.Center)
        }
    }) {
        Span({
            style {
                fontSize(14.px)
                color(Color("#991B1B"))
            }
        }) {
            Text(error)
        }
        
        Button({
            onClick { onDismiss() }
            style {
                padding(4.px)
                backgroundColor(Color.transparent)
                border(0.px)
                cursor("pointer")
                color(Color("#991B1B"))
            }
        }) {
            Icon("close", size = 20.px)
        }
    }
}
