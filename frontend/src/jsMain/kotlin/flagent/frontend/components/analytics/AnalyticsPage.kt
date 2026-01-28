package flagent.frontend.components.analytics

import androidx.compose.runtime.*
import flagent.api.model.FlagResponse
import flagent.frontend.api.ApiClient
import flagent.frontend.components.Icon
import flagent.frontend.i18n.LocalizedStrings
import flagent.frontend.navigation.Route
import flagent.frontend.navigation.Router
import flagent.frontend.theme.FlagentTheme
import flagent.frontend.util.ErrorHandler
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

/**
 * Analytics page: list of flags with links to per-flag metrics.
 * No global aggregates API on backend — show flags and "View metrics" links.
 */
@Composable
fun AnalyticsPage() {
    var flags by remember { mutableStateOf<List<FlagResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        isLoading = true
        error = null
        ErrorHandler.withErrorHandling(
            block = {
                flags = ApiClient.getFlags()
            },
            onError = { err ->
                error = ErrorHandler.getUserMessage(err)
            }
        )
        isLoading = false
    }

    Div({
        style {
            padding(20.px)
        }
    }) {
        Div({
            style {
                marginBottom(30.px)
            }
        }) {
            H1({
                style {
                    fontSize(28.px)
                    fontWeight("bold")
                    color(FlagentTheme.Text)
                    margin(0.px)
                }
            }) {
                Text(LocalizedStrings.analyticsTitle)
            }
            P({
                style {
                    color(FlagentTheme.TextLight)
                    fontSize(14.px)
                    marginTop(5.px)
                }
            }) {
                Text(LocalizedStrings.analyticsSubtitle)
            }
        }

        if (isLoading) {
            Div({
                style {
                    textAlign("center")
                    padding(40.px)
                }
            }) {
                Text(LocalizedStrings.loading)
            }
        } else if (error != null) {
            Div({
                style {
                    padding(20.px)
                    backgroundColor(FlagentTheme.Error)
                    borderRadius(8.px)
                    color(FlagentTheme.Background)
                }
            }) {
                Text(error!!)
            }
        } else if (flags.isEmpty()) {
            Div({
                style {
                    backgroundColor(FlagentTheme.Background)
                    borderRadius(8.px)
                    padding(40.px)
                    property("box-shadow", "0 2px 8px rgba(0,0,0,0.1)")
                    textAlign("center")
                }
            }) {
                Icon("analytics", size = 48.px, color = FlagentTheme.TextLight)
                P({
                    style {
                        marginTop(16.px)
                        color(FlagentTheme.Text)
                        fontSize(16.px)
                    }
                }) {
                    Text(LocalizedStrings.noFlags)
                }
                Button({
                    style {
                        marginTop(20.px)
                        padding(12.px, 24.px)
                        backgroundColor(FlagentTheme.Primary)
                        color(FlagentTheme.Background)
                        border(0.px)
                        borderRadius(6.px)
                        cursor("pointer")
                        fontSize(14.px)
                    }
                    onClick { Router.navigateTo(Route.Home) }
                }) {
                    Text(LocalizedStrings.createFlag)
                }
            }
        } else {
            Div({
                style {
                    display(DisplayStyle.Grid)
                    property("grid-template-columns", "repeat(auto-fill, minmax(280px, 1fr))")
                    gap(16.px)
                }
            }) {
                flags.forEach { flag ->
                    AnalyticsFlagCard(flag)
                }
            }
        }
    }
}

@Composable
private fun AnalyticsFlagCard(flag: FlagResponse) {
    Div({
        style {
            backgroundColor(FlagentTheme.Background)
            borderRadius(8.px)
            padding(16.px)
            property("box-shadow", "0 2px 8px rgba(0,0,0,0.1)")
            property("transition", "box-shadow 0.2s")
        }
        onMouseEnter {
            (it.target as org.w3c.dom.HTMLElement).style.boxShadow = "0 4px 12px rgba(0,0,0,0.1)"
        }
        onMouseLeave {
            (it.target as org.w3c.dom.HTMLElement).style.boxShadow = "0 2px 8px rgba(0,0,0,0.1)"
        }
    }) {
        Div({
            style {
                display(DisplayStyle.Flex)
                justifyContent(JustifyContent.SpaceBetween)
                alignItems(AlignItems.Center)
                marginBottom(10.px)
            }
        }) {
            Span({
                style {
                    fontSize(15.px)
                    fontWeight("600")
                    color(FlagentTheme.Text)
                    property("overflow", "hidden")
                    property("text-overflow", "ellipsis")
                    property("white-space", "nowrap")
                    flex(1)
                    property("min-width", "0")
                }
            }) {
                Text(flag.key.ifBlank { "Flag #${flag.id}" })
            }
            A(href = Route.FlagMetrics(flag.id).path(), attrs = {
                style {
                    display(DisplayStyle.Flex)
                    alignItems(AlignItems.Center)
                    gap(4.px)
                    padding(6.px, 10.px)
                    backgroundColor(FlagentTheme.Primary)
                    color(FlagentTheme.Background)
                    borderRadius(6.px)
                    fontSize(12.px)
                    textDecoration("none")
                    flexShrink(0)
                }
                onClick { e ->
                    e.preventDefault()
                    Router.navigateTo(Route.FlagMetrics(flag.id))
                }
            }) {
                Icon("bar_chart", size = 14.px, color = FlagentTheme.Background)
                Text(LocalizedStrings.viewMetrics)
            }
        }
        if (flag.description.isNotBlank()) {
            P({
                style {
                    fontSize(13.px)
                    color(FlagentTheme.TextLight)
                    margin(0.px)
                    property("overflow", "hidden")
                    property("text-overflow", "ellipsis")
                    property("display", "-webkit-box")
                    property("-webkit-line-clamp", "2")
                    property("-webkit-box-orient", "vertical")
                }
            }) {
                Text(flag.description)
            }
        }
    }
}
