package flagent.frontend.components.landing

import androidx.compose.runtime.Composable
import flagent.frontend.config.AppConfig
import flagent.frontend.navigation.Route
import flagent.frontend.navigation.Router
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

/**
 * Marketing landing footer: product description, links, copyright.
 */
@Composable
fun LandingFooter() {
    Div(attrs = {
        style {
            padding(48.px, 24.px)
            property("border-top", "1px solid rgba(255,255,255,0.08)")
            property("background", "rgba(0,0,0,0.2)")
            property("font-family", "'DM Sans', -apple-system, BlinkMacSystemFont, sans-serif")
        }
    }) {
        Div(attrs = {
            style {
                maxWidth(1100.px)
                property("margin", "0 auto")
                display(DisplayStyle.Flex)
                flexWrap(FlexWrap.Wrap)
                gap(48.px)
                justifyContent(JustifyContent.SpaceBetween)
            }
        }) {
            Div(attrs = {
                style {
                    maxWidth(320.px)
                }
            }) {
                P(attrs = {
                    style {
                        fontSize(14.px)
                        color(Color("rgba(255,255,255,0.7)"))
                        lineHeight("1.6")
                        margin(0.px)
                    }
                }) {
                    Text("Flagent reduces the risk of releasing new features, drives innovation by streamlining software releases, and increases revenue by optimizing end-user experience.")
                }
            }
            Div(attrs = {
                style {
                    display(DisplayStyle.Flex)
                    flexWrap(FlexWrap.Wrap)
                    gap(32.px)
                }
            }) {
                FooterColumn("Resources", listOf(
                    "Documentation" to AppConfig.docsUrl,
                    "API Reference" to "${AppConfig.docsUrl}api/",
                    "GitHub" to AppConfig.githubUrl,
                    "Roadmap" to "${AppConfig.docsUrl}guides/roadmap.md"
                ))
                FooterColumn("Product", listOf(
                    "Feature Flags" to Route.FlagsList.PATH,
                    "A/B Testing" to Route.Experiments.PATH,
                    "Analytics" to Route.Analytics.PATH
                ))
                FooterColumn("Legal", listOf(
                    "License" to "${AppConfig.githubUrl}/blob/main/LICENSE"
                ))
            }
        }
        Div(attrs = {
            style {
                maxWidth(1100.px)
                property("margin", "24px auto 0")
                paddingTop(24.px)
                property("border-top", "1px solid rgba(255,255,255,0.06)")
                fontSize(12.px)
                color(Color("rgba(255,255,255,0.5)"))
            }
        }) {
            Text("© ${js("new Date().getFullYear()") as Int} Flagent. Apache 2.0 License.")
        }
    }
}

@Composable
private fun FooterColumn(title: String, links: List<Pair<String, String>>) {
    Div(attrs = {
        style {
            display(DisplayStyle.Flex)
            flexDirection(FlexDirection.Column)
            gap(12.px)
        }
    }) {
        P(attrs = {
            style {
                fontSize(12.px)
                fontWeight(600)
                color(Color("rgba(255,255,255,0.5)"))
                margin(0.px, 0.px, 4.px, 0.px)
                property("text-transform", "uppercase")
                property("letter-spacing", "0.05em")
            }
        }) { Text(title) }
        links.forEach { (label, url) ->
            if (url.startsWith("http")) {
                A(href = url, attrs = {
                    attr("target", "_blank")
                    attr("rel", "noopener noreferrer")
                    style {
                        color(Color("rgba(255,255,255,0.7)"))
                        textDecoration("none")
                        fontSize(14.px)
                    }
                    onMouseEnter {
                        (it.target as org.w3c.dom.HTMLElement).style.color = "white"
                    }
                    onMouseLeave {
                        (it.target as org.w3c.dom.HTMLElement).style.color = "rgba(255,255,255,0.7)"
                    }
                }) { Text(label) }
            } else {
                A(href = url, attrs = {
                    style {
                        color(Color("rgba(255,255,255,0.7)"))
                        textDecoration("none")
                        fontSize(14.px)
                    }
                    onClick { e -> e.preventDefault(); Router.navigateToPath(url) }
                    onMouseEnter {
                        (it.target as org.w3c.dom.HTMLElement).style.color = "white"
                    }
                    onMouseLeave {
                        (it.target as org.w3c.dom.HTMLElement).style.color = "rgba(255,255,255,0.7)"
                    }
                }) { Text(label) }
            }
        }
    }
}
