package flagent.util

import com.auth0.jwt.JWT
import flagent.config.AppConfig
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.header

/**
 * Get subject (user) from request.
 * Only one auth method is checked (JWT, header, or cookie).
 * Returns empty string if not found.
 */
fun ApplicationCall.getSubject(): String {
    // Try JWT auth user claim
    if (AppConfig.jwtAuthEnabled) {
        return getSubjectFromJWT() ?: ""
    }

    // Try header auth
    if (AppConfig.headerAuthEnabled) {
        return request.header(AppConfig.headerAuthUserField) ?: ""
    }

    // Try cookie auth
    if (AppConfig.cookieAuthEnabled) {
        return getSubjectFromCookie() ?: ""
    }

    return ""
}

/**
 * Extract subject from JWT token
 */
private fun ApplicationCall.getSubjectFromJWT(): String? {
    val principal = principal<JWTPrincipal>()
    if (principal == null) {
        return null // Token not found in context
    }

    // Principal is only set if token is valid
    val claim = principal.payload.getClaim(AppConfig.jwtAuthUserClaim)?.asString()
    return claim
}

/**
 * Extract subject from cookie
 */
private fun ApplicationCall.getSubjectFromCookie(): String? {
    val cookie = request.cookies[AppConfig.cookieAuthUserField] ?: return null

    // If JWT claim is configured, parse JWT from cookie value
    if (AppConfig.cookieAuthUserFieldJWTClaim.isNotEmpty()) {
        // Skip verification - cookie already passed auth middleware
        // This assumes that the cookie we get already passed the auth middleware
        try {
            val decodedJWT = JWT.decode(cookie)
            val claim = decodedJWT.getClaim(AppConfig.cookieAuthUserFieldJWTClaim)?.asString()
            return claim
        } catch (e: Exception) {
            // Skip verification - cookie already passed auth middleware
            return null
        }
    }

    // Otherwise, return cookie value directly
    return cookie
}
