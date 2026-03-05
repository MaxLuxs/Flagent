package flagent.frontend.components.landing

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import flagent.frontend.components.Icon
import flagent.frontend.api.ApiClient
import flagent.frontend.api.PublicSignupRequest
import flagent.frontend.config.AppConfig
import flagent.frontend.navigation.Route
import flagent.frontend.navigation.Router
import flagent.frontend.state.LocalThemeMode
import flagent.frontend.state.ThemeMode
import flagent.frontend.theme.AppGradientBackground
import flagent.frontend.theme.FlagentTheme
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.dom.*

/**
 * Full marketing landing: navbar, hero, features, use cases, footer.
 * Shown at / when showMarketingLanding is true (SaaS).
 */
@Composable
fun MarketingLanding() {
    val themeMode = LocalThemeMode.current
    LandingNavbar()
    AppGradientBackground(mode = themeMode) {
        Div(attrs = {
            classes("landing-page")
            style {
                position(Position.Relative)
                minHeight(100.vh)
                overflow("hidden")
                paddingTop(80.px)
                property("font-family", "'DM Sans', -apple-system, BlinkMacSystemFont, sans-serif")
            }
        }) {
            LandingBackgroundShapes(themeMode)
            Div(attrs = {
                classes("landing-content")
                style {
                    position(Position.Relative)
                    padding(48.px, 24.px)
                    maxWidth(1100.px)
                    property("margin", "0 auto")
                    property("width", "100%")
                    property("box-sizing", "border-box")
                    property("z-index", "1")
                }
            }) {
            Div(attrs = {
                id("hero-section")
                classes("reveal-on-scroll")
                attr("data-reveal", "true")
            }) {
                HeroSection()
            }
            Div(attrs = {
                id("start-free-trial")
                classes("reveal-on-scroll")
                attr("data-reveal", "true")
            }) {
                StartFreeTrialSection()
            }
            Div(attrs = {
                classes("reveal-on-scroll")
                attr("data-reveal", "true")
            }) {
                CodeSnippetSection()
            }
            Div(attrs = {
                classes("reveal-on-scroll")
                attr("data-reveal", "true")
            }) {
                FeaturesSection()
            }
            Div(attrs = {
                classes("reveal-on-scroll")
                attr("data-reveal", "true")
            }) {
                RoadmapSection()
            }
            Div(attrs = {
                classes("reveal-on-scroll")
                attr("data-reveal", "true")
            }) {
                TrustSection()
            }
            Div(attrs = {
                classes("reveal-on-scroll")
                attr("data-reveal", "true")
            }) {
                SdksSection()
            }
            Div(attrs = {
                classes("reveal-on-scroll")
                attr("data-reveal", "true")
            }) {
                UseCasesSection()
            }
            Div(attrs = {
                classes("reveal-on-scroll")
                attr("data-reveal", "true")
            }) {
                QuickFactsSection()
            }
            Div(attrs = {
                classes("reveal-on-scroll")
                attr("data-reveal", "true")
                style { marginTop(64.px) }
            }) {
                EnterpriseSection()
            }
            }
            Div(attrs = {
                style { position(Position.Relative); property("z-index", "1") }
                classes("reveal-on-scroll")
                attr("data-reveal", "true")
            }) {
                LandingFooter()
            }
        }
    }
}

@Composable
fun LandingBackgroundShapes(themeMode: ThemeMode = ThemeMode.Dark) {
    val isLight = themeMode == ThemeMode.Light
    val dotOpacity = if (isLight) 0.06 else 0.12
    val blob1 = if (isLight) "rgba(14, 165, 233, 0.09)" else "rgba(14, 165, 233, 0.2)"
    val blob2 = if (isLight) "rgba(20, 184, 166, 0.08)" else "rgba(20, 184, 166, 0.18)"
    val blob3 = if (isLight) "rgba(14, 165, 233, 0.07)" else "rgba(14, 165, 233, 0.15)"
    Div(attrs = {
        style {
            position(Position.Fixed)
            property("inset", "0")
            property("z-index", "0")
            property("pointer-events", "none")
            property("background-image", "radial-gradient(rgba(14, 165, 233, $dotOpacity) 1px, transparent 1px)")
            property("background-size", "28px 28px")
        }
    }) {}
    Div(attrs = {
        style {
            position(Position.Fixed)
            property("top", "-8%")
            property("right", "-3%")
            width(450.px)
            height(450.px)
            borderRadius(50.percent)
            property("z-index", "0")
            property("pointer-events", "none")
            property("background", "radial-gradient(circle, $blob1 0%, transparent 65%)")
            property("animation", "float 14s ease-in-out infinite")
        }
    }) {}
    Div(attrs = {
        style {
            position(Position.Fixed)
            property("bottom", "-12%")
            property("left", "-6%")
            width(500.px)
            height(500.px)
            borderRadius(50.percent)
            property("z-index", "0")
            property("pointer-events", "none")
            property("background", "radial-gradient(circle, $blob2 0%, transparent 65%)")
            property("animation", "floatSlow 20s ease-in-out infinite")
        }
    }) {}
    Div(attrs = {
        style {
            position(Position.Fixed)
            property("top", "-15%")
            property("left", "50%")
            property("transform", "translateX(-50%)")
            width(600.px)
            height(400.px)
            borderRadius(50.percent)
            property("z-index", "0")
            property("pointer-events", "none")
            property("background", "radial-gradient(ellipse, $blob3 0%, transparent 70%)")
            property("animation", "glowPulse 10s ease-in-out infinite")
        }
    }) {}
}

@Composable
private fun HeroSection() {
    Div(attrs = {
        classes("hero-content")
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
            classes("hero-title")
            style {
                fontSize(52.px)
                fontWeight(700)
                color(Color.white)
                marginBottom(12.px)
                property("letter-spacing", "-0.03em")
                property("line-height", "1.15")
            }
        }) { Text("Flagent — feature flags & A/B tests in one place") }
        P(attrs = {
            style {
                fontSize(16.px)
                color(FlagentTheme.PrimaryLight)
                fontWeight(600)
                marginBottom(16.px)
                property("letter-spacing", "0.02em")
            }
        }) { Text("The only feature-flag platform built on Kotlin Multiplatform. Self-host free, upgrade when you need it.") }
        P(attrs = {
            classes("hero-subtitle")
            style {
                fontSize(20.px)
                color(Color("rgba(255,255,255,0.7)"))
                lineHeight("1.65")
                maxWidth(560.px)
                property("margin", "0 auto 40px")
            }
        }) {
            Text("Toggle features, run experiments, and roll back in one click — without redeploying. Type-safe SDKs for Kotlin, JS, Swift, Python, Go.")
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
                        el.style.transform = "translateY(-3px) scale(1.02)"
                        el.style.setProperty("box-shadow", "0 12px 32px rgba(14, 165, 233, 0.55)")
                    }
                    onMouseLeave {
                        val el = it.target as org.w3c.dom.HTMLElement
                        el.style.transform = "translateY(0) scale(1)"
                        el.style.setProperty("box-shadow", "0 8px 24px rgba(14, 165, 233, 0.45)")
                    }
                }) {
                    Icon("login", size = 20.px, color = Color.white)
                    Text(" ")
                    Text("Sign in")
                }
            }
            A(href = AppConfig.gettingStartedUrl, attrs = {
                attr("target", "_blank")
                attr("rel", "noopener noreferrer")
                style {
                    display(DisplayStyle.Flex)
                    alignItems(AlignItems.Center)
                    justifyContent(JustifyContent.Center)
                    gap(8.px)
                    padding(18.px, 36.px)
                    property(
                        "background",
                        "linear-gradient(135deg, ${FlagentTheme.Primary} 0%, ${FlagentTheme.PrimaryDark} 100%)"
                    )
                    color(Color.white)
                    textDecoration("none")
                    border(0.px)
                    borderRadius(12.px)
                    fontSize(16.px)
                    fontWeight(600)
                    property("box-shadow", "0 8px 24px rgba(14, 165, 233, 0.45)")
                    property("transition", "all 0.25s cubic-bezier(0.4, 0, 0.2, 1)")
                }
                onMouseEnter {
                    val el = it.target as org.w3c.dom.HTMLElement
                    el.style.transform = "translateY(-3px) scale(1.02)"
                    el.style.setProperty("box-shadow", "0 12px 32px rgba(14, 165, 233, 0.55)")
                }
                onMouseLeave {
                    val el = it.target as org.w3c.dom.HTMLElement
                    el.style.transform = "translateY(0) scale(1)"
                    el.style.setProperty("box-shadow", "0 8px 24px rgba(14, 165, 233, 0.45)")
                }
            }) {
                Icon("rocket_launch", size = 20.px, color = Color.white)
                Text("Start free (self-host)")
            }
            A(href = "mailto:${AppConfig.enterpriseContactEmail}?subject=Flagent%20Enterprise%20%2F%20Request%20demo", attrs = {
                style {
                    display(DisplayStyle.Flex)
                    alignItems(AlignItems.Center)
                    justifyContent(JustifyContent.Center)
                    gap(8.px)
                    padding(18.px, 36.px)
                    backgroundColor(Color.transparent)
                    color(FlagentTheme.PrimaryLight)
                    border(2.px, LineStyle.Solid, Color("rgba(14, 165, 233, 0.6)"))
                    borderRadius(12.px)
                    textDecoration("none")
                    fontSize(16.px)
                    fontWeight(600)
                    property("transition", "all 0.25s ease")
                }
                onMouseEnter {
                    val el = it.target as org.w3c.dom.HTMLElement
                    el.style.backgroundColor = "rgba(14, 165, 233, 0.15)"
                    el.style.borderColor = FlagentTheme.PrimaryLight.toString()
                    el.style.transform = "translateY(-2px) scale(1.02)"
                }
                onMouseLeave {
                    val el = it.target as org.w3c.dom.HTMLElement
                    el.style.backgroundColor = "transparent"
                    el.style.borderColor = "rgba(14, 165, 233, 0.6)"
                    el.style.transform = "translateY(0) scale(1)"
                }
            }) {
                Icon("business_center", size = 20.px, color = FlagentTheme.PrimaryLight)
                Text("Request demo")
            }
            if (!AppConfig.requiresAuth) {
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
                        el.style.transform = "translateY(-2px) scale(1.02)"
                    }
                    onMouseLeave {
                        val el = it.target as org.w3c.dom.HTMLElement
                        el.style.backgroundColor = "transparent"
                        el.style.borderColor = "rgba(14, 165, 233, 0.6)"
                        el.style.transform = "translateY(0) scale(1)"
                    }
                }) {
                    Text("Get Started")
                }
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
        P(attrs = {
            style {
                marginTop(28.px)
                fontSize(14.px)
                color(Color("rgba(255,255,255,0.55)"))
            }
        }) {
            Text("Built for Kotlin and multiplatform teams. ")
            A(href = AppConfig.docsUrl, attrs = {
                attr("target", "_blank")
                attr("rel", "noopener noreferrer")
                style {
                    color(Color("rgba(255,255,255,0.7)"))
                    textDecoration("none")
                }
            }) {
                Text("Open source · Docs")
            }
        }
    }
}

@Composable
private fun StartFreeTrialSection() {
    val scope = rememberCoroutineScope()
    var companyName by remember { mutableStateOf("") }
    var ownerEmail by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var signupMessage by remember { mutableStateOf<String?>(null) }
    var signupError by remember { mutableStateOf<String?>(null) }

    Div(attrs = {
        classes("hero-mockup")
        style {
            marginTop(32.px)
            property("margin-left", "auto")
            property("margin-right", "auto")
            maxWidth(720.px)
            property("border-radius", "18px")
            property("border", "1px solid rgba(148,163,184,0.4)")
            property("box-shadow", "0 24px 48px rgba(15,23,42,0.8)")
            padding(28.px)
            backgroundColor(Color("rgba(15,23,42,0.96)"))
        }
    }) {
        H2(attrs = {
            style {
                fontSize(24.px)
                fontWeight(600)
                color(Color.white)
                marginBottom(8.px)
            }
        }) {
            Text("Start free trial")
        }
        P(attrs = {
            style {
                fontSize(14.px)
                color(Color("rgba(226,232,240,0.85)"))
                marginBottom(20.px)
            }
        }) {
            Text("Spin up a tenant with magic-link login for the owner. No credit card required.")
        }
        Div(attrs = {
            style {
                display(DisplayStyle.Flex)
                flexWrap(FlexWrap.Wrap)
                gap(12.px)
                justifyContent(JustifyContent.Center)
                marginBottom(16.px)
            }
        }) {
            Input(InputType.Text, attrs = {
                value(companyName)
                onInput { companyName = it.target.value }
                style {
                    padding(12.px, 16.px)
                    minWidth(220.px)
                    maxWidth(260.px)
                    borderRadius(10.px)
                    border(0.px)
                    backgroundColor(Color("rgba(15,23,42,0.9)"))
                    color(Color.white)
                    fontSize(14.px)
                }
                attr("placeholder", "Company name")
            })
            Input(InputType.Email, attrs = {
                value(ownerEmail)
                onInput { ownerEmail = it.target.value }
                style {
                    padding(12.px, 16.px)
                    minWidth(220.px)
                    maxWidth(260.px)
                    borderRadius(10.px)
                    border(0.px)
                    backgroundColor(Color("rgba(15,23,42,0.9)"))
                    color(Color.white)
                    fontSize(14.px)
                }
                attr("placeholder", "Work email")
            })
        }
        Div(attrs = {
            style {
                display(DisplayStyle.Flex)
                flexWrap(FlexWrap.Wrap)
                gap(12.px)
                justifyContent(JustifyContent.Center)
                marginBottom(8.px)
            }
        }) {
            Button(attrs = {
                style {
                    padding(14.px, 32.px)
                    backgroundColor(Color.white)
                    color(FlagentTheme.Primary)
                    border(0.px)
                    borderRadius(999.px)
                    cursor("pointer")
                    fontSize(15.px)
                    fontWeight(600)
                    display(DisplayStyle.Flex)
                    alignItems(AlignItems.Center)
                    justifyContent(JustifyContent.Center)
                    gap(8.px)
                    property("box-shadow", "0 12px 30px rgba(15,23,42,0.6)")
                    property("transition", "all 0.2s ease")
                    opacity(if (isSubmitting) 0.7 else 1.0)
                }
                onClick {
                    if (companyName.isBlank() || ownerEmail.isBlank()) {
                        signupError = "Enter company name and work email"
                        signupMessage = null
                        return@onClick
                    }
                    scope.launch {
                        isSubmitting = true
                        signupError = null
                        try {
                            val slug = companyName.lowercase()
                                .replace(Regex("[^a-z0-9]+"), "-")
                                .trim('-')
                                .ifBlank { "tenant-${kotlin.js.Date().getTime().toLong()}" }
                            ApiClient.publicSignup(
                                PublicSignupRequest(
                                    key = slug,
                                    name = companyName,
                                    plan = null,
                                    ownerEmail = ownerEmail
                                )
                            )
                            signupMessage = "Check your email for a magic login link."
                        } catch (e: Throwable) {
                            val appError = flagent.frontend.util.ErrorHandler.handle(e)
                            signupError = flagent.frontend.util.ErrorHandler.getUserMessage(appError)
                            signupMessage = null
                        } finally {
                            isSubmitting = false
                        }
                    }
                }
            }) {
                Icon("rocket_launch", size = 18.px, color = FlagentTheme.Primary)
                Text(" ")
                Text("Start free trial")
            }
            Button(attrs = {
                style {
                    padding(14.px, 32.px)
                    backgroundColor(Color("rgba(15,23,42,0.9)"))
                    color(Color("rgba(248,250,252,0.95)"))
                    border(1.px, LineStyle.Solid, Color("rgba(148,163,184,0.8)"))
                    borderRadius(999.px)
                    cursor("pointer")
                    fontSize(14.px)
                    fontWeight(500)
                    display(DisplayStyle.Flex)
                    alignItems(AlignItems.Center)
                    justifyContent(JustifyContent.Center)
                    gap(8.px)
                    property("transition", "all 0.2s ease")
                }
                onClick {
                    if (companyName.isBlank()) {
                        signupError = "Enter company name"
                        signupMessage = null
                        return@onClick
                    }
                    val slug = companyName.lowercase()
                        .replace(Regex("[^a-z0-9]+"), "-")
                        .trim('-')
                        .ifBlank { "tenant-${kotlin.js.Date().getTime().toLong()}" }
                    val base = AppConfig.apiBaseUrl
                    val path = "/public/oauth/google/login?state=signup:$slug"
                    val url = if (base.isEmpty() || base == "http://localhost" || base == "https://localhost") {
                        "${window.location.origin}$path"
                    } else {
                        "$base$path"
                    }
                    window.location.href = url
                }
            }) {
                Icon("login", size = 18.px, color = Color("rgba(248,250,252,0.95)"))
                Text(" ")
                Text("Continue with Google")
            }
        }
        if (signupMessage != null || signupError != null) {
            P(attrs = {
                style {
                    marginTop(4.px)
                    fontSize(13.px)
                    color(if (signupError != null) Color("rgba(248,113,113,1)") else Color("rgba(187,247,208,1)"))
                }
            }) {
                Text(signupError ?: signupMessage ?: "")
            }
        }
    }
}

private data class CodeExample(val lang: String, val code: String)

@Composable
private fun CodeSnippetSection() {
    val examples = listOf(
        CodeExample("Kotlin", """val client = FlagentClient.create(baseUrl = "http://localhost:18000/api/v1")
val result = client.evaluate(flagKey = "new_checkout", entityID = "user123")
if (result.variantKey == "enabled") {
    NewCheckoutScreen()
} else {
    LegacyCheckout()
}"""),
        CodeExample("JavaScript", """import { FlagentClient } from '@flagent/client';
const client = new FlagentClient({ baseUrl: 'http://localhost:18000/api/v1' });
const result = await client.evaluate({ flagKey: 'new_checkout', entityID: 'user123' });
if (result.variantKey === 'enabled') NewCheckoutScreen();
else LegacyCheckout();"""),
        CodeExample("Swift", """let client = FlagentClient(baseURL: "http://localhost:18000/api/v1")
let result = try await client.evaluate(flagKey: "new_checkout", entityID: "user123")
if result.variantKey == "enabled" { NewCheckoutScreen() }
else { LegacyCheckout() }"""),
        CodeExample("Python", """from flagent import create_client
client = create_client("http://localhost:18000/api/v1")
result = await client.evaluate(flag_key="new_checkout", entity_id="user123")
if result.is_enabled(): NewCheckoutScreen()
else: LegacyCheckout()"""),
        CodeExample("Go", """result, _ := client.Evaluate(ctx, &flagent.EvaluationContext{
    FlagKey: flagent.StringPtr("new_checkout"), EntityID: flagent.StringPtr("user123"),
})
if result.IsEnabled() { NewCheckoutScreen() } else { LegacyCheckout() }"""),
        CodeExample("Flutter", """final client = Flagent.create(baseUrl: 'http://localhost:18000/api/v1');
final result = await client.evaluate(flagKey: 'new_checkout', entityID: 'user123');
if (result.variantKey == 'enabled') NewCheckoutScreen();
else LegacyCheckout();"""),
    )
    var selectedIndex by remember { mutableStateOf(0) }
    val selected = examples[selectedIndex]
    Div(attrs = {
        style { marginBottom(64.px) }
    }) {
        H2(attrs = {
            style {
                fontSize(22.px)
                fontWeight(600)
                color(Color.white)
                textAlign("center")
                marginBottom(12.px)
            }
        }) { Text("Integrate in minutes") }
        P(attrs = {
            style {
                fontSize(15.px)
                color(Color("rgba(255,255,255,0.6)"))
                textAlign("center")
                marginBottom(28.px)
            }
        }) { Text("Same API across Kotlin, JavaScript, Swift, Python, Go, Java, Flutter. Type-safe, offline-first.") }
        Div(attrs = {
            style {
                maxWidth(640.px)
                property("margin", "0 auto")
                borderRadius(14.px)
                property("border", "1px solid rgba(255,255,255,0.12)")
                property("box-shadow", "0 20px 50px rgba(0,0,0,0.35)")
                overflow("hidden")
            }
        }) {
            Div(attrs = {
                style {
                    padding(12.px, 16.px)
                    property("background", "rgba(0,0,0,0.35)")
                    property("border-bottom", "1px solid rgba(255,255,255,0.08)")
                    display(DisplayStyle.Flex)
                    alignItems(AlignItems.Center)
                    gap(4.px)
                    flexWrap(FlexWrap.Wrap)
                }
            }) {
                Div(attrs = { style { width(10.px); height(10.px); borderRadius(50.percent); backgroundColor(FlagentTheme.Error) } }) {}
                Div(attrs = { style { width(10.px); height(10.px); borderRadius(50.percent); backgroundColor(FlagentTheme.Warning) } }) {}
                Div(attrs = { style { width(10.px); height(10.px); borderRadius(50.percent); backgroundColor(FlagentTheme.Success) } }) {}
                Span(attrs = { style { marginLeft(12.px); fontSize(12.px); color(Color("rgba(255,255,255,0.5)")) } }) { Text("Flagent · ${selected.lang}") }
                Span(attrs = { style { flexGrow(1) } }) {}
                examples.forEachIndexed { i, ex ->
                    Button(attrs = {
                        style {
                            padding(6.px, 12.px)
                            fontSize(12.px)
                            fontWeight(500)
                            borderRadius(6.px)
                            border(0.px)
                            cursor("pointer")
                            if (i == selectedIndex) {
                                property("background", "rgba(14, 165, 233, 0.35)")
                                color(Color.white)
                            } else {
                                backgroundColor(Color.transparent)
                                color(Color("rgba(255,255,255,0.7)"))
                            }
                        }
                        onClick { selectedIndex = i }
                    }) { Text(ex.lang) }
                }
            }
            Div(attrs = {
                style {
                    padding(20.px, 24.px)
                    property("background", "rgba(15, 23, 42, 0.95)")
                    property("font-family", "'JetBrains Mono', 'Fira Code', monospace")
                    fontSize(13.px)
                    lineHeight("1.6")
                    color(Color("rgba(255,255,255,0.92)"))
                }
            }) {
                Pre(attrs = {
                    style {
                        margin(0.px)
                        whiteSpace("pre-wrap")
                        property("word-break", "break-word")
                    }
                }) { Text(selected.code) }
            }
        }
        Div(attrs = { style { textAlign("center"); marginTop(20.px) } }) {
            A(href = AppConfig.gettingStartedUrl, attrs = {
                attr("target", "_blank")
                attr("rel", "noopener noreferrer")
                style {
                    color(FlagentTheme.PrimaryLight)
                    textDecoration("none")
                    fontSize(14.px)
                    fontWeight(600)
                }
            }) { Text("View full Quick Start →") }
        }
    }
}

private val SDK_LINKS = listOf(
    "Kotlin" to "https://github.com/MaxLuxs/Flagent/tree/main/sdk/kotlin",
    "Ktor" to "https://github.com/MaxLuxs/Flagent/tree/main/ktor-flagent",
    "JavaScript" to "https://github.com/MaxLuxs/Flagent/tree/main/sdk/javascript",
    "Swift" to "https://github.com/MaxLuxs/Flagent/tree/main/sdk/swift",
    "Python" to "https://github.com/MaxLuxs/Flagent/tree/main/sdk/python",
    "Go" to "https://github.com/MaxLuxs/Flagent/tree/main/sdk/go",
    "Java" to "https://github.com/MaxLuxs/Flagent/tree/main/sdk/java",
    "Flutter" to "https://github.com/MaxLuxs/Flagent/tree/main/sdk/flutter-enhanced",
)

@Composable
private fun SdksSection() {
    Div(attrs = {
        style {
            marginBottom(64.px)
            padding(32.px, 24.px)
            property("background", "rgba(255,255,255,0.03)")
            property("border", "1px solid rgba(255,255,255,0.06)")
            borderRadius(16.px)
        }
    }) {
        P(attrs = {
            style {
                fontSize(14.px)
                color(Color("rgba(255,255,255,0.6)"))
                textAlign("center")
                marginBottom(20.px)
            }
        }) { Text("One API. Every platform.") }
        Div(attrs = {
            style {
                display(DisplayStyle.Flex)
                flexWrap(FlexWrap.Wrap)
                gap(12.px)
                justifyContent(JustifyContent.Center)
            }
        }) {
            SDK_LINKS.forEach { (label, url) ->
                A(href = url, attrs = {
                    attr("target", "_blank")
                    attr("rel", "noopener noreferrer")
                    style {
                        padding(8.px, 16.px)
                        property("background", "rgba(255,255,255,0.06)")
                        borderRadius(8.px)
                        fontSize(13.px)
                        color(Color("rgba(255,255,255,0.85)"))
                        fontWeight(500)
                        textDecoration("none")
                        property("transition", "all 0.2s ease")
                    }
                    onMouseEnter {
                        val el = it.target as org.w3c.dom.HTMLElement
                        el.style.setProperty("background", "rgba(14, 165, 233, 0.2)")
                        el.style.color = "white"
                    }
                    onMouseLeave {
                        val el = it.target as org.w3c.dom.HTMLElement
                        el.style.setProperty("background", "rgba(255,255,255,0.06)")
                        el.style.color = "rgba(255,255,255,0.85)"
                    }
                }) { Text(label) }
            }
        }
        P(attrs = {
            style {
                textAlign("center")
                marginTop(16.px)
                fontSize(13.px)
                color(Color("rgba(255,255,255,0.5)"))
            }
        }) {
            A(href = AppConfig.docsUrlSdks, attrs = {
                attr("target", "_blank")
                attr("rel", "noopener noreferrer")
                style { color(FlagentTheme.PrimaryLight); textDecoration("none") }
            }) { Text("SDKs overview in docs →") }
        }
    }
}

private data class QuickFact(val icon: String, val title: String, val subtitle: String)

@Composable
private fun QuickFactsSection() {
    val facts = listOf(
        QuickFact("dns", "Self-hosted or cloud", "Your infra, Docker, or Flagent Cloud (coming soon)"),
        QuickFact("verified", "Type-safe SDKs", "Kotlin, Swift, Go, JS, Python — compile-time safety"),
        QuickFact("emergency", "Instant rollback", "Turn off any feature without redeploy"),
        QuickFact("code", "Open source", "Apache 2.0 — transparent and extensible"),
        QuickFact("rocket_launch", "Kotlin-native", "First feature-flag platform built with Ktor & KMP"),
    )
    Div(attrs = {
        style {
            marginBottom(64.px)
        }
    }) {
        P(attrs = {
            style {
                fontSize(14.px)
                color(Color("rgba(255,255,255,0.5)"))
                textAlign("center")
                marginBottom(20.px)
            }
        }) { Text("Highlights") }
        Div(attrs = {
            style {
                display(DisplayStyle.Flex)
                flexWrap(FlexWrap.Wrap)
                gap(16.px)
                justifyContent(JustifyContent.Center)
            }
        }) {
            facts.forEach { fact ->
                Div(attrs = {
                    style {
                        display(DisplayStyle.Flex)
                        flexDirection(FlexDirection.Column)
                        alignItems(AlignItems.Center)
                        padding(16.px, 20.px)
                        minWidth(160.px)
                        property("background", "rgba(255,255,255,0.04)")
                        property("border", "1px solid rgba(255,255,255,0.08)")
                        borderRadius(12.px)
                        property("transition", "all 0.2s ease")
                    }
                    onMouseEnter {
                        val el = it.target as org.w3c.dom.HTMLElement
                        el.style.setProperty("border-color", "rgba(14, 165, 233, 0.35)")
                        el.style.setProperty("background", "rgba(255,255,255,0.06)")
                    }
                    onMouseLeave {
                        val el = it.target as org.w3c.dom.HTMLElement
                        el.style.setProperty("border-color", "rgba(255,255,255,0.08)")
                        el.style.setProperty("background", "rgba(255,255,255,0.04)")
                    }
                }) {
                    Div(attrs = {
                        style {
                            width(40.px)
                            height(40.px)
                            borderRadius(10.px)
                            property("background", "rgba(14, 165, 233, 0.15)")
                            display(DisplayStyle.Flex)
                            alignItems(AlignItems.Center)
                            justifyContent(JustifyContent.Center)
                            marginBottom(10.px)
                        }
                    }) {
                        Icon(fact.icon, size = 22.px, color = FlagentTheme.PrimaryLight)
                    }
                    Span(attrs = {
                        style {
                            fontSize(14.px)
                            fontWeight(600)
                            color(Color.white)
                            textAlign("center")
                        }
                    }) { Text(fact.title) }
                    Span(attrs = {
                        style {
                            fontSize(12.px)
                            color(Color("rgba(255,255,255,0.6)"))
                            textAlign("center")
                            marginTop(4.px)
                            lineHeight("1.35")
                        }
                    }) { Text(fact.subtitle) }
                }
            }
        }
    }
}

/** Single feature for Why Flagent grid; route = null means no navigation (e.g. external link or just text). */
private data class LandingFeature(
    val icon: String,
    val title: String,
    val description: String,
    val onClick: () -> Unit
)

@Composable
private fun FeaturesSection() {
    val features = listOf(
        LandingFeature("flag", "Feature Flags", "Toggle features on or off without redeploying. Target by segments and attributes.") {
            Router.navigateTo(Route.FlagsList)
        },
        LandingFeature("science", "A/B Testing", "Run experiments with variants. Deterministic bucketing (MurmurHash3), measure and roll out winners.") {
            Router.navigateTo(Route.Experiments)
        },
        LandingFeature("trending_up", "Gradual Rollout", "Percentage-based rollouts with instant rollback. No redeploy needed.") {
            Router.navigateTo(Route.Dashboard)
        },
        LandingFeature("emergency", "Kill Switches", "Disable features instantly when issues arise. Production safety without code changes.") {
            Router.navigateTo(Route.FlagsList)
        },
        LandingFeature("segment", "Targeting & Segments", "Multi-environment, user attributes, and constraint rules. Target who sees what.") {
            Router.navigateTo(Route.Segments)
        },
        LandingFeature("bug_report", "Debug Console", "Real-time evaluation testing. Inspect flag values and context in one place.") {
            Router.navigateTo(Route.DebugConsole())
        },
        LandingFeature("analytics", "Analytics & Metrics", "Evaluation counts, events, core metrics. Data recorders: Kafka, Kinesis, PubSub. We're expanding dashboards and experiment insights.") {
            Router.navigateTo(Route.Analytics)
        },
        LandingFeature("warning", "Crash Reporting", "Ingestion, list, stack traces. We're expanding: crash-by-flag correlation and richer UI (Enterprise).") {
            Router.navigateTo(Route.Crash)
        },
        LandingFeature("code", "Ktor & 8 SDKs", "Ktor plugin, REST API. Type-safe: Kotlin, JS, Swift, Python, Go, Java, Flutter. Offline-first, SSE.") {
            kotlinx.browser.window.open(AppConfig.docsUrlSdks, "_blank")
        },
    )
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
                marginBottom(12.px)
            }
        }) { Text("Why Flagent?") }
        P(attrs = {
            style {
                fontSize(15.px)
                color(Color("rgba(255,255,255,0.6)"))
                textAlign("center")
                maxWidth(560.px)
                property("margin", "0 auto 32px")
                lineHeight("1.5")
            }
        }) { Text("Feature flags, experiments, crash analytics, and integrations in one platform. Kotlin-native, self-hosted or cloud.") }
        Div(attrs = {
            classes("features-grid")
            style {
                display(DisplayStyle.Grid)
                property("grid-template-columns", "repeat(auto-fit, minmax(220px, 1fr))")
                gap(18.px)
            }
        }) {
            features.forEach { f ->
                MarketingFeatureCard(icon = f.icon, title = f.title, description = f.description, onClick = f.onClick)
            }
        }
        P(attrs = {
            style {
                textAlign("center")
                marginTop(24.px)
                fontSize(13.px)
                color(Color("rgba(255,255,255,0.5)"))
                lineHeight("1.6")
            }
        }) {
            Text("Also: ")
            A(href = AppConfig.docsUrlGitOps, attrs = {
                attr("target", "_blank"); attr("rel", "noopener noreferrer")
                style { color(FlagentTheme.PrimaryLight); textDecoration("none") }
            }) { Text("Import/Export") }
            Text(" · ")
            A(href = AppConfig.docsUrlGitOps, attrs = {
                attr("target", "_blank"); attr("rel", "noopener noreferrer")
                style { color(FlagentTheme.PrimaryLight); textDecoration("none") }
            }) { Text("Webhooks") }
            Text(" · ")
            A(href = AppConfig.docsUrlRealtime, attrs = {
                attr("target", "_blank"); attr("rel", "noopener noreferrer")
                style { color(FlagentTheme.PrimaryLight); textDecoration("none") }
            }) { Text("Realtime (SSE)") }
            Text(" · ")
            A(href = AppConfig.docsUrlMcp, attrs = {
                attr("target", "_blank"); attr("rel", "noopener noreferrer")
                style { color(FlagentTheme.PrimaryLight); textDecoration("none") }
            }) { Text("MCP") }
            Text(" · Multi-environment · JWT auth · Data recorders (Kafka, Kinesis, PubSub) · RBAC · Self-hosted · Offline-first eval · Anomaly detection · Smart Rollout · SSO · Audit log")
        }
        P(attrs = {
            style {
                textAlign("center")
                marginTop(8.px)
                fontSize(13.px)
                color(Color("rgba(255,255,255,0.5)"))
            }
        }) {
            A(href = AppConfig.docsUrl, attrs = {
                attr("target", "_blank")
                attr("rel", "noopener noreferrer")
                style { color(FlagentTheme.PrimaryLight); textDecoration("none") }
            }) { Text("Full feature list in docs →") }
        }
    }
}

@Composable
private fun RoadmapSection() {
    Div(attrs = {
        style {
            marginBottom(64.px)
            padding(28.px, 24.px)
            property("background", "linear-gradient(135deg, rgba(14, 165, 233, 0.08) 0%, rgba(20, 184, 166, 0.06) 100%)")
            property("border", "1px solid rgba(14, 165, 233, 0.2)")
            borderRadius(16.px)
        }
    }) {
        Div(attrs = {
            style {
                display(DisplayStyle.Flex)
                alignItems(AlignItems.Center)
                justifyContent(JustifyContent.Center)
                gap(8.px)
                marginBottom(16.px)
            }
        }) {
            Span(attrs = {
                style {
                    padding(4.px, 10.px)
                    property("background", "rgba(14, 165, 233, 0.25)")
                    borderRadius(20.px)
                    fontSize(12.px)
                    fontWeight(600)
                    color(FlagentTheme.PrimaryLight)
                    property("letter-spacing", "0.02em")
                }
            }) { Text("Roadmap") }
        }
        H3(attrs = {
            style {
                fontSize(18.px)
                fontWeight(600)
                color(Color.white)
                textAlign("center")
                marginBottom(8.px)
            }
        }) { Text("Coming next: richer analytics & crash-by-flag") }
        P(attrs = {
            style {
                fontSize(14.px)
                color(Color("rgba(255,255,255,0.7)"))
                textAlign("center")
                maxWidth(520.px)
                property("margin", "0 auto")
                lineHeight("1.55")
            }
        }) {
            Text("We're shipping enhanced analytics (evaluation metrics, experiment insights) and crash reporting with stack traces, grouping, and crash-by-flag correlation. Enterprise early access available.")
        }
        Div(attrs = {
            style {
                display(DisplayStyle.Flex)
                flexWrap(FlexWrap.Wrap)
                gap(12.px)
                justifyContent(JustifyContent.Center)
                marginTop(16.px)
            }
        }) {
            listOf("Analytics dashboard" to "analytics", "Crash reporting" to "bug_report").forEach { (label, iconName) ->
                Div(attrs = {
                    style {
                        display(DisplayStyle.Flex)
                        alignItems(AlignItems.Center)
                        gap(8.px)
                        padding(10.px, 16.px)
                        property("background", "rgba(255,255,255,0.06)")
                        borderRadius(10.px)
                        property("border", "1px solid rgba(255,255,255,0.08)")
                    }
                }) {
                    Icon(iconName, size = 18.px, color = FlagentTheme.PrimaryLight)
                    Span(attrs = { style { fontSize(13.px); color(Color("rgba(255,255,255,0.9)")); fontWeight(500) } }) { Text(label) }
                }
            }
        }
        Div(attrs = { style { textAlign("center"); marginTop(20.px) } }) {
            A(href = "mailto:${AppConfig.enterpriseContactEmail}?subject=Flagent%20early%20access", attrs = {
                style {
                    color(FlagentTheme.PrimaryLight)
                    textDecoration("none")
                    fontSize(13.px)
                    fontWeight(600)
                }
            }) { Text("Request early access →") }
        }
    }
}

@Composable
private fun TrustSection() {
    val items = listOf(
        "Self-hosted" to "Your infrastructure, your data. No vendor lock-in.",
        "Open source" to "Apache 2.0. Transparent, auditable, extensible.",
        "Data stays yours" to "Run on-premise or in your cloud. We never see your flag data.",
    )
    Div(attrs = {
        style {
            marginBottom(64.px)
            padding(32.px, 24.px)
            property("background", "rgba(255,255,255,0.03)")
            property("border", "1px solid rgba(255,255,255,0.08)")
            borderRadius(16.px)
        }
    }) {
        P(attrs = {
            style {
                fontSize(13.px)
                color(Color("rgba(255,255,255,0.5)"))
                textAlign("center")
                marginBottom(20.px)
                property("text-transform", "uppercase")
                property("letter-spacing", "0.06em")
            }
        }) { Text("Trust & control") }
        Div(attrs = {
            style {
                display(DisplayStyle.Flex)
                flexWrap(FlexWrap.Wrap)
                gap(24.px)
                justifyContent(JustifyContent.Center)
            }
        }) {
            items.forEach { (title, subtitle) ->
                Div(attrs = {
                    style {
                        maxWidth(260.px)
                        padding(16.px, 20.px)
                        property("background", "rgba(0,0,0,0.2)")
                        borderRadius(12.px)
                        property("border", "1px solid rgba(255,255,255,0.06)")
                    }
                }) {
                    Span(attrs = {
                        style {
                            fontSize(14.px)
                            fontWeight(600)
                            color(Color.white)
                        }
                    }) { Text(title) }
                    P(attrs = {
                        style {
                            margin(8.px, 0.px, 0.px, 0.px)
                            fontSize(13.px)
                            color(Color("rgba(255,255,255,0.65)"))
                            lineHeight("1.5")
                        }
                    }) { Text(subtitle) }
                }
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
            padding(18.px)
            property("background", "rgba(255, 255, 255, 0.04)")
            property("border", "1px solid rgba(255, 255, 255, 0.08)")
            property("backdrop-filter", "blur(12px)")
            borderRadius(14.px)
            property("box-shadow", "0 4px 24px rgba(0,0,0,0.15)")
            property("transition", "all 0.3s cubic-bezier(0.4, 0, 0.2, 1)")
            cursor("pointer")
        }
        onClick { onClick() }
        onMouseEnter {
            val el = it.target as org.w3c.dom.HTMLElement
            el.style.transform = "translateY(-4px) scale(1.02)"
            el.style.setProperty("box-shadow", "0 12px 40px rgba(0,0,0,0.25)")
            el.style.setProperty("border-color", "rgba(14, 165, 233, 0.3)")
        }
        onMouseLeave {
            val el = it.target as org.w3c.dom.HTMLElement
            el.style.transform = "translateY(0) scale(1)"
            el.style.setProperty("box-shadow", "0 4px 24px rgba(0,0,0,0.15)")
            el.style.setProperty("border-color", "rgba(255, 255, 255, 0.08)")
        }
    }) {
        Div(attrs = {
            style {
                width(44.px)
                height(44.px)
                borderRadius(12.px)
                property("background", "linear-gradient(135deg, rgba(14, 165, 233, 0.25) 0%, rgba(20, 184, 166, 0.2) 100%)")
                display(DisplayStyle.Flex)
                alignItems(AlignItems.Center)
                justifyContent(JustifyContent.Center)
                marginBottom(14.px)
            }
        }) {
            Icon(icon, size = 24.px, color = FlagentTheme.PrimaryLight)
        }
        H3(attrs = {
            style {
                margin(0.px, 0.px, 6.px, 0.px)
                fontSize(16.px)
                fontWeight(600)
                color(Color.white)
            }
        }) { Text(title) }
        P(attrs = {
            style {
                margin(0.px)
                fontSize(13.px)
                color(Color("rgba(255,255,255,0.65)"))
                lineHeight("1.5")
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
                marginBottom(12.px)
            }
        }) { Text("Use Cases") }
        P(attrs = {
            style {
                fontSize(15.px)
                color(Color("rgba(255,255,255,0.6)"))
                textAlign("center")
                maxWidth(520.px)
                property("margin", "0 auto 32px")
                lineHeight("1.5")
            }
        }) { Text("Ship features safely, experiment with confidence, roll back in one click.") }
        Div(attrs = {
            style {
                display(DisplayStyle.Grid)
                property("grid-template-columns", "repeat(auto-fit, minmax(260px, 1fr))")
                gap(24.px)
            }
        }) {
            UseCaseCard(
                icon = "rocket_launch",
                "Safe Deployments",
                "Deploy to production behind feature flags. Roll back instantly without code changes or redeploys."
            )
            UseCaseCard(
                icon = "science",
                "A/B Testing",
                "Run multi-variant experiments with deterministic bucketing. Measure impact and ship winners."
            )
            UseCaseCard(
                icon = "trending_up",
                "Gradual Rollouts",
                "Ramp from 1% to 100% at your pace. Kill switch any feature the moment issues appear."
            )
            UseCaseCard(
                icon = "segment",
                "Targeting & Segments",
                "Target by user attributes, regions, or rules. Multi-environment configs for dev, staging, prod."
            )
        }
    }
}

@Composable
private fun UseCaseCard(icon: String, title: String, description: String) {
    Div(attrs = {
        style {
            padding(20.px)
            property("background", "rgba(0,0,0,0.2)")
            borderRadius(12.px)
            property("border", "1px solid rgba(255,255,255,0.06)")
            property("transition", "all 0.2s ease")
        }
        onMouseEnter {
            val el = it.target as org.w3c.dom.HTMLElement
            el.style.setProperty("border-color", "rgba(14, 165, 233, 0.25)")
        }
        onMouseLeave {
            val el = it.target as org.w3c.dom.HTMLElement
            el.style.setProperty("border-color", "rgba(255,255,255,0.06)")
        }
    }) {
        Div(attrs = {
            style {
                width(40.px)
                height(40.px)
                borderRadius(10.px)
                property("background", "rgba(14, 165, 233, 0.15)")
                display(DisplayStyle.Flex)
                alignItems(AlignItems.Center)
                justifyContent(JustifyContent.Center)
                marginBottom(12.px)
            }
        }) {
            Icon(icon, size = 22.px, color = FlagentTheme.PrimaryLight)
        }
        H3(attrs = {
            style {
                margin(0.px, 0.px, 8.px, 0.px)
                fontSize(16.px)
                fontWeight(600)
                color(Color.white)
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

private data class EnterpriseFeature(val icon: String, val label: String)

@Composable
private fun EnterpriseSection() {
    val securityFeatures = listOf(
        EnterpriseFeature("lock", "SSO (SAML, OIDC)"),
        EnterpriseFeature("group", "RBAC & custom roles"),
        EnterpriseFeature("description", "Audit logs"),
        EnterpriseFeature("dns", "On-premise"),
    )
    val productFeatures = listOf(
        EnterpriseFeature("trending_up", "Smart Rollout"),
        EnterpriseFeature("warning", "Anomaly detection"),
        EnterpriseFeature("bug_report", "Crash-by-flag analytics"),
        EnterpriseFeature("notifications", "Slack alerts"),
        EnterpriseFeature("credit_card", "Billing & subscriptions"),
        EnterpriseFeature("support_agent", "Dedicated support"),
    )
    Div(attrs = {
        style {
            padding(48.px, 40.px)
            property("background", "linear-gradient(160deg, rgba(15, 23, 42, 0.6) 0%, rgba(14, 165, 233, 0.06) 50%, rgba(20, 184, 166, 0.04) 100%)")
            property("border", "1px solid rgba(14, 165, 233, 0.25)")
            borderRadius(24.px)
            property("backdrop-filter", "blur(12px)")
            property("box-shadow", "0 24px 48px rgba(0,0,0,0.2), inset 0 1px 0 rgba(255,255,255,0.06)")
            marginBottom(64.px)
        }
    }) {
        Div(attrs = {
            style {
                display(DisplayStyle.Flex)
                alignItems(AlignItems.Center)
                justifyContent(JustifyContent.Center)
                gap(10.px)
                marginBottom(12.px)
            }
        }) {
            Icon("business_center", size = 28.px, color = FlagentTheme.PrimaryLight)
            H2(attrs = {
                style {
                    fontSize(26.px)
                    fontWeight(700)
                    color(Color.white)
                    textAlign("center")
                    margin(0.px)
                    property("letter-spacing", "-0.02em")
                }
            }) { Text("Flagent Enterprise") }
        }
        P(attrs = {
            style {
                fontSize(16.px)
                color(Color("rgba(255,255,255,0.75)"))
                textAlign("center")
                maxWidth(560.px)
                property("margin", "0 auto 36px")
                lineHeight("1.65")
            }
        }) {
            Text("For teams that need compliance, scale, and control: identity, roles, audit trail, safe rollouts, anomaly detection, and crash-by-flag insights — with on-premise and dedicated support.")
        }
        Div(attrs = {
            style {
                display(DisplayStyle.Grid)
                property("grid-template-columns", "repeat(auto-fit, minmax(260px, 1fr))")
                gap(24.px)
                maxWidth(720.px)
                property("margin", "0 auto 32px")
            }
        }) {
            Div(attrs = {
                style {
                    padding(20.px)
                    property("background", "rgba(0,0,0,0.2)")
                    borderRadius(14.px)
                    property("border", "1px solid rgba(255,255,255,0.06)")
                }
            }) {
                Span(attrs = {
                    style {
                        fontSize(12.px)
                        fontWeight(600)
                        color(FlagentTheme.PrimaryLight)
                        property("letter-spacing", "0.04em")
                    }
                }) { Text("Security & compliance") }
                Div(attrs = { style { marginTop(12.px); display(DisplayStyle.Flex); flexWrap(FlexWrap.Wrap); gap(8.px) } }) {
                    securityFeatures.forEach { f ->
                        Span(attrs = {
                            style {
                                display(DisplayStyle.Flex)
                                alignItems(AlignItems.Center)
                                gap(6.px)
                                padding(6.px, 12.px)
                                property("background", "rgba(14, 165, 233, 0.12)")
                                borderRadius(8.px)
                                fontSize(13.px)
                                color(Color("rgba(255,255,255,0.9)"))
                                fontWeight(500)
                            }
                        }) {
                            Icon(f.icon, size = 14.px, color = FlagentTheme.PrimaryLight)
                            Text(f.label)
                        }
                    }
                }
            }
            Div(attrs = {
                style {
                    padding(20.px)
                    property("background", "rgba(0,0,0,0.2)")
                    borderRadius(14.px)
                    property("border", "1px solid rgba(255,255,255,0.06)")
                }
            }) {
                Span(attrs = {
                    style {
                        fontSize(12.px)
                        fontWeight(600)
                        color(FlagentTheme.PrimaryLight)
                        property("letter-spacing", "0.04em")
                    }
                }) { Text("Product & operations") }
                Div(attrs = { style { marginTop(12.px); display(DisplayStyle.Flex); flexWrap(FlexWrap.Wrap); gap(8.px) } }) {
                    productFeatures.forEach { f ->
                        Span(attrs = {
                            style {
                                display(DisplayStyle.Flex)
                                alignItems(AlignItems.Center)
                                gap(6.px)
                                padding(6.px, 12.px)
                                property("background", "rgba(14, 165, 233, 0.12)")
                                borderRadius(8.px)
                                fontSize(13.px)
                                color(Color("rgba(255,255,255,0.9)"))
                                fontWeight(500)
                            }
                        }) {
                            Icon(f.icon, size = 14.px, color = FlagentTheme.PrimaryLight)
                            Text(f.label)
                        }
                    }
                }
            }
        }
        Div(attrs = { style { textAlign("center") } }) {
            A(href = "mailto:${AppConfig.enterpriseContactEmail}?subject=Flagent%20Enterprise", attrs = {
                style {
                    property("display", "inline-flex")
                    alignItems(AlignItems.Center)
                    justifyContent(JustifyContent.Center)
                    gap(10.px)
                    padding(18.px, 36.px)
                    property(
                        "background",
                        "linear-gradient(135deg, ${FlagentTheme.Primary} 0%, ${FlagentTheme.PrimaryDark} 100%)"
                    )
                    color(Color.white)
                    textDecoration("none")
                    borderRadius(14.px)
                    fontSize(16.px)
                    fontWeight(600)
                    property("box-shadow", "0 8px 28px rgba(14, 165, 233, 0.5)")
                    property("transition", "all 0.25s cubic-bezier(0.4, 0, 0.2, 1)")
                }
                onMouseEnter {
                    val el = it.target as org.w3c.dom.HTMLElement
                    el.style.transform = "translateY(-3px) scale(1.02)"
                    el.style.setProperty("box-shadow", "0 14px 36px rgba(14, 165, 233, 0.6)")
                }
                onMouseLeave {
                    val el = it.target as org.w3c.dom.HTMLElement
                    el.style.transform = "translateY(0) scale(1)"
                    el.style.setProperty("box-shadow", "0 8px 28px rgba(14, 165, 233, 0.5)")
                }
            }) {
                Icon("mail", size = 22.px, color = Color.white)
                Text("Request demo")
            }
        }
    }
}
