package flagent.domain.entity

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UserTest {
    @Test
    fun `create User with all fields`() {
        val user = User(
            id = 1,
            email = "user@example.com",
            name = "Test User"
        )
        
        assertEquals(1, user.id)
        assertEquals("user@example.com", user.email)
        assertEquals("Test User", user.name)
        assertFalse(user.isBlocked)
        assertFalse(user.isDeleted)
    }
    
    @Test
    fun `create User with default values`() {
        val user = User()
        
        assertEquals(0, user.id)
        assertNull(user.email)
        assertNull(user.name)
    }
    
    @Test
    fun `create User with null email`() {
        val user = User(id = 1, email = null)
        
        assertEquals(1, user.id)
        assertNull(user.email)
    }
    
    @Test
    fun `User isBlocked when blockedAt set`() {
        val user = User(id = 1, email = "a@b.com", blockedAt = java.time.LocalDateTime.now())
        assertTrue(user.isBlocked)
    }
    
    @Test
    fun `User data class equality`() {
        val user1 = User(id = 1, email = "user@example.com")
        val user2 = User(id = 1, email = "user@example.com")
        
        assertEquals(user1, user2)
        assertEquals(user1.hashCode(), user2.hashCode())
    }
    
    @Test
    fun `User with different emails are not equal`() {
        val user1 = User(id = 1, email = "user1@example.com")
        val user2 = User(id = 1, email = "user2@example.com")
        
        assert(!user1.equals(user2)) { "Users with different emails should not be equal" }
    }
}
