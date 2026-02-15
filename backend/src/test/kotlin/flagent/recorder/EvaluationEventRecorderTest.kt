package flagent.recorder

import flagent.repository.impl.EvaluationEventRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class EvaluationEventRecorderTest {

    private lateinit var repository: EvaluationEventRepository
    private lateinit var recorder: EvaluationEventRecorder

    @BeforeTest
    fun setup() {
        repository = mockk(relaxed = true)
        recorder = EvaluationEventRecorder(repository)
    }

    @AfterTest
    fun cleanup() {
        recorder.stop()
    }

    @Test
    fun record_queuesEvent() = runBlocking {
        recorder.record(1, 1_700_000_000_000L)
        recorder.record(2, 1_700_000_000_001L)

        delay(2500)

        coVerify(atLeast = 1) { repository.saveBatch(any()) }
    }

    @Test
    fun record_withClientId_passesClientIdToBatch_oss() = runBlocking {
        recorder.record(1, 1_700_000_000_000L, "e2e-mobile-client")
        delay(2500)
        coVerify(atLeast = 1) {
            repository.saveBatch(match { batch ->
                batch.any { it.clientId == "e2e-mobile-client" }
            })
        }
    }

    @Test
    fun stop_doesNotThrow() {
        recorder.record(1, System.currentTimeMillis())
        recorder.stop()
    }
}
