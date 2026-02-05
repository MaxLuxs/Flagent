package flagent.service

import flagent.domain.entity.*
import flagent.domain.repository.IFlagRepository
import flagent.service.command.CreateFlagCommand
import flagent.service.command.PutFlagCommand
import io.mockk.*
import kotlinx.coroutines.runBlocking
import kotlin.test.*

class FlagServiceTest {
    private lateinit var flagRepository: IFlagRepository
    private lateinit var flagService: FlagService
    private lateinit var flagSnapshotService: FlagSnapshotService
    private lateinit var segmentService: SegmentService
    private lateinit var variantService: VariantService
    private lateinit var distributionService: DistributionService
    private lateinit var flagEntityTypeService: FlagEntityTypeService
    
    @BeforeTest
    fun setup() {
        flagRepository = mockk()
        flagSnapshotService = mockk()
        segmentService = mockk()
        variantService = mockk()
        distributionService = mockk()
        flagEntityTypeService = mockk()
        flagService = FlagService(
            flagRepository = flagRepository,
            flagSnapshotService = flagSnapshotService,
            segmentService = segmentService,
            variantService = variantService,
            distributionService = distributionService,
            flagEntityTypeService = flagEntityTypeService
        )
    }
    
    @Test
    fun testCreateFlagKey_GeneratesRandomKey_WhenKeyIsNull() {
        val key = flagService.createFlagKey(null)
        assertNotNull(key)
        assertTrue(key.startsWith("k"))
        assertEquals(17, key.length) // "k" + 16 chars
    }
    
    @Test
    fun testCreateFlagKey_GeneratesRandomKey_WhenKeyIsBlank() {
        val key = flagService.createFlagKey("")
        assertNotNull(key)
        assertTrue(key.startsWith("k"))
    }
    
    @Test
    fun testCreateFlagKey_ReturnsKey_WhenKeyIsValid() {
        val key = flagService.createFlagKey("test-key")
        assertEquals("test-key", key)
    }
    
    @Test
    fun testCreateFlagKey_ThrowsException_WhenKeyHasInvalidFormat() {
        assertFailsWith<IllegalArgumentException> {
            flagService.createFlagKey("invalid key with spaces!")
        }
    }
    
    @Test
    fun testCreateFlagKey_ThrowsException_WhenKeyIsTooLong() {
        val longKey = "a".repeat(64)
        assertFailsWith<IllegalArgumentException> {
            flagService.createFlagKey(longKey)
        }
    }

    @Test
    fun testCreateFlagKey_acceptsKeyWithDotsColonsSlashes() {
        assertEquals("feature.v2", flagService.createFlagKey("feature.v2"))
        assertEquals("ns:key", flagService.createFlagKey("ns:key"))
        assertEquals("path/to/flag", flagService.createFlagKey("path/to/flag"))
    }

    @Test
    fun testCreateFlagKey_acceptsKeyExactly63Chars() {
        val key63 = "a".repeat(63)
        assertEquals(key63, flagService.createFlagKey(key63))
    }

    @Test
    fun testFindFlags() = runBlocking {
        val flags = listOf(
            Flag(id = 1, key = "flag1", description = "Test flag 1", enabled = true),
            Flag(id = 2, key = "flag2", description = "Test flag 2", enabled = false)
        )
        
        coEvery { 
            flagRepository.findAll(
                limit = 10,
                offset = 0,
                enabled = null
            ) 
        } returns flags
        
        val result = flagService.findFlags(limit = 10, offset = 0, enabled = null)
        
        assertEquals(2, result.size)
        coVerify { 
            flagRepository.findAll(
                limit = 10,
                offset = 0,
                enabled = null
            ) 
        }
    }
    
    @Test
    fun testGetFlag() = runBlocking {
        val flag = Flag(id = 1, key = "test-flag", description = "Test", enabled = true)
        
        coEvery { flagRepository.findById(1) } returns flag
        
        val result = flagService.getFlag(1)
        
        assertNotNull(result)
        assertEquals(1, result.id)
        assertEquals("test-flag", result.key)
        coVerify { flagRepository.findById(1) }
    }
    
    @Test
    fun testGetFlag_ReturnsNull_WhenNotFound() = runBlocking {
        coEvery { flagRepository.findById(999) } returns null
        
        val result = flagService.getFlag(999)
        
        assertNull(result)
        coVerify { flagRepository.findById(999) }
    }
    
    @Test
    fun testCreateFlag_GeneratesKey_WhenKeyIsEmpty() = runBlocking {
        val command = CreateFlagCommand(key = "", description = "Test flag", template = null)
        val createdFlag = Flag(id = 1, key = "k123456789abcdef", description = "Test flag")
        
        coEvery { flagRepository.create(any()) } returns createdFlag
        coEvery { flagSnapshotService.saveFlagSnapshot(any(), any()) } just Runs
        
        val result = flagService.createFlag(command)
        
        assertNotNull(result)
        assertTrue(result.key.startsWith("k"))
        coVerify { flagRepository.create(match { it.key.startsWith("k") && it.description == "Test flag" }) }
    }
    
    @Test
    fun testCreateFlag_UsesProvidedKey_WhenKeyIsValid() = runBlocking {
        val command = CreateFlagCommand(key = "my-flag", description = "Test flag", template = null)
        val createdFlag = Flag(id = 1, key = "my-flag", description = "Test flag")
        
        coEvery { flagRepository.create(any()) } returns createdFlag
        coEvery { flagSnapshotService.saveFlagSnapshot(any(), any()) } just Runs
        
        val result = flagService.createFlag(command)
        
        assertEquals("my-flag", result.key)
        coVerify { flagRepository.create(match { it.key == "my-flag" }) }
    }
    
    @Test
    fun testUpdateFlag() = runBlocking {
        val existingFlag = Flag(id = 1, key = "test-flag", description = "Original")
        val updatedFlag = existingFlag.copy(description = "Updated description")
        
        coEvery { flagRepository.findById(1) } returns existingFlag
        coEvery { flagRepository.update(any()) } returns updatedFlag
        coEvery { flagSnapshotService.saveFlagSnapshot(any(), any()) } just Runs
        
        val command = PutFlagCommand(
            description = "Updated description",
            key = null,
            dataRecordsEnabled = null,
            entityType = null,
            notes = null
        )
        val result = flagService.updateFlag(1, command)
        
        assertEquals("Updated description", result.description)
        coVerify { flagRepository.update(any()) }
    }
    
    @Test
    fun testDeleteFlag() = runBlocking {
        val flag = Flag(id = 1, key = "test-flag", description = "Test")
        coEvery { flagRepository.findById(1) } returns flag
        coEvery { flagRepository.delete(1) } just Runs

        flagService.deleteFlag(1)

        coVerify { flagRepository.findById(1) }
        coVerify { flagRepository.delete(1) }
    }
    
    @Test
    fun testRestoreFlag() = runBlocking {
        val flag = Flag(id = 1, key = "test-flag", description = "Test")
        
        coEvery { flagRepository.restore(1) } returns flag
        
        val result = flagService.restoreFlag(1)
        
        assertNotNull(result)
        assertEquals(1, result.id)
        coVerify { flagRepository.restore(1) }
    }
    
    @Test
    fun testSetFlagEnabled() = runBlocking {
        val flag = Flag(id = 1, key = "test-flag", description = "Test", enabled = false)
        val enabledFlag = flag.copy(enabled = true)
        
        coEvery { flagRepository.findById(1) } returns flag
        coEvery { flagRepository.update(any()) } returns enabledFlag
        coEvery { flagSnapshotService.saveFlagSnapshot(any(), any()) } just Runs
        
        val result = flagService.setFlagEnabled(1, true)
        
        assertNotNull(result)
        assertTrue(result.enabled == true)
        coVerify { flagRepository.findById(1) }
        coVerify { flagRepository.update(match { it.enabled == true }) }
    }
    
    @Test
    fun testSetFlagEnabled_ReturnsNull_WhenFlagNotFound() = runBlocking {
        coEvery { flagRepository.findById(999) } returns null
        
        val result = flagService.setFlagEnabled(999, true)
        
        assertNull(result)
        coVerify { flagRepository.findById(999) }
        coVerify(exactly = 0) { flagRepository.update(any()) }
    }
    
    
    @Test
    fun testCreateFlagKey_WithValidSpecialCharacters() {
        val validKeys = listOf(
            "test-key",
            "test_key",
            "test.key",
            "test/key",
            "test:key",
            "test123",
            "test-key_123"
        )
        
        validKeys.forEach { key ->
            val result = flagService.createFlagKey(key)
            assertEquals(key, result)
        }
    }
    
    @Test
    fun testCreateFlagKey_ThrowsException_WhenKeyHasInvalidCharacters() {
        val invalidKeys = listOf(
            "test key", // spaces
            "test@key", // @ symbol
            "test#key", // # symbol
            "test\$key", // $ symbol
            "test%key"  // % symbol
        )
        
        invalidKeys.forEach { key ->
            assertFailsWith<IllegalArgumentException> {
                flagService.createFlagKey(key)
            }
        }
    }
    
    @Test
    fun testCreateFlagKey_ThrowsException_WhenKeyIsExactly63Chars() {
        val key63 = "a".repeat(63)
        val result = flagService.createFlagKey(key63)
        assertEquals(key63, result)
    }
    
    @Test
    fun testCreateFlagKey_ThrowsException_WhenKeyIs64Chars() {
        val key64 = "a".repeat(64)
        assertFailsWith<IllegalArgumentException> {
            flagService.createFlagKey(key64)
        }
    }
    
    @Test
    fun testFindFlags_WithAllFilters() = runBlocking {
        val flags = listOf(
            Flag(id = 1, key = "flag1", description = "Test flag 1", enabled = true)
        )
        
        coEvery { 
            flagRepository.findAll(
                limit = 20,
                offset = 10,
                enabled = true,
                description = "Test",
                key = "flag1",
                descriptionLike = "Test%",
                preload = true,
                deleted = false,
                tags = "tag1"
            ) 
        } returns flags
        
        val result = flagService.findFlags(
            limit = 20,
            offset = 10,
            enabled = true,
            description = "Test",
            key = "flag1",
            descriptionLike = "Test%",
            preload = true,
            deleted = false,
            tags = "tag1"
        )
        
        assertEquals(1, result.size)
        coVerify { 
            flagRepository.findAll(
                limit = 20,
                offset = 10,
                enabled = true,
                description = "Test",
                key = "flag1",
                descriptionLike = "Test%",
                preload = true,
                deleted = false,
                tags = "tag1"
            ) 
        }
    }
    
    @Test
    fun testCreateFlag_WithSimpleBooleanFlagTemplate() = runBlocking {
        val command = CreateFlagCommand(key = "my-flag", description = "Test flag", template = "simple_boolean_flag")
        val createdFlag = Flag(id = 1, key = "my-flag", description = "Test flag")
        val segment = Segment(id = 1, flagId = 1, rank = SegmentService.SegmentDefaultRank, rolloutPercent = 100)
        val variant = Variant(id = 1, flagId = 1, key = "on")
        
        coEvery { flagRepository.create(any()) } returns createdFlag
        coEvery { segmentService.createSegment(any(), any()) } returns segment
        coEvery { variantService.createVariant(any(), any()) } returns variant
        coEvery { distributionService.updateDistributions(any(), any()) } just Runs
        coEvery { flagSnapshotService.saveFlagSnapshot(any(), any()) } just Runs
        
        val result = flagService.createFlag(command, updatedBy = "test-user")
        
        assertEquals("my-flag", result.key)
        coVerify { flagRepository.create(any()) }
        coVerify { segmentService.createSegment(any(), "test-user") }
        coVerify { variantService.createVariant(any(), "test-user") }
        coVerify { distributionService.updateDistributions(any(), "test-user") }
        coVerify { flagSnapshotService.saveFlagSnapshot(1, "test-user") }
    }
    
    @Test
    fun testCreateFlag_ThrowsException_WhenUnknownTemplate() = runBlocking {
        val command = CreateFlagCommand(key = "my-flag", description = "Test flag", template = "unknown_template")
        val createdFlag = Flag(id = 1, key = "my-flag", description = "Test flag")
        
        coEvery { flagRepository.create(any()) } returns createdFlag
        
        assertFailsWith<IllegalArgumentException> {
            flagService.createFlag(command)
        }
    }
    
    @Test
    fun testCreateFlag_SavesSnapshot_WhenFlagSnapshotServiceProvided() = runBlocking {
        val command = CreateFlagCommand(key = "my-flag", description = "Test flag", template = null)
        val createdFlag = Flag(id = 1, key = "my-flag", description = "Test flag")
        
        coEvery { flagRepository.create(any()) } returns createdFlag
        coEvery { flagSnapshotService.saveFlagSnapshot(any(), any()) } just Runs
        
        val result = flagService.createFlag(command, updatedBy = "test-user")
        
        assertEquals("my-flag", result.key)
        coVerify { flagSnapshotService.saveFlagSnapshot(1, "test-user") }
    }
    
    @Test
    fun testUpdateFlag_WithUpdatedBy() = runBlocking {
        val existingFlag = Flag(id = 1, key = "test-flag", description = "Original")
        val updatedFlag = existingFlag.copy(description = "Updated description", updatedBy = "test-user")
        
        coEvery { flagRepository.findById(1) } returns existingFlag
        coEvery { flagRepository.update(any()) } returns updatedFlag
        coEvery { flagSnapshotService.saveFlagSnapshot(any(), any()) } just Runs
        
        val command = PutFlagCommand(
            description = "Updated description",
            key = null,
            dataRecordsEnabled = null,
            entityType = null,
            notes = null
        )
        val result = flagService.updateFlag(1, command, updatedBy = "test-user")
        
        assertEquals("Updated description", result.description)
        assertEquals("test-user", result.updatedBy)
        coVerify { flagRepository.update(match { it.updatedBy == "test-user" }) }
        coVerify { flagSnapshotService.saveFlagSnapshot(1, "test-user") }
    }
    
    @Test
    fun testUpdateFlag_WithEntityType() = runBlocking {
        val existingFlag = Flag(id = 1, key = "test-flag", description = "Test")
        val updatedFlag = existingFlag.copy(entityType = "user", updatedBy = "test-user")
        
        coEvery { flagRepository.findById(1) } returns existingFlag
        coEvery { flagRepository.update(any()) } returns updatedFlag
        coEvery { flagEntityTypeService.createOrGet("user") } returns FlagEntityType(id = 1, key = "user")
        coEvery { flagSnapshotService.saveFlagSnapshot(any(), any()) } just Runs
        
        val command = PutFlagCommand(
            description = null,
            key = null,
            dataRecordsEnabled = null,
            entityType = "user",
            notes = null
        )
        val result = flagService.updateFlag(1, command, updatedBy = "test-user")
        
        assertNotNull(result)
        coVerify { flagEntityTypeService.createOrGet("user") }
        coVerify { flagSnapshotService.saveFlagSnapshot(1, "test-user") }
    }
    
    @Test
    fun testUpdateFlag_DoesNotCreateEntityType_WhenEmpty() = runBlocking {
        val existingFlag = Flag(id = 1, key = "test-flag", description = "Test", entityType = "")
        val updatedFlag = existingFlag.copy(updatedBy = "test-user")
        
        coEvery { flagRepository.findById(1) } returns existingFlag
        coEvery { flagRepository.update(any()) } returns updatedFlag
        coEvery { flagSnapshotService.saveFlagSnapshot(any(), any()) } just Runs
        
        val command = PutFlagCommand(
            description = null,
            key = null,
            dataRecordsEnabled = null,
            entityType = "",
            notes = null
        )
        val result = flagService.updateFlag(1, command, updatedBy = "test-user")
        
        assertNotNull(result)
        coVerify(exactly = 0) { flagEntityTypeService.createOrGet(any()) }
    }
    
    @Test
    fun testSetFlagEnabled_WithUpdatedBy() = runBlocking {
        val flag = Flag(id = 1, key = "test-flag", description = "Test", enabled = false)
        val enabledFlag = flag.copy(enabled = true, updatedBy = "test-user")
        
        coEvery { flagRepository.findById(1) } returns flag
        coEvery { flagRepository.update(any()) } returns enabledFlag
        coEvery { flagSnapshotService.saveFlagSnapshot(any(), any()) } just Runs
        
        val result = flagService.setFlagEnabled(1, true, updatedBy = "test-user")
        
        assertNotNull(result)
        assertTrue(result.enabled)
        assertEquals("test-user", result.updatedBy)
        coVerify { flagSnapshotService.saveFlagSnapshot(1, "test-user") }
    }
    
    @Test
    fun testSetFlagEnabled_SavesSnapshot_WhenFlagSnapshotServiceProvided() = runBlocking {
        val flag = Flag(id = 1, key = "test-flag", description = "Test", enabled = false)
        val enabledFlag = flag.copy(enabled = true)
        
        coEvery { flagRepository.findById(1) } returns flag
        coEvery { flagRepository.update(any()) } returns enabledFlag
        coEvery { flagSnapshotService.saveFlagSnapshot(any(), any()) } just Runs
        
        val result = flagService.setFlagEnabled(1, true, updatedBy = "test-user")
        
        assertNotNull(result)
        coVerify { flagSnapshotService.saveFlagSnapshot(1, "test-user") }
    }
    
    @Test
    fun testUpdateFlag_ValidatesKey_WhenKeyNotEmpty() = runBlocking {
        val existingFlag = Flag(id = 1, key = "valid-key", description = "Test")
        val updatedFlag = existingFlag.copy()
        
        coEvery { flagRepository.findById(1) } returns existingFlag
        coEvery { flagRepository.update(any()) } returns updatedFlag
        coEvery { flagSnapshotService.saveFlagSnapshot(any(), any()) } just Runs
        
        val command = PutFlagCommand(
            description = null,
            key = "valid-key",
            dataRecordsEnabled = null,
            entityType = null,
            notes = null
        )
        val result = flagService.updateFlag(1, command)
        
        assertNotNull(result)
        coVerify { flagRepository.update(match { it.key == "valid-key" }) }
    }
    
    @Test
    fun testUpdateFlag_ThrowsException_WhenKeyIsInvalid() = runBlocking {
        val existingFlag = Flag(id = 1, key = "test-flag", description = "Test")
        coEvery { flagRepository.findById(1) } returns existingFlag
        
        val command = PutFlagCommand(
            description = null,
            key = "invalid key with spaces",
            dataRecordsEnabled = null,
            entityType = null,
            notes = null
        )
        assertFailsWith<IllegalArgumentException> {
            flagService.updateFlag(1, command)
        }
        
        coVerify(exactly = 0) { flagRepository.update(any()) }
    }
    
    @Test
    fun testCountFlags() = runBlocking {
        coEvery {
            flagRepository.countAll(
                enabled = null,
                description = null,
                key = null,
                descriptionLike = null,
                deleted = false,
                tags = null
            )
        } returns 42L
        
        val result = flagService.countFlags()
        
        assertEquals(42L, result)
        coVerify {
            flagRepository.countAll(
                enabled = null,
                description = null,
                key = null,
                descriptionLike = null,
                deleted = false,
                tags = null
            )
        }
    }
    
    @Test
    fun testCountFlags_WithFilters() = runBlocking {
        coEvery {
            flagRepository.countAll(
                enabled = true,
                description = null,
                key = null,
                descriptionLike = "payments",
                deleted = false,
                tags = "team:payments"
            )
        } returns 5L
        
        val result = flagService.countFlags(
            enabled = true,
            descriptionLike = "payments",
            tags = "team:payments"
        )
        
        assertEquals(5L, result)
        coVerify {
            flagRepository.countAll(
                enabled = true,
                description = null,
                key = null,
                descriptionLike = "payments",
                deleted = false,
                tags = "team:payments"
            )
        }
    }
    
    @Test
    fun testBatchSetEnabled() = runBlocking {
        val flag1 = Flag(id = 1, key = "flag1", description = "Test", enabled = false)
        val flag2 = Flag(id = 2, key = "flag2", description = "Test", enabled = false)
        val enabled1 = flag1.copy(enabled = true)
        val enabled2 = flag2.copy(enabled = true)
        
        coEvery { flagRepository.findById(1) } returns flag1
        coEvery { flagRepository.findById(2) } returns flag2
        coEvery { flagRepository.update(match { it.id == 1 && it.enabled }) } returns enabled1
        coEvery { flagRepository.update(match { it.id == 2 && it.enabled }) } returns enabled2
        coEvery { flagSnapshotService.saveFlagSnapshot(any(), any()) } just Runs
        
        val result = flagService.batchSetEnabled(listOf(1, 2), true, "test-user")
        
        assertEquals(2, result.size)
        assertTrue(result.all { it.enabled })
        coVerify { flagRepository.update(any()) }
        coVerify(exactly = 2) { flagSnapshotService.saveFlagSnapshot(any(), any()) }
    }
    
    @Test
    fun testBatchSetEnabled_SkipsNotFound() = runBlocking {
        val flag1 = Flag(id = 1, key = "flag1", description = "Test", enabled = false)
        val enabled1 = flag1.copy(enabled = true)
        
        coEvery { flagRepository.findById(1) } returns flag1
        coEvery { flagRepository.findById(999) } returns null
        coEvery { flagRepository.update(match { it.id == 1 && it.enabled }) } returns enabled1
        coEvery { flagSnapshotService.saveFlagSnapshot(any(), any()) } just Runs
        
        val result = flagService.batchSetEnabled(listOf(1, 999), true, "test-user")
        
        assertEquals(1, result.size)
        assertEquals(1, result[0].id)
        assertTrue(result[0].enabled)
        coVerify(exactly = 1) { flagRepository.update(any()) }
    }
}
