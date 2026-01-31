package com.flagent.sample.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flagent.client.apis.EvaluationApi
import com.flagent.client.models.EvalResult
import com.flagent.client.models.EvaluationBatchRequest
import com.flagent.client.models.EvaluationEntity
import com.flagent.sample.di.AppModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class BatchEvaluationState {
    data object Idle : BatchEvaluationState()
    data object Loading : BatchEvaluationState()
    data class Success(val results: List<EvalResult>) : BatchEvaluationState()
    data class Error(val message: String) : BatchEvaluationState()
}

class BatchEvaluationViewModel : ViewModel() {
    private val _state = MutableStateFlow<BatchEvaluationState>(BatchEvaluationState.Idle)
    val state: StateFlow<BatchEvaluationState> = _state.asStateFlow()

    private var currentBaseUrl: String = ""
    private var evaluationApi: EvaluationApi? = null

    fun initialize(baseUrl: String) {
        if (currentBaseUrl != baseUrl) {
            currentBaseUrl = baseUrl
            evaluationApi = AppModule.getEvaluationApi(baseUrl)
        }
    }

    fun evaluateBatch(
        flagKeys: List<String>,
        entities: List<EvaluationEntity>
    ) {
        val api = evaluationApi
        if (api == null) {
            _state.value = BatchEvaluationState.Error("API not initialized. Configure base URL in settings.")
            return
        }
        viewModelScope.launch {
            _state.value = BatchEvaluationState.Loading
            try {
                val request = EvaluationBatchRequest(
                    entities = entities,
                    flagKeys = flagKeys,
                    enableDebug = false
                )
                val response = api.postEvaluationBatch(request)
                val body = response.body()
                if (body != null) {
                    _state.value = BatchEvaluationState.Success(body.evaluationResults)
                } else {
                    _state.value = BatchEvaluationState.Error("Empty response")
                }
            } catch (e: Exception) {
                _state.value = BatchEvaluationState.Error(e.message ?: "Unknown error")
            }
        }
    }
}
