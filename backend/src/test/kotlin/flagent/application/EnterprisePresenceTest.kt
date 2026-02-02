package flagent.application

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class EnterprisePresenceTest {

    @AfterEach
    fun reset() {
        EnterprisePresence.enterpriseEnabled = false
    }

    @Test
    fun `enterpriseEnabled defaults to false`() {
        EnterprisePresence.enterpriseEnabled = false
        assertEquals(false, EnterprisePresence.enterpriseEnabled)
    }

    @Test
    fun `enterpriseEnabled can be set to true`() {
        EnterprisePresence.enterpriseEnabled = true
        assertEquals(true, EnterprisePresence.enterpriseEnabled)
    }
}
