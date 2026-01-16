package flagent.repository.tables

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.ReferenceOption

/**
 * Flags table
 */
object Flags : IntIdTable("flags") {
    val key = varchar("key", 64).uniqueIndex("idx_flag_key")
    val description = text("description")
    val createdBy = varchar("created_by", 255).nullable()
    val updatedBy = varchar("updated_by", 255).nullable()
    val enabled = bool("enabled").default(false)
    val snapshotId = integer("snapshot_id").default(0)
    val notes = text("notes").nullable()
    val dataRecordsEnabled = bool("data_records_enabled").default(false)
    val entityType = varchar("entity_type", 255).nullable()
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at").nullable()
    val deletedAt = timestamp("deleted_at").nullable()
}

/**
 * Segments table
 */
object Segments : IntIdTable("segments") {
    val flagId = integer("flag_id").references(Flags.id, onDelete = ReferenceOption.CASCADE).index("idx_segment_flagid")
    val description = text("description").nullable()
    val rank = integer("rank").default(999)
    val rolloutPercent = integer("rollout_percent").default(0)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at").nullable()
    val deletedAt = timestamp("deleted_at").nullable()
}

/**
 * Variants table
 */
object Variants : IntIdTable("variants") {
    val flagId = integer("flag_id").references(Flags.id, onDelete = ReferenceOption.CASCADE).index("idx_variant_flagid")
    val key = varchar("key", 255).nullable()
    val attachment = text("attachment").nullable() // JSON stored as text
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at").nullable()
    val deletedAt = timestamp("deleted_at").nullable()
}

/**
 * Constraints table
 */
object Constraints : IntIdTable("constraints") {
    val segmentId = integer("segment_id").references(Segments.id, onDelete = ReferenceOption.CASCADE).index("idx_constraint_segmentid")
    val property = varchar("property", 255)
    val operator = varchar("operator", 50)
    val value = text("value")
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at").nullable()
    val deletedAt = timestamp("deleted_at").nullable()
}

/**
 * Distributions table
 */
object Distributions : IntIdTable("distributions") {
    val segmentId = integer("segment_id").references(Segments.id, onDelete = ReferenceOption.CASCADE).index("idx_distribution_segmentid")
    val variantId = integer("variant_id").references(Variants.id, onDelete = ReferenceOption.CASCADE).index("idx_distribution_variantid")
    val variantKey = varchar("variant_key", 255).nullable()
    val percent = integer("percent").default(0) // 0-100
    val bitmap = text("bitmap").nullable()
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at").nullable()
    val deletedAt = timestamp("deleted_at").nullable()
}

/**
 * Tags table
 */
object Tags : IntIdTable("tags") {
    val value = varchar("value", 64).uniqueIndex("idx_tag_value")
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at").nullable()
    val deletedAt = timestamp("deleted_at").nullable()
}

/**
 * FlagsTags junction table for many-to-many relationship
 * Maps to flags_tags table from GORM many2many
 */
object FlagsTags : IntIdTable("flags_tags") {
    val flagId = integer("flag_id").references(Flags.id, onDelete = ReferenceOption.CASCADE)
    val tagId = integer("tag_id").references(Tags.id, onDelete = ReferenceOption.CASCADE)
    
    init {
        uniqueIndex("flags_tags_unique", flagId, tagId)
    }
}

/**
 * FlagSnapshots table
 */
object FlagSnapshots : IntIdTable("flag_snapshots") {
    val flagId = integer("flag_id").references(Flags.id, onDelete = ReferenceOption.CASCADE).index("idx_flagsnapshot_flagid")
    val updatedBy = varchar("updated_by", 255).nullable()
    val flag = text("flag") // JSON stored as text
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at").nullable()
    val deletedAt = timestamp("deleted_at").nullable()
}

/**
 * FlagEntityTypes table
 */
object FlagEntityTypes : IntIdTable("flag_entity_types") {
    val key = varchar("key", 64).uniqueIndex("flag_entity_type_key")
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at").nullable()
    val deletedAt = timestamp("deleted_at").nullable()
}

/**
 * Users table
 */
object Users : IntIdTable("users") {
    val email = text("email").nullable()
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at").nullable()
    val deletedAt = timestamp("deleted_at").nullable()
}
