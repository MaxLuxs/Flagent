package flagent.frontend.viewmodel

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNull

class AuthViewModelTest {

    @Test
    fun initialStateWhenNoStoredAuth() {
        val viewModel = AuthViewModel()
        assertFalse(viewModel.isLoading)
        assertNull(viewModel.error)
    }
}
