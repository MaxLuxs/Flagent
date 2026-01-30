package flagent.application

import flagent.api.FlagInfo
import flagent.domain.entity.Flag
import flagent.domain.repository.IFlagRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.Test

class CoreFlagRepositoryAdapterTest {

    @Test
    fun findByIdReturnsFlagInfoWhenFound() = runTest {
        val flagRepo = mockk<IFlagRepository>()
        val flag = Flag(id = 1, key = "test_flag", description = "", enabled = true)
        coEvery { flagRepo.findById(1) } returns flag
        val adapter = CoreFlagRepositoryAdapter(flagRepo)

        val result = adapter.findById(1)

        assertEquals(FlagInfo(id = 1, key = "test_flag", enabled = true), result)
        coVerify { flagRepo.findById(1) }
    }

    @Test
    fun findByIdReturnsNullWhenNotFound() = runTest {
        val flagRepo = mockk<IFlagRepository>()
        coEvery { flagRepo.findById(999) } returns null
        val adapter = CoreFlagRepositoryAdapter(flagRepo)

        val result = adapter.findById(999)

        assertNull(result)
    }

    @Test
    fun updateCallsRepositoryWhenFlagExists() = runTest {
        val flagRepo = mockk<IFlagRepository>()
        val existing = Flag(id = 2, key = "f", description = "", enabled = true)
        coEvery { flagRepo.findById(2) } returns existing
        coEvery { flagRepo.update(any()) } returns existing.copy(enabled = false)
        val adapter = CoreFlagRepositoryAdapter(flagRepo)
        val info = FlagInfo(id = 2, key = "f", enabled = false)

        adapter.update(info)

        coVerify { flagRepo.findById(2) }
        coVerify { flagRepo.update(match { it.id == 2 && !it.enabled }) }
    }

    @Test
    fun updateDoesNothingWhenFlagNotFound() = runTest {
        val flagRepo = mockk<IFlagRepository>()
        coEvery { flagRepo.findById(99) } returns null
        val adapter = CoreFlagRepositoryAdapter(flagRepo)

        adapter.update(FlagInfo(id = 99, key = "x", enabled = false))

        coVerify(exactly = 1) { flagRepo.findById(99) }
        coVerify(exactly = 0) { flagRepo.update(any()) }
    }
}
