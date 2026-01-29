package flagent.application

import flagent.api.CoreDependencies
import flagent.cache.impl.EvalCache
import flagent.domain.repository.IFlagRepository
import flagent.service.SegmentService
import flagent.service.SlackNotificationService

/**
 * Backend implementation of CoreDependencies for enterprise module (SmartRollout, AnomalyDetection).
 */
class CoreDependenciesImpl(
    private val segmentService: SegmentService,
    private val flagRepository: IFlagRepository,
    private val evalCache: EvalCache,
    private val slackNotificationService: SlackNotificationService?
) : CoreDependencies {
    override fun getSegmentService(): Any? = segmentService
    override fun getFlagRepository(): Any? = flagRepository
    override fun getEvalCache(): Any? = evalCache
    override fun getSlackNotificationService(): Any? = slackNotificationService
}
