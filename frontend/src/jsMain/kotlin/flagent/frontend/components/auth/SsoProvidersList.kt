package flagent.frontend.components.auth

import androidx.compose.runtime.*
import flagent.frontend.api.ApiClient
import flagent.frontend.api.SsoProviderResponse
import flagent.frontend.components.Icon
import flagent.frontend.components.common.EmptyState
import flagent.frontend.components.common.SkeletonLoader
import flagent.frontend.state.LocalThemeMode
import flagent.frontend.state.ThemeMode
import flagent.frontend.theme.FlagentTheme
import flagent.frontend.util.ErrorHandler
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

/**
 * SSO Providers List component (Phase 2).
 * Loads providers from GET /tenants/me/sso/providers (requires X-Tenant-ID).
 */
@Composable
fun SsoProvidersList() {
    val providers = remember { mutableStateOf<List<SsoProvider>>(emptyList()) }
    val isLoading = remember { mutableStateOf(true) }
    val error = remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        isLoading.value = true
        error.value = null
        ErrorHandler.withErrorHandling(
            block = {
                val list = ApiClient.getSsoProviders()
                providers.value = list.map { it.toSsoProvider() }
            },
            onError = { err ->
                error.value = ErrorHandler.getUserMessage(err)
            }
        )
        isLoading.value = false
    }

    val themeMode = LocalThemeMode.current
    Div({
        style {
            backgroundColor(FlagentTheme.cardBg(themeMode))
            borderRadius(8.px)
            padding(24.px)
            property("box-shadow", "0 1px 3px ${FlagentTheme.Shadow}")
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
                Text("SSO Providers")
            }
            // Add Provider button hidden until POST /sso/providers is implemented on backend
        }

        if (isLoading.value) {
            Div({ style { padding(16.px) } }) {
                repeat(3) { SkeletonLoader(height = 24.px, width = 100.percent) }
            }
        } else if (error.value != null) {
            Div({
                style {
                    padding(16.px)
                    color(FlagentTheme.errorText(themeMode))
                    backgroundColor(FlagentTheme.errorBg(themeMode))
                    borderRadius(8.px)
                }
            }) {
                Text(error.value!!)
            }
        } else if (providers.value.isEmpty()) {
            EmptyState(
                icon = "security",
                title = "No SSO providers configured",
                description = "Add SAML, OAuth, or OIDC providers via admin UI or direct DB until API creation is available.",
                actionLabel = null,
                onAction = null
            )
        } else {
            providers.value.forEach { provider ->
                SsoProviderCard(themeMode = themeMode, provider = provider)
            }
        }
    }
}

@Composable
private fun SsoProviderCard(themeMode: ThemeMode, provider: SsoProvider) {
    Div({
        style {
            padding(16.px)
            border(1.px, LineStyle.Solid, FlagentTheme.cardBorder(themeMode))
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
                        color(FlagentTheme.text(themeMode))
                        margin(0.px)
                        marginBottom(4.px)
                    }
                }) {
                    Text(provider.name)
                }
                P({
                    style {
                        fontSize(14.px)
                        color(FlagentTheme.textLight(themeMode))
                        margin(0.px)
                    }
                }) {
                    Text(provider.type)
                }
            }
            
            Span({
                style {
                    padding(4.px, 8.px)
                    backgroundColor(if (provider.enabled) FlagentTheme.successBg(themeMode) else FlagentTheme.errorBg(themeMode))
                    color(if (provider.enabled) FlagentTheme.Success else FlagentTheme.errorText(themeMode))
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

data class SsoProvider(
    val id: String,
    val name: String,
    val type: String,
    val enabled: Boolean
)

private fun SsoProviderResponse.toSsoProvider() = SsoProvider(
    id = id.toString(),
    name = name,
    type = type,
    enabled = enabled
)
