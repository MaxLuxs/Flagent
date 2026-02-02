package flagent.test

import io.ktor.client.*
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

/** Parse response as JsonObject. Use instead of body<Map<String, Any>> which is not serializable. */
suspend fun HttpResponse.bodyJsonObject() = Json.parseToJsonElement(bodyAsText()).jsonObject

/** True if value is null or JsonNull (JSON literal null). */
fun JsonElement?.isNullOrJsonNull(): Boolean = this == null || this is JsonNull

/** Throws with body if status is not 2xx. Use before bodyJsonObject() to get clear errors. */
suspend fun HttpResponse.requireSuccess(): HttpResponse {
    if (status.value !in 200..299) {
        error("Request failed: $status - ${bodyAsText()}")
    }
    return this
}

/** Parse response as JsonArray. */
suspend fun HttpResponse.bodyJsonArray() = Json.parseToJsonElement(bodyAsText()).jsonArray

fun kotlinx.serialization.json.JsonObject.intOrNull(key: String): Int? =
    this[key]?.jsonPrimitive?.content?.toIntOrNull()

fun kotlinx.serialization.json.JsonObject.stringOrNull(key: String): String? =
    this[key]?.jsonPrimitive?.content

fun kotlinx.serialization.json.JsonObject.booleanOrNull(key: String): Boolean? =
    this[key]?.jsonPrimitive?.content?.toBooleanStrictOrNull()

/**
 * Creates an authenticated client for integration tests.
 * When enterprise is present, creates a tenant via POST /admin/tenants and uses its API key.
 * When enterprise is absent, returns a base client (no tenant auth required).
 */
suspend fun ApplicationTestBuilder.createAuthenticatedClient(): HttpClient {
    val baseClient = createClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }
    val apiKey = try {
        val adminKey = System.getenv("FLAGENT_ADMIN_API_KEY")
        val r = baseClient.post("/admin/tenants") {
            contentType(ContentType.Application.Json)
            if (adminKey != null) header("X-Admin-Key", adminKey)
            setBody(
                buildJsonObject {
                    put("key", "test-tenant-${System.currentTimeMillis()}")
                    put("name", "Integration Test Tenant")
                    put("plan", "STARTER")
                    put("ownerEmail", "test@test.com")
                }.toString()
            )
        }
        if (r.status != HttpStatusCode.Created) null
        else {
            val obj = Json.parseToJsonElement(r.bodyAsText()).jsonObject
            obj["apiKey"]?.jsonPrimitive?.content
        }
    } catch (_: Exception) {
        null
    }
    return if (apiKey != null) {
        createClient {
            install(DefaultRequest) {
                header("X-API-Key", apiKey)
            }
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    } else {
        baseClient
    }
}
