package flagent.cache.impl

import flagent.domain.entity.Flag
import flagent.domain.entity.Tag
import flagent.domain.repository.IFlagRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EvalCacheTest {

    @Test
    fun `constructor throws when both repository and fetcher are null`() {
        assertFailsWith<IllegalArgumentException> {
            EvalCache(flagRepository = null, fetcher = null)
        }
    }

    @Test
    fun `getByFlagKeyOrID returns flag when using fetcher only`() = runBlocking {
        val flag = Flag(
            id = 1,
            key = "test_flag",
            description = "Test flag",
            enabled = true
        )
        val fetcher = object : EvalCacheFetcher {
            override suspend fun fetch(): List<Flag> = listOf(flag)
        }
        val cache = EvalCache(flagRepository = null, fetcher = fetcher)
        cache.start()
        cache.refresh()

        val result = cache.getByFlagKeyOrID(1)
        assertNotNull(result)
        assertEquals(flag, result)

        val resultByKey = cache.getByFlagKeyOrID("test_flag")
        assertNotNull(resultByKey)
        assertEquals(flag, resultByKey)

        cache.stop()
    }

    @Test
    fun `getByFlagKeyOrID returns flag by ID`() = runBlocking {
        val flag = Flag(
            id = 1,
            key = "test_flag",
            description = "Test flag",
            enabled = true
        )
        
        val repository = mockk<IFlagRepository> {
            coEvery { findAll(preload = true) } returns listOf(flag)
        }
        
        val cache = EvalCache(repository)
        cache.start()
        cache.refresh() // Ensure cache is loaded
        
        val result = cache.getByFlagKeyOrID(1)
        
        assertNotNull(result)
        assertEquals(flag, result)
        
        cache.stop()
    }
    
    @Test
    fun `getByFlagKeyOrID returns flag by key`() = runBlocking {
        val flag = Flag(
            id = 1,
            key = "test_flag",
            description = "Test flag",
            enabled = true
        )
        
        val repository = mockk<IFlagRepository> {
            coEvery { findAll(preload = true) } returns listOf(flag)
        }
        
        val cache = EvalCache(repository)
        cache.start()
        cache.refresh() // Ensure cache is loaded
        
        val result = cache.getByFlagKeyOrID("test_flag")
        
        assertNotNull(result)
        assertEquals(flag, result)
        
        cache.stop()
    }
    
    @Test
    fun `getByFlagKeyOrID returns null for non-existent flag`() = runBlocking {
        val repository = mockk<IFlagRepository> {
            coEvery { findAll(preload = true) } returns emptyList()
        }
        
        val cache = EvalCache(repository)
        cache.start()
        cache.refresh() // Ensure cache is loaded
        
        val result = cache.getByFlagKeyOrID(999)
        
        assertNull(result)
        
        cache.stop()
    }
    
    @Test
    fun `getByTags returns flags by tags with ANY operator`() = runBlocking {
        val flag1 = Flag(
            id = 1,
            key = "flag1",
            description = "Flag 1",
            enabled = true,
            tags = listOf(Tag(id = 1, value = "tag1"))
        )
        
        val flag2 = Flag(
            id = 2,
            key = "flag2",
            description = "Flag 2",
            enabled = true,
            tags = listOf(Tag(id = 2, value = "tag2"))
        )
        
        val repository = mockk<IFlagRepository> {
            coEvery { findAll(preload = true) } returns listOf(flag1, flag2)
        }
        
        val cache = EvalCache(repository)
        cache.start()
        cache.refresh() // Ensure cache is loaded
        
        val results = cache.getByTags(listOf("tag1", "tag2"), "ANY")
        
        assertEquals(2, results.size)
        
        cache.stop()
    }
    
    @Test
    fun `getByTags returns flags by tags with ALL operator`() = runBlocking {
        val flag1 = Flag(
            id = 1,
            key = "flag1",
            description = "Flag 1",
            enabled = true,
            tags = listOf(
                Tag(id = 1, value = "tag1"),
                Tag(id = 2, value = "tag2")
            )
        )
        
        val flag2 = Flag(
            id = 2,
            key = "flag2",
            description = "Flag 2",
            enabled = true,
            tags = listOf(Tag(id = 1, value = "tag1"))
        )
        
        val repository = mockk<IFlagRepository> {
            coEvery { findAll(preload = true) } returns listOf(flag1, flag2)
        }
        
        val cache = EvalCache(repository)
        cache.start()
        cache.refresh() // Ensure cache is loaded
        
        val results = cache.getByTags(listOf("tag1", "tag2"), "ALL")
        
        assertEquals(1, results.size)
        assertEquals(flag1, results.first())
        
        cache.stop()
    }
    
    @Test
    fun `getByTags returns empty list when no flags match`() = runBlocking {
        val flag = Flag(
            id = 1,
            key = "flag1",
            description = "Flag 1",
            enabled = true,
            tags = listOf(Tag(id = 1, value = "tag1"))
        )
        
        val repository = mockk<IFlagRepository> {
            coEvery { findAll(preload = true) } returns listOf(flag)
        }
        
        val cache = EvalCache(repository)
        cache.start()
        cache.refresh() // Ensure cache is loaded
        
        val results = cache.getByTags(listOf("tag3"), null)
        
        assertEquals(0, results.size)
        
        cache.stop()
    }
    
    @Test
    fun `getByTags excludes disabled flags`() = runBlocking {
        val flag1 = Flag(
            id = 1,
            key = "flag1",
            description = "Flag 1",
            enabled = true,
            tags = listOf(Tag(id = 1, value = "tag1"))
        )
        
        val flag2 = Flag(
            id = 2,
            key = "flag2",
            description = "Flag 2",
            enabled = false,
            tags = listOf(Tag(id = 1, value = "tag1"))
        )
        
        val repository = mockk<IFlagRepository> {
            coEvery { findAll(preload = true) } returns listOf(flag1, flag2)
        }
        
        val cache = EvalCache(repository)
        cache.start()
        cache.refresh() // Ensure cache is loaded
        
        val results = cache.getByTags(listOf("tag1"), null)
        
        assertEquals(1, results.size)
        assertEquals(flag1, results.first())
        
        cache.stop()
    }
    
    @Test
    fun `export returns all cached flags`() = runBlocking {
        val flag1 = Flag(
            id = 1,
            key = "flag1",
            description = "Flag 1",
            enabled = true
        )
        
        val flag2 = Flag(
            id = 2,
            key = "flag2",
            description = "Flag 2",
            enabled = true
        )
        
        val repository = mockk<IFlagRepository> {
            coEvery { findAll(preload = true) } returns listOf(flag1, flag2)
        }
        
        val cache = EvalCache(repository)
        cache.start()
        cache.refresh() // Ensure cache is loaded
        
        val export = cache.export()
        
        assertEquals(2, export.flags.size)
        
        cache.stop()
    }
    
    @Test
    fun `refresh updates cache from repository`() = runBlocking {
        val flag1 = Flag(
            id = 1,
            key = "flag1",
            description = "Flag 1",
            enabled = true
        )
        
        val flag2 = Flag(
            id = 2,
            key = "flag2",
            description = "Flag 2",
            enabled = true
        )
        
        val fetcher = object : EvalCacheFetcher {
            var callCount = 0
            override suspend fun fetch(): List<Flag> {
                callCount++
                return when (callCount) {
                    1 -> listOf(flag1)
                    else -> listOf(flag1, flag2)
                }
            }
        }
        val cache = EvalCache(flagRepository = null, fetcher = fetcher)
        cache.refresh() // First load: flag1 only (no start() to avoid periodic refresh race)
        
        // After first refresh, only flag1 should be in cache
        var result = cache.getByFlagKeyOrID(2)
        assertNull(result, "flag2 should not be in cache after first refresh")
        
        // Verify flag1 is in cache
        result = cache.getByFlagKeyOrID(1)
        assertNotNull(result, "flag1 should be in cache after start")
        assertEquals(flag1, result)
        
        // Refresh should update cache with new data
        cache.refresh()
        
        // Now flag2 should be available
        result = cache.getByFlagKeyOrID(2)
        assertNotNull(result, "flag2 should be in cache after second refresh")
        assertEquals(flag2, result)
        
        result = cache.getByFlagKeyOrID(1)
        assertNotNull(result, "flag1 should still be in cache after second refresh")
        assertEquals(flag1, result)
    }
    
    @Test
    fun `start and stop lifecycle works correctly`() = runBlocking {
        val flag = Flag(
            id = 1,
            key = "test_flag",
            description = "Test flag",
            enabled = true
        )
        
        val repository = mockk<IFlagRepository> {
            coEvery { findAll(preload = true) } returns listOf(flag)
        }
        
        val cache = EvalCache(repository)
        
        // Before start, cache should be empty
        var result = cache.getByFlagKeyOrID(1)
        assertNull(result)
        
        cache.start()
        cache.refresh()
        
        // After start, cache should be populated
        result = cache.getByFlagKeyOrID(1)
        assertNotNull(result)
        
        cache.stop()
        
        // After stop, cache should still be accessible (but refresh won't happen)
        result = cache.getByFlagKeyOrID(1)
        assertNotNull(result)
    }
    
    @Test
    fun `getByTags returns multiple flags with same tag`() = runBlocking {
        val flag1 = Flag(
            id = 1,
            key = "flag1",
            description = "Flag 1",
            enabled = true,
            tags = listOf(Tag(id = 1, value = "common_tag"))
        )
        
        val flag2 = Flag(
            id = 2,
            key = "flag2",
            description = "Flag 2",
            enabled = true,
            tags = listOf(Tag(id = 1, value = "common_tag"))
        )
        
        val flag3 = Flag(
            id = 3,
            key = "flag3",
            description = "Flag 3",
            enabled = true,
            tags = listOf(Tag(id = 1, value = "common_tag"))
        )
        
        val repository = mockk<IFlagRepository> {
            coEvery { findAll(preload = true) } returns listOf(flag1, flag2, flag3)
        }
        
        val cache = EvalCache(repository)
        cache.start()
        cache.refresh()
        
        val results = cache.getByTags(listOf("common_tag"), null)
        
        assertEquals(3, results.size)
        assertTrue(results.contains(flag1))
        assertTrue(results.contains(flag2))
        assertTrue(results.contains(flag3))
        
        cache.stop()
    }
    
    @Test
    fun `getByTags handles flags with multiple tags`() = runBlocking {
        val flag = Flag(
            id = 1,
            key = "flag1",
            description = "Flag 1",
            enabled = true,
            tags = listOf(
                Tag(id = 1, value = "tag1"),
                Tag(id = 2, value = "tag2"),
                Tag(id = 3, value = "tag3")
            )
        )
        
        val repository = mockk<IFlagRepository> {
            coEvery { findAll(preload = true) } returns listOf(flag)
        }
        
        val cache = EvalCache(repository)
        cache.start()
        cache.refresh()
        
        // Should find by any tag
        var results = cache.getByTags(listOf("tag1"), null)
        assertEquals(1, results.size)
        assertEquals(flag, results.first())
        
        results = cache.getByTags(listOf("tag2"), null)
        assertEquals(1, results.size)
        assertEquals(flag, results.first())
        
        results = cache.getByTags(listOf("tag3"), null)
        assertEquals(1, results.size)
        assertEquals(flag, results.first())
        
        // Should find by ALL tags
        results = cache.getByTags(listOf("tag1", "tag2", "tag3"), "ALL")
        assertEquals(1, results.size)
        assertEquals(flag, results.first())
        
        // Should not find if one tag is missing
        results = cache.getByTags(listOf("tag1", "tag2", "tag4"), "ALL")
        assertEquals(0, results.size)
        
        cache.stop()
    }
    
    @Test
    fun `export returns empty list for empty cache`() = runBlocking {
        val repository = mockk<IFlagRepository> {
            coEvery { findAll(preload = true) } returns emptyList()
        }
        
        val cache = EvalCache(repository)
        cache.start()
        cache.refresh()
        
        val export = cache.export()
        
        assertEquals(0, export.flags.size)
        
        cache.stop()
    }
    
    @Test
    fun `cache handles large number of flags efficiently`() = runBlocking {
        val flags = (1..1000).map { id ->
            Flag(
                id = id,
                key = "flag_$id",
                description = "Flag $id",
                enabled = true,
                tags = listOf(
                    Tag(id = id, value = "tag_${id % 10}") // 10 different tags
                )
            )
        }
        
        val repository = mockk<IFlagRepository> {
            coEvery { findAll(preload = true) } returns flags
        }
        
        val cache = EvalCache(repository)
        cache.start()
        cache.refresh()
        
        // Test lookup by ID
        val result = requireNotNull(cache.getByFlagKeyOrID(500))
        assertEquals("flag_500", result.key)
        
        // Test lookup by key
        val resultByKey = requireNotNull(cache.getByFlagKeyOrID("flag_750"))
        assertEquals(750, resultByKey.id)
        
        // Test lookup by tags
        val resultsByTag = cache.getByTags(listOf("tag_0"), null)
        assertEquals(100, resultsByTag.size) // 100 flags with tag_0 (1000 / 10)
        
        // Test export
        val export = cache.export()
        assertEquals(1000, export.flags.size)
        
        cache.stop()
    }
}
