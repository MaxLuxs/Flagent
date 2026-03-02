package flagent.route

import flagent.config.AppConfig
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import kotlinx.serialization.Serializable

@Serializable
data class FirebaseRcStatusResponse(
    val enabled: Boolean,
    val projectId: String?,
    val syncIntervalSeconds: Long,
    val parameterPrefix: String,
    val hasCredentials: Boolean
)

@Serializable
data class FirebaseAnalyticsStatusResponse(
    val enabled: Boolean,
    val measurementId: String?,
    val hasApiSecret: Boolean
)

@Serializable
data class FirebaseIntegrationStatusResponse(
    val firebaseRc: FirebaseRcStatusResponse,
    val firebaseAnalytics: FirebaseAnalyticsStatusResponse
)

/**
 * Admin route for Firebase integration status (Remote Config + Analytics).
 * Read-only, based on environment configuration from AppConfig.
 */
fun Routing.configureIntegrationFirebaseRoutes() {
    route("/admin") {
        authenticate("jwt") {
            route("/integrations/firebase") {
                get("/status") {
                    val firebaseRcStatus = FirebaseRcStatusResponse(
                        enabled = AppConfig.firebaseRcSyncEnabled,
                        projectId = maskIdentifier(AppConfig.firebaseRcProjectId),
                        syncIntervalSeconds = AppConfig.firebaseRcSyncInterval.inWholeSeconds,
                        parameterPrefix = AppConfig.firebaseRcParameterPrefix,
                        hasCredentials = AppConfig.firebaseRcCredentialsJson.isNotBlank() ||
                            AppConfig.firebaseRcCredentialsFile.isNotBlank()
                    )

                    val firebaseAnalyticsStatus = FirebaseAnalyticsStatusResponse(
                        enabled = AppConfig.firebaseAnalyticsEnabled,
                        measurementId = maskIdentifier(AppConfig.firebaseAnalyticsMeasurementId),
                        hasApiSecret = AppConfig.firebaseAnalyticsApiSecret.isNotBlank()
                    )

                    val response = FirebaseIntegrationStatusResponse(
                        firebaseRc = firebaseRcStatus,
                        firebaseAnalytics = firebaseAnalyticsStatus
                    )

                    call.respond(response)
                }
            }
        }
    }
}

private fun maskIdentifier(value: String?, visibleChars: Int = 4): String? {
    val nonEmpty = value?.takeIf { it.isNotBlank() } ?: return null
    return if (nonEmpty.length <= visibleChars) {
        nonEmpty
    } else {
        nonEmpty.take(visibleChars) + "***"
    }
}

