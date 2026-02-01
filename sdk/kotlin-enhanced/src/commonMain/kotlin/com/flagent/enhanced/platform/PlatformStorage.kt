package com.flagent.enhanced.platform

import com.flagent.enhanced.storage.SnapshotStorage

/**
 * Create persistent snapshot storage for the given directory path.
 * JVM: file-based storage; iOS: in-memory or platform file storage.
 */
expect fun createPersistentStorage(path: String): SnapshotStorage
