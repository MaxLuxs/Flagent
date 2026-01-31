package flagent.service

import flagent.cache.impl.EvalCacheFlagExport
import flagent.cache.impl.toEvalCacheExport
import flagent.cache.impl.toFlag
import flagent.domain.entity.*
import flagent.domain.repository.IFlagRepository
import flagent.domain.repository.IFlagSnapshotRepository
import io.mockk.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.*

class FlagSnapshotServiceTest {
    private lateinit var flagSnapshotRepository: IFlagSnapshotRepository
    private lateinit var flagRepository: IFlagRepository
    private lateinit var flagSnapshotService: FlagSnapshotService
    
    @BeforeTest
    fun setup() {
        flagSnapshotRepository = mockk()
        flagRepository = mockk()
        flagSnapshotService = FlagSnapshotService(flagSnapshotRepository, flagRepository)
    }
    
    @Test
    fun testSaveFlagSnapshot_CreatesSnapshot_WhenFlagExists() = runBlocking {
        // Arrange
        val flagId = 1
        val updatedBy = "test-user"
        val flag = Flag(
            id = flagId,
            key = "test-flag",
            description = "Test flag",
            enabled = true,
            segments = listOf(
                Segment(
                    id = 1,
                    flagId = flagId,
                    description = "Segment 1",
                    rank = 1,
                    rolloutPercent = 50,
                    constraints = listOf(
                        Constraint(
                            id = 1,
                            segmentId = 1,
                            property = "region",
                            operator = "eq",
                            value = "US"
                        )
                    ),
                    distributions = emptyList()
                )
            ),
            variants = listOf(
                Variant(
                    id = 1,
                    flagId = flagId,
                    key = "variant-1",
                    attachment = null
                )
            ),
            tags = listOf(
                Tag(id = 1, value = "test-tag")
            )
        )
        
        val flagJson = Json.encodeToString(flag.toEvalCacheExport())
        val createdSnapshot = FlagSnapshot(
            id = 10,
            flagId = flagId,
            updatedBy = updatedBy,
            flag = flagJson
        )
        
        coEvery { flagRepository.findById(flagId) } returns flag
        coEvery { flagSnapshotRepository.create(any()) } returns createdSnapshot
        coEvery { flagRepository.update(any()) } returns flag.copy(
            updatedBy = updatedBy,
            snapshotId = createdSnapshot.id
        )
        
        // Act
        flagSnapshotService.saveFlagSnapshot(flagId, updatedBy)
        
        // Assert
        coVerify { flagRepository.findById(flagId) }
        coVerify { 
            flagSnapshotRepository.create(match {
                it.flagId == flagId && 
                it.updatedBy == updatedBy &&
                it.flag.isNotEmpty()
            })
        }
        coVerify {
            flagRepository.update(match {
                it.id == flagId &&
                it.updatedBy == updatedBy &&
                it.snapshotId == createdSnapshot.id
            })
        }
    }
    
    @Test
    fun testSaveFlagSnapshot_ThrowsException_WhenFlagNotFound() = runBlocking {
        // Arrange
        val flagId = 999
        val updatedBy = "test-user"
        
        coEvery { flagRepository.findById(flagId) } returns null
        
        // Act & Assert
        assertFailsWith<IllegalArgumentException> {
            flagSnapshotService.saveFlagSnapshot(flagId, updatedBy)
        }
        
        coVerify { flagRepository.findById(flagId) }
        coVerify(exactly = 0) { flagSnapshotRepository.create(any()) }
        coVerify(exactly = 0) { flagRepository.update(any()) }
    }
    
    @Test
    fun testSaveFlagSnapshot_CreatesSnapshot_WithNullUpdatedBy() = runBlocking {
        // Arrange
        val flagId = 1
        val flag = Flag(
            id = flagId,
            key = "test-flag",
            description = "Test flag",
            enabled = true
        )
        
        val flagJson = Json.encodeToString(flag.toEvalCacheExport())
        val createdSnapshot = FlagSnapshot(
            id = 10,
            flagId = flagId,
            updatedBy = null,
            flag = flagJson
        )
        
        coEvery { flagRepository.findById(flagId) } returns flag
        coEvery { flagSnapshotRepository.create(any()) } returns createdSnapshot
        coEvery { flagRepository.update(any()) } returns flag.copy(
            updatedBy = null,
            snapshotId = createdSnapshot.id
        )
        
        // Act
        flagSnapshotService.saveFlagSnapshot(flagId, null)
        
        // Assert
        coVerify { flagRepository.findById(flagId) }
        coVerify { 
            flagSnapshotRepository.create(match {
                it.flagId == flagId && 
                it.updatedBy == null
            })
        }
        coVerify {
            flagRepository.update(match {
                it.id == flagId &&
                it.updatedBy == null &&
                it.snapshotId == createdSnapshot.id
            })
        }
    }
    
    @Test
    fun testSaveFlagSnapshot_SerializesFullFlag_WithAllRelations() = runBlocking {
        // Arrange
        val flagId = 1
        val updatedBy = "test-user"
        val flag = Flag(
            id = flagId,
            key = "test-flag",
            description = "Test flag",
            enabled = true,
            segments = listOf(
                Segment(
                    id = 1,
                    flagId = flagId,
                    description = "Segment 1",
                    rank = 1,
                    rolloutPercent = 50,
                    constraints = listOf(
                        Constraint(
                            id = 1,
                            segmentId = 1,
                            property = "region",
                            operator = "eq",
                            value = "US"
                        )
                    ),
                    distributions = listOf(
                        Distribution(
                            id = 1,
                            segmentId = 1,
                            variantId = 1,
                            variantKey = "variant-1",
                            percent = 50
                        )
                    )
                )
            ),
            variants = listOf(
                Variant(
                    id = 1,
                    flagId = flagId,
                    key = "variant-1",
                    attachment = null
                )
            ),
            tags = listOf(
                Tag(id = 1, value = "tag1"),
                Tag(id = 2, value = "tag2")
            )
        )
        
        var capturedSnapshot: FlagSnapshot? = null
        
        coEvery { flagRepository.findById(flagId) } returns flag
        coEvery { flagSnapshotRepository.create(any()) } answers {
            capturedSnapshot = firstArg()
            FlagSnapshot(
                id = 10,
                flagId = flagId,
                updatedBy = updatedBy,
                flag = capturedSnapshot!!.flag
            )
        }
        coEvery { flagRepository.update(any()) } returns flag.copy(
            updatedBy = updatedBy,
            snapshotId = 10
        )
        
        // Act
        flagSnapshotService.saveFlagSnapshot(flagId, updatedBy)
        
        // Assert
        assertNotNull(capturedSnapshot)
        val deserializedFlag = Json.decodeFromString<EvalCacheFlagExport>(capturedSnapshot!!.flag).toFlag()
        assertEquals(flagId, deserializedFlag.id)
        assertEquals("test-flag", deserializedFlag.key)
        assertEquals(1, deserializedFlag.segments.size)
        assertEquals(1, deserializedFlag.variants.size)
        assertEquals(2, deserializedFlag.tags.size)
        assertEquals(1, deserializedFlag.segments[0].constraints.size)
        assertEquals(1, deserializedFlag.segments[0].distributions.size)
    }
    
    @Test
    fun testFindSnapshotsByFlagId_ReturnsSnapshots_WhenFlagExists() = runBlocking {
        // Arrange
        val flagId = 1
        val flag = Flag(id = flagId, key = "test-flag", description = "Test")
        val snapshots = listOf(
            FlagSnapshot(id = 1, flagId = flagId, updatedBy = "user1", flag = "{}"),
            FlagSnapshot(id = 2, flagId = flagId, updatedBy = "user2", flag = "{}")
        )
        
        coEvery { flagRepository.findById(flagId) } returns flag
        coEvery { flagSnapshotRepository.findByFlagId(flagId, null, 0, true) } returns snapshots
        
        // Act
        val result = flagSnapshotService.findSnapshotsByFlagId(flagId)
        
        // Assert
        assertEquals(2, result.size)
        coVerify { flagRepository.findById(flagId) }
        coVerify { flagSnapshotRepository.findByFlagId(flagId, null, 0, true) }
    }
    
    @Test
    fun testFindSnapshotsByFlagId_ThrowsException_WhenFlagNotFound() = runBlocking {
        // Arrange
        val flagId = 999
        
        coEvery { flagRepository.findById(flagId) } returns null
        
        // Act & Assert
        assertFailsWith<IllegalArgumentException> {
            flagSnapshotService.findSnapshotsByFlagId(flagId)
        }
        
        coVerify { flagRepository.findById(flagId) }
        coVerify(exactly = 0) { flagSnapshotRepository.findByFlagId(any(), any(), any(), any()) }
    }
    
    @Test
    fun testFindSnapshotsByFlagId_WithLimit() = runBlocking {
        val flagId = 1
        val flag = Flag(id = flagId, key = "test-flag", description = "Test")
        val snapshots = listOf(
            FlagSnapshot(id = 1, flagId = flagId, updatedBy = "user1", flag = "{}"),
            FlagSnapshot(id = 2, flagId = flagId, updatedBy = "user2", flag = "{}")
        )
        
        coEvery { flagRepository.findById(flagId) } returns flag
        coEvery { flagSnapshotRepository.findByFlagId(flagId, 2, 0, true) } returns snapshots
        
        val result = flagSnapshotService.findSnapshotsByFlagId(flagId, limit = 2)
        
        assertEquals(2, result.size)
        coVerify { flagSnapshotRepository.findByFlagId(flagId, 2, 0, true) }
    }
    
    @Test
    fun testFindSnapshotsByFlagId_WithOffset() = runBlocking {
        val flagId = 1
        val flag = Flag(id = flagId, key = "test-flag", description = "Test")
        val snapshots = listOf(
            FlagSnapshot(id = 2, flagId = flagId, updatedBy = "user2", flag = "{}")
        )
        
        coEvery { flagRepository.findById(flagId) } returns flag
        coEvery { flagSnapshotRepository.findByFlagId(flagId, null, 1, true) } returns snapshots
        
        val result = flagSnapshotService.findSnapshotsByFlagId(flagId, offset = 1)
        
        assertEquals(1, result.size)
        coVerify { flagSnapshotRepository.findByFlagId(flagId, null, 1, true) }
    }
    
    @Test
    fun testFindSnapshotsByFlagId_WithSortAsc() = runBlocking {
        val flagId = 1
        val flag = Flag(id = flagId, key = "test-flag", description = "Test")
        val snapshots = listOf(
            FlagSnapshot(id = 1, flagId = flagId, updatedBy = "user1", flag = "{}"),
            FlagSnapshot(id = 2, flagId = flagId, updatedBy = "user2", flag = "{}")
        )
        
        coEvery { flagRepository.findById(flagId) } returns flag
        coEvery { flagSnapshotRepository.findByFlagId(flagId, null, 0, false) } returns snapshots
        
        val result = flagSnapshotService.findSnapshotsByFlagId(flagId, sort = "ASC")
        
        assertEquals(2, result.size)
        coVerify { flagSnapshotRepository.findByFlagId(flagId, null, 0, false) }
    }
    
    @Test
    fun testFindSnapshotsByFlagId_WithSortDesc() = runBlocking {
        val flagId = 1
        val flag = Flag(id = flagId, key = "test-flag", description = "Test")
        val snapshots = listOf(
            FlagSnapshot(id = 2, flagId = flagId, updatedBy = "user2", flag = "{}"),
            FlagSnapshot(id = 1, flagId = flagId, updatedBy = "user1", flag = "{}")
        )
        
        coEvery { flagRepository.findById(flagId) } returns flag
        coEvery { flagSnapshotRepository.findByFlagId(flagId, null, 0, true) } returns snapshots
        
        val result = flagSnapshotService.findSnapshotsByFlagId(flagId, sort = "DESC")
        
        assertEquals(2, result.size)
        coVerify { flagSnapshotRepository.findByFlagId(flagId, null, 0, true) }
    }
    
    @Test
    fun testFindSnapshotsByFlagId_WithLimitOffsetAndSort() = runBlocking {
        val flagId = 1
        val flag = Flag(id = flagId, key = "test-flag", description = "Test")
        val snapshots = listOf(
            FlagSnapshot(id = 2, flagId = flagId, updatedBy = "user2", flag = "{}")
        )
        
        coEvery { flagRepository.findById(flagId) } returns flag
        coEvery { flagSnapshotRepository.findByFlagId(flagId, 1, 1, false) } returns snapshots
        
        val result = flagSnapshotService.findSnapshotsByFlagId(flagId, limit = 1, offset = 1, sort = "ASC")
        
        assertEquals(1, result.size)
        coVerify { flagSnapshotRepository.findByFlagId(flagId, 1, 1, false) }
    }
    
    @Test
    fun testFindSnapshotsByFlagId_ReturnsEmptyList_WhenNoSnapshots() = runBlocking {
        val flagId = 1
        val flag = Flag(id = flagId, key = "test-flag", description = "Test")
        
        coEvery { flagRepository.findById(flagId) } returns flag
        coEvery { flagSnapshotRepository.findByFlagId(flagId, null, 0, true) } returns emptyList()
        
        val result = flagSnapshotService.findSnapshotsByFlagId(flagId)
        
        assertTrue(result.isEmpty())
        coVerify { flagSnapshotRepository.findByFlagId(flagId, null, 0, true) }
    }
}
