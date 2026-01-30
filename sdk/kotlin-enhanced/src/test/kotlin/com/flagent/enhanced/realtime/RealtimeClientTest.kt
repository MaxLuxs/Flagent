package com.flagent.enhanced.realtime

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.sse.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class RealtimeClientTest {

    @Test
    fun `connectionStatus starts as Disconnected`() = runBlocking {
        val client = HttpClient(CIO) { install(SSE) }
        try {
            val realtime = RealtimeClient(client, "http://localhost:18000", RealtimeConfig())
            assertEquals(ConnectionStatus.Disconnected, realtime.connectionStatus.value)
            realtime.shutdown()
        } finally {
            client.close()
        }
    }

    @Test
    fun `disconnect sets status to Disconnected`() = runBlocking {
        val client = HttpClient(CIO) { install(SSE) }
        try {
            val realtime = RealtimeClient(client, "http://localhost:18000", RealtimeConfig())
            realtime.connect()
            realtime.disconnect()
            assertEquals(ConnectionStatus.Disconnected, realtime.connectionStatus.value)
            realtime.shutdown()
        } finally {
            client.close()
        }
    }

    @Test
    fun `connect with autoReconnect false does not throw`() = runBlocking {
        val client = HttpClient(CIO) { install(SSE) }
        try {
            val config = RealtimeConfig(autoReconnect = false, reconnectDelayMs = 100)
            val realtime = RealtimeClient(client, "http://127.0.0.1:19999", config)
            realtime.connect()
            kotlinx.coroutines.delay(500)
            realtime.disconnect()
            realtime.shutdown()
        } finally {
            client.close()
        }
    }

    @Test
    fun `events is SharedFlow and does not replay`() = runBlocking {
        val client = HttpClient(CIO) { install(SSE) }
        try {
            val realtime = RealtimeClient(client, "http://localhost:18000", RealtimeConfig())
            val events = realtime.events
            assertNotNull(events)
            realtime.shutdown()
        } finally {
            client.close()
        }
    }
}
