package flagent.api.model

import kotlinx.serialization.Serializable

/**
 * Info API models
 */

@Serializable
data class InfoResponse(
    val version: String,
    val buildTime: String? = null,
    val gitCommit: String? = null,
    /** True when flagent-enterprise module is loaded (tenant, billing, SSO, anomaly, metrics). */
    val enterpriseEnabled: Boolean = false,
    /** When FLAGENT_LICENSE_KEY is set: true=valid, false=invalid/expired, null=not checked (SaaS, no license). */
    val licenseValid: Boolean? = null
)
