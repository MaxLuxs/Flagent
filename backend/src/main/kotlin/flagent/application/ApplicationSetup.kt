package flagent.application

import flagent.api.EnterpriseBackendContext
import flagent.api.EnterpriseConfigurator
import flagent.cache.impl.EvalCache
import flagent.cache.impl.createEvalCacheFetcher
import flagent.config.AppConfig
import flagent.integration.firebase.FirebaseAnalyticsReporter
import flagent.integration.firebase.FirebaseRCSyncService
import flagent.recorder.AnalyticsEventsCleanupJob
import flagent.recorder.DataRecordingService
import flagent.recorder.EvaluationEventRecorder
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
import flagent.repository.impl.VariantRepository
import flagent.repository.impl.UserRepository
import flagent.repository.impl.WebhookRepository
import flagent.service.AnalyticsEventsService
import flagent.service.UserService
import flagent.service.ConstraintService
import flagent.service.CrashReportService
import flagent.service.CoreMetricsService
import flagent.service.DistributionService
import flagent.service.EvaluationService
import flagent.service.ExportService
import flagent.service.FlagEntityTypeService
import flagent.service.FlagService
import flagent.service.FlagSnapshotService
import flagent.service.ImportService
import flagent.service.SegmentService
import flagent.service.TagService
import flagent.service.VariantService
import flagent.service.WebhookService
import flagent.service.adapter.SharedFlagEvaluatorAdapter
import flagent.domain.usecase.EvaluateFlagUseCase
import flagent.route.RealtimeEventBus

data class AppRepositories(
    val flagRepository: FlagRepository,
    val segmentRepository: SegmentRepository,
    val variantRepository: VariantRepository,
    val constraintRepository: ConstraintRepository,
    val distributionRepository: DistributionRepository,
    val tagRepository: TagRepository,
    val flagSnapshotRepository: FlagSnapshotRepository,
    val flagEntityTypeRepository: FlagEntityTypeRepository,
    val webhookRepository: WebhookRepository,
    val userRepository: UserRepository,
    val evaluationEventRepository: EvaluationEventRepository,
    val analyticsEventRepository: AnalyticsEventRepository,
    val crashReportRepository: CrashReportRepository
)

data class CacheAndSync(
    val evalCache: EvalCache,
    val firebaseRcSyncService: FirebaseRCSyncService?
)

data class RecordingAndMetrics(
    val dataRecordingService: DataRecordingService?,
    val firebaseAnalyticsReporter: FirebaseAnalyticsReporter?,
    val evaluationEventRecorder: EvaluationEventRecorder?,
    val coreMetricsService: CoreMetricsService?,
    val evaluationEventsCleanupJob: EvaluationEventsCleanupJob?,
    val analyticsEventsCleanupJob: AnalyticsEventsCleanupJob?
)

data class AppServices(
    val evaluationService: EvaluationService,
    val flagService: FlagService,
    val segmentService: SegmentService,
    val constraintService: ConstraintService,
    val distributionService: DistributionService,
    val variantService: VariantService,
    val tagService: TagService,
    val flagSnapshotService: FlagSnapshotService,
    val flagEntityTypeService: FlagEntityTypeService,
    val webhookService: WebhookService,
    val userService: UserService,
    val exportService: ExportService,
    val importService: ImportService,
    val analyticsEventsService: AnalyticsEventsService,
    val crashReportService: CrashReportService,
    val coreMetricsService: CoreMetricsService?
)

fun createRepositories(): AppRepositories = AppRepositories(
    flagRepository = FlagRepository(),
    segmentRepository = SegmentRepository(),
    variantRepository = VariantRepository(),
    constraintRepository = ConstraintRepository(),
    distributionRepository = DistributionRepository(),
    tagRepository = TagRepository(),
    flagSnapshotRepository = FlagSnapshotRepository(),
    flagEntityTypeRepository = FlagEntityTypeRepository(),
    webhookRepository = WebhookRepository(),
    userRepository = UserRepository(),
    evaluationEventRepository = EvaluationEventRepository(),
    analyticsEventRepository = AnalyticsEventRepository(),
    crashReportRepository = CrashReportRepository()
)

fun createCacheAndSync(flagRepository: FlagRepository): CacheAndSync {
    val evalCache = if (AppConfig.evalOnlyMode && AppConfig.dbDriver in listOf("json_file", "json_http")) {
        EvalCache(fetcher = createEvalCacheFetcher(flagRepository))
    } else {
        EvalCache(flagRepository = flagRepository)
    }
    evalCache.start()

    val firebaseRcSyncService = if (AppConfig.firebaseRcSyncEnabled && AppConfig.firebaseRcProjectId.isNotBlank()) {
        try {
            FirebaseRCSyncService(evalCache).also { it.start() }
        } catch (e: Exception) {
            mu.KotlinLogging.logger {}.error(e) { "Failed to start Firebase RC sync, continuing without it" }
            null
        }
    } else null

    return CacheAndSync(evalCache, firebaseRcSyncService)
}

fun createRecordingAndMetrics(
    evaluationEventRepository: EvaluationEventRepository,
    analyticsEventRepository: AnalyticsEventRepository
): RecordingAndMetrics {
    val dataRecordingService = try {
        DataRecordingService()
    } catch (e: Exception) {
        mu.KotlinLogging.logger {}.error(e) { "Failed to initialize DataRecordingService, continuing without recording" }
        null
    }

    val firebaseAnalyticsReporter = if (
        AppConfig.firebaseAnalyticsEnabled &&
        AppConfig.firebaseAnalyticsApiSecret.isNotBlank() &&
        AppConfig.firebaseAnalyticsMeasurementId.isNotBlank()
    ) {
        FirebaseAnalyticsReporter()
    } else null

    val evaluationEventRecorder = if (!AppConfig.evalOnlyMode) {
        EvaluationEventRecorder(evaluationEventRepository)
    } else null
    val coreMetricsService = if (!AppConfig.evalOnlyMode) CoreMetricsService(evaluationEventRepository) else null
    val evaluationEventsCleanupJob = if (!AppConfig.evalOnlyMode) {
        EvaluationEventsCleanupJob(evaluationEventRepository).also { it.start() }
    } else null
    val analyticsEventsCleanupJob = if (!AppConfig.evalOnlyMode) {
        AnalyticsEventsCleanupJob(analyticsEventRepository).also { it.start() }
    } else null

    return RecordingAndMetrics(
        dataRecordingService,
        firebaseAnalyticsReporter,
        evaluationEventRecorder,
        coreMetricsService,
        evaluationEventsCleanupJob,
        analyticsEventsCleanupJob
    )
}

fun createServices(
    repos: AppRepositories,
    cacheAndSync: CacheAndSync,
    recordingAndMetrics: RecordingAndMetrics,
    eventBus: RealtimeEventBus
): AppServices {
    val sharedFlagEvaluatorAdapter = SharedFlagEvaluatorAdapter()
    val evaluateFlagUseCase = EvaluateFlagUseCase(sharedFlagEvaluatorAdapter, repos.flagRepository)
    val evaluationService = EvaluationService(
        cacheAndSync.evalCache,
        evaluateFlagUseCase,
        recordingAndMetrics.dataRecordingService,
        recordingAndMetrics.firebaseAnalyticsReporter,
        recordingAndMetrics.evaluationEventRecorder
    )
    val flagSnapshotService = FlagSnapshotService(repos.flagSnapshotRepository, repos.flagRepository)
    val flagEntityTypeService = FlagEntityTypeService(repos.flagEntityTypeRepository)
    val segmentService = SegmentService(
        repos.segmentRepository,
        flagSnapshotService,
        repos.flagRepository,
        eventBus
    )
    val constraintService = ConstraintService(
        repos.constraintRepository,
        repos.segmentRepository,
        flagSnapshotService
    )
    val distributionService = DistributionService(
        repos.distributionRepository,
        repos.flagRepository,
        flagSnapshotService
    )
    val variantService = VariantService(
        repos.variantRepository,
        repos.flagRepository,
        repos.distributionRepository,
        flagSnapshotService,
        eventBus
    )
    val webhookService = WebhookService(repos.webhookRepository, repos.flagRepository)
    val userService = UserService(repos.userRepository)
    val flagService = FlagService(
        repos.flagRepository,
        flagSnapshotService,
        segmentService,
        variantService,
        distributionService,
        flagEntityTypeService,
        eventBus,
        webhookService
    )
    val tagService = TagService(repos.tagRepository, repos.flagRepository, flagSnapshotService)
    val exportService = ExportService(
        repos.flagRepository,
        repos.flagSnapshotRepository,
        repos.flagEntityTypeRepository
    )
    val importService = ImportService(
        flagService,
        segmentService,
        variantService,
        distributionService,
        constraintService,
        repos.flagRepository
    )
    val analyticsEventsService = AnalyticsEventsService(repos.analyticsEventRepository)
    val crashReportService = CrashReportService(repos.crashReportRepository)

    return AppServices(
        evaluationService,
        flagService,
        segmentService,
        constraintService,
        distributionService,
        variantService,
        tagService,
        flagSnapshotService,
        flagEntityTypeService,
        webhookService,
        userService,
        exportService,
        importService,
        analyticsEventsService,
        crashReportService,
        recordingAndMetrics.coreMetricsService
    )
}
