package flagent.route

import flagent.service.SlackNotificationService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Slack routes - API for Slack integration management
 */
fun Routing.configureSlackRoutes(slackService: SlackNotificationService?) {
    route("/api/v1/slack") {
        // Test Slack notification
        post("/test") {
            if (slackService == null) {
                call.respond(
                    HttpStatusCode.ServiceUnavailable,
                    mapOf("error" to "Slack notifications are not enabled")
                )
                return@post
            }
            
            val success = slackService.sendTestNotification()
            
            if (success) {
                call.respond(
                    HttpStatusCode.OK,
                    mapOf(
                        "success" to true,
                        "message" to "Test notification sent successfully"
                    )
                )
            } else {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf(
                        "success" to false,
                        "error" to "Failed to send test notification"
                    )
                )
            }
        }
        
        // Check Slack status
        get("/status") {
            val enabled = slackService != null
            
            call.respond(
                mapOf(
                    "enabled" to enabled,
                    "configured" to enabled
                )
            )
        }
    }
}
