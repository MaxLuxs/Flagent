package flagent.integration.firebase

/**
 * Interface for Firebase Remote Config REST API client.
 * Allows mocking in tests without real Firebase credentials.
 */
interface IFirebaseRemoteConfigClient {
    /**
     * GET current Remote Config template. Returns (body, etag).
     */
    suspend fun getRemoteConfig(): Pair<String, String?>

    /**
     * PUT new Remote Config template. Requires ETag from last GET (or "*" to force).
     */
    suspend fun updateRemoteConfig(templateJson: String, etag: String): String

    fun close()
}
