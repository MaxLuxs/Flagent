package flagent.frontend.viewmodel

import androidx.compose.runtime.*
import flagent.frontend.api.*
import flagent.frontend.util.AppLogger
import flagent.frontend.util.ErrorHandler
import io.ktor.client.call.*
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmartRolloutViewModel(private val flagId: Int) {
    private val scope = CoroutineScope(Dispatchers.Main)
    private val TAG = "SmartRolloutViewModel"
    
    var configs by mutableStateOf<List<SmartRolloutConfigResponse>>(emptyList())
        private set
    
    var history by mutableStateOf<List<RolloutHistoryEntry>>(emptyList())
        private set
    
    var isLoading by mutableStateOf(false)
        private set
    
    var error by mutableStateOf<String?>(null)
        private set
    
    fun loadConfigs() {
        scope.launch {
            isLoading = true
            error = null
            try {
                AppLogger.info(TAG, "Loading rollout configs for flag: $flagId")
                val client = ApiClient.client
                configs = client.get(ApiClient.getApiPath("/smart-rollout/flag/$flagId")).body()
                AppLogger.info(TAG, "Loaded ${configs.size} rollout configs")
            } catch (e: Throwable) {
                val status = (e as? ResponseException)?.response?.status
                if (status == HttpStatusCode.NotFound) {
                    // OSS backend has no smart-rollout; treat as empty
                    configs = emptyList()
                } else {
                    error = ErrorHandler.getUserMessage(ErrorHandler.handle(e, null))
                }
            }
            isLoading = false
        }
    }
    
    fun createConfig(request: SmartRolloutConfigRequest, onSuccess: () -> Unit = {}) {
        scope.launch {
            ErrorHandler.withErrorHandling(
                block = {
                    AppLogger.info(TAG, "Creating rollout config")
                    val client = ApiClient.client
                    val result: SmartRolloutConfigResponse = client.post(ApiClient.getApiPath("/smart-rollout")) {
                        contentType(ContentType.Application.Json)
                        setBody(request)
                    }.body()
                    configs = configs + result
                    onSuccess()
                },
                onError = { err ->
                    error = ErrorHandler.getUserMessage(err)
                }
            )
        }
    }
    
    fun updateConfig(configId: Int, request: SmartRolloutConfigRequest, onSuccess: () -> Unit = {}) {
        scope.launch {
            ErrorHandler.withErrorHandling(
                block = {
                    val client = ApiClient.client
                    val result: SmartRolloutConfigResponse = client.put(ApiClient.getApiPath("/smart-rollout/$configId")) {
                        contentType(ContentType.Application.Json)
                        setBody(request)
                    }.body()
                    configs = configs.map { if (it.id == configId) result else it }
                    onSuccess()
                },
                onError = { err ->
                    error = ErrorHandler.getUserMessage(err)
                }
            )
        }
    }
    
    fun executeRollout(configId: Int, onSuccess: () -> Unit = {}) {
        scope.launch {
            ErrorHandler.withErrorHandling(
                block = {
                    val client = ApiClient.client
                    client.post(ApiClient.getApiPath("/smart-rollout/$configId/execute"))
                    loadConfigs()
                    onSuccess()
                },
                onError = { err ->
                    error = ErrorHandler.getUserMessage(err)
                }
            )
        }
    }
    
    fun loadHistory(configId: Int) {
        scope.launch {
            ErrorHandler.withErrorHandling(
                block = {
                    val client = ApiClient.client
                    history = client.get(ApiClient.getApiPath("/smart-rollout/$configId/history")).body()
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
