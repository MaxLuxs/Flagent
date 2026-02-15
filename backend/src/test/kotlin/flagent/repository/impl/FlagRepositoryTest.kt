package flagent.repository.impl

import flagent.domain.entity.Flag
import flagent.repository.Database
import flagent.repository.tables.*
import flagent.test.PostgresTestcontainerExtension
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.*

@ExtendWith(PostgresTestcontainerExtension::class)
class FlagRepositoryTest {
    private lateinit var repository: FlagRepository

    @BeforeTest
    fun setup() {
        transaction(Database.getDatabase()) {
            SchemaUtils.createMissingTablesAndColumns(
                Flags, Segments, Variants, Constraints, Distributions,
                Tags, FlagsTags, FlagSnapshots, FlagEntityTypes, Webhooks,
                Users, EvaluationEvents, AnalyticsEvents, CrashReports
            )
        }
        repository = FlagRepository()
    }

    @AfterTest
    fun cleanup() {
        try {
            transaction(Database.getDatabase()) {
                SchemaUtils.drop(
                    Flags, Segments, Variants, Constraints, Distributions,
                    Tags, FlagsTags, FlagSnapshots, FlagEntityTypes, Users
                )
            }
        } catch (_: Exception) { }
    }
    
    @Test
    fun testCreateFlag() = runBlocking {
        val flag = Flag(
            key = "test_flag",
            description = "Test flag description",
            enabled = true
        )
        
        val created = repository.create(flag)
        
        assertTrue(created.id > 0)
        assertEquals("test_flag", created.key)
        assertEquals("Test flag description", created.description)
        assertTrue(created.enabled)
    }
    
    @Test
    fun testFindById() = runBlocking {
        val flag = Flag(
            key = "test_flag",
            description = "Test flag",
            enabled = true
        )
        
        val created = repository.create(flag)
        val found = repository.findById(created.id)
        
        assertNotNull(found)
        assertEquals(created.id, found.id)
        assertEquals("test_flag", found.key)
        assertEquals("Test flag", found.description)
    }
    
    @Test
    fun testFindById_ReturnsNull_WhenNotFound() = runBlocking {
        val found = repository.findById(999)
        assertNull(found)
    }
    
    @Test
    fun testFindByKey() = runBlocking {
        val flag = Flag(
            key = "test_flag",
            description = "Test flag",
            enabled = true
        )
        
        val created = repository.create(flag)
        val found = repository.findByKey("test_flag")
        
        assertNotNull(found)
        assertEquals(created.id, found.id)
        assertEquals("test_flag", found.key)
    }
    
    @Test
    fun testFindByKey_ReturnsNull_WhenNotFound() = runBlocking {
        val found = repository.findByKey("non_existent")
        assertNull(found)
    }
    
    @Test
    fun testFindAll() = runBlocking {
        repository.create(Flag(key = "flag1", description = "Flag 1", enabled = true))
        repository.create(Flag(key = "flag2", description = "Flag 2", enabled = false))
        repository.create(Flag(key = "flag3", description = "Flag 3", enabled = true))
        
        val all = repository.findAll()
        
        assertTrue(all.size >= 3)
    }
    
    @Test
    fun testFindAll_WithLimit() = runBlocking {
        repository.create(Flag(key = "flag1", description = "Flag 1", enabled = true))
        repository.create(Flag(key = "flag2", description = "Flag 2", enabled = true))
        repository.create(Flag(key = "flag3", description = "Flag 3", enabled = true))
        
        val limited = repository.findAll(limit = 2, offset = 0)
        
        assertEquals(2, limited.size)
    }
    
    @Test
    fun testFindAll_WithOffset() = runBlocking {
        repository.create(Flag(key = "flag1", description = "Flag 1", enabled = true))
        repository.create(Flag(key = "flag2", description = "Flag 2", enabled = true))
        repository.create(Flag(key = "flag3", description = "Flag 3", enabled = true))
        
        val offset = repository.findAll(limit = 2, offset = 1)
        
        assertTrue(offset.size <= 2)
    }
    
    @Test
    fun testFindAll_WithEnabledFilter() = runBlocking {
        repository.create(Flag(key = "flag1", description = "Flag 1", enabled = true))
        repository.create(Flag(key = "flag2", description = "Flag 2", enabled = false))
        repository.create(Flag(key = "flag3", description = "Flag 3", enabled = true))
        
        val enabled = repository.findAll(enabled = true)
        
        assertTrue(enabled.all { it.enabled })
        assertTrue(enabled.size >= 2)
    }
    
    @Test
    fun testFindAll_WithKeyFilter() = runBlocking {
        repository.create(Flag(key = "flag1", description = "Flag 1", enabled = true))
        repository.create(Flag(key = "flag2", description = "Flag 2", enabled = true))
        
        val found = repository.findAll(key = "flag1")
        
        assertEquals(1, found.size)
        assertEquals("flag1", found[0].key)
    }
    
    @Test
    fun testFindAll_WithDescriptionLike() = runBlocking {
        repository.create(Flag(key = "flag1", description = "Test flag one", enabled = true))
        repository.create(Flag(key = "flag2", description = "Test flag two", enabled = true))
        repository.create(Flag(key = "flag3", description = "Other description", enabled = true))
        
        val found = repository.findAll(descriptionLike = "test")
        
        assertTrue(found.size >= 2)
        assertTrue(found.all { it.description.lowercase().contains("test") })
    }
    
    @Test
    fun testUpdateFlag() = runBlocking {
        val flag = Flag(
            key = "test_flag",
            description = "Original description",
            enabled = true
        )
        
        val created = repository.create(flag)
        val updated = repository.update(created.copy(description = "Updated description"))
        
        assertEquals(created.id, updated.id)
        assertEquals("Updated description", updated.description)
        
        val found = repository.findById(created.id)
        assertNotNull(found)
        assertEquals("Updated description", found.description)
    }

    @Test
    fun testCreateFlagWithEnvironmentId() = runBlocking {
        val flag = Flag(
            key = "env_flag_${System.currentTimeMillis()}",
            description = "Flag with environment",
            enabled = true,
            environmentId = 5L
        )
        val created = repository.create(flag)
        assertTrue(created.id > 0)
        assertEquals(5L, created.environmentId)
        val found = repository.findById(created.id)
        assertNotNull(found)
        assertEquals(5L, found?.environmentId)
    }

    @Test
    fun testUpdateFlagPreservesEnvironmentId() = runBlocking {
        val flag = Flag(
            key = "env_update_${System.currentTimeMillis()}",
            description = "Original",
            enabled = true,
            environmentId = 10L
        )
        val created = repository.create(flag)
        val updated = repository.update(created.copy(description = "Updated", environmentId = 20L))
        assertEquals(20L, updated.environmentId)
        val found = repository.findById(created.id)
        assertNotNull(found)
        assertEquals(20L, found?.environmentId)
    }
    
    @Test
    fun testDeleteFlag() = runBlocking {
        val flag = Flag(
            key = "test_flag",
            description = "Test flag",
            enabled = true
        )
        
        val created = repository.create(flag)
        repository.delete(created.id)
        
        val found = repository.findById(created.id)
        assertNull(found)
        
        val foundDeleted = repository.findAll(deleted = true)
        assertTrue(foundDeleted.any { it.id == created.id })
    }
    
    @Test
    fun testRestoreFlag() = runBlocking {
        val flag = Flag(
            key = "test_flag",
            description = "Test flag",
            enabled = true
        )
        
        val created = repository.create(flag)
        repository.delete(created.id)
        
        val restored = repository.restore(created.id)
        
        assertNotNull(restored)
        assertEquals(created.id, restored?.id)
        
        val found = repository.findById(created.id)
        assertNotNull(found)
    }
    
    @Test
    fun testFindAll_WithPreload() = runBlocking {
        val flag = Flag(
            key = "test_flag",
            description = "Test flag",
            enabled = true
        )
        
        val created = repository.create(flag)
        val found = repository.findAll(preload = true)
        
        assertTrue(found.isNotEmpty())
        // With preload, segments and variants should be loaded (even if empty)
        val flagWithPreload = found.find { it.id == created.id }
        assertNotNull(flagWithPreload)
        assertNotNull(flagWithPreload.segments)
        assertNotNull(flagWithPreload.variants)
    }
    
    @Test
    fun testFindAll_WithTagsFilter() = runBlocking {
        // Note: This test requires Tag and FlagsTags tables to be set up
        // For now, test that it doesn't crash with empty result
        val result = repository.findAll(tags = "non_existent_tag")
        assertTrue(result.isEmpty())
    }
    
    @Test
    fun testFindAll_WithMultipleFilters() = runBlocking {
        repository.create(Flag(key = "flag1", description = "Test flag one", enabled = true))
        repository.create(Flag(key = "flag2", description = "Test flag two", enabled = false))
        repository.create(Flag(key = "flag3", description = "Other description", enabled = true))
        
        // Combined filters: enabled=true AND descriptionLike="test"
        val found = repository.findAll(
            enabled = true,
            descriptionLike = "test"
        )
        
        assertTrue(found.all { it.enabled })
        assertTrue(found.all { it.description.lowercase().contains("test") })
    }
    
    @Test
    fun testFindAll_WithDescriptionAndKeyFilter() = runBlocking {
        repository.create(Flag(key = "specific_flag", description = "Specific description", enabled = true))
        repository.create(Flag(key = "other_flag", description = "Specific description", enabled = true))
        
        val found = repository.findAll(
            description = "Specific description",
            key = "specific_flag"
        )
        
        assertEquals(1, found.size)
        assertEquals("specific_flag", found[0].key)
        assertEquals("Specific description", found[0].description)
    }
    
    @Test
    fun testFindAll_WithDeletedFilter() = runBlocking {
        val flag1 = repository.create(Flag(key = "flag1", description = "Flag 1", enabled = true))
        val flag2 = repository.create(Flag(key = "flag2", description = "Flag 2", enabled = true))
        
        // Delete one flag
        repository.delete(flag1.id)
        
        // Find non-deleted
        val active = repository.findAll(deleted = false)
        assertFalse(active.any { it.id == flag1.id })
        assertTrue(active.any { it.id == flag2.id })
        
        // Find deleted
        val deleted = repository.findAll(deleted = true)
        assertTrue(deleted.any { it.id == flag1.id })
        assertFalse(deleted.any { it.id == flag2.id })
    }
    
    @Test
    fun testFindAll_WithLimitAndOffset() = runBlocking {
        // Create multiple flags
        repeat(5) { i ->
            repository.create(Flag(key = "flag_$i", description = "Flag $i", enabled = true))
        }
        
        // Test pagination
        val firstPage = repository.findAll(limit = 2, offset = 0)
        assertEquals(2, firstPage.size)
        
        val secondPage = repository.findAll(limit = 2, offset = 2)
        assertEquals(2, secondPage.size)
        
        // Ensure different results
        assertTrue(firstPage.map { it.id }.intersect(secondPage.map { it.id }).isEmpty())
    }
    
    @Test
    fun testFindByTags() = runBlocking {
        // Test empty tags list
        val empty = repository.findByTags(emptyList())
        assertTrue(empty.isEmpty())
        
        // Test with non-existent tags
        val nonExistent = repository.findByTags(listOf("non_existent_tag"))
        assertTrue(nonExistent.isEmpty())
    }
    
    @Test
    fun testUpdateFlag_UpdatesAllFields() = runBlocking {
        val flag = Flag(
            key = "test_flag",
            description = "Original description",
            enabled = true,
            notes = "Original notes",
            entityType = "user",
            dataRecordsEnabled = false
        )
        
        val created = repository.create(flag)
        val updated = repository.update(created.copy(
            description = "Updated description",
            enabled = false,
            notes = "Updated notes",
            entityType = "order",
            dataRecordsEnabled = true
        ))
        
        val found = repository.findById(created.id)
        assertNotNull(found)
        assertEquals("Updated description", found.description)
        assertFalse(found.enabled)
        assertEquals("Updated notes", found.notes)
        assertEquals("order", found.entityType)
        assertTrue(found.dataRecordsEnabled)
    }
    
    @Test
    fun testRestoreFlag_AfterDelete() = runBlocking {
        val flag = Flag(key = "test_flag", description = "Test flag", enabled = true)
        val created = repository.create(flag)
        
        // Delete
        repository.delete(created.id)
        val afterDelete = repository.findById(created.id)
        assertNull(afterDelete)
        
        // Restore
        val restored = repository.restore(created.id)
        assertNotNull(restored)
        
        // Verify restored
        val afterRestore = repository.findById(created.id)
        assertNotNull(afterRestore)
        assertEquals(created.id, afterRestore.id)
        assertEquals("test_flag", afterRestore.key)
    }
    
    @Test
    fun testFindByIdIncludeDeleted_ReturnsArchivedFlag() = runBlocking {
        val flag = Flag(key = "archived_flag", description = "Archived", enabled = true)
        val created = repository.create(flag)
        repository.delete(created.id)
        
        assertNull(repository.findById(created.id))
        val included = repository.findByIdIncludeDeleted(created.id)
        assertNotNull(included)
        assertEquals(created.id, included?.id)
        assertEquals("archived_flag", included?.key)
    }
    
    @Test
    fun testPermanentDelete_RemovesRow() = runBlocking {
        val flag = Flag(key = "to_remove", description = "To remove", enabled = true)
        val created = repository.create(flag)
        repository.delete(created.id)
        
        repository.permanentDelete(created.id)
        
        assertNull(repository.findById(created.id))
        assertNull(repository.findByIdIncludeDeleted(created.id))
    }
    
    @Test
    fun testFindAll_WithEmptyResult() = runBlocking {
        // Search for non-existent key
        val result = repository.findAll(key = "definitely_non_existent_key_12345")
        assertTrue(result.isEmpty())
    }
    
    @Test
    fun testFindAll_NoFilters_ReturnsAllNonDeleted() = runBlocking {
        val flag1 = repository.create(Flag(key = "flag1", description = "Flag 1", enabled = true))
        val flag2 = repository.create(Flag(key = "flag2", description = "Flag 2", enabled = false))
        
        // Delete one
        repository.delete(flag1.id)
        
        val all = repository.findAll()
        // Should not include deleted flag1
        assertFalse(all.any { it.id == flag1.id })
        assertTrue(all.any { it.id == flag2.id })
    }
    
    @Test
    fun testFindAll_WithLargeOffset() = runBlocking {
        // Create a few flags
        repeat(3) { i ->
            repository.create(Flag(key = "flag_$i", description = "Flag $i", enabled = true))
        }
        
        // Offset beyond available records
        val result = repository.findAll(limit = 10, offset = 100)
        assertTrue(result.isEmpty())
    }
    
    @Test
    fun testCountAll() = runBlocking {
        repository.create(Flag(key = "count1", description = "Count flag 1", enabled = true))
        repository.create(Flag(key = "count2", description = "Count flag 2", enabled = false))
        repository.create(Flag(key = "count3", description = "Count flag 3", enabled = true))
        
        val total = repository.countAll()
        assertTrue(total >= 3)
    }
    
    @Test
    fun testCountAll_WithEnabledFilter() = runBlocking {
        repository.create(Flag(key = "count_en1", description = "Enabled", enabled = true))
        repository.create(Flag(key = "count_dis1", description = "Disabled", enabled = false))
        
        val enabledCount = repository.countAll(enabled = true)
        val disabledCount = repository.countAll(enabled = false)
        
        assertTrue(enabledCount >= 1)
        assertTrue(disabledCount >= 1)
    }
    
    @Test
    fun testCountAll_WithDescriptionLike() = runBlocking {
        repository.create(Flag(key = "count_desc1", description = "UniqueCountDesc", enabled = true))
        repository.create(Flag(key = "count_desc2", description = "UniqueCountDescTwo", enabled = true))
        repository.create(Flag(key = "count_other", description = "Other description", enabled = true))
        
        val count = repository.countAll(descriptionLike = "UniqueCountDesc")
        assertTrue(count >= 2)
    }
    
    @Test
    fun testCountAll_WithTagsFilter() = runBlocking {
        val count = repository.countAll(tags = "non_existent_tag_xyz")
        assertEquals(0L, count)
    }
}
