package flagent.frontend.components.settings

import androidx.compose.runtime.*
import flagent.frontend.api.ApiClient
import flagent.frontend.api.SlackStatusResponse
import flagent.frontend.components.Icon
import flagent.frontend.components.export.ExportPanel
import flagent.frontend.components.settings.ImportPanel
import flagent.frontend.config.AppConfig
import flagent.frontend.config.Edition
import flagent.frontend.i18n.LocalizedStrings
import flagent.frontend.navigation.Route
import flagent.frontend.navigation.Router
import flagent.frontend.state.LocalThemeMode
import flagent.frontend.theme.FlagentTheme
import flagent.frontend.util.ErrorHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

/**
 * Settings Page - настройки приложения
 */
@Composable
fun SettingsPage() {
    val themeMode = LocalThemeMode.current
    var activeTab by remember { mutableStateOf("general") }
    
    Div({
        style {
            padding(0.px)
            display(DisplayStyle.Flex)
            gap(24.px)
        }
    }) {
        // Left sidebar tabs
        Div({
            style {
                width(200.px)
                flexShrink(0)
                backgroundColor(FlagentTheme.cardBg(themeMode))
                borderRadius(8.px)
                padding(8.px)
                property("box-shadow", "0 1px 3px rgba(0,0,0,0.08)")
                property("border", "1px solid ${FlagentTheme.cardBorder(themeMode)}")
            }
        }) {
            Div({
                style {
                    marginBottom(12.px)
                    padding(8.px, 12.px)
                }
            }) {
                H2({
                    style {
                        fontSize(18.px)
                        fontWeight("600")
                        color(FlagentTheme.text(themeMode))
                        margin(0.px)
                    }
                }) {
                    Text(LocalizedStrings.settingsTitle)
                }
                Span({
                    style {
                        display(DisplayStyle.InlineBlock)
                        padding(2.px, 8.px)
                        borderRadius(8.px)
                        fontSize(11.px)
                        fontWeight("600")
                        marginTop(6.px)
                        backgroundColor(if (AppConfig.isEnterprise) Color("#F59E0B") else FlagentTheme.Primary)
                        color(Color.white)
                    }
                }) {
                    Text(if (AppConfig.isEnterprise) "ENTERPRISE" else "OPEN SOURCE")
                }
            }
            SettingsTab(themeMode, LocalizedStrings.generalTab, "settings", activeTab == "general") { activeTab = "general" }
            if (AppConfig.Features.enableSso) {
                SettingsTab(themeMode, LocalizedStrings.ssoProvidersTab, "security", activeTab == "sso") { activeTab = "sso" }
            }
            if (AppConfig.Features.enableSlack) {
                SettingsTab(themeMode, LocalizedStrings.slackTab, "notifications", activeTab == "slack") { activeTab = "slack" }
            }
            if (AppConfig.Features.enableBilling) {
                SettingsTab(themeMode, LocalizedStrings.billingTab, "payment", activeTab == "billing") { activeTab = "billing" }
            }
            SettingsTab(themeMode, "Webhooks", "webhook", activeTab == "webhooks") { activeTab = "webhooks" }
            SettingsTab(themeMode, "Export", "download", activeTab == "export") { activeTab = "export" }
            SettingsTab(themeMode, "Import", "upload", activeTab == "import") { activeTab = "import" }
            if (AppConfig.Features.enableRbac) {
                SettingsTab(themeMode, "Roles", "security", activeTab == "roles") { activeTab = "roles" }
            }
        }
        
        // Tab content
        Div({
            style {
                flex(1)
                property("min-width", "0")
            }
        }) {
        when (activeTab) {
            "general" -> GeneralSettings(themeMode)
            "sso" -> if (AppConfig.Features.enableSso) SsoSettings(themeMode)
            "slack" -> if (AppConfig.Features.enableSlack) SlackSettings(themeMode)
            "billing" -> if (AppConfig.Features.enableBilling) BillingSettings(themeMode)
            "webhooks" -> WebhooksSettings()
            "export" -> ExportPanel()
            "import" -> ImportPanel()
            "roles" -> if (AppConfig.Features.enableRbac) RolesSettings(themeMode)
        }
        }
    }
}

@Composable
private fun SettingsTab(themeMode: flagent.frontend.state.ThemeMode, label: String, icon: String, isActive: Boolean, onClick: () -> Unit) {
    Button({
        style {
            display(DisplayStyle.Flex)
            alignItems(AlignItems.Center)
            gap(8.px)
            padding(10.px, 12.px)
            width(100.percent)
            textAlign("left")
            backgroundColor(if (isActive) FlagentTheme.inputBg(themeMode) else Color.transparent)
            color(if (isActive) FlagentTheme.Primary else FlagentTheme.text(themeMode))
            border(0.px)
            borderRadius(6.px)
            cursor("pointer")
            fontSize(14.px)
            fontWeight(if (isActive) "600" else "500")
            property("transition", "all 0.15s")
            property("border-left", if (isActive) "3px solid ${FlagentTheme.Primary}" else "3px solid transparent")
        }
        onClick { onClick() }
    }) {
        Icon(icon, size = 18.px, color = if (isActive) FlagentTheme.Primary else FlagentTheme.textLight(themeMode))
        Text(label)
    }
}

@Composable
private fun GeneralSettings(themeMode: flagent.frontend.state.ThemeMode) {
    Div({
        style {
            backgroundColor(FlagentTheme.cardBg(themeMode))
            borderRadius(8.px)
            padding(20.px)
            property("box-shadow", "0 2px 8px rgba(0,0,0,0.1)")
        }
    }) {
        H2({
            style {
                fontSize(20.px)
                fontWeight("600")
                marginBottom(20.px)
                color(FlagentTheme.text(themeMode))
            }
        }) {
            Text(LocalizedStrings.generalSettings)
        }
        
        SettingItem(themeMode,
            title = LocalizedStrings.apiBaseUrl,
            description = LocalizedStrings.apiBaseUrlDesc,
            value = AppConfig.apiBaseUrl
        )
        
        SettingItem(themeMode,
            title = LocalizedStrings.debugMode,
            description = LocalizedStrings.debugModeDesc,
            value = if (AppConfig.debugMode) LocalizedStrings.enabled else LocalizedStrings.disabled
        )
        
        SettingItem(themeMode,
            title = LocalizedStrings.apiTimeout,
            description = LocalizedStrings.apiTimeoutDesc,
            value = "${AppConfig.apiTimeout}ms"
        )
        
        if (AppConfig.Features.enableMultiTenancy) {
            Div({
                style {
                    marginBottom(20.px)
                    paddingBottom(20.px)
                    property("border-bottom", "1px solid ${FlagentTheme.inputBorder(themeMode)}")
                }
            }) {
                Button({
                    style {
                        display(DisplayStyle.Flex)
                        alignItems(AlignItems.Center)
                        gap(8.px)
                        padding(0.px)
                        border(0.px)
                        backgroundColor(Color.transparent)
                        color(FlagentTheme.Primary)
                        cursor("pointer")
                        fontSize(14.px)
                        fontWeight("500")
                        textDecoration("underline")
                    }
                    onClick { Router.navigateTo(Route.Tenants) }
                }) {
                    Icon("business", size = 18.px, color = FlagentTheme.Primary)
                    Text(LocalizedStrings.manageTenantsLink)
                }
            }
        }
        
        // Feature flags status
        Div({
            style {
                marginTop(30.px)
                paddingTop(20.px)
                property("border-top", "1px solid ${FlagentTheme.cardBorder(themeMode)}")
            }
        }) {
            H3({
                style {
                    fontSize(16.px)
                    fontWeight("600")
                    marginBottom(15.px)
                    color(FlagentTheme.text(themeMode))
                }
            }) {
                Text(LocalizedStrings.enabledFeatures)
            }
            
            Div({
                style {
                    display(DisplayStyle.Grid)
                    property("grid-template-columns", "repeat(auto-fill, minmax(200px, 1fr))")
                    gap(10.px)
                }
            }) {
                FeatureBadge(themeMode, "Metrics", AppConfig.Features.enableMetrics)
                FeatureBadge(themeMode, "Smart Rollout", AppConfig.Features.enableSmartRollout)
                FeatureBadge(themeMode, "Anomaly Detection", AppConfig.Features.enableAnomalyDetection)
                FeatureBadge(themeMode, "Real-time Updates", AppConfig.Features.enableRealtime)
                
                if (AppConfig.isEnterprise) {
                    FeatureBadge(themeMode, "Multi-Tenancy", AppConfig.Features.enableMultiTenancy, isEnterprise = true)
                    FeatureBadge(themeMode, "SSO", AppConfig.Features.enableSso, isEnterprise = true)
                    FeatureBadge(themeMode, "Billing", AppConfig.Features.enableBilling, isEnterprise = true)
                    FeatureBadge(themeMode, "Slack", AppConfig.Features.enableSlack, isEnterprise = true)
                    FeatureBadge(themeMode, "Advanced Analytics", AppConfig.Features.enableAdvancedAnalytics, isEnterprise = true)
                    FeatureBadge(themeMode, "Audit Logs", AppConfig.Features.enableAuditLogs, isEnterprise = true)
                    FeatureBadge(themeMode, "RBAC", AppConfig.Features.enableRbac, isEnterprise = true)
                }
            }
        }
    }
}

@Composable
private fun SettingItem(themeMode: flagent.frontend.state.ThemeMode, title: String, description: String, value: String) {
    Div({
        style {
            marginBottom(20.px)
            paddingBottom(20.px)
                property("border-bottom", "1px solid ${FlagentTheme.inputBorder(themeMode)}")
        }
    }) {
        Div({
            style {
                fontWeight("500")
                marginBottom(4.px)
                color(FlagentTheme.text(themeMode))
            }
        }) {
            Text(title)
        }
        Div({
            style {
                fontSize(14.px)
                color(FlagentTheme.textLight(themeMode))
                marginBottom(8.px)
            }
        }) {
            Text(description)
        }
        Code({
            style {
                display(DisplayStyle.Block)
                padding(8.px, 12.px)
                backgroundColor(FlagentTheme.inputBg(themeMode))
                color(FlagentTheme.text(themeMode))
                borderRadius(4.px)
                fontSize(13.px)
                fontFamily("'Monaco', 'Courier New', monospace")
            }
        }) {
            Text(value)
        }
    }
}

@Composable
private fun FeatureBadge(themeMode: flagent.frontend.state.ThemeMode, name: String, enabled: Boolean, isEnterprise: Boolean = false) {
    Div({
        style {
            display(DisplayStyle.Flex)
            alignItems(AlignItems.Center)
            gap(6.px)
            padding(8.px, 12.px)
            backgroundColor(if (enabled) FlagentTheme.successBg(themeMode) else FlagentTheme.inputBg(themeMode))
            borderRadius(6.px)
            fontSize(13.px)
        }
    }) {
        if (isEnterprise) {
            Span({
                style {
                    fontSize(10.px)
                    padding(2.px, 6.px)
                    backgroundColor(Color("#F59E0B"))
                    color(Color.white)
                    borderRadius(4.px)
                    fontWeight("600")
                }
            }) {
                Text("ENT")
            }
        }
        Icon(
            if (enabled) "check_circle" else "cancel",
            size = 14.px,
            color = if (enabled) Color("#10B981") else Color("#EF4444")
        )
        Span({ style { color(FlagentTheme.text(themeMode)) } }) { Text(name) }
    }
}

@Composable
private fun SsoSettings(themeMode: flagent.frontend.state.ThemeMode) {
    val viewModel = remember { flagent.frontend.viewmodel.SsoViewModel() }
    
    LaunchedEffect(Unit) {
        viewModel.loadProviders()
    }
    
    Div({
        style {
            backgroundColor(FlagentTheme.cardBg(themeMode))
            borderRadius(8.px)
            padding(20.px)
            property("box-shadow", "0 2px 8px rgba(0,0,0,0.1)")
        }
    }) {
        H2({
            style {
                fontSize(20.px)
                fontWeight("600")
                marginBottom(20.px)
                color(FlagentTheme.text(themeMode))
            }
        }) {
            Text(LocalizedStrings.ssoTitle)
        }
        P({
            style {
                color(FlagentTheme.textLight(themeMode))
                marginBottom(20.px)
            }
        }) {
            Text(LocalizedStrings.ssoSubtitle)
        }
        if (viewModel.error != null) {
            Div({
                style {
                    padding(12.px)
                    backgroundColor(FlagentTheme.errorBg(themeMode))
                    borderRadius(6.px)
                    color(FlagentTheme.errorText(themeMode))
                    marginBottom(15.px)
                }
            }) {
                Text(viewModel.error!!)
                viewModel.errorHint?.let { hint ->
                    P({
                        style {
                            marginTop(8.px)
                            marginBottom(0.px)
                            fontSize(13.px)
                            color(FlagentTheme.textLight(themeMode))
                        }
                    }) {
                        Text(hint)
                    }
                }
            }
        }
        if (viewModel.isLoading && viewModel.providers.isEmpty()) {
            Div({
                style {
                    padding(20.px)
                    textAlign("center")
                    color(FlagentTheme.textLight(themeMode))
                }
            }) { Text(LocalizedStrings.loading) }
        } else if (viewModel.providers.isEmpty()) {
            Div({
                style {
                    padding(20.px)
                    backgroundColor(FlagentTheme.inputBg(themeMode))
                    borderRadius(6.px)
                    textAlign("center")
                    color(FlagentTheme.textLight(themeMode))
                }
            }) {
                Text(LocalizedStrings.noSsoProviders)
            }
        } else {
            viewModel.providers.forEach { provider ->
                Div({
                    style {
                        padding(12.px)
                        backgroundColor(FlagentTheme.inputBg(themeMode))
                        borderRadius(6.px)
                        marginBottom(10.px)
                        display(DisplayStyle.Flex)
                        justifyContent(JustifyContent.SpaceBetween)
                        alignItems(AlignItems.Center)
                    }
                }) {
                    Div {
                        Div({ style { fontWeight("500"); color(FlagentTheme.text(themeMode)) } }) { Text(provider.name) }
                        Div({ style { fontSize(14.px); color(FlagentTheme.textLight(themeMode)) } }) {
                            Text("${provider.type} • ${if (provider.enabled) LocalizedStrings.enabled else LocalizedStrings.disabled}")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SlackSettings(themeMode: flagent.frontend.state.ThemeMode) {
    val slackStatus = remember { mutableStateOf<SlackStatusResponse?>(null) }
    val slackLoading = remember { mutableStateOf(true) }
    val slackError = remember { mutableStateOf<String?>(null) }
    val testSending = remember { mutableStateOf(false) }
    val testResult = remember { mutableStateOf<String?>(null) }
    val scope = remember { CoroutineScope(Dispatchers.Main) }
    
    LaunchedEffect(Unit) {
        slackLoading.value = true
        slackError.value = null
        try {
            slackStatus.value = ApiClient.getSlackStatus()
        } catch (e: Exception) {
            slackError.value = ErrorHandler.getUserMessage(ErrorHandler.handle(e))
        } finally {
            slackLoading.value = false
        }
    }
    
    Div({
        style {
            backgroundColor(FlagentTheme.cardBg(themeMode))
            borderRadius(8.px)
            padding(20.px)
            property("box-shadow", "0 2px 8px rgba(0,0,0,0.1)")
        }
    }) {
        H2({
            style {
                fontSize(20.px)
                fontWeight("600")
                marginBottom(20.px)
                color(FlagentTheme.text(themeMode))
            }
        }) {
            Text(LocalizedStrings.slackTitle)
        }
        P({
            style {
                color(FlagentTheme.textLight(themeMode))
                marginBottom(20.px)
            }
        }) {
            Text(LocalizedStrings.slackSubtitle)
        }
        if (slackLoading.value) {
            Div({
                style {
                    padding(20.px)
                    textAlign("center")
                    color(FlagentTheme.textLight(themeMode))
                }
            }) { Text(LocalizedStrings.loading) }
        } else         if (slackError.value != null) {
            Div({
                style {
                    padding(12.px)
                    backgroundColor(FlagentTheme.errorBg(themeMode))
                    borderRadius(6.px)
                    color(FlagentTheme.errorText(themeMode))
                    marginBottom(15.px)
                }
            }) {
                Text(slackError.value!!)
            }
        } else {
            val status = slackStatus.value
            if (status != null) {
                Div({
                    style {
                        padding(12.px)
                        backgroundColor(FlagentTheme.inputBg(themeMode))
                        borderRadius(6.px)
                        marginBottom(15.px)
                        display(DisplayStyle.Flex)
                        justifyContent(JustifyContent.SpaceBetween)
                        alignItems(AlignItems.Center)
                    }
                }) {
                    Span({
                        style {
                            fontWeight("500")
                            color(FlagentTheme.text(themeMode))
                        }
                    }) {
                        Text(if (status.enabled) LocalizedStrings.enabled else LocalizedStrings.disabled)
                    }
                    Span({
                        style {
                            fontSize(14.px)
                            color(FlagentTheme.textLight(themeMode))
                        }
                    }) {
                        Text(if (status.enabled) LocalizedStrings.slackConfigured else LocalizedStrings.slackNotConfigured)
                    }
                }
                Button({
                    onClick {
                        scope.launch {
                            if (!status.enabled) return@launch
                            testSending.value = true
                            testResult.value = null
                            try {
                                val res = ApiClient.sendSlackTestNotification()
                                testResult.value = if (res.success) (res.message ?: "Test notification sent.") else (res.error ?: "Failed to send.")
                            } catch (e: Exception) {
                                testResult.value = ErrorHandler.getUserMessage(ErrorHandler.handle(e))
                            } finally {
                                testSending.value = false
                            }
                        }
                    }
                    style {
                        padding(10.px, 20.px)
                        backgroundColor(if (status.enabled && !testSending.value) FlagentTheme.Primary else FlagentTheme.inputBg(themeMode))
                        color(Color.white)
                        border(0.px)
                        borderRadius(6.px)
                        cursor(if (status.enabled && !testSending.value) "pointer" else "not-allowed")
                        fontSize(14.px)
                        fontWeight("500")
                    }
                }) {
                    Text(if (testSending.value) LocalizedStrings.sending else LocalizedStrings.sendTestNotification)
                }
                testResult.value?.let { msg ->
                    Div({
                        style {
                            marginTop(12.px)
                            padding(10.px)
                            borderRadius(6.px)
                            fontSize(14.px)
                            color(if (msg.contains("sent") || msg.contains("success")) FlagentTheme.Success else FlagentTheme.textLight(themeMode))
                        }
                    }) {
                        Text(msg)
                    }
                }
            }
        }
    }
}

@Composable
private fun BillingSettings(themeMode: flagent.frontend.state.ThemeMode) {
    val viewModel = remember { flagent.frontend.viewmodel.BillingViewModel() }
    
    LaunchedEffect(Unit) {
        viewModel.loadSubscription()
    }
    
    Div({
        style {
            backgroundColor(FlagentTheme.cardBg(themeMode))
            borderRadius(8.px)
            padding(20.px)
            property("box-shadow", "0 2px 8px rgba(0,0,0,0.1)")
        }
    }) {
        H2({
            style {
                fontSize(20.px)
                fontWeight("600")
                marginBottom(20.px)
                color(FlagentTheme.text(themeMode))
            }
        }) {
            Text(LocalizedStrings.billingTitle)
        }
        P({
            style {
                color(FlagentTheme.textLight(themeMode))
                marginBottom(20.px)
            }
        }) {
            Text(LocalizedStrings.billingSubtitle)
        }
        if (viewModel.error != null) {
            Div({
                style {
                    padding(12.px)
                    backgroundColor(FlagentTheme.errorBg(themeMode))
                    borderRadius(6.px)
                    color(FlagentTheme.errorText(themeMode))
                    marginBottom(15.px)
                }
            }) {
                Text(viewModel.error!!)
                viewModel.errorHint?.let { hint ->
                    P({
                        style {
                            marginTop(8.px)
                            marginBottom(0.px)
                            fontSize(13.px)
                            color(FlagentTheme.textLight(themeMode))
                        }
                    }) {
                        Text(hint)
                    }
                }
            }
        }
        if (viewModel.isLoading && viewModel.subscription == null) {
            Div({
                style {
                    padding(20.px)
                    textAlign("center")
                    color(FlagentTheme.textLight(themeMode))
                }
            }) { Text(LocalizedStrings.loading) }
        } else if (viewModel.subscription != null) {
            val sub = viewModel.subscription!!
            Div({
                style {
                    padding(16.px)
                    backgroundColor(FlagentTheme.inputBg(themeMode))
                    borderRadius(6.px)
                    marginBottom(15.px)
                }
            }) {
                Div({ style { fontWeight("600"); marginBottom(8.px); color(FlagentTheme.text(themeMode)) } }) {
                    Text("${LocalizedStrings.plan}: ${sub.plan.name} (${sub.status})")
                }
                Div({ style { fontSize(14.px); color(FlagentTheme.textLight(themeMode)); marginBottom(8.px) } }) {
                    Text("${LocalizedStrings.currentPeriod}: ${sub.currentPeriodStart} — ${sub.currentPeriodEnd}")
                }
                if (sub.cancelAtPeriodEnd) {
                    Div({ style { fontSize(14.px); color(FlagentTheme.Warning) } }) {
                        Text(LocalizedStrings.cancelsAtPeriodEnd)
                    }
                }
            }
            Button({
                style {
                    padding(10.px, 20.px)
                    backgroundColor(FlagentTheme.Primary)
                    color(Color.white)
                    border(0.px)
                    borderRadius(6.px)
                    cursor("pointer")
                    fontSize(14.px)
                    fontWeight("500")
                }
                onClick { viewModel.createPortalSession() }
            }) {
                Text(LocalizedStrings.openBillingPortal)
            }
        } else {
            Div({
                style {
                    padding(16.px)
                    backgroundColor(FlagentTheme.inputBg(themeMode))
                    borderRadius(6.px)
                    marginBottom(15.px)
                    color(FlagentTheme.textLight(themeMode))
                }
            }) {
                Text(LocalizedStrings.noActiveSubscription)
            }
            Button({
                style {
                    padding(10.px, 20.px)
                    backgroundColor(FlagentTheme.Primary)
                    color(Color.white)
                    border(0.px)
                    borderRadius(6.px)
                    cursor("pointer")
                    fontSize(14.px)
                    fontWeight("500")
                    marginTop(10.px)
                }
                onClick {
                    viewModel.createPortalSession()
                }
            }) {
                Text(LocalizedStrings.openBillingPortal)
            }
        }
    }
}
