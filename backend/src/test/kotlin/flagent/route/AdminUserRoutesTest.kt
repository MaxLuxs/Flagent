package flagent.route

import flagent.domain.entity.User
import flagent.domain.repository.IUserRepository
import flagent.middleware.configureJWTAuth
import flagent.service.UserService
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.mindrot.jbcrypt.BCrypt
import kotlin.test.*

class AdminUserRoutesTest {

    private fun createUserServiceWithAdminUser(): UserService {
        val repo = mockk<IUserRepository>(relaxed = true)
        val adminHash = BCrypt.hashpw("secret123", BCrypt.gensalt())
        val adminUser = User(
            id = 1,
            email = "admin@test.com",
            name = "Admin",
            passwordHash = adminHash
        )
        coEvery { repo.findByEmail("admin@test.com") } returns adminUser
        coEvery { repo.findById(1) } returns adminUser
        coEvery { repo.findAll(any(), any()) } returns listOf(adminUser)
        coEvery { repo.count() } returns 1L
        return UserService(repo)
    }

    @Test
    fun `GET admin users with JWT returns 200 and list`() = testApplication {
        val userService = createUserServiceWithAdminUser()
        application {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true; encodeDefaults = true }) }
            configureJWTAuth()
            routing {
                configureAuthRoutes(userService)
                configureAdminUserRoutes(userService)
            }
        }
        val loginResponse = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"admin@test.com","password":"secret123"}""")
        }
        assertEquals(HttpStatusCode.Created, loginResponse.status)
        val token = Json.parseToJsonElement(loginResponse.bodyAsText()).jsonObject["token"]?.jsonPrimitive?.content
            ?: fail("no token in response")
        val response = client.get("/admin/users") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("admin@test.com"))
        assertTrue(response.headers["X-Total-Count"] == "1")
    }

    @Test
    fun `GET admin users with limit and offset`() = testApplication {
        val userService = createUserServiceWithAdminUser()
        application {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true; encodeDefaults = true }) }
            configureJWTAuth()
            routing {
                configureAuthRoutes(userService)
                configureAdminUserRoutes(userService)
            }
        }
        val loginResponse = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"admin@test.com","password":"secret123"}""")
        }
        val token = Json.parseToJsonElement(loginResponse.bodyAsText()).jsonObject["token"]?.jsonPrimitive?.content
            ?: fail("no token")
        val response = client.get("/admin/users?limit=10&offset=0") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `GET admin users without auth returns 401`() = testApplication {
        val userService = createUserServiceWithAdminUser()
        application {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true; encodeDefaults = true }) }
            configureJWTAuth()
            routing { configureAdminUserRoutes(userService) }
        }
        val response = client.get("/admin/users")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `GET admin users by id with JWT returns 200`() = testApplication {
        val userService = createUserServiceWithAdminUser()
        application {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true; encodeDefaults = true }) }
            configureJWTAuth()
            routing {
                configureAuthRoutes(userService)
                configureAdminUserRoutes(userService)
            }
        }
        val loginResponse = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"admin@test.com","password":"secret123"}""")
        }
        val token = Json.parseToJsonElement(loginResponse.bodyAsText()).jsonObject["token"]?.jsonPrimitive?.content
            ?: fail("no token")
        val response = client.get("/admin/users/1") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `GET admin users by id invalid returns 400`() = testApplication {
        val userService = createUserServiceWithAdminUser()
        application {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true; encodeDefaults = true }) }
            configureJWTAuth()
            routing {
                configureAuthRoutes(userService)
                configureAdminUserRoutes(userService)
            }
        }
        val loginResponse = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"admin@test.com","password":"secret123"}""")
        }
        val token = Json.parseToJsonElement(loginResponse.bodyAsText()).jsonObject["token"]?.jsonPrimitive?.content
            ?: fail("no token")
        val response = client.get("/admin/users/notanum") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }
}
