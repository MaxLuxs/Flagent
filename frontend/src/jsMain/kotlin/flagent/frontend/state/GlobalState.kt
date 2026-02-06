package flagent.frontend.state

import androidx.compose.runtime.*
import flagent.api.model.FlagResponse
import flagent.frontend.util.randomId
import kotlinx.serialization.Serializable

/**
 * Set to true when backend returns 401 with tenant/API-key message (e.g. "Create tenant first").
 * Allows /tenants and /login in OSS frontend when backend is enterprise.
 */
object BackendOnboardingState {
    var allowTenantsAndLogin by mutableStateOf(false)
        private set
    fun setBackendNeedsTenantOrAuth() { allowTenantsAndLogin = true }
}

/**
 * Global application state
 */
class GlobalState {
    // Cache of flags for quick access
    var flagsCache by mutableStateOf<List<FlagResponse>>(emptyList())
    
    // Loading state
    var isLoading by mutableStateOf(false)
    
    // Error state
    var error by mutableStateOf<String?>(null)
    
    // Auth state (will be used in auth phase)
    var isAuthenticated by mutableStateOf(false)
    var currentUser by mutableStateOf<User?>(null)
    
    // Tenant state (will be used in multi-tenancy phase)
    var currentTenant by mutableStateOf<Tenant?>(null)
    
    // Notification state
    var notifications by mutableStateOf<List<Notification>>(emptyList())
    
    fun addNotification(notification: Notification) {
        notifications = notifications + notification
    }
    
    fun removeNotification(id: String) {
        notifications = notifications.filter { it.id != id }
    }
    
    fun clearError() {
        error = null
    }
}

/**
 * User model for authenticated admin (AuthViewModel).
 */
@Serializable
data class User(
    val id: String,
    val email: String,
    val name: String?
)

/**
 * Tenant model for multi-tenancy (TenantSwitcher, TenantViewModel).
 */
data class Tenant(
    val id: String,
    val key: String,
    val name: String,
    val plan: String = "STARTER",
    val status: String = "ACTIVE"
)

/**
 * Notification model
 */
data class Notification(
    val id: String = randomId(),
    val message: String,
    val type: NotificationType = NotificationType.INFO,
    val duration: Long = 5000L
)

enum class NotificationType {
    INFO,
    SUCCESS,
    WARNING,
    ERROR
}

/**
 * Global state instance
 */
val LocalGlobalState = compositionLocalOf { GlobalState() }
