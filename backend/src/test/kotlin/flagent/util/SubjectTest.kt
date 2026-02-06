package flagent.util

import flagent.config.AppConfig
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

class SubjectTest {
    
    @Test
    fun testGetSubjectFromJWT() = testApplication {
        mockkObject(AppConfig)
        every { AppConfig.jwtAuthEnabled } returns true
        every { AppConfig.jwtAuthUserClaim } returns "sub"
        every { AppConfig.headerAuthEnabled } returns false
        every { AppConfig.cookieAuthEnabled } returns false
        
        // Setup JWT auth
        val secret = "test-secret"
        val algorithm = Algorithm.HMAC256(secret)
        val token = JWT.create()
            .withClaim("sub", "foo@example.com")
            .sign(algorithm)
        
        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            install(Authentication) {
                jwt("jwt") {
                    verifier(JWT.require(algorithm).build())
                    validate { credential ->
                        val userClaim = credential.payload.getClaim("sub")?.asString()
                        if (userClaim != null) {
                            JWTPrincipal(credential.payload)
                        } else {
                            null
                        }
                    }
                }
            }
            
            routing {
                authenticate("jwt") {
                    get("/test") {
                        val subject = call.getSubject()
                        call.respond(mapOf("subject" to subject))
                    }
                }
            }
        }
        
        // Test with JWT token
        val response = client.get("/test") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("foo@example.com"))
        
        unmockkObject(AppConfig)
    }
    
    @Test
    fun testGetSubjectFromHeader() = testApplication {
        mockkObject(AppConfig)
        every { AppConfig.jwtAuthEnabled } returns false
        every { AppConfig.headerAuthEnabled } returns true
        every { AppConfig.headerAuthUserField } returns "X-Email"
        every { AppConfig.cookieAuthEnabled } returns false
        
        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            routing {
                    get("/test") {
                        val subject = call.getSubject()
                        call.respond(mapOf("subject" to subject))
                    }
                }
        }
        
        // Test with header (when header auth is enabled)
        val response = client.get("/test") {
            header("X-Email", "foo@example.com")
        }
        
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("foo@example.com"))
        
        unmockkObject(AppConfig)
    }
    
    @Test
    fun testGetSubjectFromCookie() = testApplication {
        mockkObject(AppConfig)
        every { AppConfig.jwtAuthEnabled } returns false
        every { AppConfig.headerAuthEnabled } returns false
        every { AppConfig.cookieAuthEnabled } returns true
        every { AppConfig.cookieAuthUserField } returns "CF_Authorization"
        every { AppConfig.cookieAuthUserFieldJWTClaim } returns "email"
        
        // Create a JWT token with email claim
        val cookieValue = "eyJhbGciOiJIUzI1NiIsImtpZCI6IjEyMzQ1In0.eyJzdWIiOiIxMjM0NTY3ODkwIiwiZW1haWwiOiJhYmNAZXhhbXBsZS5jb20iLCJpYXQiOjE1MTYyMzkwMjJ9.tzRXenFic8Eqg2awzO0eiX6Rozy_mmsJVzLJfUUfREI"
        
        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            routing {
                    get("/test") {
                        val subject = call.getSubject()
                        call.respond(mapOf("subject" to subject))
                    }
                }
        }
        
        // Test with cookie (when cookie auth is enabled)
        val response = client.get("/test") {
            cookie("CF_Authorization", cookieValue)
        }
        
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("abc@example.com"))
        
        unmockkObject(AppConfig)
    }
    
    @Test
    fun testGetSubjectFromCookieWithoutJWT() = testApplication {
        mockkObject(AppConfig)
        every { AppConfig.jwtAuthEnabled } returns false
        every { AppConfig.headerAuthEnabled } returns false
        every { AppConfig.cookieAuthEnabled } returns true
        every { AppConfig.cookieAuthUserField } returns "CF_Authorization"
        every { AppConfig.cookieAuthUserFieldJWTClaim } returns ""
        
        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            routing {
                    get("/test") {
                        val subject = call.getSubject()
                        call.respond(mapOf("subject" to subject))
                    }
                }
        }
        
        // Test with simple cookie value (no JWT)
        val response = client.get("/test") {
            cookie("CF_Authorization", "simple-cookie-value")
        }
        
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("simple-cookie-value"))
        
        unmockkObject(AppConfig)
    }
    
    @Test
    fun testGetSubjectReturnsEmptyStringWhenNoAuth() = testApplication {
        mockkObject(AppConfig)
        every { AppConfig.jwtAuthEnabled } returns false
        every { AppConfig.headerAuthEnabled } returns false
        every { AppConfig.cookieAuthEnabled } returns false
        
        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            routing {
                get("/test") {
                    val subject = call.getSubject()
                    call.respond(mapOf("subject" to subject))
                }
            }
        }
        
        val response = client.get("/test")
        
        assertEquals(HttpStatusCode.OK, response.status)
        // Should return empty string if not found
        assertTrue(response.bodyAsText().contains("\"subject\":\"\""))
        
        unmockkObject(AppConfig)
    }
    
    @Test
    fun testGetSubjectFromJWTWithMissingClaim() = testApplication {
        mockkObject(AppConfig)
        every { AppConfig.jwtAuthEnabled } returns true
        every { AppConfig.jwtAuthUserClaim } returns "sub"
        every { AppConfig.headerAuthEnabled } returns false
        every { AppConfig.cookieAuthEnabled } returns false
        
        val secret = "test-secret"
        val algorithm = Algorithm.HMAC256(secret)
        val token = JWT.create()
            .withClaim("other_claim", "value")
            .sign(algorithm)
        
        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            install(Authentication) {
                jwt("jwt") {
                    verifier(JWT.require(algorithm).build())
                    validate { credential ->
                        JWTPrincipal(credential.payload)
                    }
                }
            }
            
            routing {
                authenticate("jwt") {
                    get("/test") {
                        val subject = call.getSubject()
                        call.respond(mapOf("subject" to (if (subject.isEmpty()) "null" else subject)))
                    }
                }
            }
        }
        
        val response = client.get("/test") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        
        assertEquals(HttpStatusCode.OK, response.status)
        // Should return empty string (not null) if claim is missing, but we check for "null" in response
        assertTrue(response.bodyAsText().contains("\"subject\":\"\"") || response.bodyAsText().contains("null"))
        
        unmockkObject(AppConfig)
    }
    
    @Test
    fun testGetSubjectFromCookieWithEmptyJWTClaim() = testApplication {
        mockkObject(AppConfig)
        every { AppConfig.jwtAuthEnabled } returns false
        every { AppConfig.headerAuthEnabled } returns false
        every { AppConfig.cookieAuthEnabled } returns true
        every { AppConfig.cookieAuthUserField } returns "CF_Authorization"
        every { AppConfig.cookieAuthUserFieldJWTClaim } returns "email"
        
        // JWT token without email claim
        val cookieValue = "eyJhbGciOiJIUzI1NiIsImtpZCI6IjEyMzQ1In0.eyJzdWIiOiIxMjM0NTY3ODkwIiwiaWF0IjoxNTE2MjM5MDIyfQ.C_YsEkcHa7aSVQILzJAayFgJk-sj1cmNWIWUm7m7vy4"
        
        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            routing {
                    get("/test") {
                        val subject = call.getSubject()
                        call.respond(mapOf("subject" to (if (subject.isEmpty()) "null" else subject)))
                    }
                }
        }
        
        val response = client.get("/test") {
            cookie("CF_Authorization", cookieValue)
        }
        
        assertEquals(HttpStatusCode.OK, response.status)
        // Should return empty string (not null) if JWT claim is missing, but we check for "null" in response
        assertTrue(response.bodyAsText().contains("\"subject\":\"\"") || response.bodyAsText().contains("null"))
        
        unmockkObject(AppConfig)
    }
    
    @Test
    fun testGetSubjectPriority_JWTOverHeader() = testApplication {
        mockkObject(AppConfig)
        every { AppConfig.jwtAuthEnabled } returns true
        every { AppConfig.jwtAuthUserClaim } returns "sub"
        every { AppConfig.headerAuthEnabled } returns true
        every { AppConfig.headerAuthUserField } returns "X-Email"
        every { AppConfig.cookieAuthEnabled } returns false
        
        val secret = "test-secret"
        val algorithm = Algorithm.HMAC256(secret)
        val token = JWT.create()
            .withClaim("sub", "jwt-user@example.com")
            .sign(algorithm)
        
        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            install(Authentication) {
                jwt("jwt") {
                    verifier(JWT.require(algorithm).build())
                    validate { credential ->
                        val userClaim = credential.payload.getClaim("sub")?.asString()
                        if (userClaim != null) {
                            JWTPrincipal(credential.payload)
                        } else {
                            null
                        }
                    }
                }
            }
            
            routing {
                authenticate("jwt") {
                    get("/test") {
                        val subject = call.getSubject()
                        call.respond(mapOf("subject" to subject))
                    }
                }
            }
        }
        
        // Both JWT and header are present - JWT should take priority
        val response = client.get("/test") {
            header(HttpHeaders.Authorization, "Bearer $token")
            header("X-Email", "header-user@example.com")
        }
        
        assertEquals(HttpStatusCode.OK, response.status)
        // Should return JWT user, not header user
        assertTrue(response.bodyAsText().contains("jwt-user@example.com"))
        assertFalse(response.bodyAsText().contains("header-user@example.com"))
        
        unmockkObject(AppConfig)
    }
    
    @Test
    fun testGetSubjectPriority_HeaderOverCookie() = testApplication {
        mockkObject(AppConfig)
        every { AppConfig.jwtAuthEnabled } returns false
        every { AppConfig.headerAuthEnabled } returns true
        every { AppConfig.headerAuthUserField } returns "X-Email"
        every { AppConfig.cookieAuthEnabled } returns true
        every { AppConfig.cookieAuthUserField } returns "CF_Authorization"
        every { AppConfig.cookieAuthUserFieldJWTClaim } returns "email"
        
        val cookieValue = "eyJhbGciOiJIUzI1NiIsImtpZCI6IjEyMzQ1In0.eyJzdWIiOiIxMjM0NTY3ODkwIiwiZW1haWwiOiJjb29raWVAZXhhbXBsZS5jb20iLCJpYXQiOjE1MTYyMzkwMjJ9.test"
        
        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            routing {
                get("/test") {
                    val subject = call.getSubject()
                    call.respond(mapOf("subject" to subject))
                }
            }
        }
        
        // Both header and cookie are present - header should take priority
        val response = client.get("/test") {
            header("X-Email", "header-user@example.com")
            cookie("CF_Authorization", cookieValue)
        }
        
        assertEquals(HttpStatusCode.OK, response.status)
        // Should return header user, not cookie user
        assertTrue(response.bodyAsText().contains("header-user@example.com"))
        
        unmockkObject(AppConfig)
    }
    
    @Test
    fun testGetSubjectWithMalformedJWTInCookie() = testApplication {
        mockkObject(AppConfig)
        every { AppConfig.jwtAuthEnabled } returns false
        every { AppConfig.headerAuthEnabled } returns false
        every { AppConfig.cookieAuthEnabled } returns true
        every { AppConfig.cookieAuthUserField } returns "CF_Authorization"
        every { AppConfig.cookieAuthUserFieldJWTClaim } returns "email"
        
        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            routing {
                get("/test") {
                    val subject = call.getSubject()
                    call.respond(mapOf("subject" to (if (subject.isEmpty()) "empty" else subject)))
                }
            }
        }
        
        // Test with malformed JWT in cookie
        val response = client.get("/test") {
            cookie("CF_Authorization", "not.a.valid.jwt.token")
        }
        
        assertEquals(HttpStatusCode.OK, response.status)
        // Should return empty string when JWT decode fails
        assertTrue(response.bodyAsText().contains("\"subject\":\"empty\"") || response.bodyAsText().contains("\"subject\":\"\""))
        
        unmockkObject(AppConfig)
    }
    
    @Test
    fun testGetSubjectWithMalformedJWTInHeader() = testApplication {
        mockkObject(AppConfig)
        every { AppConfig.jwtAuthEnabled } returns true
        every { AppConfig.jwtAuthUserClaim } returns "sub"
        every { AppConfig.headerAuthEnabled } returns false
        every { AppConfig.cookieAuthEnabled } returns false
        
        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            install(Authentication) {
                jwt("jwt") {
                    // Use a dummy verifier that will reject malformed tokens
                    verifier(JWT.require(Algorithm.HMAC256("secret")).build())
                    validate { credential ->
                        JWTPrincipal(credential.payload)
                    }
                }
            }
            
            routing {
                get("/test") {
                    val subject = call.getSubject()
                    call.respond(mapOf("subject" to (if (subject.isEmpty()) "empty" else subject)))
                }
            }
        }
        
        // Test with malformed JWT in Authorization header
        val response = client.get("/test") {
            header(HttpHeaders.Authorization, "Bearer not.a.valid.jwt.token")
        }
        
        assertEquals(HttpStatusCode.OK, response.status)
        // Should return empty string when JWT verification fails
        assertTrue(response.bodyAsText().contains("\"subject\":\"empty\"") || response.bodyAsText().contains("\"subject\":\"\""))
        
        unmockkObject(AppConfig)
    }
}
