package flagent.route

import flagent.config.AppConfig
import flagent.service.UserService
import flagent.util.JwtUtils
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.post
import kotlinx.serialization.Serializable
import mu.KotlinLogging
import org.mindrot.jbcrypt.BCrypt

private val logger = KotlinLogging.logger {}

@Serializable
data class LoginRequest(val email: String = "", val password: String = "")

@Serializable
data class LoginUserResponse(val id: String, val email: String, val name: String)

@Serializable
data class LoginResponse(val token: String, val user: LoginUserResponse)

/**
 * Auth routes: POST /auth/login for admin login (email/password).
 * First checks users in DB (UserService); if not found or wrong password, falls back to
 * FLAGENT_ADMIN_EMAIL + FLAGENT_ADMIN_PASSWORD / FLAGENT_ADMIN_PASSWORD_HASH.
 * Returns JWT with admin claim for use with admin routes.
 */
fun Routing.configureAuthRoutes(userService: UserService) {
    post("/auth/login") {
        if (!AppConfig.adminAuthEnabled) {
            call.respond(HttpStatusCode.NotImplemented, mapOf("error" to "Admin auth is disabled"))
            return@post
        }
        val req = runCatching { call.receive<LoginRequest>() }.getOrElse {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid request body"))
            return@post
        }
        val reqEmail = req.email.trim()
        val reqPassword = req.password
        if (reqEmail.isBlank() || reqPassword.isBlank()) {
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "Email and password are required")
            )
            return@post
        }
        val secret = AppConfig.jwtAuthSecret
        if (secret.isEmpty()) {
            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("error" to "JWT secret not configured")
            )
            return@post
        }
        val dbUser = userService.validatePassword(reqEmail, reqPassword)
        if (dbUser != null) {
            val token = try {
                JwtUtils.createAdminToken(reqEmail, secret, AppConfig.jwtAuthUserClaim)
            } catch (e: Exception) {
                logger.error(e) { "Failed to create admin token" }
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Failed to create token")
                )
                return@post
            }
            val name = dbUser.name ?: reqEmail.substringBefore("@")
            call.respond(
                HttpStatusCode.Created, LoginResponse(
                    token = token,
                    user = LoginUserResponse(
                        id = dbUser.id.toString(),
                        email = reqEmail,
                        name = name
                    )
                )
            )
            return@post
        }
        val email = AppConfig.adminEmail
        val password = AppConfig.adminPassword
        val passwordHash = AppConfig.adminPasswordHash
        if (email.isBlank() || (password.isBlank() && passwordHash.isBlank())) {
            call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid email or password"))
            return@post
        }
        if (reqEmail != email) {
            call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid email or password"))
            return@post
        }
        val passwordValid = if (passwordHash.isNotBlank()) {
            runCatching { BCrypt.checkpw(reqPassword, passwordHash) }.getOrElse { false }
        } else {
            reqPassword == password
        }
        if (!passwordValid) {
            call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid email or password"))
            return@post
        }
        val token = try {
            JwtUtils.createAdminToken(reqEmail, secret, AppConfig.jwtAuthUserClaim)
        } catch (e: Exception) {
            logger.error(e) { "Failed to create admin token" }
            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("error" to "Failed to create token")
            )
            return@post
        }
        val name = reqEmail.substringBefore("@")
        call.respond(
            HttpStatusCode.Created, LoginResponse(
                token = token,
                user = LoginUserResponse(id = "admin", email = reqEmail, name = name)
            )
        )
    }
}
