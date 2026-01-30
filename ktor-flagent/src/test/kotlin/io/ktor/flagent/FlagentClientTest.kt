package io.ktor.flagent

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FlagentClientTest {

    @Test
    fun `FlagentException message and cause`() {
        val cause = RuntimeException("network error")
        val ex = FlagentException("eval failed", cause)
        assertEquals("eval failed", ex.message)
        assertEquals(cause, ex.cause)
    }

    @Test
    fun `FlagentException without cause`() {
        val ex = FlagentException("simple error")
        assertEquals("simple error", ex.message)
        assertTrue(ex.cause == null)
    }
}
