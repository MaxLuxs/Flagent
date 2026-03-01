package flagent.frontend.components.landing

import androidx.compose.runtime.*
import flagent.frontend.api.ApiClient
import flagent.frontend.components.Icon
import flagent.frontend.config.AppConfig
import flagent.frontend.navigation.Route
import flagent.frontend.navigation.Router
import flagent.frontend.state.LocalGlobalState
import flagent.frontend.state.Notification
import flagent.frontend.state.NotificationType
import flagent.frontend.theme.FlagentTheme
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

/**
 * Marketing landing footer: product description, links, newsletter, trust badges, copyright.
 */
@Composable
fun LandingFooter() {
    val email = remember { mutableStateOf("") }
    val globalState = LocalGlobalState.current
    val scope = rememberCoroutineScope()
    val subscribing = remember { mutableStateOf(false) }

    Div(attrs = {
        style {
            padding(48.px, 24.px)
            property("border-top", "1px solid rgba(255,255,255,0.08)")
            property("background", "rgba(0,0,0,0.2)")
            property("font-family", "'DM Sans', -apple-system, BlinkMacSystemFont, sans-serif")
        }
    }) {
        Div(attrs = {
            classes("footer-main")
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
                    Text("Flagent reduces release risk and drives innovation: ship behind flags, roll back in one click without redeploying, and run A/B tests with type-safe SDKs. Open source, self-host free.")
                }
                Div(attrs = {
                    style {
                        marginTop(24.px)
                        display(DisplayStyle.Flex)
                        flexWrap(FlexWrap.Wrap)
                        gap(12.px)
                    }
                }) {
                    Span(attrs = {
                        style {
                            display(DisplayStyle.Flex)
                            alignItems(AlignItems.Center)
                            gap(6.px)
                            padding(6.px, 12.px)
                            property("background", "rgba(255,255,255,0.06)")
                            borderRadius(8.px)
                            fontSize(12.px)
                            color(Color("rgba(255,255,255,0.8)"))
                        }
                    }) {
                        Icon("code", size = 14.px, color = FlagentTheme.PrimaryLight)
                        Text("Open Source")
                    }
                    Span(attrs = {
                        style {
                            display(DisplayStyle.Flex)
                            alignItems(AlignItems.Center)
                            gap(6.px)
                            padding(6.px, 12.px)
                            property("background", "rgba(255,255,255,0.06)")
                            borderRadius(8.px)
                            fontSize(12.px)
                            color(Color("rgba(255,255,255,0.8)"))
                        }
                    }) {
                        Icon("smart_toy", size = 14.px, color = FlagentTheme.PrimaryLight)
                        Text("Kotlin Multiplatform")
                    }
                    Span(attrs = {
                        style {
                            display(DisplayStyle.Flex)
                            alignItems(AlignItems.Center)
                            gap(6.px)
                            padding(6.px, 12.px)
                            property("background", "rgba(255,255,255,0.06)")
                            borderRadius(8.px)
                            fontSize(12.px)
                            color(Color("rgba(255,255,255,0.8)"))
                        }
                    }) {
                        Icon("verified", size = 14.px, color = FlagentTheme.PrimaryLight)
                        Text("Apache 2.0")
                    }
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
                    "API Reference" to "${AppConfig.docsUrl}api-docs.html",
                    "GitHub" to AppConfig.githubUrl,
                    "Roadmap" to "${AppConfig.docsUrl}#/guides/roadmap"
                ))
                FooterColumn("Product", listOf(
                    "Feature Flags" to Route.FlagsList.PATH,
                    "A/B Testing" to Route.Experiments.PATH,
                    "Analytics" to Route.Analytics.PATH
                ))
                FooterColumn("Legal", listOf(
                    "License" to "${AppConfig.githubUrl}/blob/main/LICENSE"
                ))
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
                    }) { Text("Newsletter") }
                    Div(attrs = {
                        style {
                            display(DisplayStyle.Flex)
                            gap(8.px)
                        }
                    }) {
                        Input(InputType.Email, attrs = {
                            value(email.value)
                            onInput { email.value = (it.target as org.w3c.dom.HTMLInputElement).value }
                            style {
                                padding(10.px, 14.px)
                                width(180.px)
                                backgroundColor(Color("rgba(255,255,255,0.06)"))
                                color(Color.white)
                                border(1.px, LineStyle.Solid, Color("rgba(255,255,255,0.12)"))
                                borderRadius(8.px)
                                fontSize(14.px)
                            }
                            attr("placeholder", "Your email")
                        })
                        Button(attrs = {
                            style {
                                padding(10.px, 18.px)
                                property(
                                    "background",
                                    "linear-gradient(135deg, ${FlagentTheme.Primary} 0%, ${FlagentTheme.PrimaryDark} 100%)"
                                )
                                color(Color.white)
                                border(0.px)
                                borderRadius(8.px)
                                cursor(if (subscribing.value) "wait" else "pointer")
                                fontSize(14.px)
                                fontWeight(500)
                            }
                            onClick {
                                val addr = email.value.trim()
                                if (addr.isBlank()) {
                                    globalState.addNotification(Notification(message = "Enter your email", type = NotificationType.WARNING))
                                    return@onClick
                                }
                                subscribing.value = true
                                scope.launch {
                                    try {
                                        ApiClient.subscribeNewsletter(addr)
                                        globalState.addNotification(Notification(message = "Subscribed successfully", type = NotificationType.SUCCESS))
                                        email.value = ""
                                    } catch (e: Throwable) {
                                        val msg = e.message?.takeIf { it.isNotBlank() } ?: "Subscription failed"
                                        globalState.addNotification(Notification(message = msg, type = NotificationType.ERROR))
                                    } finally {
                                        subscribing.value = false
                                    }
                                }
                            }
                        }) {
                            Text(if (subscribing.value) "…" else "Subscribe")
                        }
                    }
                }
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
            Text("© ${js("new Date().getFullYear()") as Int} Flagent. ")
            A(href = AppConfig.docsUrl, attrs = {
                attr("target", "_blank")
                attr("rel", "noopener noreferrer")
                style {
                    color(Color("rgba(255,255,255,0.6)"))
                    textDecoration("none")
                }
                onMouseEnter { (it.target as org.w3c.dom.HTMLElement).style.color = "white" }
                onMouseLeave { (it.target as org.w3c.dom.HTMLElement).style.color = "rgba(255,255,255,0.6)" }
            }) { Text("Powered by open source") }
            Text(". Apache 2.0 License.")
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
