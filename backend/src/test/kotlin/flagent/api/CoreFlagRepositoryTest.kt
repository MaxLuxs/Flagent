package flagent.api

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

/**
 * Unit tests for CoreFlagRepository API contract and FlagInfo (shared API).
 * Verifies that backend adapters can delegate to this interface correctly.
 */
class CoreFlagRepositoryTest {

    @Test
    fun `FlagInfo holds id key and enabled`() {
        val info = FlagInfo(id = 1, key = "my_flag", enabled = true)
        assertEquals(1, info.id)
        assertEquals("my_flag", info.key)
        assertEquals(true, info.enabled)
    }

    @Test
    fun `FlagInfo copy works`() {
        val info = FlagInfo(id = 2, key = "x", enabled = false)
        val updated = info.copy(enabled = true)
        assertEquals(2, updated.id)
        assertEquals("x", updated.key)
        assertEquals(true, updated.enabled)
    }

    @Test
    fun `findById delegates to implementation and returns FlagInfo`() = runTest {
        val repo = mockk<CoreFlagRepository>()
        val expected = FlagInfo(id = 10, key = "test", enabled = true)
        coEvery { repo.findById(10) } returns expected

        val result = repo.findById(10)

        assertEquals(expected, result)
        coVerify(exactly = 1) { repo.findById(10) }
    }

    @Test
    fun `findById returns null when not found`() = runTest {
        val repo = mockk<CoreFlagRepository>()
        coEvery { repo.findById(999) } returns null

        val result = repo.findById(999)

        assertNull(result)
        coVerify(exactly = 1) { repo.findById(999) }
    }

    @Test
    fun `update delegates to implementation`() = runTest {
        val repo = mockk<CoreFlagRepository>()
        coEvery { repo.update(any()) } returns Unit
        val flag = FlagInfo(id = 1, key = "f", enabled = false)

        repo.update(flag)

        coVerify(exactly = 1) { repo.update(flag) }
    }
}
