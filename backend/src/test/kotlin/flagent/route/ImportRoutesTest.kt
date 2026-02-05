package flagent.route

import flagent.repository.Database
import flagent.repository.impl.*
import flagent.route.RealtimeEventBus
import flagent.service.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.*
import kotlin.test.*

class ImportRoutesTest {

    @Test
    fun testImport_EmptyContent_Returns400() = testApplication {
        Database.init()
        try {
            val (flagService, segmentService, variantService, distributionService, constraintService, flagRepository) = createServices()
            val importService = ImportService(flagService, segmentService, variantService, distributionService, constraintService, flagRepository)

            application {
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
                routing {
                    configureImportRoutes(importService)
                }
            }

            val response = client.post("/api/v1/import") {
                contentType(ContentType.Application.Json)
                setBody("""{"format":"json","content":""}""")
            }
            assertEquals(HttpStatusCode.BadRequest, response.status)
            val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
            assertTrue(body.containsKey("error"))
            assertTrue(body["error"]!!.jsonPrimitive.content.contains("content"))
        } finally {
            Database.close()
        }
    }

    @Test
    fun testImport_ValidJson_CreatesFlag() = testApplication {
        Database.init()
        try {
            val (flagService, segmentService, variantService, distributionService, constraintService, flagRepository) = createServices()
            val importService = ImportService(flagService, segmentService, variantService, distributionService, constraintService, flagRepository)

            application {
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
                routing {
                    configureImportRoutes(importService)
                }
            }

            val flagKey = "import_test_${System.currentTimeMillis()}"
            val json = buildJsonObject {
                put("version", "1")
                put("flags", buildJsonArray {
                    add(buildJsonObject {
                        put("key", flagKey)
                        put("description", "Imported flag")
                        put("enabled", false)
                    })
                })
            }.toString()

            val response = client.post("/api/v1/import") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("format", "json")
                    put("content", json)
                }.toString())
            }
            assertEquals(HttpStatusCode.OK, response.status)
            val result = Json.parseToJsonElement(response.bodyAsText()).jsonObject
            assertEquals(1, result["created"]?.jsonPrimitive?.content?.toIntOrNull())
            assertEquals(0, result["updated"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0)
        } finally {
            Database.close()
        }
    }

    @Test
    fun testImport_InvalidFormat_ReturnsErrors() = testApplication {
        Database.init()
        try {
            val (flagService, segmentService, variantService, distributionService, constraintService, flagRepository) = createServices()
            val importService = ImportService(flagService, segmentService, variantService, distributionService, constraintService, flagRepository)

            application {
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
                routing {
                    configureImportRoutes(importService)
                }
            }

            val response = client.post("/api/v1/import") {
                contentType(ContentType.Application.Json)
                setBody("""{"format":"xml","content":"<flags></flags>"}""")
            }
            assertEquals(HttpStatusCode.OK, response.status)
            val result = Json.parseToJsonElement(response.bodyAsText()).jsonObject
            val errors = result["errors"]
            assertNotNull(errors)
            assertTrue(errors.toString().contains("Unsupported format") || errors.toString().contains("xml"))
        } finally {
            Database.close()
        }
    }

    @Test
    fun testImport_InvalidJson_ReturnsParseError() = testApplication {
        Database.init()
        try {
            val (flagService, segmentService, variantService, distributionService, constraintService, flagRepository) = createServices()
            val importService = ImportService(flagService, segmentService, variantService, distributionService, constraintService, flagRepository)

            application {
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
                routing {
                    configureImportRoutes(importService)
                }
            }

            val response = client.post("/api/v1/import") {
                contentType(ContentType.Application.Json)
                setBody("""{"format":"json","content":"{ invalid json }"}""")
            }
            assertEquals(HttpStatusCode.OK, response.status)
            val result = Json.parseToJsonElement(response.bodyAsText()).jsonObject
            val errors = result["errors"]
            assertNotNull(errors)
            assertTrue(errors.toString().contains("Parse error") || errors.toString().contains("error"))
        } finally {
            Database.close()
        }
    }

    private fun createServices(): ServicesTuple {
        val flagRepository = FlagRepository()
        val segmentRepository = SegmentRepository()
        val variantRepository = VariantRepository()
        val constraintRepository = ConstraintRepository()
        val distributionRepository = DistributionRepository()
        val tagRepository = TagRepository()
        val flagSnapshotRepository = FlagSnapshotRepository()
        val flagEntityTypeRepository = FlagEntityTypeRepository()

        val flagSnapshotService = FlagSnapshotService(flagSnapshotRepository, flagRepository)
        val flagEntityTypeService = FlagEntityTypeService(flagEntityTypeRepository)
        val eventBus = RealtimeEventBus()
        val segmentService = SegmentService(segmentRepository, flagSnapshotService, flagRepository, eventBus)
        val constraintService = ConstraintService(constraintRepository, segmentRepository, flagSnapshotService)
        val distributionService = DistributionService(distributionRepository, flagRepository, flagSnapshotService)
        val variantService = VariantService(variantRepository, flagRepository, distributionRepository, flagSnapshotService, eventBus)
        val flagService = FlagService(
            flagRepository,
            flagSnapshotService,
            segmentService,
            variantService,
            distributionService,
            flagEntityTypeService,
            eventBus,
            null // no webhooks in tests to avoid async DB access after test completes
        )
        return ServicesTuple(flagService, segmentService, variantService, distributionService, constraintService, flagRepository)
    }

    private data class ServicesTuple(
        val flagService: FlagService,
        val segmentService: SegmentService,
        val variantService: VariantService,
        val distributionService: DistributionService,
        val constraintService: ConstraintService,
        val flagRepository: FlagRepository
    )
}
