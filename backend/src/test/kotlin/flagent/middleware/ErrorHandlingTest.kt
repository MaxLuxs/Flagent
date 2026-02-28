package flagent.middleware

import flagent.config.AppConfig
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.ktor.serialization.kotlinx.json.*
import io.mockk.*
import kotlin.test.*
import kotlinx.serialization.json.*

class ErrorHandlingTest {
    @Test
    fun testIllegalArgumentExceptionHandling() = testApplication {
        mockkObject(AppConfig)
        every { AppConfig.sentryEnabled } returns false
        
        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            configureErrorHandling()
            routing {
                get("/test") {
                    throw IllegalArgumentException("Test error")
                }
            }
        }
        
        val response = client.get("/test")
        assertEquals(HttpStatusCode.BadRequest, response.status)
        
        unmockkObject(AppConfig)
    }
    
    @Test
    fun testIllegalStateExceptionHandling() = testApplication {
        mockkObject(AppConfig)
        every { AppConfig.sentryEnabled } returns false
        
        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            configureErrorHandling()
            routing {
                get("/test") {
                    throw IllegalStateException("Test error")
                }
            }
        }
        
        val response = client.get("/test")
        assertEquals(HttpStatusCode.InternalServerError, response.status)
        
        unmockkObject(AppConfig)
    }
    
    @Test
    fun testGenericExceptionHandling() = testApplication {
        mockkObject(AppConfig)
        every { AppConfig.sentryEnabled } returns false
        
        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            configureErrorHandling()
            routing {
                get("/test") {
                    throw RuntimeException("Test error")
                }
            }
        }
        
        val response = client.get("/test")
        assertEquals(HttpStatusCode.InternalServerError, response.status)
        
        unmockkObject(AppConfig)
    }
    
    @Test
    fun testNullPointerExceptionHandling() = testApplication {
        mockkObject(AppConfig)
        every { AppConfig.sentryEnabled } returns false
        
        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            configureErrorHandling()
            routing {
                get("/test") {
                    throw NullPointerException("Test error")
                }
            }
        }
        
        val response = client.get("/test")
        assertEquals(HttpStatusCode.InternalServerError, response.status)
        
        unmockkObject(AppConfig)
    }
    
    @Test
    fun testCustomExceptionHandling() = testApplication {
        mockkObject(AppConfig)
        every { AppConfig.sentryEnabled } returns false
        
        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            configureErrorHandling()
            routing {
                get("/test") {
                    throw CustomTestException("Custom error")
                }
            }
        }
        
        val response = client.get("/test")
        assertEquals(HttpStatusCode.InternalServerError, response.status)
        
        unmockkObject(AppConfig)
    }

    @Test
    fun testIllegalStateException_whenSentryEnabled_returns500() = testApplication {
        mockkObject(AppConfig)
        every { AppConfig.sentryEnabled } returns true
        application {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
            configureErrorHandling()
            routing {
                get("/test") { throw IllegalStateException("state error") }
            }
        }
        val response = client.get("/test")
        assertEquals(HttpStatusCode.InternalServerError, response.status)
        unmockkObject(AppConfig)
    }

    @Test
    fun testException_withNullMessage_returnsFallbackMessage() = testApplication {
        mockkObject(AppConfig)
        every { AppConfig.sentryEnabled } returns false
        application {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
            configureErrorHandling()
            routing {
                get("/test") { throw IllegalArgumentException(null as String?) }
            }
        }
        val response = client.get("/test")
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains("Bad request"))
        unmockkObject(AppConfig)
    }
}

class CustomTestException(message: String) : RuntimeException(message)
