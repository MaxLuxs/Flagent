package flagent.route

import flagent.cache.impl.EvalCache
import flagent.repository.Database
import flagent.repository.impl.*
import flagent.service.ExportService
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlin.test.*
import kotlinx.serialization.json.*

class ExportRoutesTest {
    @Test
    fun testExportEvalCacheJson() = testApplication {
        Database.init()
        val flagRepository = FlagRepository()
        val flagSnapshotRepository = FlagSnapshotRepository()
        val flagEntityTypeRepository = FlagEntityTypeRepository()
        val evalCache = EvalCache(flagRepository)
        val exportService = ExportService(flagRepository, flagSnapshotRepository, flagEntityTypeRepository)
        
        application {
            routing {
                configureExportRoutes(evalCache, exportService)
            }
        }
        
        evalCache.start()
        
        val response = client.get("/api/v1/export/eval_cache/json")
        assertEquals(HttpStatusCode.OK, response.status)
        val json = Json.parseToJsonElement(response.bodyAsText())
        assertTrue(json.jsonObject.containsKey("flags"))
        
        evalCache.stop()
        Database.close()
    }
    
    @Test
    fun testExportSQLite() = testApplication {
        Database.init()
        val flagRepository = FlagRepository()
        val flagSnapshotRepository = FlagSnapshotRepository()
        val flagEntityTypeRepository = FlagEntityTypeRepository()
        val evalCache = EvalCache(flagRepository)
        val exportService = ExportService(flagRepository, flagSnapshotRepository, flagEntityTypeRepository)
        
        application {
            routing {
                configureExportRoutes(evalCache, exportService)
            }
        }
        
        val response = client.get("/api/v1/export/sqlite")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(ContentType.Application.OctetStream, response.contentType())
        
        Database.close()
    }

    @Test
    fun testExportGitOpsJson() = testApplication {
        Database.init()
        val flagRepository = FlagRepository()
        val flagSnapshotRepository = FlagSnapshotRepository()
        val flagEntityTypeRepository = FlagEntityTypeRepository()
        val evalCache = EvalCache(flagRepository)
        val exportService = ExportService(flagRepository, flagSnapshotRepository, flagEntityTypeRepository)

        application {
            routing {
                configureExportRoutes(evalCache, exportService)
            }
        }

        val response = client.get("/api/v1/export/gitops?format=json")
        assertEquals(HttpStatusCode.OK, response.status)
        val json = Json.parseToJsonElement(response.bodyAsText())
        assertTrue(json.jsonObject.containsKey("version"))
        assertTrue(json.jsonObject.containsKey("flags"))

        Database.close()
    }

    @Test
    fun testExportGitOpsYaml() = testApplication {
        Database.init()
        val flagRepository = FlagRepository()
        val flagSnapshotRepository = FlagSnapshotRepository()
        val flagEntityTypeRepository = FlagEntityTypeRepository()
        val evalCache = EvalCache(flagRepository)
        val exportService = ExportService(flagRepository, flagSnapshotRepository, flagEntityTypeRepository)

        application {
            routing {
                configureExportRoutes(evalCache, exportService)
            }
        }

        val response = client.get("/api/v1/export/gitops?format=yaml")
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("version") || response.bodyAsText().contains("flags"))

        Database.close()
    }
}
