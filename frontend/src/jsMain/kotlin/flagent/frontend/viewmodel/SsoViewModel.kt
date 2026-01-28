package flagent.frontend.viewmodel

import androidx.compose.runtime.*
import flagent.frontend.api.ApiClient
import flagent.frontend.api.SsoProviderResponse
import flagent.frontend.i18n.LocalizedStrings
import flagent.frontend.util.AppError
import flagent.frontend.util.AppLogger
import flagent.frontend.util.ErrorHandler
import io.ktor.client.plugins.ResponseException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ViewModel for SSO providers (Enterprise)
 */
class SsoViewModel {
    private val scope = CoroutineScope(Dispatchers.Main)
    private val TAG = "SsoViewModel"
    
    var providers by mutableStateOf<List<SsoProviderResponse>>(emptyList())
        private set
    
    var isLoading by mutableStateOf(false)
        private set
    
    var error by mutableStateOf<String?>(null)
        private set
    
    var errorHint by mutableStateOf<String?>(null)
        private set
    
    fun loadProviders() {
        scope.launch {
            isLoading = true
            error = null
            errorHint = null
            ErrorHandler.withErrorHandling(
                block = {
                    AppLogger.info(TAG, "Loading SSO providers")
                    providers = ApiClient.getSsoProviders()
                },
                onError = { err ->
                    error = ErrorHandler.getUserMessage(err)
                    val status = (err.cause as? ResponseException)?.response?.status?.value
                    errorHint = when {
                        status == 404 -> LocalizedStrings.ssoHint404
                        status == 502 || status == 503 -> LocalizedStrings.ssoHint502
                        err is AppError.UnknownError || err is AppError.NotFound ->
                            LocalizedStrings.ssoErrorHint
                        else -> null
                    }
                    providers = emptyList()
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
