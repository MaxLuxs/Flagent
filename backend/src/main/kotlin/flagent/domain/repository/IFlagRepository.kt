package flagent.domain.repository

import flagent.domain.entity.Flag

/**
 * Flag repository interface
 * Domain layer - no framework dependencies
 */
interface IFlagRepository {
    suspend fun findById(id: Int): Flag?
    suspend fun findByKey(key: String): Flag?
    suspend fun findAll(
        limit: Int? = null,
        offset: Int = 0,
        enabled: Boolean? = null,
        description: String? = null,
        key: String? = null,
        descriptionLike: String? = null,
        preload: Boolean = false,
        deleted: Boolean = false,
        tags: String? = null
    ): List<Flag>
    suspend fun countAll(
        enabled: Boolean? = null,
        description: String? = null,
        key: String? = null,
        descriptionLike: String? = null,
        deleted: Boolean = false,
        tags: String? = null
    ): Long
    suspend fun findByTags(tags: List<String>): List<Flag>
    suspend fun create(flag: Flag): Flag
    suspend fun update(flag: Flag): Flag
    suspend fun delete(id: Int)
    suspend fun restore(id: Int): Flag?
}
