package flagent.route

import flagent.api.constants.ApiConstants
import flagent.domain.entity.Webhook
import flagent.domain.entity.WebhookEvents
import flagent.service.WebhookService
import io.ktor.http.HttpStatusCode
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

private val log = KotlinLogging.logger {}

@Serializable
data class CreateWebhookRequest(
    val url: String,
    val events: List<String>,
    val secret: String? = null,
    val enabled: Boolean = true
)

@Serializable
data class PutWebhookRequest(
    val url: String,
    val events: List<String>,
    val secret: String? = null,
    val enabled: Boolean = true
)

fun Routing.configureWebhookRoutes(webhookService: WebhookService) {
    route(ApiConstants.API_BASE_PATH) {
        route("/webhooks") {
            get {
                val webhooks = webhookService.findAll()
                call.respond(webhooks)
            }
            post {
                val request = call.receive<CreateWebhookRequest>()
                if (request.url.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "URL is required"))
                    return@post
                }
                val validEvents = request.events.filter { it in WebhookEvents.ALL }
                if (validEvents.isEmpty()) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "At least one valid event is required")
                    )
                    return@post
                }
                val webhook = Webhook(
                    url = request.url,
                    events = validEvents,
                    secret = request.secret,
                    enabled = request.enabled
                )
                val created = webhookService.create(webhook)
                call.respond(HttpStatusCode.Created, created)
            }
            route("/{webhookId}") {
                get {
                    val id = call.parameters["webhookId"]?.toIntOrNull()
                        ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid webhook ID")
                    val webhook = webhookService.findById(id)
                        ?: return@get call.respond(HttpStatusCode.NotFound, "Webhook not found")
                    call.respond(webhook)
                }
                put {
                    val id = call.parameters["webhookId"]?.toIntOrNull()
                        ?: return@put call.respond(HttpStatusCode.BadRequest, "Invalid webhook ID")
                    val existing = webhookService.findById(id)
                        ?: return@put call.respond(HttpStatusCode.NotFound, "Webhook not found")
                    val request = call.receive<PutWebhookRequest>()
                    if (request.url.isBlank()) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "URL is required"))
                        return@put
                    }
                    val validEvents = request.events.filter { it in WebhookEvents.ALL }
                    if (validEvents.isEmpty()) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to "At least one valid event is required")
                        )
                        return@put
                    }
                    val updated = webhookService.update(
                        existing.copy(
                            url = request.url,
                            events = validEvents,
                            secret = request.secret,
                            enabled = request.enabled
                        )
                    )
                    call.respond(updated)
                }
                delete {
                    val id = call.parameters["webhookId"]?.toIntOrNull()
                        ?: return@delete call.respond(
                            HttpStatusCode.BadRequest,
                            "Invalid webhook ID"
                        )
                    val deleted = webhookService.delete(id)
                    if (deleted) call.respond(HttpStatusCode.OK)
                    else call.respond(HttpStatusCode.NotFound, "Webhook not found")
                }
            }
        }
    }
}
