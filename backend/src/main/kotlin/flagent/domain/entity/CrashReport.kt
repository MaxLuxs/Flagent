package flagent.domain.entity

import kotlinx.serialization.Serializable

/**
 * Crash report from SDK (Firebase Crashlytics-level).
 * OSS entity; Enterprise uses same structure with tenant isolation.
 */
@Serializable
data class CrashReport(
    val id: Long = 0,
    val stackTrace: String,
    val message: String,
    val platform: String,
    val appVersion: String? = null,
    val deviceInfo: String? = null,
    val breadcrumbs: String? = null,
    val customKeys: String? = null,
    /** Keys of flags that were active when the crash occurred (SDK sends these). */
    val activeFlagKeys: List<String>? = null,
    val timestamp: Long,
    val tenantId: String? = null
)
