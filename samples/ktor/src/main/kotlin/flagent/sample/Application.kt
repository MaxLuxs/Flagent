package flagent.sample

import flagent.api.model.EntityRequest
import flagent.api.model.EvaluationBatchRequest
import flagent.api.model.EvaluationRequest
import io.ktor.flagent.getFlagentCache
import io.ktor.flagent.getFlagentClient
import io.ktor.flagent.installFlagent
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.path
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        configureApplication()
    }.start(wait = true)
}

fun Application.configureApplication() {
    val flagentBaseUrl = System.getenv("FLAGENT_BASE_URL") ?: "http://localhost:18000"

    install(StatusPages) {
        status(HttpStatusCode.NotFound) { call, status ->
            call.respond(status, mapOf("error" to "Not found", "path" to (call.request.path())))
        }
        exception<Throwable> { call, cause ->
            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("error" to (cause.message ?: "Internal error"))
            )
        }
    }

    installFlagent {
        this.flagentBaseUrl = flagentBaseUrl
        enableEvaluation = true
        enableCache = true
        cacheTtlMs = 60000
    }

    routing {
        get("/") {
            call.respond(
                mapOf(
                    "name" to "Flagent Ktor Sample",
                    "version" to "1.0",
                    "endpoints" to listOf(
                        "GET / - this info",
                        "GET /health - health check",
                        "GET /feature/{flagKey}?entityID=...&entityType=... - evaluate single flag",
                        "POST /feature-batch - batch evaluation",
                        "GET /cache/info - cache info",
                        "GET /flagent/health - Flagent plugin health",
                        "POST /flagent/evaluate - Flagent single evaluation",
                        "POST /flagent/evaluate/batch - Flagent batch evaluation"
                    )
                )
            )
        }

        get("/health") {
            call.respond(mapOf("status" to "UP", "service" to "sample-ktor"))
        }

        get("/feature/{flagKey}") {
            val flagKey = call.parameters["flagKey"] ?: run {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "flagKey required"))
                return@get
            }
            val entityID = call.request.queryParameters["entityID"] ?: "user123"
            val entityType = call.request.queryParameters["entityType"] ?: "user"
            val client = call.application.getFlagentClient()
            if (client == null) {
                call.respond(
                    HttpStatusCode.ServiceUnavailable,
                    mapOf("error" to "Flagent client not available")
                )
                return@get
            }
            try {
                val request = EvaluationRequest(
                    flagKey = flagKey,
                    entityID = entityID,
                    entityType = entityType
                )
                val response = client.evaluate(request)
                call.respond(
                    mapOf(
                        "enabled" to (response.variantKey != null),
                        "variant" to (response.variantKey ?: "disabled"),
                        "message" to "Feature ${response.flagKey} = ${response.variantKey ?: "disabled"}"
                    )
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to (e.message ?: "Evaluation failed"))
                )
            }
        }

        post("/feature-batch") {
            val client = call.application.getFlagentClient()
            if (client == null) {
                call.respond(
                    HttpStatusCode.ServiceUnavailable,
                    mapOf("error" to "Flagent client not available")
                )
                return@post
            }
            try {
                val request = EvaluationBatchRequest(
                    entities = listOf(
                        EntityRequest(entityID = "user123", entityType = "user"),
                        EntityRequest(entityID = "user456", entityType = "user")
                    ),
                    flagKeys = listOf("sample_flag_1", "sample_flag_2")
                )
                val response = client.evaluateBatch(request)
                call.respond(
                    mapOf(
                        "results" to response.evaluationResults.map { r ->
                            mapOf(
                                "flagKey" to r.flagKey,
                                "variantKey" to r.variantKey,
                                "entityID" to r.evalContext.entityID
                            )
                        },
                        "count" to response.evaluationResults.size
                    )
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to (e.message ?: "Batch evaluation failed"))
                )
            }
        }

        get("/cache/info") {
            val cache = call.application.getFlagentCache()
            call.respond(
                mapOf(
                    "cacheEnabled" to (cache != null),
                    "message" to if (cache != null) "Cache is available (no size exposed by API)" else "Cache disabled"
                )
            )
        }
    }
}
