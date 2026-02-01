package flagent.route

import flagent.config.AppConfig
import flagent.util.JwtUtils
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

@Serializable
data class LoginRequest(val email: String = "", val password: String = "")

@Serializable
data class LoginUserResponse(val id: String, val email: String, val name: String)

@Serializable
data class LoginResponse(val token: String, val user: LoginUserResponse)

/**
 * Auth routes: POST /auth/login for admin login (email/password).
 * When FLAGENT_ADMIN_AUTH_ENABLED=true, validates against FLAGENT_ADMIN_EMAIL and FLAGENT_ADMIN_PASSWORD.
 * Returns JWT with admin claim for use with admin routes.
 */
fun Routing.configureAuthRoutes() {
    post("/auth/login") {
        if (!AppConfig.adminAuthEnabled) {
            call.respond(HttpStatusCode.NotImplemented, mapOf("error" to "Admin auth is disabled"))
            return@post
        }
        val email = AppConfig.adminEmail
        val password = AppConfig.adminPassword
        val passwordHash = AppConfig.adminPasswordHash
        if (email.isBlank() || (password.isBlank() && passwordHash.isBlank())) {
            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Admin credentials not configured"))
            return@post
        }
        val req = runCatching { call.receive<LoginRequest>() }.getOrElse {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid request body"))
            return@post
        }
        val reqEmail = req.email.trim()
        val reqPassword = req.password
        if (reqEmail.isBlank() || reqPassword.isBlank()) {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Email and password are required"))
            return@post
        }
        if (reqEmail != email) {
            call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid email or password"))
            return@post
        }
        val passwordValid = if (passwordHash.isNotBlank()) {
            // FLAGENT_ADMIN_PASSWORD_HASH not yet supported; use FLAGENT_ADMIN_PASSWORD
            false
        } else {
            reqPassword == password
        }
        if (!passwordValid) {
            call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid email or password"))
            return@post
        }
        val secret = AppConfig.jwtAuthSecret
        if (secret.isEmpty()) {
            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "JWT secret not configured"))
            return@post
        }
        val token = try {
            JwtUtils.createAdminToken(reqEmail, secret, AppConfig.jwtAuthUserClaim)
        } catch (e: Exception) {
            logger.error(e) { "Failed to create admin token" }
            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to create token"))
            return@post
        }
        val name = reqEmail.substringBefore("@")
        call.respond(HttpStatusCode.Created, LoginResponse(
            token = token,
            user = LoginUserResponse(id = "admin", email = reqEmail, name = name)
        ))
    }
}
