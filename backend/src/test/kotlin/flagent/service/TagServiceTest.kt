package flagent.service

import flagent.domain.entity.Flag
import flagent.domain.entity.Tag
import flagent.domain.repository.IFlagRepository
import flagent.domain.repository.ITagRepository
import flagent.service.command.CreateTagCommand
import io.mockk.*
import kotlinx.coroutines.runBlocking
import kotlin.test.*

class TagServiceTest {
    private lateinit var tagRepository: ITagRepository
    private lateinit var flagRepository: IFlagRepository
    private lateinit var flagSnapshotService: FlagSnapshotService
    private lateinit var tagService: TagService
    
    @BeforeTest
    fun setup() {
        tagRepository = mockk()
        flagRepository = mockk()
        flagSnapshotService = mockk()
        tagService = TagService(tagRepository, flagRepository, flagSnapshotService)
    }
    
    @Test
    fun testFindTagsByFlagId() = runBlocking {
        val flag = Flag(id = 1, key = "test-flag", description = "Test")
        val tags = listOf(
            Tag(id = 1, value = "tag1"),
            Tag(id = 2, value = "tag2")
        )
        
        coEvery { flagRepository.findById(1) } returns flag
        coEvery { tagRepository.findByFlagId(1) } returns tags
        
        val result = tagService.findTagsByFlagId(1)
        
        assertEquals(2, result.size)
        coVerify { flagRepository.findById(1) }
        coVerify { tagRepository.findByFlagId(1) }
    }
    
    @Test
    fun testFindTagsByFlagId_ThrowsException_WhenFlagNotFound() = runBlocking {
        coEvery { flagRepository.findById(1) } returns null
        
        assertFailsWith<IllegalArgumentException> {
            runBlocking { tagService.findTagsByFlagId(1) }
        }
        
        coVerify(exactly = 0) { tagRepository.findByFlagId(any()) }
    }
    
    @Test
    fun testFindAllTags() = runBlocking {
        val tags = listOf(
            Tag(id = 1, value = "tag1"),
            Tag(id = 2, value = "tag2")
        )
        
        coEvery { tagRepository.findAll(any(), any(), any()) } returns tags
        
        val result = tagService.findAllTags(limit = 10, offset = 0, valueLike = null)
        
        assertEquals(2, result.size)
        coVerify { tagRepository.findAll(10, 0, null) }
    }
    
    @Test
    fun testCreateTag_ThrowsException_WhenFlagNotFound() = runBlocking {
        coEvery { flagRepository.findById(1) } returns null
        
        assertFailsWith<IllegalArgumentException> {
            runBlocking { tagService.createTag(CreateTagCommand(flagId = 1, value = "new-tag")) }
        }
    }
    
    @Test
    fun testCreateTag_ThrowsException_WhenValueIsEmpty() = runBlocking {
        val flag = Flag(id = 1, key = "test-flag", description = "Test")
        
        coEvery { flagRepository.findById(1) } returns flag
        
        assertFailsWith<IllegalArgumentException> {
            runBlocking { tagService.createTag(CreateTagCommand(flagId = 1, value = "")) }
        }
    }
    
    @Test
    fun testCreateTag_ThrowsException_WhenValueIsTooLong() = runBlocking {
        val flag = Flag(id = 1, key = "test-flag", description = "Test")
        val longValue = "a".repeat(64)
        
        coEvery { flagRepository.findById(1) } returns flag
        
        assertFailsWith<IllegalArgumentException> {
            runBlocking { tagService.createTag(CreateTagCommand(flagId = 1, value = longValue)) }
        }
    }
    
    @Test
    fun testCreateTag_CreatesNewTag_WhenTagDoesNotExist() = runBlocking {
        val flag = Flag(id = 1, key = "test-flag", description = "Test")
        val newTag = Tag(id = 1, value = "new-tag")
        
        coEvery { flagRepository.findById(1) } returns flag
        coEvery { tagRepository.findByValue("new-tag") } returns null
        coEvery { tagRepository.create(any()) } returns newTag
        coEvery { tagRepository.addTagToFlag(1, 1) } just Runs
        coEvery { flagSnapshotService.saveFlagSnapshot(any(), any()) } just Runs
        
        val result = tagService.createTag(CreateTagCommand(flagId = 1, value = "new-tag"))
        
        assertEquals("new-tag", result.value)
        coVerify { tagRepository.create(match { it.value == "new-tag" }) }
        coVerify { tagRepository.addTagToFlag(1, 1) }
        coVerify { flagSnapshotService.saveFlagSnapshot(1, null) }
    }
    
    @Test
    fun testCreateTag_UsesExistingTag_WhenTagExists() = runBlocking {
        val flag = Flag(id = 1, key = "test-flag", description = "Test")
        val existingTag = Tag(id = 1, value = "existing-tag")
        
        coEvery { flagRepository.findById(1) } returns flag
        coEvery { tagRepository.findByValue("existing-tag") } returns existingTag
        coEvery { tagRepository.addTagToFlag(1, 1) } just Runs
        coEvery { flagSnapshotService.saveFlagSnapshot(any(), any()) } just Runs
        
        val result = tagService.createTag(CreateTagCommand(flagId = 1, value = "existing-tag"))
        
        assertEquals("existing-tag", result.value)
        coVerify(exactly = 0) { tagRepository.create(any()) }
        coVerify { tagRepository.addTagToFlag(1, 1) }
        coVerify { flagSnapshotService.saveFlagSnapshot(1, null) }
    }
    
    @Test
    fun testCreateTag_WithUpdatedBy() = runBlocking {
        val flag = Flag(id = 1, key = "test-flag", description = "Test")
        val newTag = Tag(id = 1, value = "new-tag")
        
        coEvery { flagRepository.findById(1) } returns flag
        coEvery { tagRepository.findByValue("new-tag") } returns null
        coEvery { tagRepository.create(any()) } returns newTag
        coEvery { tagRepository.addTagToFlag(1, 1) } just Runs
        coEvery { flagSnapshotService.saveFlagSnapshot(any(), any()) } just Runs
        
        val result = tagService.createTag(CreateTagCommand(flagId = 1, value = "new-tag"), updatedBy = "test-user")
        
        assertEquals("new-tag", result.value)
        coVerify { flagSnapshotService.saveFlagSnapshot(1, "test-user") }
    }
    
    @Test
    fun testCreateTag_ThrowsException_WhenValueHasInvalidFormat() = runBlocking {
        val flag = Flag(id = 1, key = "test-flag", description = "Test")
        
        coEvery { flagRepository.findById(1) } returns flag
        
        assertFailsWith<IllegalArgumentException> {
            runBlocking { tagService.createTag(CreateTagCommand(flagId = 1, value = "invalid@tag")) }
        }
    }
    
    @Test
    fun testCreateTag_AcceptsValidSpecialCharacters() = runBlocking {
        val flag = Flag(id = 1, key = "test-flag", description = "Test")
        val validValues = listOf("tag-key", "tag_key", "tag.key", "tag:key", "tag/key", "tag123")
        
        coEvery { flagRepository.findById(1) } returns flag
        
        validValues.forEach { value ->
            val newTag = Tag(id = 1, value = value)
            coEvery { tagRepository.findByValue(value) } returns null
            coEvery { tagRepository.create(any()) } returns newTag
            coEvery { tagRepository.addTagToFlag(1, 1) } just Runs
            coEvery { flagSnapshotService.saveFlagSnapshot(any(), any()) } just Runs
            
            val result = tagService.createTag(CreateTagCommand(flagId = 1, value = value))
            assertEquals(value, result.value)
        }
    }
    
    @Test
    fun testDeleteTag_ThrowsException_WhenFlagNotFound() = runBlocking {
        coEvery { flagRepository.findById(1) } returns null
        
        assertFailsWith<IllegalArgumentException> {
            runBlocking { tagService.deleteTag(1, 1) }
        }
    }
    
    @Test
    fun testDeleteTag_Success() = runBlocking {
        val flag = Flag(id = 1, key = "test-flag", description = "Test")
        
        coEvery { flagRepository.findById(1) } returns flag
        coEvery { tagRepository.removeTagFromFlag(1, 1) } just Runs
        coEvery { flagSnapshotService.saveFlagSnapshot(any(), any()) } just Runs
        
        tagService.deleteTag(1, 1)
        
        coVerify { flagRepository.findById(1) }
        coVerify { tagRepository.removeTagFromFlag(1, 1) }
        coVerify { flagSnapshotService.saveFlagSnapshot(1, null) }
    }
    
    @Test
    fun testDeleteTag_WithUpdatedBy() = runBlocking {
        val flag = Flag(id = 1, key = "test-flag", description = "Test")
        
        coEvery { flagRepository.findById(1) } returns flag
        coEvery { tagRepository.removeTagFromFlag(1, 1) } just Runs
        coEvery { flagSnapshotService.saveFlagSnapshot(any(), any()) } just Runs
        
        tagService.deleteTag(1, 1, updatedBy = "test-user")
        
        coVerify { flagSnapshotService.saveFlagSnapshot(1, "test-user") }
    }
    
    @Test
    fun testFindAllTags_WithValueLike() = runBlocking {
        val tags = listOf(
            Tag(id = 1, value = "production-tag"),
            Tag(id = 2, value = "production-backend")
        )
        
        coEvery { tagRepository.findAll(10, 0, "production") } returns tags
        
        val result = tagService.findAllTags(limit = 10, offset = 0, valueLike = "production")
        
        assertEquals(2, result.size)
        coVerify { tagRepository.findAll(10, 0, "production") }
    }
    
    @Test
    fun testFindAllTags_WithLimitAndOffset() = runBlocking {
        val tags = listOf(Tag(id = 2, value = "tag2"))
        
        coEvery { tagRepository.findAll(5, 5, null) } returns tags
        
        val result = tagService.findAllTags(limit = 5, offset = 5, valueLike = null)
        
        assertEquals(1, result.size)
        coVerify { tagRepository.findAll(5, 5, null) }
    }
}
