package flagent.domain.entity

import kotlin.test.Test
import kotlin.test.assertEquals

class TagTest {
    @Test
    fun `create Tag with all fields`() {
        val tag = Tag(
            id = 1,
            value = "production"
        )
        
        assertEquals(1, tag.id)
        assertEquals("production", tag.value)
    }
    
    @Test
    fun `create Tag with default id`() {
        val tag = Tag(value = "staging")
        
        assertEquals(0, tag.id)
        assertEquals("staging", tag.value)
    }
    
    @Test
    fun `Tag data class equality`() {
        val tag1 = Tag(id = 1, value = "production")
        val tag2 = Tag(id = 1, value = "production")
        
        assertEquals(tag1, tag2)
        assertEquals(tag1.hashCode(), tag2.hashCode())
    }
    
    @Test
    fun `Tag with different values are not equal`() {
        val tag1 = Tag(id = 1, value = "production")
        val tag2 = Tag(id = 1, value = "staging")
        
        assert(!tag1.equals(tag2)) { "Tags with different values should not be equal" }
    }
}
