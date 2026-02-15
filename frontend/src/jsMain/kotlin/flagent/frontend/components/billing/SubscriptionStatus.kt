package flagent.frontend.components.billing

import androidx.compose.runtime.*
import flagent.frontend.components.Icon
import flagent.frontend.state.LocalThemeMode
import flagent.frontend.state.ThemeMode
import flagent.frontend.theme.FlagentTheme
import flagent.frontend.viewmodel.BillingViewModel
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

/**
 * Subscription Status component (Phase 4)
 */
@Composable
fun SubscriptionStatus(viewModel: BillingViewModel) {
    val themeMode = LocalThemeMode.current
    Div({
        style {
            backgroundColor(FlagentTheme.cardBg(themeMode))
            borderRadius(8.px)
            padding(24.px)
            property("box-shadow", "0 1px 3px ${FlagentTheme.Shadow}")
        }
    }) {
        H2({
            style {
                fontSize(20.px)
                fontWeight(600)
                color(FlagentTheme.text(themeMode))
                margin(0.px)
                marginBottom(24.px)
            }
        }) {
            Text("Subscription")
        }
        
        // Plan cards
        Div({
            style {
                display(DisplayStyle.Grid)
                property("grid-template-columns", "repeat(auto-fit, minmax(250px, 1fr))")
                gap(16.px)
            }
        }) {
            PlanCard(
                themeMode = themeMode,
                name = "Free",
                price = "$0/month",
                features = listOf(
                    "Up to 10 flags",
                    "Basic evaluation",
                    "Community support"
                ),
                isCurrent = true,
                onSelect = {}
            )
            
            PlanCard(
                themeMode = themeMode,
                name = "Pro",
                price = "$49/month",
                features = listOf(
                    "Unlimited flags",
                    "Advanced features",
                    "Smart rollout",
                    "Priority support"
                ),
                isCurrent = false,
                onSelect = {
                    viewModel.createCheckoutSession("pro") { url ->
                        kotlinx.browser.window.open(url, "_blank")
                    }
                }
            )
            
            PlanCard(
                themeMode = themeMode,
                name = "Enterprise",
                price = "Custom",
                features = listOf(
                    "Everything in Pro",
                    "Multi-tenancy",
                    "SSO/SAML",
                    "Dedicated support"
                ),
                isCurrent = false,
                onSelect = {}
            )
        }
    }
}

@Composable
private fun PlanCard(
    themeMode: ThemeMode,
    name: String,
    price: String,
    features: List<String>,
    isCurrent: Boolean,
    onSelect: () -> Unit
) {
    Div({
        style {
            padding(24.px)
            border(2.px, LineStyle.Solid, if (isCurrent) FlagentTheme.Primary else FlagentTheme.cardBorder(themeMode))
            borderRadius(8.px)
            backgroundColor(if (isCurrent) FlagentTheme.infoBg(themeMode) else FlagentTheme.cardBg(themeMode))
        }
    }) {
        H3({
            style {
                fontSize(18.px)
                fontWeight(600)
                color(FlagentTheme.text(themeMode))
                margin(0.px)
                marginBottom(8.px)
            }
        }) {
            Text(name)
        }
        
        P({
            style {
                fontSize(24.px)
                fontWeight(700)
                color(FlagentTheme.Primary)
                margin(0.px)
                marginBottom(16.px)
            }
        }) {
            Text(price)
        }
        
        Ul({
            style {
                listStyleType("none")
                padding(0.px)
                margin(0.px)
                marginBottom(24.px)
            }
        }) {
            features.forEach { feature ->
                Li({
                    style {
                        display(DisplayStyle.Flex)
                        alignItems(AlignItems.Center)
                        gap(8.px)
                        marginBottom(8.px)
                        fontSize(14.px)
                        color(FlagentTheme.textLight(themeMode))
                    }
                }) {
                    Icon("check", size = 16.px, color = FlagentTheme.Primary)
                    Text(feature)
                }
            }
        }
        
        Button({
            onClick { onSelect() }
            if (isCurrent) {
                attr("disabled", "")
            }
            style {
                width(100.percent)
                padding(10.px)
                backgroundColor(if (isCurrent) FlagentTheme.cardBorder(themeMode) else FlagentTheme.Primary)
                color(if (isCurrent) FlagentTheme.textLight(themeMode) else Color.white)
                border(0.px)
                borderRadius(6.px)
                cursor(if (isCurrent) "not-allowed" else "pointer")
                fontSize(14.px)
                fontWeight(500)
            }
        }) {
            Text(if (isCurrent) "Current Plan" else "Select Plan")
        }
    }
}
