package flagent.service.util

import flagent.service.FlagSnapshotService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlin.test.Test

class FlagSnapshotExtensionsTest {

    @Test
    fun `saveFlagSnapshotIfPresent on null receiver does not throw`() = runBlocking {
        val nullService: FlagSnapshotService? = null
        nullService.saveFlagSnapshotIfPresent(flagId = 1, updatedBy = "test")
    }

    @Test
    fun `saveFlagSnapshotIfPresent on non-null calls saveFlagSnapshot`() = runBlocking {
        val service = mockk<FlagSnapshotService>(relaxed = true)
        coEvery { service.saveFlagSnapshot(any(), any()) } returns Unit

        service.saveFlagSnapshotIfPresent(flagId = 42, updatedBy = "user")

        coVerify(exactly = 1) { service.saveFlagSnapshot(42, "user") }
    }

    @Test
    fun `saveFlagSnapshotIfPresent when saveFlagSnapshot throws does not propagate`() = runBlocking {
        val service = mockk<FlagSnapshotService>(relaxed = true)
        coEvery { service.saveFlagSnapshot(any(), any()) } throws RuntimeException("db error")

        service.saveFlagSnapshotIfPresent(flagId = 1, updatedBy = "user")
        // no throw
    }
}
