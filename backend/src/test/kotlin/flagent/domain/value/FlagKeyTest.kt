package flagent.domain.value

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class FlagKeyTest {
    @Test
    fun `create FlagKey with valid value`() {
        val flagKey = FlagKey("test_flag")
        assertEquals("test_flag", flagKey.value)
        assertEquals("test_flag", flagKey.toString())
    }
    
    @Test
    fun `create FlagKey with alphanumeric characters`() {
        val flagKey = FlagKey("flag123")
        assertEquals("flag123", flagKey.value)
    }
    
    @Test
    fun `create FlagKey with underscores`() {
        val flagKey = FlagKey("test_flag_key")
        assertEquals("test_flag_key", flagKey.value)
    }
    
    @Test
    fun `FlagKey throws exception for blank string`() {
        assertFailsWith<IllegalArgumentException> {
            FlagKey("")
        }
    }
    
    @Test
    fun `FlagKey throws exception for whitespace only`() {
        assertFailsWith<IllegalArgumentException> {
            FlagKey("   ")
        }
    }
    
    @Test
    fun `FlagKey value class preserves equality`() {
        val key1 = FlagKey("test_flag")
        val key2 = FlagKey("test_flag")
        assertEquals(key1.value, key2.value)
        assertEquals(key1.toString(), key2.toString())
    }
}
