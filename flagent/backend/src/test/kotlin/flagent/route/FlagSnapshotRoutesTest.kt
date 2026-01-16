package flagent.route

import flagent.repository.Database
import flagent.repository.impl.FlagRepository
import flagent.repository.impl.FlagSnapshotRepository
import flagent.service.FlagSnapshotService
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlin.test.*
import kotlinx.serialization.json.*

class FlagSnapshotRoutesTest {
    @Test
    fun testGetSnapshots() = testApplication {
        Database.init()
        val flagRepository = FlagRepository()
        val flagSnapshotRepository = FlagSnapshotRepository()
        val flagSnapshotService = FlagSnapshotService(flagSnapshotRepository, flagRepository)
        
        application {
            routing {
                configureFlagSnapshotRoutes(flagSnapshotService)
            }
        }
        
        val flag = flagRepository.create(flagent.domain.entity.Flag(key = "test_flag", description = "Test flag", enabled = true))
        
        val response = client.get("/api/v1/flags/${flag.id}/snapshots")
        assertEquals(HttpStatusCode.OK, response.status)
        val json = Json.parseToJsonElement(response.bodyAsText())
        assertTrue(json is JsonArray)
        
        Database.close()
    }
}
