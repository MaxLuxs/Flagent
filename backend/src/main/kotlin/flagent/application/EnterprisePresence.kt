package flagent.application

/**
 * Set by Application when loading EnterpriseConfigurator.
 * Used by Info route to expose enterpriseEnabled in /api/v1/info.
 */
object EnterprisePresence {
    @Volatile
    var enterpriseEnabled: Boolean = false
        internal set
}
