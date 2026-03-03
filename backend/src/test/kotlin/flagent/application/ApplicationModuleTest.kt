package flagent.application

import flagent.config.AppConfig
import flagent.repository.Database
import flagent.test.PostgresTestcontainerExtension
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import io.mockk.every
import io.mockk.mockkObject
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExtendWith(PostgresTestcontainerExtension::class)
class ApplicationModuleTest {

    @Test
    fun `module wires health route and database without errors`() = testApplication {
        // Use test DB from PostgresTestcontainerExtension; skip AppConfig.db* overrides.
        mockkObject(AppConfig)
        every { AppConfig.host } returns "0.0.0.0"
        every { AppConfig.port } returns 0
        every { AppConfig.workerPoolSize } returns 1

        // Keep conservative defaults for feature toggles so we exercise default branches
        every { AppConfig.corsEnabled } returns false
        every { AppConfig.evalOnlyMode } returns true
        every { AppConfig.jwtAuthEnabled } returns false
        every { AppConfig.webPrefix } returns ""
        every { AppConfig.mcpEnabled } returns false

        // These are accessed in logging / metrics branches; provide simple defaults
        every { AppConfig.dbDriver } returns "sqlite3"
        every { AppConfig.firebaseRcSyncEnabled } returns false
        every { AppConfig.firebaseRcProjectId } returns ""
        every { AppConfig.firebaseAnalyticsEnabled } returns false
        every { AppConfig.firebaseAnalyticsApiSecret } returns ""
        every { AppConfig.firebaseAnalyticsMeasurementId } returns ""
        every { AppConfig.recorderType } returns "noop"

        application {
            // Database is already initialised by PostgresTestcontainerExtension; init() is idempotent
            module()
        }

        val response = client.get("/api/v1/health")
        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("OK"), "Health response should contain OK, got: $body")
    }

    @Test
    fun `module registers catch-all api 404 JSON`() = testApplication {
        mockkObject(AppConfig)
        every { AppConfig.host } returns "0.0.0.0"
        every { AppConfig.port } returns 0
        every { AppConfig.workerPoolSize } returns 1
        every { AppConfig.corsEnabled } returns false
        every { AppConfig.evalOnlyMode } returns true
        every { AppConfig.jwtAuthEnabled } returns false
        every { AppConfig.webPrefix } returns ""
        every { AppConfig.mcpEnabled } returns false
        every { AppConfig.dbDriver } returns "sqlite3"
        every { AppConfig.firebaseRcSyncEnabled } returns false
        every { AppConfig.firebaseRcProjectId } returns ""
        every { AppConfig.firebaseAnalyticsEnabled } returns false
        every { AppConfig.firebaseAnalyticsApiSecret } returns ""
        every { AppConfig.firebaseAnalyticsMeasurementId } returns ""
        every { AppConfig.recorderType } returns "noop"

        application {
            module()
        }

        val response = client.get("/api/unknown/path")
        assertEquals(HttpStatusCode.NotFound, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("Not found"), "Catch-all should return JSON Not found, got: $body")
    }
}

