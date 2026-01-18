package com.flagent.sample.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flagent.client.apis.EvaluationApi
import com.flagent.client.models.EvalContext
import com.flagent.client.models.EvalResult
import com.flagent.enhanced.manager.FlagentManager
import com.flagent.sample.di.AppModule
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
}

class EvaluationViewModel : ViewModel() {
    private val _state = MutableStateFlow<EvaluationState>(EvaluationState.Idle)
    val state: StateFlow<EvaluationState> = _state.asStateFlow()
    
    private var currentBaseUrl: String = ""
    private var evaluationApi: EvaluationApi? = null
    private var flagentManager: FlagentManager? = null
    
    fun initialize(baseUrl: String) {
        if (currentBaseUrl != baseUrl) {
            currentBaseUrl = baseUrl
            evaluationApi = AppModule.getEvaluationApi(baseUrl)
            flagentManager = AppModule.getFlagentManager(
                baseUrl,
                com.flagent.enhanced.config.FlagentConfig()
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
                    entityContext = entityContext?.let { convertToJsonObjectMap(it) },
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
    
    private fun convertToJsonObjectMap(map: Map<String, Any>): Map<String, JsonObject> {
        return map.mapValues { entry ->
            val value = when (val v = entry.value) {
                is String -> JsonPrimitive(v)
                is Number -> JsonPrimitive(v)
                is Boolean -> JsonPrimitive(v)
                else -> JsonPrimitive(v.toString())
            }
            kotlinx.serialization.json.buildJsonObject {
                put(entry.key, value)
            }
        }
    }
}
