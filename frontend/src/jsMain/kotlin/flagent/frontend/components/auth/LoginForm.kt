package flagent.frontend.components.auth

import androidx.compose.runtime.*
import flagent.frontend.components.Icon
import flagent.frontend.config.AppConfig
import flagent.frontend.theme.FlagentTheme
import flagent.frontend.viewmodel.AuthViewModel
import kotlinx.browser.window
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

/**
 * Login Form with animated background, decorative shapes, and glass-morphism card.
 */
@Composable
fun LoginForm(
    viewModel: AuthViewModel,
    onSuccess: () -> Unit
) {
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val passwordVisible = remember { mutableStateOf(false) }
    val rememberMe = remember { mutableStateOf(false) }

    Div({
        classes("login-page")
        style {
            position(Position.Relative)
            display(DisplayStyle.Flex)
            justifyContent(JustifyContent.Center)
            alignItems(AlignItems.Center)
            minHeight(100.vh)
            overflow("hidden")
            property(
                "background",
                "linear-gradient(135deg, #0f172a 0%, #1e293b 25%, #0f172a 50%, #1e3a5f 75%, #0f172a 100%)"
            )
            property("background-size", "400% 400%")
            property("animation", "morphGradient 15s ease infinite")
        }
    }) {
        // Dot grid pattern overlay
        Div({
            style {
                position(Position.Absolute)
                property("inset", "0")
                property(
                    "background-image",
                    "radial-gradient(rgba(14, 165, 233, 0.15) 1px, transparent 1px)"
                )
                property("background-size", "32px 32px")
                property("pointer-events", "none")
            }
        }) {}

        // Floating decorative blobs
        Div({
            style {
                position(Position.Absolute)
                property("top", "10%")
                property("right", "15%")
                width(320.px)
                height(320.px)
                borderRadius(50.percent)
                property("background", "radial-gradient(circle, rgba(14, 165, 233, 0.25) 0%, transparent 70%)")
                property("animation", "float 12s ease-in-out infinite")
                property("pointer-events", "none")
            }
        }) {}
        Div({
            style {
                position(Position.Absolute)
                property("bottom", "20%")
                property("left", "10%")
                width(280.px)
                height(280.px)
                borderRadius(50.percent)
                property("background", "radial-gradient(circle, rgba(20, 184, 166, 0.2) 0%, transparent 70%)")
                property("animation", "floatSlow 18s ease-in-out infinite")
                property("pointer-events", "none")
            }
        }) {}
        Div({
            style {
                position(Position.Absolute)
                property("top", "50%")
                property("left", "5%")
                width(120.px)
                height(120.px)
                borderRadius(50.percent)
                property("background", "radial-gradient(circle, rgba(56, 189, 248, 0.2) 0%, transparent 70%)")
                property("animation", "floatReverse 10s ease-in-out infinite")
                property("pointer-events", "none")
            }
        }) {}
        Div({
            style {
                position(Position.Absolute)
                property("bottom", "10%")
                property("right", "20%")
                width(180.px)
                height(180.px)
                borderRadius(50.percent)
                property("background", "radial-gradient(circle, rgba(14, 165, 233, 0.15) 0%, transparent 70%)")
                property("animation", "float 14s ease-in-out infinite reverse")
                property("pointer-events", "none")
            }
        }) {}

        // Glow orbs
        Div({
            style {
                position(Position.Absolute)
                property("top", "-10%")
                property("right", "-5%")
                width(400.px)
                height(400.px)
                borderRadius(50.percent)
                property("background", "radial-gradient(circle, rgba(14, 165, 233, 0.2) 0%, transparent 60%)")
                property("animation", "glowPulse 8s ease-in-out infinite")
                property("pointer-events", "none")
            }
        }) {}

        // Login card — two-column layout so content fits without scrolling
        Div({
            style {
                position(Position.Relative)
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
                property("box-shadow", "0 25px 50px -12px rgba(0, 0, 0, 0.5), 0 0 0 1px rgba(255,255,255,0.05)")
                property("animation", "loginCardIn 0.5s cubic-bezier(0.16, 1, 0.3, 1) forwards")
                property("font-family", "'DM Sans', -apple-system, BlinkMacSystemFont, sans-serif")
            }
        }) {
            // Left column: form
            Div({
                style {
                    flex(1)
                    property("min-width", "260px")
                }
            }) {
                // Logo / title area (compact)
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
                            Text(flagent.frontend.i18n.LocalizedStrings.welcomeToFlagent)
                        }
                        P({
                            style {
                                fontSize(13.px)
                                color(Color("rgba(255,255,255,0.6)"))
                                margin(4.px, 0.px, 0.px, 0.px)
                            }
                        }) {
                            Text(flagent.frontend.i18n.LocalizedStrings.signInToManage)
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
                    Text(flagent.frontend.i18n.LocalizedStrings.emailLabel)
                }
                Input(InputType.Email) {
                    value(email.value)
                    onInput { e -> email.value = (e.target as? org.w3c.dom.HTMLInputElement)?.value ?: "" }
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
                        (it.target as org.w3c.dom.HTMLElement).style.setProperty("border-color", "rgba(14, 165, 233, 0.6)")
                        (it.target as org.w3c.dom.HTMLElement).style.setProperty("box-shadow", "0 0 0 3px rgba(14, 165, 233, 0.2)")
                    }
                    onBlur {
                        (it.target as org.w3c.dom.HTMLElement).style.setProperty("border-color", "rgba(255,255,255,0.12)")
                        (it.target as org.w3c.dom.HTMLElement).style.setProperty("box-shadow", "none")
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
                    Text(flagent.frontend.i18n.LocalizedStrings.passwordLabel)
                }
                Div(attrs = {
                    style {
                        position(Position.Relative)
                    }
                }) {
                    Input(if (passwordVisible.value) InputType.Text else InputType.Password) {
                        value(password.value)
                        onInput { e -> password.value = (e.target as? org.w3c.dom.HTMLInputElement)?.value ?: "" }
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
                            (it.target as org.w3c.dom.HTMLElement).style.setProperty("border-color", "rgba(14, 165, 233, 0.6)")
                            (it.target as org.w3c.dom.HTMLElement).style.setProperty("box-shadow", "0 0 0 3px rgba(14, 165, 233, 0.2)")
                        }
                        onBlur {
                            (it.target as org.w3c.dom.HTMLElement).style.setProperty("border-color", "rgba(255,255,255,0.12)")
                            (it.target as org.w3c.dom.HTMLElement).style.setProperty("box-shadow", "none")
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
                            (it.target as org.w3c.dom.HTMLElement).style.color = "white"
                        }
                        onMouseLeave {
                            (it.target as org.w3c.dom.HTMLElement).style.color = "rgba(255,255,255,0.6)"
                        }
                    }) {
                        Icon(if (passwordVisible.value) "visibility_off" else "visibility", size = 20.px, color = Color("rgba(255,255,255,0.6)"))
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
                    onInput { rememberMe.value = (it.target as? org.w3c.dom.HTMLInputElement)?.checked ?: false }
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
                    Text(flagent.frontend.i18n.LocalizedStrings.rememberMeLabel)
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
                        val el = it.target as org.w3c.dom.HTMLElement
                        el.style.transform = "translateY(-1px)"
                        el.style.setProperty("box-shadow", "0 6px 20px rgba(14, 165, 233, 0.5)")
                    }
                    onMouseLeave {
                        val el = it.target as org.w3c.dom.HTMLElement
                        el.style.transform = "translateY(0)"
                        el.style.setProperty("box-shadow", "0 4px 14px rgba(14, 165, 233, 0.4)")
                    }
                }
            }) {
                Text(if (viewModel.isLoading) flagent.frontend.i18n.LocalizedStrings.signingIn else flagent.frontend.i18n.LocalizedStrings.signIn)
            }

            viewModel.error?.let { error ->
                Div({
                    style {
                        marginTop(12.px)
                        padding(12.px)
                        backgroundColor(Color("rgba(239, 68, 68, 0.15)"))
                        property("border", "1px solid rgba(239, 68, 68, 0.3)")
                        borderRadius(8.px)
                        color(Color("#FCA5A5"))
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
                        (it.target as org.w3c.dom.HTMLElement).style.color = "rgba(255,255,255,0.9)"
                    }
                    onMouseLeave {
                        (it.target as org.w3c.dom.HTMLElement).style.color = "rgba(255,255,255,0.6)"
                    }
                }) {
                    Icon("mail", size = 18.px, color = Color("rgba(255,255,255,0.6)"))
                    Text(flagent.frontend.i18n.LocalizedStrings.supportLink)
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
                        (it.target as org.w3c.dom.HTMLElement).style.color = "rgba(255,255,255,0.9)"
                    }
                    onMouseLeave {
                        (it.target as org.w3c.dom.HTMLElement).style.color = "rgba(255,255,255,0.6)"
                    }
                }) {
                    Icon("code", size = 18.px, color = Color("rgba(255,255,255,0.6)"))
                    Text(flagent.frontend.i18n.LocalizedStrings.questionsAndGuides)
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
                        (it.target as org.w3c.dom.HTMLElement).style.color = "rgba(255,255,255,0.9)"
                    }
                    onMouseLeave {
                        (it.target as org.w3c.dom.HTMLElement).style.color = "rgba(255,255,255,0.6)"
                    }
                }) {
                    Icon("menu_book", size = 18.px, color = Color("rgba(255,255,255,0.6)"))
                    Text(flagent.frontend.i18n.LocalizedStrings.documentation)
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
                        Text(flagent.frontend.i18n.LocalizedStrings.orSignInWithSso)
                    }
                    Button({
                        onClick {
                            val base = AppConfig.apiBaseUrl
                            val url = if (base.isEmpty() || base == "http://localhost" || base == "https://localhost") {
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
                            val el = it.target as org.w3c.dom.HTMLElement
                            el.style.backgroundColor = "rgba(14, 165, 233, 0.15)"
                        }
                        onMouseLeave {
                            (it.target as org.w3c.dom.HTMLElement).style.backgroundColor = "transparent"
                        }
                    }) {
                        Icon("login", size = 18.px, color = FlagentTheme.PrimaryLight)
                        Text(flagent.frontend.i18n.LocalizedStrings.loginWithSso)
                    }
                }
            }
            }
        }
    }
}
