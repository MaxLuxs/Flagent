package flagent.sample

import flagent.api.model.EntityRequest
import flagent.api.model.EvaluationBatchRequest
import flagent.api.model.EvaluationRequest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import io.ktor.flagent.getFlagentCache
import io.ktor.flagent.getFlagentClient
import io.ktor.flagent.installFlagent
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
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

    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
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
                buildJsonObject {
                    put("name", "Flagent Ktor Sample")
                    put("version", "1.0")
                    put("endpoints", buildJsonArray {
                        add("GET / - this info")
                        add("GET /health - health check")
                        add("GET /feature/{flagKey}?entityID=...&entityType=...&entityContext={\"country\":\"US\"} - evaluate single flag")
                        add("POST /feature-batch - batch evaluation")
                        add("GET /cache/info - cache info")
                        add("GET /flagent/health - Flagent plugin health")
                        add("POST /flagent/evaluate - Flagent single evaluation")
                        add("POST /flagent/evaluate/batch - Flagent batch evaluation")
                    })
                }
            )
        }

        get("/health") {
            call.respond(
                buildJsonObject {
                    put("status", "UP")
                    put("service", "sample-ktor")
                }
            )
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
                        EntityRequest(
                            entityID = "user123",
                            entityType = "user",
                            entityContext = mapOf("country" to "US", "tier" to "premium")
                        ),
                        EntityRequest(
                            entityID = "user456",
                            entityType = "user",
                            entityContext = mapOf("country" to "EU", "tier" to "basic")
                        )
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
