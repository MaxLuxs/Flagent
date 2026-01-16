package flagent.service

import flagent.domain.entity.FlagEntityType
import flagent.domain.repository.IFlagEntityTypeRepository
import io.mockk.*
import kotlinx.coroutines.runBlocking
import kotlin.test.*

class FlagEntityTypeServiceTest {
    private lateinit var repository: IFlagEntityTypeRepository
    private lateinit var service: FlagEntityTypeService
    
    @BeforeTest
    fun setup() {
        repository = mockk()
        service = FlagEntityTypeService(repository)
    }
    
    @Test
    fun testFindAllEntityTypes() = runBlocking {
        val entityTypes = listOf(
            FlagEntityType(id = 1, key = "user"),
            FlagEntityType(id = 2, key = "session")
        )
        
        coEvery { repository.findAll() } returns entityTypes
        
        val result = service.findAllEntityTypes()
        
        assertEquals(2, result.size)
        assertEquals("user", result[0])
        assertEquals("session", result[1])
        coVerify { repository.findAll() }
    }
    
    @Test
    fun testCreateOrGet_CreatesNew_WhenNotExists() = runBlocking {
        coEvery { repository.findByKey("user") } returns null
        coEvery { repository.create(any()) } returns FlagEntityType(id = 1, key = "user")
        
        val result = service.createOrGet("user")
        
        assertEquals("user", result.key)
        coVerify { repository.findByKey("user") }
        coVerify { repository.create(match { it.key == "user" }) }
    }
    
    @Test
    fun testCreateOrGet_ReturnsExisting_WhenExists() = runBlocking {
        val existing = FlagEntityType(id = 1, key = "user")
        coEvery { repository.findByKey("user") } returns existing
        
        val result = service.createOrGet("user")
        
        assertEquals(existing, result)
        coVerify { repository.findByKey("user") }
        coVerify(exactly = 0) { repository.create(any()) }
    }
    
    @Test
    fun testCreateOrGet_ThrowsException_WhenKeyIsEmpty() = runBlocking {
        assertFailsWith<IllegalArgumentException> {
            service.createOrGet("")
        }
    }
    
    @Test
    fun testCreateOrGet_ThrowsException_WhenKeyHasInvalidFormat() = runBlocking {
        assertFailsWith<IllegalArgumentException> {
            service.createOrGet("invalid key with spaces!")
        }
    }
    
    @Test
    fun testCreateOrGet_ThrowsException_WhenKeyIsTooLong() = runBlocking {
        val longKey = "a".repeat(64)
        assertFailsWith<IllegalArgumentException> {
            service.createOrGet(longKey)
        }
    }
}
