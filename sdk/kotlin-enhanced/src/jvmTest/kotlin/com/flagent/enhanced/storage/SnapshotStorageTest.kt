package com.flagent.enhanced.storage

import com.flagent.enhanced.model.FlagSnapshot
import com.flagent.enhanced.model.LocalFlag
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File

class SnapshotStorageTest {

    private fun createTestSnapshot(
        fetchedAt: Long = System.currentTimeMillis(),
        ttlMs: Long = 300_000
    ): FlagSnapshot {
        return FlagSnapshot(
            flags = mapOf(
                1L to LocalFlag(
                    id = 1L,
                    key = "test_flag",
                    enabled = true,
                    segments = emptyList(),
                    variants = emptyList()
                )
            ),
            revision = "test-rev-1",
            fetchedAt = fetchedAt,
            ttlMs = ttlMs
        )
    }

    @Test
    fun `InMemorySnapshotStorage save and load returns same snapshot`() = runBlocking {
        val storage = InMemorySnapshotStorage()
        val snapshot = createTestSnapshot()

        storage.save(snapshot)
        val loaded = storage.load()

        assertNotNull(loaded)
        assertEquals(snapshot.revision, loaded?.revision)
        assertEquals(snapshot.flags.size, loaded?.flags?.size)
        assertEquals("test_flag", loaded?.flags?.get(1L)?.key)
    }

    @Test
    fun `InMemorySnapshotStorage load returns null when empty`() = runBlocking {
        val storage = InMemorySnapshotStorage()

        val loaded = storage.load()

        assertNull(loaded)
    }

    @Test
    fun `InMemorySnapshotStorage clear removes snapshot`() = runBlocking {
        val storage = InMemorySnapshotStorage()
        storage.save(createTestSnapshot())

        storage.clear()
        val loaded = storage.load()

        assertNull(loaded)
    }

    @Test
    fun `InMemorySnapshotStorage load returns null when snapshot expired`() = runBlocking {
        val storage = InMemorySnapshotStorage()
        val expiredSnapshot = createTestSnapshot(
            fetchedAt = System.currentTimeMillis() - 400_000,
            ttlMs = 300_000
        )
        storage.save(expiredSnapshot)

        val loaded = storage.load()

        assertNull(loaded)
    }

    @Test
    fun `InMemorySnapshotStorage hasValidSnapshot returns true when valid`() = runBlocking {
        val storage = InMemorySnapshotStorage()
        storage.save(createTestSnapshot())

        assertTrue(storage.hasValidSnapshot())
    }

    @Test
    fun `InMemorySnapshotStorage hasValidSnapshot returns false when empty`() = runBlocking {
        val storage = InMemorySnapshotStorage()

        assertFalse(storage.hasValidSnapshot())
    }

    @Test
    fun `InMemorySnapshotStorage hasValidSnapshot returns false when expired`() = runBlocking {
        val storage = InMemorySnapshotStorage()
        storage.save(
            createTestSnapshot(
                fetchedAt = System.currentTimeMillis() - 400_000,
                ttlMs = 300_000
            )
        )

        assertFalse(storage.hasValidSnapshot())
    }

    @Test
    fun `FileSnapshotStorage save and load returns same snapshot`() = runBlocking {
        val dir = File.createTempFile("flagent_storage", "").apply { delete(); mkdirs() }
        try {
            val storage = FileSnapshotStorage(dir)
            val snapshot = createTestSnapshot()
            storage.save(snapshot)
            val loaded = storage.load()
            assertNotNull(loaded)
            assertEquals(snapshot.revision, loaded?.revision)
            assertEquals(snapshot.flags.size, loaded?.flags?.size)
            assertEquals("test_flag", loaded?.flags?.get(1L)?.key)
        } finally {
            dir.deleteRecursively()
        }
    }

    @Test
    fun `FileSnapshotStorage load returns null when file does not exist`() = runTest {
        val dir = File.createTempFile("flagent_storage", "").apply { delete(); mkdirs() }
        try {
            val storage = FileSnapshotStorage(dir)

            val loaded = storage.load()
            assertNull(loaded)
        } finally {
            dir.deleteRecursively()
        }
    }

    @Test
    fun `FileSnapshotStorage clear removes file`() = runTest {
        val dir = File.createTempFile("flagent_storage", "").apply { delete(); mkdirs() }
        try {
            val storage = FileSnapshotStorage(dir)
            storage.save(createTestSnapshot())
            storage.clear()
            val loaded = storage.load()
            assertNull(loaded)
            assertFalse(File(dir, "flagent_snapshot.json").exists())
        } finally {
            dir.deleteRecursively()
        }
    }

    @Test
    fun `FileSnapshotStorage load returns null when snapshot expired`() = runTest {
        val dir = File.createTempFile("flagent_storage", "").apply { delete(); mkdirs() }
        try {
            val storage = FileSnapshotStorage(dir)
            storage.save(
                createTestSnapshot(
                    fetchedAt = System.currentTimeMillis() - 400_000,
                    ttlMs = 300_000
                )
            )
            val loaded = storage.load()
            assertNull(loaded)
        } finally {
            dir.deleteRecursively()
        }
    }

    @Test
    fun `FileSnapshotStorage hasValidSnapshot returns true when valid`() = runBlocking {
        val dir = File.createTempFile("flagent_storage", "").apply { delete(); mkdirs() }
        try {
            val storage = FileSnapshotStorage(dir)
            storage.save(createTestSnapshot())
            assertTrue(storage.hasValidSnapshot())
        } finally {
            dir.deleteRecursively()
        }
    }

    @Test
    fun `FileSnapshotStorage hasValidSnapshot returns false when empty`() = runTest {
        val dir = File.createTempFile("flagent_storage", "").apply { delete(); mkdirs() }
        try {
            val storage = FileSnapshotStorage(dir)
            assertFalse(storage.hasValidSnapshot())
        } finally {
            dir.deleteRecursively()
        }
    }

    @Test
    fun `FileSnapshotStorage load returns null on invalid JSON`() = runTest {
        val dir = File.createTempFile("flagent_storage", "").apply { delete(); mkdirs() }
        try {
            val snapshotFile = File(dir, "flagent_snapshot.json")
            snapshotFile.writeText("not valid json {")
            val storage = FileSnapshotStorage(dir)
            val loaded = storage.load()
            assertNull(loaded)
        } finally {
            dir.deleteRecursively()
        }
    }
}
