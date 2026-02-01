package flagent.frontend.components

import androidx.compose.runtime.Composable
import flagent.frontend.config.AppConfig
import flagent.frontend.navigation.Route
import flagent.frontend.navigation.Router
import flagent.frontend.theme.FlagentTheme
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

/**
 * Landing page at / - brief description and navigation to main sections.
 * Redirect to /dashboard when authenticated (handled in App.kt).
 */
@Composable
fun LandingPage() {
    Div({
        style {
            padding(40.px)
            maxWidth(700.px)
            property("margin", "60px auto 0")
            textAlign("center")
        }
    }) {
        H1({
            style {
                fontSize(36.px)
                fontWeight("bold")
                color(FlagentTheme.Text)
                marginBottom(16.px)
            }
        }) {
            Text("Flagent")
        }
        P({
            style {
                fontSize(18.px)
                color(FlagentTheme.TextLight)
                lineHeight("1.6")
                marginBottom(40.px)
            }
        }) {
            Text("Feature flags, A/B testing, and dynamic configuration. Manage flags, run experiments, and roll out changes safely.")
        }
        Div({
            style {
                display(DisplayStyle.Flex)
                flexWrap(FlexWrap.Wrap)
                gap(16.px)
                justifyContent(JustifyContent.Center)
            }
        }) {
            Button({
                style {
                    padding(14.px, 28.px)
                    backgroundColor(FlagentTheme.Primary)
                    color(FlagentTheme.Background)
                    border(0.px)
                    borderRadius(8.px)
                    cursor("pointer")
                    fontSize(16.px)
                    fontWeight("600")
                }
                onClick { Router.navigateTo(Route.Dashboard) }
            }) {
                Text("Dashboard")
            }
            Button({
                style {
                    padding(14.px, 28.px)
                    backgroundColor(FlagentTheme.Background)
                    color(FlagentTheme.Primary)
                    border(1.px, LineStyle.Solid, FlagentTheme.Primary)
                    borderRadius(8.px)
                    cursor("pointer")
                    fontSize(16.px)
                    fontWeight("600")
                }
                onClick { Router.navigateTo(Route.FlagsList) }
            }) {
                Text("Flags")
            }
            if (AppConfig.requiresAuth) {
                Button({
                    style {
                        padding(14.px, 28.px)
                        backgroundColor(FlagentTheme.BackgroundAlt)
                        color(FlagentTheme.Text)
                        border(1.px, LineStyle.Solid, FlagentTheme.Border)
                        borderRadius(8.px)
                        cursor("pointer")
                        fontSize(16.px)
                        fontWeight("600")
                    }
                    onClick { Router.navigateTo(Route.Login) }
                }) {
                    Text("Sign in")
                }
            }
            if (AppConfig.Features.enableMultiTenancy) {
                Button({
                    style {
                        padding(14.px, 28.px)
                        backgroundColor(FlagentTheme.BackgroundAlt)
                        color(FlagentTheme.Text)
                        border(1.px, LineStyle.Solid, FlagentTheme.Border)
                        borderRadius(8.px)
                        cursor("pointer")
                        fontSize(16.px)
                        fontWeight("600")
                    }
                    onClick { Router.navigateTo(Route.Tenants) }
                }) {
                    Text("Tenants")
                }
            }
        }
        if (AppConfig.Features.enableMultiTenancy) {
            P({
                style {
                    marginTop(24.px)
                    fontSize(14.px)
                    color(FlagentTheme.TextLight)
                }
            }) {
                Text("No tenant yet? ")
                Button({
                    style {
                        color(FlagentTheme.Primary)
                        textDecoration("underline")
                        backgroundColor(Color("transparent"))
                        border(0.px)
                        cursor("pointer")
                        padding(0.px)
                        fontSize(14.px)
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
