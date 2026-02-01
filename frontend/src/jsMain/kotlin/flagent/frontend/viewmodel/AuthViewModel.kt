package flagent.frontend.viewmodel

import androidx.compose.runtime.*
import flagent.frontend.api.ApiClient
import flagent.frontend.api.LoginResponse
import flagent.frontend.state.User
import flagent.frontend.util.AppLogger
import flagent.frontend.util.ErrorHandler
import kotlinx.browser.localStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.w3c.dom.get
import org.w3c.dom.set

/**
 * ViewModel for Authentication (Phase 2)
 */
class AuthViewModel {
    private val scope = CoroutineScope(Dispatchers.Main)
    private val TAG = "AuthViewModel"
    
    private val TOKEN_KEY = "auth_token"
    private val USER_KEY = "current_user"
    
    var isAuthenticated by mutableStateOf(false)
        private set
    
    var currentUser by mutableStateOf<User?>(null)
        private set
    
    var isLoading by mutableStateOf(false)
        private set
    
    var error by mutableStateOf<String?>(null)
        private set
    
    init {
        // Load auth state from localStorage
        loadAuthState()
    }
    
    private fun loadAuthState() {
        val token = localStorage.getItem(TOKEN_KEY)
        if (token != null) {
            isAuthenticated = true
            val userJson = localStorage.getItem(USER_KEY)
            if (userJson != null) {
                runCatching {
                    Json.decodeFromString<User>(userJson)
                }.onSuccess { currentUser = it }
            }
        }
    }
    
    fun login(email: String, password: String, onSuccess: () -> Unit = {}) {
        scope.launch {
            isLoading = true
            error = null
            
            ErrorHandler.withErrorHandling(
                block = {
                    AppLogger.info(TAG, "Logging in user: $email")
                    val response: LoginResponse = ApiClient.login(email, password)
                    val user = User(
                        id = response.user.id,
                        email = response.user.email,
                        name = response.user.name
                    )
                    localStorage.setItem(TOKEN_KEY, response.token)
                    localStorage.setItem(USER_KEY, Json.encodeToString(User.serializer(), user))
                    currentUser = user
                    isAuthenticated = true
                    onSuccess()
                },
                onError = { err ->
                    val msg = ErrorHandler.getUserMessage(err)
                    error = if (msg.contains("Admin auth is disabled", ignoreCase = true)) {
                        "Server has admin login disabled. On the backend set FLAGENT_ADMIN_AUTH_ENABLED=true and configure FLAGENT_ADMIN_EMAIL, FLAGENT_ADMIN_PASSWORD, FLAGENT_JWT_AUTH_SECRET (min 32 chars)."
                    } else msg
                    AppLogger.error(TAG, "Login failed", err.cause)
                }
            )
            
            isLoading = false
        }
    }
    
    fun logout() {
        localStorage.removeItem(TOKEN_KEY)
        localStorage.removeItem(USER_KEY)
        currentUser = null
        isAuthenticated = false
        AppLogger.info(TAG, "User logged out")
    }
    
    fun getAuthToken(): String? {
        return localStorage.getItem(TOKEN_KEY)
    }
    
    fun clearError() {
        error = null
    }
}
