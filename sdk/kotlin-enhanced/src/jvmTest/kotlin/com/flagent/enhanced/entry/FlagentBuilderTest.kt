package com.flagent.enhanced.entry

import com.flagent.client.models.EvaluationEntity
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FlagentBuilderTest {

    @Test
    fun `builder returns FlagentClient in SERVER mode`() {
        val client = Flagent.builder()
            .baseUrl("http://localhost:18000/api/v1")
            .cache(true, 60_000L)
            .build()
        assertNotNull(client)
        assertTrue(client is FlagentManagerAdapter)
    }

    @Test
    fun `builder returns FlagentClient in OFFLINE mode`() {
        val client = Flagent.builder()
            .baseUrl("http://localhost:18000/api/v1")
            .mode(FlagentMode.OFFLINE)
            .build()
        assertNotNull(client)
        assertTrue(client is OfflineFlagentManagerAdapter)
    }

    @Test
    fun `offlineSupport true sets OFFLINE mode`() {
        val client = Flagent.builder()
            .baseUrl("http://test")
            .offlineSupport(true)
            .build()
        assertTrue(client is OfflineFlagentManagerAdapter)
    }

    @Test
    fun `offlineSupport false keeps SERVER mode`() {
        val client = Flagent.builder()
            .baseUrl("http://test")
            .offlineSupport(false)
            .build()
        assertTrue(client is FlagentManagerAdapter)
    }

    @Test
    fun `initialize is no-op for server client`() = runBlocking {
        val client = Flagent.builder().baseUrl("http://localhost:18000/api/v1").build()
        client.initialize()
        client.initialize(forceRefresh = true)
        // no exception
    }

    @Test
    fun `buildBlocking returns blocking client`() {
        val blocking = Flagent.builder()
            .baseUrl("http://localhost:18000/api/v1")
            .buildBlocking()
        assertNotNull(blocking)
        blocking.initialize()
        // no exception
    }
}
