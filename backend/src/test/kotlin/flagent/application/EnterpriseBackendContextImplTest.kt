package flagent.application

import flagent.api.CoreDependencies
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import flagent.test.PostgresTestcontainerExtension
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.*

@ExtendWith(PostgresTestcontainerExtension::class)
class EnterpriseBackendContextImplTest {

    @Test
    fun runMigrations_invokesBlock() {
        var invoked = false
        val ctx = EnterpriseBackendContextImpl(null)
        ctx.runMigrations { invoked = true }
        assertTrue(invoked)
    }

    @Test
    fun runTransaction_returnsBlockResult() {
        val ctx = EnterpriseBackendContextImpl(null)
        val result = ctx.runTransaction { 42 }
        assertEquals(42, result)
    }

    @Test
    fun runTransactionSuspend_returnsBlockResult() = runTest {
        val ctx = EnterpriseBackendContextImpl(null)
        val result = ctx.runTransactionSuspend { "ok" }
        assertEquals("ok", result)
    }

    @Test
    fun createTenantSchema_doesNotThrow() = runBlocking {
        val ctx = EnterpriseBackendContextImpl(null)
        ctx.createTenantSchema("test_schema_${System.currentTimeMillis()}")
    }

    @Test
    fun runTenantSchemaMigrations_doesNotThrow() = runBlocking {
        val ctx = EnterpriseBackendContextImpl(null)
        ctx.runTenantSchemaMigrations("public")
    }

    @Test
    fun dropTenantSchema_doesNotThrow() = runBlocking {
        val ctx = EnterpriseBackendContextImpl(null)
        ctx.dropTenantSchema("test_drop_${System.currentTimeMillis()}")
    }

    @Test
    fun getCoreDependencies_returnsNull_whenNotProvided() {
        val ctx = EnterpriseBackendContextImpl(null)
        assertNull(ctx.getCoreDependencies())
    }

    @Test
    fun getCoreDependencies_returnsProvided() {
        val deps = mockk<CoreDependencies>(relaxed = true)
        val ctx = EnterpriseBackendContextImpl(deps)
        assertEquals(deps, ctx.getCoreDependencies())
    }
}
