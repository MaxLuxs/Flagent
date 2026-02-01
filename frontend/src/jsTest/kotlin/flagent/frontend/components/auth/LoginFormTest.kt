package flagent.frontend.components.auth

import flagent.frontend.viewmodel.AuthViewModel
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * LoginForm is a Composable; full rendering tests require browser/Compose test runtime.
 * Here we verify AuthViewModel contract used by LoginForm.
 */
class LoginFormTest {

    @Test
    fun loginFormExpectsAuthViewModelWithInitialState() {
        val viewModel = AuthViewModel()
        assertFalse(viewModel.isLoading)
        assertNull(viewModel.error)
        assertFalse(viewModel.isAuthenticated)
    }

    @Test
    fun loginFormExpectsAuthViewModelWithClearError() {
        val viewModel = AuthViewModel()
        viewModel.clearError()
        assertNull(viewModel.error)
    }

    @Test
    fun loginFormExpectsAuthViewModelWithGetAuthToken() {
        val viewModel = AuthViewModel()
        val token = viewModel.getAuthToken()
        assertTrue(token == null || token.isNotBlank())
    }
}
