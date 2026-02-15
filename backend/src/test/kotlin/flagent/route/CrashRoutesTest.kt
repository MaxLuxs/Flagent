package flagent.route

import flagent.domain.entity.CrashReport
import flagent.service.CrashReportService
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CrashRoutesTest {

    @Test
    fun postCrashesWithActiveFlagKeysReturnsCreatedAndGetReturnsThem() = testApplication {
        val service = mockk<CrashReportService>()
        val savedCrash = CrashReport(
            id = 1,
            stackTrace = "at app.Main",
            message = "NPE",
            platform = "android",
            appVersion = "1.0",
            deviceInfo = null,
            breadcrumbs = null,
            customKeys = null,
            activeFlagKeys = listOf("feature_x", "ab_checkout"),
            timestamp = System.currentTimeMillis(),
            tenantId = null
        )
        coEvery { service.save(any()) } returns savedCrash
        coEvery { service.list(null, any(), any(), any(), any()) } returns listOf(savedCrash)
        coEvery { service.count(null, any(), any()) } returns 1L

        application {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
            routing { configureCrashRoutes(service) }
        }

        val response = client.post("/api/v1/crashes") {
            contentType(ContentType.Application.Json)
            setBody("""
                {
                    "stackTrace": "at app.Main",
                    "message": "NPE",
                    "platform": "android",
                    "appVersion": "1.0",
                    "activeFlagKeys": ["feature_x", "ab_checkout"]
                }
            """.trimIndent())
        }
        assertEquals(HttpStatusCode.Created, response.status)
        val created = response.bodyAsText()
        assertTrue(created.contains("feature_x"), "Response should contain activeFlagKeys")
        assertTrue(created.contains("ab_checkout"), "Response should contain activeFlagKeys")

        val listResponse = client.get("/api/v1/crashes")
        assertEquals(HttpStatusCode.OK, listResponse.status)
        val listBody = listResponse.bodyAsText()
        assertTrue(listBody.contains("feature_x"), "List response should contain activeFlagKeys")
    }
}
