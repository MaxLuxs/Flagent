package flagent.frontend.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import flagent.api.model.CreateSegmentRequest
import flagent.api.model.PutSegmentRequest
import flagent.frontend.api.ApiClient
import flagent.frontend.i18n.LocalizedStrings
import flagent.frontend.navigation.Router
import flagent.frontend.state.LocalThemeMode
import flagent.frontend.theme.FlagentTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.FlexDirection
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.backgroundColor
import org.jetbrains.compose.web.css.border
import org.jetbrains.compose.web.css.borderRadius
import org.jetbrains.compose.web.css.color
import org.jetbrains.compose.web.css.cursor
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.flexDirection
import org.jetbrains.compose.web.css.gap
import org.jetbrains.compose.web.css.padding
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.style
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H3
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Label
import org.jetbrains.compose.web.dom.Text

/**
 * SegmentEditor component - create/edit segments
 */
@Composable
fun SegmentEditor(flagId: Int, segmentId: Int? = null) {
    val themeMode = LocalThemeMode.current
    val description = remember { mutableStateOf("") }
    val rolloutPercent = remember { mutableStateOf(100) }
    val loading = remember { mutableStateOf(segmentId != null) }
    val saving = remember { mutableStateOf(false) }
    val error = remember { mutableStateOf<String?>(null) }

    LaunchedEffect(segmentId) {
        if (segmentId != null) {
            loading.value = true
            error.value = null
            try {
                val segments = ApiClient.getSegments(flagId)
                val segment = segments.find { it.id == segmentId }
                if (segment != null) {
                    description.value = segment.description ?: ""
                    rolloutPercent.value = segment.rolloutPercent
                } else {
                    error.value = LocalizedStrings.segmentNotFound
                }
            } catch (e: Exception) {
                error.value = e.message ?: LocalizedStrings.failedToLoadSegment
            } finally {
                loading.value = false
            }
        }
    }

    Div({
        style {
            padding(20.px)
            border {
                width(1.px)
                style(LineStyle.Solid)
                color(FlagentTheme.inputBorder(themeMode))
            }
            borderRadius(5.px)
        }
    }) {
        H3 { 
            Text(if (segmentId != null) LocalizedStrings.editSegment else LocalizedStrings.createSegment)
            InfoTooltip(
                title = LocalizedStrings.segmentEditorTitle,
                description = LocalizedStrings.segmentsTooltipDescription,
                details = LocalizedStrings.segmentsTooltipDetails
            )
        }

        if (loading.value) {
            Spinner()
        } else if (error.value != null) {
            Div({
                style {
                    color(FlagentTheme.Error)
                    padding(10.px)
                    property("background-color", "rgba(239, 68, 68, 0.1)")
                    borderRadius(5.px)
                }
            }) {
                Text("${LocalizedStrings.error}: ${error.value}")
            }
        } else {
            Div({
                style {
                    display(DisplayStyle.Flex)
                    flexDirection(FlexDirection.Column)
                    gap(10.px)
                }
            }) {
                Label {
                    Text("${LocalizedStrings.description}:")
                    Input(InputType.Text) {
                        value(description.value)
                        onInput { event -> description.value = event.value }
                        style {
                            width(100.percent)
                            padding(5.px)
                        }
                    }
                }

                Label {
                    Text("${LocalizedStrings.rolloutPercent}: ${rolloutPercent.value}%")
                    Input(InputType.Range) {
                        value(rolloutPercent.value.toString())
                        onInput { event ->
                            val inputValue = (event.target as org.w3c.dom.HTMLInputElement).value
                            rolloutPercent.value = inputValue.toIntOrNull() ?: 0
                        }
                        attr("min", "0")
                        attr("max", "100")
                        style {
                            width(100.percent)
                        }
                    }
                }

                Button({
                    onClick {
                        if (description.value.isBlank()) {
                            error.value = LocalizedStrings.descriptionIsRequired
                            return@onClick
                        }

                        saving.value = true
                        error.value = null

                        CoroutineScope(Dispatchers.Main).launch {
                            try {
                                if (segmentId != null) {
                                    ApiClient.updateSegment(
                                        flagId,
                                        segmentId,
                                        PutSegmentRequest(
                                            description = description.value,
                                            rolloutPercent = rolloutPercent.value
                                        )
                                    )
                                } else {
                                    ApiClient.createSegment(
                                        flagId,
                                        CreateSegmentRequest(
                                            description = description.value,
                                            rolloutPercent = rolloutPercent.value
                                        )
                                    )
                                }
                                Router.navigateBack()
                            } catch (e: Exception) {
                                error.value = e.message ?: LocalizedStrings.failedToSaveSegment
                            } finally {
                                saving.value = false
                            }
                        }
                    }
                    if (saving.value || description.value.isBlank()) {
                        attr("disabled", "true")
                    }
                    style {
                        padding(10.px)
                        backgroundColor(
                            if (saving.value || description.value.isBlank()) FlagentTheme.NeutralLighter
                            else FlagentTheme.Primary
                        )
                        color(Color.white)
                        border {
                            width(0.px)
                            style(LineStyle.None)
                        }
                        borderRadius(5.px)
                        cursor(if (saving.value || description.value.isBlank()) "not-allowed" else "pointer")
                    }
                }) {
                    Text(
                        if (saving.value) LocalizedStrings.saving
                        else if (segmentId != null) LocalizedStrings.updateSegment
                        else LocalizedStrings.createSegment
                    )
                }
            }
        }
    }
}
