package flagent.application

import flagent.cache.impl.EvalCache
import flagent.config.AppConfig
import flagent.recorder.AnalyticsEventsCleanupJob
import flagent.recorder.DataRecordingService
import flagent.recorder.EvaluationEventsCleanupJob
import flagent.repository.impl.AnalyticsEventRepository
import flagent.repository.impl.ConstraintRepository
import flagent.repository.impl.CrashReportRepository
import flagent.repository.impl.DistributionRepository
import flagent.repository.impl.EvaluationEventRepository
import flagent.repository.impl.FlagEntityTypeRepository
import flagent.repository.impl.FlagRepository
import flagent.repository.impl.FlagSnapshotRepository
import flagent.repository.impl.SegmentRepository
import flagent.repository.impl.TagRepository
import flagent.repository.impl.UserRepository
import flagent.repository.impl.WebhookRepository
import flagent.route.RealtimeEventBus
import flagent.service.*
import flagent.service.adapter.SharedFlagEvaluatorAdapter
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ApplicationSetupTest {

    @AfterTest
    fun tearDown() {
        // Ensure AppConfig mocks from tests don't leak
        runCatching { unmockkObject(AppConfig) }
    }

    @Test
    fun `createRepositories creates all concrete repositories`() {
        val repos = createRepositories()

        assertTrue(repos.flagRepository is FlagRepository)
        assertTrue(repos.segmentRepository is SegmentRepository)
        assertTrue(repos.variantRepository is VariantRepository)
        assertTrue(repos.constraintRepository is ConstraintRepository)
        assertTrue(repos.distributionRepository is DistributionRepository)
        assertTrue(repos.tagRepository is TagRepository)
        assertTrue(repos.flagSnapshotRepository is FlagSnapshotRepository)
        assertTrue(repos.flagEntityTypeRepository is FlagEntityTypeRepository)
        assertTrue(repos.webhookRepository is WebhookRepository)
        assertTrue(repos.userRepository is UserRepository)
        assertTrue(repos.evaluationEventRepository is EvaluationEventRepository)
        assertTrue(repos.analyticsEventRepository is AnalyticsEventRepository)
        assertTrue(repos.crashReportRepository is CrashReportRepository)
    }

    @Test
    fun `createCacheAndSync uses EvalCache with repository in non-eval-only mode`() {
        mockkObject(AppConfig)
        every { AppConfig.evalOnlyMode } returns false
        every { AppConfig.dbDriver } returns "sqlite3"
        every { AppConfig.firebaseRcSyncEnabled } returns false
        every { AppConfig.firebaseRcProjectId } returns ""
        every { AppConfig.evalCacheRefreshTimeout } returns kotlin.time.Duration.parse("5s")
        every { AppConfig.evalCacheRefreshInterval } returns kotlin.time.Duration.parse("60s")

        val cacheAndSync = createCacheAndSync(FlagRepository())

        assertNotNull(cacheAndSync.evalCache)
        assertNull(cacheAndSync.firebaseRcSyncService)
    }

    @Test
    fun `createCacheAndSync uses fetcher when eval-only JSON driver`() {
        mockkObject(AppConfig)
        every { AppConfig.evalOnlyMode } returns true
        every { AppConfig.dbDriver } returns "json_file"
        every { AppConfig.firebaseRcSyncEnabled } returns false
        every { AppConfig.firebaseRcProjectId } returns ""
        every { AppConfig.evalCacheRefreshTimeout } returns kotlin.time.Duration.parse("5s")
        every { AppConfig.evalCacheRefreshInterval } returns kotlin.time.Duration.parse("60s")

        val cacheAndSync = createCacheAndSync(FlagRepository())

        assertNotNull(cacheAndSync.evalCache)
        assertNull(cacheAndSync.firebaseRcSyncService)
    }

    @Test
    fun `createRecordingAndMetrics returns only DataRecordingService in eval-only mode`() {
        mockkObject(AppConfig)
        every { AppConfig.evalOnlyMode } returns true
        every { AppConfig.firebaseAnalyticsEnabled } returns false
        every { AppConfig.firebaseAnalyticsApiSecret } returns ""
        every { AppConfig.firebaseAnalyticsMeasurementId } returns ""

        val rec = createRecordingAndMetrics(
            evaluationEventRepository = mockk(relaxed = true),
            analyticsEventRepository = mockk(relaxed = true)
        )

        assertNotNull(rec.dataRecordingService)
        assertNull(rec.firebaseAnalyticsReporter)
        assertNull(rec.evaluationEventRecorder)
        assertNull(rec.coreMetricsService)
        assertNull(rec.evaluationEventsCleanupJob)
        assertNull(rec.analyticsEventsCleanupJob)
    }

    @Test
    fun `createRecordingAndMetrics wires all services when eval-only disabled and firebase enabled`() {
        mockkObject(AppConfig)
        every { AppConfig.evalOnlyMode } returns false
        every { AppConfig.firebaseAnalyticsEnabled } returns true
        every { AppConfig.firebaseAnalyticsApiSecret } returns "secret"
        every { AppConfig.firebaseAnalyticsMeasurementId } returns "G-123"

        val evaluationEventsRepo = mockk<EvaluationEventRepository>(relaxed = true)
        val analyticsEventsRepo = mockk<AnalyticsEventRepository>(relaxed = true)

        val rec = createRecordingAndMetrics(evaluationEventsRepo, analyticsEventsRepo)

        assertNotNull(rec.dataRecordingService)
        assertNotNull(rec.firebaseAnalyticsReporter)
        assertNotNull(rec.evaluationEventRecorder)
        assertNotNull(rec.coreMetricsService)
        assertNotNull(rec.evaluationEventsCleanupJob)
        assertNotNull(rec.analyticsEventsCleanupJob)
    }

    @Test
    fun `createServices builds all core services using provided dependencies`() {
        val repos = AppRepositories(
            flagRepository = mockk(relaxed = true),
            segmentRepository = mockk(relaxed = true),
            variantRepository = mockk(relaxed = true),
            constraintRepository = mockk(relaxed = true),
            distributionRepository = mockk(relaxed = true),
            tagRepository = mockk(relaxed = true),
            flagSnapshotRepository = mockk(relaxed = true),
            flagEntityTypeRepository = mockk(relaxed = true),
            webhookRepository = mockk(relaxed = true),
            userRepository = mockk(relaxed = true),
            evaluationEventRepository = mockk(relaxed = true),
            analyticsEventRepository = mockk(relaxed = true),
            crashReportRepository = mockk(relaxed = true)
        )

        val evalCache = mockk<EvalCache>(relaxed = true)
        val cacheAndSync = CacheAndSync(evalCache = evalCache, firebaseRcSyncService = null)

        val recording = RecordingAndMetrics(
            dataRecordingService = mockk<DataRecordingService>(relaxed = true),
            firebaseAnalyticsReporter = null,
            evaluationEventRecorder = mockk(relaxed = true),
            coreMetricsService = mockk(relaxed = true),
            evaluationEventsCleanupJob = mockk<EvaluationEventsCleanupJob>(relaxed = true),
            analyticsEventsCleanupJob = mockk<AnalyticsEventsCleanupJob>(relaxed = true)
        )

        val eventBus = RealtimeEventBus()

        val services = createServices(repos, cacheAndSync, recording, eventBus)

        assertNotNull(services.evaluationService)
        assertNotNull(services.flagService)
        assertNotNull(services.segmentService)
        assertNotNull(services.constraintService)
        assertNotNull(services.distributionService)
        assertNotNull(services.variantService)
        assertNotNull(services.tagService)
        assertNotNull(services.flagSnapshotService)
        assertNotNull(services.flagEntityTypeService)
        assertNotNull(services.webhookService)
        assertNotNull(services.userService)
        assertNotNull(services.exportService)
        assertNotNull(services.importService)
        assertNotNull(services.analyticsEventsService)
        assertNotNull(services.crashReportService)
        // coreMetricsService is optional but should be wired from RecordingAndMetrics
        assertEquals(recording.coreMetricsService, services.coreMetricsService)
    }
}

