package flagent.frontend.util

import io.ktor.client.plugins.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import kotlinx.serialization.json.*

/**
 * Error types for better error handling
 */
sealed class AppError(open val message: String, open val cause: Throwable? = null) {
    data class NetworkError(override val message: String, override val cause: Throwable? = null) : AppError(message, cause)
    data class Unauthorized(override val message: String = "Unauthorized", override val cause: Throwable? = null) : AppError(message, cause)
    data class Forbidden(override val message: String = "Forbidden", override val cause: Throwable? = null) : AppError(message, cause)
    data class NotFound(override val message: String = "Not Found", override val cause: Throwable? = null) : AppError(message, cause)
    data class ServerError(override val message: String = "Server Error", override val cause: Throwable? = null) : AppError(message, cause)
    data class ValidationError(override val message: String, override val cause: Throwable? = null) : AppError(message, cause)
    data class UnknownError(override val message: String, override val cause: Throwable? = null) : AppError(message, cause)
}

/**
 * Extracts server error from JsonDecodingException message.
 * Format: "... JSON input: {\"error\":\"Create tenant first: POST /admin/tenants\"}"
 */
private fun extractErrorFromJsonDecodingMessage(message: String?): String? {
    if (message.isNullOrBlank()) return null
    // Direct extract: "error":"value" or "error": "value"
    Regex(""""error"\s*:\s*"([^"]+)"""").find(message)?.groupValues?.getOrNull(1)?.let {
        return it.replace("\\\"", "\"")
    }
    // Try parsing JSON object from "JSON input: {...}"
    Regex("""JSON input:\s*(\{.*\})""").find(message)?.groupValues?.getOrNull(1)?.let { jsonStr ->
        return runCatching {
            val obj = Json.parseToJsonElement(jsonStr) as? JsonObject ?: return@runCatching null
            (obj["error"] as? JsonPrimitive)?.content?.takeIf { it.isNotBlank() }
                ?: (obj["message"] as? JsonPrimitive)?.content?.takeIf { it.isNotBlank() }
        }.getOrNull()
    }
    return null
}

/**
 * Parses JSON response body for "message" or "error" field to show server message to user.
 */
private fun parseResponseMessage(body: String): String? {
    return runCatching {
        val obj = Json.parseToJsonElement(body) as? JsonObject ?: return@runCatching null
        ((obj["message"] as? JsonPrimitive)?.content?.takeIf { it.isNotBlank() }
            ?: (obj["error"] as? JsonPrimitive)?.content?.takeIf { it.isNotBlank() })
    }.getOrNull()
}

/**
 * Error handler for centralizing error handling
 */
object ErrorHandler {
    private const val TAG = "ErrorHandler"
    
    private const val SERVICE_UNAVAILABLE = "Service temporarily unavailable."
    
    /**
     * Handle error and return user-friendly message.
     * @param responseBody optional response body (JSON) to extract message/error for ResponseException
     */
    fun handle(error: Throwable, responseBody: String? = null): AppError {
        AppLogger.error(TAG, "Handling error: ${error.message}", error)
        
        return when (error) {
            is ResponseException -> {
                val status = error.response.status
                val customMsg = responseBody?.let { parseResponseMessage(it) }
                when (status) {
                    HttpStatusCode.Unauthorized -> AppError.Unauthorized(customMsg ?: "Unauthorized", error)
                    HttpStatusCode.Forbidden -> AppError.Forbidden(customMsg ?: "Forbidden", error)
                    HttpStatusCode.NotFound -> AppError.NotFound(customMsg ?: "Not Found", error)
                    HttpStatusCode.BadRequest -> AppError.ValidationError(customMsg ?: "Invalid request", error)
                    HttpStatusCode.BadGateway, HttpStatusCode.ServiceUnavailable, HttpStatusCode.GatewayTimeout ->
                        AppError.ServerError(customMsg ?: SERVICE_UNAVAILABLE, error)
                    in HttpStatusCode.InternalServerError..HttpStatusCode.GatewayTimeout ->
                        AppError.ServerError(customMsg ?: "Server error occurred", error)
                    else -> AppError.UnknownError(customMsg ?: "Request failed: $status", error)
                }
            }
            is HttpRequestTimeoutException -> AppError.NetworkError("Request timeout", error)
            else -> {
                // JsonDecodingException when API returns error object but client expects array
                val isJsonDecoding = error::class.simpleName == "JsonDecodingException" ||
                    (error.message?.contains("JSON input:") == true && error.message?.contains("Expected") == true)
                if (isJsonDecoding) {
                    val serverMsg = extractErrorFromJsonDecodingMessage(error.message)
                    when {
                        serverMsg != null && serverMsg.contains("tenant", ignoreCase = true) ->
                            AppError.Unauthorized(serverMsg, error)
                        serverMsg != null -> AppError.UnknownError(serverMsg, error)
                        else -> AppError.UnknownError(error.message ?: "Invalid response format", error)
                    }
                } else {
                    AppError.UnknownError(error.message ?: "Unknown error", error)
                }
            }
        }
    }
    
    /**
     * Get user-friendly error message
     */
    fun getUserMessage(error: AppError): String {
        return when (error) {
            is AppError.NetworkError -> "Network error. Please check your connection."
            is AppError.Unauthorized ->
                if (error.message != "Unauthorized") error.message else "Please login to continue."
            is AppError.Forbidden ->
                if (error.message != "Forbidden") error.message else "You don't have permission to perform this action."
            is AppError.NotFound ->
                if (error.message != "Not Found") error.message else "Resource not found."
            is AppError.ServerError -> error.message
            is AppError.ValidationError -> error.message
            is AppError.UnknownError ->
                if (error.message != "Unknown error") error.message else "An unexpected error occurred."
        }
    }
    
    /**
     * Execute block with error handling. Reads response body for ResponseException to show server message.
     */
    suspend fun <T> withErrorHandling(
        block: suspend () -> T,
        onError: (AppError) -> Unit
    ): T? {
        return try {
            block()
        } catch (e: Throwable) {
            val body = when (e) {
                is ResponseException -> try {
                    e.response.bodyAsText()
                } catch (_: Throwable) {
                    null
                }
                else -> null
            }
            val err = handle(e, body)
            onError(err)
            null
        }
    }
}
