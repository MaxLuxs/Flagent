package flagent.domain.entity

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class UserTest {
    @Test
    fun `create User with all fields`() {
        val user = User(
            id = 1,
            email = "user@example.com"
        )
        
        assertEquals(1, user.id)
        assertEquals("user@example.com", user.email)
    }
    
    @Test
    fun `create User with default values`() {
        val user = User()
        
        assertEquals(0, user.id)
        assertNull(user.email)
    }
    
    @Test
    fun `create User with null email`() {
        val user = User(id = 1, email = null)
        
        assertEquals(1, user.id)
        assertNull(user.email)
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
