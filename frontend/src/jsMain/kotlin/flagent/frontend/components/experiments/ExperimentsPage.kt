package flagent.frontend.components.experiments

import androidx.compose.runtime.*
import flagent.api.model.FlagResponse
import flagent.frontend.api.ApiClient
import flagent.frontend.components.Icon
import flagent.frontend.components.common.PageHeader
import flagent.frontend.config.AppConfig
import flagent.frontend.i18n.LocalizedStrings
import flagent.frontend.navigation.Route
import flagent.frontend.navigation.Router
import flagent.frontend.theme.FlagentTheme
import flagent.frontend.util.ErrorHandler
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

/**
 * Experiments (A/B) page: flags with 2+ variants. Compact table layout.
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
                val (all, _) = ApiClient.getFlags()
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
            padding(0.px)
        }
    }) {
        PageHeader(
            title = LocalizedStrings.experimentsTitle,
            subtitle = LocalizedStrings.experimentsSubtitle
        )

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
                    color(Color.white)
                }
            }) {
                Text(error!!)
                if (error!!.contains("tenant", ignoreCase = true) || error!!.contains("Create tenant", ignoreCase = true) || error!!.contains("X-API-Key", ignoreCase = true)) {
                    SideEffect { flagent.frontend.state.BackendOnboardingState.setBackendNeedsTenantOrAuth() }
                    P({
                        style {
                            marginTop(10.px)
                            fontSize(14.px)
                            display(DisplayStyle.Flex)
                            gap(12.px)
                            flexWrap(FlexWrap.Wrap)
                        }
                    }) {
                        Button({
                            style {
                                color(Color.white)
                                textDecoration("underline")
                                fontWeight("600")
                                backgroundColor(Color("transparent"))
                                border(0.px)
                                cursor("pointer")
                                padding(0.px)
                                fontSize(14.px)
                            }
                            onClick { Router.navigateToTenantsWithCreate() }
                        }) {
                            Text("Create first tenant →")
                        }
                        Button({
                            style {
                                color(Color.white)
                                textDecoration("underline")
                                fontWeight("600")
                                backgroundColor(Color("transparent"))
                                border(0.px)
                                cursor("pointer")
                                padding(0.px)
                                fontSize(14.px)
                            }
                            onClick { Router.navigateTo(Route.Login) }
                        }) {
                            Text("Log in (admin) →")
                        }
                    }
                }
            }
        } else if (flags.isEmpty()) {
            Div({
                style {
                    backgroundColor(FlagentTheme.WorkspaceCardBg)
                    borderRadius(8.px)
                    padding(40.px)
                    property("box-shadow", "0 2px 8px rgba(0,0,0,0.1)")
                    textAlign("center")
                }
            }) {
                Icon("science", size = 48.px, color = FlagentTheme.WorkspaceTextLight)
                P({
                    style {
                        marginTop(16.px)
                        color(FlagentTheme.WorkspaceText)
                        fontSize(16.px)
                    }
                }) {
                    Text(LocalizedStrings.noExperiments)
                }
                P({
                    style {
                        color(FlagentTheme.WorkspaceTextLight)
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
                        color(Color.white)
                        border(0.px)
                        borderRadius(6.px)
                        cursor("pointer")
                        fontSize(14.px)
                    }
                    onClick { Router.navigateTo(Route.FlagsList) }
                }) {
                    Text(LocalizedStrings.viewFlags)
                }
            }
        } else {
            Div({
                style {
                    backgroundColor(FlagentTheme.WorkspaceCardBg)
                    borderRadius(8.px)
                    overflow("hidden")
                    property("box-shadow", "0 1px 3px rgba(0, 0, 0, 0.08)")
                }
            }) {
                Table({
                    style {
                        width(100.percent)
                        property("border-collapse", "collapse")
                    }
                }) {
                    Thead {
                        Tr({
                            style {
                                backgroundColor(FlagentTheme.WorkspaceInputBg)
                                property("border-bottom", "1px solid ${FlagentTheme.WorkspaceCardBorder}")
                            }
                        }) {
                            listOf(LocalizedStrings.flagKey, LocalizedStrings.description, LocalizedStrings.variants, LocalizedStrings.status, LocalizedStrings.action).forEach { h ->
                                Th({
                                    style {
                                        padding(10.px, 12.px)
                                        textAlign("left")
                                        fontSize(12.px)
                                        fontWeight(600)
                                        color(FlagentTheme.WorkspaceTextLight)
                                        property("text-transform", "uppercase")
                                    }
                                }) { Text(h) }
                            }
                        }
                    }
                    Tbody {
                        flags.forEach { flag ->
                            Tr({
                                style {
                                    property("border-bottom", "1px solid ${FlagentTheme.WorkspaceCardBorder}")
                                    property("transition", "background-color 0.15s")
                                }
                                onMouseEnter { (it.target as org.w3c.dom.HTMLElement).style.backgroundColor = FlagentTheme.WorkspaceInputBg.toString() }
                                onMouseLeave { (it.target as org.w3c.dom.HTMLElement).style.backgroundColor = "transparent"
                                }
                            }) {
                                Td({
                                    style {
                                        padding(10.px, 12.px)
                                        fontSize(14.px)
                                        color(FlagentTheme.Primary)
                                        cursor("pointer")
                                    }
                                    onClick { Router.navigateTo(Route.FlagDetail(flag.id)) }
                                }) {
                                    Text(flag.key.ifBlank { "Flag #${flag.id}" })
                                }
                                Td({ style { padding(10.px, 12.px); fontSize(14.px); property("max-width", "200px"); property("overflow", "hidden"); property("text-overflow", "ellipsis"); property("white-space", "nowrap") } }) {
                                    Text(flag.description.ifBlank { "—" })
                                }
                                Td({ style { padding(10.px, 12.px); fontSize(14.px) } }) {
                                    Text(flag.variants.joinToString(", ") { it.key })
                                }
                                Td({ style { padding(10.px, 12.px); fontSize(12.px) } }) {
                                    Span({
                                        style {
                                            padding(4.px, 8.px)
                                            borderRadius(6.px)
                                            backgroundColor(if (flag.enabled) Color("#D1FAE5") else FlagentTheme.WorkspaceInputBg)
                                            color(if (flag.enabled) Color("#065F46") else FlagentTheme.WorkspaceTextLight)
                                        }
                                    }) { Text(if (flag.enabled) LocalizedStrings.enabled else LocalizedStrings.disabled) }
                                }
                                Td({
                                    style { padding(10.px, 12.px) }
                                }) {
                                    Div({
                                        style {
                                            display(DisplayStyle.Flex)
                                            gap(8.px)
                                            flexWrap(FlexWrap.Wrap)
                                        }
                                    }) {
                                        if (AppConfig.Features.enableMetrics) {
                                            Button({
                                                style {
                                                    padding(6.px, 10.px)
                                                    backgroundColor(FlagentTheme.Primary)
                                                    color(Color.white)
                                                    border(0.px)
                                                    borderRadius(6.px)
                                                    cursor("pointer")
                                                    fontSize(12.px)
                                                }
                                                onClick { Router.navigateTo(Route.FlagMetrics(flag.id)) }
                                            }) {
                                                Icon("bar_chart", size = 14.px, color = FlagentTheme.Background)
                                                Text(LocalizedStrings.viewMetrics)
                                            }
                                        }
                                        Button({
                                            style {
                                                padding(6.px, 10.px)
                                                backgroundColor(FlagentTheme.WorkspaceInputBg)
                                                color(FlagentTheme.WorkspaceText)
                                                border(1.px, LineStyle.Solid, FlagentTheme.WorkspaceCardBorder)
                                                borderRadius(6.px)
                                                cursor("pointer")
                                                fontSize(12.px)
                                            }
                                            onClick { Router.navigateTo(Route.FlagDetail(flag.id)) }
                                        }) {
                                            Icon("settings", size = 14.px)
                                            Text(LocalizedStrings.edit)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

