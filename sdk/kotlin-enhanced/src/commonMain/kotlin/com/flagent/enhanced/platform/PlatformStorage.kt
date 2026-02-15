package com.flagent.enhanced.platform

import com.flagent.enhanced.storage.SnapshotStorage

/**
 * Create persistent snapshot storage for the given directory path.
 * JVM/Android: file-based storage; iOS: in-memory or platform file storage.
 */
expect fun createPersistentStorage(path: String): SnapshotStorage

/**
 * Default directory path for persistent storage when [OfflineFlagentConfig.storagePath] is null.
 * JVM: user.home/.flagent; Android/iOS: empty string (app should set storagePath or uses in-memory).
 */
expect fun defaultStoragePath(): String
