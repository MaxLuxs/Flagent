package flagent.application

import flagent.api.CoreFlagRepository
import flagent.api.FlagInfo
import flagent.domain.repository.IFlagRepository

/**
 * Adapter from backend IFlagRepository to shared CoreFlagRepository for enterprise.
 */
class CoreFlagRepositoryAdapter(
    private val flagRepository: IFlagRepository
) : CoreFlagRepository {
    override suspend fun findById(id: Int): FlagInfo? {
        return flagRepository.findById(id)?.let {
            FlagInfo(id = it.id, key = it.key, enabled = it.enabled)
        }
    }

    override suspend fun update(flag: FlagInfo) {
        flagRepository.findById(flag.id)?.let { existing ->
            flagRepository.update(existing.copy(enabled = flag.enabled))
        }
    }
}
