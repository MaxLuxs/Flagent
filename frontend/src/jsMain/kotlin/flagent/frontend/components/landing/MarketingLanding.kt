package flagent.frontend.components.landing

import androidx.compose.runtime.Composable
import flagent.frontend.components.Icon
import flagent.frontend.config.AppConfig
import flagent.frontend.navigation.Route
import flagent.frontend.navigation.Router
import flagent.frontend.theme.FlagentTheme
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

/**
 * Full marketing landing: navbar, hero, features, use cases, footer.
 * Shown at / when showMarketingLanding is true (SaaS).
 */
@Composable
fun MarketingLanding() {
    LandingNavbar()
    Div(attrs = {
        classes("landing-page")
        style {
            position(Position.Relative)
            minHeight(100.vh)
            overflow("hidden")
            paddingTop(80.px)
            property(
                "background",
                "linear-gradient(135deg, #0f172a 0%, #1e293b 20%, #0f172a 40%, #1e3a5f 60%, #0f172a 80%, #1e293b 100%)"
            )
            property("background-size", "400% 400%")
            property("animation", "morphGradient 20s ease infinite")
            property("font-family", "'DM Sans', -apple-system, BlinkMacSystemFont, sans-serif")
        }
    }) {
        LandingBackgroundShapes()
        Div(attrs = {
            style {
                position(Position.Relative)
                padding(48.px, 24.px)
                maxWidth(1100.px)
                property("margin", "0 auto")
                property("z-index", "1")
            }
        }) {
            HeroSection()
            FeaturesSection()
            UseCasesSection()
        }
        LandingFooter()
    }
}

@Composable
private fun LandingBackgroundShapes() {
    Div(attrs = {
        style {
            position(Position.Absolute)
            property("inset", "0")
            property(
                "background-image",
                "radial-gradient(rgba(14, 165, 233, 0.12) 1px, transparent 1px)"
            )
            property("background-size", "28px 28px")
            property("pointer-events", "none")
        }
    }) {}
    Div(attrs = {
        style {
            position(Position.Absolute)
            property("top", "-8%")
            property("right", "-3%")
            width(450.px)
            height(450.px)
            borderRadius(50.percent)
            property("background", "radial-gradient(circle, rgba(14, 165, 233, 0.2) 0%, transparent 65%)")
            property("animation", "float 14s ease-in-out infinite")
            property("pointer-events", "none")
        }
    }) {}
    Div(attrs = {
        style {
            position(Position.Absolute)
            property("bottom", "-12%")
            property("left", "-6%")
            width(500.px)
            height(500.px)
            borderRadius(50.percent)
            property("background", "radial-gradient(circle, rgba(20, 184, 166, 0.18) 0%, transparent 65%)")
            property("animation", "floatSlow 20s ease-in-out infinite")
            property("pointer-events", "none")
        }
    }) {}
    Div(attrs = {
        style {
            position(Position.Absolute)
            property("top", "-15%")
            property("left", "50%")
            property("transform", "translateX(-50%)")
            width(600.px)
            height(400.px)
            borderRadius(50.percent)
            property("background", "radial-gradient(ellipse, rgba(14, 165, 233, 0.15) 0%, transparent 70%)")
            property("animation", "glowPulse 10s ease-in-out infinite")
            property("pointer-events", "none")
        }
    }) {}
}

@Composable
private fun HeroSection() {
    Div(attrs = {
        style {
            textAlign("center")
            padding(48.px, 0.px)
            marginBottom(64.px)
        }
    }) {
        Div(attrs = {
            style {
                width(72.px)
                height(72.px)
                property("margin", "0 auto 24px")
                borderRadius(18.px)
                property(
                    "background",
                    "linear-gradient(135deg, ${FlagentTheme.Primary} 0%, ${FlagentTheme.Secondary} 100%)"
                )
                display(DisplayStyle.Flex)
                alignItems(AlignItems.Center)
                justifyContent(JustifyContent.Center)
                property("box-shadow", "0 12px 40px rgba(14, 165, 233, 0.4)")
                property("animation", "float 8s ease-in-out infinite")
            }
        }) {
            Icon("flag", size = 36.px, color = Color.white)
        }
        H1(attrs = {
            style {
                fontSize(52.px)
                fontWeight(700)
                color(Color.white)
                marginBottom(20.px)
                property("letter-spacing", "-0.03em")
                property("line-height", "1.15")
            }
        }) { Text("The First Kotlin-Native Feature Flag Platform") }
        P(attrs = {
            style {
                fontSize(20.px)
                color(Color("rgba(255,255,255,0.7)"))
                lineHeight("1.65")
                maxWidth(560.px)
                property("margin", "0 auto 40px")
            }
        }) {
            Text("Type-safe, coroutine-first feature flags and experimentation. Manage flags, run experiments, and roll out changes safely.")
        }
        Div(attrs = {
            style {
                display(DisplayStyle.Flex)
                flexWrap(FlexWrap.Wrap)
                gap(16.px)
                justifyContent(JustifyContent.Center)
            }
        }) {
            if (AppConfig.requiresAuth) {
                Button(attrs = {
                    style {
                        padding(18.px, 36.px)
                        property(
                            "background",
                            "linear-gradient(135deg, ${FlagentTheme.Primary} 0%, ${FlagentTheme.PrimaryDark} 100%)"
                        )
                        color(Color.white)
                        border(0.px)
                        borderRadius(12.px)
                        cursor("pointer")
                        fontSize(16.px)
                        fontWeight(600)
                        display(DisplayStyle.Flex)
                        alignItems(AlignItems.Center)
                        justifyContent(JustifyContent.Center)
                        gap(8.px)
                        property("box-shadow", "0 8px 24px rgba(14, 165, 233, 0.45)")
                        property("transition", "all 0.25s cubic-bezier(0.4, 0, 0.2, 1)")
                    }
                    onClick { Router.navigateTo(Route.Login) }
                    onMouseEnter {
                        val el = it.target as org.w3c.dom.HTMLElement
                        el.style.transform = "translateY(-3px)"
                        el.style.setProperty("box-shadow", "0 12px 32px rgba(14, 165, 233, 0.55)")
                    }
                    onMouseLeave {
                        val el = it.target as org.w3c.dom.HTMLElement
                        el.style.transform = "translateY(0)"
                        el.style.setProperty("box-shadow", "0 8px 24px rgba(14, 165, 233, 0.45)")
                    }
                }) {
                    Icon("login", size = 20.px, color = Color.white)
                    Text(" ")
                    Text("Sign in")
                }
            }
            Button(attrs = {
                style {
                    padding(18.px, 36.px)
                    backgroundColor(Color.transparent)
                    color(FlagentTheme.PrimaryLight)
                    border(2.px, LineStyle.Solid, Color("rgba(14, 165, 233, 0.6)"))
                    borderRadius(12.px)
                    cursor("pointer")
                    fontSize(16.px)
                    fontWeight(600)
                    property("transition", "all 0.25s ease")
                }
                onClick { Router.navigateTo(Route.Dashboard) }
                onMouseEnter {
                    val el = it.target as org.w3c.dom.HTMLElement
                    el.style.backgroundColor = "rgba(14, 165, 233, 0.15)"
                    el.style.borderColor = FlagentTheme.PrimaryLight.toString()
                }
                onMouseLeave {
                    val el = it.target as org.w3c.dom.HTMLElement
                    el.style.backgroundColor = "transparent"
                    el.style.borderColor = "rgba(14, 165, 233, 0.6)"
                }
            }) {
                Text("Get Started")
            }
            A(href = AppConfig.githubUrl, attrs = {
                attr("target", "_blank")
                attr("rel", "noopener noreferrer")
                style {
                    display(DisplayStyle.Flex)
                    alignItems(AlignItems.Center)
                    justifyContent(JustifyContent.Center)
                    gap(8.px)
                    padding(18.px, 36.px)
                    backgroundColor(Color.transparent)
                    color(Color("rgba(255,255,255,0.85)"))
                    border(1.px, LineStyle.Solid, Color("rgba(255,255,255,0.25)"))
                    borderRadius(12.px)
                    textDecoration("none")
                    fontSize(16.px)
                    fontWeight(600)
                }
                onMouseEnter {
                    (it.target as org.w3c.dom.HTMLElement).style.backgroundColor = "rgba(255,255,255,0.08)"
                }
                onMouseLeave {
                    (it.target as org.w3c.dom.HTMLElement).style.backgroundColor = "transparent"
                }
            }) {
                Icon("code", size = 20.px, color = Color("rgba(255,255,255,0.85)"))
                Text("GitHub")
            }
        }
    }
}

@Composable
private fun FeaturesSection() {
    Div(attrs = {
        style {
            marginBottom(64.px)
        }
    }) {
        H2(attrs = {
            style {
                fontSize(28.px)
                fontWeight(600)
                color(Color.white)
                textAlign("center")
                marginBottom(40.px)
            }
        }) { Text("Why Flagent?") }
        Div(attrs = {
            style {
                display(DisplayStyle.Grid)
                property("grid-template-columns", "repeat(auto-fit, minmax(250px, 1fr))")
                gap(24.px)
            }
        }) {
            MarketingFeatureCard("flag", "Feature Flags", "Toggle features on or off without redeploying. Target users by segments and attributes.") {
                Router.navigateTo(Route.FlagsList)
            }
            MarketingFeatureCard("science", "A/B Testing", "Run experiments with variants. Measure impact and roll out winners confidently.") {
                Router.navigateTo(Route.Experiments)
            }
            MarketingFeatureCard("trending_up", "Gradual Rollout", "Release features gradually. Percentage-based rollouts with instant rollback.") {
                Router.navigateTo(Route.Dashboard)
            }
            MarketingFeatureCard("emergency", "Kill Switches", "Disable features instantly when issues arise. No deployment required.") {
                Router.navigateTo(Route.FlagsList)
            }
        }
    }
}

@Composable
private fun MarketingFeatureCard(
    icon: String,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Div(attrs = {
        style {
            padding(28.px)
            property("background", "rgba(255, 255, 255, 0.04)")
            property("border", "1px solid rgba(255, 255, 255, 0.08)")
            property("backdrop-filter", "blur(12px)")
            borderRadius(16.px)
            property("box-shadow", "0 4px 24px rgba(0,0,0,0.15)")
            property("transition", "all 0.3s cubic-bezier(0.4, 0, 0.2, 1)")
            cursor("pointer")
        }
        onClick { onClick() }
        onMouseEnter {
            val el = it.target as org.w3c.dom.HTMLElement
            el.style.transform = "translateY(-6px)"
            el.style.setProperty("box-shadow", "0 12px 40px rgba(0,0,0,0.25)")
            el.style.setProperty("border-color", "rgba(14, 165, 233, 0.3)")
        }
        onMouseLeave {
            val el = it.target as org.w3c.dom.HTMLElement
            el.style.transform = "translateY(0)"
            el.style.setProperty("box-shadow", "0 4px 24px rgba(0,0,0,0.15)")
            el.style.setProperty("border-color", "rgba(255, 255, 255, 0.08)")
        }
    }) {
        Div(attrs = {
            style {
                width(52.px)
                height(52.px)
                borderRadius(14.px)
                property("background", "linear-gradient(135deg, rgba(14, 165, 233, 0.25) 0%, rgba(20, 184, 166, 0.2) 100%)")
                display(DisplayStyle.Flex)
                alignItems(AlignItems.Center)
                justifyContent(JustifyContent.Center)
                marginBottom(18.px)
            }
        }) {
            Icon(icon, size = 28.px, color = FlagentTheme.PrimaryLight)
        }
        H3(attrs = {
            style {
                margin(0.px, 0.px, 10.px, 0.px)
                fontSize(18.px)
                fontWeight(600)
                color(Color.white)
            }
        }) { Text(title) }
        P(attrs = {
            style {
                margin(0.px)
                fontSize(14.px)
                color(Color("rgba(255,255,255,0.65)"))
                lineHeight("1.55")
            }
        }) { Text(description) }
    }
}

@Composable
private fun UseCasesSection() {
    Div(attrs = {
        style {
            padding(40.px)
            property("background", "rgba(255,255,255,0.04)")
            property("border", "1px solid rgba(255,255,255,0.08)")
            borderRadius(20.px)
            property("backdrop-filter", "blur(8px)")
        }
    }) {
        H2(attrs = {
            style {
                fontSize(24.px)
                fontWeight(600)
                color(Color.white)
                textAlign("center")
                marginBottom(32.px)
            }
        }) { Text("Use Cases") }
        Div(attrs = {
            style {
                display(DisplayStyle.Grid)
                property("grid-template-columns", "repeat(auto-fit, minmax(260px, 1fr))")
                gap(24.px)
            }
        }) {
            UseCaseCard(
                "Safe Deployments",
                "Deploy code to production behind feature flags, enabling instant rollbacks without code changes."
            )
            UseCaseCard(
                "A/B Testing",
                "Run experiments to test new features and measure their impact on key metrics."
            )
            UseCaseCard(
                "Gradual Rollouts",
                "Roll out features gradually and instantly disable if issues are detected."
            )
        }
    }
}

@Composable
private fun UseCaseCard(title: String, description: String) {
    Div(attrs = {
        style {
            padding(20.px)
            property("background", "rgba(0,0,0,0.2)")
            borderRadius(12.px)
            property("border", "1px solid rgba(255,255,255,0.06)")
        }
    }) {
        H3(attrs = {
            style {
                margin(0.px, 0.px, 8.px, 0.px)
                fontSize(16.px)
                fontWeight(600)
                color(FlagentTheme.PrimaryLight)
            }
        }) { Text(title) }
        P(attrs = {
            style {
                margin(0.px)
                fontSize(14.px)
                color(Color("rgba(255,255,255,0.7)"))
                lineHeight("1.5")
            }
        }) { Text(description) }
    }
}
