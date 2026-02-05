package flagent.route

import flagent.api.constants.ApiConstants
import flagent.service.ImportService
import flagent.util.getSubject
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import mu.KotlinLogging

private val log = KotlinLogging.logger {}

@Serializable
data class ImportRequest(
    val format: String = "json",
    val content: String = ""
)

fun Routing.configureImportRoutes(importService: ImportService) {
    route(ApiConstants.API_BASE_PATH) {
        post("/import") {
            try {
                val request = call.receive<ImportRequest>()
                if (request.content.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "content is required"))
                    return@post
                }
                val updatedBy = call.getSubject()
                val result = importService.importFromContent(request.format, request.content, updatedBy)
                call.respond(HttpStatusCode.OK, result)
            } catch (e: Exception) {
                log.warn(e) { "Import failed: ${e.message}" }
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.message ?: "Import failed")))
            }
        }
    }
}
