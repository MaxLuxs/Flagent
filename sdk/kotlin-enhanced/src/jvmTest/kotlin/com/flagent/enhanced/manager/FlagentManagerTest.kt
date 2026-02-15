package com.flagent.enhanced.manager

import com.flagent.client.apis.EvaluationApi
import com.flagent.client.models.EvalContext
import com.flagent.client.models.EvalResult
import com.flagent.client.models.EvaluationBatchRequest
import com.flagent.client.models.EvaluationBatchResponse
import com.flagent.client.models.EvaluationEntity
import com.flagent.enhanced.config.FlagentConfig
import com.flagent.client.infrastructure.BodyProvider
import com.flagent.client.infrastructure.HttpResponse
import io.ktor.client.statement.HttpResponse as KtorHttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.util.reflect.TypeInfo
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/** Fixed-body provider to avoid mockk suspend/generic ClassCastException in CI. */
private class FixedBodyProvider<T : Any>(private val value: T) : BodyProvider<T> {
    override suspend fun body(response: KtorHttpResponse): T = value
    @Suppress("UNCHECKED_CAST")
    override suspend fun <V : Any> typedBody(response: KtorHttpResponse, type: TypeInfo): V = value as V
}

class FlagentManagerTest {
    private fun successKtorResponse() = mockk<io.ktor.client.statement.HttpResponse>(relaxed = true) {
        every { status } returns HttpStatusCode.OK
    }

    @Test
    fun `test evaluate with cache disabled`() = runBlocking {
        val mockApi = mockk<EvaluationApi>()
        val config = FlagentConfig(enableCache = false)
        val manager = FlagentManager(mockApi, config)
        
        val expectedResult = EvalResult(
            flagID = 1,
            flagKey = "test_flag",
            variantKey = "control"
        )
        
        coEvery {
            mockApi.postEvaluation(any())
        } returns HttpResponse(successKtorResponse(), FixedBodyProvider(expectedResult))
        
        val result = manager.evaluate(flagKey = "test_flag", entityID = "user1")
        
        assertEquals("test_flag", result.flagKey)
        assertEquals("control", result.variantKey)
    }
    
    @Test
    fun `test evaluateBatch`() = runBlocking {
        val mockApi = mockk<EvaluationApi>()
        val manager = FlagentManager(mockApi, FlagentConfig(enableCache = false))
        
        val expectedResults = listOf(
            EvalResult(flagID = 1, flagKey = "flag1", variantKey = "control"),
            EvalResult(flagID = 2, flagKey = "flag2", variantKey = "variant_a")
        )
        
        val batchResponse = EvaluationBatchResponse(evaluationResults = expectedResults)
        
        coEvery {
            mockApi.postEvaluationBatch(any())
        } returns HttpResponse(successKtorResponse(), FixedBodyProvider(batchResponse))
        
        val entities = listOf(
            EvaluationEntity(entityID = "user1", entityType = "user")
        )
        
        val results = manager.evaluateBatch(
            flagKeys = listOf("flag1", "flag2"),
            entities = entities
        )
        
        assertEquals(2, results.size)
        assertEquals("flag1", results[0].flagKey)
        assertEquals("flag2", results[1].flagKey)
    }
    
    @Test
    fun `test clearCache`() = runBlocking {
        val mockApi = mockk<EvaluationApi>()
        val manager = FlagentManager(mockApi, FlagentConfig(enableCache = true))
        
        // Should not throw
        manager.clearCache()
    }
    
    @Test
    fun `test evictExpired`() = runBlocking {
        val mockApi = mockk<EvaluationApi>()
        val manager = FlagentManager(mockApi, FlagentConfig(enableCache = true))
        
        // Should not throw
        manager.evictExpired()
    }
}