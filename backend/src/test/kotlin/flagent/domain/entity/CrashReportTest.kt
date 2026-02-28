package flagent.domain.entity

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CrashReportTest {

    @Test
    fun `CrashReport with required fields`() {
        val r = CrashReport(
            stackTrace = "at foo.bar",
            message = "NPE",
            platform = "android",
            timestamp = 12345L
        )
        assertEquals(0L, r.id)
        assertEquals("at foo.bar", r.stackTrace)
        assertEquals("NPE", r.message)
        assertEquals("android", r.platform)
        assertEquals(12345L, r.timestamp)
        assertNull(r.appVersion)
        assertNull(r.deviceInfo)
        assertNull(r.breadcrumbs)
        assertNull(r.customKeys)
        assertNull(r.activeFlagKeys)
        assertNull(r.tenantId)
    }

    @Test
    fun `CrashReport with optional fields`() {
        val r = CrashReport(
            id = 1L,
            stackTrace = "trace",
            message = "msg",
            platform = "ios",
            appVersion = "1.0",
            deviceInfo = "iPhone",
            breadcrumbs = "[]",
            customKeys = "{}",
            activeFlagKeys = listOf("flag_a"),
            timestamp = 999L,
            tenantId = "t1"
        )
        assertEquals(1L, r.id)
        assertEquals("1.0", r.appVersion)
        assertEquals(listOf("flag_a"), r.activeFlagKeys)
        assertEquals("t1", r.tenantId)
    }
}
