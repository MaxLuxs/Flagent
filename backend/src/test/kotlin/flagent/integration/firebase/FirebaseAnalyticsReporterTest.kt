package flagent.integration.firebase

import flagent.service.EvalContext
import flagent.service.EvalResult
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FirebaseAnalyticsReporterTest {

    private fun createEvalResult(
        flagKey: String = "test_flag",
        variantKey: String? = null,
        entityContext: kotlinx.serialization.json.JsonObject? = null
    ) = EvalResult(
        flagID = 1,
        flagKey = flagKey,
        flagSnapshotID = 1,
        flagTags = emptyList(),
        segmentID = null,
        variantID = null,
        variantKey = variantKey,
        variantAttachment = null,
        evalContext = EvalContext(
            entityID = "user123",
            entityType = "user",
            entityContext = entityContext
        ),
        evalDebugLog = null,
        timestamp = System.currentTimeMillis()
    )

    @Test
    fun `recordAsync does nothing when entityContext lacks app_instance_id and client_id`() = runBlocking {
        var requestCount = 0
        val mockEngine = MockEngine { request ->
            requestCount++
            respond(
                content = ByteReadChannel("{}"),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val reporter = FirebaseAnalyticsReporter(
            apiSecret = "secret",
            measurementId = "G-XXX",
            httpClient = HttpClient(mockEngine)
        )
        val result = createEvalResult(entityContext = buildJsonObject { put("region", "US") })
        reporter.recordAsync(result)
        delay(150)
        reporter.close()

        assertEquals(0, requestCount)
    }

    @Test
    fun `recordAsync sends event when app_instance_id in entityContext`() = runBlocking {
        var requestCount = 0
        var capturedUrl: String? = null
        val mockEngine = MockEngine { request ->
            requestCount++
            capturedUrl = request.url.toString()
            respond(
                content = ByteReadChannel("{}"),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val reporter = FirebaseAnalyticsReporter(
            apiSecret = "secret",
            measurementId = "G-XXX",
            httpClient = HttpClient(mockEngine)
        )
        val result = createEvalResult(
            entityContext = buildJsonObject { put("app_instance_id", "firebase-instance-123") }
        )
        reporter.recordAsync(result)
        delay(200)
        reporter.close()

        assertEquals(1, requestCount)
        val url = requireNotNull(capturedUrl) { "expected URL to be captured" }
        assertTrue(url.contains("measurement_id=G-XXX"))
        assertTrue(url.contains("api_secret=secret"))
    }

    @Test
    fun `recordAsync sends event when client_id in entityContext`() = runBlocking {
        var requestCount = 0
        val mockEngine = MockEngine { request ->
            requestCount++
            respond(
                content = ByteReadChannel("{}"),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val reporter = FirebaseAnalyticsReporter(
            apiSecret = "secret",
            measurementId = "G-XXX",
            httpClient = HttpClient(mockEngine)
        )
        val result = createEvalResult(
            entityContext = buildJsonObject { put("client_id", "web-client-456") }
        )
        reporter.recordAsync(result)
        delay(200)
        reporter.close()

        assertEquals(1, requestCount)
    }

    @Test
    fun `recordAsync sends event for experiment with variantKey`() = runBlocking {
        var requestCount = 0
        val mockEngine = MockEngine { request ->
            requestCount++
            respond(
                content = ByteReadChannel("{}"),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val reporter = FirebaseAnalyticsReporter(
            apiSecret = "secret",
            measurementId = "G-XXX",
            clientIdKey = "client_id",
            httpClient = HttpClient(mockEngine)
        )
        val result = createEvalResult(
            variantKey = "treatment",
            entityContext = buildJsonObject { put("client_id", "web-456") }
        )
        reporter.recordAsync(result)
        var waited = 0
        while (requestCount < 1 && waited < 3000) {
            delay(50)
            waited += 50
        }
        reporter.close()

        assertEquals(1, requestCount)
    }

    @Test
    fun `recordAsync does not crash when HTTP returns 500`() = runBlocking {
        var requestCount = 0
        val mockEngine = MockEngine { request ->
            requestCount++
            respond(
                content = ByteReadChannel("""{"error":"internal"}"""),
                status = HttpStatusCode.InternalServerError
            )
        }
        val reporter = FirebaseAnalyticsReporter(
            apiSecret = "secret",
            measurementId = "G-XXX",
            httpClient = HttpClient(mockEngine)
        )
        val result = createEvalResult(
            entityContext = buildJsonObject { put("client_id", "web-456") }
        )
        reporter.recordAsync(result)
        delay(200)
        reporter.close()

        assertEquals(1, requestCount)
    }
}
