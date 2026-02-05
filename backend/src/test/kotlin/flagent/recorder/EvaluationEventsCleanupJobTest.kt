package flagent.recorder

import flagent.repository.impl.EvaluationEventRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Duration.Companion.milliseconds

class EvaluationEventsCleanupJobTest {

    private lateinit var repository: EvaluationEventRepository
    private lateinit var job: EvaluationEventsCleanupJob

    @BeforeTest
    fun setup() {
        repository = mockk(relaxed = true)
        coEvery { repository.deleteOlderThan(any()) } returns 0
        job = EvaluationEventsCleanupJob(
            repository = repository,
            retentionDays = 90,
            interval = 100.milliseconds,
            enabled = true
        )
    }

    @AfterTest
    fun cleanup() {
        job.stop()
    }

    @Test
    fun start_invokesDeleteOlderThan() = runBlocking {
        job.start()

        delay(150)

        coVerify(atLeast = 1) { repository.deleteOlderThan(any()) }
    }

    @Test
    fun start_whenDisabled_doesNotInvokeDeleteOlderThan() = runBlocking {
        val disabledJob = EvaluationEventsCleanupJob(repository, enabled = false)
        disabledJob.start()
        delay(50)
        disabledJob.stop()

        coVerify(exactly = 0) { repository.deleteOlderThan(any()) }
    }

    @Test
    fun stop_doesNotThrow() {
        job.start()
        job.stop()
    }
}
