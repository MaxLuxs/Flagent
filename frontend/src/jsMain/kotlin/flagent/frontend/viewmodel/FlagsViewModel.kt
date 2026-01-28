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
 * ViewModel for Flags list and management
 */
class FlagsViewModel {
    private val scope = CoroutineScope(Dispatchers.Main)
    private val TAG = "FlagsViewModel"
    
    // State
    var flags by mutableStateOf<List<FlagResponse>>(emptyList())
        private set
    
    var deletedFlags by mutableStateOf<List<FlagResponse>>(emptyList())
        private set
    
    var isLoading by mutableStateOf(false)
        private set
    
    var error by mutableStateOf<String?>(null)
        internal set
    
    // Filters
    var searchQuery by mutableStateOf("")
    var statusFilter by mutableStateOf<Boolean?>(null) // null = all, true = enabled, false = disabled
    var sortColumn by mutableStateOf<String?>(null)
    var sortAscending by mutableStateOf(true)
    
    // Filtered and sorted flags
    val filteredFlags: List<FlagResponse>
        get() = flags
            .filter { flag ->
                // Apply search filter
                val matchesSearch = searchQuery.isBlank() || 
                    flag.description.contains(searchQuery, ignoreCase = true) ||
                    flag.key.contains(searchQuery, ignoreCase = true) ||
                    flag.id.toString().contains(searchQuery)
                
                // Apply status filter
                val matchesStatus = statusFilter == null || flag.enabled == statusFilter
                
                matchesSearch && matchesStatus
            }
            .let { list ->
                // Apply sorting
                when (sortColumn) {
                    "id" -> if (sortAscending) list.sortedBy { it.id } else list.sortedByDescending { it.id }
                    "description" -> if (sortAscending) list.sortedBy { it.description } else list.sortedByDescending { it.description }
                    "updatedAt" -> if (sortAscending) list.sortedBy { it.updatedAt } else list.sortedByDescending { it.updatedAt }
                    "updatedBy" -> if (sortAscending) list.sortedBy { it.updatedBy } else list.sortedByDescending { it.updatedBy }
                    else -> list
                }
            }
    
    /**
     * Load all flags
     */
    fun loadFlags() {
        scope.launch {
            isLoading = true
            error = null
            
            ErrorHandler.withErrorHandling(
                block = {
                    AppLogger.info(TAG, "Loading flags...")
                    val result = ApiClient.getFlags()
                    flags = result.reversed() // Show newest first
                    AppLogger.info(TAG, "Loaded ${result.size} flags")
                },
                onError = { err ->
                    error = ErrorHandler.getUserMessage(err)
                    AppLogger.error(TAG, "Failed to load flags", err.cause)
                }
            )
            
            isLoading = false
        }
    }
    
    /**
     * Load deleted flags
     */
    fun loadDeletedFlags() {
        scope.launch {
            ErrorHandler.withErrorHandling(
                block = {
                    AppLogger.info(TAG, "Loading deleted flags...")
                    val result = ApiClient.getDeletedFlags()
                    deletedFlags = result.reversed()
                    AppLogger.info(TAG, "Loaded ${result.size} deleted flags")
                },
                onError = { err ->
                    error = ErrorHandler.getUserMessage(err)
                    AppLogger.error(TAG, "Failed to load deleted flags", err.cause)
                }
            )
        }
    }
    
    /**
     * Create a new flag
     */
    fun createFlag(description: String, template: String? = null, onSuccess: (FlagResponse) -> Unit) {
        if (description.isBlank()) return
        
        scope.launch {
            ErrorHandler.withErrorHandling(
                block = {
                    AppLogger.info(TAG, "Creating flag: $description")
                    val request = CreateFlagRequest(description, template = template)
                    val result = ApiClient.createFlag(request)
                    flags = listOf(result) + flags // Add to beginning
                    AppLogger.info(TAG, "Created flag: ${result.id}")
                    onSuccess(result)
                },
                onError = { err ->
                    error = ErrorHandler.getUserMessage(err)
                    AppLogger.error(TAG, "Failed to create flag", err.cause)
                }
            )
        }
    }
    
    /**
     * Delete a flag
     */
    fun deleteFlag(flagId: Int, onSuccess: () -> Unit = {}) {
        scope.launch {
            ErrorHandler.withErrorHandling(
                block = {
                    AppLogger.info(TAG, "Deleting flag: $flagId")
                    ApiClient.deleteFlag(flagId)
                    flags = flags.filter { it.id != flagId }
                    AppLogger.info(TAG, "Deleted flag: $flagId")
                    onSuccess()
                },
                onError = { err ->
                    error = ErrorHandler.getUserMessage(err)
                    AppLogger.error(TAG, "Failed to delete flag", err.cause)
                }
            )
        }
    }
    
    /**
     * Restore a deleted flag
     */
    fun restoreFlag(flagId: Int, onSuccess: () -> Unit = {}) {
        scope.launch {
            ErrorHandler.withErrorHandling(
                block = {
                    AppLogger.info(TAG, "Restoring flag: $flagId")
                    val result = ApiClient.restoreFlag(flagId)
                    deletedFlags = deletedFlags.filter { it.id != flagId }
                    flags = listOf(result) + flags
                    AppLogger.info(TAG, "Restored flag: $flagId")
                    onSuccess()
                },
                onError = { err ->
                    error = ErrorHandler.getUserMessage(err)
                    AppLogger.error(TAG, "Failed to restore flag", err.cause)
                }
            )
        }
    }
    
    /**
     * Toggle flag enabled status
     */
    fun toggleFlag(flagId: Int, enabled: Boolean) {
        scope.launch {
            ErrorHandler.withErrorHandling(
                block = {
                    AppLogger.info(TAG, "Toggling flag $flagId to enabled=$enabled")
                    val result = ApiClient.setFlagEnabled(flagId, enabled)
                    flags = flags.map { if (it.id == flagId) result else it }
                    AppLogger.info(TAG, "Toggled flag $flagId")
                },
                onError = { err ->
                    error = ErrorHandler.getUserMessage(err)
                    AppLogger.error(TAG, "Failed to toggle flag", err.cause)
                }
            )
        }
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        error = null
    }
    
    /**
     * Set sort column
     */
    fun setSortColumn(column: String) {
        if (sortColumn == column) {
            sortAscending = !sortAscending
        } else {
            sortColumn = column
            sortAscending = true
        }
    }
}
