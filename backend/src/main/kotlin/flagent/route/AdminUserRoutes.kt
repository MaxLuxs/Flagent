package flagent.route

import flagent.domain.entity.User
import flagent.service.UserService
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import kotlinx.serialization.Serializable
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

@Serializable
data class AdminUserResponse(
    val id: Int,
    val email: String?,
    val name: String?,
    val createdAt: String?,
    val updatedAt: String?,
    val blockedAt: String?
)

@Serializable
data class CreateAdminUserRequest(
    val email: String = "",
    val password: String = "",
    val name: String? = null
)

@Serializable
data class UpdateAdminUserRequest(val name: String? = null, val password: String? = null)

private fun User.toResponse(): AdminUserResponse = AdminUserResponse(
    id = id,
    email = email,
    name = name,
    createdAt = createdAt?.toString(),
    updatedAt = updatedAt?.toString(),
    blockedAt = blockedAt?.toString()
)

/**
 * Admin user management routes: list, create, get, update, delete, block, unblock.
 * All require JWT authentication.
 */
fun Routing.configureAdminUserRoutes(userService: UserService) {
    route("/admin") {
        authenticate("jwt") {
            route("/users") {
                get {
                    val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 50
                    val offset = call.request.queryParameters["offset"]?.toIntOrNull() ?: 0
                    val users = userService.findAll(limit, offset)
                    val total = userService.count()
                    call.response.headers.append("X-Total-Count", total.toString())
                    call.respond(users.map { it.toResponse() })
                }
                post {
                    val req = runCatching { call.receive<CreateAdminUserRequest>() }.getOrElse {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to "Invalid request body")
                        )
                        return@post
                    }
                    val email = req.email.trim()
                    val password = req.password
                    if (email.isBlank() || password.isBlank()) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to "Email and password are required")
                        )
                        return@post
                    }
                    try {
                        val user = userService.create(email, password, req.name)
                        call.respond(HttpStatusCode.Created, user.toResponse())
                    } catch (e: IllegalArgumentException) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to (e.message ?: "Bad request"))
                        )
                    }
                }
                get("/{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id == null) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid user id"))
                        return@get
                    }
                    val user = userService.findById(id)
                    if (user == null) {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "User not found"))
                        return@get
                    }
                    call.respond(user.toResponse())
                }
                put("/{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id == null) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid user id"))
                        return@put
                    }
                    val req = runCatching { call.receive<UpdateAdminUserRequest>() }.getOrElse {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to "Invalid request body")
                        )
                        return@put
                    }
                    try {
                        val user = userService.update(id, req.name, req.password)
                        call.respond(user.toResponse())
                    } catch (e: NoSuchElementException) {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "User not found"))
                    }
                }
                delete("/{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id == null) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid user id"))
                        return@delete
                    }
                    try {
                        userService.softDelete(id)
                        call.respond(HttpStatusCode.NoContent)
                    } catch (e: NoSuchElementException) {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "User not found"))
                    }
                }
                post("/{id}/block") {
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id == null) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid user id"))
                        return@post
                    }
                    try {
                        userService.block(id)
                        val user = userService.findById(id)
                        call.respond(HttpStatusCode.OK, user!!.toResponse())
                    } catch (e: NoSuchElementException) {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "User not found"))
                    }
                }
                post("/{id}/unblock") {
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id == null) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid user id"))
                        return@post
                    }
                    try {
                        userService.unblock(id)
                        val user = userService.findById(id)
                        call.respond(HttpStatusCode.OK, user!!.toResponse())
                    } catch (e: NoSuchElementException) {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "User not found"))
                    }
                }
            }
        }
    }
}
