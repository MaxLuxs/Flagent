package flagent.route

import flagent.config.AppConfig
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import kotlin.test.*

class AuthRoutesTest {

    @Test
    fun `POST auth login returns 501 when admin auth disabled`() = testApplication {
        if (AppConfig.adminAuthEnabled) return@testApplication
        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true; encodeDefaults = true })
            }
            routing {
                configureAuthRoutes()
            }
        }
        val response = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"admin@test","password":"pass"}""")
        }
        assertEquals(HttpStatusCode.NotImplemented, response.status)
        assertTrue(response.bodyAsText().contains("Admin auth is disabled"))
    }

    @Test
    fun `POST auth login with invalid JSON returns 400 when auth enabled`() = testApplication {
        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true; encodeDefaults = true })
            }
            routing {
                configureAuthRoutes()
            }
        }
        val response = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{invalid}""")
        }
        if (AppConfig.adminAuthEnabled) {
            assertEquals(HttpStatusCode.BadRequest, response.status)
        } else {
            assertEquals(HttpStatusCode.NotImplemented, response.status)
        }
    }

    @Test
    fun `POST auth login returns 201 with valid credentials when auth enabled`() = testApplication {
        if (!AppConfig.adminAuthEnabled) return@testApplication
        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true; encodeDefaults = true })
            }
            routing {
                configureAuthRoutes()
            }
        }
        val response = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"admin@test.com","password":"secret123"}""")
        }
        assertEquals(HttpStatusCode.Created, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("token"))
        assertTrue(body.contains("admin@test.com"))
        assertTrue(body.contains("user"))
    }

    @Test
    fun `POST auth login returns 401 with wrong email when auth enabled`() = testApplication {
        if (!AppConfig.adminAuthEnabled) return@testApplication
        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true; encodeDefaults = true })
            }
            routing {
                configureAuthRoutes()
            }
        }
        val response = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"other@test.com","password":"secret123"}""")
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertTrue(response.bodyAsText().contains("Invalid email or password"))
    }

    @Test
    fun `POST auth login returns 401 with wrong password when auth enabled`() = testApplication {
        if (!AppConfig.adminAuthEnabled) return@testApplication
        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true; encodeDefaults = true })
            }
            routing {
                configureAuthRoutes()
            }
        }
        val response = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"admin@test.com","password":"wrong"}""")
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertTrue(response.bodyAsText().contains("Invalid email or password"))
    }

    @Test
    fun `POST auth login returns 400 when email blank and auth enabled`() = testApplication {
        if (!AppConfig.adminAuthEnabled) return@testApplication
        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true; encodeDefaults = true })
            }
            routing {
                configureAuthRoutes()
            }
        }
        val response = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"","password":"secret123"}""")
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains("Email and password are required"))
    }

    @Test
    fun `POST auth login returns 400 when password blank and auth enabled`() = testApplication {
        if (!AppConfig.adminAuthEnabled) return@testApplication
        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true; encodeDefaults = true })
            }
            routing {
                configureAuthRoutes()
            }
        }
        val response = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"admin@test.com","password":""}""")
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains("Email and password are required"))
    }
}
