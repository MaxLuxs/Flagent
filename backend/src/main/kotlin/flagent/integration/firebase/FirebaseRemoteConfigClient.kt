package flagent.integration.firebase

import com.google.auth.oauth2.GoogleCredentials
import flagent.config.AppConfig
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import java.io.ByteArrayInputStream
import java.io.FileInputStream

private val logger = KotlinLogging.logger {}

/**
 * Thin wrapper over Firebase Remote Config REST API.
 * GET current template (for ETag), PUT new template.
 */
class FirebaseRemoteConfigClient(
    private val projectId: String = AppConfig.firebaseRcProjectId,
    private val credentialsJson: String = AppConfig.firebaseRcCredentialsJson,
    private val credentialsFile: String = AppConfig.firebaseRcCredentialsFile,
    httpClient: HttpClient? = null,
    private val testToken: String? = null
) : IFirebaseRemoteConfigClient {
    private val baseUrl = "https://firebaseremoteconfig.googleapis.com/v1/projects/$projectId"
    private val client = httpClient ?: HttpClient(CIO) { expectSuccess = false }
    private val closeClientOnClose = httpClient == null

    private fun getAccessToken(): String {
        if (testToken != null) return testToken
        val credentials = getCredentials()
        credentials.refreshIfExpired()
        return credentials.accessToken?.tokenValue
            ?: throw IllegalStateException("Failed to obtain Firebase RC access token")
    }

    private fun getCredentials(): GoogleCredentials {
        val stream = when {
            credentialsJson.isNotBlank() -> ByteArrayInputStream(credentialsJson.toByteArray())
            credentialsFile.isNotBlank() -> FileInputStream(credentialsFile)
            else -> {
                val defaultPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS")
                if (defaultPath != null) FileInputStream(defaultPath)
                else throw IllegalStateException(
                    "Firebase RC credentials required: set FLAGENT_FIREBASE_RC_CREDENTIALS_JSON, " +
                        "FLAGENT_FIREBASE_RC_CREDENTIALS_FILE, or GOOGLE_APPLICATION_CREDENTIALS"
                )
            }
        }
        return GoogleCredentials.fromStream(stream)
            .createScoped(listOf("https://www.googleapis.com/auth/firebase.remoteconfig"))
    }

    override suspend fun getRemoteConfig(): Pair<String, String?> = withContext(Dispatchers.IO) {
        val token = getAccessToken()
        val response = client.get("$baseUrl/remoteConfig") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        val etag = response.headers[HttpHeaders.ETag]?.trim('"')
        val body = response.bodyAsText()
        if (!response.status.isSuccess()) {
            throw FirebaseRcException("GET Remote Config failed: ${response.status} $body")
        }
        body to etag
    }

    override suspend fun updateRemoteConfig(templateJson: String, etag: String): String =
        withContext(Dispatchers.IO) {
            val token = getAccessToken()
            val response = client.put("$baseUrl/remoteConfig") {
                header(HttpHeaders.Authorization, "Bearer $token")
                header("If-Match", etag)
                header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(templateJson)
            }

            if (!response.status.isSuccess()) {
                throw FirebaseRcException(
                    "PUT Remote Config failed: ${response.status} ${response.bodyAsText()}"
                )
            }
            response.bodyAsText()
        }

    override fun close() {
        if (closeClientOnClose) {
            client.close()
        }
    }
}

class FirebaseRcException(message: String) : RuntimeException(message)
