package flagent.frontend.components.tenants

import androidx.compose.runtime.*
import flagent.frontend.components.Icon
import flagent.frontend.components.Modal
import org.jetbrains.compose.web.attributes.InputType
import flagent.frontend.components.common.EmptyState
import flagent.frontend.components.common.SkeletonLoader
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
@Composable
fun TenantsList(tenantViewModel: TenantViewModel? = null) {
    val viewModel = tenantViewModel ?: remember { TenantViewModel() }
    val showCreateForm = remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        viewModel.loadTenants()
        if (window.location.search.contains("create=1")) {
            showCreateForm.value = true
        }
    }
    
    Div({
        style {
            backgroundColor(Color.white)
            borderRadius(8.px)
            padding(24.px)
            property("box-shadow", "0 1px 3px rgba(0, 0, 0, 0.1)")
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
                    color(Color("#1E293B"))
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
                            borderBottom(2.px, LineStyle.Solid, Color("#E2E8F0"))
                        }
                    }) {
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
                    viewModel.tenants.forEach { tenant ->
                        Tr({
                            style {
                                borderBottom(1.px, LineStyle.Solid, Color("#E2E8F0"))
                            }
                        }) {
                            Td({
                                style {
                                    padding(12.px)
                                    fontSize(14.px)
                                    fontWeight(600)
                                    color(Color("#1E293B"))
                                }
                            }) {
                                Text(tenant.name)
                            }
                            Td({
                                style {
                                    padding(12.px)
                                    fontSize(14.px)
                                    color(Color("#64748B"))
                                }
                            }) {
                                Text(tenant.key)
                            }
                            Td({
                                style {
                                    padding(12.px)
                                    textAlign("right")
                                }
                            }) {
                                Button({
                                    onClick { viewModel.switchTenant(tenant) }
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
                }
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
            Label {
                Text("Key (unique identifier)")
                Input(InputType.Text) {
                    value(key.value)
                    onInput { key.value = it.value }
                    style {
                        width(100.percent)
                        padding(8.px)
                        property("margin-top", "4px")
                    }
                }
            }
            Label {
                Text("Name")
                Input(InputType.Text) {
                    value(name.value)
                    onInput { name.value = it.value }
                    style {
                        width(100.percent)
                        padding(8.px)
                        property("margin-top", "4px")
                    }
                }
            }
            Label {
                Text("Plan")
                Select(attrs = {
                    attr("value", plan.value)
                    onChange { plan.value = (it.target as org.w3c.dom.HTMLSelectElement).value }
                    style {
                        width(100.percent)
                        padding(8.px)
                        property("margin-top", "4px")
                    }
                }) {
                    Option(value = "STARTER") { Text("STARTER") }
                    Option(value = "GROWTH") { Text("GROWTH") }
                    Option(value = "SCALE") { Text("SCALE") }
                    Option(value = "ENTERPRISE") { Text("ENTERPRISE") }
                }
            }
            Label {
                Text("Owner Email")
                Input(InputType.Email) {
                    value(ownerEmail.value)
                    onInput { ownerEmail.value = it.value }
                    style {
                        width(100.percent)
                        padding(8.px)
                        property("margin-top", "4px")
                    }
                }
            }
        }
    }
}
