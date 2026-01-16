package flagent.route

import flagent.domain.entity.Distribution
import flagent.repository.Database
import flagent.repository.impl.*
import flagent.service.DistributionService
import flagent.service.FlagSnapshotService
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlin.test.*
import kotlinx.serialization.json.*

class DistributionRoutesTest {
    @Test
    fun testGetDistributions() = testApplication {
        Database.init()
        val flagRepository = FlagRepository()
        val segmentRepository = SegmentRepository()
        val distributionRepository = DistributionRepository()
        val flagSnapshotRepository = FlagSnapshotRepository()
        val flagSnapshotService = FlagSnapshotService(flagSnapshotRepository, flagRepository)
        val distributionService = DistributionService(distributionRepository, flagRepository, flagSnapshotService)
        
        application {
            routing {
                configureDistributionRoutes(distributionService)
            }
        }
        
        val flag = flagRepository.create(flagent.domain.entity.Flag(key = "test_flag", description = "Test flag", enabled = true))
        val segment = segmentRepository.create(flagent.domain.entity.Segment(flagId = flag.id, description = "Test segment", rank = 1, rolloutPercent = 100))
        
        val response = client.get("/api/v1/flags/${flag.id}/segments/${segment.id}/distributions")
        assertEquals(HttpStatusCode.OK, response.status)
        val json = Json.parseToJsonElement(response.bodyAsText())
        assertTrue(json is JsonArray)
        
        Database.close()
    }
    
    @Test
    fun testUpdateDistributions() = testApplication {
        Database.init()
        val flagRepository = FlagRepository()
        val segmentRepository = SegmentRepository()
        val variantRepository = VariantRepository()
        val distributionRepository = DistributionRepository()
        val flagSnapshotRepository = FlagSnapshotRepository()
        val flagSnapshotService = FlagSnapshotService(flagSnapshotRepository, flagRepository)
        val distributionService = DistributionService(distributionRepository, flagRepository, flagSnapshotService)
        
        application {
            routing {
                configureDistributionRoutes(distributionService)
            }
        }
        
        val flag = flagRepository.create(flagent.domain.entity.Flag(key = "test_flag", description = "Test flag", enabled = true))
        val segment = segmentRepository.create(flagent.domain.entity.Segment(flagId = flag.id, description = "Test segment", rank = 1, rolloutPercent = 100))
        val variant = variantRepository.create(flagent.domain.entity.Variant(flagId = flag.id, key = "variant1"))
        
        val response = client.put("/api/v1/flags/${flag.id}/segments/${segment.id}/distributions") {
            contentType(ContentType.Application.Json)
            setBody("""{"distributions":[{"variantID":${variant.id},"percent":100}]}""")
        }
        
        assertEquals(HttpStatusCode.OK, response.status)
        
        Database.close()
    }
}
