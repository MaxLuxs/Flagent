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
    
    @Test
    fun testCreateOrGet_AcceptsValidKeys() = runBlocking {
        val validKeys = listOf("user", "session", "device", "user-type", "user_type", "user.type", "user:type", "user/type", "user123")
        
        validKeys.forEach { key ->
            coEvery { repository.findByKey(key) } returns null
            coEvery { repository.create(any()) } returns FlagEntityType(id = 1, key = key)
            
            val result = service.createOrGet(key)
            
            assertEquals(key, result.key)
            coVerify { repository.findByKey(key) }
            coVerify { repository.create(match { it.key == key }) }
        }
    }
    
    @Test
    fun testCreateOrGet_AcceptsKeyAtMaxLength() = runBlocking {
        val key63 = "a".repeat(63)
        
        coEvery { repository.findByKey(key63) } returns null
        coEvery { repository.create(any()) } returns FlagEntityType(id = 1, key = key63)
        
        val result = service.createOrGet(key63)
        
        assertEquals(key63, result.key)
    }
    
    @Test
    fun testCreateOrGet_ThrowsException_WhenKeyHasSpaces() = runBlocking {
        assertFailsWith<IllegalArgumentException> {
            service.createOrGet("user type")
        }
    }
    
    @Test
    fun testCreateOrGet_ThrowsException_WhenKeyHasSpecialInvalidCharacters() = runBlocking {
        val invalidKeys = listOf("user@type", "user#type", "user\$type", "user%type", "user&type")
        
        invalidKeys.forEach { key ->
            assertFailsWith<IllegalArgumentException> {
                service.createOrGet(key)
            }
        }
    }
    
    @Test
    fun testFindAllEntityTypes_ReturnsEmptyList_WhenNoTypes() = runBlocking {
        coEvery { repository.findAll() } returns emptyList()
        
        val result = service.findAllEntityTypes()
        
        assertTrue(result.isEmpty())
        coVerify { repository.findAll() }
    }
}
