package flagent.frontend.viewmodel

import androidx.compose.runtime.*
import flagent.api.model.*
import flagent.frontend.api.ApiClient
import flagent.frontend.util.AppLogger
import flagent.frontend.util.ErrorHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ViewModel for Flag editor
 */
class FlagEditorViewModel(private val flagId: Int?) {
    private val scope = CoroutineScope(Dispatchers.Main)
    private val TAG = "FlagEditorViewModel"
    
    // State
    var flag by mutableStateOf<FlagResponse?>(null)
        private set
    
    var segments by mutableStateOf<List<SegmentResponse>>(emptyList())
        private set
    
    var variants by mutableStateOf<List<VariantResponse>>(emptyList())
        private set
    
    var isLoading by mutableStateOf(false)
        private set
    
    var error by mutableStateOf<String?>(null)
        private set
    
    var isSaving by mutableStateOf(false)
        private set
    
    // Editor state
    var currentTab by mutableStateOf("config")
    
    /**
     * Load flag data
     */
    fun loadFlag() {
        if (flagId == null) return
        
        scope.launch {
            isLoading = true
            error = null
            
            ErrorHandler.withErrorHandling(
                block = {
                    AppLogger.info(TAG, "Loading flag: $flagId")
                    val result = ApiClient.getFlag(flagId)
                    flag = result
                    
                    // Load related data
                    loadSegments()
                    loadVariants()
                    
                    AppLogger.info(TAG, "Loaded flag: $flagId")
                },
                onError = { err ->
                    error = ErrorHandler.getUserMessage(err)
                    AppLogger.error(TAG, "Failed to load flag", err.cause)
                }
            )
            
            isLoading = false
        }
    }
    
    /**
     * Load segments
     */
    fun loadSegments() {
        if (flagId == null) return
        
        scope.launch {
            ErrorHandler.withErrorHandling(
                block = {
                    val result = ApiClient.getSegments(flagId)
                    segments = result
                },
                onError = { err ->
                    error = ErrorHandler.getUserMessage(err)
                }
            )
        }
    }
    
    /**
     * Load variants
     */
    fun loadVariants() {
        if (flagId == null) return
        
        scope.launch {
            ErrorHandler.withErrorHandling(
                block = {
                    val result = ApiClient.getVariants(flagId)
                    variants = result
                },
                onError = { err ->
                    error = ErrorHandler.getUserMessage(err)
                }
            )
        }
    }
    
    /**
     * Update flag
     */
    fun updateFlag(request: PutFlagRequest, onSuccess: () -> Unit = {}) {
        if (flagId == null) return
        
        scope.launch {
            isSaving = true
            
            ErrorHandler.withErrorHandling(
                block = {
                    AppLogger.info(TAG, "Updating flag: $flagId")
                    val result = ApiClient.updateFlagFull(flagId, request)
                    flag = result
                    AppLogger.info(TAG, "Updated flag: $flagId")
                    onSuccess()
                },
                onError = { err ->
                    error = ErrorHandler.getUserMessage(err)
                    AppLogger.error(TAG, "Failed to update flag", err.cause)
                }
            )
            
            isSaving = false
        }
    }
    
    /**
     * Create segment
     */
    fun createSegment(request: CreateSegmentRequest, onSuccess: () -> Unit = {}) {
        if (flagId == null) return
        
        scope.launch {
            ErrorHandler.withErrorHandling(
                block = {
                    val result = ApiClient.createSegment(flagId, request)
                    segments = segments + result
                    onSuccess()
                },
                onError = { err ->
                    error = ErrorHandler.getUserMessage(err)
                }
            )
        }
    }
    
    /**
     * Update segment
     */
    fun updateSegment(segmentId: Int, request: PutSegmentRequest, onSuccess: () -> Unit = {}) {
        if (flagId == null) return
        
        scope.launch {
            ErrorHandler.withErrorHandling(
                block = {
                    val result = ApiClient.updateSegment(flagId, segmentId, request)
                    segments = segments.map { if (it.id == segmentId) result else it }
                    onSuccess()
                },
                onError = { err ->
                    error = ErrorHandler.getUserMessage(err)
                }
            )
        }
    }
    
    /**
     * Delete segment
     */
    fun deleteSegment(segmentId: Int, onSuccess: () -> Unit = {}) {
        if (flagId == null) return
        
        scope.launch {
            ErrorHandler.withErrorHandling(
                block = {
                    ApiClient.deleteSegment(flagId, segmentId)
                    segments = segments.filter { it.id != segmentId }
                    onSuccess()
                },
                onError = { err ->
                    error = ErrorHandler.getUserMessage(err)
                }
            )
        }
    }
    
    /**
     * Reorder segments
     */
    fun reorderSegments(segmentIds: List<Int>, onSuccess: () -> Unit = {}) {
        if (flagId == null) return
        
        scope.launch {
            ErrorHandler.withErrorHandling(
                block = {
                    val request = PutSegmentReorderRequest(segmentIds)
                    ApiClient.reorderSegments(flagId, request)
                    // Reload segments to get updated order
                    loadSegments()
                    onSuccess()
                },
                onError = { err ->
                    error = ErrorHandler.getUserMessage(err)
                }
            )
        }
    }
    
    /**
     * Create variant
     */
    fun createVariant(request: CreateVariantRequest, onSuccess: () -> Unit = {}) {
        if (flagId == null) return
        
        scope.launch {
            ErrorHandler.withErrorHandling(
                block = {
                    val result = ApiClient.createVariant(flagId, request)
                    variants = variants + result
                    onSuccess()
                },
                onError = { err ->
                    error = ErrorHandler.getUserMessage(err)
                }
            )
        }
    }
    
    /**
     * Update variant
     */
    fun updateVariant(variantId: Int, request: PutVariantRequest, onSuccess: () -> Unit = {}) {
        if (flagId == null) return
        
        scope.launch {
            ErrorHandler.withErrorHandling(
                block = {
                    val result = ApiClient.updateVariant(flagId, variantId, request)
                    variants = variants.map { if (it.id == variantId) result else it }
                    onSuccess()
                },
                onError = { err ->
                    error = ErrorHandler.getUserMessage(err)
                }
            )
        }
    }
    
    /**
     * Delete variant
     */
    fun deleteVariant(variantId: Int, onSuccess: () -> Unit = {}) {
        if (flagId == null) return
        
        scope.launch {
            ErrorHandler.withErrorHandling(
                block = {
                    ApiClient.deleteVariant(flagId, variantId)
                    variants = variants.filter { it.id != variantId }
                    onSuccess()
                },
                onError = { err ->
                    error = ErrorHandler.getUserMessage(err)
                }
            )
        }
    }
    
    /**
     * Clear error
     */
    fun clearError() {
        error = null
    }
}
