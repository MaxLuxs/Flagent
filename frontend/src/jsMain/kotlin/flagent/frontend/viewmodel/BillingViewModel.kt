package flagent.frontend.viewmodel

import androidx.compose.runtime.*
import flagent.frontend.api.ApiClient
import flagent.frontend.api.CreateCheckoutSessionRequest
import flagent.frontend.api.CreatePortalSessionRequest
import flagent.frontend.api.SubscriptionResponse
import flagent.frontend.i18n.LocalizedStrings
import flagent.frontend.util.AppError
import flagent.frontend.util.AppLogger
import flagent.frontend.util.ErrorHandler
import io.ktor.client.plugins.ResponseException
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ViewModel for Billing (Enterprise)
 */
class BillingViewModel {
    private val scope = CoroutineScope(Dispatchers.Main)
    private val TAG = "BillingViewModel"
    
    var subscription by mutableStateOf<SubscriptionResponse?>(null)
        private set
    
    var isLoading by mutableStateOf(false)
        private set
    
    var error by mutableStateOf<String?>(null)
        private set
    
    var errorHint by mutableStateOf<String?>(null)
        private set
    
    fun loadSubscription() {
        scope.launch {
            isLoading = true
            error = null
            errorHint = null
            ErrorHandler.withErrorHandling(
                block = {
                    subscription = ApiClient.getBillingSubscription()
                },
                onError = { err ->
                    error = ErrorHandler.getUserMessage(err)
                    val status = (err.cause as? ResponseException)?.response?.status?.value
                    errorHint = when {
                        status == 404 -> LocalizedStrings.billingHint404
                        status == 502 || status == 503 -> LocalizedStrings.billingHint502
                        err is AppError.UnknownError || err is AppError.NotFound ->
                            LocalizedStrings.billingErrorHint
                        else -> null
                    }
                    subscription = null
                }
            )
            isLoading = false
        }
    }
    
    fun createCheckoutSession(priceId: String, onSuccess: (String) -> Unit = {}) {
        scope.launch {
            isLoading = true
            error = null
            val baseUrl = window.location.origin
            ErrorHandler.withErrorHandling(
                block = {
                    AppLogger.info(TAG, "Creating checkout session for price: $priceId")
                    val request = CreateCheckoutSessionRequest(
                        priceId = priceId,
                        successUrl = "$baseUrl/settings?billing=success",
                        cancelUrl = "$baseUrl/settings?billing=cancel"
                    )
                    val response = ApiClient.createBillingCheckout(request)
                    onSuccess(response.sessionUrl)
                    window.location.href = response.sessionUrl
                },
                onError = { err ->
                    error = ErrorHandler.getUserMessage(err)
                    val status = (err.cause as? ResponseException)?.response?.status?.value
                    errorHint = when {
                        status == 404 -> LocalizedStrings.billingHint404
                        status == 502 || status == 503 -> LocalizedStrings.billingHint502
                        err is AppError.UnknownError || err is AppError.NotFound ->
                            LocalizedStrings.billingErrorHint
                        else -> null
                    }
                }
            )
            isLoading = false
        }
    }
    
    fun createPortalSession(onSuccess: (String) -> Unit = {}) {
        scope.launch {
            isLoading = true
            error = null
            val baseUrl = window.location.origin
            ErrorHandler.withErrorHandling(
                block = {
                    AppLogger.info(TAG, "Creating billing portal session")
                    val request = CreatePortalSessionRequest(returnUrl = "$baseUrl/settings")
                    val response = ApiClient.createBillingPortal(request)
                    onSuccess(response.sessionUrl)
                    window.location.href = response.sessionUrl
                },
                onError = { err ->
                    error = ErrorHandler.getUserMessage(err)
                    val status = (err.cause as? ResponseException)?.response?.status?.value
                    errorHint = when {
                        status == 404 -> LocalizedStrings.billingHint404
                        status == 502 || status == 503 -> LocalizedStrings.billingHint502
                        err is AppError.UnknownError || err is AppError.NotFound ->
                            LocalizedStrings.billingErrorHint
                        else -> null
                    }
                }
            )
            isLoading = false
        }
    }
    
    fun clearError() {
        error = null
        errorHint = null
    }
}
