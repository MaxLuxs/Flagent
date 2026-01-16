package io.ktor.flagent

import flagent.api.model.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.client.statement.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * FlagentClient - HTTP client for Flagent server
 */
class FlagentClient(
    private val baseUrl: String,
    private val connectTimeoutMs: Long = 5000,
    private val requestTimeoutMs: Long = 10000
) {
    private val client = HttpClient(CIO) {
        install(io.ktor.client.plugins.HttpTimeout) {
            requestTimeoutMillis = requestTimeoutMs
            connectTimeoutMillis = connectTimeoutMs
        }
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                encodeDefaults = true
            })
        }
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.INFO
        }
    }
    
    /**
     * Evaluate flag
     */
    suspend fun evaluate(request: EvaluationRequest): EvaluationResponse {
        return withContext(Dispatchers.IO) {
            try {
                val response = client.post("$baseUrl/api/v1/evaluation") {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }
                
                if (response.status.isSuccess()) {
                    response.body<EvaluationResponse>()
                } else {
                    val errorText = response.bodyAsText()
                    logger.error { "Failed to evaluate flag: ${response.status} - $errorText" }
                    throw FlagentException("Failed to evaluate flag: ${response.status}")
                }
            } catch (e: FlagentException) {
                throw e
            } catch (e: Exception) {
                logger.error(e) { "Failed to evaluate flag" }
                throw FlagentException("Failed to evaluate flag", e)
            }
        }
    }
    
    /**
     * Evaluate flags by batch request
     */
    suspend fun evaluateBatch(request: EvaluationBatchRequest): EvaluationBatchResponse {
        return withContext(Dispatchers.IO) {
            try {
                val response = client.post("$baseUrl/api/v1/evaluation/batch") {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }
                
                if (response.status.isSuccess()) {
                    response.body<EvaluationBatchResponse>()
                } else {
                    val errorText = response.bodyAsText()
                    logger.error { "Failed to evaluate flags batch: ${response.status} - $errorText" }
                    throw FlagentException("Failed to evaluate flags batch: ${response.status}")
                }
            } catch (e: FlagentException) {
                throw e
            } catch (e: Exception) {
                logger.error(e) { "Failed to evaluate flags batch" }
                throw FlagentException("Failed to evaluate flags batch", e)
            }
        }
    }
    
    /**
     * Evaluate flags by tags
     */
    suspend fun evaluateByTags(
        tags: List<String>,
        operator: String? = null,
        entityID: String? = null,
        entityType: String? = null,
        entityContext: Map<String, String>? = null,
        enableDebug: Boolean = false
    ): List<EvaluationResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val request = EvaluationBatchRequest(
                    entities = listOf(
                        EntityRequest(
                            entityID = entityID,
                            entityType = entityType,
                            entityContext = entityContext
                        )
                    ),
                    flagTags = tags,
                    flagTagsOperator = operator,
                    enableDebug = enableDebug
                )
                val batchResponse = evaluateBatch(request)
                batchResponse.evaluationResults
            } catch (e: Exception) {
                logger.error(e) { "Failed to evaluate flags by tags" }
                throw FlagentException("Failed to evaluate flags by tags", e)
            }
        }
    }
    
    /**
     * Close client
     */
    fun close() {
        client.close()
    }
}

/**
 * FlagentException - exception for Flagent operations
 */
class FlagentException(message: String, cause: Throwable? = null) : Exception(message, cause)
