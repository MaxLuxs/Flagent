package flagent.frontend.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import flagent.frontend.api.ApiClient
import flagent.frontend.i18n.LocalizedStrings
import flagent.frontend.navigation.Route
import flagent.frontend.navigation.Router
import flagent.frontend.theme.FlagentTheme
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

/**
 * Breadcrumbs component - navigation breadcrumbs with flag name
 */
@Composable
fun Breadcrumbs() {
    val route = Router.currentRoute
    val flagKey = remember { mutableStateOf<String?>(null) }
    
    // Load flag key for FlagDetail and FlagHistory routes
    LaunchedEffect(route) {
        when (route) {
            is Route.FlagDetail -> {
                try {
                    val flag = ApiClient.getFlag(route.flagId)
                    flagKey.value = flag.key
                } catch (e: Exception) {
                    flagKey.value = null
                }
            }
            is Route.FlagHistory -> {
                try {
                    val flag = ApiClient.getFlag(route.flagId)
                    flagKey.value = flag.key
                } catch (e: Exception) {
                    flagKey.value = null
                }
            }
            else -> flagKey.value = null
        }
    }
    
    Div({
        style {
            marginBottom(20.px)
            padding(10.px, 0.px)
        }
    }) {
        Div({
            style {
                display(DisplayStyle.Flex)
                alignItems(AlignItems.Center)
                gap(8.px)
                fontSize(14.px)
            }
        }) {
            A(href = "#", attrs = {
                onClick { 
                    it.preventDefault()
                    Router.navigateTo(Route.Home) 
                }
                style {
                    textDecoration("none")
                    color(FlagentTheme.Primary)
                    cursor("pointer")
                    property("transition", "color 0.2s")
                }
                onMouseEnter {
                    val element = it.target as org.w3c.dom.HTMLElement
                    element.style.color = FlagentTheme.Secondary.toString()
                }
                onMouseLeave {
                    val element = it.target as org.w3c.dom.HTMLElement
                    element.style.color = FlagentTheme.Primary.toString()
                }
            }) {
                Text(LocalizedStrings.home)
            }
            
            when (route) {
                is Route.FlagDetail -> {
                    BreadcrumbSeparator()
                    A(href = "#", attrs = {
                        onClick { 
                            it.preventDefault()
                            Router.navigateTo(Route.FlagDetail(route.flagId)) 
                        }
                        style {
                            textDecoration("none")
                            color(FlagentTheme.Primary)
                            cursor("pointer")
                            fontWeight("500")
                            property("transition", "color 0.2s")
                        }
                        onMouseEnter {
                            val element = it.target as org.w3c.dom.HTMLElement
                            element.style.color = FlagentTheme.Secondary.toString()
                        }
                        onMouseLeave {
                            val element = it.target as org.w3c.dom.HTMLElement
                            element.style.color = FlagentTheme.Primary.toString()
                        }
                    }) {
                        Text(flagKey.value ?: LocalizedStrings.flagNumber(route.flagId))
                    }
                }
                is Route.CreateFlag -> {
                    BreadcrumbSeparator()
                    Span({
                        style {
                            color(FlagentTheme.Text)
                            fontWeight("500")
                        }
                    }) {
                        Text(LocalizedStrings.createFlag)
                    }
                }
                is Route.DebugConsole -> {
                    BreadcrumbSeparator()
                    Span({
                        style {
                            color(FlagentTheme.Text)
                            fontWeight("500")
                        }
                    }) {
                        Text(LocalizedStrings.debugConsole)
                    }
                }
                is Route.FlagHistory -> {
                    BreadcrumbSeparator()
                    if (flagKey.value != null) {
                        A(href = "#", attrs = {
                            onClick { 
                                it.preventDefault()
                                Router.navigateTo(Route.FlagDetail(route.flagId)) 
                            }
                            style {
                                textDecoration("none")
                                color(FlagentTheme.Primary)
                                cursor("pointer")
                                fontWeight("500")
                                property("transition", "color 0.2s")
                            }
                            onMouseEnter {
                                val element = it.target as org.w3c.dom.HTMLElement
                                element.style.color = FlagentTheme.Secondary.toString()
                            }
                            onMouseLeave {
                                val element = it.target as org.w3c.dom.HTMLElement
                                element.style.color = FlagentTheme.Primary.toString()
                            }
                        }) {
                            Text(flagKey.value ?: LocalizedStrings.flagNumber(route.flagId))
                        }
                        BreadcrumbSeparator()
                    }
                    Span({
                        style {
                            color(FlagentTheme.Text)
                            fontWeight("500")
                        }
                    }) {
                        Text(LocalizedStrings.history)
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun BreadcrumbSeparator() {
    Span({
        style {
            color(FlagentTheme.TextLight)
            margin(0.px, 4.px)
            fontSize(12.px)
        }
    }) {
        Text("›")
    }
}
