package flagent.frontend.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ErrorHandlerTest {
    
    @Test
    fun testNetworkErrorMessage() {
        val error = AppError.NetworkError("Connection failed")
        val message = ErrorHandler.getUserMessage(error)
        assertEquals("Network error. Please check your connection.", message)
    }
    
    @Test
    fun testUnauthorizedMessage() {
        val error = AppError.Unauthorized()
        val message = ErrorHandler.getUserMessage(error)
        assertEquals("Please login to continue.", message)
    }
    
    @Test
    fun testForbiddenMessage() {
        val error = AppError.Forbidden()
        val message = ErrorHandler.getUserMessage(error)
        assertEquals("You don't have permission to perform this action.", message)
    }
    
    @Test
    fun testNotFoundMessage() {
        val error = AppError.NotFound()
        val message = ErrorHandler.getUserMessage(error)
        assertEquals("Resource not found.", message)
    }
    
    @Test
    fun testServerErrorMessage() {
        val error = AppError.ServerError()
        val message = ErrorHandler.getUserMessage(error)
        assertEquals("Server Error", message)
    }
    
    @Test
    fun testValidationErrorMessage() {
        val error = AppError.ValidationError("Invalid input")
        val message = ErrorHandler.getUserMessage(error)
        assertEquals("Invalid input", message)
    }
    
    @Test
    fun testUnknownErrorMessage() {
        val error = AppError.UnknownError("Something went wrong")
        val message = ErrorHandler.getUserMessage(error)
        assertEquals("An unexpected error occurred.", message)
    }
}
