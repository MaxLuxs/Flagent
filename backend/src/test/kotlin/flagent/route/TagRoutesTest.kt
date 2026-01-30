package flagent.route

import flagent.repository.Database
import flagent.repository.impl.FlagRepository
import flagent.repository.impl.TagRepository
import flagent.service.TagService
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.ktor.serialization.kotlinx.json.*
import kotlin.test.*
import kotlinx.serialization.json.*

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
            
            val flag = flagRepository.create(flagent.domain.entity.Flag(key = "test_flag", description = "Test flag", enabled = true))
            
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
            
            val flag = flagRepository.create(flagent.domain.entity.Flag(key = "test_flag", description = "Test flag", enabled = true))
            
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
            
            val flag = flagRepository.create(flagent.domain.entity.Flag(key = "test_flag", description = "Test flag", enabled = true))
            val tag = tagRepository.create(flagent.domain.entity.Tag(value = "test_tag"))
            tagRepository.addTagToFlag(flag.id, tag.id)
            
            val response = client.delete("/api/v1/flags/${flag.id}/tags/${tag.id}")
            assertEquals(HttpStatusCode.OK, response.status)
        } finally {
            Database.close()
        }
    }
}
