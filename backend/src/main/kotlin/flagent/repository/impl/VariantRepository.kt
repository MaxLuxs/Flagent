package flagent.repository.impl
import org.jetbrains.exposed.v1.jdbc.*

import flagent.domain.entity.Variant
import flagent.domain.repository.IVariantRepository
import flagent.repository.Database
import flagent.repository.tables.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.core.*

class VariantRepository : IVariantRepository {
    
    private val json = Json { ignoreUnknownKeys = true }
    
    override suspend fun findByFlagId(flagId: Int): List<Variant> = withContext(Dispatchers.IO) {
        Database.transaction {
            Variants.selectAll()
                .where { (Variants.flagId eq flagId) and (Variants.deletedAt.isNull()) }
                .orderBy(Variants.id, SortOrder.ASC)
                .map { mapRowToVariant(it) }
        }
    }
    
    override suspend fun findById(id: Int): Variant? = withContext(Dispatchers.IO) {
        Database.transaction {
            Variants.selectAll()
                .where { (Variants.id eq id) and (Variants.deletedAt.isNull()) }
                .firstOrNull()
                ?.let { mapRowToVariant(it) }
        }
    }
    
    override suspend fun create(variant: Variant): Variant = withContext(Dispatchers.IO) {
        Database.transaction {
            val attachmentJson = variant.attachment?.let { json.encodeToString(kotlinx.serialization.json.JsonObject.serializer(), it) }
            
            val id = Variants.insert {
                it[flagId] = variant.flagId
                it[key] = variant.key
                it[attachment] = attachmentJson
                it[createdAt] = java.time.LocalDateTime.now()
            }[Variants.id].value
            
            variant.copy(id = id)
        }
    }
    
    override suspend fun update(variant: Variant): Variant = withContext(Dispatchers.IO) {
        Database.transaction {
            val attachmentJson = variant.attachment?.let { json.encodeToString(kotlinx.serialization.json.JsonObject.serializer(), it) }
            
            Variants.update({ Variants.id eq variant.id }) {
                it[key] = variant.key
                it[attachment] = attachmentJson
                it[updatedAt] = java.time.LocalDateTime.now()
            }
            variant
        }
    }
    
    override suspend fun delete(id: Int): Unit = withContext(Dispatchers.IO) {
        Database.transaction {
            Variants.update({ Variants.id eq id }) {
                it[deletedAt] = java.time.LocalDateTime.now()
            }
        }
        Unit
    }
    
    private fun mapRowToVariant(row: ResultRow): Variant {
        val attachmentJson = row[Variants.attachment]
        val attachment = attachmentJson?.let {
            try {
                json.parseToJsonElement(it) as? kotlinx.serialization.json.JsonObject
            } catch (e: Exception) {
                null
            }
        }
        
        return Variant(
            id = row[Variants.id].value,
            flagId = row[Variants.flagId],
            key = row[Variants.key] ?: "",
            attachment = attachment
        )
    }
}
