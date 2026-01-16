package com.flagent.sample.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flagent.client.models.EvaluationEntity
import com.flagent.client.models.EvalResult
import com.flagent.enhanced.manager.FlagentManager
import com.flagent.sample.di.AppModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class BatchEvaluationState {
    object Idle : BatchEvaluationState()
    object Loading : BatchEvaluationState()
    data class Success(val results: List<EvalResult>) : BatchEvaluationState()
    data class Error(val message: String, val throwable: Throwable? = null) : BatchEvaluationState()
}

class BatchEvaluationViewModel : ViewModel() {
    private val _state = MutableStateFlow<BatchEvaluationState>(BatchEvaluationState.Idle)
    val state: StateFlow<BatchEvaluationState> = _state.asStateFlow()
    
    private var currentBaseUrl: String = ""
    private var flagentManager: FlagentManager? = null
    
    fun initialize(baseUrl: String) {
        if (currentBaseUrl != baseUrl) {
            currentBaseUrl = baseUrl
            flagentManager = AppModule.getFlagentManager(
                baseUrl,
                com.flagent.enhanced.config.FlagentConfig()
            )
        }
    }
    
    fun evaluateBatch(
        flagKeys: List<String>? = null,
        flagIDs: List<Int>? = null,
        entities: List<EvaluationEntity>,
        enableDebug: Boolean = false
    ) {
        if (flagentManager == null) {
            _state.value = BatchEvaluationState.Error("Manager not initialized. Please configure base URL in settings.")
            return
        }
        
        if (entities.isEmpty()) {
            _state.value = BatchEvaluationState.Error("At least one entity is required")
            return
        }
        
        if (flagKeys == null && flagIDs == null) {
            _state.value = BatchEvaluationState.Error("Either flagKeys or flagIDs must be provided")
            return
        }
        
        viewModelScope.launch {
            _state.value = BatchEvaluationState.Loading
            try {
                val results = flagentManager!!.evaluateBatch(
                    flagKeys = flagKeys,
                    flagIDs = flagIDs,
                    entities = entities,
                    enableDebug = enableDebug
                )
                _state.value = BatchEvaluationState.Success(results)
            } catch (e: Exception) {
                _state.value = BatchEvaluationState.Error(
                    message = e.message ?: "Unknown error occurred",
                    throwable = e
                )
            }
        }
    }
    
    fun resetState() {
        _state.value = BatchEvaluationState.Idle
    }
}
