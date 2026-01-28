package flagent.route

import flagent.config.AppConfig
import flagent.domain.entity.SsoProvider
import flagent.domain.entity.SsoProviderMetadata
import flagent.domain.entity.SsoProviderType
import flagent.middleware.tenantContext
import flagent.service.SsoAuthResult
import flagent.service.SsoException
import flagent.service.SsoService
import flagent.service.dto.sso.*
import flagent.service.dto.TenantUserDTO
import flagent.service.dto.toDTO
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.util.Base64

private val logger = LoggerFactory.getLogger("SsoRoutes")

/**
 * SSO/SAML routes.
 *
 * Provider Management (requires tenant context):
 * - POST /sso/providers - Create SSO provider
 * - GET /sso/providers - List providers
 * - GET /sso/providers/{id} - Get provider
 * - PUT /sso/providers/{id} - Update provider
 * - DELETE /sso/providers/{id} - Delete provider
 *
 * Authentication (public):
 * - GET /sso/login/{providerId} - Initiate SSO login
 * - POST /sso/callback/saml/{providerId} - SAML callback
 * - GET /sso/callback/oauth/{providerId} - OAuth callback
 * - POST /sso/logout - Logout
 * - POST /sso/validate - Validate session token
 */
fun Route.ssoRoutes(ssoService: SsoService) {
    
    // ============================================================================
    // SSO PROVIDER MANAGEMENT (requires tenant context)
    // ============================================================================
    
    route("/sso/providers") {
        
        /**
         * Create SSO provider.
         *
         * POST /sso/providers
         * Body: { name, type, metadata }
         * Response: { provider }
         */
        post {
            try {
                val tenantContext = call.tenantContext()
                
                // Check permissions (OWNER only)
                if (!tenantContext.canManageBilling()) {
                    call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Insufficient permissions"))
                    return@post
                }
                
                val request = call.receive<CreateSsoProviderRequest>()
                
                // Parse provider type
                val providerType = try {
                    SsoProviderType.valueOf(request.type.uppercase())
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid provider type"))
                    return@post
                }
                
                // Parse metadata based on type
                val metadata = parseProviderMetadata(request.metadata, providerType)
                
                val provider = ssoService.createProvider(
                    SsoProvider(
                        tenantId = tenantContext.tenantId,
                        name = request.name,
                        type = providerType,
                        enabled = request.enabled ?: true,
                        metadata = metadata
                    )
                )
                
                logger.info("Created SSO provider: id=${provider.id}, type=${provider.type}")
                
                call.respond(HttpStatusCode.Created, provider.toDTO())
                
            } catch (e: Exception) {
                logger.error("Failed to create SSO provider", e)
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }
        
        /**
         * List SSO providers.
         *
         * GET /sso/providers
         * Response: [{ provider }, ...]
         */
        get {
            try {
                val tenantContext = call.tenantContext()
                
                val providers = ssoService.listProviders(tenantContext.tenantId)
                
                call.respond(HttpStatusCode.OK, providers.map { it.toDTO() })
                
            } catch (e: Exception) {
                logger.error("Failed to list SSO providers", e)
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }
        
        /**
         * Get SSO provider.
         *
         * GET /sso/providers/{id}
         * Response: { provider }
         */
        get("/{id}") {
            try {
                val tenantContext = call.tenantContext()
                
                val providerId = call.parameters["id"]?.toLongOrNull()
                    ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid provider ID"))
                
                val provider = ssoService.getProvider(providerId)
                    ?: return@get call.respond(HttpStatusCode.NotFound, mapOf("error" to "Provider not found"))
                
                // Check tenant ownership
                if (provider.tenantId != tenantContext.tenantId) {
                    call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Access denied"))
                    return@get
                }
                
                call.respond(HttpStatusCode.OK, provider.toDTO())
                
            } catch (e: Exception) {
                logger.error("Failed to get SSO provider", e)
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }
        
        /**
         * Update SSO provider.
         *
         * PUT /sso/providers/{id}
         * Body: { name?, enabled?, metadata? }
         * Response: { provider }
         */
        put("/{id}") {
            try {
                val tenantContext = call.tenantContext()
                
                if (!tenantContext.canManageBilling()) {
                    call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Insufficient permissions"))
                    return@put
                }
                
                val providerId = call.parameters["id"]?.toLongOrNull()
                    ?: return@put call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid provider ID"))
                
                val provider = ssoService.getProvider(providerId)
                    ?: return@put call.respond(HttpStatusCode.NotFound, mapOf("error" to "Provider not found"))
                
                if (provider.tenantId != tenantContext.tenantId) {
                    call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Access denied"))
                    return@put
                }
                
                val request = call.receive<UpdateSsoProviderRequest>()
                
                val updatedProvider = provider.copy(
                    name = request.name ?: provider.name,
                    enabled = request.enabled ?: provider.enabled,
                    metadata = if (request.metadata != null) {
                        parseProviderMetadata(request.metadata, provider.type)
                    } else {
                        provider.metadata
                    }
                )
                
                val result = ssoService.updateProvider(updatedProvider)
                
                call.respond(HttpStatusCode.OK, result.toDTO())
                
            } catch (e: Exception) {
                logger.error("Failed to update SSO provider", e)
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }
        
        /**
         * Delete SSO provider.
         *
         * DELETE /sso/providers/{id}
         * Response: 204 No Content
         */
        delete("/{id}") {
            try {
                val tenantContext = call.tenantContext()
                
                if (!tenantContext.canManageBilling()) {
                    call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Insufficient permissions"))
                    return@delete
                }
                
                val providerId = call.parameters["id"]?.toLongOrNull()
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid provider ID"))
                
                val provider = ssoService.getProvider(providerId)
                    ?: return@delete call.respond(HttpStatusCode.NotFound, mapOf("error" to "Provider not found"))
                
                if (provider.tenantId != tenantContext.tenantId) {
                    call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Access denied"))
                    return@delete
                }
                
                ssoService.deleteProvider(providerId)
                
                call.respond(HttpStatusCode.NoContent)
                
            } catch (e: Exception) {
                logger.error("Failed to delete SSO provider", e)
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }
    }
    
    // ============================================================================
    // SSO AUTHENTICATION (public endpoints)
    // ============================================================================
    
    /**
     * Initiate SSO login.
     *
     * GET /sso/login/{providerId}
     * Response: Redirect to IdP
     */
    get("/sso/login/{providerId}") {
        try {
            val providerId = call.parameters["providerId"]?.toLongOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid provider ID"))
            
            val provider = ssoService.getProvider(providerId)
                ?: return@get call.respond(HttpStatusCode.NotFound, mapOf("error" to "Provider not found"))
            
            if (!provider.enabled) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Provider is disabled"))
                return@get
            }
            
            // Generate redirect URL based on provider type
            val redirectUrl = when (provider.type) {
                SsoProviderType.SAML -> {
                    val metadata = provider.metadata as SsoProviderMetadata.Saml
                    buildSamlRedirectUrl(metadata, providerId)
                }
                SsoProviderType.OAUTH, SsoProviderType.OIDC -> {
                    val metadata = provider.metadata as SsoProviderMetadata.OAuth
                    buildOAuthRedirectUrl(metadata, providerId)
                }
            }
            
            logger.info("Initiating SSO login: providerId=$providerId, type=${provider.type}")
            
            call.respondRedirect(redirectUrl)
            
        } catch (e: Exception) {
            logger.error("Failed to initiate SSO login", e)
            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
        }
    }
    
    /**
     * SAML callback.
     *
     * POST /sso/callback/saml/{providerId}
     * Body: SAMLResponse=<base64_encoded_response>
     * Response: { sessionToken, user }
     */
    post("/sso/callback/saml/{providerId}") {
        try {
            val providerId = call.parameters["providerId"]?.toLongOrNull()
                ?: return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid provider ID"))
            
            val formParameters = call.receiveParameters()
            val samlResponse = formParameters["SAMLResponse"]
                ?: return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing SAMLResponse"))
            
            val ipAddress = call.request.local.remoteHost
            val userAgent = call.request.headers["User-Agent"]
            
            val result = ssoService.handleSamlResponse(
                providerId = providerId,
                samlResponse = samlResponse,
                ipAddress = ipAddress,
                userAgent = userAgent
            )
            
            when (result) {
                is SsoAuthResult.Success -> {
                    respondAuthCallbackHtml(
                        sessionToken = result.session.sessionToken,
                        user = result.user.toDTO(),
                        call = call
                    )
                }
                is SsoAuthResult.Failure -> {
                    respondAuthErrorHtml(result.reason, call)
                }
            }
            
        } catch (e: SsoException) {
            logger.warn("SAML authentication failed: ${e.message}")
            respondAuthErrorHtml(e.message ?: "SAML authentication failed", call)
        } catch (e: Exception) {
            logger.error("SAML callback error", e)
            respondAuthErrorHtml("Authentication failed", call)
        }
    }
    
    /**
     * OAuth callback.
     *
     * GET /sso/callback/oauth/{providerId}?code=<authorization_code>
     * Response: { sessionToken, user }
     */
    get("/sso/callback/oauth/{providerId}") {
        try {
            val providerId = call.parameters["providerId"]?.toLongOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid provider ID"))
            
            val code = call.request.queryParameters["code"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing authorization code"))
            
            val ipAddress = call.request.local.remoteHost
            val userAgent = call.request.headers["User-Agent"]
            
            val result = ssoService.handleOAuthCallback(
                providerId = providerId,
                code = code,
                ipAddress = ipAddress,
                userAgent = userAgent
            )
            
            when (result) {
                is SsoAuthResult.Success -> {
                    respondAuthCallbackHtml(
                        sessionToken = result.session.sessionToken,
                        user = result.user.toDTO(),
                        call = call
                    )
                }
                is SsoAuthResult.Failure -> {
                    respondAuthErrorHtml(result.reason, call)
                }
            }
            
        } catch (e: SsoException) {
            logger.warn("OAuth authentication failed: ${e.message}")
            respondAuthErrorHtml(e.message ?: "OAuth authentication failed", call)
        } catch (e: Exception) {
            logger.error("OAuth callback error", e)
            respondAuthErrorHtml("Authentication failed", call)
        }
    }
    
    /**
     * Logout.
     *
     * POST /sso/logout
     * Header: Authorization: Bearer <sessionToken>
     * Response: 204 No Content
     */
    post("/sso/logout") {
        try {
            val authHeader = call.request.headers["Authorization"]
            val sessionToken = authHeader?.removePrefix("Bearer ")?.trim()
                ?: return@post call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Missing session token"))
            
            ssoService.logout(sessionToken)
            
            call.respond(HttpStatusCode.NoContent)
            
        } catch (e: Exception) {
            logger.error("Logout error", e)
            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Logout failed"))
        }
    }
    
    /**
     * Validate session token.
     *
     * POST /sso/validate
     * Body: { sessionToken }
     * Response: { valid, session?, user? }
     */
    post("/sso/validate") {
        try {
            val request = call.receive<ValidateTokenRequest>()
            
            val session = ssoService.validateToken(request.sessionToken)
            
            if (session != null) {
                call.respond(
                    HttpStatusCode.OK,
                    mapOf(
                        "valid" to true,
                        "session" to session.toDTO()
                    )
                )
            } else {
                call.respond(
                    HttpStatusCode.OK,
                    mapOf("valid" to false)
                )
            }
            
        } catch (e: Exception) {
            logger.error("Token validation error", e)
            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Validation failed"))
        }
    }
}

// ============================================================================
// HELPER FUNCTIONS
// ============================================================================

/**
 * Build SAML redirect URL.
 *
 * Generates SAML AuthnRequest and builds redirect URL to IdP.
 * Note: This is a simplified implementation. For production with
 * enterprise IdPs, use opensaml-java for full SAML 2.0 compliance.
 */
private fun buildSamlRedirectUrl(metadata: SsoProviderMetadata.Saml, providerId: Long): String {
    // Build minimal SAML AuthnRequest XML
    val requestId = "_${java.util.UUID.randomUUID()}"
    val issueInstant = java.time.Instant.now().toString()
    val acsUrl = "http://localhost:18000/sso/callback/saml/$providerId"
    
    val authnRequest = """
        <samlp:AuthnRequest
            xmlns:samlp="urn:oasis:names:tc:SAML:2.0:protocol"
            xmlns:saml="urn:oasis:names:tc:SAML:2.0:assertion"
            ID="$requestId"
            Version="2.0"
            IssueInstant="$issueInstant"
            AssertionConsumerServiceURL="$acsUrl"
            Destination="${metadata.ssoUrl}">
            <saml:Issuer>${metadata.entityId}</saml:Issuer>
        </samlp:AuthnRequest>
    """.trimIndent()
    
    // Encode as base64
    val encodedRequest = Base64.getEncoder().encodeToString(authnRequest.toByteArray())
    
    // URL encode
    val urlEncodedRequest = java.net.URLEncoder.encode(encodedRequest, "UTF-8")
    
    // Build redirect URL
    return "${metadata.ssoUrl}?SAMLRequest=$urlEncodedRequest&RelayState=$providerId"
}

/**
 * Build OAuth redirect URL.
 */
private fun buildOAuthRedirectUrl(metadata: SsoProviderMetadata.OAuth, providerId: Long): String {
    val params = mapOf(
        "client_id" to metadata.clientId,
        "response_type" to "code",
        "redirect_uri" to "http://localhost:18000/sso/callback/oauth/$providerId",
        "scope" to metadata.scopes.joinToString(" "),
        "state" to java.util.UUID.randomUUID().toString()
    )
    
    val queryString = params.entries.joinToString("&") { "${it.key}=${java.net.URLEncoder.encode(it.value, "UTF-8")}" }
    
    return "${metadata.authorizationUrl}?$queryString"
}

/**
 * Parse provider metadata from JSON.
 */
private fun parseProviderMetadata(json: String, type: SsoProviderType): SsoProviderMetadata {
    return when (type) {
        SsoProviderType.SAML -> Json.decodeFromString<SsoProviderMetadata.Saml>(json)
        SsoProviderType.OAUTH -> Json.decodeFromString<SsoProviderMetadata.OAuth>(json)
        SsoProviderType.OIDC -> Json.decodeFromString<SsoProviderMetadata.Oidc>(json)
    }
}

/** Escape string for use inside a JSON string value. */
private fun escapeJsonString(s: String): String =
    s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t")

/**
 * Respond with HTML that stores sessionToken and user in localStorage, then redirects.
 * Used by SSO callbacks so the SPA receives auth state after IdP redirect.
 */
private suspend fun respondAuthCallbackHtml(
    sessionToken: String,
    user: TenantUserDTO,
    call: io.ktor.server.application.ApplicationCall
) {
    val name = user.email.substringBefore("@").takeIf { it.isNotBlank() } ?: ""
    val userJson = """{"id":"${user.id}","email":"${escapeJsonString(user.email)}","name":"${escapeJsonString(name)}"}"""
    val payload = """{"sessionToken":"${escapeJsonString(sessionToken)}","user":$userJson}"""
    val safePayload = payload.replace("</script>", "<\\/script>")
    val redirectUrl = AppConfig.ssoCallbackRedirectUrl.replace("\\", "\\\\").replace("\"", "\\\"")
    val html = """
<!DOCTYPE html>
<html><head><meta charset="UTF-8"><title>Signing in…</title></head><body>
<script type="application/json" id="auth-payload">$safePayload</script>
<script>
(function(){
  var el = document.getElementById('auth-payload');
  var d = JSON.parse(el.textContent);
  try { localStorage.setItem('auth_token', d.sessionToken); } catch (e) {}
  try { localStorage.setItem('current_user', JSON.stringify(d.user)); } catch (e) {}
  window.location.replace("$redirectUrl");
})();
</script>
<p>Signing in…</p>
</body></html>
    """.trimIndent()
    call.respondText(html, ContentType.Text.Html)
}

/**
 * Respond with HTML that shows auth error and link to login.
 */
private suspend fun respondAuthErrorHtml(message: String, call: io.ktor.server.application.ApplicationCall) {
    val safeMsg = message.replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")
    val html = """
<!DOCTYPE html>
<html><head><meta charset="UTF-8"><title>Sign-in failed</title></head><body>
<p>Sign-in failed: $safeMsg</p>
<p><a href="/login">Back to login</a></p>
</body></html>
    """.trimIndent()
    call.respondText(html, ContentType.Text.Html, HttpStatusCode.Unauthorized)
}
