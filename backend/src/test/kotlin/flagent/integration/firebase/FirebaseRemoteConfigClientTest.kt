package flagent.integration.firebase

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class FirebaseRemoteConfigClientTest {

    @Test
    fun `getRemoteConfig returns body and etag on success`() = runBlocking {
        val mockBody = """{"parameters":{"foo":{"defaultValue":{"value":"true"}}}}"""
        val mockEtag = "etag-abc"
        val mockEngine = MockEngine { request ->
            assertEquals("GET", request.method.value)
            assertTrue(request.url.toString().contains("remoteConfig"))
            respond(
                content = ByteReadChannel(mockBody),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ETag, "\"$mockEtag\"")
            )
        }
        val client = FirebaseRemoteConfigClient(
            projectId = "test-project",
            httpClient = HttpClient(mockEngine),
            testToken = "fake-token"
        )
        val (body, etag) = client.getRemoteConfig()
        client.close()

        assertEquals(mockBody, body)
        assertEquals(mockEtag, etag)
    }

    @Test
    fun `getRemoteConfig throws on non-success status`() = runBlocking {
        val mockEngine = MockEngine {
            respond(
                content = ByteReadChannel("""{"error":"unauthorized"}"""),
                status = HttpStatusCode.Unauthorized
            )
        }
        val client = FirebaseRemoteConfigClient(
            projectId = "test-project",
            httpClient = HttpClient(mockEngine),
            testToken = "fake-token"
        )
        assertFailsWith<FirebaseRcException> {
            client.getRemoteConfig()
        }
        client.close()
    }

    @Test
    fun `updateRemoteConfig sends PUT with If-Match and body`() = runBlocking {
        var capturedEtag: String? = null
        val mockEngine = MockEngine { request ->
            capturedEtag = request.headers["If-Match"]
            respond(
                content = ByteReadChannel("""{"parameters":{}}"""),
                status = HttpStatusCode.OK
            )
        }
        val client = FirebaseRemoteConfigClient(
            projectId = "test-project",
            httpClient = HttpClient(mockEngine),
            testToken = "fake-token"
        )
        val template = """{"parameters":{"flag_a":{"defaultValue":{"value":"true"}}}}"""
        client.updateRemoteConfig(template, "etag-123")
        client.close()

        assertEquals("etag-123", capturedEtag)
    }

    @Test
    fun `updateRemoteConfig throws on non-success status`() = runBlocking {
        val mockEngine = MockEngine {
            respond(
                content = ByteReadChannel("""{"error":"conflict"}"""),
                status = HttpStatusCode.Conflict
            )
        }
        val client = FirebaseRemoteConfigClient(
            projectId = "test-project",
            httpClient = HttpClient(mockEngine),
            testToken = "fake-token"
        )
        assertFailsWith<FirebaseRcException> {
            client.updateRemoteConfig("""{"parameters":{}}""", "etag-1")
        }
        client.close()
    }
}
