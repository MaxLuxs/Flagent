package com.flagent.enhanced.platform

import com.flagent.enhanced.storage.InMemorySnapshotStorage
import com.flagent.enhanced.storage.SnapshotStorage

actual fun createPersistentStorage(path: String): SnapshotStorage =
    InMemorySnapshotStorage()
