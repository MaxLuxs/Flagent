package flagent.route

import flagent.domain.entity.Constraint
import flagent.repository.Database
import flagent.repository.impl.*
import flagent.service.ConstraintService
import flagent.service.FlagSnapshotService
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

class ConstraintRoutesTest {
    @Test
    fun testGetConstraints() = testApplication {
        Database.init()
        try {
            val flagRepository = FlagRepository()
            val segmentRepository = SegmentRepository()
            val constraintRepository = ConstraintRepository()
            val flagSnapshotRepository = FlagSnapshotRepository()
            val flagSnapshotService = FlagSnapshotService(flagSnapshotRepository, flagRepository)
            val constraintService = ConstraintService(constraintRepository, segmentRepository, flagSnapshotService)
            
            application {
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
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
        } finally {
            Database.close()
        }
    }
    
    @Test
    fun testCreateConstraint() = testApplication {
        Database.init()
        try {
            val flagRepository = FlagRepository()
            val segmentRepository = SegmentRepository()
            val constraintRepository = ConstraintRepository()
            val flagSnapshotRepository = FlagSnapshotRepository()
            val flagSnapshotService = FlagSnapshotService(flagSnapshotRepository, flagRepository)
            val constraintService = ConstraintService(constraintRepository, segmentRepository, flagSnapshotService)
            
            application {
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
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
        } finally {
            Database.close()
        }
    }
    
    @Test
    fun testUpdateConstraint() = testApplication {
        Database.init()
        try {
            val flagRepository = FlagRepository()
            val segmentRepository = SegmentRepository()
            val constraintRepository = ConstraintRepository()
            val flagSnapshotRepository = FlagSnapshotRepository()
            val flagSnapshotService = FlagSnapshotService(flagSnapshotRepository, flagRepository)
            val constraintService = ConstraintService(constraintRepository, segmentRepository, flagSnapshotService)
            
            application {
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
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
        } finally {
            Database.close()
        }
    }
    
    @Test
    fun testDeleteConstraint() = testApplication {
        Database.init()
        try {
            val flagRepository = FlagRepository()
            val segmentRepository = SegmentRepository()
            val constraintRepository = ConstraintRepository()
            val flagSnapshotRepository = FlagSnapshotRepository()
            val flagSnapshotService = FlagSnapshotService(flagSnapshotRepository, flagRepository)
            val constraintService = ConstraintService(constraintRepository, segmentRepository, flagSnapshotService)
            
            application {
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
                routing {
                    configureConstraintRoutes(constraintService)
                }
            }
            
            val flag = flagRepository.create(flagent.domain.entity.Flag(key = "test_flag", description = "Test flag", enabled = true))
            val segment = segmentRepository.create(flagent.domain.entity.Segment(flagId = flag.id, description = "Test segment", rank = 1, rolloutPercent = 100))
            val constraint = constraintRepository.create(flagent.domain.entity.Constraint(segmentId = segment.id, property = "region", operator = "EQ", value = "US"))
            
            val response = client.delete("/api/v1/flags/${flag.id}/segments/${segment.id}/constraints/${constraint.id}")
            assertEquals(HttpStatusCode.OK, response.status)
        } finally {
            Database.close()
        }
    }

    @Test
    fun `GET constraints with invalid ids returns 400`() = testApplication {
        val constraintService = mockk<ConstraintService>(relaxed = true)

        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            routing { configureConstraintRoutes(constraintService) }
        }

        val resp1 = client.get("/api/v1/flags/notanum/segments/1/constraints")
        assertEquals(HttpStatusCode.BadRequest, resp1.status)

        val resp2 = client.get("/api/v1/flags/1/segments/notanum/constraints")
        assertEquals(HttpStatusCode.BadRequest, resp2.status)
    }

    @Test
    fun `POST constraints with invalid ids returns 400`() = testApplication {
        val constraintService = mockk<ConstraintService>(relaxed = true)

        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            routing { configureConstraintRoutes(constraintService) }
        }

        val body = """{"property":"region","operator":"EQ","value":"US"}"""

        val resp1 = client.post("/api/v1/flags/notanum/segments/1/constraints") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        assertEquals(HttpStatusCode.BadRequest, resp1.status)

        val resp2 = client.post("/api/v1/flags/1/segments/notanum/constraints") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        assertEquals(HttpStatusCode.BadRequest, resp2.status)
    }

    @Test
    fun `POST constraints maps IllegalArgumentException to 400`() = testApplication {
        val constraintService = mockk<ConstraintService>()
        coEvery { constraintService.createConstraint(any(), any()) } throws IllegalArgumentException("invalid constraint")

        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            routing { configureConstraintRoutes(constraintService) }
        }

        val resp = client.post("/api/v1/flags/1/segments/2/constraints") {
            contentType(ContentType.Application.Json)
            setBody("""{"property":"region","operator":"EQ","value":"US"}""")
        }
        assertEquals(HttpStatusCode.BadRequest, resp.status)
        assertTrue(resp.bodyAsText().contains("invalid constraint"))
    }

    @Test
    fun `PUT constraint with invalid ids returns 400`() = testApplication {
        val constraintService = mockk<ConstraintService>(relaxed = true)

        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            routing { configureConstraintRoutes(constraintService) }
        }

        val body = """{"property":"region","operator":"EQ","value":"US"}"""

        val resp1 = client.put("/api/v1/flags/notanum/segments/1/constraints/1") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        assertEquals(HttpStatusCode.BadRequest, resp1.status)

        val resp2 = client.put("/api/v1/flags/1/segments/notanum/constraints/1") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        assertEquals(HttpStatusCode.BadRequest, resp2.status)

        val resp3 = client.put("/api/v1/flags/1/segments/1/constraints/notanum") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        assertEquals(HttpStatusCode.BadRequest, resp3.status)
    }

    @Test
    fun `PUT constraint returns 404 when not found`() = testApplication {
        val constraintService = mockk<ConstraintService>()
        coEvery { constraintService.getConstraint(10) } returns null

        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            routing { configureConstraintRoutes(constraintService) }
        }

        val resp = client.put("/api/v1/flags/1/segments/1/constraints/10") {
            contentType(ContentType.Application.Json)
            setBody("""{"property":"region","operator":"EQ","value":"US"}""")
        }
        assertEquals(HttpStatusCode.NotFound, resp.status)
    }

    @Test
    fun `PUT constraint returns 400 when belongs to another segment`() = testApplication {
        val constraintService = mockk<ConstraintService>()
        coEvery { constraintService.getConstraint(10) } returns Constraint(id = 10, segmentId = 999, property = "p", operator = "EQ", value = "v")

        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            routing { configureConstraintRoutes(constraintService) }
        }

        val resp = client.put("/api/v1/flags/1/segments/1/constraints/10") {
            contentType(ContentType.Application.Json)
            setBody("""{"property":"region","operator":"EQ","value":"US"}""")
        }
        assertEquals(HttpStatusCode.BadRequest, resp.status)
        assertTrue(resp.bodyAsText().contains("does not belong to this segment"))
    }

    @Test
    fun `DELETE constraint with invalid ids returns 400`() = testApplication {
        val constraintService = mockk<ConstraintService>(relaxed = true)

        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            routing { configureConstraintRoutes(constraintService) }
        }

        val resp1 = client.delete("/api/v1/flags/notanum/segments/1/constraints/1")
        assertEquals(HttpStatusCode.BadRequest, resp1.status)

        val resp2 = client.delete("/api/v1/flags/1/segments/notanum/constraints/1")
        assertEquals(HttpStatusCode.BadRequest, resp2.status)

        val resp3 = client.delete("/api/v1/flags/1/segments/1/constraints/notanum")
        assertEquals(HttpStatusCode.BadRequest, resp3.status)
    }

    @Test
    fun `DELETE constraint returns 404 when not found`() = testApplication {
        val constraintService = mockk<ConstraintService>()
        coEvery { constraintService.getConstraint(10) } returns null

        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            routing { configureConstraintRoutes(constraintService) }
        }

        val resp = client.delete("/api/v1/flags/1/segments/1/constraints/10")
        assertEquals(HttpStatusCode.NotFound, resp.status)
    }

    @Test
    fun `DELETE constraint returns 400 when belongs to another segment`() = testApplication {
        val constraintService = mockk<ConstraintService>()
        coEvery { constraintService.getConstraint(10) } returns Constraint(id = 10, segmentId = 999, property = "p", operator = "EQ", value = "v")

        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            routing { configureConstraintRoutes(constraintService) }
        }

        val resp = client.delete("/api/v1/flags/1/segments/1/constraints/10")
        assertEquals(HttpStatusCode.BadRequest, resp.status)
        assertTrue(resp.bodyAsText().contains("does not belong to this segment"))
    }
}
