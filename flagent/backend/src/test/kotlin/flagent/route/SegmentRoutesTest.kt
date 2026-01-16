package flagent.route

import flagent.repository.Database
import flagent.repository.impl.*
import flagent.service.SegmentService
import flagent.service.FlagSnapshotService
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlin.test.*
import kotlinx.serialization.json.*

class SegmentRoutesTest {
    @Test
    fun testGetSegments() = testApplication {
        Database.init()
        val flagRepository = FlagRepository()
        val segmentRepository = SegmentRepository()
        val flagSnapshotRepository = FlagSnapshotRepository()
        val flagSnapshotService = FlagSnapshotService(flagSnapshotRepository, flagRepository)
        val segmentService = SegmentService(segmentRepository, flagSnapshotService)
        
        application {
            routing {
                configureSegmentRoutes(segmentService)
            }
        }
        
        // Create a flag first
        val flag = flagRepository.create(flagent.domain.entity.Flag(key = "test_flag", description = "Test flag", enabled = true))
        
        val response = client.get("/api/v1/flags/${flag.id}/segments")
        assertEquals(HttpStatusCode.OK, response.status)
        val json = Json.parseToJsonElement(response.bodyAsText())
        assertTrue(json is JsonArray)
        
        Database.close()
    }
    
    @Test
    fun testCreateSegment() = testApplication {
        Database.init()
        val flagRepository = FlagRepository()
        val segmentRepository = SegmentRepository()
        val flagSnapshotRepository = FlagSnapshotRepository()
        val flagSnapshotService = FlagSnapshotService(flagSnapshotRepository, flagRepository)
        val segmentService = SegmentService(segmentRepository, flagSnapshotService)
        
        application {
            routing {
                configureSegmentRoutes(segmentService)
            }
        }
        
        val flag = flagRepository.create(flagent.domain.entity.Flag(key = "test_flag", description = "Test flag", enabled = true))
        
        val response = client.post("/api/v1/flags/${flag.id}/segments") {
            contentType(ContentType.Application.Json)
            setBody("""{"description":"Test segment","rolloutPercent":100}""")
        }
        
        assertEquals(HttpStatusCode.OK, response.status)
        val json = Json.parseToJsonElement(response.bodyAsText())
        assertTrue(json.jsonObject.containsKey("id"))
        assertTrue(json.jsonObject.containsKey("description"))
        
        Database.close()
    }
    
    @Test
    fun testGetSegmentById() = testApplication {
        Database.init()
        val flagRepository = FlagRepository()
        val segmentRepository = SegmentRepository()
        val flagSnapshotRepository = FlagSnapshotRepository()
        val flagSnapshotService = FlagSnapshotService(flagSnapshotRepository, flagRepository)
        val segmentService = SegmentService(segmentRepository, flagSnapshotService)
        
        application {
            routing {
                configureSegmentRoutes(segmentService)
            }
        }
        
        val flag = flagRepository.create(flagent.domain.entity.Flag(key = "test_flag", description = "Test flag", enabled = true))
        val segment = segmentRepository.create(flagent.domain.entity.Segment(flagId = flag.id, description = "Test segment", rank = 1, rolloutPercent = 100))
        
        val response = client.get("/api/v1/flags/${flag.id}/segments/${segment.id}")
        assertEquals(HttpStatusCode.OK, response.status)
        val json = Json.parseToJsonElement(response.bodyAsText())
        assertEquals(segment.id, json.jsonObject["id"]!!.jsonPrimitive.int)
        
        Database.close()
    }
    
    @Test
    fun testUpdateSegment() = testApplication {
        Database.init()
        val flagRepository = FlagRepository()
        val segmentRepository = SegmentRepository()
        val flagSnapshotRepository = FlagSnapshotRepository()
        val flagSnapshotService = FlagSnapshotService(flagSnapshotRepository, flagRepository)
        val segmentService = SegmentService(segmentRepository, flagSnapshotService)
        
        application {
            routing {
                configureSegmentRoutes(segmentService)
            }
        }
        
        val flag = flagRepository.create(flagent.domain.entity.Flag(key = "test_flag", description = "Test flag", enabled = true))
        val segment = segmentRepository.create(flagent.domain.entity.Segment(flagId = flag.id, description = "Test segment", rank = 1, rolloutPercent = 100))
        
        val response = client.put("/api/v1/flags/${flag.id}/segments/${segment.id}") {
            contentType(ContentType.Application.Json)
            setBody("""{"description":"Updated segment","rolloutPercent":50}""")
        }
        
        assertEquals(HttpStatusCode.OK, response.status)
        
        Database.close()
    }
    
    @Test
    fun testDeleteSegment() = testApplication {
        Database.init()
        val flagRepository = FlagRepository()
        val segmentRepository = SegmentRepository()
        val flagSnapshotRepository = FlagSnapshotRepository()
        val flagSnapshotService = FlagSnapshotService(flagSnapshotRepository, flagRepository)
        val segmentService = SegmentService(segmentRepository, flagSnapshotService)
        
        application {
            routing {
                configureSegmentRoutes(segmentService)
            }
        }
        
        val flag = flagRepository.create(flagent.domain.entity.Flag(key = "test_flag", description = "Test flag", enabled = true))
        val segment = segmentRepository.create(flagent.domain.entity.Segment(flagId = flag.id, description = "Test segment", rank = 1, rolloutPercent = 100))
        
        val response = client.delete("/api/v1/flags/${flag.id}/segments/${segment.id}")
        assertEquals(HttpStatusCode.OK, response.status)
        
        Database.close()
    }
    
    @Test
    fun testGetSegments_InvalidFlagId() = testApplication {
        Database.init()
        val segmentRepository = SegmentRepository()
        val flagRepository = FlagRepository()
        val flagSnapshotRepository = FlagSnapshotRepository()
        val flagSnapshotService = FlagSnapshotService(flagSnapshotRepository, flagRepository)
        val segmentService = SegmentService(segmentRepository, flagSnapshotService)
        
        application {
            routing {
                configureSegmentRoutes(segmentService)
            }
        }
        
        val response = client.get("/api/v1/flags/invalid/segments")
        assertEquals(HttpStatusCode.BadRequest, response.status)
        
        Database.close()
    }
}
