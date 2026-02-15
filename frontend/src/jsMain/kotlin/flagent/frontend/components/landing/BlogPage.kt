package flagent.frontend.components.landing

import androidx.compose.runtime.*
import flagent.frontend.components.Icon
import flagent.frontend.config.AppConfig
import flagent.frontend.navigation.Route
import flagent.frontend.navigation.Router
import flagent.frontend.theme.FlagentTheme
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

/**
 * Blog page: Coming soon placeholder, email subscription, link to GitHub Discussions.
 */
@Composable
fun BlogPage() {
    val email = remember { mutableStateOf("") }

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
                maxWidth(700.px)
                property("margin", "0 auto")
                property("z-index", "1")
                textAlign("center")
            }
        }) {
            Div(attrs = {
                style {
                    width(64.px)
                    height(64.px)
                    property("margin", "0 auto 24px")
                    borderRadius(16.px)
                    property(
                        "background",
                        "linear-gradient(135deg, rgba(14, 165, 233, 0.3) 0%, rgba(20, 184, 166, 0.25) 100%)"
                    )
                    display(DisplayStyle.Flex)
                    alignItems(AlignItems.Center)
                    justifyContent(JustifyContent.Center)
                }
            }) {
                Icon("article", size = 32.px, color = FlagentTheme.PrimaryLight)
            }
            H1(attrs = {
                style {
                    fontSize(36.px)
                    fontWeight(700)
                    color(Color.white)
                    marginBottom(16.px)
                }
            }) { Text("Blog") }
            P(attrs = {
                style {
                    fontSize(18.px)
                    color(Color("rgba(255,255,255,0.7)"))
                    lineHeight("1.6")
                    marginBottom(32.px)
                }
            }) {
                Text("Coming soon. Stay updated with Flagent news, tutorials, and best practices.")
            }
            Div(attrs = {
                style {
                    display(DisplayStyle.Flex)
                    flexWrap(FlexWrap.Wrap)
                    gap(12.px)
                    justifyContent(JustifyContent.Center)
                    marginBottom(32.px)
                }
            }) {
                Input(InputType.Email, attrs = {
                    value(email.value)
                    onInput { email.value = (it.target as org.w3c.dom.HTMLInputElement).value }
                    style {
                        padding(14.px, 20.px)
                        width(280.px)
                        backgroundColor(Color("rgba(255,255,255,0.06)"))
                        color(Color.white)
                        border(1.px, LineStyle.Solid, Color("rgba(255,255,255,0.12)"))
                        borderRadius(10.px)
                        fontSize(15.px)
                    }
                    attr("placeholder", "Your email")
                })
                Button(attrs = {
                    style {
                        padding(14.px, 28.px)
                        property(
                            "background",
                            "linear-gradient(135deg, ${FlagentTheme.Primary} 0%, ${FlagentTheme.PrimaryDark} 100%)"
                        )
                        color(Color.white)
                        border(0.px)
                        borderRadius(10.px)
                        cursor("pointer")
                        fontSize(15.px)
                        fontWeight(600)
                    }
                    // Newsletter: Coming soon — see internal/docs/tasks/TODO-plan.md
                    onClick { /* Coming soon — no backend yet */ }
                }) {
                    Text("Subscribe")
                }
                Span(attrs = {
                    style {
                        marginLeft(8.px)
                        fontSize(13.px)
                        color(Color("rgba(255,255,255,0.6)"))
                    }
                }) {
                    Text("(Coming soon)")
                }
            }
            A(href = AppConfig.blogUrl, attrs = {
                attr("target", "_blank")
                attr("rel", "noopener noreferrer")
                style {
                    display(DisplayStyle.Flex)
                    alignItems(AlignItems.Center)
                    gap(8.px)
                    padding(16.px, 32.px)
                    property(
                        "background",
                        "linear-gradient(135deg, ${FlagentTheme.Primary} 0%, ${FlagentTheme.PrimaryDark} 100%)"
                    )
                    color(Color.white)
                    textDecoration("none")
                    borderRadius(12.px)
                    fontSize(16.px)
                    fontWeight(600)
                }
                onMouseEnter {
                    (it.target as org.w3c.dom.HTMLElement).style.transform = "translateY(-2px)"
                }
                onMouseLeave {
                    (it.target as org.w3c.dom.HTMLElement).style.transform = "translateY(0)"
                }
            }) {
                Icon("forum", size = 20.px, color = Color.white)
                Text("Go to GitHub Discussions")
            }
            Div(attrs = {
                style {
                    marginTop(32.px)
                }
            }) {
                Button(attrs = {
                    style {
                        backgroundColor(Color.transparent)
                        color(FlagentTheme.PrimaryLight)
                        border(0.px)
                        cursor("pointer")
                        fontSize(14.px)
                        textDecoration("underline")
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
