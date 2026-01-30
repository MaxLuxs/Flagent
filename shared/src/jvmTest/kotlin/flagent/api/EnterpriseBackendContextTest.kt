package flagent.api

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Unit tests for EnterpriseBackendContext and CoreDependencies API contract.
 * Verifies that backend implementations can be used through these interfaces.
 */
class EnterpriseBackendContextTest {

    @Test
    fun `runMigrations invokes block`() {
        var invoked = false
        val ctx = mockk<EnterpriseBackendContext>()
        every { ctx.runMigrations(any()) } answers {
            firstArg<() -> Unit>().invoke()
        }

        ctx.runMigrations { invoked = true }

        assertTrue(invoked)
    }

    @Test
    fun `runTransaction returns block result`() {
        val ctx = mockk<EnterpriseBackendContext>()
        every { ctx.runTransaction<Int>(any()) } answers {
            firstArg<() -> Int>().invoke()
        }

        val result = ctx.runTransaction { 42 }

        assertEquals(42, result)
    }

    @Test
    fun `runTransactionSuspend returns block result`() = runTest {
        val ctx = FakeBackendContext()
        val result = ctx.runTransactionSuspend { "ok" }
        assertEquals("ok", result)
    }

    private class FakeBackendContext : EnterpriseBackendContext {
        override fun runMigrations(block: () -> Unit) {}
        override fun <T> runTransaction(block: () -> T): T = block()
        override suspend fun <T> runTransactionSuspend(block: suspend () -> T): T = block()
        override suspend fun createTenantSchema(schemaName: String) {}
        override suspend fun runTenantSchemaMigrations(schemaName: String) {}
        override suspend fun dropTenantSchema(schemaName: String) {}
        override fun getCoreDependencies(): CoreDependencies? = null
    }

    @Test
    fun `getCoreDependencies returns null when not provided`() {
        val ctx = mockk<EnterpriseBackendContext>()
        every { ctx.getCoreDependencies() } returns null

        val deps = ctx.getCoreDependencies()

        assertNull(deps)
    }

    @Test
    fun `getCoreDependencies returns CoreDependencies with segment service and flag repo`() = runTest {
        val segmentService = mockk<CoreSegmentService>()
        val flagRepo = mockk<CoreFlagRepository>()
        val depsImpl = mockk<CoreDependencies>()
        every { depsImpl.getSegmentService() } returns segmentService
        every { depsImpl.getFlagRepository() } returns flagRepo
        every { depsImpl.getEvalCache() } returns null
        every { depsImpl.getSlackNotificationService() } returns null

        val segment = depsImpl.getSegmentService()
        val repo = depsImpl.getFlagRepository()

        assertEquals(segmentService, segment)
        assertEquals(flagRepo, repo)
        assertNull(depsImpl.getEvalCache())
        assertNull(depsImpl.getSlackNotificationService())
    }
}
