package flagent.frontend.components.landing

import androidx.compose.runtime.Composable
import flagent.frontend.components.Icon
import flagent.frontend.config.AppConfig
import flagent.frontend.navigation.Route
import flagent.frontend.navigation.Router
import flagent.frontend.theme.FlagentTheme
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

/** Plan availability: yes, no, or limit text */
private data class PlanAvailability(
    val openSource: String,
    val pro: String,
    val enterprise: String
)

/** Feature row for comparison table */
private data class FeatureRow(
    val capability: String,
    val availability: PlanAvailability
)

private val FEATURE_COMPARISON = listOf(
    // Flags and experiments
    listOf(
        FeatureRow("Feature flags", PlanAvailability("Yes", "Yes", "Yes")),
        FeatureRow("A/B testing & experiments", PlanAvailability("Yes", "Yes", "Yes")),
        FeatureRow("Gradual rollouts & kill switches", PlanAvailability("Yes", "Yes", "Yes")),
        FeatureRow("Multi-environment, targeting, segments", PlanAvailability("Yes", "Yes", "Yes")),
        FeatureRow("Client-side evaluation (offline-first)", PlanAvailability("Yes", "Yes", "Yes")),
        FeatureRow("Official SDKs (Kotlin, JS, Swift, Python, Go, Java)", PlanAvailability("Yes", "Yes", "Yes")),
        FeatureRow("Ktor plugin & REST API", PlanAvailability("Yes", "Yes", "Yes")),
        FeatureRow("Admin UI & Debug Console", PlanAvailability("Yes", "Yes", "Yes")),
    ),
    // Analytics
    listOf(
        FeatureRow("Analytics events (first_open, session_start, custom)", PlanAvailability("Yes", "Yes", "Yes")),
        FeatureRow("Evaluation counts & core metrics", PlanAvailability("Yes", "Yes", "Yes")),
        FeatureRow("Advanced analytics & insights (per-flag/variant)", PlanAvailability("No", "Yes", "Yes")),
        FeatureRow("Data recorders (Kafka, Kinesis, PubSub)", PlanAvailability("Yes", "Yes", "Yes")),
    ),
    // Crash analytics
    listOf(
        FeatureRow("Crash reporting (ingestion, list, stack traces)", PlanAvailability("Yes", "Yes", "Yes")),
        FeatureRow("Crash rate by flag & anomaly integration", PlanAvailability("No", "No", "Yes")),
    ),
    // Automation and reliability
    listOf(
        FeatureRow("Anomaly detection & alerts", PlanAvailability("No", "No", "Yes")),
        FeatureRow("Smart rollout (auto-rollback)", PlanAvailability("No", "No", "Yes")),
    ),
    // Integrations and hosting
    listOf(
        FeatureRow("Export / Import, Webhooks, Realtime (SSE), MCP", PlanAvailability("Yes", "Yes", "Yes")),
        FeatureRow("Self-hosted deployment", PlanAvailability("Yes", "Yes", "Yes")),
        FeatureRow("Managed cloud hosting", PlanAvailability("No", "Yes", "Yes")),
        FeatureRow("On-premise, multi-tenancy", PlanAvailability("No", "No", "Yes")),
    ),
    // Security and control
    listOf(
        FeatureRow("Basic auth (JWT)", PlanAvailability("Yes", "Yes", "Yes")),
        FeatureRow("RBAC", PlanAvailability("Basic", "Basic", "Custom roles")),
        FeatureRow("SSO (SAML, OIDC)", PlanAvailability("No", "No", "Yes")),
        FeatureRow("Audit log", PlanAvailability("Basic", "Basic", "Advanced")),
    ),
    // Support
    listOf(
        FeatureRow("Community support", PlanAvailability("Yes", "Yes", "Yes")),
        FeatureRow("Priority support", PlanAvailability("No", "Yes", "Yes")),
        FeatureRow("99.9% uptime SLA", PlanAvailability("No", "Yes", "Yes")),
        FeatureRow("Dedicated support", PlanAvailability("No", "No", "Yes")),
    ),
)

private val CATEGORY_TITLES = listOf(
    "Flags and experiments",
    "Analytics",
    "Crash analytics",
    "Automation and reliability",
    "Integrations and hosting",
    "Security and control",
    "Support",
)

/**
 * Pricing page with tariff cards and detailed feature comparison table (Unleash-style).
 */
@Composable
fun PricingPage() {
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
                FlagentTheme.GradientHero
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
            Div(attrs = {
                style {
                    textAlign("center")
                    marginBottom(48.px)
                }
            }) {
                H1(attrs = {
                    style {
                        fontSize(36.px)
                        fontWeight(700)
                        color(Color.white)
                        marginBottom(16.px)
                    }
                }) { Text("Pricing") }
                P(attrs = {
                    style {
                        fontSize(18.px)
                        color(Color("rgba(255,255,255,0.7)"))
                        lineHeight("1.6")
                        maxWidth(560.px)
                        property("margin", "0 auto")
                    }
                }) {
                    Text("Flagent offers flexible pricing for teams of all sizes. Start free, scale as you grow.")
                }
                P(attrs = {
                    style {
                        fontSize(14.px)
                        color(Color("rgba(255,255,255,0.55)"))
                        lineHeight("1.5")
                        maxWidth(640.px)
                        property("margin", "16px auto 0")
                    }
                }) {
                    Text("Flagent is in active development. Self-hosted is available today. Flagent Cloud (SaaS) is planned but not yet launched. Contact us for a consultation.")
                }
            }
            // Open Source Self-Hosted
            H2(attrs = {
                style {
                    fontSize(20.px)
                    fontWeight(600)
                    color(Color.white)
                    marginTop(32.px)
                    marginBottom(16.px)
                }
            }) { Text("Open Source Self-Hosted") }
            Div(attrs = {
                classes("pricing-grid")
                style {
                    display(DisplayStyle.Grid)
                    property("grid-template-columns", "repeat(auto-fit, minmax(280px, 1fr))")
                    gap(24.px)
                    marginBottom(48.px)
                }
            }) {
                PricingCard(
                    name = "Open Source",
                    price = "Free",
                    description = "Use today. Self-hosted, full-featured. No limits on flags or experiments.",
                    features = listOf(
                        "Unlimited flags & experiments",
                        "Analytics, crash reporting, A/B testing",
                        "Official SDKs (Kotlin, JS, Swift, Python, Go, Java)",
                        "Community support"
                    ),
                    ctaLabel = "Get Started",
                    highlighted = false,
                    badge = null,
                    onCta = { Router.navigateTo(Route.Dashboard) }
                )
            }
            // Flagent Cloud (SaaS) — Coming soon
            H2(attrs = {
                style {
                    fontSize(20.px)
                    fontWeight(600)
                    color(Color.white)
                    marginTop(24.px)
                    marginBottom(8.px)
                }
            }) { Text("Flagent Cloud (SaaS)") }
            P(attrs = {
                style {
                    fontSize(14.px)
                    color(Color("rgba(255,255,255,0.6)"))
                    marginBottom(16.px)
                }
            }) { Text("Planned — not yet available. Contact us for a consultation.") }
            Div(attrs = {
                classes("pricing-grid")
                style {
                    display(DisplayStyle.Grid)
                    property("grid-template-columns", "repeat(auto-fit, minmax(260px, 1fr))")
                    gap(24.px)
                    marginBottom(48.px)
                }
            }) {
                PricingCard(
                    name = "Starter",
                    price = "Contact us",
                    description = "Planned entry tier: managed hosting, community support.",
                    features = listOf(
                        "Everything in Open Source",
                        "Managed hosting (planned)",
                        "Community support"
                    ),
                    ctaLabel = "Contact us",
                    highlighted = false,
                    badge = "Planned",
                    onCta = { Router.navigateTo(Route.Home) }
                )
                PricingCard(
                    name = "Pro",
                    price = "Contact us",
                    description = "Planned: higher limits, SLA, priority support.",
                    features = listOf(
                        "Everything in Starter",
                        "Advanced analytics (planned)",
                        "99.9% uptime SLA",
                        "Priority support"
                    ),
                    ctaLabel = "Contact us",
                    highlighted = true,
                    badge = "Planned",
                    onCta = { Router.navigateTo(Route.Home) }
                )
                PricingCard(
                    name = "Team",
                    price = "Contact us",
                    description = "Planned: multi-tenant, extended limits.",
                    features = listOf(
                        "Everything in Pro",
                        "Multi-tenancy (planned)",
                        "Extended limits"
                    ),
                    ctaLabel = "Contact us",
                    highlighted = false,
                    badge = "Planned",
                    onCta = { Router.navigateTo(Route.Home) }
                )
            }
            // Enterprise
            H2(attrs = {
                style {
                    fontSize(20.px)
                    fontWeight(600)
                    color(Color.white)
                    marginTop(24.px)
                    marginBottom(16.px)
                }
            }) { Text("Enterprise") }
            Div(attrs = {
                classes("pricing-grid")
                style {
                    display(DisplayStyle.Grid)
                    property("grid-template-columns", "repeat(auto-fit, minmax(280px, 1fr))")
                    gap(24.px)
                    marginBottom(64.px)
                }
            }) {
                PricingCard(
                    name = "Enterprise",
                    price = "Custom",
                    description = "Dedicated infrastructure, custom development, SLA, on-premise, training.",
                    features = listOf(
                        "Everything in Pro / Team",
                        "SSO (SAML, OIDC)",
                        "Multi-tenancy & custom RBAC",
                        "On-premise deployment",
                        "Dedicated support"
                    ),
                    ctaLabel = "Contact Sales",
                    highlighted = false,
                    badge = null,
                    onCta = { Router.navigateTo(Route.Home) }
                )
            }
            H2(attrs = {
                style {
                    fontSize(24.px)
                    fontWeight(600)
                    color(Color.white)
                    marginBottom(24.px)
                    textAlign("center")
                }
            }) {
                Text("Compare plans")
            }
            FeatureComparisonTable()
            Div(attrs = {
                style {
                    textAlign("center")
                    marginTop(48.px)
                }
            }) {
                Button(attrs = {
                    style {
                        padding(12.px, 24.px)
                        backgroundColor(Color.transparent)
                        color(FlagentTheme.PrimaryLight)
                        border(1.px, LineStyle.Solid, Color("rgba(14, 165, 233, 0.5)"))
                        borderRadius(8.px)
                        cursor("pointer")
                        fontSize(14.px)
                        fontWeight(500)
                    }
                    onClick { Router.navigateTo(Route.Home) }
                }) {
                    Text("Back to Home")
                }
            }
        }
        LandingFooter()
    }
}

@Composable
private fun FeatureComparisonTable() {
    Div(attrs = {
        style {
            property("overflow-x", "auto")
            marginBottom(24.px)
        }
    }) {
        Div(attrs = {
            style {
                property("background", "rgba(255,255,255,0.04)")
                property("border", "1px solid rgba(255,255,255,0.08)")
                borderRadius(12.px)
                property("font-size", "14px")
            }
        }) {
            // Header row
            Div(attrs = {
                style {
                    display(DisplayStyle.Grid)
                    property("grid-template-columns", "1fr 100px 100px 120px")
                    property("gap", "0")
                    property("border-bottom", "1px solid rgba(255,255,255,0.12)")
                }
            }) {
                Div(attrs = { style { padding(14.px, 16.px); color(Color("rgba(255,255,255,0.7)")); fontWeight(500) } }) { Text("Capability") }
                Div(attrs = { style { padding(14.px, 16.px); textAlign("center"); color(Color.white); fontWeight(600) } }) { Text("Open Source") }
                Div(attrs = { style { padding(14.px, 16.px); textAlign("center"); color(FlagentTheme.PrimaryLight); fontWeight(600) } }) { Text("SaaS (Planned)") }
                Div(attrs = { style { padding(14.px, 16.px); textAlign("center"); color(Color.white); fontWeight(600) } }) { Text("Enterprise") }
            }
            FEATURE_COMPARISON.forEachIndexed { catIndex, categoryRows ->
                Div(attrs = {
                    style {
                        display(DisplayStyle.Grid)
                        property("grid-template-columns", "1fr 100px 100px 120px")
                        property("gap", "0")
                        property("background", "rgba(0,0,0,0.2)")
                    }
                }) {
                    Div(attrs = {
                        style {
                            property("grid-column", "1 / -1")
                            padding(12.px, 16.px)
                            color(Color.white)
                            fontWeight(600)
                            fontSize(13.px)
                            property("text-transform", "uppercase")
                            property("letter-spacing", "0.05em")
                        }
                    }) {
                        Text(CATEGORY_TITLES.getOrElse(catIndex) { "" })
                    }
                }
                categoryRows.forEach { row ->
                    Div(attrs = {
                        style {
                            display(DisplayStyle.Grid)
                            property("grid-template-columns", "1fr 100px 100px 120px")
                            property("gap", "0")
                            property("border-bottom", "1px solid rgba(255,255,255,0.06)")
                        }
                    }) {
                        Div(attrs = { style { padding(12.px, 16.px); color(Color("rgba(255,255,255,0.9)")) } }) { Text(row.capability) }
                        Div(attrs = { style { padding(12.px, 16.px); textAlign("center") } }) { PlanCell(row.availability.openSource) }
                        Div(attrs = { style { padding(12.px, 16.px); textAlign("center") } }) { PlanCell(row.availability.pro) }
                        Div(attrs = { style { padding(12.px, 16.px); textAlign("center") } }) { PlanCell(row.availability.enterprise) }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlanCell(value: String) {
    when (value.lowercase()) {
        "yes" -> Span(attrs = {
            style {
                color(FlagentTheme.Success)
                display(DisplayStyle.Flex)
                alignItems(AlignItems.Center)
                justifyContent(JustifyContent.Center)
                gap(6.px)
            }
        }) {
            Icon("check_circle", size = 18.px, color = FlagentTheme.Success)
            Text("Yes")
        }
        "no" -> Span(attrs = {
            style {
                color(Color("rgba(255,255,255,0.4)"))
            }
        }) { Text("—") }
        else -> Span(attrs = {
            style {
                color(Color("rgba(255,255,255,0.85)"))
            }
        }) { Text(value) }
    }
}

@Composable
private fun PricingCard(
    name: String,
    price: String,
    description: String,
    features: List<String>,
    ctaLabel: String,
    highlighted: Boolean,
    badge: String? = null,
    onCta: () -> Unit
) {
    Div(attrs = {
        style {
            padding(32.px)
            property(
                "background",
                if (highlighted) "rgba(255, 255, 255, 0.06)" else "rgba(255, 255, 255, 0.04)"
            )
            property(
                "border",
                if (highlighted) "1px solid rgba(14, 165, 233, 0.4)" else "1px solid rgba(255, 255, 255, 0.08)"
            )
            property("backdrop-filter", "blur(12px)")
            borderRadius(16.px)
            property("box-shadow", if (highlighted) "0 8px 32px rgba(14, 165, 233, 0.15)" else "0 4px 24px rgba(0,0,0,0.15)")
            property("transition", "all 0.3s cubic-bezier(0.4, 0, 0.2, 1)")
        }
        onMouseEnter {
            val el = it.target as org.w3c.dom.HTMLElement
            el.style.transform = "translateY(-4px)"
            el.style.setProperty("box-shadow", if (highlighted) "0 12px 40px rgba(14, 165, 233, 0.25)" else "0 12px 40px rgba(0,0,0,0.25)")
        }
        onMouseLeave {
            val el = it.target as org.w3c.dom.HTMLElement
            el.style.transform = "translateY(0)"
            el.style.setProperty("box-shadow", if (highlighted) "0 8px 32px rgba(14, 165, 233, 0.15)" else "0 4px 24px rgba(0,0,0,0.15)")
        }
    }) {
        Div(attrs = {
            style {
                display(DisplayStyle.Flex)
                alignItems(AlignItems.Center)
                gap(8.px)
                marginBottom(8.px)
            }
        }) {
            H3(attrs = {
                style {
                    margin(0.px)
                    fontSize(20.px)
                    fontWeight(600)
                    color(Color.white)
                }
            }) { Text(name) }
            badge?.let { b ->
                Span(attrs = {
                    style {
                        fontSize(11.px)
                        padding(4.px, 8.px)
                        backgroundColor(Color("rgba(255,255,255,0.15)"))
                        borderRadius(6.px)
                        color(Color("rgba(255,255,255,0.85)"))
                        fontWeight(500)
                    }
                }) { Text(b) }
            }
        }
        Div(attrs = {
            style {
                marginBottom(16.px)
            }
        }) {
            Span(attrs = {
                style {
                    fontSize(32.px)
                    fontWeight(700)
                    color(if (highlighted) FlagentTheme.PrimaryLight else Color.white)
                }
            }) { Text(price) }
        }
        P(attrs = {
            style {
                margin(0.px, 0.px, 24.px, 0.px)
                fontSize(14.px)
                color(Color("rgba(255,255,255,0.65)"))
                lineHeight("1.5")
            }
        }) { Text(description) }
        Ul(attrs = {
            style {
                listStyle("none")
                padding(0.px)
                margin(0.px, 0.px, 24.px, 0.px)
            }
        }) {
            features.forEach { feature ->
                Li(attrs = {
                    style {
                        display(DisplayStyle.Flex)
                        alignItems(AlignItems.Center)
                        gap(10.px)
                        padding(6.px, 0.px)
                        fontSize(14.px)
                        color(Color("rgba(255,255,255,0.85)"))
                    }
                }) {
                    Span(attrs = {
                        style {
                            color(FlagentTheme.Success)
                            display(DisplayStyle.Flex)
                            alignItems(AlignItems.Center)
                        }
                    }) {
                        Icon("check_circle", size = 18.px, color = FlagentTheme.Success)
                    }
                    Text(feature)
                }
            }
        }
        Button(attrs = {
            style {
                width(100.percent)
                padding(14.px, 24.px)
                property(
                    "background",
                    if (highlighted) "linear-gradient(135deg, ${FlagentTheme.Primary} 0%, ${FlagentTheme.PrimaryDark} 100%)"
                    else "rgba(14, 165, 233, 0.2)"
                )
                color(Color.white)
                border(if (highlighted) 0.px else 1.px, LineStyle.Solid, Color("rgba(14, 165, 233, 0.5)"))
                borderRadius(10.px)
                cursor("pointer")
                fontSize(15.px)
                fontWeight(600)
                property("transition", "all 0.2s ease")
            }
            onClick { onCta() }
            onMouseEnter {
                val el = it.target as org.w3c.dom.HTMLElement
                if (!highlighted) {
                    el.style.backgroundColor = "rgba(14, 165, 233, 0.3)"
                } else {
                    el.style.transform = "scale(1.02)"
                }
            }
            onMouseLeave {
                val el = it.target as org.w3c.dom.HTMLElement
                if (!highlighted) {
                    el.style.backgroundColor = "rgba(14, 165, 233, 0.2)"
                } else {
                    el.style.transform = "scale(1)"
                }
            }
        }) {
            Text(ctaLabel)
        }
    }
}
