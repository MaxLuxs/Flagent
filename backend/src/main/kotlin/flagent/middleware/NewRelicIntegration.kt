package flagent.middleware

import flagent.config.AppConfig
import io.ktor.server.application.*
import com.newrelic.api.agent.NewRelic
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * New Relic integration middleware
 *
 * Note: New Relic Java Agent API works without agent (no-op), but for full functionality
 * requires Java Agent to be started with JVM (-javaagent:newrelic.jar)
 */
fun Application.configureNewRelic() {
    if (!AppConfig.newRelicEnabled) {
        return
    }
    
    try {
        // New Relic API works without agent (no-op implementations)
        // For full functionality, Java Agent should be started with JVM
        // The API will work regardless, so we just log that it's enabled
        logger.info { "New Relic API enabled. For full functionality, start JVM with -javaagent:newrelic.jar" }
    } catch (e: Exception) {
        logger.error(e) { "Failed to initialize New Relic" }
    }
}
