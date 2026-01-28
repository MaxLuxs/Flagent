package flagent.frontend.viewmodel

import androidx.compose.runtime.*
import flagent.frontend.api.*
import flagent.frontend.util.AppLogger
import flagent.frontend.util.ErrorHandler
import flagent.frontend.util.currentTimeMillis
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ViewModel for Metrics & Analytics
 */
class MetricsViewModel(private val flagId: Int) {
    private val scope = CoroutineScope(Dispatchers.Main)
    private val TAG = "MetricsViewModel"
    
    var metrics by mutableStateOf<List<MetricDataPointResponse>>(emptyList())
        private set
    
    var aggregation by mutableStateOf<MetricAggregationResponse?>(null)
        private set
    
    var isLoading by mutableStateOf(false)
        private set
    
    var error by mutableStateOf<String?>(null)
        internal set
    
    // Filters
    var selectedMetricType by mutableStateOf<MetricType?>(null)
    var selectedVariantId by mutableStateOf<Int?>(null)
    var startTime by mutableStateOf<Long>(currentTimeMillis() - 3600000) // Last hour
    var endTime by mutableStateOf<Long>(currentTimeMillis())
    
    /**
     * Load metrics
     */
    fun loadMetrics() {
        scope.launch {
            isLoading = true
            error = null
            
            ErrorHandler.withErrorHandling(
                block = {
                    AppLogger.info(TAG, "Loading metrics for flag: $flagId")
                    val client = ApiClient.client
                    val url = buildString {
                        append(ApiClient.getApiPath("/metrics/$flagId"))
                        append("?start_time=$startTime&end_time=$endTime")
                        selectedMetricType?.let { append("&metric_type=${it.name}") }
                        selectedVariantId?.let { append("&variant_id=$it") }
                    }
                    metrics = client.get(url).body()
                    AppLogger.info(TAG, "Loaded ${metrics.size} metrics")
                },
                onError = { err ->
                    error = ErrorHandler.getUserMessage(err)
                    AppLogger.error(TAG, "Failed to load metrics", err.cause)
                }
            )
            
            isLoading = false
        }
    }
    
    /**
     * Load aggregation
     */
    fun loadAggregation() {
        if (selectedMetricType == null) return
        
        scope.launch {
            ErrorHandler.withErrorHandling(
                block = {
                    val client = ApiClient.client
                    val url = buildString {
                        append(ApiClient.getApiPath("/metrics/$flagId/aggregation"))
                        append("?metric_type=${selectedMetricType!!.name}")
                        append("&window_start=$startTime&window_end=$endTime")
                        selectedVariantId?.let { append("&variant_id=$it") }
                    }
                    aggregation = client.get(url).body()
                },
                onError = { err ->
                    error = ErrorHandler.getUserMessage(err)
                }
            )
        }
    }
    
    /**
     * Send test metric
     */
    fun sendMetric(request: MetricDataPointRequest, onSuccess: () -> Unit = {}) {
        scope.launch {
            ErrorHandler.withErrorHandling(
                block = {
                    AppLogger.info(TAG, "Sending metric")
                    val client = ApiClient.client
                    client.post(ApiClient.getApiPath("/metrics")) {
                        contentType(ContentType.Application.Json)
                        setBody(request)
                    }
                    onSuccess()
                    loadMetrics()
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
