package flagent.frontend.components.tenants

import androidx.compose.runtime.*
import flagent.frontend.components.Icon
import flagent.frontend.state.LocalGlobalState
import flagent.frontend.state.Tenant
import flagent.frontend.theme.FlagentTheme
import flagent.frontend.viewmodel.TenantViewModel
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

/**
 * Tenant Switcher component (Phase 2)
 */
@Composable
fun TenantSwitcher(viewModel: TenantViewModel) {
    val globalState = LocalGlobalState.current
    val isOpen = remember { mutableStateOf(false) }
    
    Div({
        style {
            position(Position.Relative)
        }
    }) {
        // Current tenant button
        Button({
            onClick { isOpen.value = !isOpen.value }
            style {
                display(DisplayStyle.Flex)
                alignItems(AlignItems.Center)
                gap(8.px)
                padding(8.px, 16.px)
                backgroundColor(Color.transparent)
                color(Color.white)
                border(1.px, LineStyle.Solid, Color("rgba(255, 255, 255, 0.3)"))
                borderRadius(6.px)
                cursor("pointer")
                fontSize(14.px)
            }
        }) {
            Icon("business", size = 20.px)
            Span({
                style {
                    fontWeight(500)
                }
            }) {
                Text(globalState.currentTenant?.name ?: "Select Tenant")
            }
            Icon("arrow_drop_down", size = 20.px)
        }
        
        // Dropdown menu
        if (isOpen.value) {
            Div({
                style {
                    position(Position.Absolute)
                    property("top", "calc(100% + 8px)")
                    property("right", "0")
                    backgroundColor(Color.white)
                    borderRadius(6.px)
                    property("box-shadow", "0 4px 6px rgba(0, 0, 0, 0.1)")
                    minWidth(200.px)
                    property("z-index", "1000")
                }
            }) {
                viewModel.tenants.forEach { tenant ->
                    TenantMenuItem(
                        tenant = tenant,
                        isCurrent = tenant.id == globalState.currentTenant?.id,
                        onClick = {
                            viewModel.switchTenant(tenant)
                            globalState.currentTenant = tenant
                            isOpen.value = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TenantMenuItem(
    tenant: Tenant,
    isCurrent: Boolean,
    onClick: () -> Unit
) {
    Button({
        onClick { onClick() }
        style {
            width(100.percent)
            display(DisplayStyle.Flex)
            alignItems(AlignItems.Center)
            justifyContent(JustifyContent.SpaceBetween)
            padding(12.px, 16.px)
            backgroundColor(if (isCurrent) Color("#F8FAFC") else Color.transparent)
            color(Color("#1E293B"))
            border(0.px)
            property("border-bottom", "1px solid #E2E8F0")
            cursor("pointer")
            fontSize(14.px)
            textAlign("left")
        }
    }) {
        Span {
            Text(tenant.name)
        }
        if (isCurrent) {
            Icon("check", size = 16.px, color = FlagentTheme.Primary)
        }
    }
}
