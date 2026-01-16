package flagent.domain.repository

import flagent.domain.entity.Tag

/**
 * Tag repository interface
 * Domain layer - no framework dependencies
 */
interface ITagRepository {
    suspend fun findAll(limit: Int? = null, offset: Int = 0, valueLike: String? = null): List<Tag>
    suspend fun findByValue(value: String): Tag?
    suspend fun create(tag: Tag): Tag
    suspend fun delete(id: Int)
    suspend fun addTagToFlag(flagId: Int, tagId: Int)
    suspend fun removeTagFromFlag(flagId: Int, tagId: Int)
    suspend fun findByFlagId(flagId: Int): List<Tag>
}
