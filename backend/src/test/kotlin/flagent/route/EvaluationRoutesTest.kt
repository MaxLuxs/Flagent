package flagent.route

import flagent.cache.impl.EvalCache
import flagent.repository.impl.FlagRepository
import flagent.service.EvaluationService
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlin.test.*
import kotlinx.serialization.json.*

class EvaluationRoutesTest {
    @Test
    fun testEvaluationEndpoint() = testApplication {
        val flagRepository = FlagRepository()
        val evalCache = EvalCache(flagRepository)
        evalCache.start()
        val evaluationService = EvaluationService(evalCache)
        
        application {
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
    }
    
    @Test
    fun testEvaluationBatchEndpoint() = testApplication {
        val flagRepository = FlagRepository()
        val evalCache = EvalCache(flagRepository)
        evalCache.start()
        val evaluationService = EvaluationService(evalCache)
        
        application {
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
    }
}
