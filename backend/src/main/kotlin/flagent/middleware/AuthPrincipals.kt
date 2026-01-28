package flagent.middleware

import io.ktor.server.auth.Principal

/**
 * Principal for SSO/JWT auth: tenant ID from token (used by Billing, etc.).
 */
data class UserPrincipal(val tenantId: Long) : Principal
