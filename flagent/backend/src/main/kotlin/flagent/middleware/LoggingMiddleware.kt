package flagent.middleware

import flagent.config.AppConfig
import io.ktor.server.application.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.request.*
import org.slf4j.event.Level

/**
 * Logging middleware - structured logging for requests/responses
 * Maps to pkg/config/middleware.go SetupGlobalMiddleware verbose logger
 */
fun Application.configureLogging() {
    if (AppConfig.middlewareVerboseLoggerEnabled) {
        install(CallLogging) {
            level = Level.INFO
            filter { call ->
                val path = call.request.path()
                !AppConfig.middlewareVerboseLoggerExcludeURLs.any { excludePath ->
                    path.startsWith(excludePath)
                }
            }
            format { call ->
                val status = call.response.status()
                val httpMethod = call.request.httpMethod
                val userAgent = call.request.header("User-Agent")
                val path = call.request.path()
                // Try X-Forwarded-For header first (for reverse proxy), then fallback to remote host
                val clientIp = call.request.header("X-Forwarded-For")?.split(",")?.firstOrNull()?.trim()
                    ?: call.request.header("X-Real-IP")
                    ?: "unknown"
                
                "$httpMethod $path - $status - $clientIp - $userAgent"
            }
        }
    }
}
