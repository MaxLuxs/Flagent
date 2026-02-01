package flagent.util

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import flagent.config.AppConfig
import java.util.Date

/**
 * Utility for creating admin JWT tokens (used by POST /auth/login).
 * Same secret and claim names as AuthMiddleware so enterprise AdminAuth can verify.
 */
object JwtUtils {

    private const val ADMIN_CLAIM = "admin"
    private const val DEFAULT_TTL_SECONDS = 86400L // 24 hours

    /**
     * Create a JWT with sub=email and admin=true for admin login.
     * Uses FLAGENT_JWT_AUTH_SECRET and HS256 to match core JWT auth.
     */
    fun createAdminToken(
        email: String,
        secret: String = AppConfig.jwtAuthSecret,
        userClaim: String = AppConfig.jwtAuthUserClaim,
        ttlSeconds: Long = DEFAULT_TTL_SECONDS
    ): String {
        if (secret.isEmpty()) throw IllegalStateException("JWT secret is required to create admin token")
        val algorithm = Algorithm.HMAC256(secret)
        val expiresAt = Date(System.currentTimeMillis() + ttlSeconds * 1000)
        return JWT.create()
            .withSubject(email)
            .withClaim(userClaim, email)
            .withClaim(ADMIN_CLAIM, true)
            .withExpiresAt(expiresAt)
            .sign(algorithm)
    }
}
