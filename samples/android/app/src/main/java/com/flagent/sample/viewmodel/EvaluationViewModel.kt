package com.flagent.sample.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flagent.client.apis.EvaluationApi
import com.flagent.client.models.EvalContext
import com.flagent.client.models.EvalResult
import com.flagent.enhanced.manager.FlagentManager
import com.flagent.sample.di.AppModule
import com.flagent.openfeature.AttributeValue
import com.flagent.openfeature.DefaultOpenFeatureClient
import com.flagent.openfeature.EvaluationContext as OfEvaluationContext
import com.flagent.openfeature.FlagentOpenFeatureConfig
import com.flagent.openfeature.FlagentOpenFeatureProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

sealed class EvaluationState {
    object Idle : EvaluationState()
    object Loading : EvaluationState()
    data class Success(val result: EvalResult, val fromCache: Boolean = false) : EvaluationState()
    data class Error(val message: String, val throwable: Throwable? = null) : EvaluationState()
    data class OpenFeatureSuccess(
        val enabled: Boolean,
        val maxItems: Int,
        val discount: Double
    ) : EvaluationState()
}

class EvaluationViewModel : ViewModel() {
    private val _state = MutableStateFlow<EvaluationState>(EvaluationState.Idle)
    val state: StateFlow<EvaluationState> = _state.asStateFlow()
    
    private var currentBaseUrl: String = ""
    private var evaluationApi: EvaluationApi? = null
    private var flagentManager: FlagentManager? = null
    private var openFeatureClient: DefaultOpenFeatureClient? = null
    
    fun initialize(baseUrl: String) {
        if (currentBaseUrl != baseUrl) {
            currentBaseUrl = baseUrl
            evaluationApi = AppModule.getEvaluationApi(baseUrl)
            flagentManager = AppModule.getFlagentManager(baseUrl, com.flagent.enhanced.config.FlagentConfig())
            openFeatureClient = DefaultOpenFeatureClient(
                FlagentOpenFeatureProvider(
                    FlagentOpenFeatureConfig(baseUrl = baseUrl) { api ->
                        // Reuse the same auth as other SDKs if configured in AppModule in the future.
                        // For now this sample uses default public dev settings.
                    }
                )
            )
        }
    }
    
    fun evaluateBasic(
        flagKey: String? = null,
        flagID: Long? = null,
        entityID: String? = null,
        entityType: String? = null,
        entityContext: Map<String, Any>? = null,
        enableDebug: Boolean = false
    ) {
        if (evaluationApi == null) {
            _state.value = EvaluationState.Error("API not initialized. Please configure base URL in settings.")
            return
        }
        
        viewModelScope.launch {
            _state.value = EvaluationState.Loading
            try {
                val evalContext = EvalContext(
                    flagKey = flagKey,
                    flagID = flagID,
                    entityID = entityID,
                    entityType = entityType,
                    entityContext = entityContext?.let { convertToJsonObject(it) },
                    enableDebug = enableDebug
                )
                val response = evaluationApi!!.postEvaluation(evalContext)
                val result = response.body()
                _state.value = EvaluationState.Success(result, fromCache = false)
            } catch (e: Exception) {
                _state.value = EvaluationState.Error(
                    message = e.message ?: "Unknown error occurred",
                    throwable = e
                )
            }
        }
    }

    fun evaluateOpenFeatureLike(
        flagKey: String,
        entityID: String,
        entityType: String? = null,
        entityContext: Map<String, Any>? = null
    ) {
        val client = openFeatureClient
        if (client == null) {
            _state.value = EvaluationState.Error("OpenFeature client not initialized. Please configure base URL in settings.")
            return
        }

        viewModelScope.launch {
            _state.value = EvaluationState.Loading
            try {
                val ctx = OfEvaluationContext(
                    targetingKey = entityID,
                    attributes = (entityContext ?: emptyMap()).mapValues { (_, v) ->
                        when (v) {
                            is Boolean -> AttributeValue.BooleanValue(v)
                            is Int -> AttributeValue.IntValue(v)
                            is Long -> AttributeValue.IntValue(v.toInt())
                            is Double -> AttributeValue.DoubleValue(v)
                            is Float -> AttributeValue.DoubleValue(v.toDouble())
                            else -> AttributeValue.StringValue(v.toString())
                        }
                    }
                )

                val enabled = client.getBooleanValue(
                    key = flagKey,
                    defaultValue = false,
                    context = ctx
                )
                val maxItems = client.getIntValue(
                    key = "cart_max_items",
                    defaultValue = 20,
                    context = ctx
                )
                val discount = client.getDoubleValue(
                    key = "discount_rate",
                    defaultValue = 0.0,
                    context = ctx
                )

                _state.value = EvaluationState.OpenFeatureSuccess(
                    enabled = enabled,
                    maxItems = maxItems,
                    discount = discount
                )
            } catch (e: Exception) {
                _state.value = EvaluationState.Error(
                    message = e.message ?: "Unknown error occurred (openfeature)",
                    throwable = e
                )
            }
        }
    }
    
    fun evaluateEnhanced(
        flagKey: String? = null,
        flagID: Long? = null,
        entityID: String? = null,
        entityType: String? = null,
        entityContext: Map<String, Any>? = null,
        enableDebug: Boolean = false
    ) {
        if (flagentManager == null) {
            _state.value = EvaluationState.Error("Manager not initialized. Please configure base URL in settings.")
            return
        }
        
        viewModelScope.launch {
            _state.value = EvaluationState.Loading
            try {
                val result = flagentManager!!.evaluate(
                    flagKey = flagKey,
                    flagID = flagID,
                    entityID = entityID,
                    entityType = entityType,
                    entityContext = entityContext,
                    enableDebug = enableDebug
                )
                _state.value = EvaluationState.Success(result, fromCache = false)
            } catch (e: Exception) {
                _state.value = EvaluationState.Error(
                    message = e.message ?: "Unknown error occurred",
                    throwable = e
                )
            }
        }
    }
    
    fun resetState() {
        _state.value = EvaluationState.Idle
    }
    
    private fun convertToJsonObject(map: Map<String, Any>): JsonObject {
        return kotlinx.serialization.json.buildJsonObject {
            map.forEach { (key, value) ->
                val prim = when (val v = value) {
                    is String -> JsonPrimitive(v)
                    is Number -> JsonPrimitive(v)
                    is Boolean -> JsonPrimitive(v)
                    else -> JsonPrimitive(v.toString())
                }
                put(key, prim)
            }
        }
    }
}
