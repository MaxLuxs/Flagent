package flagent.route

import flagent.domain.entity.User
import flagent.domain.repository.IUserRepository
import flagent.middleware.addJwtProvider
import flagent.service.UserService
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.Authentication
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import io.mockk.coEvery
import io.mockk.coJustRun
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
            install(Authentication) {
                addJwtProvider()
            }
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
            install(Authentication) {
                addJwtProvider()
            }
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
            install(Authentication) {
                addJwtProvider()
            }
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
            install(Authentication) {
                addJwtProvider()
            }
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
            install(Authentication) {
                addJwtProvider()
            }
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

    @Test
    fun `POST admin users with invalid body returns 400`() = testApplication {
        val userService = createUserServiceWithAdminUser()
        application {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true; encodeDefaults = true }) }
            install(Authentication) { addJwtProvider() }
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

        val response = client.post("/admin/users") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("{invalid-json")
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains("Invalid request body"))
    }

    @Test
    fun `POST admin users with blank email or password returns 400`() = testApplication {
        val userService = createUserServiceWithAdminUser()
        application {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true; encodeDefaults = true }) }
            install(Authentication) { addJwtProvider() }
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

        val response = client.post("/admin/users") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{"email":"","password":""}""")
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains("Email and password are required"))
    }

    @Test
    fun `POST admin users maps IllegalArgumentException to 400`() = testApplication {
        val repo = mockk<IUserRepository>(relaxed = true)
        coEvery { repo.findByEmail("admin@test.com") } returns User(id = 1, email = "admin@test.com", name = "Admin", passwordHash = "hash")
        coEvery { repo.create(any()) } throws IllegalArgumentException("duplicate")
        val userService = UserService(repo)

        application {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true; encodeDefaults = true }) }
            install(Authentication) { addJwtProvider() }
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

        val response = client.post("/admin/users") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{"email":"new@test.com","password":"pwd"}""")
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `GET admin user by id returns 404 when not found`() = testApplication {
        val repo = mockk<IUserRepository>(relaxed = true)
        coEvery { repo.findByEmail("admin@test.com") } returns User(id = 1, email = "admin@test.com", name = "Admin", passwordHash = "hash")
        coEvery { repo.findById(1) } returns null
        val userService = UserService(repo)

        application {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true; encodeDefaults = true }) }
            install(Authentication) { addJwtProvider() }
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
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `PUT admin user with invalid body returns 400`() = testApplication {
        val userService = createUserServiceWithAdminUser()
        application {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true; encodeDefaults = true }) }
            install(Authentication) { addJwtProvider() }
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
        val response = client.put("/admin/users/1") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("{invalid")
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `PUT admin user maps NoSuchElementException to 404`() = testApplication {
        val repo = mockk<IUserRepository>(relaxed = true)
        val adminHash = BCrypt.hashpw("secret123", BCrypt.gensalt())
        val adminUser = User(id = 1, email = "admin@test.com", name = "Admin", passwordHash = adminHash)
        coEvery { repo.findByEmail("admin@test.com") } returns adminUser
        coEvery { repo.findById(1) } returns adminUser
        coEvery { repo.update(any()) } throws NoSuchElementException("user not found")
        val userService = UserService(repo)

        application {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true; encodeDefaults = true }) }
            install(Authentication) { addJwtProvider() }
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
        val response = client.put("/admin/users/1") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{"name":"New Name"}""")
        }
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `DELETE admin user maps NoSuchElementException to 404`() = testApplication {
        val repo = mockk<IUserRepository>(relaxed = true)
        val adminHash = BCrypt.hashpw("secret123", BCrypt.gensalt())
        val adminUser = User(id = 1, email = "admin@test.com", name = "Admin", passwordHash = adminHash)
        coEvery { repo.findByEmail("admin@test.com") } returns adminUser
        coEvery { repo.findById(1) } returns adminUser
        coJustRun { repo.softDelete(1) }
        coEvery { repo.softDelete(2) } throws NoSuchElementException("user not found")
        val userService = UserService(repo)

        application {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true; encodeDefaults = true }) }
            install(Authentication) { addJwtProvider() }
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
        val response = client.delete("/admin/users/2") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `POST admin users block and unblock invalid id returns 400`() = testApplication {
        val userService = createUserServiceWithAdminUser()
        application {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true; encodeDefaults = true }) }
            install(Authentication) { addJwtProvider() }
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
        val blockResponse = client.post("/admin/users/notanum/block") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.BadRequest, blockResponse.status)
        val unblockResponse = client.post("/admin/users/notanum/unblock") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.BadRequest, unblockResponse.status)
    }

    @Test
    fun `POST admin users block maps NoSuchElementException to 404`() = testApplication {
        val repo = mockk<IUserRepository>(relaxed = true)
        val adminHash = BCrypt.hashpw("secret123", BCrypt.gensalt())
        val adminUser = User(id = 1, email = "admin@test.com", name = "Admin", passwordHash = adminHash)
        coEvery { repo.findByEmail("admin@test.com") } returns adminUser
        coEvery { repo.findById(1) } returns null
        coEvery { repo.setBlockedAt(1, any()) } throws NoSuchElementException("user not found")
        val userService = UserService(repo)

        application {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true; encodeDefaults = true }) }
            install(Authentication) { addJwtProvider() }
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
        val response = client.post("/admin/users/1/block") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.NotFound, response.status)
    }
}
