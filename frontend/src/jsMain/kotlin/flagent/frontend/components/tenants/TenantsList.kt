package flagent.frontend.components.tenants

import androidx.compose.runtime.*
import flagent.frontend.components.Icon
import flagent.frontend.components.Modal
import org.jetbrains.compose.web.attributes.InputType
import flagent.frontend.components.common.EmptyState
import flagent.frontend.components.common.SkeletonLoader
import flagent.frontend.state.LocalThemeMode
import flagent.frontend.state.Tenant
import flagent.frontend.state.ThemeMode
import flagent.frontend.theme.FlagentTheme
import flagent.frontend.util.borderBottom
import flagent.frontend.util.borderCollapse
import flagent.frontend.util.textTransform
import flagent.frontend.viewmodel.TenantViewModel
import kotlinx.browser.localStorage
import kotlinx.browser.window
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

/**
 * Tenants List component (Phase 2)
 * Uses shared tenantViewModel when provided (from App/ShellLayout) so Switch and TenantSwitcher stay in sync.
 */
private sealed class GroupByOption {
    object None : GroupByOption()
    object ByPlan : GroupByOption()
    object ByStatus : GroupByOption()
}

@Composable
fun TenantsList(tenantViewModel: TenantViewModel? = null) {
    val viewModel = tenantViewModel ?: remember { TenantViewModel() }
    val showCreateForm = remember { mutableStateOf(false) }
    val themeMode = LocalThemeMode.current
    val searchQuery = remember { mutableStateOf("") }
    val planFilter = remember { mutableStateOf<String?>(null) }
    val statusFilter = remember { mutableStateOf<String?>(null) }
    val groupBy = remember { mutableStateOf<GroupByOption>(GroupByOption.None) }

    val filteredTenants = remember(viewModel.tenants, searchQuery.value, planFilter.value, statusFilter.value) {
        viewModel.tenants
            .filter { t ->
                val matchesSearch = searchQuery.value.isBlank() ||
                    t.name.contains(searchQuery.value, ignoreCase = true) ||
                    t.key.contains(searchQuery.value, ignoreCase = true)
                val matchesPlan = planFilter.value == null || t.plan == planFilter.value
                val matchesStatus = statusFilter.value == null || t.status == statusFilter.value
                matchesSearch && matchesPlan && matchesStatus
            }
    }

    val groupedTenants = remember(filteredTenants, groupBy.value) {
        when (groupBy.value) {
            is GroupByOption.None -> listOf("" to filteredTenants)
            is GroupByOption.ByPlan -> filteredTenants.groupBy { it.plan }.toList().sortedBy { it.first }
            is GroupByOption.ByStatus -> filteredTenants.groupBy { it.status }.toList().sortedBy { it.first }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadTenants()
        if (window.location.search.contains("create=1")) {
            showCreateForm.value = true
        }
    }
    
    Div({
        style {
            backgroundColor(FlagentTheme.cardBg(themeMode))
            borderRadius(8.px)
            padding(24.px)
            property("border", "1px solid ${FlagentTheme.cardBorder(themeMode)}")
        }
    }) {
        Div({
            style {
                display(DisplayStyle.Flex)
                justifyContent(JustifyContent.SpaceBetween)
                alignItems(AlignItems.Center)
                marginBottom(24.px)
            }
        }) {
            H2({
                style {
                    fontSize(20.px)
                    fontWeight(600)
                    color(FlagentTheme.text(themeMode))
                    margin(0.px)
                }
            }) {
                Text("Tenants")
            }
            
            Button({
                onClick { showCreateForm.value = true }
                style {
                    padding(10.px, 16.px)
                    backgroundColor(FlagentTheme.Primary)
                    color(Color.white)
                    border(0.px)
                    borderRadius(6.px)
                    cursor("pointer")
                    fontSize(14.px)
                }
            }) {
                Text("Create Tenant")
            }
        }
        
        if (showCreateForm.value) {
            CreateTenantModal(
                onDismiss = { showCreateForm.value = false },
                onCreate = { key, name, plan, ownerEmail ->
                    viewModel.createTenant(key, name, plan, ownerEmail) { tenant, apiKey ->
                        localStorage.setItem("api_key", apiKey)
                        viewModel.storeApiKeyForTenant(tenant.id, apiKey)
                        viewModel.switchTenant(tenant)
                        showCreateForm.value = false
                    }
                },
                confirmLoading = viewModel.isLoading
            )
        }
        
        viewModel.error?.let { err ->
            Div({
                style {
                    padding(12.px)
                    marginBottom(16.px)
                    backgroundColor(Color("#FEF2F2"))
                    color(Color("#991B1B"))
                    borderRadius(6.px)
                    fontSize(14.px)
                }
            }) {
                Text(err)
            }
        }

        if (!viewModel.tenants.isEmpty()) {
            Div({
                style {
                    display(DisplayStyle.Flex)
                    gap(12.px)
                    marginBottom(16.px)
                    alignItems(AlignItems.Center)
                    flexWrap(FlexWrap.Wrap)
                }
            }) {
                Div({
                    style {
                        display(DisplayStyle.Flex)
                        alignItems(AlignItems.Center)
                        gap(8.px)
                        flex(1)
                        minWidth(200.px)
                    }
                }) {
                    Icon(name = "search", size = 18.px, color = FlagentTheme.textLight(themeMode))
                    Input(InputType.Text) {
                        attr("placeholder", "Search by name or key")
                        value(searchQuery.value)
                        onInput { searchQuery.value = it.value }
                        style {
                            flex(1)
                            minWidth(140.px)
                            padding(10.px, 12.px)
                            backgroundColor(FlagentTheme.inputBg(themeMode))
                            color(FlagentTheme.text(themeMode))
                            border(1.px, LineStyle.Solid, FlagentTheme.inputBorder(themeMode))
                            borderRadius(6.px)
                            fontSize(14.px)
                        }
                    }
                }
                Select({
                    attr("value", planFilter.value ?: "")
                    onChange { planFilter.value = (it.target as org.w3c.dom.HTMLSelectElement).value.takeIf { v -> v.isNotBlank() } }
                    style {
                        padding(10.px, 12.px)
                        backgroundColor(FlagentTheme.inputBg(themeMode))
                        color(FlagentTheme.text(themeMode))
                        border(1.px, LineStyle.Solid, FlagentTheme.inputBorder(themeMode))
                        borderRadius(6.px)
                        fontSize(14.px)
                        minWidth(120.px)
                    }
                }) {
                    Option(value = "") { Text("All plans") }
                    Option(value = "STARTER") { Text("STARTER") }
                    Option(value = "GROWTH") { Text("GROWTH") }
                    Option(value = "SCALE") { Text("SCALE") }
                    Option(value = "ENTERPRISE") { Text("ENTERPRISE") }
                }
                Select({
                    attr("value", statusFilter.value ?: "")
                    onChange { statusFilter.value = (it.target as org.w3c.dom.HTMLSelectElement).value.takeIf { v -> v.isNotBlank() } }
                    style {
                        padding(10.px, 12.px)
                        backgroundColor(FlagentTheme.inputBg(themeMode))
                        color(FlagentTheme.text(themeMode))
                        border(1.px, LineStyle.Solid, FlagentTheme.inputBorder(themeMode))
                        borderRadius(6.px)
                        fontSize(14.px)
                        minWidth(120.px)
                    }
                }) {
                    Option(value = "") { Text("All statuses") }
                    Option(value = "ACTIVE") { Text("ACTIVE") }
                    Option(value = "SUSPENDED") { Text("SUSPENDED") }
                    Option(value = "CANCELLED") { Text("CANCELLED") }
                }
                Select({
                    attr("value", when (groupBy.value) { is GroupByOption.ByPlan -> "plan"; is GroupByOption.ByStatus -> "status"; else -> "none" })
                    onChange {
                        groupBy.value = when ((it.target as org.w3c.dom.HTMLSelectElement).value) {
                            "plan" -> GroupByOption.ByPlan
                            "status" -> GroupByOption.ByStatus
                            else -> GroupByOption.None
                        }
                    }
                    style {
                        padding(10.px, 12.px)
                        backgroundColor(FlagentTheme.inputBg(themeMode))
                        color(FlagentTheme.text(themeMode))
                        border(1.px, LineStyle.Solid, FlagentTheme.inputBorder(themeMode))
                        borderRadius(6.px)
                        fontSize(14.px)
                        minWidth(130.px)
                    }
                }) {
                    Option(value = "none") { Text("No grouping") }
                    Option(value = "plan") { Text("Group by plan") }
                    Option(value = "status") { Text("Group by status") }
                }
            }
        }

        if (viewModel.isLoading) {
            SkeletonLoader(height = 200.px)
        } else if (viewModel.tenants.isEmpty()) {
            EmptyState(
                icon = "business",
                title = "No tenants",
                description = "Create your first tenant to get started",
                actionLabel = "Create Tenant",
                onAction = { showCreateForm.value = true }
            )
        } else if (filteredTenants.isEmpty()) {
            EmptyState(
                icon = "search",
                title = "No tenants match filters",
                description = "Try adjusting search or filters",
                actionLabel = "Clear filters",
                onAction = {
                    searchQuery.value = ""
                    planFilter.value = null
                    statusFilter.value = null
                }
            )
        } else {
            // Tenants table
            Table({
                style {
                    width(100.percent)
                    borderCollapse("collapse")
                }
            }) {
                Thead {
                    Tr({
                        style {
                            borderBottom(2.px, LineStyle.Solid, FlagentTheme.cardBorder(themeMode))
                        }
                    }) {
                        Th({
                            style {
                                textAlign("left")
                                padding(12.px)
                                fontSize(12.px)
                                fontWeight(600)
                                color(FlagentTheme.textLight(themeMode))
                                textTransform("uppercase")
                            }
                        }) {
                            Text("Name")
                        }
                        Th({
                            style {
                                textAlign("left")
                                padding(12.px)
                                fontSize(12.px)
                                fontWeight(600)
                                color(Color("#64748B"))
                                textTransform("uppercase")
                            }
                        }) {
                            Text("Key")
                        }
                        Th({
                            style {
                                textAlign("left")
                                padding(12.px)
                                fontSize(12.px)
                                fontWeight(600)
                                color(Color("#64748B"))
                                textTransform("uppercase")
                            }
                        }) {
                            Text("Plan")
                        }
                        Th({
                            style {
                                textAlign("left")
                                padding(12.px)
                                fontSize(12.px)
                                fontWeight(600)
                                color(Color("#64748B"))
                                textTransform("uppercase")
                            }
                        }) {
                            Text("Status")
                        }
                        Th({
                            style {
                                textAlign("right")
                                padding(12.px)
                                fontSize(12.px)
                                fontWeight(600)
                                color(Color("#64748B"))
                                textTransform("uppercase")
                            }
                        }) {
                            Text("Actions")
                        }
                    }
                }
                
                Tbody {
                    groupedTenants.forEach { (groupKey, tenants) ->
                        if (groupKey.isNotBlank()) {
                            Tr({
                                style {
                                    backgroundColor(FlagentTheme.inputBg(themeMode))
                                }
                            }) {
                                Td({
                                    attr("colspan", "5")
                                    style {
                                        padding(10.px, 12.px)
                                        fontSize(12.px)
                                        fontWeight(600)
                                        color(FlagentTheme.textLight(themeMode))
                                        textTransform("uppercase")
                                    }
                                }) {
                                    Text(groupKey)
                                }
                            }
                        }
                        tenants.forEach { tenant ->
                            TenantRow(themeMode = themeMode, tenant = tenant, onSwitch = { viewModel.switchTenant(tenant) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TenantRow(
    themeMode: ThemeMode,
    tenant: Tenant,
    onSwitch: () -> Unit
) {
    Tr({
        style {
            borderBottom(1.px, LineStyle.Solid, FlagentTheme.cardBorder(themeMode))
        }
    }) {
        Td({ style { padding(12.px); fontSize(14.px); fontWeight(600); color(FlagentTheme.text(themeMode)) } }) {
            Text(tenant.name)
        }
        Td({ style { padding(12.px); fontSize(14.px); color(FlagentTheme.textLight(themeMode)) } }) {
            Text(tenant.key)
        }
        Td({ style { padding(12.px); fontSize(14.px); color(FlagentTheme.textLight(themeMode)) } }) {
            Text(tenant.plan)
        }
        Td({ style { padding(12.px); fontSize(14.px); color(FlagentTheme.textLight(themeMode)) } }) {
            Text(tenant.status)
        }
        Td({ style { padding(12.px); textAlign("right") } }) {
            Button({
                onClick { onSwitch() }
                style {
                    padding(6.px, 12.px)
                    backgroundColor(FlagentTheme.Primary)
                    color(Color.white)
                    border(0.px)
                    borderRadius(4.px)
                    cursor("pointer")
                    fontSize(12.px)
                }
            }) {
                Text("Switch")
            }
        }
    }
}

@Composable
private fun CreateTenantModal(
    onDismiss: () -> Unit,
    onCreate: (key: String, name: String, plan: String, ownerEmail: String) -> Unit,
    confirmLoading: Boolean = false
) {
    val themeMode = LocalThemeMode.current
    val key = remember { mutableStateOf("") }
    val name = remember { mutableStateOf("") }
    val plan = remember { mutableStateOf("STARTER") }
    val ownerEmail = remember { mutableStateOf("") }
    
    Modal(
        title = "Create Tenant",
        onClose = onDismiss,
        onConfirm = {
            onCreate(key.value.trim(), name.value.trim(), plan.value, ownerEmail.value.trim())
            // Modal stays open; onSuccess in createTenant callback will close via onDismiss
        },
        confirmText = "Create",
        cancelText = "Cancel",
        showCancel = true,
        confirmDisabled = key.value.isBlank() || name.value.isBlank() || ownerEmail.value.isBlank(),
        confirmLoading = confirmLoading
    ) {
        Div({
            style {
                display(DisplayStyle.Flex)
                flexDirection(org.jetbrains.compose.web.css.FlexDirection.Column)
                gap(12.px)
            }
        }) {
            Div({ style { marginBottom(8.px) } }) {
                Span({ style { fontSize(14.px); fontWeight("500"); color(FlagentTheme.text(themeMode)); marginBottom(4.px); property("display", "block") } }) { Text("Key (unique identifier)") }
                Input(InputType.Text) {
                    value(key.value)
                    onInput { key.value = it.value }
                    style {
                        width(100.percent)
                        padding(10.px, 12.px)
                        backgroundColor(FlagentTheme.inputBg(themeMode))
                        color(FlagentTheme.text(themeMode))
                        border(1.px, LineStyle.Solid, FlagentTheme.inputBorder(themeMode))
                        borderRadius(6.px)
                    }
                }
            }
            Div({ style { marginBottom(8.px) } }) {
                Span({ style { fontSize(14.px); fontWeight("500"); color(FlagentTheme.text(themeMode)); marginBottom(4.px); property("display", "block") } }) { Text("Name") }
                Input(InputType.Text) {
                    value(name.value)
                    onInput { name.value = it.value }
                    style {
                        width(100.percent)
                        padding(10.px, 12.px)
                        backgroundColor(FlagentTheme.inputBg(themeMode))
                        color(FlagentTheme.text(themeMode))
                        border(1.px, LineStyle.Solid, FlagentTheme.inputBorder(themeMode))
                        borderRadius(6.px)
                    }
                }
            }
            Div({ style { marginBottom(8.px) } }) {
                Span({ style { fontSize(14.px); fontWeight("500"); color(FlagentTheme.text(themeMode)); marginBottom(4.px); property("display", "block") } }) { Text("Plan") }
                Select(attrs = {
                    attr("value", plan.value)
                    onChange { plan.value = (it.target as org.w3c.dom.HTMLSelectElement).value }
                    style {
                        width(100.percent)
                        padding(10.px, 12.px)
                        backgroundColor(FlagentTheme.inputBg(themeMode))
                        color(FlagentTheme.text(themeMode))
                        border(1.px, LineStyle.Solid, FlagentTheme.inputBorder(themeMode))
                        borderRadius(6.px)
                    }
                }) {
                    Option(value = "STARTER") { Text("STARTER") }
                    Option(value = "GROWTH") { Text("GROWTH") }
                    Option(value = "SCALE") { Text("SCALE") }
                    Option(value = "ENTERPRISE") { Text("ENTERPRISE") }
                }
            }
            Div({ style { marginBottom(8.px) } }) {
                Span({ style { fontSize(14.px); fontWeight("500"); color(FlagentTheme.text(themeMode)); marginBottom(4.px); property("display", "block") } }) { Text("Owner Email") }
                Input(InputType.Email) {
                    value(ownerEmail.value)
                    onInput { ownerEmail.value = it.value }
                    style {
                        width(100.percent)
                        padding(10.px, 12.px)
                        backgroundColor(FlagentTheme.inputBg(themeMode))
                        color(FlagentTheme.text(themeMode))
                        border(1.px, LineStyle.Solid, FlagentTheme.inputBorder(themeMode))
                        borderRadius(6.px)
                    }
                }
            }
        }
    }
}
