package flagent.middleware

import flagent.config.AppConfig
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import javax.crypto.SecretKey

private val logger = LoggerFactory.getLogger("SsoJwtAuth")

/**
 * Configures bearer auth "jwt-auth" that validates SSO session tokens (jjwt)
 * and produces UserPrincipal(tenantId) for Billing and other protected routes.
 */
fun Application.configureSsoJwtAuth() {
    val secret = AppConfig.ssoJwtSecret
    if (secret.length < 32) {
        logger.warn("FLAGENT_SSO_JWT_SECRET should be at least 32 characters for HS256")
    }
    val key: SecretKey = Keys.hmacShaKeyFor(secret.toByteArray(Charsets.UTF_8))

    install(Authentication) {
        bearer("jwt-auth") {
            authenticate { credential ->
                try {
                    val body = Jwts.parser()
                        .verifyWith(key)
                        .build()
                        .parseSignedClaims(credential.token)
                        .payload
                    val tenantIdObj = body["tenant_id"] ?: return@authenticate null
                    val tenantId = when (tenantIdObj) {
                        is Number -> tenantIdObj.toLong()
                        else -> return@authenticate null
                    }
                    UserPrincipal(tenantId)
                } catch (e: Exception) {
                    logger.debug("jwt-auth validation failed: ${e.message}")
                    null
                }
            }
        }
    }
}
