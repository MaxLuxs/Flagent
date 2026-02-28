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
 * Single-client wrapper so integration tests use one HttpClient (avoids "EmbeddedServer was stopped"
 * when creating a second client via createClient).
 */
class AuthenticatedTestClient(
    private val client: HttpClient,
    private val apiKey: String?
) {
    suspend fun get(url: String, block: io.ktor.client.request.HttpRequestBuilder.() -> Unit = {}) =
        client.get(url) { if (apiKey != null) header("X-API-Key", apiKey); block() }

    suspend fun post(url: String, block: io.ktor.client.request.HttpRequestBuilder.() -> Unit = {}) =
        client.post(url) { if (apiKey != null) header("X-API-Key", apiKey); block() }

    suspend fun put(url: String, block: io.ktor.client.request.HttpRequestBuilder.() -> Unit = {}) =
        client.put(url) { if (apiKey != null) header("X-API-Key", apiKey); block() }

    suspend fun delete(url: String, block: io.ktor.client.request.HttpRequestBuilder.() -> Unit = {}) =
        client.delete(url) { if (apiKey != null) header("X-API-Key", apiKey); block() }

    /** No-op: test engine manages lifecycle; do not close the underlying client. */
    fun close() {}
}

/**
 * Creates an authenticated client for integration tests.
 * Uses the test engine's client (no createClient) so the embedded server stays running.
 * When enterprise is present, creates a tenant via POST /admin/tenants and returns a wrapper that adds X-API-Key.
 */
suspend fun ApplicationTestBuilder.createAuthenticatedClient(): AuthenticatedTestClient {
    val apiKey = try {
        val adminKey = System.getenv("FLAGENT_ADMIN_API_KEY")
        val r = client.post("/admin/tenants") {
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
    return AuthenticatedTestClient(client, apiKey)
}
