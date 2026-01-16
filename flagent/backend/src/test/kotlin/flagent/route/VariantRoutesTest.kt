package flagent.route

import flagent.repository.Database
import flagent.repository.impl.*
import flagent.service.VariantService
import flagent.service.FlagSnapshotService
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlin.test.*
import kotlinx.serialization.json.*

class VariantRoutesTest {
    @Test
    fun testGetVariants() = testApplication {
        Database.init()
        val flagRepository = FlagRepository()
        val variantRepository = VariantRepository()
        val distributionRepository = DistributionRepository()
        val flagSnapshotRepository = FlagSnapshotRepository()
        val flagSnapshotService = FlagSnapshotService(flagSnapshotRepository, flagRepository)
        val variantService = VariantService(variantRepository, flagRepository, distributionRepository, flagSnapshotService)
        
        application {
            routing {
                configureVariantRoutes(variantService)
            }
        }
        
        val flag = flagRepository.create(flagent.domain.entity.Flag(key = "test_flag", description = "Test flag", enabled = true))
        
        val response = client.get("/api/v1/flags/${flag.id}/variants")
        assertEquals(HttpStatusCode.OK, response.status)
        val json = Json.parseToJsonElement(response.bodyAsText())
        assertTrue(json is JsonArray)
        
        Database.close()
    }
    
    @Test
    fun testCreateVariant() = testApplication {
        Database.init()
        val flagRepository = FlagRepository()
        val variantRepository = VariantRepository()
        val distributionRepository = DistributionRepository()
        val flagSnapshotRepository = FlagSnapshotRepository()
        val flagSnapshotService = FlagSnapshotService(flagSnapshotRepository, flagRepository)
        val variantService = VariantService(variantRepository, flagRepository, distributionRepository, flagSnapshotService)
        
        application {
            routing {
                configureVariantRoutes(variantService)
            }
        }
        
        val flag = flagRepository.create(flagent.domain.entity.Flag(key = "test_flag", description = "Test flag", enabled = true))
        
        val response = client.post("/api/v1/flags/${flag.id}/variants") {
            contentType(ContentType.Application.Json)
            setBody("""{"key":"variant1"}""")
        }
        
        assertEquals(HttpStatusCode.OK, response.status)
        val json = Json.parseToJsonElement(response.bodyAsText())
        assertTrue(json.jsonObject.containsKey("id"))
        
        Database.close()
    }
    
    @Test
    fun testUpdateVariant() = testApplication {
        Database.init()
        val flagRepository = FlagRepository()
        val variantRepository = VariantRepository()
        val distributionRepository = DistributionRepository()
        val flagSnapshotRepository = FlagSnapshotRepository()
        val flagSnapshotService = FlagSnapshotService(flagSnapshotRepository, flagRepository)
        val variantService = VariantService(variantRepository, flagRepository, distributionRepository, flagSnapshotService)
        
        application {
            routing {
                configureVariantRoutes(variantService)
            }
        }
        
        val flag = flagRepository.create(flagent.domain.entity.Flag(key = "test_flag", description = "Test flag", enabled = true))
        val variant = variantRepository.create(flagent.domain.entity.Variant(flagId = flag.id, key = "variant1"))
        
        val response = client.put("/api/v1/flags/${flag.id}/variants/${variant.id}") {
            contentType(ContentType.Application.Json)
            setBody("""{"key":"variant_updated"}""")
        }
        
        assertEquals(HttpStatusCode.OK, response.status)
        
        Database.close()
    }
    
    @Test
    fun testDeleteVariant() = testApplication {
        Database.init()
        val flagRepository = FlagRepository()
        val variantRepository = VariantRepository()
        val distributionRepository = DistributionRepository()
        val flagSnapshotRepository = FlagSnapshotRepository()
        val flagSnapshotService = FlagSnapshotService(flagSnapshotRepository, flagRepository)
        val variantService = VariantService(variantRepository, flagRepository, distributionRepository, flagSnapshotService)
        
        application {
            routing {
                configureVariantRoutes(variantService)
            }
        }
        
        val flag = flagRepository.create(flagent.domain.entity.Flag(key = "test_flag", description = "Test flag", enabled = true))
        val variant = variantRepository.create(flagent.domain.entity.Variant(flagId = flag.id, key = "variant1"))
        
        val response = client.delete("/api/v1/flags/${flag.id}/variants/${variant.id}")
        assertEquals(HttpStatusCode.OK, response.status)
        
        Database.close()
    }
}
