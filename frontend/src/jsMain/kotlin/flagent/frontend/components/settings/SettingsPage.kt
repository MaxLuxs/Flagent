package flagent.frontend.components.settings

import androidx.compose.runtime.*
import flagent.frontend.api.ApiClient
import flagent.frontend.api.SlackStatusResponse
import flagent.frontend.components.Icon
import flagent.frontend.components.tenants.TenantsList
import flagent.frontend.config.AppConfig
import flagent.frontend.config.Edition
import flagent.frontend.i18n.LocalizedStrings
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
    var activeTab by remember { mutableStateOf("general") }
    
    Div({
        style {
            padding(20.px)
        }
    }) {
        // Header
        Div({
            style {
                marginBottom(30.px)
            }
        }) {
            H1({
                style {
                    fontSize(28.px)
                    fontWeight("bold")
                    color(FlagentTheme.Text)
                    margin(0.px)
                }
            }) {
                Text("⚙️ ${LocalizedStrings.settingsTitle}")
            }
            P({
                style {
                    color(FlagentTheme.TextLight)
                    fontSize(14.px)
                    marginTop(5.px)
                }
            }) {
                Text(LocalizedStrings.settingsSubtitle)
            }
            
            // Edition badge
            Span({
                style {
                    display(DisplayStyle.InlineBlock)
                    padding(4.px, 12.px)
                    borderRadius(12.px)
                    fontSize(12.px)
                    fontWeight("600")
                    marginTop(10.px)
                    if (AppConfig.isEnterprise) {
                        backgroundColor(Color("#F59E0B"))
                    } else {
                        backgroundColor(FlagentTheme.Primary)
                    }
                    color(FlagentTheme.Background)
                }
            }) {
                Text(if (AppConfig.isEnterprise) "ENTERPRISE EDITION" else "OPEN SOURCE EDITION")
            }
            Div({
                style {
                    marginTop(10.px)
                }
            }) {
                Span({
                    style {
                        fontSize(12.px)
                        color(FlagentTheme.TextLight)
                        fontWeight("500")
                    }
                }) {
                    Text("${LocalizedStrings.deploymentModeLabel}: ")
                }
                Span({
                    style {
                        fontSize(13.px)
                        color(FlagentTheme.Text)
                        fontWeight("600")
                    }
                }) {
                    Text(
                        AppConfig.getDeploymentPlanDisplayLabel(
                            LocalizedStrings.selfHostedOpenSource,
                            LocalizedStrings.selfHostedEnterprise,
                            LocalizedStrings.saasEnterprise,
                            LocalizedStrings.saasLowPrice
                        )
                    )
                }
            }
            P({
                style {
                    display(DisplayStyle.Flex)
                    alignItems(AlignItems.Center)
                    gap(6.px)
                    marginTop(12.px)
                    fontSize(13.px)
                    color(FlagentTheme.TextLight)
                }
            }) {
                Icon("info", size = 16.px, color = FlagentTheme.TextLight)
                Text(LocalizedStrings.settingsIntroHint)
            }
        }
        
        // Tabs
        Div({
            style {
                display(DisplayStyle.Flex)
                gap(10.px)
                marginBottom(30.px)
                    property("border-bottom", "2px solid ${FlagentTheme.Border}")
            }
        }) {
            SettingsTab(LocalizedStrings.generalTab, "settings", activeTab == "general") { activeTab = "general" }
            
            if (AppConfig.Features.enableMultiTenancy) {
                SettingsTab(LocalizedStrings.multiTenancyTab, "business", activeTab == "tenants") { activeTab = "tenants" }
            }
            
            if (AppConfig.Features.enableSso) {
                SettingsTab(LocalizedStrings.ssoProvidersTab, "security", activeTab == "sso") { activeTab = "sso" }
            }
            
            if (AppConfig.Features.enableSlack) {
                SettingsTab(LocalizedStrings.slackTab, "notifications", activeTab == "slack") { activeTab = "slack" }
            }
            
            if (AppConfig.Features.enableBilling) {
                SettingsTab(LocalizedStrings.billingTab, "payment", activeTab == "billing") { activeTab = "billing" }
            }
        }
        
        // Tab content
        when (activeTab) {
            "general" -> GeneralSettings()
            "tenants" -> if (AppConfig.Features.enableMultiTenancy) TenantsList()
            "sso" -> if (AppConfig.Features.enableSso) SsoSettings()
            "slack" -> if (AppConfig.Features.enableSlack) SlackSettings()
            "billing" -> if (AppConfig.Features.enableBilling) BillingSettings()
        }
    }
}

@Composable
private fun SettingsTab(label: String, icon: String, isActive: Boolean, onClick: () -> Unit) {
    Button({
        style {
            display(DisplayStyle.Flex)
            alignItems(AlignItems.Center)
            gap(8.px)
            padding(12.px, 20.px)
            backgroundColor(if (isActive) FlagentTheme.Primary else Color.transparent)
            color(if (isActive) FlagentTheme.Background else FlagentTheme.TextLight)
            border(0.px)
            borderRadius(8.px, 8.px, 0.px, 0.px)
            cursor("pointer")
            fontSize(14.px)
            fontWeight(if (isActive) "600" else "500")
            property("transition", "all 0.2s")
        }
        onClick { onClick() }
    }) {
        Icon(icon, size = 18.px, color = if (isActive) FlagentTheme.Background else FlagentTheme.TextLight)
        Text(label)
    }
}

@Composable
private fun GeneralSettings() {
    Div({
        style {
            backgroundColor(FlagentTheme.Background)
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
            }
        }) {
            Text(LocalizedStrings.generalSettings)
        }
        
        SettingItem(
            title = LocalizedStrings.apiBaseUrl,
            description = LocalizedStrings.apiBaseUrlDesc,
            value = AppConfig.apiBaseUrl
        )
        
        SettingItem(
            title = LocalizedStrings.debugMode,
            description = LocalizedStrings.debugModeDesc,
            value = if (AppConfig.debugMode) LocalizedStrings.enabled else LocalizedStrings.disabled
        )
        
        SettingItem(
            title = LocalizedStrings.apiTimeout,
            description = LocalizedStrings.apiTimeoutDesc,
            value = "${AppConfig.apiTimeout}ms"
        )
        
        // Feature flags status
        Div({
            style {
                marginTop(30.px)
                paddingTop(20.px)
                property("border-top", "1px solid ${FlagentTheme.Border}")
            }
        }) {
            H3({
                style {
                    fontSize(16.px)
                    fontWeight("600")
                    marginBottom(15.px)
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
                FeatureBadge("Metrics", AppConfig.Features.enableMetrics)
                FeatureBadge("Smart Rollout", AppConfig.Features.enableSmartRollout)
                FeatureBadge("Anomaly Detection", AppConfig.Features.enableAnomalyDetection)
                FeatureBadge("Real-time Updates", AppConfig.Features.enableRealtime)
                
                if (AppConfig.isEnterprise) {
                    FeatureBadge("Multi-Tenancy", AppConfig.Features.enableMultiTenancy, isEnterprise = true)
                    FeatureBadge("SSO", AppConfig.Features.enableSso, isEnterprise = true)
                    FeatureBadge("Billing", AppConfig.Features.enableBilling, isEnterprise = true)
                    FeatureBadge("Slack", AppConfig.Features.enableSlack, isEnterprise = true)
                    FeatureBadge("Advanced Analytics", AppConfig.Features.enableAdvancedAnalytics, isEnterprise = true)
                    FeatureBadge("Audit Logs", AppConfig.Features.enableAuditLogs, isEnterprise = true)
                    FeatureBadge("RBAC", AppConfig.Features.enableRbac, isEnterprise = true)
                }
            }
        }
    }
}

@Composable
private fun SettingItem(title: String, description: String, value: String) {
    Div({
        style {
            marginBottom(20.px)
            paddingBottom(20.px)
                property("border-bottom", "1px solid ${FlagentTheme.BackgroundAlt}")
        }
    }) {
        Div({
            style {
                fontWeight("500")
                marginBottom(4.px)
            }
        }) {
            Text(title)
        }
        Div({
            style {
                fontSize(14.px)
                color(FlagentTheme.TextLight)
                marginBottom(8.px)
            }
        }) {
            Text(description)
        }
        Code({
            style {
                display(DisplayStyle.Block)
                padding(8.px, 12.px)
                backgroundColor(FlagentTheme.BackgroundAlt)
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
private fun FeatureBadge(name: String, enabled: Boolean, isEnterprise: Boolean = false) {
    Div({
        style {
            display(DisplayStyle.Flex)
            alignItems(AlignItems.Center)
            gap(6.px)
            padding(8.px, 12.px)
            backgroundColor(if (enabled) Color("#D1FAE5") else FlagentTheme.BackgroundAlt)
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
                    color(FlagentTheme.Background)
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
        Text(name)
    }
}

@Composable
private fun SsoSettings() {
    val viewModel = remember { flagent.frontend.viewmodel.SsoViewModel() }
    
    LaunchedEffect(Unit) {
        viewModel.loadProviders()
    }
    
    Div({
        style {
            backgroundColor(FlagentTheme.Background)
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
            }
        }) {
            Text(LocalizedStrings.ssoTitle)
        }
        P({
            style {
                color(FlagentTheme.TextLight)
                marginBottom(20.px)
            }
        }) {
            Text(LocalizedStrings.ssoSubtitle)
        }
        if (viewModel.error != null) {
            Div({
                style {
                    padding(12.px)
                    backgroundColor(Color("#FEE2E2"))
                    borderRadius(6.px)
                    color(Color("#DC2626"))
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
                            color(FlagentTheme.TextLight)
                        }
                    }) {
                        Text(hint)
                    }
                }
            }
        }
        if (viewModel.isLoading && viewModel.providers.isEmpty()) {
            Div({ style { padding(20.px); textAlign("center") } }) { Text(LocalizedStrings.loading) }
        } else if (viewModel.providers.isEmpty()) {
            Div({
                style {
                    padding(20.px)
                    backgroundColor(FlagentTheme.BackgroundAlt)
                    borderRadius(6.px)
                    textAlign("center")
                    color(FlagentTheme.TextLight)
                }
            }) {
                Text(LocalizedStrings.noSsoProviders)
            }
        } else {
            viewModel.providers.forEach { provider ->
                Div({
                    style {
                        padding(12.px)
                        backgroundColor(FlagentTheme.BackgroundAlt)
                        borderRadius(6.px)
                        marginBottom(10.px)
                        display(DisplayStyle.Flex)
                        justifyContent(JustifyContent.SpaceBetween)
                        alignItems(AlignItems.Center)
                    }
                }) {
                    Div {
                        Div({ style { fontWeight("500") } }) { Text(provider.name) }
                        Div({ style { fontSize(14.px); color(FlagentTheme.TextLight) } }) {
                            Text("${provider.type} • ${if (provider.enabled) LocalizedStrings.enabled else LocalizedStrings.disabled}")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SlackSettings() {
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
            backgroundColor(FlagentTheme.Background)
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
            }
        }) {
            Text(LocalizedStrings.slackTitle)
        }
        P({
            style {
                color(FlagentTheme.TextLight)
                marginBottom(20.px)
            }
        }) {
            Text(LocalizedStrings.slackSubtitle)
        }
        if (slackLoading.value) {
            Div({ style { padding(20.px); textAlign("center") } }) { Text(LocalizedStrings.loading) }
        } else if (slackError.value != null) {
            Div({
                style {
                    padding(12.px)
                    backgroundColor(Color("#FEE2E2"))
                    borderRadius(6.px)
                    color(Color("#DC2626"))
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
                        backgroundColor(FlagentTheme.BackgroundAlt)
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
                            color(FlagentTheme.Text)
                        }
                    }) {
                        Text(if (status.enabled) LocalizedStrings.enabled else LocalizedStrings.disabled)
                    }
                    Span({
                        style {
                            fontSize(14.px)
                            color(FlagentTheme.TextLight)
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
                        backgroundColor(if (status.enabled && !testSending.value) FlagentTheme.Primary else FlagentTheme.NeutralLighter)
                        color(FlagentTheme.Background)
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
                            color(if (msg.contains("sent") || msg.contains("success")) FlagentTheme.Success else FlagentTheme.TextLight)
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
private fun BillingSettings() {
    val viewModel = remember { flagent.frontend.viewmodel.BillingViewModel() }
    
    LaunchedEffect(Unit) {
        viewModel.loadSubscription()
    }
    
    Div({
        style {
            backgroundColor(FlagentTheme.Background)
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
            }
        }) {
            Text(LocalizedStrings.billingTitle)
        }
        P({
            style {
                color(FlagentTheme.TextLight)
                marginBottom(20.px)
            }
        }) {
            Text(LocalizedStrings.billingSubtitle)
        }
        if (viewModel.error != null) {
            Div({
                style {
                    padding(12.px)
                    backgroundColor(Color("#FEE2E2"))
                    borderRadius(6.px)
                    color(Color("#DC2626"))
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
                            color(FlagentTheme.TextLight)
                        }
                    }) {
                        Text(hint)
                    }
                }
            }
        }
        if (viewModel.isLoading && viewModel.subscription == null) {
            Div({ style { padding(20.px); textAlign("center") } }) { Text(LocalizedStrings.loading) }
        } else if (viewModel.subscription != null) {
            val sub = viewModel.subscription!!
            Div({
                style {
                    padding(16.px)
                    backgroundColor(FlagentTheme.BackgroundAlt)
                    borderRadius(6.px)
                    marginBottom(15.px)
                }
            }) {
                Div({ style { fontWeight("600"); marginBottom(8.px) } }) {
                    Text("${LocalizedStrings.plan}: ${sub.plan.name} (${sub.status})")
                }
                Div({ style { fontSize(14.px); color(FlagentTheme.TextLight); marginBottom(8.px) } }) {
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
                    color(FlagentTheme.Background)
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
                    backgroundColor(FlagentTheme.BackgroundAlt)
                    borderRadius(6.px)
                    marginBottom(15.px)
                    color(FlagentTheme.TextLight)
                }
            }) {
                Text(LocalizedStrings.noActiveSubscription)
            }
            Button({
                style {
                    padding(10.px, 20.px)
                    backgroundColor(FlagentTheme.Primary)
                    color(FlagentTheme.Background)
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
