package flagent.repository.impl

import flagent.domain.entity.Flag
import flagent.domain.entity.Segment
import flagent.repository.Database
import flagent.repository.tables.*
import flagent.test.PostgresTestcontainerExtension
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.*

@ExtendWith(PostgresTestcontainerExtension::class)
class SegmentRepositoryTest {
    private lateinit var flagRepository: FlagRepository
    private lateinit var repository: SegmentRepository

    @BeforeTest
    fun setup() {
        transaction(Database.getDatabase()) {
            SchemaUtils.createMissingTablesAndColumns(
                Flags, Segments, Variants, Constraints, Distributions,
                Tags, FlagsTags, FlagSnapshots, FlagEntityTypes, Webhooks,
                Users, EvaluationEvents, AnalyticsEvents, CrashReports
            )
        }
        flagRepository = FlagRepository()
        repository = SegmentRepository()
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
    fun testCreateSegment() = runBlocking {
        val flag = flagRepository.create(Flag(key = "test_flag", description = "Test flag", enabled = true))
        
        val segment = Segment(
            flagId = flag.id,
            description = "Test segment",
            rank = 1,
            rolloutPercent = 100
        )
        
        val created = repository.create(segment)
        
        assertTrue(created.id > 0)
        assertEquals(flag.id, created.flagId)
        assertEquals("Test segment", created.description)
        assertEquals(1, created.rank)
        assertEquals(100, created.rolloutPercent)
    }
    
    @Test
    fun testFindById() = runBlocking {
        val flag = flagRepository.create(Flag(key = "test_flag", description = "Test flag", enabled = true))
        val segment = repository.create(Segment(flagId = flag.id, description = "Test segment", rank = 1, rolloutPercent = 100))
        
        val found = repository.findById(segment.id)
        
        assertNotNull(found)
        assertEquals(segment.id, found.id)
        assertEquals(flag.id, found.flagId)
    }
    
    @Test
    fun testFindById_ReturnsNull_WhenNotFound() = runBlocking {
        val found = repository.findById(999)
        assertNull(found)
    }
    
    @Test
    fun testFindByFlagId() = runBlocking {
        val flag = flagRepository.create(Flag(key = "test_flag", description = "Test flag", enabled = true))
        repository.create(Segment(flagId = flag.id, description = "Segment 1", rank = 1, rolloutPercent = 100))
        repository.create(Segment(flagId = flag.id, description = "Segment 2", rank = 2, rolloutPercent = 50))
        
        val segments = repository.findByFlagId(flag.id)
        
        assertEquals(2, segments.size)
        assertTrue(segments.all { it.flagId == flag.id })
    }
    
    @Test
    fun testUpdateSegment() = runBlocking {
        val flag = flagRepository.create(Flag(key = "test_flag", description = "Test flag", enabled = true))
        val segment = repository.create(Segment(flagId = flag.id, description = "Original", rank = 1, rolloutPercent = 100))
        
        val updated = repository.update(segment.copy(description = "Updated", rolloutPercent = 50))
        
        assertEquals(segment.id, updated.id)
        assertEquals("Updated", updated.description)
        assertEquals(50, updated.rolloutPercent)
        
        val found = repository.findById(segment.id)
        assertNotNull(found)
        assertEquals("Updated", found.description)
    }
    
    @Test
    fun testDeleteSegment() = runBlocking {
        val flag = flagRepository.create(Flag(key = "test_flag", description = "Test flag", enabled = true))
        val segment = repository.create(Segment(flagId = flag.id, description = "Test segment", rank = 1, rolloutPercent = 100))
        
        repository.delete(segment.id)
        
        val found = repository.findById(segment.id)
        assertNull(found)
    }
    
    @Test
    fun testReorderSegments() = runBlocking {
        val flag = flagRepository.create(Flag(key = "test_flag", description = "Test flag", enabled = true))
        val segment1 = repository.create(Segment(flagId = flag.id, description = "Segment 1", rank = 1, rolloutPercent = 100))
        val segment2 = repository.create(Segment(flagId = flag.id, description = "Segment 2", rank = 2, rolloutPercent = 100))
        val segment3 = repository.create(Segment(flagId = flag.id, description = "Segment 3", rank = 3, rolloutPercent = 100))
        
        repository.reorder(flag.id, listOf(segment3.id, segment1.id, segment2.id))
        
        val segments = repository.findByFlagId(flag.id)
        assertEquals(3, segments.size)
        assertEquals(segment3.id, segments[0].id) // rank 0
        assertEquals(segment1.id, segments[1].id) // rank 1
        assertEquals(segment2.id, segments[2].id) // rank 2
    }
}
