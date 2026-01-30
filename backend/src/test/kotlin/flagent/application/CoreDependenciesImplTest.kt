package flagent.application

import flagent.cache.impl.EvalCache
import flagent.domain.repository.IFlagRepository
import flagent.service.SegmentService
import io.mockk.mockk
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.Test

class CoreDependenciesImplTest {

    @Test
    fun getSegmentServiceReturnsAdapter() {
        val deps = CoreDependenciesImpl(
            segmentService = mockk(relaxed = true),
            flagRepository = mockk(relaxed = true),
            evalCache = mockk(relaxed = true)
        )
        val segmentService = deps.getSegmentService()
        assertNotNull(segmentService)
    }

    @Test
    fun getFlagRepositoryReturnsAdapter() {
        val deps = CoreDependenciesImpl(
            segmentService = mockk(relaxed = true),
            flagRepository = mockk(relaxed = true),
            evalCache = mockk(relaxed = true)
        )
        val flagRepo = deps.getFlagRepository()
        assertNotNull(flagRepo)
    }

    @Test
    fun getEvalCacheReturnsProvidedCache() {
        val evalCache = mockk<EvalCache>(relaxed = true)
        val deps = CoreDependenciesImpl(
            segmentService = mockk(relaxed = true),
            flagRepository = mockk(relaxed = true),
            evalCache = evalCache
        )
        assertNotNull(deps.getEvalCache())
    }

    @Test
    fun getSlackNotificationServiceReturnsNull() {
        val deps = CoreDependenciesImpl(
            segmentService = mockk(relaxed = true),
            flagRepository = mockk(relaxed = true),
            evalCache = mockk(relaxed = true)
        )
        assertNull(deps.getSlackNotificationService())
    }
}
