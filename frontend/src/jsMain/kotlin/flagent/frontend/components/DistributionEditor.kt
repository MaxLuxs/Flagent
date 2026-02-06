package flagent.frontend.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import flagent.frontend.api.ApiClient
import flagent.frontend.i18n.LocalizedStrings
import flagent.frontend.state.LocalThemeMode
import flagent.frontend.theme.FlagentTheme
import flagent.api.model.DistributionRequest
import flagent.api.model.PutDistributionsRequest
import flagent.api.model.VariantResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

/**
 * DistributionEditor component - manage variant distributions
 */
@Composable
fun DistributionEditor(flagId: Int, segmentId: Int, variants: List<VariantResponse>) {
    val themeMode = LocalThemeMode.current
    val distributions = remember { mutableStateMapOf<Int, Int>() }
    val loading = remember { mutableStateOf(true) }
    val saving = remember { mutableStateOf(false) }
    val error = remember { mutableStateOf<String?>(null) }
    val success = remember { mutableStateOf(false) }
    val refreshTrigger = remember { mutableStateOf(0) }
    
    LaunchedEffect(segmentId, refreshTrigger.value) {
        loading.value = true
        error.value = null
        try {
            val existingDistributions = ApiClient.getDistributions(flagId, segmentId)
            distributions.clear()
            existingDistributions.forEach { dist ->
                distributions[dist.variantID] = dist.percent
            }
        } catch (e: Exception) {
            error.value = e.message ?: LocalizedStrings.failedToLoadDistributions
        } finally {
            loading.value = false
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
            Text(LocalizedStrings.editDistribution)
            InfoTooltip(
                title = LocalizedStrings.distributionsTooltipTitle,
                description = LocalizedStrings.distributionsTooltipDescription,
                details = LocalizedStrings.distributionsTooltipDetails
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
            variants.forEach { variant ->
                Div({
                    style {
                        marginBottom(20.px)
                    }
                }) {
                    Slider(
                        value = distributions[variant.id] ?: 0,
                        onValueChange = { newValue ->
                            distributions[variant.id] = newValue
                        },
                        min = 0,
                        max = 100,
                        step = 1,
                        label = variant.key,
                        showValue = true,
                        disabled = false
                    )
                }
            }
            
            val total = distributions.values.sum()
            if (total != 100) {
                Div({
                    style {
                        color(FlagentTheme.Error)
                        padding(10.px)
                        property("background-color", "rgba(239, 68, 68, 0.1)")
                        borderRadius(5.px)
                        marginBottom(10.px)
                    }
                }) {
                    Text(LocalizedStrings.percentagesMustAddUpTo100(total))
                }
            }
            
            if (success.value) {
                Div({
                    style {
                        color(FlagentTheme.Success)
                        padding(10.px)
                        property("background-color", "rgba(16, 185, 129, 0.1)")
                        borderRadius(5.px)
                        marginBottom(10.px)
                        border {
                            width(1.px)
                            style(LineStyle.Solid)
                            color(Color("rgba(16, 185, 129, 0.2)"))
                        }
                    }
                }) {
                    Text(LocalizedStrings.distributionsSavedSuccessfully)
                }
            }
            
            Button({
                onClick {
                    saving.value = true
                    error.value = null
                    
                    CoroutineScope(Dispatchers.Main).launch {
                        try {
                            val distributionRequests = distributions.map { entry ->
                                val variantId = entry.key
                                val percent = entry.value
                                DistributionRequest(
                                    variantID = variantId,
                                    percent = percent
                                )
                            }
                            
                            ApiClient.updateDistributions(
                                flagId,
                                segmentId,
                                PutDistributionsRequest(distributionRequests)
                            )
                            success.value = true
                            refreshTrigger.value++
                            // Clear success message after 2 seconds
                            delay(2000)
                            success.value = false
                        } catch (e: Exception) {
                            error.value = e.message ?: LocalizedStrings.failedToSaveDistributions
                        } finally {
                            saving.value = false
                        }
                    }
                }
                if (saving.value || total != 100) {
                    attr("disabled", "true")
                }
                style {
                    padding(10.px)
                    backgroundColor(if (saving.value || total != 100) FlagentTheme.NeutralLighter else FlagentTheme.Primary)
                    color(Color.white)
                    border {
                        width(0.px)
                        style(LineStyle.None)
                    }
                    borderRadius(5.px)
                    cursor(if (saving.value || total != 100) "not-allowed" else "pointer")
                }
            }) {
                Text(if (saving.value) LocalizedStrings.saving else LocalizedStrings.saveDistribution)
            }
        }
    }
}
