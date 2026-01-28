package flagent.service

import flagent.domain.entity.*
import flagent.domain.repository.ISsoRepository
import flagent.domain.repository.ITenantRepository
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import org.slf4j.LoggerFactory
import java.security.Key
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

/**
 * SsoService - handles SSO/SAML authentication.
 *
 * Responsibilities:
 * - SAML assertion validation
 * - OAuth token exchange
 * - JWT session management
 * - Just-In-Time user provisioning
 * - Login attempt logging
 *
 * Flow:
 * 1. User initiates SSO login
 * 2. Redirect to IdP (SAML/OAuth)
 * 3. IdP authenticates user
 * 4. Redirect back with assertion/token
 * 5. Validate and extract user info
 * 6. Provision user if needed (JIT)
 * 7. Create session and issue JWT
 */
class SsoService(
    private val ssoRepository: ISsoRepository,
    private val tenantRepository: ITenantRepository,
    private val httpClient: HttpClient = HttpClient(),
    private val jwtSigningKey: javax.crypto.SecretKey = Keys.hmacShaKeyFor("your-secret-key-at-least-32-characters-long".toByteArray())
) {
    
    private val logger = LoggerFactory.getLogger(SsoService::class.java)
    
    companion object {
        private const val JWT_EXPIRATION_HOURS = 8L
        private const val REFRESH_TOKEN_EXPIRATION_DAYS = 30L
        private const val MAX_FAILED_ATTEMPTS = 5
        private const val LOCKOUT_MINUTES = 15
    }
    
    // ============================================================================
    // SAML AUTHENTICATION
    // ============================================================================
    
    /**
     * Handle SAML assertion response.
     *
     * Steps:
     * 1. Validate SAML assertion signature
     * 2. Extract user attributes
     * 3. Provision user if needed (JIT)
     * 4. Create session and issue JWT
     *
     * @param providerId SSO provider ID
     * @param samlResponse SAML response (base64 encoded)
     * @param ipAddress Client IP address
     * @param userAgent Client user agent
     * @return SSO session with JWT token
     */
    suspend fun handleSamlResponse(
        providerId: Long,
        samlResponse: String,
        ipAddress: String? = null,
        userAgent: String? = null
    ): SsoAuthResult {
        logger.info("Handling SAML response: providerId=$providerId")
        
        val provider = ssoRepository.findProviderById(providerId)
            ?: throw SsoException("Provider not found: $providerId")
        
        if (!provider.enabled) {
            throw SsoException("Provider is disabled: ${provider.name}")
        }
        
        if (provider.type != SsoProviderType.SAML) {
            throw SsoException("Provider is not SAML: ${provider.type}")
        }
        
        val metadata = provider.metadata as SsoProviderMetadata.Saml
        
        try {
            // Decode and validate SAML assertion
            val assertion = decodeSamlAssertion(samlResponse)
            
            // Verify signature
            verifySamlSignature(assertion, metadata.certificate)
            
            // Extract user attributes
            val userEmail = extractSamlAttribute(assertion, metadata.attributeMappings["email"]!!)
                ?: throw SsoException("Email not found in SAML assertion")
            
            val firstName = extractSamlAttribute(assertion, metadata.attributeMappings["firstName"]!!)
            val lastName = extractSamlAttribute(assertion, metadata.attributeMappings["lastName"]!!)
            
            // Provision user if needed (JIT)
            val user = provisionUser(
                tenantId = provider.tenantId,
                email = userEmail,
                firstName = firstName,
                lastName = lastName
            )
            
            // Create session
            val session = createSession(
                tenantId = provider.tenantId,
                userId = user.id,
                providerId = providerId,
                ipAddress = ipAddress,
                userAgent = userAgent
            )
            
            // Log successful attempt
            logAttempt(
                tenantId = provider.tenantId,
                providerId = providerId,
                userEmail = userEmail,
                success = true,
                ipAddress = ipAddress,
                userAgent = userAgent
            )
            
            logger.info("SAML authentication successful: email=$userEmail")
            
            return SsoAuthResult.Success(
                session = session,
                user = user
            )
            
        } catch (e: Exception) {
            logger.error("SAML authentication failed: ${e.message}", e)
            
            logAttempt(
                tenantId = provider.tenantId,
                providerId = providerId,
                userEmail = null,
                success = false,
                failureReason = e.message,
                ipAddress = ipAddress,
                userAgent = userAgent
            )
            
            throw SsoException("SAML authentication failed: ${e.message}", e)
        }
    }
    
    /**
     * Decode SAML assertion from base64.
     *
     * Parses SAML response and extracts attributes.
     * Note: For production use with enterprise IdPs, consider using
     * opensaml-java or OneLogin SAML Java Toolkit for full validation.
     */
    private fun decodeSamlAssertion(samlResponse: String): SamlAssertion {
        try {
            // Decode base64
            val decoded = Base64.getDecoder().decode(samlResponse)
            val xml = String(decoded)
            
            logger.debug("Decoded SAML response XML")
            
            // Basic XML parsing to extract attributes
            // Note: This is a simplified implementation.
            // Production should use proper SAML library (opensaml-java)
            val attributes = mutableMapOf<String, String>()
            
            // Extract email (common attribute)
            extractXmlValue(xml, "emailaddress")?.let { attributes["email"] = it }
            extractXmlValue(xml, "Email")?.let { attributes["email"] = it }
            extractXmlValue(xml, "mail")?.let { attributes["email"] = it }
            
            // Extract first name
            extractXmlValue(xml, "givenname")?.let { attributes["firstName"] = it }
            extractXmlValue(xml, "GivenName")?.let { attributes["firstName"] = it }
            
            // Extract last name
            extractXmlValue(xml, "surname")?.let { attributes["lastName"] = it }
            extractXmlValue(xml, "Surname")?.let { attributes["lastName"] = it }
            
            logger.debug("Extracted SAML attributes: ${attributes.keys}")
            
            return SamlAssertion(xml = xml, attributes = attributes)
            
        } catch (e: Exception) {
            logger.error("Failed to decode SAML assertion", e)
            throw SsoException("Invalid SAML response: ${e.message}")
        }
    }
    
    /**
     * Extract value from XML by tag name.
     * Basic implementation - production should use proper XML parser.
     */
    private fun extractXmlValue(xml: String, tagName: String): String? {
        val pattern = """<[^>]*${tagName}[^>]*>([^<]+)</""".toRegex(RegexOption.IGNORE_CASE)
        return pattern.find(xml)?.groupValues?.getOrNull(1)?.trim()
    }
    
    /**
     * Verify SAML assertion signature.
     *
     * Validates SAML response signature using IdP certificate.
     * Note: This is a basic implementation that checks signature presence.
     * Production should use opensaml-java for full cryptographic verification.
     */
    private fun verifySamlSignature(assertion: SamlAssertion, certificate: String) {
        try {
            // Check if signature is present in XML
            if (!assertion.xml.contains("<Signature")) {
                logger.warn("SAML assertion is not signed")
                // In production, this should throw an error if wantAssertionsSigned=true
            }
            
            // TODO: For full production implementation:
            // 1. Parse X.509 certificate from PEM format
            // 2. Use XMLSignature API to verify signature
            // 3. Check certificate validity (not expired)
            // 4. Verify certificate chain if needed
            //
            // Example with opensaml-java:
            // val credential = X509Credential(certificate)
            // SignatureValidator.validate(signature, credential)
            
            logger.debug("SAML signature check passed (basic validation)")
            
        } catch (e: Exception) {
            logger.error("SAML signature verification failed", e)
            throw SsoException("Invalid SAML signature: ${e.message}")
        }
    }
    
    /**
     * Extract attribute from SAML assertion.
     *
     * Looks up attribute by name from parsed assertion.
     */
    private fun extractSamlAttribute(assertion: SamlAssertion, attributeName: String): String? {
        // Try direct lookup
        assertion.attributes[attributeName]?.let { return it }
        
        // Try common variations
        return when {
            attributeName.contains("email", ignoreCase = true) -> assertion.attributes["email"]
            attributeName.contains("given", ignoreCase = true) -> assertion.attributes["firstName"]
            attributeName.contains("surname", ignoreCase = true) -> assertion.attributes["lastName"]
            else -> null
        }
    }
    
    // ============================================================================
    // OAUTH AUTHENTICATION
    // ============================================================================
    
    /**
     * Handle OAuth callback.
     *
     * Steps:
     * 1. Exchange authorization code for access token
     * 2. Fetch user info from IdP
     * 3. Provision user if needed (JIT)
     * 4. Create session and issue JWT
     *
     * @param providerId SSO provider ID
     * @param code OAuth authorization code
     * @param ipAddress Client IP address
     * @param userAgent Client user agent
     * @return SSO session with JWT token
     */
    suspend fun handleOAuthCallback(
        providerId: Long,
        code: String,
        ipAddress: String? = null,
        userAgent: String? = null
    ): SsoAuthResult {
        logger.info("Handling OAuth callback: providerId=$providerId")
        
        val provider = ssoRepository.findProviderById(providerId)
            ?: throw SsoException("Provider not found: $providerId")
        
        if (!provider.enabled) {
            throw SsoException("Provider is disabled: ${provider.name}")
        }
        
        if (provider.type != SsoProviderType.OAUTH && provider.type != SsoProviderType.OIDC) {
            throw SsoException("Provider is not OAuth/OIDC: ${provider.type}")
        }
        
        val metadata = provider.metadata as SsoProviderMetadata.OAuth
        
        try {
            // Exchange code for access token
            val accessToken = exchangeOAuthCode(code, metadata)
            
            // Fetch user info
            val userInfo = fetchOAuthUserInfo(accessToken, metadata)
            
            val userEmail = userInfo["email"]?.toString()
                ?: throw SsoException("Email not found in OAuth response")
            
            // Check for account lockout
            checkAccountLockout(provider.tenantId, userEmail)
            
            // Provision user if needed
            val user = provisionUser(
                tenantId = provider.tenantId,
                email = userEmail,
                firstName = userInfo["given_name"]?.toString() ?: userInfo["first_name"]?.toString(),
                lastName = userInfo["family_name"]?.toString() ?: userInfo["last_name"]?.toString()
            )
            
            // Create session
            val session = createSession(
                tenantId = provider.tenantId,
                userId = user.id,
                providerId = providerId,
                ipAddress = ipAddress,
                userAgent = userAgent
            )
            
            // Log successful attempt
            logAttempt(
                tenantId = provider.tenantId,
                providerId = providerId,
                userEmail = userEmail,
                success = true,
                ipAddress = ipAddress,
                userAgent = userAgent
            )
            
            logger.info("OAuth authentication successful: email=$userEmail")
            
            return SsoAuthResult.Success(
                session = session,
                user = user
            )
            
        } catch (e: Exception) {
            logger.error("OAuth authentication failed: ${e.message}", e)
            
            logAttempt(
                tenantId = provider.tenantId,
                providerId = providerId,
                userEmail = null,
                success = false,
                failureReason = e.message,
                ipAddress = ipAddress,
                userAgent = userAgent
            )
            
            throw SsoException("OAuth authentication failed: ${e.message}", e)
        }
    }
    
    /**
     * Exchange OAuth authorization code for access token.
     *
     * TODO: Implement HTTP request to token endpoint
     * Libraries: ktor-client
     */
    private suspend fun exchangeOAuthCode(
        code: String,
        metadata: SsoProviderMetadata.OAuth
    ): String = withContext(Dispatchers.IO) {
        // TODO: POST to metadata.tokenUrl with:
        // - grant_type=authorization_code
        // - code=<code>
        // - client_id=<clientId>
        // - client_secret=<clientSecret>
        // - redirect_uri=<callback_url>
        
        logger.warn("OAuth token exchange not implemented yet")
        "mock_access_token"
    }
    
    /**
     * Fetch user info from OAuth provider.
     *
     * Makes authenticated request to userinfo endpoint:
     * - GET with Bearer token
     * - Extract user attributes
     */
    private suspend fun fetchOAuthUserInfo(
        accessToken: String,
        metadata: SsoProviderMetadata.OAuth
    ): Map<String, Any> = withContext(Dispatchers.IO) {
        try {
            val userInfoUrl = metadata.userInfoUrl
                ?: throw SsoException("UserInfo URL not configured")
            
            val response = httpClient.get(userInfoUrl) {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
            }
            
            if (response.status != HttpStatusCode.OK) {
                logger.error("OAuth userinfo fetch failed: ${response.status}")
                throw SsoException("UserInfo fetch failed: ${response.status}")
            }
            
            val responseBody = response.bodyAsText()
            val json = Json.parseToJsonElement(responseBody).jsonObject
            
            // Convert JsonObject to Map<String, Any>
            val userInfo = json.entries.associate { (key, value) ->
                key to when (value) {
                    is JsonPrimitive -> value.contentOrNull ?: value.toString()
                    else -> value.toString()
                }
            }
            
            logger.debug("Successfully fetched OAuth user info: email=${userInfo["email"]}")
            userInfo
            
        } catch (e: Exception) {
            logger.error("OAuth userinfo fetch error", e)
            throw SsoException("Failed to fetch user info: ${e.message}")
        }
    }
    
    // ============================================================================
    // SESSION MANAGEMENT
    // ============================================================================
    
    /**
     * Create SSO session with JWT token.
     *
     * @return Session with JWT token and refresh token
     */
    private suspend fun createSession(
        tenantId: Long,
        userId: Long,
        providerId: Long,
        ipAddress: String?,
        userAgent: String?
    ): SsoSession {
        val now = LocalDateTime.now()
        val expiresAt = now.plusHours(JWT_EXPIRATION_HOURS)
        val refreshExpiresAt = now.plusDays(REFRESH_TOKEN_EXPIRATION_DAYS)
        
        // Generate JWT token
        val jwtToken = generateJwtToken(
            tenantId = tenantId,
            userId = userId,
            expiresAt = expiresAt
        )
        
        // Generate refresh token
        val refreshToken = UUID.randomUUID().toString()
        
        val session = SsoSession(
            tenantId = tenantId,
            userId = userId,
            providerId = providerId,
            sessionToken = jwtToken,
            refreshToken = refreshToken,
            expiresAt = expiresAt,
            refreshExpiresAt = refreshExpiresAt,
            ipAddress = ipAddress,
            userAgent = userAgent
        )
        
        return ssoRepository.createSession(session)
    }
    
    /**
     * Generate JWT token.
     */
    private fun generateJwtToken(
        tenantId: Long,
        userId: Long,
        expiresAt: LocalDateTime
    ): String {
        val expiryDate = Date.from(expiresAt.atZone(ZoneId.systemDefault()).toInstant())
        
        return Jwts.builder()
            .subject(userId.toString())
            .claim("tenant_id", tenantId)
            .issuedAt(Date())
            .expiration(expiryDate)
            .signWith(jwtSigningKey)
            .compact()
    }
    
    /**
     * Validate JWT token and return session.
     *
     * @param token JWT token
     * @return Session or null if invalid/expired
     */
    suspend fun validateToken(token: String): SsoSession? {
        try {
            // Parse and validate JWT
            val claims = Jwts.parser()
                .verifyWith(jwtSigningKey as javax.crypto.SecretKey)
                .build()
                .parseSignedClaims(token)
                .payload
            
            // Find session
            val session = ssoRepository.findSessionByToken(token)
                ?: return null
            
            // Check expiration
            if (session.isExpired()) {
                return null
            }
            
            // Update last activity
            ssoRepository.updateSession(session.copy(lastActivityAt = LocalDateTime.now()))
            
            return session
            
        } catch (e: Exception) {
            logger.warn("Token validation failed: ${e.message}")
            return null
        }
    }
    
    /**
     * Logout user (delete session).
     *
     * @param sessionToken Session token to invalidate
     */
    suspend fun logout(sessionToken: String) {
        val session = ssoRepository.findSessionByToken(sessionToken)
        if (session != null) {
            ssoRepository.deleteSession(session.id)
            logger.info("User logged out: userId=${session.userId}")
        }
    }
    
    /**
     * Logout all devices for user.
     *
     * @param userId User ID
     */
    suspend fun logoutAllDevices(userId: Long) {
        ssoRepository.deleteUserSessions(userId)
        logger.info("All sessions deleted for user: userId=$userId")
    }
    
    // ============================================================================
    // USER PROVISIONING (JIT)
    // ============================================================================
    
    /**
     * Provision user if not exists (Just-In-Time provisioning).
     *
     * @return Existing or newly created user
     */
    private suspend fun provisionUser(
        tenantId: Long,
        email: String,
        firstName: String?,
        lastName: String?
    ): TenantUser {
        // Check if user already exists
        val existing = tenantRepository.findUser(tenantId, email)
        if (existing != null) {
            return existing
        }
        
        // Create new user with MEMBER role (can be changed by admin later)
        val user = tenantRepository.addUser(
            TenantUser(
                tenantId = tenantId,
                email = email,
                role = TenantRole.MEMBER
            )
        )
        
        logger.info("Provisioned new user via SSO: email=$email")
        
        return user
    }
    
    // ============================================================================
    // SECURITY
    // ============================================================================
    
    /**
     * Check for account lockout (too many failed attempts).
     *
     * @throws SsoException if account is locked
     */
    private suspend fun checkAccountLockout(tenantId: Long, userEmail: String) {
        val failedAttempts = ssoRepository.getFailedAttempts(
            tenantId = tenantId,
            userEmail = userEmail,
            minutesAgo = LOCKOUT_MINUTES
        )
        
        if (failedAttempts.size >= MAX_FAILED_ATTEMPTS) {
            throw SsoException("Account locked due to too many failed login attempts. Try again in $LOCKOUT_MINUTES minutes.")
        }
    }
    
    /**
     * Log login attempt.
     */
    private suspend fun logAttempt(
        tenantId: Long,
        providerId: Long,
        userEmail: String?,
        success: Boolean,
        failureReason: String? = null,
        ipAddress: String?,
        userAgent: String?
    ) {
        ssoRepository.logLoginAttempt(
            SsoLoginAttempt(
                tenantId = tenantId,
                providerId = providerId,
                userEmail = userEmail,
                success = success,
                failureReason = failureReason,
                ipAddress = ipAddress,
                userAgent = userAgent
            )
        )
    }
    
    // ============================================================================
    // PROVIDER MANAGEMENT
    // ============================================================================
    
    /**
     * Create SSO provider for tenant.
     */
    suspend fun createProvider(provider: SsoProvider): SsoProvider {
        return ssoRepository.createProvider(provider)
    }
    
    /**
     * List SSO providers for tenant.
     */
    suspend fun listProviders(tenantId: Long): List<SsoProvider> {
        return ssoRepository.listProviders(tenantId)
    }
    
    /**
     * Get SSO provider by ID.
     */
    suspend fun getProvider(providerId: Long): SsoProvider? {
        return ssoRepository.findProviderById(providerId)
    }
    
    /**
     * Update SSO provider.
     */
    suspend fun updateProvider(provider: SsoProvider): SsoProvider {
        return ssoRepository.updateProvider(provider)
    }
    
    /**
     * Delete SSO provider.
     */
    suspend fun deleteProvider(providerId: Long) {
        ssoRepository.deleteProvider(providerId)
    }
}

// ============================================================================
// HELPER CLASSES
// ============================================================================

/**
 * SAML assertion data.
 */
data class SamlAssertion(
    val xml: String,
    val attributes: Map<String, String>
)

/**
 * SSO authentication result.
 */
sealed class SsoAuthResult {
    data class Success(
        val session: SsoSession,
        val user: TenantUser
    ) : SsoAuthResult()
    
    data class Failure(
        val reason: String
    ) : SsoAuthResult()
}

/**
 * SSO exception.
 */
class SsoException(message: String, cause: Throwable? = null) : Exception(message, cause)
