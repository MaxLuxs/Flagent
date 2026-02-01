package flagent.frontend.viewmodel

import kotlinx.browser.localStorage
import kotlinx.serialization.json.Json
import flagent.frontend.state.User
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AuthViewModelTest {

    private val tokenKey = "auth_token"
    private val userKey = "current_user"

    @Test
    fun initialStateWhenNoStoredAuth() {
        clearAuthStorage()
        val viewModel = AuthViewModel()
        assertFalse(viewModel.isLoading)
        assertNull(viewModel.error)
        assertFalse(viewModel.isAuthenticated)
        assertNull(viewModel.currentUser)
    }

    @Test
    fun loadAuthStateRestoresFromLocalStorage() {
        clearAuthStorage()
        localStorage.setItem(tokenKey, "test-token")
        val user = User(id = "1", email = "u@test.com", name = "User")
        localStorage.setItem(userKey, Json.encodeToString(User.serializer(), user))
        val viewModel = AuthViewModel()
        assertTrue(viewModel.isAuthenticated)
        assertTrue(viewModel.currentUser?.email == "u@test.com")
        assertTrue(viewModel.getAuthToken() == "test-token")
        clearAuthStorage()
    }

    @Test
    fun logoutClearsStateAndStorage() {
        clearAuthStorage()
        localStorage.setItem(tokenKey, "t")
        localStorage.setItem(userKey, """{"id":"1","email":"a@b.com","name":"A"}""")
        val viewModel = AuthViewModel()
        assertTrue(viewModel.isAuthenticated)
        viewModel.logout()
        assertFalse(viewModel.isAuthenticated)
        assertNull(viewModel.currentUser)
        assertNull(viewModel.getAuthToken())
        assertNull(localStorage.getItem(tokenKey))
        assertNull(localStorage.getItem(userKey))
    }

    @Test
    fun clearErrorClearsErrorState() {
        clearAuthStorage()
        val viewModel = AuthViewModel()
        viewModel.clearError()
        assertNull(viewModel.error)
    }

    @Test
    fun getAuthTokenReturnsNullWhenNotStored() {
        clearAuthStorage()
        val viewModel = AuthViewModel()
        assertNull(viewModel.getAuthToken())
    }

    private fun clearAuthStorage() {
        localStorage.removeItem(tokenKey)
        localStorage.removeItem(userKey)
    }
}
