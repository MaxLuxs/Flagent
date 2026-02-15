package flagent.frontend.components.tenants

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import flagent.frontend.api.ApiClient
import flagent.frontend.api.CreateApiKeyRequest
import flagent.frontend.components.Icon
import flagent.frontend.components.Modal
import flagent.frontend.components.common.EmptyState
import flagent.frontend.components.common.SkeletonLoader
import flagent.frontend.i18n.LocalizedStrings
import flagent.frontend.navigation.Route
import flagent.frontend.navigation.Router
import flagent.frontend.state.LocalThemeMode
import flagent.frontend.state.Tenant
import flagent.frontend.state.ThemeMode
import flagent.frontend.theme.FlagentTheme
import flagent.frontend.util.ErrorHandler
import flagent.frontend.util.borderBottom
import flagent.frontend.util.borderCollapse
import flagent.frontend.util.textTransform
import flagent.frontend.viewmodel.TenantViewModel
import kotlinx.browser.localStorage
import kotlinx.browser.window
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.Color
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
import org.jetbrains.compose.web.css.flexDirection
import org.jetbrains.compose.web.css.flexWrap
import org.jetbrains.compose.web.css.fontSize
import org.jetbrains.compose.web.css.fontWeight
import org.jetbrains.compose.web.css.gap
import org.jetbrains.compose.web.css.justifyContent
import org.jetbrains.compose.web.css.margin
import org.jetbrains.compose.web.css.marginBottom
import org.jetbrains.compose.web.css.marginTop
import org.jetbrains.compose.web.css.minWidth
import org.jetbrains.compose.web.css.padding
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.textAlign
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Br
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H2
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

    val filteredTenants =
        remember(viewModel.tenants, searchQuery.value, planFilter.value, statusFilter.value) {
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
            is GroupByOption.ByPlan -> filteredTenants.groupBy { it.plan }.toList()
                .sortedBy { it.first }

            is GroupByOption.ByStatus -> filteredTenants.groupBy { it.status }.toList()
                .sortedBy { it.first }
        }
    }

    val hasApiKeyInStorage =
        remember { mutableStateOf((localStorage.getItem("api_key") ?: "").isNotBlank()) }
    val pastedApiKey = remember { mutableStateOf("") }
    val useKeyLoading = remember { mutableStateOf(false) }
    val useKeyError = remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        viewModel.loadTenants()
        hasApiKeyInStorage.value = (localStorage.getItem("api_key") ?: "").isNotBlank()
    }
    LaunchedEffect(viewModel.tenants, viewModel.isLoading) {
        if (viewModel.isLoading) return@LaunchedEffect
        if (window.location.search.contains("create=1")) {
            showCreateForm.value = viewModel.tenants.isEmpty()
            if (viewModel.tenants.isNotEmpty()) {
                window.history.replaceState(null, "", Route.Tenants.PATH)
            }
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
        if (!hasApiKeyInStorage.value && viewModel.tenants.isNotEmpty()) {
            Div({
                style {
                    padding(12.px, 16.px)
                    marginBottom(16.px)
                    backgroundColor(FlagentTheme.inputBg(themeMode))
                    borderRadius(8.px)
                    property("border", "1px solid ${FlagentTheme.cardBorder(themeMode)}")
                    fontSize(14.px)
                    color(FlagentTheme.text(themeMode))
                }
            }) {
                Text(LocalizedStrings.selectTenantToContinue)
                Br()
                Text(LocalizedStrings.tenantApiKeyOnlyInThisBrowser)
            }
        }
        if (!hasApiKeyInStorage.value) {
            Div({
                style {
                    padding(16.px)
                    marginBottom(16.px)
                    backgroundColor(FlagentTheme.inputBg(themeMode))
                    borderRadius(8.px)
                    property("border", "1px solid ${FlagentTheme.cardBorder(themeMode)}")
                }
            }) {
                P({
                    style {
                        fontSize(14.px)
                        fontWeight(600)
                        color(FlagentTheme.text(themeMode))
                        margin(0.px, 0.px, 8.px, 0.px)
                    }
                }) {
                    Text(LocalizedStrings.useApiKeyFromAnotherDevice)
                }
                P({
                    style {
                        fontSize(13.px)
                        color(FlagentTheme.textLight(themeMode))
                        margin(0.px, 0.px, 12.px, 0.px)
                    }
                }) {
                    Text(LocalizedStrings.useApiKeyFromAnotherDeviceDescription)
                }
                useKeyError.value?.let { err ->
                    P({
                        style {
                            fontSize(13.px)
                            color(FlagentTheme.errorText(themeMode))
                            marginBottom(8.px)
                        }
                    }) {
                        Text(err)
                    }
                }
                Div({
                    style {
                        display(DisplayStyle.Flex)
                        gap(8.px)
                        alignItems(AlignItems.Center)
                        flexWrap(FlexWrap.Wrap)
                    }
                }) {
                    Input(InputType.Text) {
                        attr(
                            "placeholder",
                            LocalizedStrings.apiKeyPastePlaceholder
                        )
                        attr("type", "password")
                        value(pastedApiKey.value)
                        onInput { pastedApiKey.value = it.value; useKeyError.value = null }
                        style {
                            flex(1)
                            minWidth(200.px)
                            padding(10.px, 12.px)
                            backgroundColor(FlagentTheme.cardBg(themeMode))
                            color(FlagentTheme.text(themeMode))
                            border(1.px, LineStyle.Solid, FlagentTheme.inputBorder(themeMode))
                            borderRadius(6.px)
                            fontSize(14.px)
                        }
                    }
                    Button({
                        onClick {
                            val key = pastedApiKey.value.trim()
                            if (key.isBlank()) {
                                useKeyError.value = LocalizedStrings.apiKeyPastePlaceholder
                                return@onClick
                            }
                            useKeyLoading.value = true
                            useKeyError.value = null
                            localStorage.setItem("api_key", key)
                            scope.launch {
                                try {
                                    val response = ApiClient.getTenantMe()
                                    val tenant = Tenant(
                                        id = response.id.toString(),
                                        key = response.key,
                                        name = response.name,
                                        plan = response.plan,
                                        status = response.status
                                    )
                                    viewModel.storeApiKeyForTenant(tenant.id, key)
                                    viewModel.switchTenant(tenant)
                                    viewModel.loadTenants()
                                    hasApiKeyInStorage.value = true
                                    pastedApiKey.value = ""
                                    Router.navigateTo(Route.Dashboard)
                                } catch (e: Throwable) {
                                    localStorage.removeItem("api_key")
                                    useKeyError.value =
                                        ErrorHandler.getUserMessage(ErrorHandler.handle(e))
                                }
                                useKeyLoading.value = false
                            }
                        }
                        style {
                            padding(10.px, 16.px)
                            backgroundColor(FlagentTheme.Primary)
                            color(Color.white)
                            border(0.px)
                            borderRadius(6.px)
                            cursor(if (useKeyLoading.value) "wait" else "pointer")
                            fontSize(14.px)
                        }
                    }) {
                        Text(if (useKeyLoading.value) "…" else LocalizedStrings.useThisKey)
                    }
                }
            }
        }
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
                Text(LocalizedStrings.tenantsNav)
            }

            Button({
                onClick {
                    viewModel.clearError()
                    showCreateForm.value = true
                }
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
                Text(LocalizedStrings.createTenant)
            }
        }

        val createdApiKey = remember { mutableStateOf<String?>(null) }
        val createdTenantName = remember { mutableStateOf<String?>(null) }
        val createKeyForTenant = remember { mutableStateOf<Tenant?>(null) }
        val createdNewApiKey = remember { mutableStateOf<String?>(null) }
        val createKeyLoading = remember { mutableStateOf(false) }
        val createKeyError = remember { mutableStateOf<String?>(null) }
        if (showCreateForm.value) {
            CreateTenantModal(
                onDismiss = {
                    showCreateForm.value = false
                    createdApiKey.value = null
                    createdTenantName.value = null
                    viewModel.clearError()
                },
                onCreate = { key, name, plan, ownerEmail ->
                    viewModel.createTenant(key, name, plan, ownerEmail) { tenant, apiKey ->
                        localStorage.setItem("api_key", apiKey)
                        viewModel.storeApiKeyForTenant(tenant.id, apiKey)
                        viewModel.switchTenant(tenant)
                        hasApiKeyInStorage.value = true
                        createdApiKey.value = apiKey
                        createdTenantName.value = tenant.name
                    }
                },
                createdApiKey = createdApiKey.value,
                createdTenantName = createdTenantName.value,
                onContinueToDashboard = {
                    showCreateForm.value = false
                    createdApiKey.value = null
                    createdTenantName.value = null
                    Router.navigateTo(Route.Dashboard)
                },
                confirmLoading = viewModel.isLoading,
                createError = viewModel.error,
                onClearError = { viewModel.clearError() }
            )
        }
        createKeyForTenant.value?.let { tenant ->
            CreateApiKeyModal(
                tenant = tenant,
                createdNewApiKey = createdNewApiKey.value,
                createKeyLoading = createKeyLoading.value,
                createKeyError = createKeyError.value,
                onDismiss = {
                    createKeyForTenant.value = null
                    createdNewApiKey.value = null
                    createKeyError.value = null
                },
                onCreate = { keyName ->
                    createKeyLoading.value = true
                    createKeyError.value = null
                    scope.launch {
                        try {
                            val response = ApiClient.createTenantApiKey(
                                tenant.id.toLong(),
                                CreateApiKeyRequest(
                                    name = keyName.ifBlank { "Recovery" },
                                    scopes = emptyList()
                                )
                            )
                            createdNewApiKey.value = response.apiKey
                        } catch (e: Throwable) {
                            createKeyError.value =
                                ErrorHandler.getUserMessage(ErrorHandler.handle(e))
                        }
                        createKeyLoading.value = false
                    }
                },
                onUseInThisBrowser = {
                    createdNewApiKey.value?.let { key ->
                        viewModel.storeApiKeyForTenant(tenant.id, key)
                        localStorage.setItem("api_key", key)
                        viewModel.switchTenant(tenant)
                        hasApiKeyInStorage.value = true
                    }
                    createKeyForTenant.value = null
                    createdNewApiKey.value = null
                }
            )
        }

        if (!showCreateForm.value && viewModel.error != null) {
            val err = viewModel.error!!
            Div({
                style {
                    padding(12.px)
                    marginBottom(16.px)
                    backgroundColor(FlagentTheme.errorBg(themeMode))
                    color(FlagentTheme.errorText(themeMode))
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
                        attr(
                            "placeholder",
                            LocalizedStrings.searchByNameOrKey
                        )
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
                    onChange {
                        planFilter.value = it.target.value.takeIf { v -> v.isNotBlank() }
                    }
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
                    Option(value = "") { Text(LocalizedStrings.allPlans) }
                    Option(value = "STARTER") { Text("STARTER") }
                    Option(value = "GROWTH") { Text("GROWTH") }
                    Option(value = "SCALE") { Text("SCALE") }
                    Option(value = "ENTERPRISE") { Text("ENTERPRISE") }
                }
                Select({
                    attr("value", statusFilter.value ?: "")
                    onChange {
                        statusFilter.value = it.target.value.takeIf { v -> v.isNotBlank() }
                    }
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
                    Option(value = "") { Text(LocalizedStrings.allStatuses) }
                    Option(value = "ACTIVE") { Text("ACTIVE") }
                    Option(value = "SUSPENDED") { Text("SUSPENDED") }
                    Option(value = "CANCELLED") { Text("CANCELLED") }
                }
                Select({
                    attr(
                        "value", when (groupBy.value) {
                            is GroupByOption.ByPlan -> "plan"; is GroupByOption.ByStatus -> "status"; else -> "none"
                        }
                    )
                    onChange {
                        groupBy.value = when (it.target.value) {
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
                    Option(value = "none") { Text(LocalizedStrings.noGrouping) }
                    Option(value = "plan") { Text(LocalizedStrings.groupByPlan) }
                    Option(value = "status") { Text(LocalizedStrings.groupByStatus) }
                }
            }
        }

        if (viewModel.isLoading) {
            SkeletonLoader(height = 200.px)
        } else if (viewModel.tenants.isEmpty()) {
            EmptyState(
                icon = "business",
                title = LocalizedStrings.noTenants,
                description = LocalizedStrings.createFirstTenantDescription,
                actionLabel = LocalizedStrings.createTenant,
                onAction = { viewModel.clearError(); showCreateForm.value = true }
            )
        } else if (filteredTenants.isEmpty()) {
            EmptyState(
                icon = "search",
                title = LocalizedStrings.noTenantsMatchFilters,
                description = LocalizedStrings.tryAdjustingFilters,
                actionLabel = LocalizedStrings.clearFilters,
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
                            Text(LocalizedStrings.nameLabel)
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
                            Text(LocalizedStrings.keyColumn)
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
                            Text(LocalizedStrings.plan)
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
                            Text(LocalizedStrings.status)
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
                            Text(LocalizedStrings.actionsLabel)
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
                            TenantRow(
                                themeMode = themeMode,
                                tenant = tenant,
                                hasApiKey = viewModel.hasApiKeyForTenant(tenant.id),
                                onSwitch = {
                                    if (viewModel.hasApiKeyForTenant(tenant.id)) {
                                        viewModel.clearError()
                                        viewModel.switchTenant(tenant)
                                        hasApiKeyInStorage.value = true
                                        Router.navigateTo(Route.Dashboard)
                                    } else {
                                        viewModel.setError(LocalizedStrings.apiKeyNotAvailableForTenant)
                                    }
                                },
                                onCreateApiKey = { createKeyForTenant.value = it }
                            )
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
    hasApiKey: Boolean,
    onSwitch: () -> Unit,
    onCreateApiKey: (Tenant) -> Unit
) {
    Tr({
        style {
            borderBottom(1.px, LineStyle.Solid, FlagentTheme.cardBorder(themeMode))
        }
    }) {
        Td({
            style {
                padding(12.px); fontSize(14.px); fontWeight(600); color(
                FlagentTheme.text(
                    themeMode
                )
            )
            }
        }) {
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
            Div({
                style {
                    display(DisplayStyle.Flex)
                    gap(8.px)
                    justifyContent(JustifyContent.FlexEnd)
                    flexWrap(FlexWrap.Wrap)
                }
            }) {
                Button({
                    attr(
                        "title",
                        if (hasApiKey) "" else LocalizedStrings.apiKeyNotAvailableForTenant
                    )
                    onClick { onSwitch() }
                    style {
                        padding(6.px, 12.px)
                        backgroundColor(
                            if (hasApiKey) FlagentTheme.Primary else FlagentTheme.cardBorder(
                                themeMode
                            )
                        )
                        color(if (hasApiKey) Color.white else FlagentTheme.textLight(themeMode))
                        border(0.px)
                        borderRadius(4.px)
                        cursor("pointer")
                        fontSize(12.px)
                    }
                }) {
                    Text(LocalizedStrings.switchTenant)
                }
                Button({
                    onClick { onCreateApiKey(tenant) }
                    style {
                        padding(6.px, 12.px)
                        backgroundColor(FlagentTheme.cardBorder(themeMode))
                        color(FlagentTheme.text(themeMode))
                        border(1.px, LineStyle.Solid, FlagentTheme.inputBorder(themeMode))
                        borderRadius(4.px)
                        cursor("pointer")
                        fontSize(12.px)
                    }
                }) {
                    Text(LocalizedStrings.createApiKey)
                }
            }
        }
    }
}

@Composable
private fun CreateTenantModal(
    onDismiss: () -> Unit,
    onCreate: (key: String, name: String, plan: String, ownerEmail: String) -> Unit,
    createdApiKey: String?,
    createdTenantName: String?,
    onContinueToDashboard: () -> Unit,
    confirmLoading: Boolean = false,
    createError: String? = null,
    onClearError: () -> Unit = {}
) {
    val themeMode = LocalThemeMode.current
    val key = remember { mutableStateOf("") }
    val name = remember { mutableStateOf("") }
    val plan = remember { mutableStateOf("STARTER") }
    val ownerEmail = remember { mutableStateOf("") }
    val copyFeedback = remember { mutableStateOf(false) }

    if (createdApiKey != null) {
        Modal(
            title = LocalizedStrings.tenantCreatedSaveKey,
            onClose = onContinueToDashboard,
            onConfirm = onContinueToDashboard,
            confirmText = LocalizedStrings.continueToDashboard,
            cancelText = "",
            showCancel = false,
            confirmDisabled = false,
            confirmLoading = false
        ) {
            Div({
                style {
                    display(DisplayStyle.Flex)
                    flexDirection(org.jetbrains.compose.web.css.FlexDirection.Column)
                    gap(16.px)
                }
            }) {
                createdTenantName?.let { tenantName ->
                    P({
                        style {
                            fontSize(14.px)
                            fontWeight(600)
                            color(FlagentTheme.text(themeMode))
                            margin(0.px)
                        }
                    }) {
                        Text("«$tenantName»")
                    }
                }
                P({
                    style {
                        fontSize(13.px)
                        color(FlagentTheme.textLight(themeMode))
                        margin(0.px)
                    }
                }) {
                    Text(LocalizedStrings.apiKeyUseHint)
                }
                Div({
                    style {
                        display(DisplayStyle.Flex)
                        gap(8.px)
                        alignItems(AlignItems.Center)
                        flexWrap(FlexWrap.Wrap)
                    }
                }) {
                    Input(InputType.Text) {
                        value(createdApiKey)
                        attr("readonly", "readonly")
                        style {
                            flex(1)
                            minWidth(200.px)
                            padding(10.px, 12.px)
                            backgroundColor(FlagentTheme.inputBg(themeMode))
                            color(FlagentTheme.text(themeMode))
                            border(1.px, LineStyle.Solid, FlagentTheme.cardBorder(themeMode))
                            borderRadius(6.px)
                            fontSize(13.px)
                            property("font-family", "monospace")
                        }
                    }
                    Button({
                        onClick {
                            try {
                                val clip = js("navigator.clipboard")
                                if (clip != null && clip != js("undefined")) {
                                    clip.writeText(createdApiKey)
                                }
                                copyFeedback.value = true
                                window.setTimeout(
                                    { copyFeedback.value = false },
                                    2000
                                )
                            } catch (_: Throwable) {
                            }
                        }
                        style {
                            padding(10.px, 16.px)
                            backgroundColor(if (copyFeedback.value) Color("#10B981") else FlagentTheme.Primary)
                            color(Color.white)
                            border(0.px)
                            borderRadius(6.px)
                            cursor("pointer")
                            fontSize(14.px)
                        }
                    }) {
                        Text(if (copyFeedback.value) LocalizedStrings.copiedToClipboard else LocalizedStrings.copyApiKey)
                    }
                }
            }
        }
        return@CreateTenantModal
    }

    Modal(
        title = "Create Tenant",
        onClose = onDismiss,
        onConfirm = {
            onCreate(key.value.trim(), name.value.trim(), plan.value, ownerEmail.value.trim())
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
            createError?.let { err ->
                P({
                    style {
                        fontSize(13.px)
                        color(FlagentTheme.errorText(themeMode))
                        margin(0.px)
                        padding(8.px, 12.px)
                        backgroundColor(FlagentTheme.errorBg(themeMode))
                        borderRadius(6.px)
                    }
                }) {
                    Text(err)
                }
            }
            Div({ style { marginBottom(8.px) } }) {
                Span({
                    style {
                        fontSize(14.px); fontWeight("500"); color(FlagentTheme.text(themeMode)); marginBottom(
                        4.px
                    ); property("display", "block")
                    }
                }) { Text(LocalizedStrings.keyUniqueIdentifier) }
                Input(InputType.Text) {
                    value(key.value)
                    onInput { key.value = it.value; onClearError() }
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
                Span({
                    style {
                        fontSize(14.px); fontWeight("500"); color(FlagentTheme.text(themeMode)); marginBottom(
                        4.px
                    ); property("display", "block")
                    }
                }) { Text(LocalizedStrings.nameLabel) }
                Input(InputType.Text) {
                    value(name.value)
                    onInput { name.value = it.value; onClearError() }
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
                Span({
                    style {
                        fontSize(14.px); fontWeight("500"); color(FlagentTheme.text(themeMode)); marginBottom(
                        4.px
                    ); property("display", "block")
                    }
                }) { Text(LocalizedStrings.plan) }
                Select(attrs = {
                    attr("value", plan.value)
                    onChange {
                        plan.value = it.target.value; onClearError()
                    }
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
                Span({
                    style {
                        fontSize(14.px); fontWeight("500"); color(FlagentTheme.text(themeMode)); marginBottom(
                        4.px
                    ); property("display", "block")
                    }
                }) { Text(LocalizedStrings.ownerEmail) }
                Input(InputType.Email) {
                    value(ownerEmail.value)
                    onInput { ownerEmail.value = it.value; onClearError() }
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
            P({
                style {
                    fontSize(12.px)
                    color(FlagentTheme.textLight(themeMode))
                    margin(0.px)
                    marginTop(4.px)
                }
            }) {
                Text(LocalizedStrings.afterCreateYouWillGetApiKey)
            }
        }
    }
}

@Composable
private fun CreateApiKeyModal(
    tenant: Tenant,
    createdNewApiKey: String?,
    createKeyLoading: Boolean,
    createKeyError: String?,
    onDismiss: () -> Unit,
    onCreate: (keyName: String) -> Unit,
    onUseInThisBrowser: () -> Unit
) {
    val themeMode = LocalThemeMode.current
    val keyName = remember { mutableStateOf("Recovery") }
    val copyFeedback = remember { mutableStateOf(false) }

    if (createdNewApiKey != null) {
        Modal(
            title = LocalizedStrings.newKeyCreatedSaveNow,
            onClose = onDismiss,
            onConfirm = onDismiss,
            confirmText = LocalizedStrings.closeButton,
            cancelText = "",
            showCancel = false,
            confirmDisabled = false,
            confirmLoading = false
        ) {
            Div({
                style {
                    display(DisplayStyle.Flex)
                    flexDirection(org.jetbrains.compose.web.css.FlexDirection.Column)
                    gap(16.px)
                }
            }) {
                P({
                    style {
                        fontSize(13.px)
                        color(FlagentTheme.textLight(themeMode))
                        margin(0.px)
                    }
                }) {
                    Text(LocalizedStrings.apiKeyUseHint)
                }
                Div({
                    style {
                        display(DisplayStyle.Flex)
                        gap(8.px)
                        alignItems(AlignItems.Center)
                        flexWrap(FlexWrap.Wrap)
                    }
                }) {
                    Input(InputType.Text) {
                        value(createdNewApiKey)
                        attr("readonly", "readonly")
                        style {
                            flex(1)
                            minWidth(200.px)
                            padding(10.px, 12.px)
                            backgroundColor(FlagentTheme.inputBg(themeMode))
                            color(FlagentTheme.text(themeMode))
                            border(1.px, LineStyle.Solid, FlagentTheme.cardBorder(themeMode))
                            borderRadius(6.px)
                            fontSize(13.px)
                            property("font-family", "monospace")
                        }
                    }
                    Button({
                        onClick {
                            try {
                                val clip = js("navigator.clipboard")
                                if (clip != null && clip != js("undefined")) {
                                    clip.writeText(createdNewApiKey)
                                }
                                copyFeedback.value = true
                                window.setTimeout(
                                    { copyFeedback.value = false },
                                    2000
                                )
                            } catch (_: Throwable) {
                            }
                        }
                        style {
                            padding(10.px, 16.px)
                            backgroundColor(if (copyFeedback.value) Color("#10B981") else FlagentTheme.Primary)
                            color(Color.white)
                            border(0.px)
                            borderRadius(6.px)
                            cursor("pointer")
                            fontSize(14.px)
                        }
                    }) {
                        Text(if (copyFeedback.value) LocalizedStrings.copiedToClipboard else LocalizedStrings.copyApiKey)
                    }
                }
                Button({
                    onClick { onUseInThisBrowser() }
                    style {
                        padding(10.px, 16.px)
                        backgroundColor(FlagentTheme.cardBorder(themeMode))
                        color(FlagentTheme.text(themeMode))
                        border(1.px, LineStyle.Solid, FlagentTheme.inputBorder(themeMode))
                        borderRadius(6.px)
                        cursor("pointer")
                        fontSize(14.px)
                    }
                }) {
                    Text(LocalizedStrings.useThisKeyInThisBrowser)
                }
            }
        }
        return@CreateApiKeyModal
    }

    Modal(
        title = "${LocalizedStrings.createApiKeyForTenant}: ${tenant.name}",
        onClose = onDismiss,
        onConfirm = { onCreate(keyName.value.trim()) },
        confirmText = LocalizedStrings.createApiKey,
        cancelText = LocalizedStrings.cancel,
        showCancel = true,
        confirmDisabled = createKeyLoading,
        confirmLoading = createKeyLoading
    ) {
        Div({
            style {
                display(DisplayStyle.Flex)
                flexDirection(org.jetbrains.compose.web.css.FlexDirection.Column)
                gap(12.px)
            }
        }) {
            createKeyError?.let { err ->
                P({
                    style {
                        fontSize(13.px)
                        color(FlagentTheme.errorText(themeMode))
                        margin(0.px)
                    }
                }) {
                    Text(err)
                }
            }
            Div({ style { marginBottom(8.px) } }) {
                Span({
                    style {
                        fontSize(14.px); fontWeight("500"); color(FlagentTheme.text(themeMode)); marginBottom(
                        4.px
                    ); property("display", "block")
                    }
                }) {
                    Text(LocalizedStrings.nameLabel)
                }
                Input(InputType.Text) {
                    attr(
                        "placeholder",
                        LocalizedStrings.createApiKeyNamePlaceholder
                    )
                    value(keyName.value)
                    onInput { keyName.value = it.value }
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
