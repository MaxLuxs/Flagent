package flagent.route

import flagent.repository.Database
import flagent.repository.impl.FlagRepository
import flagent.service.FlagService
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlin.test.*
import kotlinx.serialization.json.*

class FlagRoutesTest {
    @Test
    fun testGetFlags() = testApplication {
        Database.init()
        try {
            val flagRepository = FlagRepository()
            val flagService = FlagService(flagRepository)
            
            application {
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
                routing {
                    configureFlagRoutes(flagService)
                }
            }
            
            val response = client.get("/api/v1/flags")
            assertEquals(HttpStatusCode.OK, response.status)
            val json = Json.parseToJsonElement(response.bodyAsText())
            assertTrue(json is JsonArray)
        } finally {
            Database.close()
        }
    }
    
    @Test
    fun testCreateFlag() = testApplication {
        Database.init()
        try {
            val flagRepository = FlagRepository()
            val flagService = FlagService(flagRepository)
            
            application {
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
                routing {
                    configureFlagRoutes(flagService)
                }
            }
            
            val response = client.post("/api/v1/flags") {
                contentType(ContentType.Application.Json)
                setBody("""{"description":"Test flag"}""")
            }
            
            assertEquals(HttpStatusCode.OK, response.status)
            val json = Json.parseToJsonElement(response.bodyAsText())
            assertTrue(json.jsonObject.containsKey("id"))
            assertTrue(json.jsonObject.containsKey("key"))
        } finally {
            Database.close()
        }
    }
    
    @Test
    fun testGetFlagById() = testApplication {
        Database.init()
        try {
            val flagRepository = FlagRepository()
            val flagService = FlagService(flagRepository)
            
            application {
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
                routing {
                    configureFlagRoutes(flagService)
                }
            }
            
            // First create a flag
            val createResponse = client.post("/api/v1/flags") {
                contentType(ContentType.Application.Json)
                setBody("""{"description":"Test flag"}""")
            }
            val createdFlag = Json.parseToJsonElement(createResponse.bodyAsText())
            val flagId = createdFlag.jsonObject["id"]!!.jsonPrimitive.int
            
            // Then get it
            val response = client.get("/api/v1/flags/$flagId")
            assertEquals(HttpStatusCode.OK, response.status)
            val json = Json.parseToJsonElement(response.bodyAsText())
            assertEquals(flagId, json.jsonObject["id"]!!.jsonPrimitive.int)
        } finally {
            Database.close()
        }
    }
    
    @Test
    fun testGetFlags_ReturnsXTotalCountHeader() = testApplication {
        Database.init()
        try {
            val flagRepository = FlagRepository()
            val flagService = FlagService(flagRepository)
            
            application {
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
                routing {
                    configureFlagRoutes(flagService)
                }
            }
            
            val response = client.get("/api/v1/flags")
            assertEquals(HttpStatusCode.OK, response.status)
            val totalCount = response.headers["X-Total-Count"]
            assertNotNull(totalCount)
            assertTrue(totalCount!!.toLongOrNull()!! >= 0)
        } finally {
            Database.close()
        }
    }
    
    @Test
    fun testGetFlags_WithPaginationParams() = testApplication {
        Database.init()
        try {
            val flagRepository = FlagRepository()
            val flagService = FlagService(flagRepository)
            
            application {
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
                routing {
                    configureFlagRoutes(flagService)
                }
            }
            
            val response = client.get("/api/v1/flags?limit=5&offset=0")
            assertEquals(HttpStatusCode.OK, response.status)
            val json = Json.parseToJsonElement(response.bodyAsText())
            assertTrue(json is JsonArray)
            assertTrue(json.jsonArray.size <= 5)
        } finally {
            Database.close()
        }
    }
    
    @Test
    fun testGetFlags_WithFilters() = testApplication {
        Database.init()
        try {
            val flagRepository = FlagRepository()
            val flagService = FlagService(flagRepository)
            
            application {
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
                routing {
                    configureFlagRoutes(flagService)
                }
            }
            
            val response = client.get("/api/v1/flags?enabled=true&descriptionLike=test")
            assertEquals(HttpStatusCode.OK, response.status)
            val totalCount = response.headers["X-Total-Count"]
            assertNotNull(totalCount)
        } finally {
            Database.close()
        }
    }

    @Test
    fun testGetFlags_WithKeyFilter() = testApplication {
        Database.init()
        try {
            val flagRepository = FlagRepository()
            val flagService = FlagService(flagRepository)
            
            application {
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
                routing {
                    configureFlagRoutes(flagService)
                }
            }
            
            val uniqueKey = "key_filter_${System.currentTimeMillis()}"
            client.post("/api/v1/flags") {
                contentType(ContentType.Application.Json)
                setBody("""{"description":"Key filter test","key":"$uniqueKey"}""")
            }
            client.post("/api/v1/flags") {
                contentType(ContentType.Application.Json)
                setBody("""{"description":"Other flag","key":"other_key_${System.currentTimeMillis()}"}""")
            }
            
            val response = client.get("/api/v1/flags?key=$uniqueKey")
            assertEquals(HttpStatusCode.OK, response.status)
            val json = Json.parseToJsonElement(response.bodyAsText())
            assertTrue(json is JsonArray)
            assertEquals(1, json.jsonArray.size)
            assertEquals(uniqueKey, json.jsonArray[0].jsonObject["key"]?.jsonPrimitive?.content)
            assertEquals("1", response.headers["X-Total-Count"])
        } finally {
            Database.close()
        }
    }
    
    @Test
    fun testBatchSetEnabled_ReturnsUpdatedFlags() = testApplication {
        Database.init()
        try {
            val flagRepository = FlagRepository()
            val flagService = FlagService(flagRepository)
            
            application {
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
                routing {
                    configureFlagRoutes(flagService)
                }
            }
            
            val createResponse = client.post("/api/v1/flags") {
                contentType(ContentType.Application.Json)
                setBody("""{"description":"Batch test flag","key":"batch_test_flag"}""")
            }
            val created = Json.parseToJsonElement(createResponse.bodyAsText()).jsonObject
            val flagId = created["id"]!!.jsonPrimitive.int
            
            val batchResponse = client.put("/api/v1/flags/batch/enabled") {
                contentType(ContentType.Application.Json)
                setBody("""{"ids":[${flagId}],"enabled":true}""")
            }
            assertEquals(HttpStatusCode.OK, batchResponse.status)
            val updated = Json.parseToJsonElement(batchResponse.bodyAsText())
            assertTrue(updated is JsonArray)
            assertEquals(1, updated.jsonArray.size)
            assertEquals(true, updated.jsonArray[0].jsonObject["enabled"]?.jsonPrimitive?.content?.toBoolean())
        } finally {
            Database.close()
        }
    }
    
    @Test
    fun testBatchSetEnabled_Returns400WhenIdsEmpty() = testApplication {
        Database.init()
        try {
            val flagRepository = FlagRepository()
            val flagService = FlagService(flagRepository)
            
            application {
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
                routing {
                    configureFlagRoutes(flagService)
                }
            }
            
            val response = client.put("/api/v1/flags/batch/enabled") {
                contentType(ContentType.Application.Json)
                setBody("""{"ids":[],"enabled":true}""")
            }
            assertEquals(HttpStatusCode.BadRequest, response.status)
        } finally {
            Database.close()
        }
    }
}
