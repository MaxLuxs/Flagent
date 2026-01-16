package flagent.route

import flagent.repository.Database
import flagent.repository.impl.*
import flagent.service.ConstraintService
import flagent.service.FlagSnapshotService
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlin.test.*
import kotlinx.serialization.json.*

class ConstraintRoutesTest {
    @Test
    fun testGetConstraints() = testApplication {
        Database.init()
        val flagRepository = FlagRepository()
        val segmentRepository = SegmentRepository()
        val constraintRepository = ConstraintRepository()
        val flagSnapshotRepository = FlagSnapshotRepository()
        val flagSnapshotService = FlagSnapshotService(flagSnapshotRepository, flagRepository)
        val constraintService = ConstraintService(constraintRepository, segmentRepository, flagSnapshotService)
        
        application {
            routing {
                configureConstraintRoutes(constraintService)
            }
        }
        
        val flag = flagRepository.create(flagent.domain.entity.Flag(key = "test_flag", description = "Test flag", enabled = true))
        val segment = segmentRepository.create(flagent.domain.entity.Segment(flagId = flag.id, description = "Test segment", rank = 1, rolloutPercent = 100))
        
        val response = client.get("/api/v1/flags/${flag.id}/segments/${segment.id}/constraints")
        assertEquals(HttpStatusCode.OK, response.status)
        val json = Json.parseToJsonElement(response.bodyAsText())
        assertTrue(json is JsonArray)
        
        Database.close()
    }
    
    @Test
    fun testCreateConstraint() = testApplication {
        Database.init()
        val flagRepository = FlagRepository()
        val segmentRepository = SegmentRepository()
        val constraintRepository = ConstraintRepository()
        val flagSnapshotRepository = FlagSnapshotRepository()
        val flagSnapshotService = FlagSnapshotService(flagSnapshotRepository, flagRepository)
        val constraintService = ConstraintService(constraintRepository, segmentRepository, flagSnapshotService)
        
        application {
            routing {
                configureConstraintRoutes(constraintService)
            }
        }
        
        val flag = flagRepository.create(flagent.domain.entity.Flag(key = "test_flag", description = "Test flag", enabled = true))
        val segment = segmentRepository.create(flagent.domain.entity.Segment(flagId = flag.id, description = "Test segment", rank = 1, rolloutPercent = 100))
        
        val response = client.post("/api/v1/flags/${flag.id}/segments/${segment.id}/constraints") {
            contentType(ContentType.Application.Json)
            setBody("""{"property":"region","operator":"EQ","value":"US"}""")
        }
        
        assertEquals(HttpStatusCode.OK, response.status)
        val json = Json.parseToJsonElement(response.bodyAsText())
        assertTrue(json.jsonObject.containsKey("id"))
        
        Database.close()
    }
    
    @Test
    fun testUpdateConstraint() = testApplication {
        Database.init()
        val flagRepository = FlagRepository()
        val segmentRepository = SegmentRepository()
        val constraintRepository = ConstraintRepository()
        val flagSnapshotRepository = FlagSnapshotRepository()
        val flagSnapshotService = FlagSnapshotService(flagSnapshotRepository, flagRepository)
        val constraintService = ConstraintService(constraintRepository, segmentRepository, flagSnapshotService)
        
        application {
            routing {
                configureConstraintRoutes(constraintService)
            }
        }
        
        val flag = flagRepository.create(flagent.domain.entity.Flag(key = "test_flag", description = "Test flag", enabled = true))
        val segment = segmentRepository.create(flagent.domain.entity.Segment(flagId = flag.id, description = "Test segment", rank = 1, rolloutPercent = 100))
        val constraint = constraintRepository.create(flagent.domain.entity.Constraint(segmentId = segment.id, property = "region", operator = "EQ", value = "US"))
        
        val response = client.put("/api/v1/flags/${flag.id}/segments/${segment.id}/constraints/${constraint.id}") {
            contentType(ContentType.Application.Json)
            setBody("""{"property":"region","operator":"EQ","value":"EU"}""")
        }
        
        assertEquals(HttpStatusCode.OK, response.status)
        
        Database.close()
    }
    
    @Test
    fun testDeleteConstraint() = testApplication {
        Database.init()
        val flagRepository = FlagRepository()
        val segmentRepository = SegmentRepository()
        val constraintRepository = ConstraintRepository()
        val flagSnapshotRepository = FlagSnapshotRepository()
        val flagSnapshotService = FlagSnapshotService(flagSnapshotRepository, flagRepository)
        val constraintService = ConstraintService(constraintRepository, segmentRepository, flagSnapshotService)
        
        application {
            routing {
                configureConstraintRoutes(constraintService)
            }
        }
        
        val flag = flagRepository.create(flagent.domain.entity.Flag(key = "test_flag", description = "Test flag", enabled = true))
        val segment = segmentRepository.create(flagent.domain.entity.Segment(flagId = flag.id, description = "Test segment", rank = 1, rolloutPercent = 100))
        val constraint = constraintRepository.create(flagent.domain.entity.Constraint(segmentId = segment.id, property = "region", operator = "EQ", value = "US"))
        
        val response = client.delete("/api/v1/flags/${flag.id}/segments/${segment.id}/constraints/${constraint.id}")
        assertEquals(HttpStatusCode.OK, response.status)
        
        Database.close()
    }
}
