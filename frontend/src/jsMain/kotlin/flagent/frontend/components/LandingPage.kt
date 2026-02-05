package flagent.frontend.components

import androidx.compose.runtime.Composable
import flagent.frontend.config.AppConfig
import flagent.frontend.navigation.Route
import flagent.frontend.navigation.Router
import flagent.frontend.theme.FlagentTheme
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

/**
 * Landing page at / - hero section with animated gradient background, decorative shapes,
 * feature cards, and navigation. When auth required, primary CTA is Sign in.
 */
@Composable
fun LandingPage() {
    Div({
        classes("landing-page")
        style {
            position(Position.Relative)
            minHeight(100.vh)
            overflow("hidden")
            property(
                "background",
                "linear-gradient(135deg, #0f172a 0%, #1e293b 20%, #0f172a 40%, #1e3a5f 60%, #0f172a 80%, #1e293b 100%)"
            )
            property("background-size", "400% 400%")
            property("animation", "morphGradient 20s ease infinite")
            property("font-family", "'DM Sans', -apple-system, BlinkMacSystemFont, sans-serif")
        }
    }) {
        // Dot grid pattern
        Div({
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

        // Floating blobs
        Div({
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
        Div({
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
        Div({
            style {
                position(Position.Absolute)
                property("top", "35%")
                property("right", "12%")
                width(140.px)
                height(140.px)
                borderRadius(50.percent)
                property("background", "radial-gradient(circle, rgba(56, 189, 248, 0.15) 0%, transparent 70%)")
                property("animation", "floatReverse 11s ease-in-out infinite")
                property("pointer-events", "none")
            }
        }) {}
        Div({
            style {
                position(Position.Absolute)
                property("top", "15%")
                property("left", "8%")
                width(100.px)
                height(100.px)
                borderRadius(50.percent)
                property("background", "radial-gradient(circle, rgba(20, 184, 166, 0.12) 0%, transparent 70%)")
                property("animation", "float 9s ease-in-out infinite reverse")
                property("pointer-events", "none")
            }
        }) {}
        Div({
            style {
                position(Position.Absolute)
                property("bottom", "25%")
                property("right", "5%")
                width(200.px)
                height(200.px)
                borderRadius(50.percent)
                property("background", "radial-gradient(circle, rgba(14, 165, 233, 0.1) 0%, transparent 70%)")
                property("animation", "floatSlow 16s ease-in-out infinite")
                property("pointer-events", "none")
            }
        }) {}

        // Glow orb
        Div({
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

        Div({
            style {
                position(Position.Relative)
                padding(48.px, 24.px)
                maxWidth(1100.px)
                property("margin", "0 auto")
                property("z-index", "1")
            }
        }) {
            // Hero section
            Div({
                style {
                    textAlign("center")
                    padding(48.px, 0.px)
                    marginBottom(56.px)
                }
            }) {
                // Logo badge
                Div({
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
                    Icon(name = "flag", size = 36.px, color = Color.white)
                }
                H1({
                    style {
                        fontSize(52.px)
                        fontWeight(700)
                        color(Color.white)
                        marginBottom(20.px)
                        property("letter-spacing", "-0.03em")
                        property("line-height", "1.15")
                    }
                }) {
                    Text("Flagent")
                }
                P({
                    style {
                        fontSize(20.px)
                        color(Color("rgba(255,255,255,0.7)"))
                        lineHeight("1.65")
                        maxWidth(560.px)
                        property("margin", "0 auto 40px")
                    }
                }) {
                    Text("Feature flags, A/B testing, and dynamic configuration. Manage flags, run experiments, and roll out changes safely.")
                }

                // CTA buttons - Sign in is primary when auth required
                Div({
                    style {
                        display(DisplayStyle.Flex)
                        flexWrap(FlexWrap.Wrap)
                        gap(16.px)
                        justifyContent(JustifyContent.Center)
                    }
                }) {
                    if (AppConfig.requiresAuth) {
                        Button({
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
                            onClick { Router.navigateTo(Route.Login) }
                        }) {
                            Icon("login", size = 20.px, color = Color.white)
                            Text(" ")
                            Text("Sign in")
                        }
                    }
                    Button({
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
                        onClick { Router.navigateTo(Route.Dashboard) }
                    }) {
                        Text("Dashboard")
                    }
                    Button({
                        style {
                            padding(18.px, 36.px)
                            backgroundColor(Color.transparent)
                            color(Color("rgba(255,255,255,0.85)"))
                            border(1.px, LineStyle.Solid, Color("rgba(255,255,255,0.25)"))
                            borderRadius(12.px)
                            cursor("pointer")
                            fontSize(16.px)
                            fontWeight(600)
                            property("transition", "all 0.25s ease")
                        }
                        onMouseEnter {
                            val el = it.target as org.w3c.dom.HTMLElement
                            el.style.backgroundColor = "rgba(255,255,255,0.08)"
                        }
                        onMouseLeave {
                            (it.target as org.w3c.dom.HTMLElement).style.backgroundColor = "transparent"
                        }
                        onClick { Router.navigateTo(Route.FlagsList) }
                    }) {
                        Text("Flags")
                    }
                    if (AppConfig.Features.enableMultiTenancy && !AppConfig.requiresAuth) {
                        Button({
                            style {
                                padding(18.px, 36.px)
                                backgroundColor(Color.transparent)
                                color(Color("rgba(255,255,255,0.85)"))
                                border(1.px, LineStyle.Solid, Color("rgba(255,255,255,0.25)"))
                                borderRadius(12.px)
                                cursor("pointer")
                                fontSize(16.px)
                                fontWeight(600)
                            }
                            onClick { Router.navigateTo(Route.Tenants) }
                        }) {
                            Text("Tenants")
                        }
                    }
                }
            }

            // Feature cards - glass style
            Div({
                style {
                    display(DisplayStyle.Grid)
                    property("grid-template-columns", "repeat(auto-fit, minmax(250px, 1fr))")
                    gap(24.px)
                    marginBottom(48.px)
                }
            }) {
                FeatureCard(
                    icon = "flag",
                    title = "Feature Flags",
                    description = "Toggle features on or off without redeploying. Target users by segments and attributes."
                ) {
                    Router.navigateTo(Route.FlagsList)
                }
                FeatureCard(
                    icon = "science",
                    title = "A/B Testing",
                    description = "Run experiments with variants. Measure impact and roll out winners confidently."
                ) {
                    Router.navigateTo(Route.Experiments)
                }
                FeatureCard(
                    icon = "trending_up",
                    title = "Gradual Rollout",
                    description = "Release features gradually. Percentage-based rollouts with instant rollback."
                ) {
                    Router.navigateTo(Route.Dashboard)
                }
                FeatureCard(
                    icon = "emergency",
                    title = "Kill Switches",
                    description = "Disable features instantly when issues arise. No deployment required."
                ) {
                    Router.navigateTo(Route.FlagsList)
                }
            }

            if (AppConfig.Features.enableMultiTenancy) {
                Div({
                    style {
                        textAlign("center")
                        marginTop(32.px)
                        padding(20.px)
                        property("background", "rgba(255,255,255,0.04)")
                        property("border", "1px solid rgba(255,255,255,0.08)")
                        borderRadius(14.px)
                        property("backdrop-filter", "blur(8px)")
                    }
                }) {
                    P({
                        style {
                            margin(0.px)
                            fontSize(15.px)
                            color(Color("rgba(255,255,255,0.7)"))
                        }
                    }) {
                        Text("No tenant yet? ")
                        Button({
                            style {
                                color(FlagentTheme.PrimaryLight)
                                textDecoration("underline")
                                backgroundColor(Color.transparent)
                                border(0.px)
                                cursor("pointer")
                                padding(0.px)
                                fontSize(15.px)
                                fontWeight(600)
                            }
                            onClick { Router.navigateToTenantsWithCreate() }
                        }) {
                            Text("Create your first tenant")
                        }
                        Text(" to get started.")
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureCard(
    icon: String,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Div({
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
        onClick { onClick() }
    }) {
        Div({
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
            Icon(name = icon, size = 28.px, color = FlagentTheme.PrimaryLight)
        }
        H3({
            style {
                margin(0.px, 0.px, 10.px, 0.px)
                fontSize(18.px)
                fontWeight(600)
                color(Color.white)
            }
        }) {
            Text(title)
        }
        P({
            style {
                margin(0.px)
                fontSize(14.px)
                color(Color("rgba(255,255,255,0.65)"))
                lineHeight("1.55")
            }
        }) {
            Text(description)
        }
    }
}
