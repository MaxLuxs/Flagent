package flagent.middleware

import io.ktor.server.auth.Principal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuthPrincipalsTest {

    @Test
    fun `UserPrincipal holds tenantId and is Principal`() {
        val p = UserPrincipal(tenantId = 123L)
        assertEquals(123L, p.tenantId)
        assertTrue(p is Principal)
    }
}
