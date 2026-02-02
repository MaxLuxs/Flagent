package flagent.application

/**
 * Set by EnterprisePlugin when FLAGENT_LICENSE_KEY is configured.
 * Used by Info route to expose licenseValid in /api/v1/info.
 * null = not applicable (no license key, e.g. SaaS), true = valid, false = invalid/expired.
 */
object LicenseState {
    @Volatile
    var licenseValid: Boolean? = null
}
