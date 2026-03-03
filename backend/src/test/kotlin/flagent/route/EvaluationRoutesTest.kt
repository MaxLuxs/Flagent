package flagent.route

import flagent.api.model.EvaluationBatchRequest
import flagent.api.model.EvaluationBatchRequestEntity
import flagent.service.EvalContext
import flagent.service.EvalDebugLog
import flagent.service.EvalResult
import flagent.service.EvaluationService
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
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EvaluationRoutesTest {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private fun dummyEvalResult(): EvalResult =
        EvalResult(
            flagID = 1,
            flagKey = "test_flag",
            flagSnapshotID = 1,
            flagTags = emptyList(),
            segmentID = 1,
            variantID = 1,
            variantKey = "on",
            variantAttachment = mapOf("a" to "b"),
            evalContext = EvalContext(
                entityID = "e1",
                entityType = "user",
                entityContext = mapOf("region" to "US")
            ),
            evalDebugLog = EvalDebugLog(
                message = "ok",
                segmentDebugLogs = listOf(EvalDebugLog.SegmentDebugLog(segmentID = 1, message = "hit"))
            ),
            timestamp = 123L
        )

    @Test
    fun `POST evaluation maps EvalResult to EvaluationResponse`() = testApplication {
        val service = mockk<EvaluationService>()
        coEvery {
            service.evaluateFlag(
                flagID = 1,
                flagKey = null,
                entityID = "e1",
                entityType = "user",
                entityContext = any(),
                enableDebug = true,
                environmentId = any(),
                clientId = "client-1"
            )
        } returns dummyEvalResult()

        application {
            install(ContentNegotiation) { json(json) }
            routing { configureEvaluationRoutes(service) }
        }

        val resp = client.post("/api/v1/evaluation") {
            header("X-Client-Id", "client-1")
            contentType(ContentType.Application.Json)
            setBody(
                json.encodeToString(
                    mapOf(
                        "flagID" to 1,
                        "entityID" to "e1",
                        "entityType" to "user",
                        "entityContext" to mapOf("region" to "US"),
                        "enableDebug" to true
                    )
                )
            )
        }

        assertEquals(HttpStatusCode.OK, resp.status)
        val body = json.parseToJsonElement(resp.bodyAsText()).jsonObject
        assertEquals(1, body["flagID"]!!.jsonPrimitive.int)
        assertEquals("test_flag", body["flagKey"]!!.jsonPrimitive.content)
        assertEquals("on", body["variantKey"]!!.jsonPrimitive.content)
        val ctx = body["evalContext"]!!.jsonObject
        assertEquals("e1", ctx["entityID"]!!.jsonPrimitive.content)
        val debug = body["evalDebugLog"]!!.jsonObject
        assertEquals("ok", debug["msg"]!!.jsonPrimitive.content)
    }

    @Test
    fun `POST evaluation batch returns 400 when body invalid`() = testApplication {
        val service = mockk<EvaluationService>(relaxed = true)
        application {
            install(ContentNegotiation) { json(json) }
            routing { configureEvaluationRoutes(service) }
        }

        val resp = client.post("/api/v1/evaluation/batch") {
            contentType(ContentType.Application.Json)
            setBody("{invalid")
        }
        assertEquals(HttpStatusCode.BadRequest, resp.status)
        assertTrue(resp.bodyAsText().contains("Invalid request body"))
    }

    @Test
    fun `POST evaluation batch returns 400 when no flag selector`() = testApplication {
        val service = mockk<EvaluationService>(relaxed = true)
        application {
            install(ContentNegotiation) { json(json) }
            routing { configureEvaluationRoutes(service) }
        }

        val req = EvaluationBatchRequest(
            flagIDs = emptyList(),
            flagKeys = emptyList(),
            flagTags = emptyList(),
            flagTagsOperator = null,
            entities = listOf(EvaluationBatchRequestEntity(entityID = "e1", entityType = null, entityContext = null)),
            enableDebug = false
        )

        val resp = client.post("/api/v1/evaluation/batch") {
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(req))
        }
        assertEquals(HttpStatusCode.BadRequest, resp.status)
        assertTrue(resp.bodyAsText().contains("At least one of flagIDs, flagKeys, or flagTags is required"))
    }

    @Test
    fun `POST evaluation batch returns 400 when entities empty`() = testApplication {
        val service = mockk<EvaluationService>(relaxed = true)
        application {
            install(ContentNegotiation) { json(json) }
            routing { configureEvaluationRoutes(service) }
        }

        val req = EvaluationBatchRequest(
            flagIDs = listOf(1),
            flagKeys = emptyList(),
            flagTags = emptyList(),
            flagTagsOperator = null,
            entities = emptyList(),
            enableDebug = false
        )

        val resp = client.post("/api/v1/evaluation/batch") {
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(req))
        }
        assertEquals(HttpStatusCode.BadRequest, resp.status)
        assertTrue(resp.bodyAsText().contains("entities must not be empty"))
    }

    @Test
    fun `POST evaluation batch evaluates by tags and keys and ids`() = testApplication {
        val service = mockk<EvaluationService>()

        coEvery {
            service.evaluateFlagsByTags(
                tags = listOf("tag-a"),
                operator = "ALL",
                entityID = any(),
                entityType = any(),
                entityContext = any(),
                enableDebug = any(),
                environmentId = any(),
                clientId = any()
            )
        } returns listOf(dummyEvalResult())

        coEvery {
            service.evaluateFlag(
                flagID = 1,
                flagKey = null,
                entityID = any(),
                entityType = any(),
                entityContext = any(),
                enableDebug = any(),
                environmentId = any(),
                clientId = any()
            )
        } returns dummyEvalResult()

        coEvery {
            service.evaluateFlag(
                flagID = null,
                flagKey = "key-1",
                entityID = any(),
                entityType = any(),
                entityContext = any(),
                enableDebug = any(),
                environmentId = any(),
                clientId = any()
            )
        } returns dummyEvalResult()

        application {
            install(ContentNegotiation) { json(json) }
            routing { configureEvaluationRoutes(service) }
        }

        val req = EvaluationBatchRequest(
            flagIDs = listOf(1),
            flagKeys = listOf("key-1"),
            flagTags = listOf("tag-a"),
            flagTagsOperator = "ALL",
            entities = listOf(
                EvaluationBatchRequestEntity(entityID = "e1", entityType = "user", entityContext = mapOf("k" to JsonPrimitive("v")))
            ),
            enableDebug = true
        )

        val resp = client.post("/api/v1/evaluation/batch") {
            header("X-Client-Id", "client-1")
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(req))
        }

        assertEquals(HttpStatusCode.OK, resp.status)
        val body = json.parseToJsonElement(resp.bodyAsText()).jsonObject
        val arr = body["evaluationResults"] as JsonArray
        // 1 by tags + 1 by id + 1 by key = 3
        assertEquals(3, arr.size)
    }
}

package flagent.route

import flagent.cache.impl.EvalCache
import flagent.domain.usecase.EvaluateFlagUseCase
import flagent.repository.Database
import flagent.repository.impl.FlagRepository
import flagent.service.EvaluationService
import flagent.service.adapter.SharedFlagEvaluatorAdapter
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

class EvaluationRoutesTest {
    @Test
    fun testEvaluationEndpoint() = testApplication {
        Database.init()
        try {
            val flagRepository = FlagRepository()
            val evalCache = EvalCache(flagRepository)
            evalCache.start()
            val evaluateFlagUseCase = EvaluateFlagUseCase(SharedFlagEvaluatorAdapter())
            val evaluationService = EvaluationService(evalCache, evaluateFlagUseCase)
            
            application {
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
                routing {
                    configureEvaluationRoutes(evaluationService)
                }
            }
            
            val response = client.post("/api/v1/evaluation") {
                contentType(ContentType.Application.Json)
                setBody("""{"flagID":1,"entityID":"test-entity"}""")
            }
            
            assertEquals(HttpStatusCode.OK, response.status)
            val json = Json.parseToJsonElement(response.bodyAsText())
            assertTrue(json.jsonObject.containsKey("flagID"))
        } finally {
            Database.close()
        }
    }
    
    @Test
    fun testEvaluationBatchEndpoint() = testApplication {
        Database.init()
        try {
            val flagRepository = FlagRepository()
            val evalCache = EvalCache(flagRepository)
            evalCache.start()
            val evaluateFlagUseCase = EvaluateFlagUseCase(SharedFlagEvaluatorAdapter())
            val evaluationService = EvaluationService(evalCache, evaluateFlagUseCase)
            
            application {
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
                routing {
                    configureEvaluationRoutes(evaluationService)
                }
            }
            
            val response = client.post("/api/v1/evaluation/batch") {
                contentType(ContentType.Application.Json)
                setBody("""{"entities":[{"entityID":"test-entity"}],"flagIDs":[1]}""")
            }
            
            assertEquals(HttpStatusCode.OK, response.status)
            val json = Json.parseToJsonElement(response.bodyAsText())
            assertTrue(json.jsonObject.containsKey("evaluationResults"))
        } finally {
            Database.close()
        }
    }
}
