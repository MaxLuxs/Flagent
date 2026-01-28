package flagent.frontend.viewmodel

import androidx.compose.runtime.*
import flagent.frontend.api.*
import flagent.frontend.util.AppLogger
import flagent.frontend.util.ErrorHandler
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AnomalyViewModel(private val flagId: Int? = null) {
    private val scope = CoroutineScope(Dispatchers.Main)
    private val TAG = "AnomalyViewModel"
    
    var config by mutableStateOf<AnomalyDetectionConfig?>(null)
        private set
    
    var alerts by mutableStateOf<List<AnomalyAlertResponse>>(emptyList())
        private set
    
    var isLoading by mutableStateOf(false)
        private set
    
    var error by mutableStateOf<String?>(null)
        private set
    
    fun loadConfig() {
        if (flagId == null) return
        
        scope.launch {
            isLoading = true
            error = null
            
            ErrorHandler.withErrorHandling(
                block = {
                    AppLogger.info(TAG, "Loading anomaly config for flag: $flagId")
                    val client = ApiClient.client
                    config = client.get(ApiClient.getApiPath("/anomaly/config/$flagId")).body()
                },
                onError = { err ->
                    error = ErrorHandler.getUserMessage(err)
                }
            )
            
            isLoading = false
        }
    }
    
    fun saveConfig(request: AnomalyDetectionConfigRequest, onSuccess: () -> Unit = {}) {
        scope.launch {
            ErrorHandler.withErrorHandling(
                block = {
                    AppLogger.info(TAG, "Saving anomaly config")
                    val client = ApiClient.client
                    val result: AnomalyDetectionConfig = client.post(ApiClient.getApiPath("/anomaly/config")) {
                        contentType(ContentType.Application.Json)
                        setBody(request)
                    }.body()
                    config = result
                    onSuccess()
                },
                onError = { err ->
                    error = ErrorHandler.getUserMessage(err)
                }
            )
        }
    }
    
    fun detectAnomalies(onSuccess: () -> Unit = {}) {
        if (flagId == null) return
        
        scope.launch {
            ErrorHandler.withErrorHandling(
                block = {
                    AppLogger.info(TAG, "Detecting anomalies for flag: $flagId")
                    val client = ApiClient.client
                    client.post(ApiClient.getApiPath("/anomaly/detect/$flagId"))
                    loadAlerts()
                    onSuccess()
                },
                onError = { err ->
                    error = ErrorHandler.getUserMessage(err)
                }
            )
        }
    }
    
    fun loadAlerts() {
        if (flagId == null) {
            loadUnresolvedAlerts()
            return
        }
        
        scope.launch {
            ErrorHandler.withErrorHandling(
                block = {
                    val client = ApiClient.client
                    alerts = client.get(ApiClient.getApiPath("/anomaly/alerts/$flagId")).body()
                },
                onError = { err ->
                    error = ErrorHandler.getUserMessage(err)
                }
            )
        }
    }
    
    fun loadUnresolvedAlerts() {
        scope.launch {
            ErrorHandler.withErrorHandling(
                block = {
                    val client = ApiClient.client
                    alerts = client.get(ApiClient.getApiPath("/anomaly/alerts/unresolved")).body()
                },
                onError = { err ->
                    error = ErrorHandler.getUserMessage(err)
                }
            )
        }
    }
    
    fun resolveAlert(alertId: Int, onSuccess: () -> Unit = {}) {
        scope.launch {
            ErrorHandler.withErrorHandling(
                block = {
                    val client = ApiClient.client
                    val result: AnomalyAlertResponse = client.post(ApiClient.getApiPath("/anomaly/alerts/$alertId/resolve")).body()
                    alerts = alerts.map { if (it.id == alertId) result else it }
                    onSuccess()
                },
                onError = { err ->
                    error = ErrorHandler.getUserMessage(err)
                }
            )
        }
    }
    
    fun clearError() {
        error = null
    }
}
