package flagent.frontend.components.segments

import androidx.compose.runtime.*
import flagent.api.model.FlagResponse
import flagent.api.model.SegmentResponse
import flagent.frontend.api.ApiClient
import flagent.frontend.components.Icon
import flagent.frontend.components.common.PageHeader
import flagent.frontend.i18n.LocalizedStrings
import flagent.frontend.navigation.Route
import flagent.frontend.navigation.Router
import flagent.frontend.state.LocalThemeMode
import flagent.frontend.theme.FlagentTheme
import flagent.frontend.util.ErrorHandler
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

/**
 * Data class for a segment with its parent flag info (for list display).
 */
data class SegmentWithFlag(
    val segment: SegmentResponse,
    val flag: FlagResponse
)

/**
 * Segments page: list all segments across all flags with search and filters.
 * Segments are loaded via flags with preload (segments included in FlagResponse).
 */
@Composable
fun SegmentsPage() {
    val themeMode = LocalThemeMode.current
    var flags by remember { mutableStateOf<List<FlagResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        isLoading = true
        error = null
        ErrorHandler.withErrorHandling(
            block = {
                val (all, _) = ApiClient.getFlags(preload = true)
                flags = all
            },
            onError = { err ->
                error = ErrorHandler.getUserMessage(err)
            }
        )
        isLoading = false
    }

    val segmentsWithFlags: List<SegmentWithFlag> = remember(flags, searchQuery) {
        val list = flags.flatMap { flag ->
            flag.segments.map { seg -> SegmentWithFlag(seg, flag) }
        }
        val q = searchQuery.trim().lowercase()
        if (q.isEmpty()) list
        else list.filter {
            it.segment.description?.lowercase()?.contains(q) == true ||
                it.flag.key.lowercase().contains(q) ||
                it.segment.id.toString().contains(q)
        }
    }

    Div({
        style {
            padding(0.px)
        }
    }) {
        PageHeader(
            title = LocalizedStrings.segmentsTitle,
            subtitle = LocalizedStrings.segmentsSubtitle
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
                            Text(LocalizedStrings.createFirstTenant)
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
                            Text(LocalizedStrings.logInAdmin)
                        }
                    }
                }
            }
        } else if (segmentsWithFlags.isEmpty()) {
            Div({
                style {
                    backgroundColor(FlagentTheme.cardBg(themeMode))
                    borderRadius(8.px)
                    padding(40.px)
                    property("box-shadow", FlagentTheme.ShadowCard)
                    textAlign("center")
                }
            }) {
                Icon("segment", size = 48.px, color = FlagentTheme.textLight(themeMode))
                P({
                    style {
                        marginTop(16.px)
                        color(FlagentTheme.text(themeMode))
                        fontSize(16.px)
                    }
                }) {
                    Text(
                        if (searchQuery.isNotBlank())
                            LocalizedStrings.noSegmentsMatchSearch
                        else
                            LocalizedStrings.noSegmentsInProject
                    )
                }
                if (searchQuery.isNotBlank()) {
                    Button({
                        style {
                            marginTop(12.px)
                            padding(8.px, 16.px)
                            backgroundColor(FlagentTheme.inputBg(themeMode))
                            color(FlagentTheme.text(themeMode))
                            border(0.px)
                            borderRadius(6.px)
                            cursor("pointer")
                            fontSize(14.px)
                        }
                        onClick { searchQuery = "" }
                    }) {
                        Text(LocalizedStrings.clearSearch)
                    }
                } else {
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
            }
        } else {
            Div({
                style {
                    marginBottom(16.px)
                    display(DisplayStyle.Flex)
                    gap(12.px)
                    flexWrap(FlexWrap.Wrap)
                    alignItems(AlignItems.Center)
                }
            }) {
                Div({
                    style {
                        display(DisplayStyle.Flex)
                        alignItems(AlignItems.Center)
                        gap(8.px)
                        flex(1)
                        property("min-width", "200px")
                    }
                }) {
                    Span({
                        classes("material-icons")
                        style {
                            fontSize(20.px)
                            color(FlagentTheme.textLight(themeMode))
                        }
                    }) { Text("search") }
                    Input(InputType.Text) {
                        value(searchQuery)
                        onInput { event -> searchQuery = event.value }
                        attr("placeholder", LocalizedStrings.searchSegmentsPlaceholder)
                        style {
                            flex(1)
                            padding(8.px, 12.px)
                            borderRadius(6.px)
                            border(1.px, LineStyle.Solid, FlagentTheme.inputBorder(themeMode))
                            backgroundColor(FlagentTheme.inputBg(themeMode))
                            color(FlagentTheme.text(themeMode))
                            fontSize(14.px)
                        }
                    }
                }
                Span({
                    style {
                        color(FlagentTheme.textLight(themeMode))
                        fontSize(14.px)
                    }
                }) {
                    Text("${segmentsWithFlags.size} ${LocalizedStrings.segments.lowercase()}")
                }
            }

            Div({
                style {
                    backgroundColor(FlagentTheme.cardBg(themeMode))
                    borderRadius(8.px)
                    overflow("hidden")
                    property("box-shadow", "0 1px 3px ${FlagentTheme.Shadow}")
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
                                backgroundColor(FlagentTheme.inputBg(themeMode))
                                property("border-bottom", "1px solid ${FlagentTheme.cardBorder(themeMode)}")
                            }
                        }) {
                            listOf(
                                LocalizedStrings.flagKey,
                                "ID",
                                LocalizedStrings.description,
                                LocalizedStrings.rolloutPercent,
                                LocalizedStrings.constraints,
                                LocalizedStrings.action
                            ).forEach { h ->
                                Th({
                                    style {
                                        padding(10.px, 12.px)
                                        textAlign("left")
                                        fontSize(12.px)
                                        fontWeight(600)
                                        color(FlagentTheme.textLight(themeMode))
                                        property("text-transform", "uppercase")
                                    }
                                }) { Text(h) }
                            }
                        }
                    }
                    Tbody {
                        segmentsWithFlags.forEach { (segment, flag) ->
                            Tr({
                                style {
                                    property("border-bottom", "1px solid ${FlagentTheme.cardBorder(themeMode)}")
                                    property("transition", "background-color 0.15s")
                                }
                                onMouseEnter { (it.target as org.w3c.dom.HTMLElement).style.backgroundColor = FlagentTheme.inputBg(themeMode).toString() }
                                onMouseLeave { (it.target as org.w3c.dom.HTMLElement).style.backgroundColor = "transparent" }
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
                                Td({ style { padding(10.px, 12.px); fontSize(14.px); color(FlagentTheme.textLight(themeMode)) } }) {
                                    Text(segment.id.toString())
                                }
                                Td({
                                    style {
                                        padding(10.px, 12.px)
                                        fontSize(14.px)
                                        property("max-width", "240px")
                                        property("overflow", "hidden")
                                        property("text-overflow", "ellipsis")
                                        property("white-space", "nowrap")
                                    }
                                }) {
                                    Text(segment.description?.ifBlank { "—" } ?: "—")
                                }
                                Td({ style { padding(10.px, 12.px); fontSize(14.px) } }) {
                                    Text("${segment.rolloutPercent}%")
                                }
                                Td({ style { padding(10.px, 12.px); fontSize(14.px) } }) {
                                    Text(segment.constraints.size.toString())
                                }
                                Td({
                                    style { padding(10.px, 12.px) }
                                }) {
                                    Button({
                                        style {
                                            padding(6.px, 10.px)
                                            backgroundColor(FlagentTheme.inputBg(themeMode))
                                            color(FlagentTheme.text(themeMode))
                                            border(1.px, LineStyle.Solid, FlagentTheme.cardBorder(themeMode))
                                            borderRadius(6.px)
                                            cursor("pointer")
                                            fontSize(12.px)
                                        }
                                        onClick { Router.navigateTo(Route.FlagDetail(flag.id)) }
                                    }) {
                                        Icon("settings", size = 14.px)
                                        Text(" ${LocalizedStrings.edit}")
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
