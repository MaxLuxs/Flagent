package flagent.frontend.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import flagent.api.model.CreateConstraintRequest
import flagent.api.model.PutConstraintRequest
import flagent.frontend.api.ApiClient
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
    val apiClient = remember { ApiClient("http://localhost:18000") }
    val property = remember { mutableStateOf("") }
    val operator = remember { mutableStateOf("EQ") }
    val value = remember { mutableStateOf("") }
    val loading = remember { mutableStateOf(constraintId != null) }
    val saving = remember { mutableStateOf(false) }
    val error = remember { mutableStateOf<String?>(null) }

    val operators = listOf(
        "EQ",
        "NEQ",
        "GT",
        "GTE",
        "LT",
        "LTE",
        "IN",
        "NOT_IN",
        "CONTAINS",
        "NOT_CONTAINS",
        "STARTS_WITH",
        "ENDS_WITH"
    )

    LaunchedEffect(constraintId) {
        if (constraintId != null) {
            loading.value = true
            error.value = null
            try {
                val constraints = apiClient.getConstraints(flagId, segmentId)
                val constraint = constraints.find { it.id == constraintId }
                if (constraint != null) {
                    property.value = constraint.property
                    operator.value = constraint.operator
                    value.value = constraint.value
                } else {
                    error.value = "Constraint not found"
                }
            } catch (e: Exception) {
                error.value = e.message ?: "Failed to load constraint"
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
            Text(if (constraintId != null) "Edit Constraint" else "Create Constraint")
            InfoTooltip(
                title = "Constraints",
                description = "Constraints define targeting rules for segments. They check if an entity's properties match specific conditions. Only entities that match all constraints in a segment will be assigned variants from that segment.",
                details = "Operators: EQ (equals), NEQ (not equals), GT/LT (greater/less than), IN/NOT_IN (in list), CONTAINS (string contains), STARTS_WITH/ENDS_WITH (string matching)."
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
                Text("Error: ${error.value}")
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
                    Text("Property:")
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
                    Text("Operator:")
                    Select(attrs = {
                        attr("value", operator.value)
                        onChange { event ->
                            val selected = (event.target as org.w3c.dom.HTMLSelectElement).value
                            operator.value = selected
                        }
                        style {
                            width(100.percent)
                            padding(5.px)
                        }
                    }) {
                        operators.forEach { op ->
                            Option(value = op) {
                                Text(op)
                            }
                        }
                    }
                }

                Label {
                    Text("Value:")
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
                            error.value = "Property and Value are required"
                            return@onClick
                        }

                        saving.value = true
                        error.value = null

                        CoroutineScope(Dispatchers.Main).launch {
                            try {
                                if (constraintId != null) {
                                    apiClient.updateConstraint(
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
                                    apiClient.createConstraint(
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
                                error.value = e.message ?: "Failed to save constraint"
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
                    Text(if (saving.value) "Saving..." else if (constraintId != null) "Update Constraint" else "Create Constraint")
                }
            }
        }
    }
}
