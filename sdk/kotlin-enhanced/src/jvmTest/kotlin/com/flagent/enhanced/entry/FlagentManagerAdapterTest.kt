package com.flagent.enhanced.entry

import com.flagent.client.models.EvalResult
import com.flagent.client.models.EvaluationEntity
import com.flagent.enhanced.config.FlagentConfig
import com.flagent.enhanced.manager.FlagentManager
import com.flagent.client.apis.EvaluationApi
import com.flagent.client.infrastructure.BodyProvider
import com.flagent.client.infrastructure.HttpResponse
import io.ktor.client.statement.HttpResponse as KtorHttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.util.reflect.TypeInfo
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test

private class FixedBodyProvider<T : Any>(private val value: T) : BodyProvider<T> {
    override suspend fun body(response: KtorHttpResponse): T = value
    @Suppress("UNCHECKED_CAST")
    override suspend fun <V : Any> typedBody(response: KtorHttpResponse, type: TypeInfo): V = value as V
}

class FlagentManagerAdapterTest {

    private fun successKtorResponse() = mockk<KtorHttpResponse>(relaxed = true) {
        every { status } returns HttpStatusCode.OK
    }

    @Test
    fun `adapter evaluate delegates to manager`() = runBlocking {
        val mockApi = mockk<EvaluationApi>()
        val manager = FlagentManager(mockApi, FlagentConfig(enableCache = false))
        val client: FlagentClient = FlagentManagerAdapter(manager)

        val expected = EvalResult(flagID = 1, flagKey = "f1", variantKey = "on")
        coEvery { mockApi.postEvaluation(any()) } returns HttpResponse(
            successKtorResponse(),
            FixedBodyProvider(expected)
        )

        val result = client.evaluate(flagKey = "f1", entityID = "user1")
        assertEquals("f1", result.flagKey)
        assertEquals("on", result.variantKey)
    }

    @Test
    fun `adapter isEnabled returns true when variantKey is set`() = runBlocking {
        val mockApi = mockk<EvaluationApi>()
        val manager = FlagentManager(mockApi, FlagentConfig(enableCache = false))
        val client: FlagentClient = FlagentManagerAdapter(manager)

        coEvery { mockApi.postEvaluation(any()) } returns HttpResponse(
            successKtorResponse(),
            FixedBodyProvider(EvalResult(flagID = 1, flagKey = "f1", variantKey = "on"))
        )
        assert(client.isEnabled("f1", "user1"))
    }

    @Test
    fun `adapter isEnabled returns false when variantKey is null`() = runBlocking {
        val mockApi = mockk<EvaluationApi>()
        val manager = FlagentManager(mockApi, FlagentConfig(enableCache = false))
        val client: FlagentClient = FlagentManagerAdapter(manager)

        coEvery { mockApi.postEvaluation(any()) } returns HttpResponse(
            successKtorResponse(),
            FixedBodyProvider(EvalResult(flagID = 1, flagKey = "f1", variantKey = null))
        )
        assertFalse(client.isEnabled("f1", "user1"))
    }

    @Test
    fun `adapter evaluateBatch delegates to manager`() = runBlocking {
        val mockApi = mockk<EvaluationApi>()
        val manager = FlagentManager(mockApi, FlagentConfig(enableCache = false))
        val client: FlagentClient = FlagentManagerAdapter(manager)

        val expected = listOf(
            EvalResult(flagID = 1, flagKey = "a", variantKey = "x"),
            EvalResult(flagID = 2, flagKey = "b", variantKey = "y")
        )
        coEvery { mockApi.postEvaluationBatch(any()) } returns HttpResponse(
            successKtorResponse(),
            FixedBodyProvider(com.flagent.client.models.EvaluationBatchResponse(evaluationResults = expected))
        )

        val entities = listOf(EvaluationEntity(entityID = "u1", entityType = "user"))
        val results = client.evaluateBatch(flagKeys = listOf("a", "b"), entities = entities)
        assertEquals(2, results.size)
        assertEquals("a", results[0].flagKey)
        assertEquals("b", results[1].flagKey)
    }
}
