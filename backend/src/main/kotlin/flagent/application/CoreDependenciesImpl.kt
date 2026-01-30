package flagent.application

import flagent.api.CoreDependencies
import flagent.cache.impl.EvalCache
import flagent.domain.repository.IFlagRepository
import flagent.service.SegmentService

/**
 * Backend implementation of CoreDependencies for enterprise module (SmartRollout, AnomalyDetection).
 * Slack is not in core; enterprise creates its own SlackNotificationService when needed.
 */
class CoreDependenciesImpl(
    private val segmentService: SegmentService,
    private val flagRepository: IFlagRepository,
    private val evalCache: EvalCache
) : CoreDependencies {
    override fun getSegmentService() = CoreSegmentServiceAdapter(segmentService)
    override fun getFlagRepository() = CoreFlagRepositoryAdapter(flagRepository)
    override fun getEvalCache(): Any? = evalCache
    override fun getSlackNotificationService(): Any? = null
}
