package flagent.service

import flagent.domain.entity.CrashReport
import flagent.repository.impl.CrashReportRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class CrashReportServiceTest {

    @Test
    fun save_returnsSaved() = runBlocking {
        val repo = mockk<CrashReportRepository>(relaxed = true)
        val crash = CrashReport(stackTrace = "at foo", message = "NPE", platform = "android", timestamp = 1L)
        val saved = crash.copy(id = 1L)
        coEvery { repo.save(any()) } returns saved
        val service = CrashReportService(repo)
        assertEquals(1L, service.save(crash).id)
    }

    @Test
    fun saveBatch_returnsList() = runBlocking {
        val repo = mockk<CrashReportRepository>(relaxed = true)
        val crashes = listOf(
            CrashReport(stackTrace = "a", message = "m", platform = "p", timestamp = 1L)
        )
        val saved = crashes.mapIndexed { i, c -> c.copy(id = i.toLong() + 1) }
        coEvery { repo.saveBatch(any()) } returns saved
        val service = CrashReportService(repo)
        assertEquals(1, service.saveBatch(crashes).size)
    }

    @Test
    fun list_returnsFromRepo() = runBlocking {
        val repo = mockk<CrashReportRepository>(relaxed = true)
        val list = listOf(
            CrashReport(id = 1L, stackTrace = "s", message = "m", platform = "p", timestamp = 1L)
        )
        coEvery { repo.list(null, null, null, 50, 0) } returns list
        val service = CrashReportService(repo)
        assertEquals(list, service.list(null, null, null, 50, 0))
    }

    @Test
    fun count_returnsFromRepo() = runBlocking {
        val repo = mockk<CrashReportRepository>(relaxed = true)
        coEvery { repo.count(null, null, null) } returns 10L
        val service = CrashReportService(repo)
        assertEquals(10L, service.count(null, null, null))
    }
}
