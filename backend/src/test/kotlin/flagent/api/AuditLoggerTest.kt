package flagent.api

import io.ktor.server.application.ApplicationCall
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNull

class AuditLoggerTest {

    @BeforeTest
    fun resetRegistry() {
        AuditLoggerRegistry.logger = null
        TenantIdProvider.getter = { null }
    }

    @AfterTest
    fun cleanup() {
        AuditLoggerRegistry.logger = null
        TenantIdProvider.getter = { null }
    }

    @Test
    fun `AuditLoggerRegistry when null does not throw on access`() {
        assertNull(AuditLoggerRegistry.logger)
    }

    @Test
    fun `AuditLoggerRegistry when set delegates log call`() {
        val logger = mockk<AuditLogger>(relaxed = true)
        AuditLoggerRegistry.logger = logger

        AuditLoggerRegistry.logger?.log(call = null, action = "create", resource = "flag", details = mapOf("id" to 1))

        verify(exactly = 1) { logger.log(null, "create", "flag", mapOf("id" to 1)) }
    }

    @Test
    fun `TenantIdProvider getter returns value when set`() {
        val call = mockk<ApplicationCall>()
        TenantIdProvider.getter = { 42L }

        val result = TenantIdProvider.getter(call)

        kotlin.test.assertEquals(42L, result)
    }

    @Test
    fun `TenantIdProvider getter returns null by default`() {
        TenantIdProvider.getter = { null }
        val call = mockk<ApplicationCall>()
        every { call.toString() } returns "call"

        val result = TenantIdProvider.getter(call)

        assertNull(result)
    }
}
