package flagent.middleware

import flagent.config.AppConfig
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import java.security.KeyFactory
import java.security.interfaces.RSAPublicKey
import java.security.spec.X509EncodedKeySpec
import java.util.Base64

/**
 * Authentication middleware interface
 */
interface AuthMiddleware {
    fun isWhitelisted(path: String): Boolean
}

/**
 * JWT Authentication middleware.
 * Only runs when FLAGENT_JWT_AUTH_ENABLED=true. When disabled: no "jwt" provider is installed,
 * and admin routes (which use authenticate("jwt")) are not registered — see Application.kt.
 * This keeps /admin inaccessible unless JWT is explicitly configured with a secret.
 */
fun Application.configureJWTAuth() {
    if (!AppConfig.jwtAuthEnabled) return

    val secret = AppConfig.jwtAuthSecret
    if (secret.isEmpty()) {
        throw IllegalStateException("JWT_AUTH_SECRET is required when JWT_AUTH_ENABLED=true")
    }

    val algorithm = when (AppConfig.jwtAuthSigningMethod) {
        "HS256" -> Algorithm.HMAC256(secret)
        "HS512" -> Algorithm.HMAC512(secret)
        "RS256" -> {
            // For RS256, secret contains RSA public key in PEM format
            // For verification, we only need the public key
            try {
                // Remove PEM headers and footers, and all whitespace
                val publicKeyPEM = secret
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replace("-----BEGIN RSA PUBLIC KEY-----", "")
                    .replace("-----END RSA PUBLIC KEY-----", "")
                    .replace("\\s".toRegex(), "")
                    .replace("\n", "")
                    .replace("\r", "")
                    .replace(" ", "")
                
                // Base64 decode the key
                val decoded = Base64.getDecoder().decode(publicKeyPEM)
                
                // Generate the PublicKey object using X509EncodedKeySpec
                val keySpec = X509EncodedKeySpec(decoded)
                val keyFactory = KeyFactory.getInstance("RSA")
                val publicKey = keyFactory.generatePublic(keySpec) as RSAPublicKey
                
                // For verification, we pass public key and null for private key
                Algorithm.RSA256(publicKey, null)
            } catch (e: Exception) {
                throw IllegalArgumentException("Failed to parse RSA public key for RS256: ${e.message}", e)
            }
        }
        else -> throw IllegalArgumentException("Unsupported JWT signing method: ${AppConfig.jwtAuthSigningMethod}")
    }
    
    install(Authentication) {
        jwt("jwt") {
            realm = AppConfig.jwtAuthNoTokenRedirectURL
            verifier(
                JWT.require(algorithm)
                    .build()
            )
            skipWhen { call ->
                // Skip authentication if path is whitelisted
                val path = call.request.path()
                isWhitelisted(
                    path = path,
                    prefixWhitelist = AppConfig.jwtAuthPrefixWhitelistPaths,
                    exactWhitelist = AppConfig.jwtAuthExactWhitelistPaths,
                    noTokenStatusCode = AppConfig.jwtAuthNoTokenStatusCode
                )
            }
            validate { credential ->
                try {
                    val userClaim = credential.payload.getClaim(AppConfig.jwtAuthUserClaim)?.asString()
                    if (userClaim != null) {
                        JWTPrincipal(credential.payload)
                    } else {
                        null
                    }
                } catch (e: JWTVerificationException) {
                    null
                }
            }
            challenge { defaultScheme, realm ->
                when (AppConfig.jwtAuthNoTokenStatusCode) {
                    307 -> {
                        call.respondRedirect(AppConfig.jwtAuthNoTokenRedirectURL)
                    }
                    else -> {
                        call.respond(
                            HttpStatusCode.Unauthorized,
                            mapOf("error" to "Not authorized")
                        )
                    }
                }
            }
        }
    }
}

/**
 * Basic Authentication middleware
 */
fun Application.configureBasicAuth() {
    if (!AppConfig.basicAuthEnabled) return
    
    val username = AppConfig.basicAuthUsername
    val password = AppConfig.basicAuthPassword
    
    if (username.isEmpty() || password.isEmpty()) {
        throw IllegalStateException("BASIC_AUTH_USERNAME and BASIC_AUTH_PASSWORD are required when BASIC_AUTH_ENABLED=true")
    }
    
    install(Authentication) {
        basic("basic") {
            realm = "Flagent"
            skipWhen { call ->
                // Skip authentication if path is whitelisted
                val path = call.request.path()
                isWhitelisted(
                    path = path,
                    prefixWhitelist = AppConfig.basicAuthPrefixWhitelistPaths,
                    exactWhitelist = AppConfig.basicAuthExactWhitelistPaths,
                    noTokenStatusCode = 401 // Basic auth always uses 401
                )
            }
            validate { credentials ->
                if (credentials.name == username && credentials.password == password) {
                    UserIdPrincipal(credentials.name)
                } else {
                    null
                }
            }
        }
    }
}

/**
 * Header Authentication middleware
 */
fun Application.configureHeaderAuth() {
    if (!AppConfig.headerAuthEnabled) return
    
    install(Authentication) {
        bearer("header") {
            realm = "Flagent"
            authenticate { credential ->
                // Extract user from header field
                // In real implementation, you might want to validate the token
                UserIdPrincipal(credential.token)
            }
        }
    }
}

/**
 * Cookie Authentication middleware
 * Supports both JWT tokens in cookies and plain cookie values
 */
fun Application.configureCookieAuth() {
    if (!AppConfig.cookieAuthEnabled) return
    
    // Cookie auth is handled in Subject utility (flagent.util.Subject.kt)
    // Cookie authentication is extracted directly from cookies without requiring
    // authentication middleware
}

/**
 * HasSafePrefix checks if the given string is a safe URL path prefix
 */
private fun hasSafePrefix(s: String, prefix: String): Boolean {
    if (prefix.isEmpty()) {
        return true
    }
    
    // Check for path traversal attempts or suspicious patterns
    if (s == "." || s == ".." || s.contains("..")) {
        return false
    }
    
    // Normalize the path (prefix is controlled by us, no need to clean it)
    val cleanedS = s.replace("//", "/").trimEnd('/')
    
    // Check if the normalized path starts with the prefix
    return cleanedS.startsWith(prefix)
}

/**
 * Helper function to check if path is whitelisted
 */
fun isWhitelisted(
    path: String,
    prefixWhitelist: List<String>,
    exactWhitelist: List<String>,
    noTokenStatusCode: Int = 307
): Boolean {
    // If we set to 401 unauthorized, let the client handles the 401 itself
    // Only exact whitelist paths are checked in this case
    if (noTokenStatusCode == 401) {
        if (exactWhitelist.contains(path)) {
            return true
        }
    } else {
        // Check exact whitelist
        if (exactWhitelist.contains(path)) {
            return true
        }
        
        // Check prefix whitelist with safe prefix check
        for (prefix in prefixWhitelist) {
            if (prefix.isNotEmpty() && hasSafePrefix(path, prefix)) {
                return true
            }
        }
    }
    
    return false
}
