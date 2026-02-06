package flagent.sample

import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ApplicationTest {

    @Test
    fun `health endpoint returns UP`() = testApplication {
        application {
            configureApplication()
        }
        val response = client.get("/health")
        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        assertEquals(true, body.contains("UP"))
        assertEquals(true, body.contains("sample-ktor"))
    }

    @Test
    fun `root endpoint returns API info`() = testApplication {
        application {
            configureApplication()
        }
        val response = client.get("/") {
            header("Accept", "application/json")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("Flagent Ktor Sample"), "Body should contain name: $body")
        assertTrue(body.contains("endpoints"), "Body should contain endpoints key: $body")
    }
}
