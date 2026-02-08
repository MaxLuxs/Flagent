package flagent.route.integration

import flagent.api.constants.ApiConstants
import flagent.config.AppConfig
import flagent.service.FlagService
import flagent.service.command.CreateFlagCommand
import flagent.util.TrunkUtils
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

private val log = KotlinLogging.logger {}

/**
 * Verifies GitHub webhook signature (X-Hub-Signature-256).
 */
private fun verifyGitHubSignature(payload: String, signature: String?, secret: String): Boolean {
    if (secret.isBlank() || signature.isNullOrBlank()) return false
    val expectedPrefix = "sha256="
    if (!signature.startsWith(expectedPrefix)) return false
    val signatureHex = signature.removePrefix(expectedPrefix)
    return try {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secret.toByteArray(Charsets.UTF_8), "HmacSHA256"))
        val computed = mac.doFinal(payload.toByteArray(Charsets.UTF_8))
        val expectedBytes = signatureHex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
        java.security.MessageDigest.isEqual(computed, expectedBytes)
    } catch (e: Exception) {
        log.warn(e) { "Failed to verify GitHub signature" }
        false
    }
}

fun Routing.configureIntegrationWebhookRoutes(flagService: FlagService) {
    route(ApiConstants.API_BASE_PATH) {
        route("/integrations") {
            route("/github") {
                post("/webhook") {
                    if (!AppConfig.githubAutoCreateFlag) {
                        call.respond(HttpStatusCode.OK, mapOf("message" to "GitHub auto-create disabled"))
                        return@post
                    }
                    val signature = call.request.header("X-Hub-Signature-256")
                    val payload = call.receiveText()
                    if (AppConfig.githubWebhookSecret.isNotBlank() && !verifyGitHubSignature(payload, signature, AppConfig.githubWebhookSecret)) {
                        log.warn { "GitHub webhook signature verification failed" }
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid signature"))
                        return@post
                    }
                    try {
                        val event = Json { ignoreUnknownKeys = true }.decodeFromString<GitHubPullRequestEvent>(payload)
                        if (event.action !in listOf("opened", "synchronize")) {
                            call.respond(HttpStatusCode.OK, mapOf("message" to "Ignored action: ${event.action}"))
                            return@post
                        }
                        val branch = event.pullRequest.head.ref
                        val flagKey = TrunkUtils.branchToFlagKey(branch)
                        val existing = flagService.findFlags(key = flagKey, limit = 1)
                        if (existing.isNotEmpty()) {
                            call.respond(HttpStatusCode.OK, mapOf("message" to "Flag already exists", "key" to flagKey))
                            return@post
                        }
                        val command = CreateFlagCommand(
                            key = flagKey,
                            description = "Auto from PR #${event.pullRequest.number} branch: $branch"
                        )
                        val flag = flagService.createFlag(command, updatedBy = "github-webhook")
                        log.info { "Created flag ${flag.key} from PR #${event.pullRequest.number} branch=$branch" }
                        call.respond(HttpStatusCode.OK, mapOf("message" to "Flag created", "key" to flag.key, "id" to flag.id))
                    } catch (e: Exception) {
                        log.warn(e) { "GitHub webhook processing failed: ${e.message}" }
                        call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.message ?: "Webhook processing failed")))
                    }
                }
            }
        }
    }
}
