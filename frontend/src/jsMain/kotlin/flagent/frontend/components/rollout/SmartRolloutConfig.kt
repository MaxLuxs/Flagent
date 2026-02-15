package flagent.frontend.components.rollout

import androidx.compose.runtime.*
import flagent.frontend.api.SmartRolloutConfigRequest
import flagent.frontend.components.common.EmptyState
import flagent.frontend.components.common.SkeletonLoader
import flagent.frontend.state.LocalThemeMode
import flagent.frontend.theme.FlagentTheme
import flagent.frontend.viewmodel.SmartRolloutViewModel
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

/**
 * Smart Rollout Configuration component (Phase 3)
 */
@Composable
fun SmartRolloutConfig(flagId: Int) {
    val themeMode = LocalThemeMode.current
    val viewModel = remember { SmartRolloutViewModel(flagId) }
    val showCreateForm = remember { mutableStateOf(false) }
    
    LaunchedEffect(flagId) {
        viewModel.loadConfigs()
    }
    
    Div({
        style {
            backgroundColor(FlagentTheme.cardBg(themeMode))
            borderRadius(8.px)
            padding(24.px)
            property("box-shadow", "0 1px 3px ${FlagentTheme.Shadow}")
        }
    }) {
        // Header
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
                Text("Smart Rollout (AI-Powered)")
            }
            
            Button({
                onClick { showCreateForm.value = !showCreateForm.value }
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
                Text(if (showCreateForm.value) "Cancel" else "Create Config")
            }
        }
        
        // Loading state
        if (viewModel.isLoading) {
            SkeletonLoader(height = 200.px)
        } else if (viewModel.configs.isEmpty()) {
            EmptyState(
                icon = "trending_up",
                title = "No rollout configs",
                description = "Create a smart rollout configuration to automatically manage gradual rollouts",
                actionLabel = "Create Config",
                onAction = { showCreateForm.value = true }
            )
        } else {
            // Configs list
            viewModel.configs.forEach { config ->
                RolloutConfigCard(config, viewModel)
            }
        }
        
        // Error state
        viewModel.error?.let { error ->
            Div({
                style {
                    marginTop(16.px)
                    padding(12.px)
                    backgroundColor(FlagentTheme.errorBg(themeMode))
                    borderRadius(6.px)
                    color(FlagentTheme.errorText(themeMode))
                    fontSize(14.px)
                }
            }) {
                Text(error)
            }
        }
    }
}

@Composable
private fun RolloutConfigCard(config: flagent.frontend.api.SmartRolloutConfigResponse, viewModel: SmartRolloutViewModel) {
    val themeMode = LocalThemeMode.current
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
                marginBottom(12.px)
            }
        }) {
            Div {
                P({
                    style {
                        fontSize(14.px)
                        fontWeight(600)
                        color(FlagentTheme.text(themeMode))
                        margin(0.px)
                        marginBottom(4.px)
                    }
                }) {
                    Text("Segment #${config.segmentId}")
                }
                P({
                    style {
                        fontSize(12.px)
                        color(FlagentTheme.textLight(themeMode))
                        margin(0.px)
                    }
                }) {
                    Text("Target: ${config.targetRolloutPercent}% | Current: ${config.currentRolloutPercent}%")
                }
            }
            
            Span({
                style {
                    padding(4.px, 8.px)
                    backgroundColor(if (config.enabled) FlagentTheme.successBg(themeMode) else FlagentTheme.errorBg(themeMode))
                    color(if (config.enabled) FlagentTheme.Success else FlagentTheme.errorText(themeMode))
                    borderRadius(4.px)
                    fontSize(12.px)
                    fontWeight(500)
                }
            }) {
                Text(if (config.enabled) "Enabled" else "Disabled")
            }
        }
        
        // Progress bar
        Div({
            style {
                width(100.percent)
                height(8.px)
                backgroundColor(FlagentTheme.cardBorder(themeMode))
                borderRadius(4.px)
                overflow("hidden")
                marginBottom(12.px)
            }
        }) {
            Div({
                style {
                    width((config.currentRolloutPercent.toFloat() / config.targetRolloutPercent.toFloat() * 100).percent)
                    height(100.percent)
                    backgroundColor(FlagentTheme.Primary)
                }
            })
        }
        
        // Execute button
        Button({
            onClick { viewModel.executeRollout(config.id) }
            style {
                padding(8.px, 16.px)
                backgroundColor(FlagentTheme.Secondary)
                color(Color.white)
                border(0.px)
                borderRadius(6.px)
                cursor("pointer")
                fontSize(12.px)
            }
        }) {
            Text("Execute Rollout Step")
        }
    }
}
