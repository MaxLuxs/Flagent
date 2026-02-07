package flagent.frontend.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import flagent.frontend.api.ApiClient
import flagent.frontend.i18n.LocalizedStrings
import flagent.frontend.navigation.Route
import flagent.frontend.navigation.Router
import flagent.frontend.state.LocalThemeMode
import flagent.frontend.theme.FlagentTheme
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

/**
 * Breadcrumbs component - navigation breadcrumbs with flag name
 */
@Composable
fun Breadcrumbs() {
    val themeMode = LocalThemeMode.current
    val route = Router.currentRoute
    val flagKey = remember { mutableStateOf<String?>(null) }
    
    // Load flag key for FlagDetail, FlagHistory, FlagMetrics, FlagRollout, FlagAnomalies routes
    LaunchedEffect(route) {
        when (route) {
            is Route.FlagDetail, is Route.FlagHistory, is Route.FlagMetrics, is Route.FlagRollout, is Route.FlagAnomalies -> {
                try {
                    val flagId = when (route) {
                        is Route.FlagDetail -> route.flagId
                        is Route.FlagHistory -> route.flagId
                        is Route.FlagMetrics -> route.flagId
                        is Route.FlagRollout -> route.flagId
                        is Route.FlagAnomalies -> route.flagId
                        else -> return@LaunchedEffect
                    }
                    val flag = ApiClient.getFlag(flagId)
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
            when (route) {
                is Route.FlagMetrics -> {
                    val fromCrash = route.metricType == "CRASH_RATE"
                    A(href = "#", attrs = {
                        onClick { 
                            it.preventDefault()
                            Router.navigateTo(if (fromCrash) Route.Crash else Route.Analytics)
                        }
                        style {
                            textDecoration("none")
                            color(FlagentTheme.PrimaryLight)
                            cursor("pointer")
                            property("transition", "color 0.2s")
                        }
                        onMouseEnter {
                            val element = it.target as org.w3c.dom.HTMLElement
                            element.style.color = FlagentTheme.PrimaryLight.toString()
                        }
                        onMouseLeave {
                            val element = it.target as org.w3c.dom.HTMLElement
                            element.style.color = FlagentTheme.PrimaryLight.toString()
                        }
                    }) {
                        Text(if (fromCrash) LocalizedStrings.backToCrash else LocalizedStrings.backToAnalytics)
                    }
                }
                else -> {
                    A(href = "#", attrs = {
                        onClick { 
                            it.preventDefault()
                            Router.navigateTo(Route.FlagsList) 
                        }
                        style {
                            textDecoration("none")
                            color(FlagentTheme.PrimaryLight)
                            cursor("pointer")
                            property("transition", "color 0.2s")
                        }
                        onMouseEnter {
                            val element = it.target as org.w3c.dom.HTMLElement
                            element.style.color = FlagentTheme.PrimaryLight.toString()
                        }
                        onMouseLeave {
                            val element = it.target as org.w3c.dom.HTMLElement
                            element.style.color = FlagentTheme.PrimaryLight.toString()
                        }
                    }) {
                        Text(LocalizedStrings.home)
                    }
                }
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
                            color(FlagentTheme.PrimaryLight)
                            cursor("pointer")
                            fontWeight("500")
                            property("transition", "color 0.2s")
                        }
                        onMouseEnter {
                            val element = it.target as org.w3c.dom.HTMLElement
                            element.style.color = FlagentTheme.PrimaryLight.toString()
                        }
                        onMouseLeave {
                            val element = it.target as org.w3c.dom.HTMLElement
                            element.style.color = FlagentTheme.PrimaryLight.toString()
                        }
                    }) {
                        Text(flagKey.value ?: LocalizedStrings.flagNumber(route.flagId))
                    }
                }
                is Route.CreateFlag -> {
                    BreadcrumbSeparator()
                    Span({
                        style {
                            color(FlagentTheme.text(themeMode))
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
                            color(FlagentTheme.text(themeMode))
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
                                color(FlagentTheme.PrimaryLight)
                                cursor("pointer")
                                fontWeight("500")
                                property("transition", "color 0.2s")
                            }
                            onMouseEnter {
                                val element = it.target as org.w3c.dom.HTMLElement
                                element.style.color = FlagentTheme.PrimaryLight.toString()
                            }
                            onMouseLeave {
                                val element = it.target as org.w3c.dom.HTMLElement
                                element.style.color = FlagentTheme.PrimaryLight.toString()
                            }
                        }) {
                            Text(flagKey.value ?: LocalizedStrings.flagNumber(route.flagId))
                        }
                        BreadcrumbSeparator()
                    }
                    Span({
                        style {
                            color(FlagentTheme.text(themeMode))
                            fontWeight("500")
                        }
                    }) {
                        Text(LocalizedStrings.history)
                    }
                }
                is Route.FlagMetrics -> {
                    BreadcrumbSeparator()
                    Span({
                        style {
                            color(FlagentTheme.text(themeMode))
                            fontWeight("500")
                        }
                    }) {
                        Text(flagKey.value ?: LocalizedStrings.flagNumber(route.flagId))
                    }
                }
                is Route.FlagRollout -> {
                    BreadcrumbSeparator()
                    if (flagKey.value != null) {
                        A(href = "#", attrs = {
                            onClick {
                                it.preventDefault()
                                Router.navigateTo(Route.FlagDetail(route.flagId))
                            }
                            style {
                                textDecoration("none")
                                color(FlagentTheme.PrimaryLight)
                                cursor("pointer")
                                fontWeight("500")
                                property("transition", "color 0.2s")
                            }
                            onMouseEnter {
                                val element = it.target as org.w3c.dom.HTMLElement
                                element.style.color = FlagentTheme.PrimaryLight.toString()
                            }
                            onMouseLeave {
                                val element = it.target as org.w3c.dom.HTMLElement
                                element.style.color = FlagentTheme.PrimaryLight.toString()
                            }
                        }) {
                            Text(flagKey.value ?: LocalizedStrings.flagNumber(route.flagId))
                        }
                        BreadcrumbSeparator()
                    }
                    Span({
                        style {
                            color(FlagentTheme.text(themeMode))
                            fontWeight("500")
                        }
                    }) {
                        Text(LocalizedStrings.rollout)
                    }
                }
                is Route.FlagAnomalies -> {
                    BreadcrumbSeparator()
                    if (flagKey.value != null) {
                        A(href = "#", attrs = {
                            onClick {
                                it.preventDefault()
                                Router.navigateTo(Route.FlagDetail(route.flagId))
                            }
                            style {
                                textDecoration("none")
                                color(FlagentTheme.PrimaryLight)
                                cursor("pointer")
                                fontWeight("500")
                                property("transition", "color 0.2s")
                            }
                            onMouseEnter {
                                val element = it.target as org.w3c.dom.HTMLElement
                                element.style.color = FlagentTheme.PrimaryLight.toString()
                            }
                            onMouseLeave {
                                val element = it.target as org.w3c.dom.HTMLElement
                                element.style.color = FlagentTheme.PrimaryLight.toString()
                            }
                        }) {
                            Text(flagKey.value ?: LocalizedStrings.flagNumber(route.flagId))
                        }
                        BreadcrumbSeparator()
                    }
                    Span({
                        style {
                            color(FlagentTheme.text(themeMode))
                            fontWeight("500")
                        }
                    }) {
                        Text(LocalizedStrings.anomalies)
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun BreadcrumbSeparator() {
    val themeMode = LocalThemeMode.current
    Span({
        style {
            color(FlagentTheme.textLight(themeMode))
            margin(0.px, 4.px)
            fontSize(12.px)
        }
    }) {
        Text("›")
    }
}
