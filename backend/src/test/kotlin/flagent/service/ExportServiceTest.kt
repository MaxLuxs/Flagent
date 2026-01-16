package flagent.service

import flagent.domain.entity.*
import flagent.domain.repository.IFlagEntityTypeRepository
import flagent.domain.repository.IFlagRepository
import flagent.domain.repository.IFlagSnapshotRepository
import flagent.repository.Database as FlagentDatabase
import flagent.repository.impl.*
import flagent.repository.tables.*
import io.mockk.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.sqlite.SQLiteDataSource
import java.io.File
import java.nio.file.Files
import kotlin.test.*

/**
 * Integration tests for ExportService
 * Tests SQLite export functionality including:
 * - Export of all data (flags, segments, variants, constraints, distributions, tags)
 * - Preservation of relationships between entities
 * - exclude_snapshots parameter
 * - SQLite file format correctness
 */
class ExportServiceTest {
    private lateinit var flagRepository: IFlagRepository
    private lateinit var flagSnapshotRepository: IFlagSnapshotRepository
    private lateinit var flagEntityTypeRepository: IFlagEntityTypeRepository
    private lateinit var exportService: ExportService
    
    @BeforeTest
    fun setup() {
        // Initialize in-memory database for testing
        FlagentDatabase.init()
        
        // Create repositories
        flagRepository = FlagRepository()
        flagSnapshotRepository = FlagSnapshotRepository()
        flagEntityTypeRepository = FlagEntityTypeRepository()
        
        // Create export service
        exportService = ExportService(flagRepository, flagSnapshotRepository, flagEntityTypeRepository)
    }
    
    @AfterTest
    fun cleanup() {
        FlagentDatabase.close()
    }
    
    @Test
    fun testExportSQLite_ExportsAllFlags() = runBlocking {
        // Arrange: Create test data
        val flag = createTestFlag()
        val createdFlag = flagRepository.create(flag)
        
        // Act: Export to SQLite
        val sqliteBytes = exportService.exportSQLite(excludeSnapshots = false)
        
        // Assert: Verify SQLite file is created and contains data
        assertTrue(sqliteBytes.isNotEmpty(), "SQLite export should return non-empty bytes")
        
        // Verify by reading the SQLite file
        val tempFile = File.createTempFile("test_export_", ".sqlite")
        try {
            tempFile.writeBytes(sqliteBytes)
            
            val db = org.jetbrains.exposed.sql.Database.connect(
                url = "jdbc:sqlite:${tempFile.absolutePath}",
                driver = "org.sqlite.JDBC"
            )
            
            transaction(db) {
                val flagCount = Flags.selectAll().count()
                assertEquals(1, flagCount, "Should export 1 flag")
                
                val exportedFlag = Flags.select { Flags.id eq createdFlag.id }.firstOrNull()
                assertNotNull(exportedFlag, "Exported flag should exist")
                assertEquals(createdFlag.key, exportedFlag[Flags.key], "Flag key should match")
                assertEquals(createdFlag.description, exportedFlag[Flags.description], "Flag description should match")
            }
        } finally {
            tempFile.delete()
        }
    }
    
    @Test
    fun testExportSQLite_ExportsAllRelatedData() = runBlocking {
        // Arrange: Create test data with all relationships
        val flag = createTestFlagWithRelations()
        val createdFlag = flagRepository.create(flag)
        
        // Act: Export to SQLite
        val sqliteBytes = exportService.exportSQLite(excludeSnapshots = false)
        
        // Assert: Verify all related data is exported
        val tempFile = File.createTempFile("test_export_", ".sqlite")
        try {
            tempFile.writeBytes(sqliteBytes)
            
            val db = org.jetbrains.exposed.sql.Database.connect(
                url = "jdbc:sqlite:${tempFile.absolutePath}",
                driver = "org.sqlite.JDBC"
            )
            
            transaction(db) {
                // Check flags
                val flagCount = Flags.selectAll().count()
                assertEquals(1, flagCount, "Should export 1 flag")
                
                // Check variants
                val variantCount = Variants.selectAll().count()
                assertEquals(2, variantCount, "Should export 2 variants")
                
                // Check segments
                val segmentCount = Segments.selectAll().count()
                assertEquals(1, segmentCount, "Should export 1 segment")
                
                // Check constraints
                val constraintCount = Constraints.selectAll().count()
                assertEquals(1, constraintCount, "Should export 1 constraint")
                
                // Check distributions
                val distributionCount = Distributions.selectAll().count()
                assertEquals(2, distributionCount, "Should export 2 distributions")
                
                // Check tags
                val tagCount = Tags.selectAll().count()
                assertTrue(tagCount >= 1, "Should export at least 1 tag")
                
                // Check flags_tags junction table
                val flagsTagsCount = FlagsTags.selectAll().count()
                assertTrue(flagsTagsCount >= 1, "Should export at least 1 flag-tag relationship")
            }
        } finally {
            tempFile.delete()
        }
    }
    
    @Test
    fun testExportSQLite_PreservesRelationships() = runBlocking {
        // Arrange: Create test data with relationships
        val flag = createTestFlagWithRelations()
        val createdFlag = flagRepository.create(flag)
        
        // Act: Export to SQLite
        val sqliteBytes = exportService.exportSQLite(excludeSnapshots = false)
        
        // Assert: Verify relationships are preserved
        val tempFile = File.createTempFile("test_export_", ".sqlite")
        try {
            tempFile.writeBytes(sqliteBytes)
            
            val db = org.jetbrains.exposed.sql.Database.connect(
                url = "jdbc:sqlite:${tempFile.absolutePath}",
                driver = "org.sqlite.JDBC"
            )
            
            transaction(db) {
                // Verify variant belongs to flag
                val variant = Variants.select { Variants.flagId eq createdFlag.id }.firstOrNull()
                assertNotNull(variant, "Variant should exist")
                assertEquals(createdFlag.id, variant[Variants.flagId], "Variant should belong to flag")
                
                // Verify segment belongs to flag
                val segment = Segments.select { Segments.flagId eq createdFlag.id }.firstOrNull()
                assertNotNull(segment, "Segment should exist")
                assertEquals(createdFlag.id, segment[Segments.flagId], "Segment should belong to flag")
                
                // Verify constraint belongs to segment
                val constraint = Constraints.select { Constraints.segmentId eq segment[Segments.id].value }.firstOrNull()
                assertNotNull(constraint, "Constraint should exist")
                assertEquals(segment[Segments.id].value, constraint[Constraints.segmentId], "Constraint should belong to segment")
                
                // Verify distribution belongs to segment and variant
                val distribution = Distributions.select { 
                    Distributions.segmentId eq segment[Segments.id].value 
                }.firstOrNull()
                assertNotNull(distribution, "Distribution should exist")
                assertEquals(segment[Segments.id].value, distribution[Distributions.segmentId], "Distribution should belong to segment")
                assertNotNull(distribution[Distributions.variantId], "Distribution should have variant ID")
            }
        } finally {
            tempFile.delete()
        }
    }
    
    @Test
    fun testExportSQLite_ExcludesSnapshots_WhenExcludeSnapshotsIsTrue() = runBlocking {
        // Arrange: Create flag and snapshot
        val flag = createTestFlag()
        val createdFlag = flagRepository.create(flag)
        
        // Create snapshot
        val snapshot = FlagSnapshot(
            flagId = createdFlag.id,
            updatedBy = "test-user",
            flag = Json.encodeToString(Flag.serializer(), createdFlag)
        )
        flagSnapshotRepository.create(snapshot)
        
        // Act: Export with exclude_snapshots = true
        val sqliteBytes = exportService.exportSQLite(excludeSnapshots = true)
        
        // Assert: Verify snapshots are not exported
        val tempFile = File.createTempFile("test_export_", ".sqlite")
        try {
            tempFile.writeBytes(sqliteBytes)
            
            val db = org.jetbrains.exposed.sql.Database.connect(
                url = "jdbc:sqlite:${tempFile.absolutePath}",
                driver = "org.sqlite.JDBC"
            )
            
            transaction(db) {
                val snapshotCount = FlagSnapshots.selectAll().count()
                assertEquals(0, snapshotCount, "Should not export snapshots when exclude_snapshots=true")
            }
        } finally {
            tempFile.delete()
        }
    }
    
    @Test
    fun testExportSQLite_IncludesSnapshots_WhenExcludeSnapshotsIsFalse() = runBlocking {
        // Arrange: Create flag and snapshot
        val flag = createTestFlag()
        val createdFlag = flagRepository.create(flag)
        
        // Create snapshot
        val snapshot = FlagSnapshot(
            flagId = createdFlag.id,
            updatedBy = "test-user",
            flag = Json.encodeToString(Flag.serializer(), createdFlag)
        )
        val createdSnapshot = flagSnapshotRepository.create(snapshot)
        
        // Act: Export with exclude_snapshots = false
        val sqliteBytes = exportService.exportSQLite(excludeSnapshots = false)
        
        // Assert: Verify snapshots are exported
        val tempFile = File.createTempFile("test_export_", ".sqlite")
        try {
            tempFile.writeBytes(sqliteBytes)
            
            val db = org.jetbrains.exposed.sql.Database.connect(
                url = "jdbc:sqlite:${tempFile.absolutePath}",
                driver = "org.sqlite.JDBC"
            )
            
            transaction(db) {
                val snapshotCount = FlagSnapshots.selectAll().count()
                assertEquals(1, snapshotCount, "Should export snapshots when exclude_snapshots=false")
                
                val exportedSnapshot = FlagSnapshots.select { 
                    FlagSnapshots.id eq createdSnapshot.id 
                }.firstOrNull()
                assertNotNull(exportedSnapshot, "Exported snapshot should exist")
                assertEquals(createdFlag.id, exportedSnapshot[FlagSnapshots.flagId], "Snapshot should belong to flag")
                assertEquals("test-user", exportedSnapshot[FlagSnapshots.updatedBy], "Snapshot updatedBy should match")
            }
        } finally {
            tempFile.delete()
        }
    }
    
    @Test
    fun testExportSQLite_ExportsEntityTypes() = runBlocking {
        // Arrange: Create entity type
        val entityType = FlagEntityType(key = "test_entity_type")
        val createdEntityType = flagEntityTypeRepository.create(entityType)
        
        // Act: Export to SQLite
        val sqliteBytes = exportService.exportSQLite(excludeSnapshots = false)
        
        // Assert: Verify entity types are exported
        val tempFile = File.createTempFile("test_export_", ".sqlite")
        try {
            tempFile.writeBytes(sqliteBytes)
            
            val db = org.jetbrains.exposed.sql.Database.connect(
                url = "jdbc:sqlite:${tempFile.absolutePath}",
                driver = "org.sqlite.JDBC"
            )
            
            transaction(db) {
                val entityTypeCount = FlagEntityTypes.selectAll().count()
                assertTrue(entityTypeCount >= 1, "Should export at least 1 entity type")
                
                val exportedEntityType = FlagEntityTypes.select { 
                    FlagEntityTypes.id eq createdEntityType.id 
                }.firstOrNull()
                assertNotNull(exportedEntityType, "Exported entity type should exist")
                assertEquals("test_entity_type", exportedEntityType[FlagEntityTypes.key], "Entity type key should match")
            }
        } finally {
            tempFile.delete()
        }
    }
    
    @Test
    fun testExportSQLite_ValidSQLiteFormat() = runBlocking {
        // Arrange: Create test data
        val flag = createTestFlag()
        flagRepository.create(flag)
        
        // Act: Export to SQLite
        val sqliteBytes = exportService.exportSQLite(excludeSnapshots = false)
        
        // Assert: Verify SQLite file format
        val tempFile = File.createTempFile("test_export_", ".sqlite")
        try {
            tempFile.writeBytes(sqliteBytes)
            
            // SQLite files start with "SQLite format 3" header
            val header = String(sqliteBytes.take(16).toByteArray())
            assertTrue(header.startsWith("SQLite format 3"), "Should be valid SQLite format")
            
            // Verify file can be opened as SQLite database
            val db = org.jetbrains.exposed.sql.Database.connect(
                url = "jdbc:sqlite:${tempFile.absolutePath}",
                driver = "org.sqlite.JDBC"
            )
            
            transaction(db) {
                // Try to query - if it works, format is valid
                val result = Flags.selectAll().count()
                assertTrue(result >= 0, "Should be able to query exported database")
            }
        } finally {
            tempFile.delete()
        }
    }
    
    @Test
    fun testExportSQLite_ExportsMultipleFlags() = runBlocking {
        // Arrange: Create multiple flags
        val flag1 = createTestFlag(key = "flag1")
        val flag2 = createTestFlag(key = "flag2")
        flagRepository.create(flag1)
        flagRepository.create(flag2)
        
        // Act: Export to SQLite
        val sqliteBytes = exportService.exportSQLite(excludeSnapshots = false)
        
        // Assert: Verify all flags are exported
        val tempFile = File.createTempFile("test_export_", ".sqlite")
        try {
            tempFile.writeBytes(sqliteBytes)
            
            val db = org.jetbrains.exposed.sql.Database.connect(
                url = "jdbc:sqlite:${tempFile.absolutePath}",
                driver = "org.sqlite.JDBC"
            )
            
            transaction(db) {
                val flagCount = Flags.selectAll().count()
                assertEquals(2, flagCount, "Should export 2 flags")
                
                val exportedFlag1 = Flags.select { Flags.key eq "flag1" }.firstOrNull()
                val exportedFlag2 = Flags.select { Flags.key eq "flag2" }.firstOrNull()
                assertNotNull(exportedFlag1, "Flag1 should be exported")
                assertNotNull(exportedFlag2, "Flag2 should be exported")
            }
        } finally {
            tempFile.delete()
        }
    }
    
    // Helper functions
    
    private fun createTestFlag(key: String = "test_flag"): Flag {
        return Flag(
            key = key,
            description = "Test flag",
            enabled = true
        )
    }
    
    private fun createTestFlagWithRelations(): Flag {
        val variant1 = Variant(
            flagId = 1,
            key = "control",
            attachment = null
        )
        val variant2 = Variant(
            flagId = 1,
            key = "treatment",
            attachment = null
        )
        
        val segment = Segment(
            flagId = 1,
            description = "Test segment",
            rank = 0,
            rolloutPercent = 100
        )
        
        val constraint = Constraint(
            segmentId = 1,
            property = "user_id",
            operator = "EQ",
            value = "123"
        )
        
        val distribution1 = Distribution(
            segmentId = 1,
            variantId = 0, // Will be set after variant creation
            variantKey = "control",
            percent = 50
        )
        val distribution2 = Distribution(
            segmentId = 1,
            variantId = 0, // Will be set after variant creation
            variantKey = "treatment",
            percent = 50
        )
        
        val tag = Tag(value = "test_tag")
        
        return Flag(
            key = "test_flag",
            description = "Test flag with relations",
            enabled = true,
            variants = listOf(variant1, variant2),
            segments = listOf(segment),
            tags = listOf(tag)
        )
    }
}
