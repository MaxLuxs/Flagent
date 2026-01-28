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
 * Login Form component (Phase 2)
 */
@Composable
fun LoginForm(
    viewModel: AuthViewModel,
    onSuccess: () -> Unit
) {
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    
    Div({
        style {
            display(DisplayStyle.Flex)
            justifyContent(JustifyContent.Center)
            alignItems(AlignItems.Center)
            minHeight(100.vh)
            backgroundColor(Color("#F8FAFC"))
        }
    }) {
        Div({
            style {
                backgroundColor(Color.white)
                borderRadius(8.px)
                padding(32.px)
                property("box-shadow", "0 4px 6px rgba(0, 0, 0, 0.1)")
                maxWidth(400.px)
                width(100.percent)
            }
        }) {
            H1({
                style {
                    fontSize(24.px)
                    fontWeight(600)
                    color(Color("#1E293B"))
                    margin(0.px)
                    marginBottom(24.px)
                    textAlign("center")
                }
            }) {
                Text("Welcome to Flagent")
            }
            
            // Email input
            Div({
                style {
                    marginBottom(16.px)
                }
            }) {
                Span({
                    style {
                        display(DisplayStyle.Block)
                        fontSize(14.px)
                        fontWeight(500)
                        color(Color("#1E293B"))
                        marginBottom(8.px)
                    }
                }) {
                    Text("Email")
                }
                Input(InputType.Email) {
                    value(email.value)
                    onInput { email.value = it.value }
                    style {
                        width(100.percent)
                        padding(10.px, 16.px)
                        border(1.px, LineStyle.Solid, Color("#E2E8F0"))
                        borderRadius(6.px)
                        fontSize(14.px)
                        property("box-sizing", "border-box")
                    }
                }
            }
            
            // Password input
            Div({
                style {
                    marginBottom(24.px)
                }
            }) {
                Span({
                    style {
                        display(DisplayStyle.Block)
                        fontSize(14.px)
                        fontWeight(500)
                        color(Color("#1E293B"))
                        marginBottom(8.px)
                    }
                }) {
                    Text("Password")
                }
                Input(InputType.Password) {
                    value(password.value)
                    onInput { password.value = it.value }
                    style {
                        width(100.percent)
                        padding(10.px, 16.px)
                        border(1.px, LineStyle.Solid, Color("#E2E8F0"))
                        borderRadius(6.px)
                        fontSize(14.px)
                        property("box-sizing", "border-box")
                    }
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
                    padding(12.px)
                    backgroundColor(FlagentTheme.Primary)
                    color(Color.white)
                    border(0.px)
                    borderRadius(6.px)
                    cursor(if (viewModel.isLoading) "not-allowed" else "pointer")
                    fontSize(14.px)
                    fontWeight(500)
                }
            }) {
                Text(if (viewModel.isLoading) "Loading..." else "Sign In")
            }

            if (AppConfig.Features.enableSso) {
                Div({
                    style {
                        marginTop(16.px)
                        paddingTop(16.px)
                        property("border-top", "1px solid #E2E8F0")
                    }
                }) {
                    Span({
                        style {
                            display(DisplayStyle.Block)
                            fontSize(12.px)
                            color(Color("#64748B"))
                            marginBottom(12.px)
                        }
                    }) {
                        Text("Or sign in with SSO")
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
                            padding(10.px, 16.px)
                            backgroundColor(Color.transparent)
                            color(FlagentTheme.Primary)
                            border(1.px, LineStyle.Solid, FlagentTheme.Primary)
                            borderRadius(6.px)
                            cursor("pointer")
                            fontSize(14.px)
                            fontWeight(500)
                            display(DisplayStyle.Flex)
                            alignItems(AlignItems.Center)
                            justifyContent(JustifyContent.Center)
                            gap(8.px)
                        }
                    }) {
                        Icon("login", size = 18.px, color = FlagentTheme.Primary)
                        Text("Login with SSO")
                    }
                }
            }
            
            // Error message
            viewModel.error?.let { error ->
                Div({
                    style {
                        marginTop(16.px)
                        padding(12.px)
                        backgroundColor(Color("#FEE2E2"))
                        borderRadius(6.px)
                        color(Color("#991B1B"))
                        fontSize(14.px)
                    }
                }) {
                    Text(error)
                }
            }
        }
    }
}
