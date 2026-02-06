package com.flagent.enhanced.crash

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Crash reporter for Flagent (Firebase Crashlytics-level).
 * Installs UncaughtExceptionHandler to capture crashes and POST to Flagent.
 *
 * @param baseUrl Base URL of Flagent server (e.g. "http://localhost:18000")
 * @param apiKey Optional X-API-Key for tenant-scoped crash reports
 * @param platform Platform identifier: "android", "ios", "jvm"
 * @param appVersion Optional app version string
 * @param deviceInfo Optional device info (e.g. "Android 14, Pixel 7")
 */
class FlagentCrashReporter(
    private val baseUrl: String,
    private val apiKey: String? = null,
    private val platform: String = "jvm",
    private val appVersion: String? = null,
    private val deviceInfo: String? = null,
    httpClient: HttpClient? = null
) {
    private val client = httpClient ?: HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    private val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

    private val crashHandler = Thread.UncaughtExceptionHandler { thread, throwable ->
        try {
            val stackTrace = throwable.stackTraceToString()
            val message = throwable.message ?: throwable.javaClass.simpleName
            val payload = CrashReportRequest(
                stackTrace = stackTrace,
                message = message,
                platform = platform,
                appVersion = appVersion,
                deviceInfo = deviceInfo,
                breadcrumbs = null,
                customKeys = null,
                timestamp = System.currentTimeMillis()
            )
            runBlocking {
                withContext(Dispatchers.IO) {
                    try {
                        client.post("$baseUrl/api/v1/crashes") {
                            contentType(ContentType.Application.Json)
                            apiKey?.let { header("X-API-Key", it) }
                            setBody(Json.encodeToString(CrashReportRequest.serializer(), payload))
                        }
                    } catch (_: Exception) { /* best-effort */ }
                }
            }
        } catch (_: Exception) { /* avoid secondary crash */ }
        defaultHandler?.uncaughtException(thread, throwable)
    }

    /**
     * Install crash reporter. Call early in app startup (e.g. Application.onCreate).
     */
    fun install() {
        Thread.setDefaultUncaughtExceptionHandler(crashHandler)
    }

    /**
     * Uninstall and restore previous handler.
     */
    fun uninstall() {
        Thread.setDefaultUncaughtExceptionHandler(defaultHandler)
    }

    @Serializable
    private data class CrashReportRequest(
        val stackTrace: String,
        val message: String,
        val platform: String,
        val appVersion: String? = null,
        val deviceInfo: String? = null,
        val breadcrumbs: String? = null,
        val customKeys: String? = null,
        val timestamp: Long
    )
}
