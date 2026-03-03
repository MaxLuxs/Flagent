package flagent.route

import flagent.repository.Database
import flagent.repository.tables.NewsletterSubscribers
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.*
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.post
import kotlinx.serialization.Serializable
import mu.KotlinLogging
import java.util.regex.Pattern

private val logger = KotlinLogging.logger {}

private val EMAIL_REGEX = Pattern.compile(
    "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
)

@Serializable
data class NewsletterSubscribeRequest(val email: String = "")

private sealed class SubscribeResult {
    data object Success : SubscribeResult()
    data object AlreadySubscribed : SubscribeResult()
    data class Error(val message: String) : SubscribeResult()
}

/**
 * Public newsletter subscription (no API key).
 * POST /newsletter — body: { "email": "user@example.com" }
 */
fun Routing.configureNewsletterRoutes() {
    post("/newsletter") {
        try {
            val req = call.receive<NewsletterSubscribeRequest>()
            val raw = req.email.trim()
            if (raw.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Email is required"))
                return@post
            }
            if (raw.length > 255) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Email too long"))
                return@post
            }
            if (!EMAIL_REGEX.matcher(raw).matches()) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid email format"))
                return@post
            }
            val emailNorm = raw.lowercase()
            val result = Database.transaction {
                val exists = NewsletterSubscribers.selectAll().where { NewsletterSubscribers.email eq emailNorm }.limit(1).firstOrNull()
                if (exists != null) return@transaction SubscribeResult.AlreadySubscribed
                try {
                    NewsletterSubscribers.insert {
                        it[NewsletterSubscribers.email] = emailNorm
                        it[NewsletterSubscribers.createdAt] = java.time.LocalDateTime.now()
                    }
                    SubscribeResult.Success
                } catch (e: Exception) {
                    if (e.message?.contains("unique") == true || e.message?.contains("duplicate") == true) {
                        SubscribeResult.AlreadySubscribed
                    } else {
                        SubscribeResult.Error(e.message ?: "Subscription failed")
                    }
                }
            }
            when (result) {
                SubscribeResult.Success -> {
                    logger.info { "Newsletter subscribed: $emailNorm" }
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Subscribed successfully"))
                }
                SubscribeResult.AlreadySubscribed ->
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Already subscribed"))
                is SubscribeResult.Error -> {
                    logger.warn { "Newsletter subscribe failed: ${result.message}" }
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to result.message))
                }
            }
        } catch (e: Exception) {
            if (e.message?.contains("unique") == true || e.message?.contains("duplicate") == true) {
                call.respond(HttpStatusCode.OK, mapOf("message" to "Already subscribed"))
                return@post
            }
            logger.warn(e) { "Newsletter subscribe failed: ${e.message}" }
            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("error" to (e.message ?: "Subscription failed"))
            )
        }
    }
}
