package flagent.middleware

import flagent.config.AppConfig
import io.ktor.server.application.*
import io.ktor.server.plugins.compression.*

/**
 * Compression middleware - Gzip compression for responses
 */
fun Application.configureCompression() {
    if (AppConfig.middlewareGzipEnabled) {
        install(Compression) {
            gzip {
                priority = 1.0
            }
        }
    }
}
