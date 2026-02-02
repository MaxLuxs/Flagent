package flagent.application

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import kotlin.test.assertNull

class LicenseStateTest {

    @AfterEach
    fun reset() {
        LicenseState.licenseValid = null
    }

    @Test
    fun `licenseValid can be null when no license configured`() {
        LicenseState.licenseValid = null
        assertNull(LicenseState.licenseValid)
    }

    @Test
    fun `licenseValid can be true when license is valid`() {
        LicenseState.licenseValid = true
        assert(LicenseState.licenseValid == true)
    }

    @Test
    fun `licenseValid can be false when license is invalid or expired`() {
        LicenseState.licenseValid = false
        assert(LicenseState.licenseValid == false)
    }
}
