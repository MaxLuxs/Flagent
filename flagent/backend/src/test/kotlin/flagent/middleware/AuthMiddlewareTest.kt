package flagent.middleware

import flagent.config.AppConfig
import flagent.util.getSubject
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.ktor.serialization.kotlinx.json.*
import io.mockk.*
import kotlin.test.*
import kotlinx.serialization.json.*

/**
 * Tests for Cookie Authentication middleware
 * Cookie authentication tests
 */
class AuthMiddlewareTest {
    
    @Test
    fun testCookieAuthDisabled() = testApplication {
        mockkObject(AppConfig)
        every { AppConfig.cookieAuthEnabled } returns false
        
        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            configureCookieAuth()
            
            routing {
                get("/protected") {
                    // Since cookie auth is disabled, configureCookieAuth does nothing
                    // So we need to manually check if auth is required
                    val subject = call.getSubject()
                    if (subject.isEmpty()) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Not authorized"))
                    } else {
                        call.respond(mapOf("status" to "authenticated"))
                    }
                }
            }
        }
        
        // Cookie auth is disabled
        val response = client.get("/protected")
        
        // Should return 401 Unauthorized when cookie auth is disabled
        assertEquals(HttpStatusCode.Unauthorized, response.status)
        
        unmockkObject(AppConfig)
    }
    
    @Test
    fun testCookieAuthWithPlainCookieValue() = testApplication {
        mockkObject(AppConfig)
        every { AppConfig.cookieAuthEnabled } returns true
        every { AppConfig.cookieAuthUserField } returns "CF_Authorization"
        every { AppConfig.cookieAuthUserFieldJWTClaim } returns ""
        every { AppConfig.jwtAuthEnabled } returns false
        every { AppConfig.headerAuthEnabled } returns false
        
        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            configureCookieAuth()
            
            routing {
                get("/protected") {
                    val subject = call.getSubject()
                    if (subject.isNotEmpty()) {
                        call.respond(mapOf("userId" to subject))
                    } else {
                        call.respond(
                            HttpStatusCode.Unauthorized,
                            mapOf("error" to "Not authorized")
                        )
                    }
                }
            }
        }
        
        val response = client.get("/protected") {
            cookie("CF_Authorization", "simple-user-id")
        }
        
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("simple-user-id"))
        
        unmockkObject(AppConfig)
    }
    
    @Test
    fun testCookieAuthWithJWTToken() = testApplication {
        mockkObject(AppConfig)
        every { AppConfig.cookieAuthEnabled } returns true
        every { AppConfig.cookieAuthUserField } returns "CF_Authorization"
        every { AppConfig.cookieAuthUserFieldJWTClaim } returns "email"
        every { AppConfig.jwtAuthEnabled } returns false
        every { AppConfig.headerAuthEnabled } returns false
        
        val secret = "test-secret"
        val algorithm = Algorithm.HMAC256(secret)
        val token = JWT.create()
            .withClaim("email", "user@example.com")
            .sign(algorithm)
        
        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            configureCookieAuth()
            
            routing {
                get("/protected") {
                    val subject = call.getSubject()
                    if (subject.isNotEmpty()) {
                        call.respond(mapOf("email" to subject))
                    } else {
                        call.respond(
                            HttpStatusCode.Unauthorized,
                            mapOf("error" to "Not authorized")
                        )
                    }
                }
            }
        }
        
        val response = client.get("/protected") {
            cookie("CF_Authorization", token)
        }
        
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("user@example.com"))
        
        unmockkObject(AppConfig)
    }
    
    @Test
    fun testCookieAuthWithInvalidJWTToken() = testApplication {
        mockkObject(AppConfig)
        every { AppConfig.cookieAuthEnabled } returns true
        every { AppConfig.cookieAuthUserField } returns "CF_Authorization"
        every { AppConfig.cookieAuthUserFieldJWTClaim } returns "email"
        every { AppConfig.jwtAuthEnabled } returns false
        every { AppConfig.headerAuthEnabled } returns false
        
        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            configureCookieAuth()
            
            routing {
                get("/protected") {
                    val subject = call.getSubject()
                    if (subject.isNotEmpty()) {
                        call.respond(mapOf("status" to "authenticated"))
                    } else {
                        call.respond(
                            HttpStatusCode.Unauthorized,
                            mapOf("error" to "Not authorized")
                        )
                    }
                }
            }
        }
        
        val response = client.get("/protected") {
            cookie("CF_Authorization", "invalid-jwt-token")
        }
        
        // Should return 401 Unauthorized (JWT decode will fail or return null)
        assertEquals(HttpStatusCode.Unauthorized, response.status)
        
        unmockkObject(AppConfig)
    }
    
    @Test
    fun testCookieAuthWithoutCookie() = testApplication {
        mockkObject(AppConfig)
        every { AppConfig.cookieAuthEnabled } returns true
        every { AppConfig.cookieAuthUserField } returns "CF_Authorization"
        every { AppConfig.cookieAuthUserFieldJWTClaim } returns "email"
        every { AppConfig.jwtAuthEnabled } returns false
        every { AppConfig.headerAuthEnabled } returns false
        
        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            configureCookieAuth()
            
            routing {
                get("/protected") {
                    val subject = call.getSubject()
                    if (subject.isNotEmpty()) {
                        call.respond(mapOf("status" to "authenticated"))
                    } else {
                        call.respond(
                            HttpStatusCode.Unauthorized,
                            mapOf("error" to "Not authorized")
                        )
                    }
                }
            }
        }
        
        val response = client.get("/protected")
        
        // Should return 401 Unauthorized
        assertEquals(HttpStatusCode.Unauthorized, response.status)
        
        unmockkObject(AppConfig)
    }
    
    @Test
    fun testCookieAuthWithJWTWithoutSecret() = testApplication {
        mockkObject(AppConfig)
        every { AppConfig.cookieAuthEnabled } returns true
        every { AppConfig.cookieAuthUserField } returns "CF_Authorization"
        every { AppConfig.cookieAuthUserFieldJWTClaim } returns "email"
        every { AppConfig.jwtAuthEnabled } returns false
        every { AppConfig.headerAuthEnabled } returns false
        
        // JWT token without verification (as per original implementation)
        // This matches the behavior in subject.go where JWT parsing skips error check
        val token = "eyJhbGciOiJIUzI1NiIsImtpZCI6IjEyMzQ1In0.eyJzdWIiOiIxMjM0NTY3ODkwIiwiZW1haWwiOiJhYmNAZXhhbXBsZS5jb20iLCJpYXQiOjE1MTYyMzkwMjJ9.tzRXenFic8Eqg2awzO0eiX6Rozy_mmsJVzLJfUUfREI"
        
        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            configureCookieAuth()
            
            routing {
                get("/protected") {
                    val subject = call.getSubject()
                    if (subject.isNotEmpty()) {
                        call.respond(mapOf("email" to subject))
                    } else {
                        call.respond(
                            HttpStatusCode.Unauthorized,
                            mapOf("error" to "Not authorized")
                        )
                    }
                }
            }
        }
        
        val response = client.get("/protected") {
            cookie("CF_Authorization", token)
        }
        
        // Should decode without verification (as per original)
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("abc@example.com"))
        
        unmockkObject(AppConfig)
    }
    
    @Test
    fun testCookieAuthWithExpiredJWT() = testApplication {
        mockkObject(AppConfig)
        every { AppConfig.cookieAuthEnabled } returns true
        every { AppConfig.cookieAuthUserField } returns "CF_Authorization"
        every { AppConfig.cookieAuthUserFieldJWTClaim } returns "email"
        every { AppConfig.jwtAuthEnabled } returns false
        every { AppConfig.headerAuthEnabled } returns false
        
        // Create expired JWT token
        val secret = "test-secret"
        val algorithm = Algorithm.HMAC256(secret)
        val expiredToken = JWT.create()
            .withClaim("email", "user@example.com")
            .withExpiresAt(java.util.Date(System.currentTimeMillis() - 1000)) // Expired 1 second ago
            .sign(algorithm)
        
        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            configureCookieAuth()
            
            routing {
                get("/protected") {
                    val subject = call.getSubject()
                    if (subject.isNotEmpty()) {
                        call.respond(mapOf("email" to subject))
                    } else {
                        call.respond(
                            HttpStatusCode.Unauthorized,
                            mapOf("error" to "Not authorized")
                        )
                    }
                }
            }
        }
        
        val response = client.get("/protected") {
            cookie("CF_Authorization", expiredToken)
        }
        
        // JWT.decode() doesn't verify expiration, so it will decode successfully
        // The email claim will be extracted, so subject will not be empty
        // This matches the original behavior where JWT.decode() doesn't check expiration
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("user@example.com"))
        
        unmockkObject(AppConfig)
    }
    
    @Test
    fun testCookieAuthWithWrongJWTSecret() = testApplication {
        mockkObject(AppConfig)
        every { AppConfig.cookieAuthEnabled } returns true
        every { AppConfig.cookieAuthUserField } returns "CF_Authorization"
        every { AppConfig.cookieAuthUserFieldJWTClaim } returns "email"
        every { AppConfig.jwtAuthEnabled } returns false
        every { AppConfig.headerAuthEnabled } returns false
        
        // Create JWT with different secret
        val wrongSecret = "wrong-secret"
        val algorithm = Algorithm.HMAC256(wrongSecret)
        val token = JWT.create()
            .withClaim("email", "user@example.com")
            .sign(algorithm)
        
        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            configureCookieAuth()
            
            routing {
                get("/protected") {
                    val subject = call.getSubject()
                    if (subject.isNotEmpty()) {
                        call.respond(mapOf("email" to subject))
                    } else {
                        call.respond(
                            HttpStatusCode.Unauthorized,
                            mapOf("error" to "Not authorized")
                        )
                    }
                }
            }
        }
        
        val response = client.get("/protected") {
            cookie("CF_Authorization", token)
        }
        
        // JWT.decode() doesn't verify signature, so it will decode successfully
        // Should return 200 with email claim
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("user@example.com"))
        
        unmockkObject(AppConfig)
    }
    
    @Test
    fun testCookieAuthWithMalformedJWT() = testApplication {
        mockkObject(AppConfig)
        every { AppConfig.cookieAuthEnabled } returns true
        every { AppConfig.cookieAuthUserField } returns "CF_Authorization"
        every { AppConfig.cookieAuthUserFieldJWTClaim } returns "email"
        every { AppConfig.jwtAuthEnabled } returns false
        every { AppConfig.headerAuthEnabled } returns false
        
        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            configureCookieAuth()
            
            routing {
                get("/protected") {
                    val subject = call.getSubject()
                    if (subject.isNotEmpty()) {
                        call.respond(mapOf("email" to subject))
                    } else {
                        call.respond(
                            HttpStatusCode.Unauthorized,
                            mapOf("error" to "Not authorized")
                        )
                    }
                }
            }
        }
        
        // Test with malformed JWT (not a valid JWT format)
        val response = client.get("/protected") {
            cookie("CF_Authorization", "not.a.valid.jwt")
        }
        
        // Should return 401 Unauthorized (JWT decode will fail)
        assertEquals(HttpStatusCode.Unauthorized, response.status)
        
        unmockkObject(AppConfig)
    }
}
