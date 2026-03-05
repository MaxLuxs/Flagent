package flagent.route

import flagent.domain.entity.Segment
import flagent.repository.Database
import flagent.repository.impl.*
import flagent.service.FlagSnapshotService
import flagent.service.SegmentService
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.ktor.serialization.kotlinx.json.*
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.serialization.json.*

class SegmentRoutesTest {
    @Test
    fun testGetSegments() = testApplication {
        Database.init()
        try {
            val flagRepository = FlagRepository()
            val segmentRepository = SegmentRepository()
            val flagSnapshotRepository = FlagSnapshotRepository()
            val flagSnapshotService = FlagSnapshotService(flagSnapshotRepository, flagRepository)
            val segmentService = SegmentService(segmentRepository, flagSnapshotService)
            
            application {
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
                routing {
                    configureSegmentRoutes(segmentService)
                }
            }
            
            val flag = flagRepository.create(flagent.domain.entity.Flag(key = "test_flag", description = "Test flag", enabled = true))
            
            val response = client.get("/api/v1/flags/${flag.id}/segments")
            assertEquals(HttpStatusCode.OK, response.status)
            val json = Json.parseToJsonElement(response.bodyAsText())
            assertTrue(json is JsonArray)
        } finally {
            Database.close()
        }
    }
    
    @Test
    fun testCreateSegment() = testApplication {
        Database.init()
        try {
            val flagRepository = FlagRepository()
            val segmentRepository = SegmentRepository()
            val flagSnapshotRepository = FlagSnapshotRepository()
            val flagSnapshotService = FlagSnapshotService(flagSnapshotRepository, flagRepository)
            val segmentService = SegmentService(segmentRepository, flagSnapshotService)
            
            application {
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
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
        } finally {
            Database.close()
        }
    }
    
    @Test
    fun testGetSegmentById() = testApplication {
        Database.init()
        try {
            val flagRepository = FlagRepository()
            val segmentRepository = SegmentRepository()
            val flagSnapshotRepository = FlagSnapshotRepository()
            val flagSnapshotService = FlagSnapshotService(flagSnapshotRepository, flagRepository)
            val segmentService = SegmentService(segmentRepository, flagSnapshotService)
            
            application {
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
                routing {
                    configureSegmentRoutes(segmentService)
                }
            }
            
            val flag = flagRepository.create(flagent.domain.entity.Flag(key = "test_flag", description = "Test flag", enabled = true))
            val segment = segmentRepository.create(flagent.domain.entity.Segment(flagId = flag.id, description = "Test segment", rank = 1, rolloutPercent = 100))
            
            val response = client.get("/api/v1/flags/${flag.id}/segments/${segment.id}")
            assertEquals(HttpStatusCode.OK, response.status)
            val json = Json.parseToJsonElement(response.bodyAsText())
            assertEquals(segment.id, json.jsonObject["id"]!!.jsonPrimitive.content.toInt())
        } finally {
            Database.close()
        }
    }
    
    @Test
    fun testUpdateSegment() = testApplication {
        Database.init()
        try {
            val flagRepository = FlagRepository()
            val segmentRepository = SegmentRepository()
            val flagSnapshotRepository = FlagSnapshotRepository()
            val flagSnapshotService = FlagSnapshotService(flagSnapshotRepository, flagRepository)
            val segmentService = SegmentService(segmentRepository, flagSnapshotService)
            
            application {
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
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
        } finally {
            Database.close()
        }
    }
    
    @Test
    fun testDeleteSegment() = testApplication {
        Database.init()
        try {
            val flagRepository = FlagRepository()
            val segmentRepository = SegmentRepository()
            val flagSnapshotRepository = FlagSnapshotRepository()
            val flagSnapshotService = FlagSnapshotService(flagSnapshotRepository, flagRepository)
            val segmentService = SegmentService(segmentRepository, flagSnapshotService)
            
            application {
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
                routing {
                    configureSegmentRoutes(segmentService)
                }
            }
            
            val flag = flagRepository.create(flagent.domain.entity.Flag(key = "test_flag", description = "Test flag", enabled = true))
            val segment = segmentRepository.create(flagent.domain.entity.Segment(flagId = flag.id, description = "Test segment", rank = 1, rolloutPercent = 100))
            
            val response = client.delete("/api/v1/flags/${flag.id}/segments/${segment.id}")
            assertEquals(HttpStatusCode.OK, response.status)
        } finally {
            Database.close()
        }
    }
    
    @Test
    fun testGetSegments_InvalidFlagId() = testApplication {
        Database.init()
        try {
            val segmentRepository = SegmentRepository()
            val flagRepository = FlagRepository()
            val flagSnapshotRepository = FlagSnapshotRepository()
            val flagSnapshotService = FlagSnapshotService(flagSnapshotRepository, flagRepository)
            val segmentService = SegmentService(segmentRepository, flagSnapshotService)
            
            application {
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
                routing {
                    configureSegmentRoutes(segmentService)
                }
            }
            
            val response = client.get("/api/v1/flags/invalid/segments")
            assertEquals(HttpStatusCode.BadRequest, response.status)
        } finally {
            Database.close()
        }
    }

    @Test
    fun `GET segment by id returns 404 when not found`() = testApplication {
        val segmentService = mockk<SegmentService>()
        coEvery { segmentService.getSegment(10) } returns null

        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            routing {
                configureSegmentRoutes(segmentService)
            }
        }

        val response = client.get("/api/v1/flags/1/segments/10")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `GET segment by id returns 400 when segment belongs to another flag`() = testApplication {
        val segmentService = mockk<SegmentService>()
        coEvery { segmentService.getSegment(10) } returns Segment(id = 10, flagId = 999, description = "s", rank = 0, rolloutPercent = 100)

        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            routing {
                configureSegmentRoutes(segmentService)
            }
        }

        val response = client.get("/api/v1/flags/1/segments/10")
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains("does not belong to this flag"))
    }

    @Test
    fun `PUT segment with invalid ids returns 400`() = testApplication {
        val segmentService = mockk<SegmentService>(relaxed = true)

        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            routing {
                configureSegmentRoutes(segmentService)
            }
        }

        val response1 = client.put("/api/v1/flags/notanum/segments/1") {
            contentType(ContentType.Application.Json)
            setBody("""{"description":"d","rolloutPercent":10}""")
        }
        assertEquals(HttpStatusCode.BadRequest, response1.status)

        val response2 = client.put("/api/v1/flags/1/segments/notanum") {
            contentType(ContentType.Application.Json)
            setBody("""{"description":"d","rolloutPercent":10}""")
        }
        assertEquals(HttpStatusCode.BadRequest, response2.status)
    }

    @Test
    fun `DELETE segment with invalid ids returns 400`() = testApplication {
        val segmentService = mockk<SegmentService>(relaxed = true)

        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            routing {
                configureSegmentRoutes(segmentService)
            }
        }

        val response1 = client.delete("/api/v1/flags/notanum/segments/1")
        assertEquals(HttpStatusCode.BadRequest, response1.status)

        val response2 = client.delete("/api/v1/flags/1/segments/notanum")
        assertEquals(HttpStatusCode.BadRequest, response2.status)
    }

    @Test
    fun `DELETE segment returns 400 when segment belongs to another flag`() = testApplication {
        val segmentService = mockk<SegmentService>()
        coEvery { segmentService.getSegment(10) } returns Segment(id = 10, flagId = 2, description = "s", rank = 0, rolloutPercent = 100)

        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            routing {
                configureSegmentRoutes(segmentService)
            }
        }

        val response = client.delete("/api/v1/flags/1/segments/10")
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains("does not belong to this flag"))
    }

    @Test
    fun `PUT segments reorder with invalid flag id returns 400`() = testApplication {
        val segmentService = mockk<SegmentService>(relaxed = true)

        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            routing {
                configureSegmentRoutes(segmentService)
            }
        }

        val response = client.put("/api/v1/flags/notanum/segments/reorder") {
            contentType(ContentType.Application.Json)
            setBody("""{"segmentIDs":[1,2,3]}""")
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }
}
