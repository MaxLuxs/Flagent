package flagent.util

import flagent.config.AppConfig
import com.auth0.jwt.JWT
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*

/**
 * Get subject (user) from request
 * Maps to pkg/handler/subject.go from original project
 * 
 * Note: Uses else-if logic from original - only one auth method is checked
 * Returns empty string if not found (matches original behavior)
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
 * Maps to pkg/handler/subject.go lines 17-25
 * 
 * In original: token is stored in context via jwtAuthUserProperty
 * In Ktor: principal is available via call.principal<JWTPrincipal>()
 */
private fun ApplicationCall.getSubjectFromJWT(): String? {
    val principal = principal<JWTPrincipal>()
    if (principal == null) {
        return null // Token not found in context (matches original: !ok)
    }
    
    // In original: checks token.Valid, but in Ktor principal is only set if valid
    val claim = principal.payload.getClaim(AppConfig.jwtAuthUserClaim)?.asString()
    return claim
}

/**
 * Extract subject from cookie
 * Maps to pkg/handler/subject.go lines 29-44
 */
private fun ApplicationCall.getSubjectFromCookie(): String? {
    val cookie = request.cookies[AppConfig.cookieAuthUserField] ?: return null
    
    // If JWT claim is configured, parse JWT from cookie value
    if (AppConfig.cookieAuthUserFieldJWTClaim.isNotEmpty()) {
        // Similar to original: skip error check, assume cookie already passed auth middleware
        // This assumes that the cookie we get already passed the auth middleware
        try {
            val decodedJWT = JWT.decode(cookie)
            val claim = decodedJWT.getClaim(AppConfig.cookieAuthUserFieldJWTClaim)?.asString()
            return claim
        } catch (e: Exception) {
            // Skip error check as per original implementation
            return null
        }
    }
    
    // Otherwise, return cookie value directly
    return cookie
}
