package flagent.route

import flagent.domain.entity.FlagSnapshot
import flagent.repository.Database
import flagent.repository.impl.FlagRepository
import flagent.repository.impl.FlagSnapshotRepository
import flagent.service.FlagSnapshotService
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FlagSnapshotRoutesTest {
    @Test
    fun testGetSnapshots() = testApplication {
        Database.init()
        try {
            val flagRepository = FlagRepository()
            val flagSnapshotRepository = FlagSnapshotRepository()
            val flagSnapshotService = FlagSnapshotService(flagSnapshotRepository, flagRepository)

            application {
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
                routing {
                    configureFlagSnapshotRoutes(flagSnapshotService)
                }
            }

            val flag = flagRepository.create(
                flagent.domain.entity.Flag(
                    key = "test_flag",
                    description = "Test flag",
                    enabled = true
                )
            )

            val response = client.get("/api/v1/flags/${flag.id}/snapshots")
            assertEquals(HttpStatusCode.OK, response.status)
            val json = Json.parseToJsonElement(response.bodyAsText())
            assertTrue(json is JsonArray)
        } finally {
            Database.close()
        }
    }

    @Test
    fun `GET snapshots with invalid flag id returns 400`() = testApplication {
        val service = mockk<FlagSnapshotService>()
        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            routing {
                configureFlagSnapshotRoutes(service)
            }
        }

        val response = client.get("/api/v1/flags/not-a-number/snapshots")
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `GET snapshots returns 404 when service throws IllegalArgumentException`() = testApplication {
        val service = mockk<FlagSnapshotService>()
        coEvery { service.findSnapshotsByFlagId(42, any(), any(), any()) } throws IllegalArgumentException("error finding flagID 42")

        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            routing {
                configureFlagSnapshotRoutes(service)
            }
        }

        val response = client.get("/api/v1/flags/42/snapshots")
        assertEquals(HttpStatusCode.NotFound, response.status)
        assertTrue(response.bodyAsText().contains("flagID 42"))
    }

    @Test
    fun `GET snapshots returns 500 on unexpected exception`() = testApplication {
        val service = mockk<FlagSnapshotService>()
        coEvery { service.findSnapshotsByFlagId(7, any(), any(), any()) } throws RuntimeException("boom")

        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            routing {
                configureFlagSnapshotRoutes(service)
            }
        }

        val response = client.get("/api/v1/flags/7/snapshots")
        assertEquals(HttpStatusCode.InternalServerError, response.status)
        assertTrue(response.bodyAsText().contains("boom"))
    }

    @Test
    fun `GET snapshots maps malformed flag JSON with fallback`() = testApplication {
        val service = mockk<FlagSnapshotService>()
        val snapshot = FlagSnapshot(
            id = 10,
            flagId = 99,
            updatedBy = "tester",
            flag = "{not-json",
            updatedAt = "2024-01-01T00:00:00Z"
        )
        coEvery { service.findSnapshotsByFlagId(99, any(), any(), any()) } returns listOf(snapshot)

        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            routing {
                configureFlagSnapshotRoutes(service)
            }
        }

        val response = client.get("/api/v1/flags/99/snapshots")
        assertEquals(HttpStatusCode.OK, response.status)
        val json = Json.parseToJsonElement(response.bodyAsText())
        assertTrue(json is JsonArray)
        val first = json[0].jsonObject
        val flagObj = first["flag"]!!.jsonObject
        // Fallback flagResponse should have default values
        assertEquals("0", flagObj["id"]!!.jsonPrimitive.content)
        assertEquals("", flagObj["key"]!!.jsonPrimitive.content)
        assertEquals("false", flagObj["enabled"]!!.jsonPrimitive.content)
    }
}

