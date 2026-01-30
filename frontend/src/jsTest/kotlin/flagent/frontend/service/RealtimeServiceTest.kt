package flagent.frontend.service

import kotlin.test.Test
import kotlin.test.assertEquals

class RealtimeServiceTest {

    @Test
    fun testDisconnectBeforeConnectDoesNotThrow() {
        var connectionState: Boolean? = null
        val service = RealtimeService(
            onEvent = {},
            onConnectionChange = { connectionState = it }
        )
        service.disconnect()
        assertEquals(false, connectionState)
    }
}
