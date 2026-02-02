package flagent.frontend.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import flagent.api.constants.ConstraintOperators
import flagent.api.model.CreateConstraintRequest
import flagent.api.model.PutConstraintRequest
import flagent.frontend.api.ApiClient
import flagent.frontend.i18n.LocalizedStrings
import flagent.frontend.navigation.Router
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
import org.jetbrains.compose.web.dom.Option
import org.jetbrains.compose.web.dom.Select
import org.jetbrains.compose.web.dom.Text

/**
 * ConstraintEditor component - create/edit constraints
 */
@Composable
fun ConstraintEditor(flagId: Int, segmentId: Int, constraintId: Int? = null) {
    val property = remember { mutableStateOf("") }
    val operator = remember { mutableStateOf("EQ") }
    val value = remember { mutableStateOf("") }
    val loading = remember { mutableStateOf(constraintId != null) }
    val saving = remember { mutableStateOf(false) }
    val error = remember { mutableStateOf<String?>(null) }

    val operators = ConstraintOperators.OPERATOR_DISPLAY_PAIRS

    LaunchedEffect(constraintId) {
        if (constraintId != null) {
            loading.value = true
            error.value = null
            try {
                val constraints = ApiClient.getConstraints(flagId, segmentId)
                val constraint = constraints.find { it.id == constraintId }
                if (constraint != null) {
                    property.value = constraint.property
                    operator.value = constraint.operator
                    value.value = constraint.value
                } else {
                    error.value = LocalizedStrings.constraintNotFound
                }
            } catch (e: Exception) {
                error.value = e.message ?: LocalizedStrings.failedToLoadConstraint
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
                color(FlagentTheme.Border)
            }
            borderRadius(5.px)
        }
    }) {
        H3 {
            Text(if (constraintId != null) LocalizedStrings.editConstraint else LocalizedStrings.createConstraint)
            InfoTooltip(
                title = LocalizedStrings.constraintsTooltipTitle,
                description = LocalizedStrings.constraintsTooltipDescription,
                details = LocalizedStrings.constraintsTooltipDetails
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
                    Text("${LocalizedStrings.property}:")
                    Input(InputType.Text) {
                        value(property.value)
                        onInput { event -> property.value = event.value }
                        style {
                            width(100.percent)
                            padding(5.px)
                        }
                    }
                }

                Label {
                    Text("${LocalizedStrings.operator}:")
                    Select(attrs = {
                        attr("value", operator.value)
                        onChange { event ->
                            val selected = event.target.value
                            operator.value = selected
                        }
                        style {
                            width(100.percent)
                            padding(5.px)
                        }
                    }) {
                        operators.forEach { (opValue, display) ->
                            Option(value = opValue) {
                                Text(display)
                            }
                        }
                    }
                }

                Label {
                    Text("${LocalizedStrings.value}:")
                    Input(InputType.Text) {
                        value(value.value)
                        onInput { event -> value.value = event.value }
                        style {
                            width(100.percent)
                            padding(5.px)
                        }
                    }
                }

                Button({
                    onClick {
                        if (property.value.isBlank() || value.value.isBlank()) {
                            error.value = LocalizedStrings.propertyAndValueAreRequired
                            return@onClick
                        }

                        saving.value = true
                        error.value = null

                        CoroutineScope(Dispatchers.Main).launch {
                            try {
                                if (constraintId != null) {
                                    ApiClient.updateConstraint(
                                        flagId,
                                        segmentId,
                                        constraintId,
                                        PutConstraintRequest(
                                            property = property.value,
                                            operator = operator.value,
                                            value = value.value
                                        )
                                    )
                                } else {
                                    ApiClient.createConstraint(
                                        flagId,
                                        segmentId,
                                        CreateConstraintRequest(
                                            property = property.value,
                                            operator = operator.value,
                                            value = value.value
                                        )
                                    )
                                }
                                Router.navigateBack()
                            } catch (e: Exception) {
                                error.value = e.message ?: LocalizedStrings.failedToSaveConstraint
                            } finally {
                                saving.value = false
                            }
                        }
                    }
                    if (saving.value || property.value.isBlank() || value.value.isBlank()) {
                        attr("disabled", "true")
                    }
                    style {
                        padding(10.px)
                        backgroundColor(
                            if (saving.value || property.value.isBlank() || value.value.isBlank()) FlagentTheme.NeutralLighter
                            else FlagentTheme.Primary
                        )
                        color(FlagentTheme.Background)
                        border {
                            width(0.px)
                            style(LineStyle.None)
                        }
                        borderRadius(5.px)
                        cursor(if (saving.value || property.value.isBlank() || value.value.isBlank()) "not-allowed" else "pointer")
                    }
                }) {
                    Text(
                        if (saving.value) LocalizedStrings.saving
                        else if (constraintId != null) LocalizedStrings.updateConstraint
                        else LocalizedStrings.createConstraint
                    )
                }
            }
        }
    }
}
