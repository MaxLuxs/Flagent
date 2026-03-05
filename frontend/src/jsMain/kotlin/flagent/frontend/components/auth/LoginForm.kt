package flagent.frontend.components.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import flagent.frontend.components.Icon
import flagent.frontend.config.AppConfig
import flagent.frontend.i18n.LocalizedStrings
import flagent.frontend.navigation.Route
import flagent.frontend.navigation.Router
import flagent.frontend.state.LocalThemeMode
import flagent.frontend.theme.AppGradientBackground
import flagent.frontend.theme.FlagentTheme
import flagent.frontend.viewmodel.AuthViewModel
import kotlinx.browser.window
import org.w3c.dom.url.URLSearchParams
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.FlexDirection
import org.jetbrains.compose.web.css.FlexWrap
import org.jetbrains.compose.web.css.JustifyContent
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.Position
import org.jetbrains.compose.web.css.alignItems
import org.jetbrains.compose.web.css.backgroundColor
import org.jetbrains.compose.web.css.border
import org.jetbrains.compose.web.css.borderRadius
import org.jetbrains.compose.web.css.color
import org.jetbrains.compose.web.css.cursor
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.flex
import org.jetbrains.compose.web.css.flexDirection
import org.jetbrains.compose.web.css.flexShrink
import org.jetbrains.compose.web.css.flexWrap
import org.jetbrains.compose.web.css.fontSize
import org.jetbrains.compose.web.css.fontWeight
import org.jetbrains.compose.web.css.gap
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.justifyContent
import org.jetbrains.compose.web.css.margin
import org.jetbrains.compose.web.css.marginBottom
import org.jetbrains.compose.web.css.marginTop
import org.jetbrains.compose.web.css.maxWidth
import org.jetbrains.compose.web.css.minHeight
import org.jetbrains.compose.web.css.overflow
import org.jetbrains.compose.web.css.padding
import org.jetbrains.compose.web.css.paddingLeft
import org.jetbrains.compose.web.css.paddingTop
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.position
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.textAlign
import org.jetbrains.compose.web.css.textDecoration
import org.jetbrains.compose.web.css.vh
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Label
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.HTMLElement

/**
 * Login Form with animated background, decorative shapes, and glass-morphism card.
 */
@Composable
fun LoginForm(
    viewModel: AuthViewModel,
    onSuccess: () -> Unit
) {
    val themeMode = LocalThemeMode.current
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val passwordVisible = remember { mutableStateOf(false) }
    val rememberMe = remember { mutableStateOf(false) }
    // Optional tenant key from URL (?tenant=my-tenant) for Google OAuth state
    val tenantKeyFromUrl = remember {
        runCatching {
            val params = URLSearchParams(window.location.search)
            params.get("tenant") ?: ""
        }.getOrDefault("")
    }

    AppGradientBackground {
        Div({
            classes("login-page")
            style {
                position(Position.Relative)
                display(DisplayStyle.Flex)
                justifyContent(JustifyContent.Center)
                alignItems(AlignItems.Center)
                minHeight(100.vh)
                overflow("hidden")
            }
        }) {
            // Background circles (fixed, behind the card)
            Div({
                style {
                    position(Position.Fixed)
                    property("top", "10%")
                    property("right", "15%")
                    width(320.px)
                    height(320.px)
                    borderRadius(50.percent)
                    property("z-index", "0")
                    property("pointer-events", "none")
                    property(
                        "background",
                        "radial-gradient(circle, rgba(14, 165, 233, 0.25) 0%, transparent 70%)"
                    )
                    property("animation", "float 12s ease-in-out infinite")
                }
            }) {}
            Div({
                style {
                    position(Position.Fixed)
                    property("bottom", "20%")
                    property("left", "10%")
                    width(280.px)
                    height(280.px)
                    borderRadius(50.percent)
                    property("z-index", "0")
                    property("pointer-events", "none")
                    property(
                        "background",
                        "radial-gradient(circle, rgba(20, 184, 166, 0.2) 0%, transparent 70%)"
                    )
                    property("animation", "floatSlow 18s ease-in-out infinite")
                }
            }) {}
            Div({
                style {
                    position(Position.Fixed)
                    property("top", "50%")
                    property("left", "5%")
                    width(120.px)
                    height(120.px)
                    borderRadius(50.percent)
                    property("z-index", "0")
                    property("pointer-events", "none")
                    property(
                        "background",
                        "radial-gradient(circle, rgba(56, 189, 248, 0.2) 0%, transparent 70%)"
                    )
                    property("animation", "floatReverse 10s ease-in-out infinite")
                }
            }) {}
            Div({
                style {
                    position(Position.Fixed)
                    property("bottom", "10%")
                    property("right", "20%")
                    width(180.px)
                    height(180.px)
                    borderRadius(50.percent)
                    property("z-index", "0")
                    property("pointer-events", "none")
                    property(
                        "background",
                        "radial-gradient(circle, rgba(14, 165, 233, 0.15) 0%, transparent 70%)"
                    )
                    property("animation", "float 14s ease-in-out infinite reverse")
                }
            }) {}
            Div({
                style {
                    position(Position.Fixed)
                    property("top", "-10%")
                    property("right", "-5%")
                    width(400.px)
                    height(400.px)
                    borderRadius(50.percent)
                    property("z-index", "0")
                    property("pointer-events", "none")
                    property(
                        "background",
                        "radial-gradient(circle, rgba(14, 165, 233, 0.2) 0%, transparent 60%)"
                    )
                    property("animation", "glowPulse 8s ease-in-out infinite")
                }
            }) {}

            // Login card — two-column layout so content fits without scrolling
            Div({
                style {
                    position(Position.Relative)
                    property("z-index", "1")
                    width(100.percent)
                    maxWidth(680.px)
                    margin(24.px)
                    padding(28.px)
                    display(DisplayStyle.Flex)
                    flexDirection(FlexDirection.Row)
                    flexWrap(FlexWrap.Wrap)
                    gap(32.px)
                    property("background", "rgba(255, 255, 255, 0.03)")
                    property("backdrop-filter", "blur(20px)")
                    property("-webkit-backdrop-filter", "blur(20px)")
                    property("border", "1px solid rgba(255, 255, 255, 0.08)")
                    borderRadius(16.px)
                    property(
                        "box-shadow",
                        "0 25px 50px -12px rgba(0, 0, 0, 0.5), 0 0 0 1px rgba(255,255,255,0.05)"
                    )
                    property("animation", "loginCardIn 0.5s cubic-bezier(0.16, 1, 0.3, 1) forwards")
                    property(
                        "font-family",
                        "'DM Sans', -apple-system, BlinkMacSystemFont, sans-serif"
                    )
                }
            }) {
                // Left column: form
                Div({
                    style {
                        flex(1)
                        property("min-width", "260px")
                    }
                }) {
                    // Top: Back to site (first element in card)
                    Button({
                        onClick {
                            if (AppConfig.showMarketingLanding) {
                                Router.navigateTo(Route.Home)
                            } else {
                                Router.navigateTo(Route.Dashboard)
                            }
                        }
                        style {
                            padding(8.px, 14.px)
                            backgroundColor(Color.transparent)
                            color(Color("rgba(255,255,255,0.8)"))
                            border(1.px, LineStyle.Solid, Color("rgba(255,255,255,0.3)"))
                            borderRadius(8.px)
                            cursor("pointer")
                            fontSize(13.px)
                            fontWeight(500)
                            display(DisplayStyle.Flex)
                            alignItems(AlignItems.Center)
                            gap(6.px)
                            marginBottom(20.px)
                            property("transition", "all 0.2s ease")
                        }
                        onMouseEnter {
                            (it.target as HTMLElement).style.backgroundColor =
                                "rgba(255,255,255,0.1)"
                            (it.target as HTMLElement).style.color = "white"
                        }
                        onMouseLeave {
                            (it.target as HTMLElement).style.backgroundColor = "transparent"
                            (it.target as HTMLElement).style.color = "rgba(255,255,255,0.8)"
                        }
                    }) {
                        Icon("arrow_back", size = 18.px, color = Color("rgba(255,255,255,0.8)"))
                        Text(LocalizedStrings.backToSite)
                    }

                    // Header: logo + title
                    Div({
                        style {
                            display(DisplayStyle.Flex)
                            alignItems(AlignItems.Center)
                            gap(12.px)
                            marginBottom(20.px)
                        }
                    }) {
                        Div({
                            style {
                                width(44.px)
                                height(44.px)
                                flexShrink(0)
                                borderRadius(12.px)
                                property(
                                    "background",
                                    "linear-gradient(135deg, ${FlagentTheme.Primary} 0%, ${FlagentTheme.Secondary} 100%)"
                                )
                                display(DisplayStyle.Flex)
                                alignItems(AlignItems.Center)
                                justifyContent(JustifyContent.Center)
                                property("box-shadow", "0 6px 16px rgba(14, 165, 233, 0.35)")
                            }
                        }) {
                            Icon("flag", size = 22.px, color = Color.white)
                        }
                        Div({
                            style {
                                flex(1)
                                property("min-width", "0")
                            }
                        }) {
                            H1({
                                style {
                                    fontSize(20.px)
                                    fontWeight(600)
                                    color(Color.white)
                                    margin(0.px)
                                    property("letter-spacing", "-0.02em")
                                }
                            }) {
                                Text(LocalizedStrings.welcomeToFlagent)
                            }
                            P({
                                style {
                                    fontSize(13.px)
                                    color(Color("rgba(255,255,255,0.6)"))
                                    margin(4.px, 0.px, 0.px, 0.px)
                                }
                            }) {
                                Text(LocalizedStrings.signInToManage)
                            }
                        }
                    }

                    // Email input
                    Div(attrs = { style { marginBottom(14.px) } }) {
                        Label(attrs = {
                            style {
                                display(DisplayStyle.Block)
                                fontSize(13.px)
                                fontWeight(500)
                                color(Color("rgba(255,255,255,0.8)"))
                                marginBottom(8.px)
                            }
                        }) {
                            Text(LocalizedStrings.emailLabel)
                        }
                        Input(InputType.Email) {
                            value(email.value)
                            onInput { e -> email.value = e.target.value }
                            attr("placeholder", "you@example.com")
                            style {
                                width(100.percent)
                                padding(14.px, 16.px)
                                property("background", "rgba(255,255,255,0.06)")
                                property("border", "1px solid rgba(255,255,255,0.12)")
                                borderRadius(10.px)
                                fontSize(15.px)
                                color(Color.white)
                                property("box-sizing", "border-box")
                                property("transition", "all 0.2s ease")
                            }
                            onFocus {
                                (it.target as HTMLElement).style.setProperty(
                                    "border-color",
                                    "rgba(14, 165, 233, 0.6)"
                                )
                                (it.target as HTMLElement).style.setProperty(
                                    "box-shadow",
                                    "0 0 0 3px rgba(14, 165, 233, 0.2)"
                                )
                            }
                            onBlur {
                                (it.target as HTMLElement).style.setProperty(
                                    "border-color",
                                    "rgba(255,255,255,0.12)"
                                )
                                (it.target as HTMLElement).style.setProperty("box-shadow", "none")
                            }
                        }
                    }

                    // Password input with visibility toggle
                    Div(attrs = { style { marginBottom(14.px) } }) {
                        Label(attrs = {
                            style {
                                display(DisplayStyle.Block)
                                fontSize(13.px)
                                fontWeight(500)
                                color(Color("rgba(255,255,255,0.8)"))
                                marginBottom(8.px)
                            }
                        }) {
                            Text(LocalizedStrings.passwordLabel)
                        }
                        Div(attrs = {
                            style {
                                position(Position.Relative)
                            }
                        }) {
                            Input(if (passwordVisible.value) InputType.Text else InputType.Password) {
                                value(password.value)
                                onInput { e -> password.value = e.target.value }
                                attr("placeholder", "••••••••")
                                style {
                                    width(100.percent)
                                    padding(14.px, 44.px, 14.px, 16.px)
                                    property("background", "rgba(255,255,255,0.06)")
                                    property("border", "1px solid rgba(255,255,255,0.12)")
                                    borderRadius(10.px)
                                    fontSize(15.px)
                                    color(Color.white)
                                    property("box-sizing", "border-box")
                                    property("transition", "all 0.2s ease")
                                }
                                onFocus {
                                    (it.target as HTMLElement).style.setProperty(
                                        "border-color",
                                        "rgba(14, 165, 233, 0.6)"
                                    )
                                    (it.target as HTMLElement).style.setProperty(
                                        "box-shadow",
                                        "0 0 0 3px rgba(14, 165, 233, 0.2)"
                                    )
                                }
                                onBlur {
                                    (it.target as HTMLElement).style.setProperty(
                                        "border-color",
                                        "rgba(255,255,255,0.12)"
                                    )
                                    (it.target as HTMLElement).style.setProperty(
                                        "box-shadow",
                                        "none"
                                    )
                                }
                            }
                            Button(attrs = {
                                style {
                                    position(Position.Absolute)
                                    property("top", "50%")
                                    property("right", "12px")
                                    property("transform", "translateY(-50%)")
                                    padding(4.px)
                                    backgroundColor(Color.transparent)
                                    color(Color("rgba(255,255,255,0.6)"))
                                    border(0.px)
                                    cursor("pointer")
                                    display(DisplayStyle.Flex)
                                    alignItems(AlignItems.Center)
                                    justifyContent(JustifyContent.Center)
                                }
                                onClick { passwordVisible.value = !passwordVisible.value }
                                onMouseEnter {
                                    (it.target as HTMLElement).style.color = "white"
                                }
                                onMouseLeave {
                                    (it.target as HTMLElement).style.color = "rgba(255,255,255,0.6)"
                                }
                            }) {
                                Icon(
                                    if (passwordVisible.value) "visibility_off" else "visibility",
                                    size = 20.px,
                                    color = Color("rgba(255,255,255,0.6)")
                                )
                            }
                        }
                    }

                    // Remember me
                    Div(attrs = {
                        style {
                            display(DisplayStyle.Flex)
                            alignItems(AlignItems.Center)
                            marginBottom(16.px)
                            gap(8.px)
                        }
                    }) {
                        Input(InputType.Checkbox) {
                            checked(rememberMe.value)
                            onInput { rememberMe.value = it.target.checked }
                            style {
                                width(18.px)
                                height(18.px)
                                cursor("pointer")
                            }
                        }
                        Label(attrs = {
                            style {
                                fontSize(14.px)
                                color(Color("rgba(255,255,255,0.7)"))
                                cursor("pointer")
                            }
                            onClick { rememberMe.value = !rememberMe.value }
                        }) {
                            Text(LocalizedStrings.rememberMeLabel)
                        }
                    }

                    Button({
                        onClick {
                            viewModel.login(email.value, password.value) {
                                onSuccess()
                            }
                        }
                        if (viewModel.isLoading) {
                            attr("disabled", "")
                        }
                        style {
                            width(100.percent)
                            padding(14.px)
                            property(
                                "background",
                                "linear-gradient(135deg, ${FlagentTheme.Primary} 0%, ${FlagentTheme.PrimaryDark} 100%)"
                            )
                            color(Color.white)
                            border(0.px)
                            borderRadius(10.px)
                            cursor(if (viewModel.isLoading) "not-allowed" else "pointer")
                            fontSize(15.px)
                            fontWeight(600)
                            property("box-shadow", "0 4px 14px rgba(14, 165, 233, 0.4)")
                            property("transition", "all 0.2s ease")
                        }
                        if (!viewModel.isLoading) {
                            onMouseEnter {
                                val el = it.target as HTMLElement
                                el.style.transform = "translateY(-1px)"
                                el.style.setProperty(
                                    "box-shadow",
                                    "0 6px 20px rgba(14, 165, 233, 0.5)"
                                )
                            }
                            onMouseLeave {
                                val el = it.target as HTMLElement
                                el.style.transform = "translateY(0)"
                                el.style.setProperty(
                                    "box-shadow",
                                    "0 4px 14px rgba(14, 165, 233, 0.4)"
                                )
                            }
                        }
                    }) {
                        Text(if (viewModel.isLoading) LocalizedStrings.signingIn else LocalizedStrings.signIn)
                    }

                    viewModel.error?.let { error ->
                        Div({
                            style {
                                marginTop(12.px)
                                padding(12.px)
                                backgroundColor(FlagentTheme.errorBg(themeMode))
                                property("border", "1px solid var(--flagent-color-error)")
                                borderRadius(8.px)
                                color(FlagentTheme.errorText(themeMode))
                                fontSize(13.px)
                            }
                        }) {
                            Text(error)
                        }
                    }
                }

                // Right column: links and SSO (horizontal grouping)
                Div({
                    style {
                        display(DisplayStyle.Flex)
                        flexDirection(FlexDirection.Column)
                        gap(16.px)
                        property("min-width", "180px")
                    }
                }) {
                    // Links section: support, GitHub, documentation (compact)
                    Div({
                        style {
                            paddingTop(4.px)
                            property("border-left", "1px solid rgba(255,255,255,0.08)")
                            paddingLeft(20.px)
                            display(DisplayStyle.Flex)
                            flexDirection(FlexDirection.Column)
                            gap(10.px)
                        }
                    }) {
                        A(href = "mailto:${AppConfig.supportEmail}", attrs = {
                            attr("target", "_blank")
                            attr("rel", "noopener noreferrer")
                            style {
                                display(DisplayStyle.Flex)
                                alignItems(AlignItems.Center)
                                gap(8.px)
                                color(Color("rgba(255,255,255,0.6)"))
                                textDecoration("none")
                                fontSize(13.px)
                                property("transition", "color 0.2s ease")
                            }
                            onMouseEnter {
                                (it.target as HTMLElement).style.color = "rgba(255,255,255,0.9)"
                            }
                            onMouseLeave {
                                (it.target as HTMLElement).style.color = "rgba(255,255,255,0.6)"
                            }
                        }) {
                            Icon("mail", size = 18.px, color = Color("rgba(255,255,255,0.6)"))
                            Text(LocalizedStrings.supportLink)
                        }
                        A(href = AppConfig.githubUrl, attrs = {
                            attr("target", "_blank")
                            attr("rel", "noopener noreferrer")
                            style {
                                display(DisplayStyle.Flex)
                                alignItems(AlignItems.Center)
                                gap(8.px)
                                color(Color("rgba(255,255,255,0.6)"))
                                textDecoration("none")
                                fontSize(13.px)
                                property("transition", "color 0.2s ease")
                            }
                            onMouseEnter {
                                (it.target as HTMLElement).style.color = "rgba(255,255,255,0.9)"
                            }
                            onMouseLeave {
                                (it.target as HTMLElement).style.color = "rgba(255,255,255,0.6)"
                            }
                        }) {
                            Icon("code", size = 18.px, color = Color("rgba(255,255,255,0.6)"))
                            Text(LocalizedStrings.questionsAndGuides)
                        }
                        A(href = AppConfig.docsUrl, attrs = {
                            attr("target", "_blank")
                            attr("rel", "noopener noreferrer")
                            style {
                                display(DisplayStyle.Flex)
                                alignItems(AlignItems.Center)
                                gap(8.px)
                                color(Color("rgba(255,255,255,0.6)"))
                                textDecoration("none")
                                fontSize(13.px)
                                property("transition", "color 0.2s ease")
                            }
                            onMouseEnter {
                                (it.target as HTMLElement).style.color = "rgba(255,255,255,0.9)"
                            }
                            onMouseLeave {
                                (it.target as HTMLElement).style.color = "rgba(255,255,255,0.6)"
                            }
                        }) {
                            Icon("menu_book", size = 18.px, color = Color("rgba(255,255,255,0.6)"))
                            Text(LocalizedStrings.documentation)
                        }
                    }

                    if (AppConfig.Features.enableSso) {
                        Div({
                            style {
                                paddingTop(12.px)
                                property("border-top", "1px solid rgba(255,255,255,0.08)")
                            }
                        }) {
                            Div(attrs = {
                                style {
                                    display(DisplayStyle.Block)
                                    fontSize(12.px)
                                    color(Color("rgba(255,255,255,0.5)"))
                                    marginBottom(12.px)
                                    textAlign("center")
                                }
                            }) {
                                Text(LocalizedStrings.orSignInWithSso)
                            }
                            Button({
                                onClick {
                                    val base = AppConfig.apiBaseUrl
                                    val url =
                                        if (base.isEmpty() || base == "http://localhost" || base == "https://localhost") {
                                            "${window.location.origin}/sso/login/1"
                                        } else {
                                            "$base/sso/login/1"
                                        }
                                    window.location.href = url
                                }
                                style {
                                    width(100.percent)
                                    padding(12.px, 16.px)
                                    backgroundColor(Color.transparent)
                                    color(FlagentTheme.PrimaryLight)
                                    border(1.px, LineStyle.Solid, Color("rgba(14, 165, 233, 0.5)"))
                                    borderRadius(10.px)
                                    cursor("pointer")
                                    fontSize(14.px)
                                    fontWeight(500)
                                    display(DisplayStyle.Flex)
                                    alignItems(AlignItems.Center)
                                    justifyContent(JustifyContent.Center)
                                    gap(8.px)
                                    property("transition", "all 0.2s ease")
                                }
                                onMouseEnter {
                                    val el = it.target as HTMLElement
                                    el.style.backgroundColor = "rgba(14, 165, 233, 0.15)"
                                }
                                onMouseLeave {
                                    (it.target as HTMLElement).style.backgroundColor = "transparent"
                                }
                            }) {
                                Icon("login", size = 18.px, color = FlagentTheme.PrimaryLight)
                                Text(LocalizedStrings.loginWithSso)
                            }
                        }
                        // Optional Google OAuth for public SaaS: sign in to specific tenant by key from URL
                        if (tenantKeyFromUrl.isNotBlank()) {
                            Div({
                                style {
                                    paddingTop(10.px)
                                }
                            }) {
                                Button({
                                    onClick {
                                        val base = AppConfig.apiBaseUrl
                                        // tenantKey is already validated on backend (^[a-z0-9-]+$), no escaping needed
                                        val path = "/public/oauth/google/login?state=$tenantKeyFromUrl"
                                        val url =
                                            if (base.isEmpty() || base == "http://localhost" || base == "https://localhost") {
                                                "${window.location.origin}$path"
                                            } else {
                                                "$base$path"
                                            }
                                        window.location.href = url
                                    }
                                    style {
                                        width(100.percent)
                                        padding(12.px, 16.px)
                                        backgroundColor(Color("rgba(15,23,42,0.85)"))
                                        color(Color("rgba(248,250,252,0.95)"))
                                        border(1.px, LineStyle.Solid, Color("rgba(148,163,184,0.8)"))
                                        borderRadius(10.px)
                                        cursor("pointer")
                                        fontSize(14.px)
                                        fontWeight(500)
                                        display(DisplayStyle.Flex)
                                        alignItems(AlignItems.Center)
                                        justifyContent(JustifyContent.Center)
                                        gap(8.px)
                                        property("transition", "all 0.2s ease")
                                    }
                                    onMouseEnter {
                                        val el = it.target as HTMLElement
                                        el.style.backgroundColor = "rgba(15,23,42,1)"
                                    }
                                    onMouseLeave {
                                        (it.target as HTMLElement).style.backgroundColor = "rgba(15,23,42,0.85)"
                                    }
                                }) {
                                    Icon("login", size = 18.px, color = Color("rgba(248,250,252,0.95)"))
                                    Text("Sign in with Google")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
