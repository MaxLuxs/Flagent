package com.flagent.enhanced.platform

import com.flagent.enhanced.storage.FileSnapshotStorage
import com.flagent.enhanced.storage.SnapshotStorage
import java.io.File

actual fun createPersistentStorage(path: String): SnapshotStorage =
    FileSnapshotStorage(File(path))
