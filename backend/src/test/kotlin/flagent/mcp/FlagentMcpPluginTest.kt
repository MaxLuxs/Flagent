package flagent.mcp

import flagent.cache.impl.EvalCache
import flagent.cache.impl.EvalCacheJSON
import flagent.service.EvalResult
import flagent.service.EvaluationService
import flagent.service.EvalContext
import flagent.service.FlagService
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertNotNull

/**
 * Tests for Flagent MCP plugin.
 * Covers server creation and registration; full tool handler coverage would require MCP transport integration.
 */
class FlagentMcpPluginTest {

    private fun defaultEvalResult() = EvalResult(
        flagID = 1,
        flagKey = "test_flag",
        flagSnapshotID = 0,
        flagTags = emptyList(),
        segmentID = null,
        variantID = 1,
        variantKey = "on",
        variantAttachment = null,
        evalContext = EvalContext(),
        evalDebugLog = null,
        timestamp = 0L
    )

    @Test
    fun createFlagentMcpServer_returnsServer_withEvalOnly() = runBlocking {
        val evaluationService = mockk<EvaluationService>(relaxed = true)
        val evalCache = mockk<EvalCache>(relaxed = true)
        coEvery { evaluationService.evaluateFlag(any(), any(), any(), any(), any(), any(), any()) } returns defaultEvalResult()
        every { evalCache.export() } returns EvalCacheJSON(flags = emptyList())
        val server = createFlagentMcpServer(evaluationService, evalCache, flagService = null)
        assertNotNull(server)
    }

    @Test
    fun createFlagentMcpServer_returnsServer_withFlagService() = runBlocking {
        val evaluationService = mockk<EvaluationService>(relaxed = true)
        val evalCache = mockk<EvalCache>(relaxed = true)
        val flagService = mockk<FlagService>(relaxed = true)
        coEvery { evaluationService.evaluateFlag(any(), any(), any(), any(), any(), any(), any()) } returns defaultEvalResult()
        every { evalCache.export() } returns EvalCacheJSON(flags = emptyList())
        val server = createFlagentMcpServer(evaluationService, evalCache, flagService)
        assertNotNull(server)
    }
}
