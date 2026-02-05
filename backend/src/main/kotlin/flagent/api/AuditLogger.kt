package flagent.api

import io.ktor.server.application.ApplicationCall

/**
 * Optional audit logger for enterprise. When enterprise is present, it registers an implementation.
 * Backend calls this after mutations. No-op when not set.
 * @param call current ApplicationCall (may be null for background ops); implementation extracts tenantId/actor
 */
interface AuditLogger {
    fun log(call: ApplicationCall?, action: String, resource: String, details: Map<String, Any?>?)
}

object AuditLoggerRegistry {
    var logger: AuditLogger? = null
}

object TenantIdProvider {
    var getter: (io.ktor.server.application.ApplicationCall) -> Long? = { null }
}
