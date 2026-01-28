package flagent.frontend.components.auth

import androidx.compose.runtime.*
import flagent.frontend.components.Icon
import flagent.frontend.components.common.EmptyState
import flagent.frontend.theme.FlagentTheme
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

/**
 * SSO Providers List component (Phase 2)
 */
@Composable
fun SsoProvidersList() {
    val providers = remember { mutableStateOf<List<SsoProvider>>(emptyList()) }
    
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
                Text("SSO Providers")
            }
            
            Button({
                onClick { /* TODO: Open create form */ }
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
                Text("Add Provider")
            }
        }
        
        if (providers.value.isEmpty()) {
            EmptyState(
                icon = "security",
                title = "No SSO providers configured",
                description = "Add SAML, OAuth, or OIDC providers for single sign-on",
                actionLabel = "Add Provider",
                onAction = { /* TODO */ }
            )
        } else {
            providers.value.forEach { provider ->
                SsoProviderCard(provider)
            }
        }
    }
}

@Composable
private fun SsoProviderCard(provider: SsoProvider) {
    Div({
        style {
            padding(16.px)
            border(1.px, LineStyle.Solid, Color("#E2E8F0"))
            borderRadius(6.px)
            marginBottom(12.px)
        }
    }) {
        Div({
            style {
                display(DisplayStyle.Flex)
                justifyContent(JustifyContent.SpaceBetween)
                alignItems(AlignItems.Center)
            }
        }) {
            Div {
                P({
                    style {
                        fontSize(16.px)
                        fontWeight(600)
                        color(Color("#1E293B"))
                        margin(0.px)
                        marginBottom(4.px)
                    }
                }) {
                    Text(provider.name)
                }
                P({
                    style {
                        fontSize(14.px)
                        color(Color("#64748B"))
                        margin(0.px)
                    }
                }) {
                    Text(provider.type)
                }
            }
            
            Span({
                style {
                    padding(4.px, 8.px)
                    backgroundColor(if (provider.enabled) Color("#DCFCE7") else Color("#FEE2E2"))
                    color(if (provider.enabled) Color("#166534") else Color("#991B1B"))
                    borderRadius(4.px)
                    fontSize(12.px)
                    fontWeight(500)
                }
            }) {
                Text(if (provider.enabled) "Enabled" else "Disabled")
            }
        }
    }
}

// Temporary data class (will be replaced with API model)
data class SsoProvider(
    val id: String,
    val name: String,
    val type: String,
    val enabled: Boolean
)
