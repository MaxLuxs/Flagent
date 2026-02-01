package flagent.util

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import kotlin.test.*

class JwtUtilsTest {

    @Test
    fun createAdminTokenReturnsNonEmptyJwt() {
        val token = JwtUtils.createAdminToken("admin@test.com", "test-secret-at-least-32-chars", "sub")
        assertTrue(token.isNotBlank())
        assertTrue(token.split(".").size == 3)
    }

    @Test
    fun createAdminTokenContainsAdminClaim() {
        val secret = "test-secret-at-least-32-characters-long"
        val token = JwtUtils.createAdminToken("admin@test.com", secret, "sub")
        val algorithm = Algorithm.HMAC256(secret)
        val verifier = JWT.require(algorithm).build()
        val decoded = verifier.verify(token)
        assertTrue(decoded.getClaim("admin").asBoolean())
        assertEquals("admin@test.com", decoded.subject)
        assertEquals("admin@test.com", decoded.getClaim("sub").asString())
    }

    @Test
    fun createAdminTokenThrowsWhenSecretEmpty() {
        assertFailsWith<IllegalStateException> {
            JwtUtils.createAdminToken("a@b.com", "", "sub")
        }
    }

    @Test
    fun createAdminTokenWithCustomUserClaim() {
        val secret = "test-secret-at-least-32-characters-long"
        val token = JwtUtils.createAdminToken("u@x.com", secret, "email")
        val algorithm = Algorithm.HMAC256(secret)
        val verifier = JWT.require(algorithm).build()
        val decoded = verifier.verify(token)
        assertEquals("u@x.com", decoded.getClaim("email").asString())
        assertTrue(decoded.getClaim("admin").asBoolean())
    }

    @Test
    fun createAdminTokenHasExpirationInFuture() {
        val secret = "test-secret-at-least-32-characters-long"
        val token = JwtUtils.createAdminToken("a@b.com", secret, "sub", ttlSeconds = 3600L)
        val algorithm = Algorithm.HMAC256(secret)
        val verifier = JWT.require(algorithm).build()
        val decoded = verifier.verify(token)
        assertTrue(decoded.expiresAt?.time?.let { it > System.currentTimeMillis() } == true)
    }
}
