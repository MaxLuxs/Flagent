package flagent.frontend.components.landing

import androidx.compose.runtime.Composable
import flagent.frontend.config.AppConfig
import flagent.frontend.navigation.Route
import flagent.frontend.navigation.Router
import flagent.frontend.theme.FlagentTheme
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

/**
 * Pricing page placeholder. Links to contact/plans.
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
                "linear-gradient(135deg, #0f172a 0%, #1e293b 50%, #0f172a 100%)"
            )
            property("font-family", "'DM Sans', -apple-system, BlinkMacSystemFont, sans-serif")
        }
    }) {
        Div(attrs = {
            style {
                position(Position.Relative)
                padding(48.px, 24.px)
                maxWidth(700.px)
                property("margin", "0 auto")
                textAlign("center")
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
                    marginBottom(32.px)
                }
            }) {
                Text("Flagent offers flexible pricing for teams of all sizes. Contact us for enterprise plans and custom solutions.")
            }
            Button(attrs = {
                style {
                    padding(16.px, 32.px)
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
                }
                onClick { Router.navigateTo(Route.Home) }
            }) {
                Text("Back to Home")
            }
        }
        LandingFooter()
    }
}
