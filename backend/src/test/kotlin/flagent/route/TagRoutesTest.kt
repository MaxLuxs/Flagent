package flagent.route

import flagent.domain.entity.Tag
import flagent.repository.Database
import flagent.repository.impl.FlagRepository
import flagent.repository.impl.TagRepository
import flagent.service.TagService
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
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TagRoutesTest {
    @Test
    fun testGetTags() = testApplication {
        Database.init()
        try {
            val flagRepository = FlagRepository()
            val tagRepository = TagRepository()
            val tagService = TagService(tagRepository, flagRepository)

            application {
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
                routing {
                    configureTagRoutes(tagService)
                }
            }

            val flag = flagRepository.create(
                flagent.domain.entity.Flag(
                    key = "test_flag",
                    description = "Test flag",
                    enabled = true
                )
            )

            val response = client.get("/api/v1/flags/${flag.id}/tags")
            assertEquals(HttpStatusCode.OK, response.status)
            val json = Json.parseToJsonElement(response.bodyAsText())
            assertTrue(json is JsonArray)
        } finally {
            Database.close()
        }
    }

    @Test
    fun testCreateTag() = testApplication {
        Database.init()
        try {
            val flagRepository = FlagRepository()
            val tagRepository = TagRepository()
            val tagService = TagService(tagRepository, flagRepository)

            application {
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
                routing {
                    configureTagRoutes(tagService)
                }
            }

            val flag = flagRepository.create(
                flagent.domain.entity.Flag(
                    key = "test_flag",
                    description = "Test flag",
                    enabled = true
                )
            )

            val response = client.post("/api/v1/flags/${flag.id}/tags") {
                contentType(ContentType.Application.Json)
                setBody("""{"value":"test_tag"}""")
            }

            assertEquals(HttpStatusCode.OK, response.status)
            val json = Json.parseToJsonElement(response.bodyAsText())
            assertTrue(json.jsonObject.containsKey("id"))
        } finally {
            Database.close()
        }
    }

    @Test
    fun testDeleteTag() = testApplication {
        Database.init()
        try {
            val flagRepository = FlagRepository()
            val tagRepository = TagRepository()
            val tagService = TagService(tagRepository, flagRepository)

            application {
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
                routing {
                    configureTagRoutes(tagService)
                }
            }

            val flag = flagRepository.create(
                flagent.domain.entity.Flag(
                    key = "test_flag",
                    description = "Test flag",
                    enabled = true
                )
            )
            val tag = tagRepository.create(flagent.domain.entity.Tag(value = "test_tag"))
            tagRepository.addTagToFlag(flag.id, tag.id)

            val response = client.delete("/api/v1/flags/${flag.id}/tags/${tag.id}")
            assertEquals(HttpStatusCode.OK, response.status)
        } finally {
            Database.close()
        }
    }

    @Test
    fun `GET flags tags with invalid flag id returns 400`() = testApplication {
        val service = mockk<TagService>()
        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            routing { configureTagRoutes(service) }
        }

        val response = client.get("/api/v1/flags/notanum/tags")
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `GET flags tags returns 404 when service throws IllegalArgumentException`() = testApplication {
        val service = mockk<TagService>()
        coEvery { service.findTagsByFlagId(123) } throws IllegalArgumentException("error finding flagID 123")

        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            routing { configureTagRoutes(service) }
        }

        val response = client.get("/api/v1/flags/123/tags")
        assertEquals(HttpStatusCode.NotFound, response.status)
        assertTrue(response.bodyAsText().contains("flagID 123"))
    }

    @Test
    fun `POST flags tags with invalid flag id returns 400`() = testApplication {
        val service = mockk<TagService>()
        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            routing { configureTagRoutes(service) }
        }

        val response = client.post("/api/v1/flags/notanum/tags") {
            contentType(ContentType.Application.Json)
            setBody("""{"value":"tag"}""")
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `POST flags tags maps IllegalArgumentException with error finding to 404`() = testApplication {
        val service = mockk<TagService>()
        coEvery { service.createTag(any(), any()) } throws IllegalArgumentException("error finding flagID 5")

        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            routing { configureTagRoutes(service) }
        }

        val response = client.post("/api/v1/flags/5/tags") {
            contentType(ContentType.Application.Json)
            setBody("""{"value":"tag"}""")
        }
        assertEquals(HttpStatusCode.NotFound, response.status)
        assertTrue(response.bodyAsText().contains("flagID 5"))
    }

    @Test
    fun `DELETE flags tags with invalid ids returns 400`() = testApplication {
        val service = mockk<TagService>()
        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            routing { configureTagRoutes(service) }
        }

        val response1 = client.delete("/api/v1/flags/notanum/tags/1")
        assertEquals(HttpStatusCode.BadRequest, response1.status)

        val response2 = client.delete("/api/v1/flags/1/tags/notanum")
        assertEquals(HttpStatusCode.BadRequest, response2.status)
    }

    @Test
    fun `DELETE flags tags maps IllegalArgumentException with error finding to 404`() = testApplication {
        val service = mockk<TagService>()
        coEvery { service.deleteTag(10, 20, any()) } throws IllegalArgumentException("error finding tagID 20")

        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            routing { configureTagRoutes(service) }
        }

        val response = client.delete("/api/v1/flags/10/tags/20")
        assertEquals(HttpStatusCode.NotFound, response.status)
        assertTrue(response.bodyAsText().contains("tagID 20"))
    }
}

