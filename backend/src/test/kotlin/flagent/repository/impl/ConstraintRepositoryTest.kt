package flagent.repository.impl

import flagent.domain.entity.Constraint
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
class ConstraintRepositoryTest {
    private lateinit var flagRepository: FlagRepository
    private lateinit var segmentRepository: SegmentRepository
    private lateinit var repository: ConstraintRepository

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
        segmentRepository = SegmentRepository()
        repository = ConstraintRepository()
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
    fun testCreateConstraint() = runBlocking {
        val flag = flagRepository.create(Flag(key = "test_flag", description = "Test flag", enabled = true))
        val segment = segmentRepository.create(Segment(flagId = flag.id, description = "Test segment", rank = 1, rolloutPercent = 100))
        
        val constraint = Constraint(
            segmentId = segment.id,
            property = "region",
            operator = "EQ",
            value = "US"
        )
        
        val created = repository.create(constraint)
        
        assertTrue(created.id > 0)
        assertEquals(segment.id, created.segmentId)
        assertEquals("region", created.property)
        assertEquals("EQ", created.operator)
        assertEquals("US", created.value)
    }
    
    @Test
    fun testFindById() = runBlocking {
        val flag = flagRepository.create(Flag(key = "test_flag", description = "Test flag", enabled = true))
        val segment = segmentRepository.create(Segment(flagId = flag.id, description = "Test segment", rank = 1, rolloutPercent = 100))
        val constraint = repository.create(Constraint(segmentId = segment.id, property = "region", operator = "EQ", value = "US"))
        
        val found = repository.findById(constraint.id)
        
        assertNotNull(found)
        assertEquals(constraint.id, found.id)
        assertEquals("region", found.property)
    }
    
    @Test
    fun testFindBySegmentId() = runBlocking {
        val flag = flagRepository.create(Flag(key = "test_flag", description = "Test flag", enabled = true))
        val segment = segmentRepository.create(Segment(flagId = flag.id, description = "Test segment", rank = 1, rolloutPercent = 100))
        repository.create(Constraint(segmentId = segment.id, property = "region", operator = "EQ", value = "US"))
        repository.create(Constraint(segmentId = segment.id, property = "age", operator = "GT", value = "18"))
        
        val constraints = repository.findBySegmentId(segment.id)
        
        assertEquals(2, constraints.size)
        assertTrue(constraints.all { it.segmentId == segment.id })
    }
    
    @Test
    fun testUpdateConstraint() = runBlocking {
        val flag = flagRepository.create(Flag(key = "test_flag", description = "Test flag", enabled = true))
        val segment = segmentRepository.create(Segment(flagId = flag.id, description = "Test segment", rank = 1, rolloutPercent = 100))
        val constraint = repository.create(Constraint(segmentId = segment.id, property = "region", operator = "EQ", value = "US"))
        
        val updated = repository.update(constraint.copy(value = "EU"))
        
        assertEquals(constraint.id, updated.id)
        assertEquals("EU", updated.value)
        
        val found = repository.findById(constraint.id)
        assertNotNull(found)
        assertEquals("EU", found.value)
    }
    
    @Test
    fun testDeleteConstraint() = runBlocking {
        val flag = flagRepository.create(Flag(key = "test_flag", description = "Test flag", enabled = true))
        val segment = segmentRepository.create(Segment(flagId = flag.id, description = "Test segment", rank = 1, rolloutPercent = 100))
        val constraint = repository.create(Constraint(segmentId = segment.id, property = "region", operator = "EQ", value = "US"))
        
        repository.delete(constraint.id)
        
        val found = repository.findById(constraint.id)
        assertNull(found)
    }
}
