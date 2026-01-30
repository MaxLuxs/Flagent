package flagent.service.base

import flagent.service.FlagSnapshotService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class BaseServiceTest {

    /**
     * Concrete subclass to test protected BaseService methods.
     */
    private class TestBaseService(flagSnapshotService: FlagSnapshotService? = null) :
        BaseService(flagSnapshotService) {

        fun exposeRequireNotNull(value: String?, paramName: String): String =
            requireNotNull(value, paramName)

        fun exposeRequirePositive(value: Int, paramName: String): Int =
            requirePositive(value, paramName)

        fun exposeRequireNonNegative(value: Int, paramName: String): Int =
            requireNonNegative(value, paramName)

        suspend fun exposeSaveSnapshot(flagId: Int, updatedBy: String) =
            saveSnapshot(flagId, updatedBy)
    }

    @Test
    fun requireNotNull_returnsValue_whenNotNull() {
        val service = TestBaseService()
        assertEquals("valid", service.exposeRequireNotNull("valid", "param"))
    }

    @Test
    fun requireNotNull_throws_whenNull() {
        val service = TestBaseService()
        assertFailsWith<IllegalArgumentException> {
            service.exposeRequireNotNull(null, "paramName")
        }.message?.let { msg ->
            assertEquals("paramName is required", msg)
        }
    }

    @Test
    fun requirePositive_returnsValue_whenPositive() {
        val service = TestBaseService()
        assertEquals(1, service.exposeRequirePositive(1, "param"))
        assertEquals(100, service.exposeRequirePositive(100, "param"))
    }

    @Test
    fun requirePositive_throws_whenZeroOrNegative() {
        val service = TestBaseService()
        assertFailsWith<IllegalArgumentException> {
            service.exposeRequirePositive(0, "paramName")
        }.message?.let { msg ->
            assertEquals("paramName must be positive", msg)
        }
        assertFailsWith<IllegalArgumentException> {
            service.exposeRequirePositive(-1, "paramName")
        }
    }

    @Test
    fun requireNonNegative_returnsValue_whenZeroOrPositive() {
        val service = TestBaseService()
        assertEquals(0, service.exposeRequireNonNegative(0, "param"))
        assertEquals(1, service.exposeRequireNonNegative(1, "param"))
    }

    @Test
    fun requireNonNegative_throws_whenNegative() {
        val service = TestBaseService()
        assertFailsWith<IllegalArgumentException> {
            service.exposeRequireNonNegative(-1, "paramName")
        }.message?.let { msg ->
            assertEquals("paramName must be non-negative", msg)
        }
    }

    @Test
    fun saveSnapshot_doesNothing_whenFlagSnapshotServiceIsNull() = runBlocking {
        val service = TestBaseService(flagSnapshotService = null)
        service.exposeSaveSnapshot(1, "user")
        // no throw
    }

    @Test
    fun saveSnapshot_callsService_whenFlagSnapshotServiceProvided() = runBlocking {
        val snapshotService = mockk<FlagSnapshotService>()
        coEvery { snapshotService.saveFlagSnapshot(any(), any()) } just runs
        val service = TestBaseService(flagSnapshotService = snapshotService)
        service.exposeSaveSnapshot(42, "test-user")
        coVerify { snapshotService.saveFlagSnapshot(42, "test-user") }
    }

    @Test
    fun saveSnapshot_doesNotThrow_whenServiceThrows() = runBlocking {
        val snapshotService = mockk<FlagSnapshotService>()
        coEvery { snapshotService.saveFlagSnapshot(any(), any()) } throws RuntimeException("db error")
        val service = TestBaseService(flagSnapshotService = snapshotService)
        service.exposeSaveSnapshot(1, "user")
        // BaseService catches and logs, does not rethrow
    }
}
