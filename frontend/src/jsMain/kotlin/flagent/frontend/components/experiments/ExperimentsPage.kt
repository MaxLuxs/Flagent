package flagent.frontend.components.experiments

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
 * Experiments (A/B) page: flags with 2+ variants.
 */
@Composable
fun ExperimentsPage() {
    var flags by remember { mutableStateOf<List<FlagResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        isLoading = true
        error = null
        ErrorHandler.withErrorHandling(
            block = {
                val all = ApiClient.getFlags()
                flags = all.filter { it.variants.size >= 2 }
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
                Text(LocalizedStrings.experimentsTitle)
            }
            P({
                style {
                    color(FlagentTheme.TextLight)
                    fontSize(14.px)
                    marginTop(5.px)
                }
            }) {
                Text(LocalizedStrings.experimentsSubtitle)
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
                Icon("science", size = 48.px, color = FlagentTheme.TextLight)
                P({
                    style {
                        marginTop(16.px)
                        color(FlagentTheme.Text)
                        fontSize(16.px)
                    }
                }) {
                    Text(LocalizedStrings.noExperiments)
                }
                P({
                    style {
                        color(FlagentTheme.TextLight)
                        fontSize(14.px)
                        marginTop(8.px)
                    }
                }) {
                    Text(LocalizedStrings.noExperimentsHint)
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
                    Text(LocalizedStrings.viewFlags)
                }
            }
        } else {
            Div({
                style {
                    display(DisplayStyle.Grid)
                    property("grid-template-columns", "repeat(auto-fill, minmax(320px, 1fr))")
                    gap(20.px)
                }
            }) {
                flags.forEach { flag ->
                    ExperimentCard(flag)
                }
            }
        }
    }
}

@Composable
private fun ExperimentCard(flag: FlagResponse) {
    Div({
        style {
            backgroundColor(FlagentTheme.Background)
            borderRadius(8.px)
            padding(20.px)
            property("box-shadow", "0 2px 8px rgba(0,0,0,0.1)")
            property("transition", "box-shadow 0.2s")
        }
        onMouseEnter {
            (it.target as org.w3c.dom.HTMLElement).style.boxShadow = "0 4px 16px rgba(0,0,0,0.12)"
        }
        onMouseLeave {
            (it.target as org.w3c.dom.HTMLElement).style.boxShadow = "0 2px 8px rgba(0,0,0,0.1)"
        }
    }) {
        Div({
            style {
                display(DisplayStyle.Flex)
                justifyContent(JustifyContent.SpaceBetween)
                alignItems(AlignItems.FlexStart)
                marginBottom(12.px)
            }
        }) {
            Div({
                style {
                    flex(1)
                    property("min-width", "0")
                }
            }) {
                A(href = Route.FlagDetail(flag.id).path(), attrs = {
                    style {
                        fontSize(18.px)
                        fontWeight("600")
                        color(FlagentTheme.Primary)
                        textDecoration("none")
                    }
                    onClick { e ->
                        e.preventDefault()
                        Router.navigateTo(Route.FlagDetail(flag.id))
                    }
                }) {
                    Text(flag.key.ifBlank { "Flag #${flag.id}" })
                }
                if (flag.description.isNotBlank()) {
                    P({
                        style {
                            fontSize(14.px)
                            color(FlagentTheme.TextLight)
                            marginTop(4.px)
                            property("overflow", "hidden")
                            property("text-overflow", "ellipsis")
                            property("white-space", "nowrap")
                        }
                    }) {
                        Text(flag.description)
                    }
                }
            }
            Span({
                style {
                    fontSize(12.px)
                    padding(4.px, 8.px)
                    borderRadius(6.px)
                    backgroundColor(if (flag.enabled) Color("#D1FAE5") else FlagentTheme.BackgroundDark)
                    color(if (flag.enabled) Color("#065F46") else FlagentTheme.TextLight)
                }
            }) {
                Text(if (flag.enabled) LocalizedStrings.enabled else LocalizedStrings.disabled)
            }
        }

        Div({
            style {
                marginBottom(16.px)
            }
        }) {
            Span({
                style {
                    fontSize(12.px)
                    color(FlagentTheme.TextLight)
                    fontWeight("600")
                }
            }) {
                Text("${LocalizedStrings.variants}: ")
            }
            Span({
                style {
                    fontSize(14.px)
                    color(FlagentTheme.Text)
                }
            }) {
                Text(flag.variants.joinToString(", ") { it.key })
            }
        }

        Div({
            style {
                display(DisplayStyle.Flex)
                gap(10.px)
                flexWrap(FlexWrap.Wrap)
            }
        }) {
            Button({
                style {
                    display(DisplayStyle.Flex)
                    alignItems(AlignItems.Center)
                    gap(6.px)
                    padding(8.px, 14.px)
                    backgroundColor(FlagentTheme.Primary)
                    color(FlagentTheme.Background)
                    border(0.px)
                    borderRadius(6.px)
                    cursor("pointer")
                    fontSize(13.px)
                }
                onClick { Router.navigateTo(Route.FlagMetrics(flag.id)) }
            }) {
                Icon("bar_chart", size = 16.px, color = FlagentTheme.Background)
                Text(LocalizedStrings.viewMetrics)
            }
            Button({
                style {
                    display(DisplayStyle.Flex)
                    alignItems(AlignItems.Center)
                    gap(6.px)
                    padding(8.px, 14.px)
                    backgroundColor(FlagentTheme.BackgroundAlt)
                    color(FlagentTheme.Text)
                    border(1.px, LineStyle.Solid, FlagentTheme.Border)
                    borderRadius(6.px)
                    cursor("pointer")
                    fontSize(13.px)
                }
                onClick { Router.navigateTo(Route.FlagDetail(flag.id)) }
            }) {
                Icon("settings", size = 16.px)
                Text(LocalizedStrings.edit)
            }
        }
    }
}
